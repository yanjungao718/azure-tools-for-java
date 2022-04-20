/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.common.component;

import com.microsoft.azure.toolkit.lib.AzService;
import com.microsoft.azure.toolkit.lib.common.event.AzureEvent;
import com.microsoft.azure.toolkit.lib.common.event.AzureEventBus;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// TODO: merge with AzureModuleLabelView
public class AzureServiceLabelView<T extends AzService> implements NodeView {
    @Nonnull
    @Getter
    private final T service;
    @Getter
    protected String label;
    @Getter
    private final String iconPath;
    private final AzureEventBus.EventListener listener;
    @Getter
    private String description;
    @Nullable
    @Setter
    @Getter
    private Refresher refresher;

    public AzureServiceLabelView(@Nonnull T service) {
        this(service, service.getName());
    }

    public AzureServiceLabelView(@Nonnull T service, String label) {
        this(service, label, String.format("/icons/%s.svg", service.getClass().getSimpleName().toLowerCase()));
    }

    public AzureServiceLabelView(@Nonnull T service, String label, String iconPath) {
        this.service = service;
        this.label = label;
        this.iconPath = iconPath;
        this.listener = new AzureEventBus.EventListener(this::onEvent);
        AzureEventBus.on("module.refreshed.module", listener);
        AzureEventBus.on("module.children_changed.module", listener);
        AzureEventBus.on("service.children_changed.service", listener);
        this.refreshView();
    }

    public void dispose() {
        AzureEventBus.off("module.refreshed.module", listener);
        AzureEventBus.off("module.children_changed.module", listener);
        AzureEventBus.off("service.children_changed.service", listener);
        this.refresher = null;
    }

    public void onEvent(AzureEvent event) {
        final String type = event.getType();
        final Object source = event.getSource();
        final boolean childrenChanged = StringUtils.equalsAnyIgnoreCase(type, "module.children_changed.module", "service.children_changed.service");
        final AzureTaskManager tm = AzureTaskManager.getInstance();
        if (source instanceof AzService && source.equals(this.service)) {
            tm.runLater(() -> this.refreshChildren(childrenChanged));
        }
    }
}
