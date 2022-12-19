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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

/**
 * This is a mixin that adds a {@code --verbose} option to a command.
 * This class will configure Log4j2, using the specified verbosity:
 * <ul>
 *   <li>{@code -vvv} : TRACE level is enabled</li>
 *   <li>{@code -vv} : DEBUG level is enabled</li>
 *   <li>{@code -v} : INFO level is enabled</li>
 *   <li>(not specified) : WARN level is enabled</li>
 * </ul>
 * With {@code ---silent} not logs are shown.
 * <p>
 *   Make sure that {@link #configureLoggers} is called before executing any command.
 *   This can be accomplished with:
 * </p><pre>
 * public static void main(String... args) {
 *     new CommandLine(new MyApp())
 *             .setExecutionStrategy(LoggingMixin::executionStrategy))
 *             .execute(args);
 * }
 * </pre>
 */
public class LoggingMixin {

    /**
     * This mixin is able to climb the command hierarchy because the
     * {@code @Spec(Target.MIXEE)}-annotated field gets a reference to the command where it is used.
     */
    @Spec(MIXEE)
    private CommandSpec mixee;

    private boolean[] verbosity = new boolean[0];

    private boolean silent = false;

    // Each subcommand that mixes in the LoggingMixin has its own instance of this class,
    // so there may be many LoggingMixin instances.
    // We want to store the verbosity value in a single, central place, so
    // we find the top-level command,
    // and store the verbosity level on our top-level command's LoggingMixin.
    private static LoggingMixin getTopLevelCommandLoggingMixin(CommandSpec commandSpec) {
        return ((MainCommand) commandSpec.root().userObject()).loggingMixin;
    }

    @Option(
        names = { "-v", "--verbose" },
        description = { "Specify multiple -v options to increase verbosity.", "For example, `-v -v -v` or `-vvv`" }
    )
    public void setVerbose(boolean[] verbosity) {
        getTopLevelCommandLoggingMixin(this.mixee).verbosity = verbosity;
    }

    public boolean[] getVerbosity() {
        return getTopLevelCommandLoggingMixin(this.mixee).verbosity;
    }

    @Option(names = { "-s", "--silent" }, negatable = true, description = "Silent output. False by default.")
    public void setSilent(boolean silent) {
        getTopLevelCommandLoggingMixin(this.mixee).silent = silent;
    }

    public boolean getSilent() {
        return getTopLevelCommandLoggingMixin(this.mixee).silent;
    }

    /**
     * Configures Log4j2 based on the verbosity level of the top-level command's LoggingMixin,
     * before invoking the default execution strategy ({@link picocli.CommandLine.RunLast RunLast}) and returning the result.
     * <p>
     *   Example usage:
     * </p>
     * <pre>
     * public void main(String... args) {
     *     new CommandLine(new MyApp())
     *             .setExecutionStrategy(LoggingMixin::executionStrategy))
     *             .execute(args);
     * }
     * </pre>
     *
     * @param parseResult represents the result of parsing the command line
     * @return the exit code of executing the most specific subcommand
     */
    public static int executionStrategy(ParseResult parseResult) {
        getTopLevelCommandLoggingMixin(parseResult.commandSpec()).configureLoggers();
        return new CommandLine.RunLast().execute(parseResult);
    }

    /**
     * Configures the Log4j2 console appender(s), using the specified verbosity:
     * <ul>
     *   <li>{@code -vvv} : enable TRACE level</li>
     *   <li>{@code -vv} : enable DEBUG level</li>
     *   <li>{@code -v} : enable INFO level</li>
     *   <li>(not specified) : enable WARN level</li>
     * </ul>
     */
    public void configureLoggers() {
        Level level = getTopLevelCommandLoggingMixin(this.mixee).calculateLogLevel();

        LoggerContext loggerContext = LoggerContext.getContext(false);
        LoggerConfig rootConfig = loggerContext.getConfiguration().getRootLogger();
        for (Appender appender : rootConfig.getAppenders().values()) {
            if (appender instanceof ConsoleAppender) {
                rootConfig.removeAppender(appender.getName());
                rootConfig.addAppender(appender, level, null);
            }
        }

        if (rootConfig.getLevel().isMoreSpecificThan(level)) {
            rootConfig.setLevel(level);
        }

        loggerContext.updateLoggers(); // apply the changes
    }

    /**
     * Get right Log level
     *
     * @return the Log Level for the current options
     */
    private Level calculateLogLevel() {
        if (this.getSilent()) {
            return Level.OFF;
        }

        switch (this.getVerbosity().length) {
            case 0:
                return Level.WARN;
            case 1:
                return Level.INFO;
            case 2:
                return Level.DEBUG;
            default:
                return Level.TRACE;
        }
    }

    /**
     * Reconfigure Log4j Using ConfigurationBuilder with the Configurator
     *
     * @return Initialized LoggerContext
     */
    public static LoggerContext initializeLog4j() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setStatusLevel(Level.ERROR); // Internal log4j2 errors
        builder.setConfigurationName("KuFlowCtlLoggginConfiguration");

        LayoutComponentBuilder layoutBuilder = builder
            .newLayout("PatternLayout")
            .addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable");

        AppenderComponentBuilder appenderBuilder = builder
            .newAppender("ConsoleAppender", ConsoleAppender.PLUGIN_NAME)
            .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT)
            .add(layoutBuilder);
        builder.add(appenderBuilder);

        RootLoggerComponentBuilder rootLogerBuilder = builder
            .newRootLogger(Level.ERROR)
            .addAttribute("additivity", false)
            .add(builder.newAppenderRef("ConsoleAppender").addAttribute("level", Level.WARN));

        builder.add(rootLogerBuilder);

        return Configurator.initialize(builder.build());
    }
}
