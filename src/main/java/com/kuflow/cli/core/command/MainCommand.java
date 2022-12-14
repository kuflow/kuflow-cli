/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.cli.core.command;

import com.kuflow.cli.core.model.EnvironmentProperties;
import io.github.cdimascio.dotenv.Dotenv;
import java.net.URL;
import java.nio.file.Path;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "kuflowctl", mixinStandardHelpOptions = true, subcommands = SaveElementCommand.class)
public class MainCommand implements Runnable {

    private static final String KUFLOW_ENVIRONMENT_FILE = ".kuflow";

    private static final String KUFLOW_ENV_VAR__ENDPOINT = "KUFLOW_ENDPOINT";
    private static final String KUFLOW_ENV_VAR__CLIENT_ID = "KUFLOW_CLIENT_ID";
    private static final String KUFLOW_ENV_VAR__CLIENT_SECRET = "KUFLOW_CLIENT_SECRET";

    @Option(names = "--silent", negatable = true, description = "Silent output. False by default.")
    private boolean silent = false;

    @ArgGroup(exclusive = true, multiplicity = "0..1")
    EnvFileOrEnvOptions envFileOrEnvOptions;

    static class EnvFileOrEnvOptions {

        @Option(names = "--environment-file", description = "environmentFile")
        private Path environmentFile;

        @ArgGroup(exclusive = false, multiplicity = "1")
        EnvOptions envOptions;
    }

    static class EnvOptions {

        @Option(names = "--endpoint", description = "Endpoint", required = true)
        private URL endpoint;

        @Option(names = "--client-id", description = "clientId", required = true)
        private String clientId;

        @Option(names = "--client-secret", description = "clientSecret", required = true)
        private String clientSecret;
    }

    @Override
    public void run() {
        EnvironmentProperties environmentProperties = this.getEnvironmentProperties();
        if (environmentProperties == null) {
            new CommandLine(this).usage(System.out);
        }
    }

    /**
     * Order of precedence for read access configuration.
     * - 1) Command line options, Through:
     *    a) Individual options
     *    b) Specified EnvFile or EnvVars (if exists the var take precedence)
     * - 2) Default EnvFile location or EnvironmentVars (if exists take precedence)
     * @return
     */
    public EnvironmentProperties getEnvironmentProperties() {
        if (this.envFileOrEnvOptions != null) {
            if (this.envFileOrEnvOptions.envOptions != null) {
                EnvOptions envOptions = this.envFileOrEnvOptions.envOptions;
                return new EnvironmentProperties(envOptions.endpoint.toString(), envOptions.clientId, envOptions.clientSecret);
            } else if (this.envFileOrEnvOptions.environmentFile != null) {
                return this.loadFromEnvironmentFileOrEnvironmentProperty(
                        this.envFileOrEnvOptions.environmentFile.getParent().toString(),
                        this.envFileOrEnvOptions.environmentFile.getFileName().toString()
                    );
            }
        }

        return this.loadFromEnvironmentFileOrEnvironmentProperty(System.getProperty("user.home"), KUFLOW_ENVIRONMENT_FILE);
    }

    private EnvironmentProperties loadFromEnvironmentFileOrEnvironmentProperty(String directory, String fileName) {
        Dotenv dotenv = Dotenv.configure().directory(directory).filename(fileName).ignoreIfMissing().load();

        boolean error = false;
        String endpoint = dotenv.get(KUFLOW_ENV_VAR__ENDPOINT);
        error |= !this.checkDefined(endpoint, KUFLOW_ENV_VAR__ENDPOINT);

        String clientId = dotenv.get(KUFLOW_ENV_VAR__CLIENT_ID);
        error |= !this.checkDefined(clientId, KUFLOW_ENV_VAR__CLIENT_ID);

        String clientSecret = dotenv.get(KUFLOW_ENV_VAR__CLIENT_SECRET);
        error |= !this.checkDefined(clientSecret, KUFLOW_ENV_VAR__CLIENT_SECRET);

        if (error) {
            return null;
        }

        return new EnvironmentProperties(endpoint, clientId, clientSecret);
    }

    private boolean checkDefined(String value, String valueName) {
        if (value == null || value == "") {
            // System.err.println(String.format("No %s defined", valueName));
            return false;
        }

        return true;
    }
}
