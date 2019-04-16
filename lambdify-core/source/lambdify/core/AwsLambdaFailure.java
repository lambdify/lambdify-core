package lambdify.core;

class AwsLambdaFailure extends RuntimeException {
	AwsLambdaFailure(String message){
		super(message);
	}
}