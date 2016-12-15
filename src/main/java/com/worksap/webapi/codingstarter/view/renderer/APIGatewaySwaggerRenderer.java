/*
 *   Copyright 2016 Works Applications Co.,Ltd.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.worksap.webapi.codingstarter.view.renderer;

import com.worksap.webapi.codingstarter.ApplicationOption;
import com.worksap.webapi.codingstarter.view.utils.Utils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * TODO: Write Javadoc
 */
public class APIGatewaySwaggerRenderer {

    private final Yaml yaml;
    private final String awsRegion;
    private final String awsAccountId;
    private final boolean awsApiGatewayUseApiKey;
    private final boolean awsApiGatewayEnableCors;
    private final String apiSpecPath;
    private final VelocityEngine velocityEngine;
    private final VelocityContext originalVelocityContext;
    private final Utils utils;

    private Map<String, Object> apiSpec;

    public APIGatewaySwaggerRenderer(ApplicationOption option, VelocityEngine velocityEngine, VelocityContext velocityContext, Utils utils) throws IOException, ReflectiveOperationException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yaml = new Yaml(options);
        this.awsRegion = option.getAwsRegion();
        this.awsAccountId = option.getAwsAccountId();
        this.awsApiGatewayUseApiKey = option.isAwsApiGatewayUseApiKey();
        this.awsApiGatewayEnableCors = option.isAwsApiGatewayEnableCors();
        this.apiSpecPath = option.getApiSpecPath();
        this.velocityEngine = velocityEngine;
        this.originalVelocityContext = velocityContext;
        this.utils = utils;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadApiSpec(String apiSpecPath) throws IOException, ReflectiveOperationException {
        try (InputStream stream = new BufferedInputStream(new FileInputStream(new File(apiSpecPath)))) {
            return addExtension((Map<String, Object>) yaml.load(stream));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> addExtension(Map<String, Object> apiSpec) throws ReflectiveOperationException, IOException {
        for (Map.Entry<String, Object> pathEntry : ((Map<String, Object>) apiSpec.get("paths")).entrySet()) {
            String path = pathEntry.getKey();
            Map<String, Object> operations = (Map<String, Object>) pathEntry.getValue();
            for (Map.Entry<String, Object> operationEntry : operations.entrySet()) {
                String method = operationEntry.getKey().toUpperCase();
                Map<String, Object> operation = (Map<String, Object>) operationEntry.getValue();
                String functionName = toFunctionName(method, path, (String) operation.get("operationId"));
                processOperation(functionName, operation, path, method);
            }

            if (awsApiGatewayEnableCors) {
                if (operations.containsKey("options")) {
                    throw new IllegalStateException();
                }

                operations.put("options", generateCorsOptionsOperation());
            }
        }

        if (awsApiGatewayUseApiKey) {
            Map<String, Object> securityDefinitions = (Map<String, Object>) apiSpec.get("securityDefinitions");
            if (securityDefinitions == null) {
                securityDefinitions = new HashMap<>();
                apiSpec.put("securityDefinitions", securityDefinitions);
            }
            securityDefinitions.put("api_key", generateApiKeyDefinition());
        }

        return apiSpec;
    }

    private Map<String, Object> generateCorsOptionsOperation() {
        Map<String, Object> operation = new HashMap<>();

        operation.put("summary", "CORS support");

        operation.put("description", "Enable CORS by returning correct headers");

        operation.put("consumes", Collections.singletonList("application/json"));

        operation.put("produces", Collections.singletonList("application/json"));

        operation.put("tags", Collections.singletonList("CORS"));

        Map<String, Object> integration = new HashMap<>();
        integration.put("type", "mock");
        integration.put("requestTemplates", Collections.singletonMap("application/json", "{\"statusCode\" : 200}"));
        Map<String, Object> integrationResponse = new HashMap<>();
        integrationResponse.put("statusCode", "200");
        Map<String, Object> responseParameters = new HashMap<>();
        responseParameters.put("method.response.header.Access-Control-Allow-Headers", "'Content-Type,X-Amz-Date,Authorization,X-Api-Key'");
        responseParameters.put("method.response.header.Access-Control-Allow-Methods", "'*'");
        responseParameters.put("method.response.header.Access-Control-Allow-Origin", "'*'");
        integrationResponse.put("responseParameters", responseParameters);
        integrationResponse.put("responseTemplates", Collections.singletonMap("application/json", "{}"));
        integration.put("responses", Collections.singletonMap("default", integrationResponse));

        operation.put("x-amazon-apigateway-integration", integration);

        Map<String, Object> corsOptionsResponse = new HashMap<>();
        corsOptionsResponse.put("description", "Default response for CORS method");
        Map<String, Object> responseHeaders = new HashMap<>();
        responseHeaders.put("Access-Control-Allow-Headers", Collections.singletonMap("type", "string"));
        responseHeaders.put("Access-Control-Allow-Methods", Collections.singletonMap("type", "string"));
        responseHeaders.put("Access-Control-Allow-Origin", Collections.singletonMap("type", "string"));
        corsOptionsResponse.put("headers", responseHeaders);
        operation.put("responses", Collections.singletonMap("200", corsOptionsResponse));

        if (awsApiGatewayUseApiKey) {
            Map<String, List<String>> apiKeySecurity = Collections.singletonMap("api_key", new ArrayList<>());
            operation.put("security", Collections.singletonList(apiKeySecurity));
        }

        return operation;
    }

    private Map<String, String> generateApiKeyDefinition() {
        Map<String, String> apiKeyDefinition = new HashMap<>();
        apiKeyDefinition.put("type", "apiKey");
        apiKeyDefinition.put("name", "x-api-key");
        apiKeyDefinition.put("in", "header");
        return apiKeyDefinition;
    }

    private String toFunctionName(String method, String path, String operationId) {
        switch (method) {
            case "POST":
                return utils.getAWS().asPostMethodName(path, operationId);
            case "PUT":
                return utils.getAWS().asPutMethodName(path, operationId);
            case "PATCH":
                return utils.getAWS().asPatchMethodName(path, operationId);
            case "GET":
                return utils.getAWS().asGetMethodName(path, operationId);
            case "DELETE":
                return utils.getAWS().asDeleteMethodName(path, operationId);
        }
        throw new IllegalArgumentException();
    }

    @SuppressWarnings("unchecked")
    private void processOperation(String functionName, Map<String, Object> operation, String path, String method) throws IOException {
        Map<String, String> requestTemplates = Collections.singletonMap("application/json", renderRequestMappingTemplate(path, method));

        Map<String, Object> responses = new HashMap<>();
        for (Map.Entry<String, Object> responseEntry : ((Map<String, Object>) operation.get("responses")).entrySet()) {
            String httpStatusCode = String.valueOf(responseEntry.getKey());
            Map<String, Object> apiResponse = (Map<String, Object>) responseEntry.getValue();

            if (httpStatusCode.startsWith("2")) {
                responses.put("default", createSuccessResponse(httpStatusCode, apiResponse));
            } else {
                responses.put("^" + httpStatusCode + ":.*", createErrorResponse(httpStatusCode, apiResponse));
            }

            if (awsApiGatewayEnableCors) {
                Map<String, Object> headers = (Map<String, Object>) apiResponse.get("headers");
                if (headers == null) {
                    headers = new HashMap<>();
                    apiResponse.put("headers", headers);
                }

                headers.put("Access-Control-Allow-Headers", Collections.singletonMap("type", "string"));
                headers.put("Access-Control-Allow-Methods", Collections.singletonMap("type", "string"));
                headers.put("Access-Control-Allow-Origin", Collections.singletonMap("type", "string"));
            }
        }

        Map<String, Object> integration = new HashMap<>();
        integration.put("requestTemplates", requestTemplates);
        integration.put("uri", "arn:aws:apigateway:" + awsRegion + ":lambda:path/2015-03-31/functions/arn:aws:lambda:"
                + awsRegion + ":" + awsAccountId + ":function:" + functionName + "/invocations");
        integration.put("passthroughBehavior", "when_no_templates");
        integration.put("httpMethod", "POST"); // Lambda must be called by POST
        integration.put("responses", responses);
        integration.put("type", "aws");

        operation.put("x-amazon-apigateway-integration", integration);

        if (awsApiGatewayUseApiKey) {
            Map<String, List<String>> apiKeySecurity = Collections.singletonMap("api_key", new ArrayList<>());

            List<Object> security = (List<Object>) operation.get("security");
            if (security == null) {
                operation.put("security", Collections.singletonList(apiKeySecurity));
            } else {
                security.add(apiKeySecurity);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> createSuccessResponse(String httpStatusCode, Map<String, Object> apiResponse) throws IOException {
        Map<String, Object> awsResponse = new HashMap<>();

        awsResponse.put("responseTemplates", Collections.singletonMap("application/json", "$input.json('$.body')"));

        Map<String, Object> responseParameters = new HashMap<>();
        Map<String, Object> headers = (Map<String, Object>) apiResponse.get("headers");
        if (headers != null) {
            for (Map.Entry<String, Object> headerEntry : headers.entrySet()) {
                String headerName = headerEntry.getKey();
                responseParameters.put("method.response.header." + headerName,
                        "integration.response.body" + utils.getCase().lowerCamelCase(headerName));
            }
        }
        if (awsApiGatewayEnableCors) {
            responseParameters.put("method.response.header.Access-Control-Allow-Headers", "'Content-Type,X-Amz-Date,Authorization,X-Api-Key'");
            responseParameters.put("method.response.header.Access-Control-Allow-Methods", "'*'");
            responseParameters.put("method.response.header.Access-Control-Allow-Origin", "'*'");
        }
        if (!responseParameters.isEmpty()) {
            awsResponse.put("responseParameters", responseParameters);
        }

        awsResponse.put("statusCode", httpStatusCode);
        return awsResponse;
    }

    private Map<String, Object> createErrorResponse(String httpStatusCode, Map<String, Object> apiResponse) {
        Map<String, Object> awsResponse = new HashMap<>();
        awsResponse.put("responseTemplates",Collections.singletonMap("application/json",
                "{\"message\": \"$util.escapeJavaScript($input.path('$.errorMessage').substring(5))\"}"));
        awsResponse.put("statusCode", httpStatusCode);

        if (awsApiGatewayEnableCors) {
            Map<String, Object> responseParameters = new HashMap<>();
            responseParameters.put("method.response.header.Access-Control-Allow-Headers", "'Content-Type,X-Amz-Date,Authorization,X-Api-Key'");
            responseParameters.put("method.response.header.Access-Control-Allow-Methods", "'*'");
            responseParameters.put("method.response.header.Access-Control-Allow-Origin", "'*'");
            awsResponse.put("responseParameters", responseParameters);
        }

        return awsResponse;
    }

    private String renderRequestMappingTemplate(String path, String method) throws IOException {
        return renderMappingTemplate(path, method, "/templates/aws/apigateway/requestmapping.vm.vm");
    }

    private String renderMappingTemplate(String path, String method, String templateName) throws IOException {
        VelocityContext velocityContext = (VelocityContext) originalVelocityContext.clone();
        Template template = velocityEngine.getTemplate(templateName);
        try (StringWriter writer = new StringWriter()) {
            Map<String, String> params = new HashMap<>();
            params.put("path", path);
            params.put("method", method);
            velocityContext.put("params", params);
            template.merge(velocityContext, writer);
            return writer.toString();
        }
    }

    public void writeFile(Path newFilePath) throws IOException, ReflectiveOperationException {
        if (apiSpec == null) {
            apiSpec = loadApiSpec(apiSpecPath);
        }

        try (Writer writer = Files.newBufferedWriter(newFilePath)) {
            writer.append(yaml.dump(apiSpec));
        }
    }
}
