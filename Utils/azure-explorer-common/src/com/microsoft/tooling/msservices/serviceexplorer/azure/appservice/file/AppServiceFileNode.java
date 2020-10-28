/*
 * Copyright (c) Microsoft Corporation
 *
 * All rights reserved.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.appservice.file;

import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.azure.toolkit.lib.appservice.file.AppServiceFile;
import com.microsoft.azure.toolkit.lib.appservice.file.AppServiceFileService;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.serviceexplorer.AzureRefreshableNode;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.WebAppModule;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;
import rx.Observable;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

@Log
public class AppServiceFileNode extends AzureRefreshableNode {
    private static final String MODULE_ID = WebAppModule.class.getName();
    private static final String ERROR_DOWNLOADING = "Failed to download file[%s] to [%s].";
    private static final String SUCCESS_DOWNLOADING = "File[%s] is successfully downloaded to [%s].";
    private final AppServiceFileService fileService;
    private final AppServiceFile file;

    public AppServiceFileNode(final AppServiceFile file, final Node parent, AppServiceFileService service) {
        super(file.getName(), file.getName(), parent, null);
        this.file = file;
        this.fileService = service;
        if (this.file.getType() != AppServiceFile.Type.DIRECTORY) {
            this.addDownloadAction();
        }
    }

    private void addDownloadAction() {
        this.addAction("Download", new NodeActionListener() {
            @Override
            protected void actionPerformed(final NodeActionEvent e) {
                final File dest = DefaultLoader.getUIHelper().showFileSaver("Download", AppServiceFileNode.this.file.getName());
                if (Objects.nonNull(dest)) {
                    AppServiceFileNode.this.saveFileContentTo(dest);
                }
            }
        });
    }

    private void saveFileContentTo(final File dest) {
        final String fileName = this.file.getName();
        final String path = this.file.getPath();
        final String name = String.format("downloading %s ...", fileName);
        DefaultLoader.getIdeHelper().runInBackground(this.getProject(), name, true, false, null, () -> {
            final SettableFuture<Boolean> future = SettableFuture.create();
            final Observable<byte[]> content = this.loadFileContent(true);
            content.doOnError((e) -> future.set(false)).doOnCompleted(() -> future.set(true)).subscribe((data) -> {
                try {
                    FileUtils.writeByteArrayToFile(dest, data, true);
                } catch (final IOException e) {
                    log.log(Level.INFO, ERROR_DOWNLOADING, e);
                    throw new RuntimeException(e);
                }
            });
            try {
                if (future.get()) {
                    final String message = String.format(SUCCESS_DOWNLOADING, fileName, dest.getAbsolutePath());
                    DefaultLoader.getUIHelper().showInfo(AppServiceFileNode.this, message);
                    return;
                }
            } catch (final InterruptedException | ExecutionException ignored) {
            }
            final String message = String.format(ERROR_DOWNLOADING, fileName, dest.getAbsolutePath());
            DefaultLoader.getUIHelper().showError(AppServiceFileNode.this, message);
        });
    }

    @Override
    protected void refreshItems() {
        if (this.file.getType() != AppServiceFile.Type.DIRECTORY) {
            this.loadFileContent(true);
            return;
        }
        this.fileService.getFilesInDirectory(this.file.getPath()).stream()
                        .map(file -> new AppServiceFileNode(file, this, fileService))
                        .forEach(this::addChildNode);
    }

    @Override
    public void onNodeDblClicked(Object context) {
        final Runnable runnable = () -> {
            this.loadFileContent(false);
            DefaultLoader.getIdeHelper().openAppServiceFile(this.file, context);
        };
        final String message = String.format("opening file %s", this.file.getName());
        DefaultLoader.getIdeHelper().runInBackground(this.getProject(), message, false, true, null, runnable);
    }

    private synchronized Observable<byte[]> loadFileContent(boolean force) {
        if (force || Objects.isNull(this.file.getContent())) {
            final Observable<byte[]> content = this.fileService.getFileContent(this.file.getPath());
            this.file.setContent(content);
        }
        return this.file.getContent();
    }

    @Override
    public Icon getIcon() {
        return DefaultLoader.getIdeHelper().getFileTypeIcon(this.file.getName(), this.file.getType() == AppServiceFile.Type.DIRECTORY);
    }
}
