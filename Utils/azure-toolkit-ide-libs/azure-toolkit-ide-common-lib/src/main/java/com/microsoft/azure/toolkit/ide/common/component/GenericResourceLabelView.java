/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.common.component;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcon;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class GenericResourceLabelView<T extends AbstractAzResource<?, ?, ?>> implements NodeView {

    @Nonnull
    @Getter
    private final T resource;
    @Getter
    private final String label;
    @Getter
    private final String description;
    @Getter
    private final AzureIcon icon;
    @Nullable
    @Setter
    @Getter
    private Refresher refresher;
    @Getter
    private final boolean enabled = true;

    public GenericResourceLabelView(@Nonnull T resource) {
        this.resource = resource;
        final ResourceId resourceId = ResourceId.fromString(resource.getId());
        this.label = resourceId.name();
        this.description = resource.getResourceTypeName();
        this.icon = AzureIcon.UNKNOWN_ICON;
    }

    public void dispose() {
        this.refresher = null;
    }

    @Override
    public String getIconPath() {
        return Optional.ofNullable(getIcon()).map(AzureIcon::getIconPath).orElse(StringUtils.EMPTY);
    }
}
