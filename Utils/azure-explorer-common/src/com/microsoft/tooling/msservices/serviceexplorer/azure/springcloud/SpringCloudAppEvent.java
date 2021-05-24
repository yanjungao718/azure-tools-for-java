/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.springcloud;

import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudDeployment;
import org.apache.commons.lang3.StringUtils;

public class SpringCloudAppEvent {
    enum EventKind {
        SPRING_APP_UPDATE, SPRING_APP_DELETE
    }
    private EventKind kind;
    private String id;
    private String clusterId;
    private SpringCloudApp app;
    private SpringCloudDeployment deployment;

    public SpringCloudAppEvent(EventKind kind, String clusterId, SpringCloudApp app) {
        this.kind = kind;
        this.clusterId = clusterId;
        this.app = app;
        this.id = app == null ? null : app.entity().getId();
        if (StringUtils.isNotBlank(app.getActiveDeploymentName())) {
            this.deployment = app.deployment(app.getActiveDeploymentName());
        }
    }

    public SpringCloudAppEvent(EventKind kind, String clusterId, String id) {
        this.kind = kind;
        this.clusterId = clusterId;
        this.id = id;
    }

    public EventKind getKind() {
        return kind;
    }

    public String getId() {
        return id;
    }

    public String getClusterId() {
        return clusterId;
    }

    public SpringCloudApp getApp() {
        return app;
    }

    public SpringCloudDeployment getDeployment() {
        return deployment;
    }

    public boolean isDelete() {
        return kind == EventKind.SPRING_APP_DELETE;
    }

    public boolean isUpdate() {
        return kind == EventKind.SPRING_APP_UPDATE;
    }
}
