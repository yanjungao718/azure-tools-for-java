/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common.properties;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.AzureIcons;
import com.microsoft.azure.toolkit.intellij.common.properties.AzureResourceEditorViewManager.AzureResourceFileType;
import com.microsoft.azure.toolkit.lib.common.entity.IAzureBaseResource;

import javax.annotation.Nonnull;
import javax.swing.*;

public class IntellijShowPropertiesViewAction {
    private static final AzureResourceEditorViewManager manager = new AzureResourceEditorViewManager((resource) -> {
        final Icon icon = getFileTypeIcon(resource);
        final String name = getFileTypeName(resource);
        return new AzureResourceFileType(name, icon);
    });

    public static void showPropertyView(@Nonnull IAzureBaseResource<?, ?> resource, @Nonnull Project project) {
        manager.showEditor(resource, project);
    }

    public static void closePropertiesView(@Nonnull IAzureBaseResource<?, ?> resource, @Nonnull Project project) {
        manager.closeEditor(resource, project);
    }

    private static String getFileTypeName(@Nonnull IAzureBaseResource<?, ?> resource) {
        return String.format("%s_FILE_TYPE", resource.getClass().getSimpleName().toUpperCase());
    }

    private static Icon getFileTypeIcon(@Nonnull IAzureBaseResource<?, ?> resource) {
        return AzureIcons.getIcon(String.format("/icons/%s.svg", resource.getClass().getSimpleName().toLowerCase()));
    }
}
