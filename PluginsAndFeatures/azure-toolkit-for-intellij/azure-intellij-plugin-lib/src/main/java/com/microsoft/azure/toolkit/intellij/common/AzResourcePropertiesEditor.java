/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.azure.toolkit.intellij.common.properties.IntellijShowPropertiesViewAction;
import com.microsoft.azure.toolkit.lib.common.event.AzureEvent;
import com.microsoft.azure.toolkit.lib.common.event.AzureEventBus;
import com.microsoft.azure.toolkit.lib.common.model.AzResourceBase;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

public abstract class AzResourcePropertiesEditor<T extends AzResourceBase> extends BaseEditor {

    private final AzureEventBus.EventListener<Object, AzureEvent<Object>> listener;
    private final Project project;
    private final T resource;

    public AzResourcePropertiesEditor(@Nonnull final VirtualFile virtualFile, @Nonnull T resource, @Nonnull final Project project) {
        super(virtualFile);
        this.resource = resource;
        this.project = project;
        this.listener = new AzureEventBus.EventListener<>(this::onEvent);
        AzureEventBus.on("resource.status_changed.resource", listener);
    }

    public void onEvent(AzureEvent<Object> event) {
        final String type = event.getType();
        final Object source = event.getSource();
        if (source instanceof AzResourceBase && ((AzResourceBase) source).getId().equals(this.resource.getId())) {
            if (StringUtils.equalsAnyIgnoreCase(((AzResourceBase) source).getStatus(), "deleted", "removed")) {
                IntellijShowPropertiesViewAction.closePropertiesView(resource, project);
            } else {
                this.refresh();
            }
        }
    }

    @Override
    public void dispose() {
        AzureEventBus.off("resource.status_changed.resource", listener);
    }

    protected abstract void refresh();
}
