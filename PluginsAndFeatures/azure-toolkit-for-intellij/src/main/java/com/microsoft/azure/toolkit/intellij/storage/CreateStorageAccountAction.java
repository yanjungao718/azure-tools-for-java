/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.storage;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.storage.component.StorageAccountCreationDialog;
import com.microsoft.azure.toolkit.intellij.storage.task.CreateStorageAccountTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.storage.model.StorageAccountConfig;
import com.microsoft.intellij.actions.AzureSignInAction;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.Name;
import com.microsoft.tooling.msservices.serviceexplorer.AzureActionEnum;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.storage.StorageModule;

@Name("Create")
public class CreateStorageAccountAction extends NodeActionListener {

    private final StorageModule module;

    public CreateStorageAccountAction(StorageModule storageModule) {
        this.module = storageModule;
    }

    @Override
    public AzureActionEnum getAction() {
        return AzureActionEnum.CREATE;
    }

    @Override
    public void actionPerformed(NodeActionEvent e) {
        final Project project = (Project) module.getProject();
        AzureSignInAction.requireSignedIn(project, () -> doActionPerformed(true, project));
    }

    private void doActionPerformed(boolean isLoggedIn, Project project) {
        final StorageAccountCreationDialog dialog = new StorageAccountCreationDialog(project);
        dialog.setOkActionListener((data) -> this.createStorageAccount(data, project, dialog));
        dialog.show();
    }

    private void createStorageAccount(final StorageAccountConfig config, final Project project, StorageAccountCreationDialog dialog) {
        final Runnable runnable = () -> {
            final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
            indicator.setIndeterminate(true);
            DefaultLoader.getIdeHelper().invokeLater(dialog::close);
            new CreateStorageAccountTask(config).execute();
        };
        final String progressMessage = Node.getProgressMessage(AzureActionEnum.CREATE.getDoingName(), StorageModule.MODULE_NAME, config.getName());
        AzureTaskManager.getInstance().runInBackground(new AzureTask<>(project, progressMessage, false, runnable));
    }

}
