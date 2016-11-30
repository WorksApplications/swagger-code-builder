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

import io.swagger.models.Model;
import io.swagger.models.RefModel;
import io.swagger.models.properties.Property;

import java.util.Map;

/**
 * TODO: Write Javadoc
 */
public class SchemaUtils {

    public String resolveName(Property schema, Map<String, Model> definitions, String defaultName) {
        String title = schema.getTitle();
        if (title != null) {
            return title;
        }

        return defaultName;
    }

    public String resolveName(Model schema, Map<String, Model> definitions, String defaultName) {
        String title = schema.getTitle();
        if (title != null) {
            return title;
        }

        if (definitions != null && schema instanceof RefModel) {
            String simpleRef = ((RefModel) schema).getSimpleRef();
            Model resolveModel = definitions.get(simpleRef);

            String resolveTitle = resolveModel.getTitle();
            if (resolveTitle != null) {
                return resolveTitle;
            }

            return simpleRef;
        }

        return defaultName;
    }
}
