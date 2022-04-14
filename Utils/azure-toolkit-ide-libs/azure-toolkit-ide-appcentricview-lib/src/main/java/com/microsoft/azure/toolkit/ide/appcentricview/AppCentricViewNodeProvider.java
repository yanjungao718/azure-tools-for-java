/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appcentricview;

import com.microsoft.azure.toolkit.ide.common.IExplorerNodeProvider;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.ide.common.component.AzureResourceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.AzureSubscriptionLabelView;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.ide.common.genericresource.GenericResourceLabelView;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResourceModule;
import com.microsoft.azure.toolkit.lib.resource.AzureResources;
import com.microsoft.azure.toolkit.lib.resource.GenericResource;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroup;
import com.microsoft.azure.toolkit.lib.resource.ResourcesServiceSubscription;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.stream.Collectors;

public class AppCentricViewNodeProvider implements IExplorerNodeProvider {
    private static final String NAME = "Resource Management";
    private static final String ICON = "/icons/Microsoft.Resources/default.svg";

    @Override
    public boolean accept(@Nonnull Object data, @Nullable Node<?> parent, ViewType type) {
        return (type == ViewType.APP_CENTRIC && (data instanceof AzureResources || data instanceof ResourceGroup)) ||
            data instanceof ResourcesServiceSubscription;
    }

    @Nullable
    @Override
    public Node<?> createNode(@Nonnull Object data, @Nullable Node<?> parent, @Nonnull Manager manager) {
        if (data instanceof AzureResources) {
            return new Node<>((AzureResources) data)
                .view(new AppCentricRootLabelView((AzureResources) data, ICON))
                .actions(AppCentricViewActionsContributor.SERVICE_ACTIONS)
                .addChildren(AbstractAzResourceModule::list, (d, p) -> this.createNode(d, p, manager));
        } else if (data instanceof ResourcesServiceSubscription) {
            final ResourcesServiceSubscription sub = (ResourcesServiceSubscription) data;
            return new Node<>(sub)
                .view(new AzureSubscriptionLabelView<>(sub))
                .actions(AppCentricViewActionsContributor.SUBSCRIPTION_ACTIONS)
                .inlineAction(ResourceCommonActionsContributor.PIN)
                .addChildren(s -> s.resourceGroups().list(), (d, p) -> this.createNode(d, p, manager));
        } else if (data instanceof ResourceGroup) {
            final ResourceGroup rg = (ResourceGroup) data;
            return new Node<>(rg)
                .view(new AzureResourceLabelView<>(rg))
                .actions(AppCentricViewActionsContributor.RESOURCE_GROUP_ACTIONS)
                .inlineAction(ResourceCommonActionsContributor.PIN)
                .addChildren(group -> group.genericResources().list().stream().map(GenericResource::toConcreteResource)
                    .map(r -> manager.createNode(r, parent, ViewType.APP_CENTRIC))
                    .sorted(Comparator.comparing(r -> ((Node<?>) r).view() instanceof GenericResourceLabelView)
                        .thenComparing(r -> ((AbstractAzResource<?, ?, ?>) ((Node<?>) r).data()).getFullResourceType())
                        .thenComparing(r -> ((AbstractAzResource<?, ?, ?>) ((Node<?>) r).data()).getName()))
                    .collect(Collectors.toList()));
        }
        return null;
    }
}
