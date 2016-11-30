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

import io.swagger.models.Swagger;

/**
 * TODO: Write Javadoc
 */
public class Shortcut {
    public String packageName(Swagger api) {
        String packageName = ((String) api.getInfo().getVendorExtensions().get("x-wap-package-name"));
        if (packageName == null) {
            throw new IllegalStateException("info.x-wap-package-name is required for java project");
        }
        return packageName.toLowerCase();
    }
}
