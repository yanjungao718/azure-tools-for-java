/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.webapp;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.legacy.appservice.AppServiceComboBox;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.AzureAppService;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.WebApp;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.legacy.webapp.WebAppService;

import java.util.List;
import java.util.stream.Collectors;

public class WebAppComboBox extends AppServiceComboBox<WebAppComboBoxModel> {

    public WebAppComboBox(final Project project) {
        super(project);
    }

    @Override
    protected void createResource() {
        // todo: hide deployment part in creation dialog
        WebAppCreationDialog webAppCreationDialog = new WebAppCreationDialog(project);
        webAppCreationDialog.setDeploymentVisible(false);
        webAppCreationDialog.setOkActionListener(webAppConfig -> {
            final WebAppComboBoxModel newModel =
                new WebAppComboBoxModel(WebAppService.convertConfig2Settings(webAppConfig));
            newModel.setNewCreateResource(true);
            WebAppComboBox.this.addItem(newModel);
            WebAppComboBox.this.setSelectedItem(newModel);
            AzureTaskManager.getInstance().runLater(webAppCreationDialog::close);
        });
        webAppCreationDialog.show();
    }

    @Override
    @AzureOperation(
        name = "webapp.list_apps",
        type = AzureOperation.Type.SERVICE
    )
    protected List<WebAppComboBoxModel> loadAppServiceModels() {
        final List<WebApp> webApps = Azure.az(AzureAppService.class).webapps(false);
        return webApps.stream().parallel()
                .filter(this::isJavaAppService)
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                .map(WebAppComboBoxModel::new)
                .collect(Collectors.toList());
    }
}
