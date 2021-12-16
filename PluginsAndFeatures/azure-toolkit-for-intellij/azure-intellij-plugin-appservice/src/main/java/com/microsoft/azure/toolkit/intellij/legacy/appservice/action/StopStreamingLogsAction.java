/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.appservice.action;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.legacy.appservice.AppServiceStreamingLogManager;
import com.microsoft.azure.toolkit.lib.appservice.service.IAppService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StopStreamingLogsAction {

    private Project project;
    private IAppService<?> appService;
    private String resourceId;

    public StopStreamingLogsAction(@Nonnull final IAppService<?> appService, @Nullable final Project project) {
        super();
        this.project = project;
        this.appService = appService;
        this.resourceId = appService.id();
    }
    
    public void execute() {
        AppServiceStreamingLogManager.INSTANCE.closeStreamingLog(project, resourceId);
    }
}
