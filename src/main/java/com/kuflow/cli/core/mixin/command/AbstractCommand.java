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

import com.kuflow.cli.core.mixin.command.MainMixin.EnvFileOrEnvOptions;
import com.kuflow.cli.core.mixin.command.MainMixin.EnvOptions;
import com.kuflow.cli.core.model.EnvironmentProperties;
import com.kuflow.cli.core.util.RestClientFactory;
import com.kuflow.rest.KuFlowRestClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

public abstract class AbstractCommand {

    private static final String KUFLOW_ENVIRONMENT_FILE = ".kuflow.yml";

    private static final String KUFLOW_ENV_VAR__ENDPOINT = "KUFLOW_ENDPOINT";
    private static final String KUFLOW_ENV_VAR__CLIENT_ID = "KUFLOW_CLIENT_ID";
    private static final String KUFLOW_ENV_VAR__CLIENT_SECRET = "KUFLOW_CLIENT_SECRET";

    private KuFlowRestClient kuFlowRestClient;

    protected KuFlowRestClient getKuFlowRestClient(EnvironmentProperties properties) {
        if (this.kuFlowRestClient == null) {
            this.kuFlowRestClient = RestClientFactory.kuFlowRestClient(properties);
        }

        return this.kuFlowRestClient;
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
    public EnvironmentProperties getEnvironmentProperties(MainMixin mainMixin) {
        EnvironmentProperties propertiesEnvironmentFile = null;

        // Try to load from commandline options or command line specific configuration file
        if (mainMixin.getGlobalEnvFileOrEnvOptions() != null) {
            EnvFileOrEnvOptions envFileOrEnvOptions = mainMixin.getGlobalEnvFileOrEnvOptions();
            if (envFileOrEnvOptions.getGlobalEnvOptions() != null) {
                EnvOptions envOptions = envFileOrEnvOptions.getGlobalEnvOptions();
                propertiesEnvironmentFile = new EnvironmentProperties();
                propertiesEnvironmentFile.getKuflow().setEndpoint(envOptions.getGlobalEndpoint().toString());
                propertiesEnvironmentFile.getKuflow().setClientId(envOptions.getGlobalClientId());
                propertiesEnvironmentFile.getKuflow().setClientSecret(envOptions.getGlobalClientSecret());

                return propertiesEnvironmentFile;
            } else if (envFileOrEnvOptions.getGlobalEnvironmentFile() != null) {
                Path environmentFile = envFileOrEnvOptions.getGlobalEnvironmentFile();
                propertiesEnvironmentFile =
                    this.loadFromEnvironmentFile(environmentFile.getParent().toString(), environmentFile.getFileName().toString());
                if (propertiesEnvironmentFile != null) {
                    return propertiesEnvironmentFile;
                }
            }
        }

        // Try to load from environment vars
        propertiesEnvironmentFile = this.loadFromEnvironmentVariables();
        if (propertiesEnvironmentFile != null) {
            return propertiesEnvironmentFile;
        }

        // try to load from default configuration file
        return this.loadFromEnvironmentFile(System.getProperty("user.home"), KUFLOW_ENVIRONMENT_FILE);
    }

    private EnvironmentProperties loadFromEnvironmentFile(String directory, String fileName) {
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

        if (!result.getKuflow().isFilled()) {
            return null;
        }

        return result;
    }

    private EnvironmentProperties loadFromEnvironmentVariables() {
        String endpoint = System.getProperty(KUFLOW_ENV_VAR__ENDPOINT);
        String clientId = System.getProperty(KUFLOW_ENV_VAR__CLIENT_ID);
        String clientSecret = System.getProperty(KUFLOW_ENV_VAR__CLIENT_SECRET);

        EnvironmentProperties propertiesEnvironmentFile = new EnvironmentProperties();
        propertiesEnvironmentFile.getKuflow().setEndpoint(endpoint);
        propertiesEnvironmentFile.getKuflow().setClientId(clientId);
        propertiesEnvironmentFile.getKuflow().setClientSecret(clientSecret);

        if (!propertiesEnvironmentFile.getKuflow().isFilled()) {
            return null;
        }

        return propertiesEnvironmentFile;
    }

    // private EnvironmentProperties loadFromEnvironmentFileOrEnvironmentProperty(String directory, String fileName) {
    //     Dotenv dotenv = Dotenv.configure().directory(directory).filename(fileName).ignoreIfMissing().load();

    //     boolean error = false;
    //     String endpoint = dotenv.get(KUFLOW_ENV_VAR__ENDPOINT);
    //     error |= !this.checkDefined(endpoint, KUFLOW_ENV_VAR__ENDPOINT);

    //     String clientId = dotenv.get(KUFLOW_ENV_VAR__CLIENT_ID);
    //     error |= !this.checkDefined(clientId, KUFLOW_ENV_VAR__CLIENT_ID);

    //     String clientSecret = dotenv.get(KUFLOW_ENV_VAR__CLIENT_SECRET);
    //     error |= !this.checkDefined(clientSecret, KUFLOW_ENV_VAR__CLIENT_SECRET);

    //     if (error) {
    //         return null;
    //     }

    //     return new EnvironmentProperties(endpoint, clientId, clientSecret);
    // }

    private boolean checkDefined(String value, String valueName) {
        if (value == null || value == "") {
            return false;
        }

        return true;
    }
}
