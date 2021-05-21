/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.springcloud;

import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.event.AzureEventBus;
import com.microsoft.azure.toolkit.lib.springcloud.AzureSpringCloud;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudCluster;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import com.microsoft.azuretools.telemetry.AppInsightsConstants;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetry.TelemetryProperties;
import com.microsoft.tooling.msservices.serviceexplorer.*;
import io.reactivex.rxjava3.disposables.Disposable;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SpringCloudNode
 */
public class SpringCloudNode extends RefreshableNode implements TelemetryProperties {
    private static final String EMPTY_POSTFIX = " (Empty)";

    private Disposable rxSubscription;
    private SpringCloudCluster cluster;

    public SpringCloudNode(AzureRefreshableNode parent, SpringCloudCluster cluster) {
        super(cluster.id(), cluster.name(), parent, null, true);
        this.cluster = cluster;
        loadActions();
        AzureEventBus.after("springcloud|app.create", this::onAppCreatedOrRemoved);
        AzureEventBus.after("springcloud|app.remove", this::onAppCreatedOrRemoved);
    }

    public void onAppCreatedOrRemoved(SpringCloudApp app) {
        if (this.cluster.name().equals(app.getCluster().name())) {
            refreshItems();
        }
    }

    @Override
    public @Nullable AzureIconSymbol getIconSymbol() {
        return AzureIconSymbol.SpringCloud.CLUSTER;
    }

    private void notifyDataRefresh(SpringCloudAppEvent event) {
        if (event.isDelete()) {
            SpringCloudAppNode matchedNode =
                    Arrays.stream(childNodes.toArray(new SpringCloudAppNode[0])).filter(node -> event.getId().equals(node.getId())).findFirst().orElse(null);
            if (matchedNode != null) {
                matchedNode.unsubscribe();
                this.removeDirectChildNode(matchedNode);
            }
            if (this.childNodes.isEmpty()) {
                this.setName(this.cluster.name() + EMPTY_POSTFIX);
            }
        } else {
            if (Arrays.stream(childNodes.toArray(new SpringCloudAppNode[0])).noneMatch(node -> event.getId().equals(node.getId()))) {
                addChildNode(new SpringCloudAppNode(event.getApp(), this));
            }
            this.setName(this.cluster.name());
        }
    }

    @Override
    protected void loadActions() {
        super.loadActions();
        addAction(initActionBuilder(this::openInPortal).withAction(AzureActionEnum.OPEN_IN_PORTAL).withBackgroudable(true).build());
    }

    private BasicActionBuilder initActionBuilder(Runnable runnable) {
        return new BasicActionBuilder(runnable)
                .withModuleName(SpringCloudModule.MODULE_NAME)
                .withInstanceName(name);
    }

    @Override
    protected void refreshItems() {
        final List<SpringCloudApp> apps = cluster.refresh().apps();
        this.setName(CollectionUtils.isEmpty(apps) ? this.cluster.name() + EMPTY_POSTFIX : this.cluster.name());
        apps.forEach(app -> {
            SpringCloudStateManager.INSTANCE.notifySpringAppUpdate(this.cluster.id(), app);
            addChildNode(new SpringCloudAppNode(app, this));
        });
        rxSubscription = SpringCloudStateManager.INSTANCE.subscribeSpringAppEvent(event -> {
            notifyDataRefresh(event);
        }, this.cluster.id());
    }

    @Override
    public void removeAllChildNodes() {
        SpringCloudAppNode[] childNodes = getChildNodes().toArray(new SpringCloudAppNode[0]);
        super.removeAllChildNodes();
        for (final SpringCloudAppNode child : childNodes) {
            child.unsubscribe();
        }
    }

    @Override
    public Map<String, String> toProperties() {
        final Map<String, String> properties = new HashMap<>();
        properties.put(AppInsightsConstants.SubscriptionId, this.cluster.entity().getSubscriptionId());
        // todo: track region name
        return properties;
    }

    @Override
    public void removeNode(String sid, String id, Node node) {
        ((SpringCloudAppNode) node).unsubscribe();
        removeDirectChildNode(node);
    }

    @Override
    public String getServiceName() {
        return TelemetryConstants.SPRING_CLOUD;
    }

    public void unsubscribe() {
        if (rxSubscription != null && !rxSubscription.isDisposed()) {
            rxSubscription.dispose();
        }
    }

    private void openInPortal() {
        this.openResourcesInPortal(this.cluster.entity().getSubscriptionId(), this.cluster.id());
    }

}
