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

import com.kuflow.cli.core.enumeration.CommandType;
import com.kuflow.rest.model.Log;
import com.kuflow.rest.model.LogLevel;
import java.util.UUID;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = CommandType.Key.APPEND_LOG, mixinStandardHelpOptions = true)
public class AppendLogCommand extends AbstractCommand implements Runnable {

    @ParentCommand
    private MainCommand mainCommand;

    @Mixin
    public LoggingMixin loggingMixin;

    @Option(names = { "-t", "--task-id" }, description = "Task identifier", required = true)
    public UUID taskId;

    @Option(names = { "-lv", "--log-level" }, description = "Log level", required = true)
    private LogLevel logLevel;

    @Parameters(description = "value", arity = "1..1")
    private String value;

    @Override
    public void run() {
        Log log = new Log();
        log.setLevel(this.logLevel);
        log.setMessage(this.value);

        super.getKuFlowRestClient(this.mainCommand.getEnvironmentProperties()).getTaskOperations().actionsTaskAppendLog(this.taskId, log);
    }
}
