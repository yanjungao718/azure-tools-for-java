/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.azuresdk.dependencesurvey.activity;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.startup.StartupActivity;
import com.microsoft.azure.toolkit.intellij.azuresdk.service.WorkspaceTaggingService;
import com.microsoft.azure.toolkit.lib.common.telemetry.AzureTelemeter;
import com.microsoft.azure.toolkit.lib.common.telemetry.Telemetry;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkspaceTaggingActivity implements StartupActivity.DumbAware {
    private static final Pattern PATTERN = Pattern.compile("(Gradle|Maven): (.+):(.+):(.+)");
    private static final String WORKSPACE_TAGGING = "workspace-tagging";
    private static final String WORKSPACE_TAGGING_FAILURE = "workspace-tagging-failure";
    private static final String OPERATION_NAME = "operationName";
    private static final String SERVICE_NAME = "serviceName";
    private static final String SYSTEM = "system";
    private static final String TAG = "tag";

    @Override
    public void runActivity(@NotNull final Project project) {
        // swallow exception for workspace tagging
        Mono.fromCallable(() -> getWorkspaceTags(project)).subscribe(this::trackWorkspaceTagging, err -> {
        });
    }

    private Set<String> getWorkspaceTags(@NotNull final Project project) {
        return ReadAction.nonBlocking(() -> {
            if (project.isDisposed()) {
                return SetUtils.EMPTY_SET;
            }
            final Set<String> tagSet = new HashSet<>();
            OrderEnumerator.orderEntries(project).forEachLibrary(library -> {
                if (StringUtils.isEmpty(library.getName())) {
                    return true;
                }
                final Matcher matcher = PATTERN.matcher(library.getName());
                if (matcher.matches()) {
                    final String tag = WorkspaceTaggingService.getWorkspaceTag(matcher.group(2), matcher.group(3));
                    if (StringUtils.isNotEmpty(tag)) {
                        tagSet.add(tag);
                    }
                }
                return true;
            });
            return tagSet;
        }).executeSynchronously();
    }

    private void trackWorkspaceTagging(final Set<String> tagSet) {
        final Map<String, String> properties = new HashMap<>();
        properties.put(SERVICE_NAME, SYSTEM);
        properties.put(OPERATION_NAME, WORKSPACE_TAGGING);
        properties.put(TAG, StringUtils.join(tagSet, ","));
        AzureTelemeter.log(Telemetry.Type.INFO, properties);
    }
}
