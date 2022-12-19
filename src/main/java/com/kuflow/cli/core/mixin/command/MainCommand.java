/*
 * The MIT License
 * Copyright © 2021-present KuFlow S.L.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.kuflow.cli.core.mixin.command;

import static java.util.stream.Collectors.joining;

import com.kuflow.cli.core.model.EnvironmentProperties;
import com.kuflow.cli.core.util.Constants;
import com.kuflow.cli.core.util.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(
    name = "kuflowctl",
    mixinStandardHelpOptions = true,
    subcommands = { AppendLogCommand.class, SaveElementFieldCommand.class, SaveElementDocumentCommand.class }
)
public class MainCommand implements Runnable {

    @Mixin
    public LoggingMixin loggingMixin;

    @ArgGroup(exclusive = true, multiplicity = "0..1")
    public EnvFileOrEnvOptions envFileOrEnvOptions;

    @Option(
        names = "--endpoint",
        description = "KuFlow Api endpoint. By default is " + Constants.KUFLOW_REST_API_ENDPOINT,
        defaultValue = Constants.KUFLOW_REST_API_ENDPOINT
    )
    private URL endpoint;

    static class EnvFileOrEnvOptions {

        @Option(
            names = "--environment-file",
            description = "Environment file in YAML format. More info: https://github.com/kuflow/kuflow-cli"
        )
        private Path environmentFile;

        @ArgGroup(exclusive = false, multiplicity = "1")
        private EnvOptions envOptions;
    }

    static class EnvOptions {

        @Option(names = "--client-id", description = "The 'Application' identifier", required = true)
        private String clientId;

        @Option(names = "--client-secret", description = "The 'Application' token", required = true)
        private String clientSecret;
    }

    @Override
    public void run() {
        new CommandLine(this).usage(System.out);
    }

    /**
     * Order of precedence for read access configuration.
     * - 1) Command line options, Through:
     *    a) Individual options
     *    b) Specified EnvFile
     * - 2) Environment Vars
     * - 3) Default EnvFile location
     * @return
     */
    public EnvironmentProperties getEnvironmentProperties() {
        EnvironmentProperties environmentProperties = new EnvironmentProperties();

        // Try to load from commandline options or command line specific configuration file
        environmentProperties.getKuflow().setEndpoint(this.endpoint.toString());
        if (this.envFileOrEnvOptions != null) {
            if (this.envFileOrEnvOptions.envOptions != null) {
                EnvOptions envOptions = this.envFileOrEnvOptions.envOptions;
                environmentProperties.getKuflow().setClientId(envOptions.clientId);
                environmentProperties.getKuflow().setClientSecret(envOptions.clientSecret);
            } else if (this.envFileOrEnvOptions.environmentFile != null) {
                Path environmentFile = this.envFileOrEnvOptions.environmentFile;
                environmentProperties =
                    this.loadFromEnvironmentFile(
                            environmentProperties,
                            environmentFile.getParent().toString(),
                            environmentFile.getFileName().toString()
                        );
            }
        }

        if (environmentProperties.getKuflow().isFilled()) {
            return environmentProperties;
        }

        // Try to load from environment vars
        environmentProperties = this.loadFromEnvironmentVariables(environmentProperties);
        if (environmentProperties.getKuflow().isFilled()) {
            return environmentProperties;
        }

        // Try to load from default configuration file
        environmentProperties =
            this.loadFromEnvironmentFile(environmentProperties, System.getProperty("user.home"), Constants.KUFLOW_ENVIRONMENT_FILE);

        if (!environmentProperties.getKuflow().isFilled()) {
            throw new RuntimeException(String.format("Invalid environment properties. %s", environmentProperties.getKuflow()));
        }

        return environmentProperties;
    }

    private EnvironmentProperties loadFromEnvironmentFile(EnvironmentProperties currentProperties, String directory, String fileName) {
        Constructor constructor = new Constructor(EnvironmentProperties.class);
        constructor.setPropertyUtils(
            new PropertyUtils() {
                @Override
                public Property getProperty(Class<?> type, String name) {
                    // To Camel-case
                    name =
                        Arrays
                            .stream(name.split("-"))
                            .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase())
                            .collect(joining());
                    // First character in lower case
                    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                    return super.getProperty(type, name);
                }
            }
        );

        Yaml yaml = new Yaml(constructor);

        EnvironmentProperties result = null;
        File file = new File(directory, fileName);
        try (InputStream inputStream = new FileInputStream(file)) {
            result = yaml.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error reading file %s", file.getName()), e);
        }

        if (currentProperties != null) {
            return currentProperties.merge(result);
        }

        return result;
    }

    private EnvironmentProperties loadFromEnvironmentVariables(EnvironmentProperties currentProperties) {
        String endpoint = System.getProperty(Constants.KUFLOW_ENV_VAR__ENDPOINT);
        String clientId = System.getProperty(Constants.KUFLOW_ENV_VAR__CLIENT_ID);
        String clientSecret = System.getProperty(Constants.KUFLOW_ENV_VAR__CLIENT_SECRET);

        EnvironmentProperties environmentProperties = new EnvironmentProperties();

        if (StringUtils.isBlank(currentProperties.getKuflow().getEndpoint())) {
            environmentProperties.getKuflow().setEndpoint(endpoint);
        }

        if (StringUtils.isBlank(currentProperties.getKuflow().getClientId())) {
            environmentProperties.getKuflow().setClientId(clientId);
        }

        if (StringUtils.isBlank(currentProperties.getKuflow().getClientSecret())) {
            environmentProperties.getKuflow().setClientSecret(clientSecret);
        }

        return environmentProperties;
    }
}
