/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.appservice.property;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;

public class AppServicePropertyEditorInput implements IEditorInput {

    private static final String SLOTS = "slots";
    private static final String SITES = "sites";

    private String resourceId;
    private String subscriptionId;
    private String type;
    private String appServiceId;
    private String appServiceName;
    private String slotName;

    public AppServicePropertyEditorInput(@Nonnull final String resourceId) {
        final ResourceId resource = ResourceId.fromString(resourceId);
        this.resourceId = resourceId;
        this.subscriptionId = resource.subscriptionId();
        this.type = resource.resourceType();
        if (StringUtils.equalsIgnoreCase(type, SITES)) {
            this.appServiceId = resourceId;
            this.appServiceName = resource.name();
            this.slotName = null;
        } else if (StringUtils.equalsIgnoreCase(type, SLOTS)) {
            final ResourceId parent = resource.parent();
            this.appServiceId = parent.id();
            this.appServiceName = parent.name();
            this.slotName = resource.name();
        }
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getType() {
        return type;
    }

    public String getAppServiceId() {
        return appServiceId;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public String getSlotName() {
        return slotName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppServicePropertyEditorInput that = (AppServicePropertyEditorInput) o;
        return Objects.equals(resourceId, that.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceId);
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    @Override
    public String getName() {
        return StringUtils.equalsIgnoreCase(this.type, SITES) ? getAppServiceName() : getSlotName();
    }

    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    @Override
    public String getToolTipText() {
        return null;
    }

    @Override
    public <T> T getAdapter(Class<T> adapterClass) {
        return null;
    }

}
