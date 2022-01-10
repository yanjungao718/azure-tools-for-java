/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.function;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.appservice.function.FunctionAppConfig;
import com.microsoft.azure.toolkit.intellij.legacy.webapp.runner.webappconfig.slimui.AppServiceComboBox;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.AzureFunction;
import com.microsoft.azure.toolkit.lib.appservice.entity.AppServicePlanEntity;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.FunctionApp;
import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionAppComboBox extends AppServiceComboBox<FunctionAppConfig> {

    public FunctionAppComboBox(final Project project) {
        super(project);
    }

    @Override
    protected void createResource() {
        final FunctionAppCreationDialog functionAppCreationDialog = new FunctionAppCreationDialog(project);
        functionAppCreationDialog.setOkActionListener(functionAppConfig -> {
            FunctionAppComboBox.this.addItem(functionAppConfig);
            FunctionAppComboBox.this.setSelectedItem(functionAppConfig);
            AzureTaskManager.getInstance().runLater(functionAppCreationDialog::close);
        });
        functionAppCreationDialog.showAndGet();
    }

    @Nonnull
    @Override
    @AzureOperation(
        name = "function.list_java_apps",
        type = AzureOperation.Type.SERVICE
    )
    protected List<FunctionAppConfig> loadAppServiceModels() {
        return Azure.az(AzureFunction.class).list().parallelStream()
                .map(this::convertRemoteResourceToConfig)
                .sorted((app1, app2) -> app1.getName().compareToIgnoreCase(app2.getName()))
                .collect(Collectors.toList());
    }

    private FunctionAppConfig convertRemoteResourceToConfig(final FunctionApp functionApp) {
        final FunctionAppConfig result = FunctionAppConfig.builder()
                .resourceId(functionApp.id())
                .name(functionApp.name())
                .runtime(null)
                .resourceGroup(ResourceGroup.builder().name(functionApp.resourceGroup()).build())
                .subscription(Subscription.builder().id(functionApp.subscriptionId()).build()).build();
        AzureTaskManager.getInstance()
                .runOnPooledThreadAsObservable(new AzureTask<>(functionApp::entity))
                .subscribe(entity -> {
                    result.setRuntime(entity.getRuntime());
                    result.setRegion(entity.getRegion());
                    result.setServicePlan(AppServicePlanEntity.builder().id(entity.getAppServicePlanId()).build());
                });
        return result;
    }
}
