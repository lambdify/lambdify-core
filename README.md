# Lambdify Core
A lightweight flexible JVM runtime for AWS Lambda.

## Why another AWS Lambda Core library?
Amazon Web Services used to have a standard Java SDK which depends on a set of third-party libraries to make their
development routine easier. Although it is a great solution and works very well, due to the nature of how AWS Lambda
loads your functions, it may lead to long cold start times.

Aside of that, one of the biggest goals of Lambdify is being compatible with GraalVM builds, leveraging better
response times during the function execution.

## Creating a Lambda function
All you have to do is create a Java class that implements the interface `RequestHandler`. That
will be the entry point of your Lambda Function.

```java
import lambdify.core.*;
import lombok.*;

public class MyLambdaFunction implements RequestHandler<FullName, String> {

    public String handleRequest( FullName fullName ) {
        return fullName.firstName + " " + fullName.lastName;
    }
}

@Value public class FullName {
   final String firstName;
   final String middleName;
   final String surName;
}
```

Now, we need to configure our maven's `pom.yml` file by setting up the `lambdify-plugin`:
```yml
build:
  plugins:
    # Deploys your app as a AWS Lambda Function
    - groupId: org.lambdify
      artifactId: lambdify-plugin
      version: "<LAMBDIFY-VERSION-HERE>"
      executions:
        - { id: "package", goals: ["package"], phase: "verify" }

        # optional, should be included if you want to upload the artifact
        # to S3 using maven.
        - { id: "s3deploy", goals: ["s3-deploy"], phase: "deploy" }
      configuration:
        enabled:            ${config.lambdify.enabled}
        handler:            "${config.lambdify.handler}"

        # required only for S3 artifact deployment
        regionName:         "${config.lambdify.region}"
        jarFileName:        "${config.lambdify.jar-name}"
        zipFileName:        "${config.lambdify.zip-name}"
        s3Key:              "${config.lambdify.s3.key}"
        s3Bucket:           "${config.lambdify.s3.bucket}"
```

For more details, consult the Lambdify's [documentation](https://github.com/lambdify/lambdify).

## Reporting Bugs/Feature Requests

We welcome you to use the GitHub issue tracker to report bugs or suggest features.

When filing an issue, please check existing open, or recently closed, issues to make sure somebody else hasn't already
reported the issue. Please try to include as much information as you can. Details like these are incredibly useful:

* A reproducible test case or series of steps
* The version of our code being used
* Any modifications you've made relevant to the bug
* Anything unusual about your environment or deployment


## Contributing via Pull Requests
Contributions via pull requests are much appreciated. Before sending us a pull request, please ensure that:

1. You are working against the latest source on the *master* branch.
2. You check existing open, and recently merged, pull requests to make sure someone else hasn't addressed the problem already.
3. You open an issue to discuss any significant work - we would hate for your time to be wasted.

To send us a pull request, please:

1. Fork the repository.
2. Modify the source; please focus on the specific change you are contributing. If you also reformat all the code, it will be hard for us to focus on your change.
3. Ensure local tests pass.
4. Commit to your fork using clear commit messages.
5. Send us a pull request, answering any default questions in the pull request interface.
6. Pay attention to any automated CI failures reported in the pull request, and stay involved in the conversation.

GitHub provides additional document on [forking a repository](https://help.github.com/articles/fork-a-repo/) and
[creating a pull request](https://help.github.com/articles/creating-a-pull-request/).


## Finding contributions to work on
Looking at the existing issues is a great way to find something to contribute on. As our projects, by default, use the default GitHub issue labels ((enhancement/bug/duplicate/help wanted/invalid/question/wontfix), looking at any 'help wanted' issues is a great place to start.

## License
All open source libraries developed by Lambdify are licenced under the Apache License 2 terms.

