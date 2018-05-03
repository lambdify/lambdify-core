package lambdify.mojo;

import com.amazonaws.auth.*;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.apigateway.AmazonApiGatewayClient;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import lombok.val;
import org.junit.jupiter.api.*;

@Disabled( "This test requires a valid API Gateway endpoint pre-configured in order to work properly." )
class AWSTest {

    final AWSCredentialsProviderChain credentials = DefaultAWSCredentialsProviderChain.getInstance();
    final AWS aws = new AWS();

    @BeforeEach
    void configureAWS() {
        val regionName = "sa-east-1";
        aws.lambda = AWSLambdaClient.builder().withCredentials( credentials ).withRegion( Regions.fromName( regionName ) ).build();
        aws.sts = AWSSecurityTokenServiceClient.builder().withCredentials( credentials ).withRegion( Regions.fromName( regionName ) ).build();
        aws.apiGateway = AmazonApiGatewayClient.builder().withCredentials( credentials ).withRegion( Regions.fromName( regionName ) ).build();
    }

    @Test void test()
    {
        val restApiID = "10oya1sioc";
        val path = "/api/db/";

        val result = aws.createResourcePath( restApiID, path );
        System.out.println( result.toString() );
    }
}
