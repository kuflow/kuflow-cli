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

import com.azure.core.util.BinaryData;
import com.kuflow.cli.core.enumeration.CommandType;
import com.kuflow.cli.core.util.FileUtils;
import com.kuflow.rest.model.Document;
import com.kuflow.rest.model.TaskSaveElementValueDocumentCommand;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(name = CommandType.Key.SAVE_ELEMENT_DOCUMENT, mixinStandardHelpOptions = true)
public class SaveElementDocumentCommand extends AbstractCommand implements Runnable {

    @ParentCommand
    private MainCommand mainCommand;

    @Mixin
    public LoggingMixin loggingMixin;

    @Mixin
    public SaveElementMixin saveElementMixin;

    @Option(names = { "-doi", "--document-id" }, description = "Document identifier. Can be optional.", arity = "0..1")
    private Optional<UUID> documentId;

    @Parameters(description = "File path to upload", arity = "1..*")
    private List<Path> paths;

    @Override
    public void run() {
        List<Path> invalidFiles = this.paths.stream().filter(p -> Files.isDirectory(p)).collect(Collectors.toList());

        if (!invalidFiles.isEmpty()) {
            String invalidPaths = invalidFiles.stream().map(p -> p.toString()).collect(joining(", "));
            throw new RuntimeException(String.format("Directories are not supported, specify file paths. [%s]", invalidPaths));
        }

        boolean isMultiple = this.paths.size() > 1;

        this.paths.stream()
            .forEach(p -> {
                try {
                    this.uploadFile(p, isMultiple);
                } catch (Exception e) {
                    throw new RuntimeException(String.format("Unable to upload file: %s", p), e);
                }
            });
    }

    private void uploadFile(Path fileToUpload, boolean isMultiple) throws Exception {
        TaskSaveElementValueDocumentCommand command = new TaskSaveElementValueDocumentCommand();
        command.setElementDefinitionCode(this.saveElementMixin.elementCode);
        command.setElementValueId((isMultiple) ? null : this.documentId.orElse(null));
        command.setElementValueValid(this.saveElementMixin.valid);

        BinaryData file = BinaryData.fromFile(fileToUpload);
        Document document = new Document().setFileContent(file);
        document.setFileName(fileToUpload.getFileName().toString());
        document.setContentType(FileUtils.guessMimeType(fileToUpload));

        super
            .getKuFlowRestClient(this.mainCommand.getEnvironmentProperties())
            .getTaskOperations()
            .actionsTaskSaveElementValueDocument(this.saveElementMixin.taskId, command, document);
    }
}
