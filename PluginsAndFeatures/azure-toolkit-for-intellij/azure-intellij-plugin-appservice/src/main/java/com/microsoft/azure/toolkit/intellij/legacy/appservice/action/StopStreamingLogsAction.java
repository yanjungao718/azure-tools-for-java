/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.appservice.action;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.legacy.appservice.AppServiceStreamingLogManager;
import com.microsoft.azure.toolkit.lib.appservice.AppServiceAppBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StopStreamingLogsAction {

    private final Project project;
    private final String resourceId;

    public StopStreamingLogsAction(@Nonnull final AppServiceAppBase<?, ?, ?> appService, @Nullable final Project project) {
        super();
        this.project = project;
        this.resourceId = appService.getId();
    }

    public void execute() {
        AppServiceStreamingLogManager.INSTANCE.closeStreamingLog(project, resourceId);
    }
}
