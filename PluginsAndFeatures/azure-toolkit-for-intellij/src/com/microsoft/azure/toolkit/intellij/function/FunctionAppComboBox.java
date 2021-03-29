/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.function;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.toolkit.intellij.appservice.AppServiceComboBox;
import com.microsoft.azure.toolkit.intellij.webapp.WebAppComboBoxModel;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import com.microsoft.azuretools.core.mvp.model.ResourceEx;
import com.microsoft.azuretools.core.mvp.model.function.AzureFunctionMvpModel;
import com.microsoft.azuretools.core.mvp.model.webapp.AzureWebAppMvpModel;
import com.microsoft.azuretools.utils.WebAppUtils;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionAppComboBox extends AppServiceComboBox<FunctionAppComboBoxModel> {

    public FunctionAppComboBox(final Project project) {
        super(project);
    }

    @Override
    protected void createResource() {
        final FunctionAppCreationDialog functionAppCreationDialog = new FunctionAppCreationDialog(project);
        functionAppCreationDialog.setOkActionListener(functionAppConfig -> {
            final FunctionAppComboBoxModel newModel = new FunctionAppComboBoxModel(functionAppConfig);
            newModel.setNewCreateResource(true);
            FunctionAppComboBox.this.addItem(newModel);
            FunctionAppComboBox.this.setSelectedItem(newModel);
            DefaultLoader.getIdeHelper().invokeLater(functionAppCreationDialog::close);
        });
        functionAppCreationDialog.showAndGet();
    }

    @NotNull
    @Override
    @AzureOperation(
        name = "function.list.detail|subscription|selected",
        type = AzureOperation.Type.SERVICE
    )
    protected List<? extends FunctionAppComboBoxModel> loadItems() throws Exception {
        return AzureFunctionMvpModel.getInstance().listJavaFunctionApps(false).parallelStream()
            .map(FunctionAppComboBoxModel::new)
            .sorted((app1, app2) -> app1.getAppName().compareToIgnoreCase(app2.getAppName()))
            .collect(Collectors.toList());
    }
}
