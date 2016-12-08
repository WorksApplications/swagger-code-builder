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

/**
 * TODO: Write Javadoc
 */
public class JavaUtils {
    private CaseUtils caseUtils;

    public JavaUtils(CaseUtils caseUtils) {
        this.caseUtils = caseUtils;
    }

    public String asMethodName(PathOperation pathOperation) {
        switch (pathOperation.getMethod()) {
        case "POST":
            return asPostMethodName(pathOperation.getPath(), pathOperation.getOperation());
        case "PUT":
            return asPutMethodName(pathOperation.getPath(), pathOperation.getOperation());
        case "PATCH":
            return asPatchMethodName(pathOperation.getPath(), pathOperation.getOperation());
        case "GET":
            return asGetMethodName(pathOperation.getPath(), pathOperation.getOperation());
        case "DELETE":
            return asDeleteMethodName(pathOperation.getPath(), pathOperation.getOperation());
        }
        throw new IllegalArgumentException();
    }

    public String asPostMethodName(String path, Path pathItem) {
        return asPostMethodName(path, pathItem.getPost());
    }
    public String asPostMethodName(String path, Operation postOperation) {
        return asPostMethodName(path, postOperation.getOperationId());
    }
    public String asPostMethodName(String path, String operationId) {
        if (operationId != null) {
            return caseUtils.upperCamelCase(operationId);
        }

        if ("/".equals(path)) {
            return "PostRoot";
        }

        return appendEntityWords(path, new StringBuilder("Create")).toString();
    }


    public String asPutMethodName(String path, Path pathItem) {
        return asPutMethodName(path, pathItem.getPut());
    }
    public String asPutMethodName(String path, Operation putOperation) {
        return asPutMethodName(path, putOperation.getOperationId());
    }
    public String asPutMethodName(String path, String operationId) {
        if (operationId != null) {
            return caseUtils.upperCamelCase(operationId);
        }

        if ("/".equals(path)) {
            return "PutRoot";
        }

        return appendEntityWords(path, new StringBuilder("Update")).toString();
    }

    public String asPatchMethodName(String path, Path pathItem) {
        return asPutMethodName(path, pathItem.getPatch());
    }
    public String asPatchMethodName(String path, Operation patchOperation) {
        return asPatchMethodName(path, patchOperation.getOperationId());
    }
    public String asPatchMethodName(String path, String operationId) {
        if (operationId != null) {
            return caseUtils.upperCamelCase(operationId);
        }

        if ("/".equals(path)) {
            return "PatchRoot";
        }

        return appendEntityWords(path, new StringBuilder("Patch")).toString();
    }

    public String asGetMethodName(String path, Path pathItem) {
        return asGetMethodName(path, pathItem.getGet());
    }
    public String asGetMethodName(String path, Operation getOperation) {
        return asGetMethodName(path, getOperation.getOperationId());
    }
    public String asGetMethodName(String path, String operationId) {
        if (operationId != null) {
            return caseUtils.upperCamelCase(operationId);
        }

        if ("/".equals(path)) {
            return "GetRoot";
        }

        if (path.endsWith("}")) { // Parameter as a key
            return appendEntityWords(path, new StringBuilder("Find")).toString();
        } else {
            return appendEntityWords(path, new StringBuilder("Search")).toString();
        }
    }

    public String asDeleteMethodName(String path, Path pathItem) {
        return asDeleteMethodName(path, pathItem.getDelete());
    }
    public String asDeleteMethodName(String path, Operation deleteOperation) {
        return asDeleteMethodName(path, deleteOperation.getOperationId());
    }
    public String asDeleteMethodName(String path, String operationId) {
        if (operationId != null) {
            return caseUtils.upperCamelCase(operationId);
        }

        if ("/".equals(path)) {
            return "DeleteRoot";
        }

        return appendEntityWords(path, new StringBuilder("Delete")).toString();
    }

    private StringBuilder appendEntityWords(String path, StringBuilder result) {
        String[] pathParts = path.split("/");
        int length = pathParts.length;
        for (int i = 0; i < length; i++) {
            String part = pathParts[i];
            if (part.startsWith("{")) continue;

            result.append(caseUtils.upperCamelCase(caseUtils.singularize(part)));
        }
        return result;
    }

    public String toPackageDirectoriesPath(String packageName) {
        if (packageName == null) return null;
        return packageName.replace('.', '/');
    }

    public String toType(String type, String format) {
        if (type == null) return "String";
        switch (type) {
            case "integer":
                if ("int64".equals(format)) {
                    return "Long";
                }
                return "Integer";
            case "number":
                if ("float".equals(format)) {
                    return "Float";
                }
                return "Double";
            case "boolean":
                return "Boolean";
            case "object":
                return "Object";
        }
        return "String";
    }
}
