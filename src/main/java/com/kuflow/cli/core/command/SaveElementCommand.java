/*
 * Copyright (c) 2021-present KuFlow S.L.
 *
 * All rights reserved.
 */

package com.kuflow.cli.core.command;

import com.azure.core.util.BinaryData;
import com.kuflow.cli.core.enumeration.CommandType;
import com.kuflow.rest.model.Document;
import com.kuflow.rest.model.TaskSaveElementCommand;
import com.kuflow.rest.model.TaskSaveElementValueDocumentCommand;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(
    name = CommandType.Key.SAVE_ELEMENT,
    mixinStandardHelpOptions = true,
    subcommands = { SaveElementCommand.FieldSubCommand.class, SaveElementCommand.DocumentSubCommand.class }
)
public class SaveElementCommand implements Runnable {

    @Option(names = { "-t", "--task-id" }, description = "Task identifier", required = true)
    private UUID taskId;

    @Option(names = { "-e", "--element-code" }, description = "Task element code")
    private String elementCode;

    // TODO KF: In CLI, "valid" is only supported for all element values, not individual values.
    @Option(
        names = "--valid",
        negatable = true,
        description = "Mark element value/s as valid. Is only supported for all element values, not individual values. True by default."
    )
    private boolean valid = true;

    @ParentCommand
    private MainCommand mainElementCommand;

    @Override
    public void run() {
        new CommandLine(this).usage(System.out);
    }

    @Command(name = CommandType.Key.SAVE_ELEMENT__FIELD, mixinStandardHelpOptions = true)
    static class FieldSubCommand extends AbstractCommand implements Runnable {

        // TODO KF: All CLI parameters are treated as a list of strings. To be improved in the future.
        // @Option(names = { "-ft", "--field-type" }, description = "Field type, 'TEXT' by default", defaultValue = FieldType.Key.STRING)
        // private FieldType fieldType = FieldType.STRING;

        @Parameters(description = "value", arity = "1..*")
        private List<String> value;

        @ParentCommand
        private SaveElementCommand saveElementCommand;

        @Override
        public void run() {
            TaskSaveElementCommand taskSaveElementCommand = new TaskSaveElementCommand();
            taskSaveElementCommand.setElementDefinitionCode(this.saveElementCommand.elementCode);
            taskSaveElementCommand.setElementValueAsStringList(this.value);
            taskSaveElementCommand.setElementValueValid(this.saveElementCommand.valid);

            super
                .getKuFlowRestClient(this.saveElementCommand.mainElementCommand.getEnvironmentProperties())
                .getTaskOperations()
                .actionsTaskSaveElement(this.saveElementCommand.taskId, taskSaveElementCommand);
        }
    }

    @Command(name = CommandType.Key.SAVE_ELEMENT__DOCUMENT, mixinStandardHelpOptions = true)
    static class DocumentSubCommand extends AbstractCommand implements Runnable {

        @Option(
            names = { "-doi", "--document-id" },
            description = "Document identifier. Can be optional. In a multiple document element, it match with a specific document.",
            arity = "0..1"
        )
        private Optional<UUID> documentId;

        @Parameters(description = "File path to upload", arity = "1..*")
        private List<Path> paths;

        @ParentCommand
        private SaveElementCommand saveElementCommand;

        @Override
        public void run() {
            List<Path> invalidFiles = this.paths.stream().filter(p -> Files.isDirectory(p)).collect(Collectors.toList());

            if (!invalidFiles.isEmpty()) {
                // TODO Loggin error if NOT SILENT. create custom exception and optional mapping
                // error code?
                System.out.println("Directories unssuported");
                throw new RuntimeException("Directories unssuported");
            }

            this.paths.stream()
                .forEach(p -> {
                    try {
                        this.uploadFile(p);
                    } catch (Exception e) {
                        // TODO Loggin error if NOT SILENT. create custom exception and optional mapping
                        throw new RuntimeException(e);
                    }
                });
        }

        private void uploadFile(Path fileToUpload) throws Exception {
            TaskSaveElementValueDocumentCommand command = new TaskSaveElementValueDocumentCommand();
            command.setElementDefinitionCode(this.saveElementCommand.elementCode);
            command.setElementValueId(this.documentId.orElse(null));
            command.setElementValueValid(this.saveElementCommand.valid);

            BinaryData file = BinaryData.fromFile(fileToUpload);
            Document document = new Document().setFileContent(file);
            super
                .getKuFlowRestClient(this.saveElementCommand.mainElementCommand.getEnvironmentProperties())
                .getTaskOperations()
                .actionsTaskSaveElementValueDocument(this.saveElementCommand.taskId, command, document);
        }
    }
}
