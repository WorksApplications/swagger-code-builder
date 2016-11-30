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

import com.worksap.webapi.codingstarter.view.renderer.APIGatewaySwaggerRenderer;
import com.worksap.webapi.codingstarter.view.renderer.VelocityTemplateRenderer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * TODO: Write Javadoc
 */
@Slf4j
public class StructureVisitor {

    private final String outputPath;
    private final VelocityTemplateRenderer templateRenderer;
    private final APIGatewaySwaggerRenderer apiGatewaySwaggerRenderer;

    public StructureVisitor(ApplicationOption option, VelocityTemplateRenderer templateRenderer,
                            APIGatewaySwaggerRenderer apiGatewaySwaggerRenderer) {
        this.outputPath = option.getOutputPath();
        this.templateRenderer  = templateRenderer;
        this.apiGatewaySwaggerRenderer = apiGatewaySwaggerRenderer;
    }

    public void visit(Map<String, Object> node) throws IOException {
        Path rootDirPath = Paths.get(outputPath);
        if (!Files.isDirectory(rootDirPath)) {
            throw new IOException("Output directory does not exist");
        }

        visitChild(rootDirPath, node);
    }

    private void visitChild(Path parentDirPath, Map<String, Object> node) throws IOException {
        if (node == null) return;
        for (Map.Entry<String, Object> entry : node.entrySet()) {
            String filename = entry.getKey();

            if (filename.endsWith("/")) {
                String nextDirName = filename.substring(0, filename.length() - 1);
                if (nextDirName.length() == 0) {
                    throw new IOException("Cannot create empty name directory");
                }

                Path nextDirPath = parentDirPath.resolve(nextDirName);
                if (Files.notExists(nextDirPath)) {
                    Files.createDirectories(nextDirPath);
                }

                visitChild(nextDirPath, (Map<String, Object>) entry.getValue());
            } else {
                Path newFilePath = parentDirPath.resolve(filename);

                Map<String, Object> renderInfo = (Map<String, Object>) entry.getValue();

                String templateType = (String) renderInfo.getOrDefault("type", "template");
                switch (templateType) {
                    case "template":
                        String templateName = "/templates/" + Objects.requireNonNull(renderInfo.get("template"));
                        log.info("process: {}, {}", newFilePath, templateName);
                        Object params = renderInfo.get("params");
                        templateRenderer.writeFile(newFilePath, templateName, params);
                        break;
                    case "swagger-api-gateway":
                        log.info("process: {}", newFilePath);
                        apiGatewaySwaggerRenderer.writeFile(newFilePath);
                        break;
                }

                String permission = (String) renderInfo.get("permission");
                if (permission != null) {
                    Set<PosixFilePermission> perms = PosixFilePermissions.fromString(permission);
                    Files.setPosixFilePermissions(newFilePath, perms);
                }
            }
        }
    }
}
