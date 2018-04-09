package lambdify.mojo;

import java.io.File;
import com.amazonaws.auth.*;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.apigateway.AmazonApiGatewayClient;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.ResourceNotFoundException;
import com.amazonaws.services.s3.*;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import lombok.experimental.var;
import lombok.val;
import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.*;

/**
 *
 */
@Mojo( name = "deploy-on-aws-lambda", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME )
public class LambdaDeployerMojo extends AbstractMojo {

	final AWSCredentialsProviderChain credentials = DefaultAWSCredentialsProviderChain.getInstance();
	final AWS aws = new AWS();

	@Parameter( defaultValue = "true", required = true )
	Boolean enabled;

	@Parameter( defaultValue = "true", required = true )
	Boolean createAPIEndpoints;

	@Parameter( defaultValue = "false", required = true )
	Boolean force;

	@Parameter( defaultValue = "us-east-1", required = true )
	String regionName;

	@Parameter( defaultValue = "60", required = true )
	Integer lambdaTimeout;

	@Parameter( defaultValue = "128", required = true )
	Integer lambdaMemory;

	@Parameter( defaultValue = "${project.groupId}-${project.artifactId}-${project.version}", required = true )
	String projectName;

	@Parameter( defaultValue = "${project.build.directory}", required = true )
	File targetDirectory;

	@Parameter( defaultValue = "${project.build.finalName}.jar", required = true )
	String jarFileName;

	@Parameter( defaultValue = "${project.groupId}-${project.artifactId}-${project.version}", required = true )
	String s3Key;

	@Parameter( required = true )
	String s3Bucket;

	@Parameter( required = true )
	String lambdaRole;

	@Parameter( required = true )
	String handlerClass;

	@Override
	public void execute() throws MojoFailureException {
		if ( !enabled ) return;

		configureAWS();

		val packageFile = getJarFile();
		if ( !packageFile.exists() )
			throw new MojoFailureException( "Package not found: " + packageFile.getName() );

		uploadPackage( packageFile );

		val parsedProjectName = projectName.replaceAll( "[._]", "-" );
		val lambdaFunction = setupLambdaFunction( parsedProjectName );

		if (createAPIEndpoints)
			createRestAPI( parsedProjectName, lambdaFunction );
	}

	private File getJarFile(){
		return new File( targetDirectory.getAbsolutePath() + File.separatorChar + jarFileName );
	}

	private void uploadPackage( File packageFile ) {
		getLog().info( "Deploying package on AWS S3: " + s3Bucket + "/" + s3Key );
		val s3 = AmazonS3Client.builder().withCredentials( credentials )
				.withRegion( Regions.fromName(regionName) ).build();
		s3.putObject( s3Bucket, s3Key, packageFile );
	}

	private void configureAWS() {
		aws.lambda = AWSLambdaClient.builder().withCredentials( credentials ).withRegion( Regions.fromName( regionName ) ).build();
		aws.sts = AWSSecurityTokenServiceClient.builder().withCredentials( credentials ).withRegion( Regions.fromName( regionName ) ).build();
		aws.apiGateway = AmazonApiGatewayClient.builder().withCredentials( credentials ).withRegion( Regions.fromName( regionName ) ).build();
	}

	private String setupLambdaFunction(String projectName) {
		try {
			return updateLambdaFunction( projectName );
		} catch ( ResourceNotFoundException cause ) {
			return createLambdaFunction( projectName );
		}
	}

	private String updateLambdaFunction( final String projectName ){
		val result = aws.getFunction( projectName );
		val functionArn = result.getConfiguration().getFunctionArn();
		getLog().info( "Updating AWS Lambda Function '"+projectName+"'..." );
		aws.updateFunction( functionArn, s3Bucket, s3Key  );
		return functionArn;
	}

	private String createLambdaFunction( final String projectName ){
		getLog().info( "Creating AWS Lambda Function '"+projectName+"'..." );
		val result = aws.createFunction(
				projectName, handlerClass, s3Bucket, s3Key, lambdaTimeout, lambdaMemory, lambdaRole );
		return result.getFunctionArn();
	}

	private void createRestAPI(String parsedProjectName, String lambdaFunction){
		var result = aws.getRestApi( parsedProjectName );
		if ( result != null && force ) {
			getLog().warn( "Removing REST API '" + parsedProjectName + "' API Gateway. Reason: force=true." );
			aws.deleteRestApi( result.getId() );
			result = null;
		}

		if ( result == null ) {
			val accountId = aws.getMyAccountId();
			setupApiGateway(accountId, lambdaFunction, parsedProjectName);
		}
	}

	private void setupApiGateway( String accountId, String functionArn, String projectName ) {
		getLog().info( "Creating REST API '"+ projectName +"'..." );
		val restApiID = aws.createRestApi(projectName).getId();

		getLog().info( "Pointing the all requests to lambda function '"+ functionArn +"'" );
		var resourceId = aws.getRootResourceId( restApiID );
        aws.putMethod(restApiID, resourceId);
        aws.assignLambdaToResource(restApiID, resourceId, functionArn, regionName);
        getLog().info( "Pointing the all sub-requests to lambda function '"+ functionArn +"'" );
        resourceId = aws.createProxyResource(resourceId, restApiID).getId();
		aws.putMethod(restApiID, resourceId);
		aws.assignLambdaToResource(restApiID, resourceId, functionArn, regionName);

		aws.deployFunction(restApiID);
		val sourceArn = "arn:aws:execute-api:"+regionName+":"+accountId+":"+restApiID+"/*/*/*";
		aws.addPermissionToInvokeLambdaFunctions(projectName, sourceArn);
	}
}
