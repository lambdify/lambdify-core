package lambdify.apigateway;

import java.util.*;
import java.util.stream.Collectors;
import lambdify.apigateway.PolicyDocument.Statement;
import lombok.*;

@Value
public class AuthPolicy {

    public static final String
        VERSION = "Version",
        STATEMENT = "Statement",
        EFFECT = "Effect",
        ACTION = "Action",
        NOT_ACTION = "NotAction",
        RESOURCE = "Resource",
        NOT_RESOURCE = "NotResource",
        CONDITION = "Condition";

    String principalId;
    PolicyDocument policyDocument;
    Map<String, String> context;
    String usageIdentifierKey;

    public Map<String, Object> getPolicyDocument(){
        val serializablePolicy = new HashMap<String, Object>();
        serializablePolicy.put(VERSION, policyDocument.version);
        val statements = policyDocument.getStatements();
        serializablePolicy.put(STATEMENT, statements.stream()
            .map( this::statementToMap ).collect(Collectors.toList() ) );
        return serializablePolicy;
    }

    private Map<String, Object> statementToMap(Statement statement) {
        val serializableStatement = new HashMap<String, Object>();
        serializableStatement.put(EFFECT, statement.effect);
        serializableStatement.put(ACTION, statement.action);
        serializableStatement.put(RESOURCE, statement.getResources());
        serializableStatement.put(CONDITION, statement.condition);
        return serializableStatement;
    }
}