/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.springcloud;

import com.microsoft.azure.management.appplatform.v2020_07_01.DeploymentResourceStatus;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.springcloud.AzureSpringCloud;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudDeployment;
import lombok.Lombok;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpringCloudMonitorUtil {
    private static final Logger LOGGER = Logger.getLogger(SpringCloudMonitorUtil.class.getName());

    public static void awaitAndMonitoringStatus(SpringCloudApp appInner, DeploymentResourceStatus originalStatus) {
        String clusterId = appInner.getCluster().id();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future future = executor.submit(() -> {
            DeploymentResourceStatus status = null;
            do {
                try {
                    SpringCloudApp app = Azure.az(AzureSpringCloud.class).cluster(appInner.getCluster().name()).app(appInner.name());
                    if (app == null) {
                        SpringCloudStateManager.INSTANCE.notifySpringAppDelete(clusterId, app.entity().getId());
                        return;
                    }
                    SpringCloudDeployment deployment = app.deployment(app.getActiveDeploymentName());
                    SpringCloudStateManager.INSTANCE.notifySpringAppUpdate(clusterId, app);
                    if (deployment == null) {
                        return;
                    }

                    status = DeploymentResourceStatus.fromString(deployment.entity().getStatus());
                    SpringCloudStateManager.INSTANCE.notifySpringAppUpdate(clusterId, app);
                    Thread.sleep(1000 * 5);
                } catch (InterruptedException e) {
                    throw Lombok.sneakyThrow(e);
                }

            } while (originalStatus == status);
        });
        try {
            future.get(180, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, String.format("Failed to get the spring cloud app status for app: %s.", appInner.name()), e);
        }
    }

}
