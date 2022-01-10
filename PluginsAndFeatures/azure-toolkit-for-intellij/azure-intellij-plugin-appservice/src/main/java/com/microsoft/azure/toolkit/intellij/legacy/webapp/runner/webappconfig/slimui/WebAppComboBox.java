/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.webapp.runner.webappconfig.slimui;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.appservice.webapp.model.WebAppConfig;
import com.microsoft.azure.toolkit.intellij.legacy.webapp.WebAppCreationDialog;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.AzureWebApp;
import com.microsoft.azure.toolkit.lib.appservice.entity.AppServicePlanEntity;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.WebApp;
import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;

import java.util.List;
import java.util.stream.Collectors;

public class WebAppComboBox extends AppServiceComboBox<WebAppConfig> {
    public WebAppComboBox(Project project) {
        super(project);
    }

    @Override
    protected List<WebAppConfig> loadAppServiceModels() {
        final List<WebApp> webApps = Azure.az(AzureWebApp.class).list();
        return webApps.stream().parallel()
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                .map(this::convertRemoteResourceToWebAppConfig)
                .collect(Collectors.toList());
    }

    private WebAppConfig convertRemoteResourceToWebAppConfig(final WebApp webApp) {
        final WebAppConfig webAppConfig = WebAppConfig.builder()
                .resourceId(webApp.id())
                .name(webApp.name())
                .runtime(null)
                .subscription(Subscription.builder().id(webApp.subscriptionId()).build())
                .resourceGroup(ResourceGroup.builder().name(webApp.resourceGroup()).build())
                .build();
        AzureTaskManager.getInstance()
                .runOnPooledThreadAsObservable(new AzureTask<>(webApp::entity))
                .subscribe(entity -> {
                    webAppConfig.setRuntime(entity.getRuntime());
                    webAppConfig.setRegion(entity.getRegion());
                    webAppConfig.setServicePlan(AppServicePlanEntity.builder().id(entity.getAppServicePlanId()).build());
                });
        return webAppConfig;
    }

    @Override
    protected void createResource() {
        // todo: hide deployment part in creation dialog
        WebAppCreationDialog webAppCreationDialog = new WebAppCreationDialog(project);
        webAppCreationDialog.setDeploymentVisible(false);
        webAppCreationDialog.setOkActionListener(webAppConfig -> {
            WebAppComboBox.this.addItem(webAppConfig);
            WebAppComboBox.this.setSelectedItem(webAppConfig);
            AzureTaskManager.getInstance().runLater(webAppCreationDialog::close);
        });
        webAppCreationDialog.show();
    }
}
