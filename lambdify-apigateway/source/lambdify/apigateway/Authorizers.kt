/*
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * This source code is a Kotlin version from the Java classes found at
 * https://github.com/awslabs/aws-apigateway-lambda-authorizer-blueprints/tree/master/blueprints/java/src/io.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at
 *
 *     http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package lambdify.apigateway

/**
 * AuthPolicy receives a set of allowed and denied methods and generates a valid
 * AWS policy for the API Gateway authorizer. The constructor receives the calling
 * user principal, the AWS account ID of the API owner, and an apiOptions object.
 * The apiOptions can contain an API Gateway RestApi Id, a region for the RestApi, and a
 * stage that calls should be allowed/denied for. For example
 *
 * new AuthPolicy(principalId, AuthPolicy.PolicyDocument.getDenyAllPolicy(region, awsAccountId, restApiId, stage));
 *
 * @author Jack Kohn
 */
data class AuthPolicy(
        val principalId: String,
        @field:Transient val policyDocumentObject: AuthPolicy.PolicyDocument
) {

    /**
     * IAM Policies use capitalized field names, but Lambda by default will serialize object members using camel case
     *
     * This method implements a custom serializer to return the IAM Policy as a well-formed JSON document, with the correct field names
     *
     * @return IAM Policy as a well-formed JSON document
     */
    val policyDocument: Map<String, Any>
        get() {
            val serializablePolicy = mutableMapOf<String, Any>()
            serializablePolicy[VERSION] = policyDocumentObject.version
            val statements = policyDocumentObject.statement
            val serializableStatementArray = arrayOfNulls<Map<*, *>>(statements.size)
            for (i in statements.indices) {
                val serializableStatement = HashMap<String, Any>()
                val statement = statements[i]
                serializableStatement[EFFECT] = statement.effect
                serializableStatement[ACTION] = statement.action
                serializableStatement[RESOURCE] = statement.resource
                serializableStatement[CONDITION] = statement.condition
                serializableStatementArray[i] = serializableStatement
            }
            serializablePolicy[STATEMENT] = serializableStatementArray
            return serializablePolicy
        }

    /**
     * PolicyDocument represents an IAM Policy, specifically for the execute-api:Invoke action
     * in the context of a API Gateway Authorizer
     *
     * Initialize the PolicyDocument with
     * the region where the RestApi is configured,
     * the AWS Account ID that owns the RestApi,
     * the RestApi identifier
     * and the Stage on the RestApi that the Policy will apply to
     */
    class PolicyDocument

        /**
         * Creates a new PolicyDocument with the given context,
         * and initializes two base Statement objects for allowing and denying access to API Gateway methods
         *
         * @param region the region where the RestApi is configured
         * @param awsAccountId the AWS Account ID that owns the RestApi
         * @param restApiId the RestApi identifier
         * @param stage and the Stage on the RestApi that the Policy will apply to
         */
        (@field:Transient internal var region: String,
         @field:Transient internal var awsAccountId: String,
         @field:Transient internal var restApiId: String,
         @field:Transient internal var stage: String)
    {

        var version = "2012-10-17" // override if necessary

        private val allowStatement = Statement.emptyInvokeStatement("Allow")
        private val denyStatement = Statement.emptyInvokeStatement("Deny")
        private val statements = mutableListOf(allowStatement, denyStatement)

        val statement: Array<AuthPolicy.Statement>
            get() = statements.toTypedArray()

        fun allowMethod(httpMethod: Methods, resourcePath: String) =
                addResourceToStatement(allowStatement, httpMethod, resourcePath)

        fun denyMethod(httpMethod: Methods, resourcePath: String) =
                addResourceToStatement(denyStatement, httpMethod, resourcePath)

        fun addStatement(statement: AuthPolicy.Statement) {
            statements.add(statement)
        }

        private fun addResourceToStatement(statement: Statement, httpMethod: Methods, resourcePath: String) {
            var resourcePath = resourcePath
            // resourcePath must start with '/'
            // to specify the root resource only, resourcePath should be an empty string
            if (resourcePath == "/") {
                resourcePath = ""
            }
            val resource = if (resourcePath.startsWith("/")) resourcePath.substring(1) else resourcePath
            val method = if (httpMethod == Methods.ALL) "*" else httpMethod.toString()
            statement.addResource("arn:aws:execute-api:$region:$awsAccountId:$restApiId/$stage/$method/$resource")
        }

        companion object {

            /**
             * Generates a new PolicyDocument with a single statement that allows the requested method/resourcePath
             *
             * @param region API Gateway region
             * @param awsAccountId AWS Account that owns the API Gateway RestApi
             * @param restApiId RestApi identifier
             * @param stage Stage name
             * @param method HttpMethod to allow
             * @param resourcePath Resource path to allow
             * @return new PolicyDocument that allows the requested method/resourcePath
             */
            fun allowOnePolicy(region: String, awsAccountId: String, restApiId: String, stage: String, method: Methods, resourcePath: String): PolicyDocument {
                val policyDocument = AuthPolicy.PolicyDocument(region, awsAccountId, restApiId, stage)
                policyDocument.allowMethod(method, resourcePath)
                return policyDocument
            }

            /**
             * Generates a new PolicyDocument with a single statement that denies the requested method/resourcePath
             *
             * @param region API Gateway region
             * @param awsAccountId AWS Account that owns the API Gateway RestApi
             * @param restApiId RestApi identifier
             * @param stage Stage name
             * @param method HttpMethod to deny
             * @param resourcePath Resource path to deny
             * @return new PolicyDocument that denies the requested method/resourcePath
             */
            fun denyOnePolicy(region: String, awsAccountId: String, restApiId: String, stage: String, method: Methods, resourcePath: String): PolicyDocument {
                val policyDocument = AuthPolicy.PolicyDocument(region, awsAccountId, restApiId, stage)
                policyDocument.denyMethod(method, resourcePath)
                return policyDocument
            }

            fun allowAllPolicy(region: String, awsAccountId: String, restApiId: String, stage: String): AuthPolicy.PolicyDocument {
                return allowOnePolicy(region, awsAccountId, restApiId, stage, Methods.ALL, "*")
            }

            fun denyAllPolicy(region: String, awsAccountId: String, restApiId: String, stage: String): PolicyDocument {
                return denyOnePolicy(region, awsAccountId, restApiId, stage, Methods.ALL, "*")
            }
        }
    }

    class Statement(
            val effect: String,
            val action: String,
            private val resourceList: MutableList<String>,
            val condition: MutableMap<String, Map<String, Any>>) {

        fun addResource(resource: String) {
            resourceList.add(resource)
        }

        fun addCondition(operator: String, key: String, value: Any) {
            condition[operator] = mapOf(key to value)
        }

        val resource: Array<String> get() = resourceList.toTypedArray()

        companion object {

            fun emptyInvokeStatement(effect: String): Statement =
                    Statement(effect, "execute-api:Invoke", ArrayList(), HashMap())
        }
    }

    companion object {

        // IAM Policy Constants
        val VERSION = "Version"
        val STATEMENT = "Statement"
        val EFFECT = "Effect"
        val ACTION = "Action"
        val NOT_ACTION = "NotAction"
        val RESOURCE = "Resource"
        val NOT_RESOURCE = "NotResource"
        val CONDITION = "Condition"

        fun unauthorized(): AuthPolicy {
            throw RuntimeException("Unauthorized")
        }
    }
}

