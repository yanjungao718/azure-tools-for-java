/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.appservice.action;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.legacy.appservice.AppServiceStreamingLogManager;
import com.microsoft.azure.toolkit.lib.appservice.service.IAppService;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.FunctionApp;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.WebApp;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.WebAppDeploymentSlot;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StartStreamingLogsAction {
    private Project project;
    private IAppService<?> appService;
    private String resourceId;

    public StartStreamingLogsAction(@Nonnull final IAppService<?> appService, @Nullable final Project project) {
        super();
        this.project = project;
        this.appService = appService;
        this.resourceId = appService.id();
    }

    public void execute() {
        final AzureString title = AzureOperationBundle.title("appservice.start_log_stream.app", appService.name());
        AzureTaskManager.getInstance().runInBackground(new AzureTask(project, title, false, () -> {
            if (appService instanceof WebApp) {
                AppServiceStreamingLogManager.INSTANCE.showWebAppStreamingLog(project, resourceId);
            } else if (appService instanceof WebAppDeploymentSlot) {
                AppServiceStreamingLogManager.INSTANCE.showWebAppDeploymentSlotStreamingLog(project, resourceId);
            } else if (appService instanceof FunctionApp) {
                AppServiceStreamingLogManager.INSTANCE.showFunctionStreamingLog(project, resourceId);
            } else {
                AzureMessager.getMessager().error("Unsupported operation", "Unsupported operation");
            }
        }));
    }
}
