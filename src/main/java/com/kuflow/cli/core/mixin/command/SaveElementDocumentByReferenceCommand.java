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
import com.kuflow.rest.model.TaskElementValueDocumentItem;
import com.kuflow.rest.model.TaskSaveElementCommand;
import java.util.List;
import java.util.stream.Collectors;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = CommandType.Key.SAVE_ELEMENT_DOCUMENT_BY_REFERENCE, mixinStandardHelpOptions = true)
public class SaveElementDocumentByReferenceCommand extends AbstractCommand implements Runnable {

    @ParentCommand
    private MainCommand mainCommand;

    @Mixin
    public LoggingMixin loggingMixin;

    @Mixin
    public SaveElementMixin saveElementMixin;

    @Parameters(
        description = "List of element value URIs. Example ku:task/d0b7b39e-3724-325a-92d5-743d6ef8ba98/element-value/c79158ae-38a9-4381-9cca-f1efd68411e7",
        arity = "1..*"
    )
    private List<String> uris;

    @Override
    public void run() {
        TaskSaveElementCommand taskSaveElementCommand = new TaskSaveElementCommand();
        taskSaveElementCommand.setElementDefinitionCode(this.saveElementMixin.elementCode);
        taskSaveElementCommand.setElementValueValid(this.saveElementMixin.valid);

        List<TaskElementValueDocumentItem> valueDocumentItems =
            this.uris.stream()
                .map(uri -> {
                    TaskElementValueDocumentItem valueDocumentItem = new TaskElementValueDocumentItem();
                    valueDocumentItem.setUri(uri);

                    return valueDocumentItem;
                })
                .collect(Collectors.toList());

        taskSaveElementCommand.setElementValueAsDocumentList(valueDocumentItems);

        super
            .getKuFlowRestClient(this.mainCommand.getEnvironmentProperties())
            .getTaskOperations()
            .actionsTaskSaveElement(this.saveElementMixin.taskId, taskSaveElementCommand);
    }
}
