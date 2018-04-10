# Lambdify Plugin
This is a Maven plugin designed to:
- Deploy your uber-jar into S3 (AWS expects a single jar deployment)
- Create a function that uses your just uploaded uber-jar from S3
- Create an API Gateway, if required, pointing to your Lambda function

## Usage
Include the `lambdify-plugin` on your Maven project.
```xml
   <plugin>
     <groupId>ibratan.foss.lambdify</groupId>
     <artifactId>lambdify-plugin</artifactId>
     <version>1.0.0-SNAPSHOT</version>
     <executions>
       <execution>
         <phase>deploy</phase>
         <goals>
           <goal>deploy-on-aws-lambda</goal>
         </goals>
       </execution>
     </executions>
     <configuration>
       <enabled>true</enabled>
       <createAPIEndpoints>true</createAPIEndpoints>
       <regionName>${config.lambda.region}</regionName>
       <lambdaTimeout>${config.lambda.timeout}</lambdaTimeout>
       <lambdaMemory>${config.lambda.memory}</lambdaMemory>
       <s3Bucket>${config.lambda.s3bucket}</s3Bucket>
       <lambdaRole>${config.lambda.role}</lambdaRole>
       <handlerClass>${config.lambda.class}</handlerClass>
     </configuration>
   </plugin>
```

## Configuration Requirements
The following properties should be defined in order to your project work properly:
- **config.lambda.region**: The AWS Region you intent to deploy your lambda functions
- **config.lambda.timeout**: The execution timeout of your function (in seconds)
- **config.lambda.memory**: The expected memory usage of your lambda function (should be a valid memory value)
- **config.lambda.s3bucket**: The S3 Bucket you expect to deploy your jar file
- **config.lambda.role**: The AWS Role that will be associated to your Lambda Function
- **config.lambda.class**: The Java class (or endpoint) you intent to expose as Lambda Function

Please enrure you have also properly configured your `maven-shade-plugin` to generate an uber-jar from your
project before upload it to S3 and deploy your function.

Worth to notice that Lambdify-plugin will create the API Gateway REST API for your Lambda function only if `createAPIEndpoints`
is set to true. Also, once the API was created, it will skip this step on the following deployments. Thus, you are able to do
any kind of manual adjustment on your API after the initial setup.