/**
 * Object representation of input to an implementation of an API Gateway custom authorizer of type TOKEN
 *
 * @author Jack Kohn
 * @author Miere Liniel Teixeira
 */
data class TokenAuthorizerContext (
    val type: String,
    val authorizationToken: String,
    val methodArn: String ) {

    fun grantPermission( principalId: String ): AuthPolicy {
        return AuthPolicy(
            principalId, AuthPolicy.PolicyDocument.allowAllPolicy(
                method.region, method.awsAccountId, method.restApiId, method.stage)
        )
    }

    fun denyPermission( principalId: String ): AuthPolicy {
        return AuthPolicy(
                principalId, AuthPolicy.PolicyDocument.denyAllPolicy(
                method.region, method.awsAccountId, method.restApiId, method.stage)
        )
    }

    val method: MethodArn get() {
        val arnPartials = methodArn.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        val region = arnPartials[3]
        val awsAccountId = arnPartials[4]
        val apiGatewayArnPartials = arnPartials[5].split("/".toRegex()).dropLastWhile({ it.isEmpty() })//.toTypedArray()
        val restApiId = apiGatewayArnPartials[0]
        val stage = apiGatewayArnPartials[1]
        val httpMethod = apiGatewayArnPartials[2]
        var resource = "" // root resource
        if (apiGatewayArnPartials.size > 3 ) resource = apiGatewayArnPartials.subList( 3, apiGatewayArnPartials.size ).joinToString( "/" ) { it }
        return MethodArn(region, awsAccountId, restApiId, stage, httpMethod, resource)
    }
}

data class MethodArn(
    val region:String, val awsAccountId:String, val restApiId:String,
    val stage:String, val httpMethod:String, val resource:String
)