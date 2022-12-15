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
import com.kuflow.rest.model.TaskSaveElementCommand;
import java.util.List;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;

@Command(name = CommandType.Key.SAVE_ELEMENT_FIELD, mixinStandardHelpOptions = true)
public class SaveElementFieldCommand extends AbstractCommand implements Runnable {

    @Mixin
    public LoggingMixin loggingMixin;

    @Mixin
    public MainMixin mainMixin;

    @Mixin
    private SaveElementMixin saveElementMixin;

    // TODO KF: All CLI parameters are treated as a list of strings. To be improved in the future.
    // @Option(names = { "-ft", "--field-type" }, description = "Field type, 'TEXT' by default", defaultValue = FieldType.Key.STRING)
    // private FieldType fieldType = FieldType.STRING;

    @Parameters(description = "value", arity = "1..*")
    private List<String> value;

    @Override
    public void run() {
        TaskSaveElementCommand taskSaveElementCommand = new TaskSaveElementCommand();
        taskSaveElementCommand.setElementDefinitionCode(this.saveElementMixin.elementCode);
        taskSaveElementCommand.setElementValueAsStringList(this.value);
        taskSaveElementCommand.setElementValueValid(this.saveElementMixin.valid);

        super
            .getKuFlowRestClient(this.getEnvironmentProperties(this.mainMixin))
            .getTaskOperations()
            .actionsTaskSaveElement(this.saveElementMixin.taskId, taskSaveElementCommand);
    }
}
