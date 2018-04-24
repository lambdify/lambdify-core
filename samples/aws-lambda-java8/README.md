# AWS Lambda
A sample Hello World project using AWS Lambda

## Configuring
Update the following lines from the `pom.yml` file with the information about your AWS environment:
```yml
  # configure your lambda project here
  config.lambda.region: "sa-east-1"
  config.lambda.timeout: 60
  config.lambda.memory: 128
  config.lambda.s3bucket: ibratan-deployments
  config.lambda.role: arn:aws:iam::1234567890:role/my-lambda-role
  config.lambda.class: samples.aws.lambda.HelloWorld
```

## Deploying
Open a terminal at the place you've cloned this project and then type:
```./mvnw clean deploy```
