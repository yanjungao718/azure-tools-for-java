/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.arm;

import com.microsoft.azure.toolkit.ide.common.IExplorerNodeProvider;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.ide.common.component.AzureModuleLabelView;
import com.microsoft.azure.toolkit.ide.common.component.AzureResourceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.ide.common.genericresource.GenericResourceLabelView;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResource;
import com.microsoft.azure.toolkit.lib.common.model.AbstractAzResourceModule;
import com.microsoft.azure.toolkit.lib.resource.AzureResources;
import com.microsoft.azure.toolkit.lib.resource.GenericResource;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ResourceGroupNodeProvider implements IExplorerNodeProvider {
    private static final String NAME = "Resource Management";
    private static final String RESOURCE_GROUPS_ICON = "/icons/Microsoft.Resources/resourceGroups/default.svg";
    private static final String DEPLOYMENTS_ICON = "/icons/Microsoft.Resources/resourceGroups/deployments/default.svg";

    @Override
    public boolean accept(@Nonnull Object data, @Nullable Node<?> parent, ViewType type) {
        return (type == ViewType.APP_CENTRIC && data instanceof AzureResources) || data instanceof ResourceGroup;
    }

    @Nullable
    @Override
    public Node<?> createNode(@Nonnull Object data, @Nullable Node<?> parent, @Nonnull Manager manager) {
        if (data instanceof AzureResources) {
            final AzureResources service = (AzureResources) data;
            final Function<AzureResources, List<ResourceGroup>> groupsLoader = s -> s.list().stream()
                .flatMap(m -> m.resourceGroups().list().stream()).collect(Collectors.toList());
            return new Node<>((AzureResources) data)
                .view(new AppCentricRootLabelView((AzureResources) data, RESOURCE_GROUPS_ICON))
                .actions(ResourceGroupActionsContributor.APPCENTRIC_RESOURCE_GROUPS_ACTIONS)
                .addChildren(groupsLoader, (d, p) -> this.createNode(d, p, manager));
        } else if (data instanceof ResourceGroup) {
            final ResourceGroup rg = (ResourceGroup) data;
            return new Node<>(rg)
                .view(new AzureResourceLabelView<>(rg))
                .actions(ResourceGroupActionsContributor.RESOURCE_GROUP_ACTIONS)
                .inlineAction(ResourceCommonActionsContributor.PIN)
                .addChild(ResourceGroup::deployments, (module, p) -> new Node<>(module)
                    .view(new AzureModuleLabelView<>(module, "Deployments", DEPLOYMENTS_ICON))
                    .actions(DeploymentActionsContributor.DEPLOYMENTS_ACTIONS)
                    .addChildren(AbstractAzResourceModule::list, (d, mn) -> manager.createNode(d, mn, ViewType.APP_CENTRIC)))
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
