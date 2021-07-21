/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@State(
        name = "azure-settings",
        storages = {@Storage("azure/azure-settings.xml")}
)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProjectAzureSettings implements PersistentStateComponent<ProjectAzureSettings.Settings> {

    private Settings settings = new Settings();

    public static ProjectAzureSettings getInstance(Project project) {
        return ServiceManager.getService(project, ProjectAzureSettings.class);
    }

    @Override
    public Settings getState() {
        return this.settings;
    }

    @Override
    public void loadState(Settings settings) {
        XmlSerializerUtil.copyBean(settings, this.settings);
    }

    /**
     * Add settings in this class as its properties.
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Settings {

        @MapAnnotation(surroundKeyWithTag = false, surroundValueWithTag = false, surroundWithTag = false, entryTagName = "action", keyAttributeName = "id")
        private final Map<String, Boolean> suppressedActions = Collections.synchronizedMap(new HashMap<>());

    }
}
