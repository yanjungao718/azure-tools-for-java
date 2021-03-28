/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.azuresdk.dependencesurvey.activity;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.startup.StartupActivity;
import com.microsoft.azure.toolkit.intellij.azuresdk.service.WorkspaceTaggingService;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetrywrapper.EventType;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WorkspaceTaggingActivity implements StartupActivity.DumbAware {
    private static final Pattern PATTERN = Pattern.compile("(Gradle|Maven): (.*):(.*):(.*)");

    @Override
    public void runActivity(@NotNull final Project project) {
        ApplicationManager.getApplication().runReadAction(() -> trackProjectDependencies(project));

    }

    private void trackProjectDependencies(@NotNull final Project project) {
        final Set<String> tagSet = new java.util.HashSet<>();
        OrderEnumerator.orderEntries(project).forEachLibrary(library -> {
            final Matcher matcher = PATTERN.matcher(StringUtils.isEmpty(library.getName()) ? StringUtils.EMPTY : library.getName());
            if (matcher.matches()) {
                final String tag = WorkspaceTaggingService.getWorkspaceTag(matcher.group(2), matcher.group(3));
                if (StringUtils.isNotEmpty(tag)) {
                    tagSet.add(tag);
                }
            }
            return true;
        });
        final Map<String, String> properties = tagSet.stream().collect(Collectors.toMap(tag -> tag, tag -> "true"));
        EventUtil.logEvent(EventType.info, TelemetryConstants.SYSTEM, TelemetryConstants.WORKSPACE_TAGGING, properties);
    }
}
