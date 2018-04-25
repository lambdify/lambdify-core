# lambdify
Simplified AWS Lambda Development for Kotlin and Java 8.

## What is Lambdify?
The main goal of this project is provide an easy environment to create serverless applications through the AWS Lambda API. 
Lambdify consists of:
- A [Maven-Plugin](https://github.com/lambdify/lambdify/tree/master/lambdify-plugin) to deploy your uber-jar as a Lambda Application
- An [API Gateway router](https://github.com/lambdify/lambdify/tree/master/lambdify-apigateway) to receive and handle requests from multiple endpoints at the same Lambda Application.
- A pre-defined Maven project structure (boilerplate) for Lambda applications written in [java8](https://github.com/lambdify/lambdify/tree/master/lambdify-project-java) and [kotlin](https://github.com/lambdify/lambdify/tree/master/lambdify-project-kotlin).

## Documentation
A well writen documentation wasn't finished yet. But you can get some inspirations at the sample
AWS project available at the `samples` directory.

## Getting Started
The easiest way to get started with Lambdify is using the simplified project structure. [Here](https://github.com/lambdify/lambdify/releases/download/0.2.0.Final/aws-apigateway-kotlin-simplified.zip) you can download a ZIP file with a pre-configured project in which you can easily adapt for your needs.

## Acknowledgements
This project was made public as an effort of [Ibratan](https://github.com/Ibratan) to provide good and reliable software to
the Open Source community.

Also this project was deeply based on [Kikaha's Maven Plugin](http://kikaha.io).

## License
Apache License 2
