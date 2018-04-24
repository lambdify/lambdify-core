package lambdify.mojo;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.model.*;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.*;
import com.amazonaws.services.lambda.model.Runtime;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.model.*;
import lombok.experimental.var;
import lombok.val;

class AWS {

	AWSLambda lambda;
	AmazonApiGateway apiGateway;
	AWSSecurityTokenService sts;

	String getMyAccountId(){
		try {
			final GetCallerIdentityResult identity = sts.getCallerIdentity(new GetCallerIdentityRequest());
			return identity.getAccount();
		} finally {
			await();
		}
	}

	RestApi getRestApi(String name ){
		try {
			final List<RestApi> items = apiGateway.getRestApis(new GetRestApisRequest()).getItems();
			return first( items, i-> name.equals(i.getName()) );
		} finally {
			await();
		}
	}

	AddPermissionResult addPermissionToInvokeLambdaFunctions(String functionName, String sourceArn) {
		removePermissionToInvokeLambdaFunction( functionName );
		try {
			final AddPermissionRequest request = new AddPermissionRequest().withPrincipal("apigateway.amazonaws.com")
				.withFunctionName(functionName).withStatementId(functionName + "-lambda-InvokeFunction")
				.withAction("lambda:InvokeFunction").withSourceArn(sourceArn);
			return lambda.addPermission( request );
		} finally {
			await();
		}
	}

	private boolean removePermissionToInvokeLambdaFunction(String functionName ) {
		try {
			final RemovePermissionRequest request = new RemovePermissionRequest()
					.withFunctionName(functionName).withStatementId(functionName + "-lambda-InvokeFunction");
			lambda.removePermission(request);
			return true;
		} catch ( ResourceNotFoundException cause ) {
			return false;
		} finally {
			await();
		}
	}

	DeleteRestApiResult deleteRestApi(String id) {
		try {
			final DeleteRestApiRequest request = new DeleteRestApiRequest().withRestApiId(id);
			return apiGateway.deleteRestApi( request );
		} finally {
			await();
		}
	}

	PutIntegrationResult assignLambdaToResource( String restApiID, String resourceId, String functionArn, String regionName ) {
		try {
			final PutIntegrationRequest request = new PutIntegrationRequest()
				.withRestApiId(restApiID).withResourceId(resourceId)
				.withUri("arn:aws:apigateway:"+ regionName +":lambda:path/2015-03-31/functions/"+ functionArn +"/invocations")
				.withHttpMethod("ANY").withType(IntegrationType.AWS_PROXY)
				.withIntegrationHttpMethod("POST");
			return apiGateway.putIntegration( request );
		} finally {
			await();
		}
	}

	GetFunctionResult getFunction(String name ) {
		try {
			final GetFunctionRequest request = new GetFunctionRequest().withFunctionName(name);
			return lambda.getFunction( request );
		} finally {
			await();
		}
	}

    CreateFunctionResult createFunction( String name, String handlerClass, String s3Bucket, String s3Key, int timeout, int memory, String lambdaRole ) {
		try {
			final FunctionCode functionCode = new FunctionCode().withS3Bucket( s3Bucket ).withS3Key( s3Key );
			final CreateFunctionRequest request = new CreateFunctionRequest().withFunctionName(name).withCode(functionCode).withRole(lambdaRole)
				.withRuntime(Runtime.Java8).withHandler( handlerClass )
				.withTimeout(timeout).withMemorySize(memory);
			return lambda.createFunction( request );
		} finally {
			await();
		}
	}

    UpdateFunctionCodeResult updateFunction( String name, String s3Bucket, String s3Key ) {
		try {
			final UpdateFunctionCodeRequest request = new UpdateFunctionCodeRequest()
                    .withFunctionName(name).withS3Bucket(s3Bucket).withS3Key(s3Key);
			return lambda.updateFunctionCode( request );
		} finally {
			await();
		}
	}

    DeleteFunctionResult deleteFunction(String lambdaFunctionName) {
	    try {
            val request = new DeleteFunctionRequest().withFunctionName(lambdaFunctionName);
            return lambda.deleteFunction(request);
        } finally {
	        await();
        }
    }

	CreateRestApiResult createRestApi( String name ){
		try {
			final CreateRestApiRequest request = new CreateRestApiRequest().withName(name);
			return apiGateway.createRestApi( request );
		} finally {
			await();
		}
	}

	PutMethodResult putMethod( String restApiId, String resourceId ){
		try {
			final PutMethodRequest request = new PutMethodRequest()
					.withHttpMethod("ANY").withAuthorizationType("NONE")
					.withRestApiId(restApiId).withResourceId(resourceId);
			return apiGateway.putMethod( request );
		} finally {
			await();
		}
	}

    public String getRootResourceId(String restApiID ) {
        return findFirstResource(restApiID, r -> "/".equals(r.getPath())).getId();
    }

    public Resource findFirstResource(String restApiID, Function<Resource, Boolean> condition) {
        val resources = listResources( restApiID );
        resources.sort( Comparator.comparing(Resource::getPath).reversed() );
        return first( resources, condition );
    }

	public List<Resource> listResources(String restApiID){
        try {
            final GetResourcesRequest request = new GetResourcesRequest()
                    .withRestApiId(restApiID).withLimit(500);
            return apiGateway.getResources( request ).getItems();
        } finally {
            await();
        }
    }

    public CreateResourceResult createResourcePath( String restApiID, String path ) {
        val root = findFirstResource( restApiID, r -> path.contains(r.getPath()) );

        if ( root.getPath().equals( path ) ) {
            return new CreateResourceResult().withId( root.getId() )
                .withParentId( root.getParentId() ).withPath( root.getPath() )
                .withPathPart( root.getPathPart() ).withResourceMethods( root.getResourceMethods() );
        }

        val tokens = path.replace( root.getPath(), "" ).replaceFirst("/$","").replaceFirst("^/","").split("/");
        var rootId = root.getId();
        var result = (CreateResourceResult)null;
        for ( val token : tokens ) {
            result = createResource( rootId, restApiID, token );
            rootId = result.getId();
        }
        return result;
    }

	public CreateResourceResult createProxyResource(String resourceId, String restApiID) {
		return createResource( resourceId, restApiID, "{proxy+}" );
	}

    public CreateResourceResult createResource(String resourceId, String restApiID, String path) {
        try {
            final CreateResourceRequest request = new CreateResourceRequest()
                .withParentId(resourceId).withRestApiId(restApiID).withPathPart( path );
            return apiGateway.createResource( request );
        } finally {
            await();
        }
    }

	public CreateDeploymentResult deployFunction(String restApiID) {
		try {
			final CreateDeploymentRequest request = new CreateDeploymentRequest().withRestApiId(restApiID).withStageName("Production");
			return apiGateway.createDeployment(request);
		} finally {
			await();
		}
	}

	static <T> T first( List<T> list ) {
		return list.get(0);
	}

	static <T> T first( Iterable<T> list, Function<T, Boolean> condition ) {
		for ( T obj : list )
			if ( condition.apply( obj ) )
				return obj;
		return null;
	}

	static void await() {
		try {
			Thread.sleep( 700 );
		} catch ( InterruptedException e ) {
			throw new IllegalStateException( e );
		}
	}
}