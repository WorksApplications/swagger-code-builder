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

package com.worksap.webapi.codingstarter;

import lombok.Data;
import org.kohsuke.args4j.Option;

/**
 * TODO: Write Javadoc
 */
@Data
public class ApplicationOption {
    @Option(name = "-h", aliases = "--help", usage = "Print usage message and exit", help = true)
    private boolean help = false;

    @Option(name = "--structure", usage = "Project structure", required = true)
    private String structure;

    @Option(name = "--api-spec-path", usage = "File path for Open API (Swagger) file", required = true)
    private String apiSpecPath;

    @Option(name = "--output-path", usage = "Output directory")
    private String outputPath = "out";

    @Option(name = "--java-group-id", usage = "Group ID of the artifact for Java project")
    private String javaGroupId = "com.worksap.webapi";

    @Option(name = "--aws-region", usage = "AWS region to deploy")
    private String awsRegion = "ap-northeast-1";

    @Option(name = "--aws-account-id", usage = "AWS account ID to deploy")
    private String awsAccountId;

    @Option(name = "--aws-api-gateway-use-api-key", usage = "Add x-api-key to swagger file")
    private boolean awsApiGatewayUseApiKey = false;
}
