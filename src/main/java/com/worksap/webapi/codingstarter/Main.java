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
import com.worksap.webapi.codingstarter.view.utils.Utils;
import com.worksap.webapi.codingstarter.view.utils.Shortcut;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class Main {
    public static void main(String[] args) throws Exception {
        ApplicationOption applicationOption = parseOption(args);
        if (applicationOption == null) {
            System.exit(-1);
        }

        Properties properties = new Properties();
        properties.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        properties.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        VelocityEngine ve = new VelocityEngine(properties);

        Swagger swagger = new SwaggerParser().read(applicationOption.getApiSpecPath());
        Utils utils = new Utils();

        VelocityContext context = new VelocityContext();
        context.put("api", swagger);
        context.put("option", applicationOption);
        context.put("utils", utils);
        context.put("shortcut", new Shortcut());

        VelocityTemplateRenderer templateRenderer = new VelocityTemplateRenderer(ve, context);
        APIGatewaySwaggerRenderer apiGatewaySwaggerRenderer = new APIGatewaySwaggerRenderer(
                applicationOption, ve, context, utils);
        StructureVisitor visitor = new StructureVisitor(applicationOption, templateRenderer, apiGatewaySwaggerRenderer);
        visitor.visit(loadStructure(applicationOption, ve, context));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadStructure(ApplicationOption applicationOption, VelocityEngine ve, VelocityContext context) {
        Template template = ve.getTemplate("/structures/" + applicationOption.getStructure() + ".yaml.vm");
        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        String yamlDocument = writer.toString();
        log.debug("Output tree:\n{}", yamlDocument);
        return (Map<String, Object>) new Yaml().load(yamlDocument);
    }

    private static ApplicationOption parseOption(String[] args) {
        ApplicationOption applicationOption = new ApplicationOption();
        CmdLineParser parser = new CmdLineParser(applicationOption);
        try {
            parser.parseArgument(args);
            if (applicationOption.isHelp()) {
                parser.printSingleLineUsage(System.out); System.out.println();
                System.out.println();
                parser.printUsage(System.out);
                return null;
            }
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            return null;
        }
        return applicationOption;
    }
}
