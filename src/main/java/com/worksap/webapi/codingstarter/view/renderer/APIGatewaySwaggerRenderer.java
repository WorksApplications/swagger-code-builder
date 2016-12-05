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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Write Javadoc
 */
public class APIGatewaySwaggerRenderer {
    private Yaml yaml;
    private final String awsRegion;
    private final String awsAccountId;
    private final String apiSpecPath;
    private final VelocityEngine velocityEngine;
    private final VelocityContext velocityContext;
    private final Utils utils;

    private Map<String, Object> apiSpec;

    public APIGatewaySwaggerRenderer(ApplicationOption option, VelocityEngine velocityEngine, VelocityContext velocityContext, Utils utils) throws IOException, ReflectiveOperationException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yaml = new Yaml(options);
        this.awsRegion = option.getAwsRegion();
        this.awsAccountId = option.getAwsAccountId();
        this.apiSpecPath = option.getApiSpecPath();
        this.velocityEngine = velocityEngine;
        this.velocityContext = velocityContext;
        this.utils = utils;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadApiSpec(String apiSpecPath) throws IOException, ReflectiveOperationException {
        try (InputStream stream = new BufferedInputStream(new FileInputStream(new File(apiSpecPath)))) {
            return addIntegration((Map<String, Object>) yaml.load(stream));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> addIntegration(Map<String, Object> apiSpec) throws ReflectiveOperationException, IOException {
        for (Map.Entry<String, Object> pathEntry : ((Map<String, Object>) apiSpec.get("paths")).entrySet()) {
            String path = pathEntry.getKey();
            Map<String, Object> operations = (Map<String, Object>) pathEntry.getValue();
            for (Map.Entry<String, Object> operationEntry : operations.entrySet()) {
                String method = operationEntry.getKey().toUpperCase();
                Map<String, Object> operation = (Map<String, Object>) operationEntry.getValue();
                String functionName = toFunctionName(method, path, (String) operation.get("operationId"));
                addIntegration(functionName, operation, path, method);
            }
        }
        return apiSpec;
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
    private void addIntegration(String functionName, Map<String, Object> operation, String path, String method) throws IOException {
        Map<String, String> requestTemplates = Collections.singletonMap("application/json", renderRequestMappingTemplate(path, method));

        Map<String, Object> responses = new HashMap<>();
        for (Map.Entry<String, Object> responseEntry : ((Map<String, Object>) operation.get("responses")).entrySet()) {
            String httpStatusCode = String.valueOf(responseEntry.getKey());
            Map<String, Object> apiResponse = (Map<String, Object>) responseEntry.getValue();

            if (httpStatusCode.startsWith("2")) {
                responses.put("default", createSuccessResponse(httpStatusCode, apiResponse, path, method));
            } else {
                responses.put("^" + httpStatusCode + ":.*", createErrorResponse(httpStatusCode, apiResponse));
            }
        }

        Map<String, Object> integration = new HashMap<>();
        integration.put("requestTemplates", requestTemplates);
        integration.put("uri", "arn:aws:apigateway:" + awsRegion + ":lambda:path/2015-03-31/functions/arn:aws:lambda:"
                + awsRegion + ":" + awsAccountId + ":function:" + functionName + "/invocations");
        integration.put("passthroughBehavior", "when_no_templates");
        integration.put("httpMethod", "POST");
        integration.put("responses", responses);
        integration.put("type", "aws");

        operation.put("x-amazon-apigateway-integration", integration);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> createSuccessResponse(String httpStatusCode, Map<String, Object> apiResponse, String path, String method) throws IOException {
        Map<String, Object> awsResponse = new HashMap<>();
        awsResponse.put("responseTemplates", Collections.singletonMap("application/json", "$input.json('$.body')"));
        Map<String, Object> headers = (Map<String, Object>) apiResponse.get("headers");
        if (headers != null) {
            for (Map.Entry<String, Object> headerEntry : headers.entrySet()) {
                String headerName = headerEntry.getKey();
                awsResponse.put("method.response.header." + headerName,
                        "integration.response.body" + utils.getCase().lowerCamelCase(headerName));
            }
        }
        awsResponse.put("statusCode", httpStatusCode);
        return awsResponse;
    }

    private Map<String, Object> createErrorResponse(String httpStatusCode, Map<String, Object> apiResponse) {
        Map<String, Object> awsResponse = new HashMap<>();
        awsResponse.put("responseTemplates", Collections.singletonMap("application/json", "{\"message\": \"$input.body\"}"));
        awsResponse.put("statusCode", httpStatusCode);
        return awsResponse;
    }

    private String renderRequestMappingTemplate(String path, String method) throws IOException {
        return renderMappingTemplate(path, method, "/templates/aws/apigateway/requestmapping.vm.vm");
    }

    private String renderMappingTemplate(String path, String method, String templateName) throws IOException {
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
