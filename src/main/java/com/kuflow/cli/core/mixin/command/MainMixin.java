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

import static picocli.CommandLine.Spec.Target.MIXEE;

import java.net.URL;
import java.nio.file.Path;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

/*
 * This is a complex class because we want store/access the "main mixin" in a signle global object and
 * not in every command.
 *
 * In other hand, the use of Mixins is needed in order to enable the use of options ("main mixin") before the command.
 *
 * Examples:
 * $kuflowctl save-element-field  --environment-file=/tmp  -t 71d62af1-ffb9-4ef0-be5c-8dbc8090641a -e code "My value"
 * $kuflowctl --environment-file=/tmp  save-element-field   -t 71d62af1-ffb9-4ef0-be5c-8dbc8090641a -e code "My value"
 *
 *
 */
@Command(mixinStandardHelpOptions = true) // add --help and --version to all commands that have this mixin
public class MainMixin {

    /**
     * This mixin is able to climb the command hierarchy because the
     * {@code @Spec(Target.MIXEE)}-annotated field gets a reference to the command where it is used.
     */
    @Spec(MIXEE)
    private CommandSpec mixee; // spec of the command where the @Mixin is used

    private EnvFileOrEnvOptions envFileOrEnvOptions;

    @ArgGroup(exclusive = true, multiplicity = "0..1")
    public void setEnvFileOrEnvOptions(EnvFileOrEnvOptions envFileOrEnvOptions) {
        getTopLevelCommandMainMixin(this.mixee).envFileOrEnvOptions = envFileOrEnvOptions;
        envFileOrEnvOptions.mainMixin = getTopLevelCommandMainMixin(this.mixee);
    }

    public static MainMixin getTopLevelCommandMainMixin(CommandSpec commandSpec) {
        return ((MainCommand) commandSpec.root().userObject()).mainMixin;
    }

    public EnvFileOrEnvOptions getGlobalEnvFileOrEnvOptions() {
        return getTopLevelCommandMainMixin(this.mixee).envFileOrEnvOptions;
    }

    static class EnvFileOrEnvOptions {

        private MainMixin mainMixin;

        private Path environmentFile;

        private EnvOptions envOptions = new EnvOptions();

        @Option(names = "--environment-file", description = "environmentFile")
        public void setEnvironmentFile(Path environmentFile) {
            EnvFileOrEnvOptions envFileOrEnvOptions = this.getOrCreateEnvFileOrEnvOptions();
            envFileOrEnvOptions.environmentFile = environmentFile;
        }

        public Path getGlobalEnvironmentFile() {
            if (!this.checkIfNullSafety()) {
                return null;
            }

            return getTopLevelCommandMainMixin(this.mainMixin.mixee).envFileOrEnvOptions.environmentFile;
        }

        @ArgGroup(exclusive = false, multiplicity = "1")
        public void setEnvOptions(EnvOptions envOptions) {
            this.envOptions = envOptions;
            envOptions.mainMixin = this.mainMixin;
        }

        public EnvOptions getGlobalEnvOptions() {
            if (!this.checkIfNullSafety()) {
                return null;
            }

            return this.mainMixin.getGlobalEnvFileOrEnvOptions().envOptions;
        }

        private EnvFileOrEnvOptions getOrCreateEnvFileOrEnvOptions() {
            EnvFileOrEnvOptions envFileOrEnvOptions = getTopLevelCommandMainMixin(this.mainMixin.mixee).envFileOrEnvOptions;
            if (envFileOrEnvOptions == null) {
                envFileOrEnvOptions = new EnvFileOrEnvOptions();
                getTopLevelCommandMainMixin(this.mainMixin.mixee).envFileOrEnvOptions = envFileOrEnvOptions;
            }

            return envFileOrEnvOptions;
        }

        private boolean checkIfNullSafety() {
            if (this.mainMixin == null || this.mainMixin.mixee == null) {
                return false;
            }
            MainMixin topLevelCommandMainMixin = getTopLevelCommandMainMixin(this.mainMixin.mixee);
            if (topLevelCommandMainMixin.envFileOrEnvOptions == null) {
                return false;
            }

            return true;
        }
    }

    static class EnvOptions {

        private MainMixin mainMixin;

        private URL endpoint;

        private String clientId;

        private String clientSecret;

        @Option(names = "--endpoint", description = "Endpoint", required = true)
        public void setEndpoint(URL endpoint) {
            EnvOptions envOptions = this.getOrCreateEnvOptions();
            envOptions.endpoint = endpoint;
        }

        public URL getGlobalEndpoint() {
            if (!this.checkIfNullSafety()) {
                return null;
            }

            return getTopLevelCommandMainMixin(this.mainMixin.mixee).envFileOrEnvOptions.envOptions.endpoint;
        }

        @Option(names = "--client-id", description = "clientId", required = true)
        public void setClientId(String clientId) {
            EnvOptions envOptions = this.getOrCreateEnvOptions();
            envOptions.clientId = clientId;
        }

        public String getGlobalClientId() {
            if (!this.checkIfNullSafety()) {
                return null;
            }

            return getTopLevelCommandMainMixin(this.mainMixin.mixee).envFileOrEnvOptions.envOptions.clientId;
        }

        @Option(names = "--client-secret", description = "clientSecret", required = true)
        public void setClientSecret(String clientSecret) {
            EnvOptions envOptions = this.getOrCreateEnvOptions();
            envOptions.clientSecret = clientSecret;
        }

        public String getGlobalClientSecret() {
            if (!this.checkIfNullSafety()) {
                return null;
            }

            return getTopLevelCommandMainMixin(this.mainMixin.mixee).envFileOrEnvOptions.envOptions.clientSecret;
        }

        private EnvOptions getOrCreateEnvOptions() {
            EnvFileOrEnvOptions envFileOrEnvOptions = getTopLevelCommandMainMixin(this.mainMixin.mixee).envFileOrEnvOptions;
            if (envFileOrEnvOptions == null) {
                envFileOrEnvOptions = new EnvFileOrEnvOptions();
                getTopLevelCommandMainMixin(this.mainMixin.mixee).envFileOrEnvOptions = envFileOrEnvOptions;
            }

            EnvOptions envOptions = envFileOrEnvOptions.envOptions;
            if (envOptions == null) {
                envOptions = new EnvOptions();
                getTopLevelCommandMainMixin(this.mainMixin.mixee).envFileOrEnvOptions.envOptions = envOptions;
            }
            return envOptions;
        }

        private boolean checkIfNullSafety() {
            if (this.mainMixin == null || this.mainMixin.mixee == null) {
                return false;
            }
            MainMixin topLevelCommandMainMixin = getTopLevelCommandMainMixin(this.mainMixin.mixee);
            if (topLevelCommandMainMixin.envFileOrEnvOptions == null || topLevelCommandMainMixin.envFileOrEnvOptions.envOptions == null) {
                return false;
            }

            return true;
        }
    }
}
