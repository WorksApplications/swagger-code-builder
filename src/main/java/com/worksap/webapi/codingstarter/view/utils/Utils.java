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

package com.worksap.webapi.codingstarter.view.utils;

import com.worksap.webapi.codingstarter.model.PathOperation;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import org.jboss.dna.common.text.Inflector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO: Write Javadoc
 */
public class Utils {
    private Pattern versionPattern = Pattern.compile("^([^.]+)(?:\\.([^.]+))?(?:\\.([^-]+))?(?:-(\\S+))?");

    private Inflector inflector = Inflector.getInstance();
    private CaseUtils caseUtils = new CaseUtils(inflector);
    private AWSUtils awsUtils = new AWSUtils(caseUtils);
    private JavaUtils javaUtils = new JavaUtils(caseUtils);
    private ParameterUtils parameterUtils = new ParameterUtils();
    private SchemaUtils schemaUtils = new SchemaUtils();

    public CaseUtils getCase() {
        return caseUtils;
    }

    public JavaUtils getJava() {
        return javaUtils;
    }

    public ParameterUtils getParameter() {
        return parameterUtils;
    }

    public SchemaUtils getSchema() {
        return schemaUtils;
    }

    public SemanticVersion parseVersion(String version) {
        if (version == null) {
            return null;
        }

        Matcher matcher = versionPattern.matcher(version);
        if (!matcher.matches()) {
            return null;
        }

        return SemanticVersion.builder()
                .major(matcher.group(1))
                .minor(matcher.group(2))
                .patch(matcher.group(3))
                .suffix(matcher.group(4))
                .build();
    }

    public List<PathOperation> toPathOperation(Map<String, Path> paths) {
        if (paths == null) return null;

        Set<Map.Entry<String, Path>> pathEntries = paths.entrySet();
        ArrayList<PathOperation> result = new ArrayList<>(pathEntries.size() * 4);
        for (Map.Entry<String, Path> pathEntry : pathEntries) {
            String path = pathEntry.getKey();
            Path pathItem = pathEntry.getValue();

            PathOperation postOperation = toPathOperation(path, pathItem.getPost(), "POST");
            if (postOperation != null) {
                result.add(postOperation);
            }

            PathOperation putOperation = toPathOperation(path, pathItem.getPut(), "PUT");
            if (putOperation != null) {
                result.add(putOperation);
            }

            PathOperation getOperation = toPathOperation(path, pathItem.getGet(), "GET");
            if (getOperation != null) {
                result.add(getOperation);
            }

            PathOperation deleteOperation = toPathOperation(path, pathItem.getDelete(), "DELETE");
            if (deleteOperation != null) {
                result.add(deleteOperation);
            }
        }

        result.trimToSize();
        return result;
    }

    private PathOperation toPathOperation(String path, Operation operation, String method) {
        if (operation == null) return null;
        return new PathOperation(path, operation, method);
    }

    public AWSUtils getAWS() {
        return awsUtils;
    }
}
