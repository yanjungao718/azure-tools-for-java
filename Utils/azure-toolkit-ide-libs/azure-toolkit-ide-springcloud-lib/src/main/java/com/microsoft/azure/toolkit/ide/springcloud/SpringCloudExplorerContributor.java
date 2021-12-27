/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.springcloud;

import com.microsoft.azure.toolkit.ide.common.IExplorerContributor;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.ide.common.component.AzureResourceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.AzureServiceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.lib.common.entity.IAzureResource;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessager;
import com.microsoft.azure.toolkit.lib.springcloud.AzureSpringCloud;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudCluster;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.microsoft.azure.toolkit.lib.Azure.az;

public class SpringCloudExplorerContributor implements IExplorerContributor {

    private static final String NAME = "Spring Cloud";
    private static final String ICON = "/icons/springcloud.svg";

    @Override
    public Node<?> getModuleNode() {
        final AzureActionManager am = AzureActionManager.getInstance();
        final IAzureMessager messager = AzureMessager.getDefaultMessager();

        final AzureSpringCloud service = az(AzureSpringCloud.class);
        return new Node<>(service).view(new AzureServiceLabelView<>(service, "Spring Cloud", ICON))
                .actions(SpringCloudActionsContributor.SERVICE_ACTIONS)
                .addChildren(this::listClusters, (cluster, ascNode) -> new Node<>(cluster)
                        .view(new AzureResourceLabelView<>(cluster))
                        .actions(SpringCloudActionsContributor.CLUSTER_ACTIONS)
                        .addChildren(this::listApps, (app, clusterNode) -> new Node<>(app)
                                .view(new AzureResourceLabelView<>(app))
                                .actions(SpringCloudActionsContributor.APP_ACTIONS)));
    }

    @Nonnull
    private List<SpringCloudApp> listApps(SpringCloudCluster c) {
        return c.apps().stream().sorted(Comparator.comparing(IAzureResource::name)).collect(Collectors.toList());
    }

    @Nonnull
    private List<SpringCloudCluster> listClusters(AzureSpringCloud s) {
        return s.clusters().stream().sorted(Comparator.comparing(IAzureResource::name)).collect(Collectors.toList());
    }
}
