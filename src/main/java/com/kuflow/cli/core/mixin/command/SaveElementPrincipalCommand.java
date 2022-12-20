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

import com.kuflow.cli.core.enumeration.CommandType;
import com.kuflow.rest.model.PrincipalType;
import com.kuflow.rest.model.TaskElementValuePrincipalItem;
import com.kuflow.rest.model.TaskSaveElementCommand;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import picocli.CommandLine.Command;
import picocli.CommandLine.IParameterConsumer;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = CommandType.Key.SAVE_ELEMENT_PRINCIPAL, mixinStandardHelpOptions = true)
public class SaveElementPrincipalCommand extends AbstractCommand implements Runnable {

    @ParentCommand
    private MainCommand mainCommand;

    @Mixin
    public LoggingMixin loggingMixin;

    @Mixin
    public SaveElementMixin saveElementMixin;

    @Parameters(
        description = "List of values in format 'PrincipalType=Value' where PrincipalType is a valid type and Value is a UUID.",
        arity = "1..*",
        parameterConsumer = TaskElementValuePrincipalItemConverter.class
    )
    private List<TaskElementValuePrincipalItem> values;

    @Override
    public void run() {
        TaskSaveElementCommand taskSaveElementCommand = new TaskSaveElementCommand();
        taskSaveElementCommand.setElementDefinitionCode(this.saveElementMixin.elementCode);
        taskSaveElementCommand.setElementValueValid(this.saveElementMixin.valid);
        taskSaveElementCommand.setElementValueAsPrincipalList(this.values);

        super
            .getKuFlowRestClient(this.mainCommand.getEnvironmentProperties())
            .getTaskOperations()
            .actionsTaskSaveElement(this.saveElementMixin.taskId, taskSaveElementCommand);
    }

    static class TaskElementValuePrincipalItemConverter implements IParameterConsumer {

        @Override
        public void consumeParameters(Stack<String> args, ArgSpec argSpec, CommandSpec commandSpec) {
            List<TaskElementValuePrincipalItem> values = argSpec.getValue();
            if (values == null) {
                values = new LinkedList<>();
            }

            while (!args.isEmpty()) {
                String arg = args.pop();

                String[] splitted = arg.split("=");

                if (splitted.length != 2) {
                    throw new ParameterException(
                        commandSpec.commandLine(),
                        "Missing format for Principal specification. Please specify 'PrincipalType=Value'."
                    );
                }

                PrincipalType principalType = PrincipalType.fromString(splitted[0].toUpperCase());
                if (principalType == null) {
                    String message = String.format(
                        "Wrong specification. PrincipalType must be one of %s",
                        Arrays.stream(PrincipalType.values()).map(PrincipalType::name).collect(joining())
                    );

                    throw new ParameterException(commandSpec.commandLine(), message);
                }

                UUID value;
                try {
                    value = UUID.fromString(splitted[1]);
                } catch (IllegalArgumentException e) {
                    throw new ParameterException(commandSpec.commandLine(), "Wrong specification. Value must be a UUID");
                }

                TaskElementValuePrincipalItem principalItem = new TaskElementValuePrincipalItem();
                principalItem.setType(principalType);
                principalItem.setId(value);

                values.add(principalItem);

                // Not in this case, but if there are other distinct parameters: stop processing after a ';'
                if (";".equals(arg)) {
                    break;
                }
            }

            argSpec.setValue(values);
        }
    }
}
