package lambdify.mojo;

import com.amazonaws.auth.*;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.apigateway.AmazonApiGatewayClient;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import org.apache.maven.plugin.*;
import org.apache.maven.project.MavenProject;

/**
 *
 */
public abstract class AWSMojo extends AbstractMojo {

	protected final AWSCredentialsProviderChain credentials = DefaultAWSCredentialsProviderChain.getInstance();
	protected final AWS aws = new AWS();

	@Override
	public void execute() throws MojoFailureException, MojoExecutionException {
		try {
			if ( !getProject().getPackaging().equals( "jar" ) || !getEnabled() ) return;
			configureAWS();
			run();
		} catch ( MojoExecutionException | MojoFailureException e ) {
			throw e;
		} catch ( RuntimeException e ) {
			throw new MojoExecutionException( e.getMessage(), e );
		} catch ( Exception e ) {
			throw new MojoFailureException( "Unhandled failure: " + e.getMessage(), e );
		}
	}

	protected abstract boolean getEnabled();

	protected abstract MavenProject getProject();

	private void configureAWS() {
		aws.lambda = AWSLambdaClient.builder().withCredentials( credentials ).withRegion( Regions.fromName( getRegionName() ) ).build();
		aws.sts = AWSSecurityTokenServiceClient.builder().withCredentials( credentials ).withRegion( Regions.fromName( getRegionName() ) ).build();
		aws.apiGateway = AmazonApiGatewayClient.builder().withCredentials( credentials ).withRegion( Regions.fromName( getRegionName() ) ).build();
	}

	protected abstract String getRegionName();

	protected abstract void run() throws Exception;
}
