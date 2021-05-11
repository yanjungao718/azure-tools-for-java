/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.core.mvp.model.sql;

import com.azure.resourcemanager.sql.models.SqlServer;
import com.microsoft.azure.management.mysql.v2020_01_01.Server;
import com.microsoft.azure.management.mysql.v2020_01_01.ServerPropertiesForDefaultCreate;
import com.microsoft.azure.management.mysql.v2020_01_01.implementation.MySQLManager;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.core.mvp.model.AzureMvpModel;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SqlServerMvpModel {

    public static List<SqlServer> listMySQLServers() {
        final List<SqlServer> servers = new ArrayList<>();
        final List<Subscription> subscriptions = AzureMvpModel.getInstance().getSelectedSubscriptions();
        if (CollectionUtils.isEmpty(subscriptions)) {
            return servers;
        }
        subscriptions.parallelStream().forEach(subscription -> {
            try {
                List<SqlServer> subServers = SqlServerMvpModel.listMySQLServersBySubscriptionId(subscription.subscriptionId());
                synchronized (servers) {
                    servers.addAll(subServers);
                }
            } catch (IOException e) {
                // swallow exception and skip error subscription
            }
        });
        return servers;
    }

    public static Server findServer(final String subscriptionId, final String resourceGroup, final String name) {
        final MySQLManager manager = AuthMethodManager.getInstance().getMySQLManager(subscriptionId);
        Server result = manager.servers().getByResourceGroup(resourceGroup, name);
        return result;
    }

    public static Server create(final String subscriptionId, final String resourceGroupName, final String serverName,
                                Region region, final ServerPropertiesForDefaultCreate properties) {
        final MySQLManager manager = AuthMethodManager.getInstance().getMySQLManager(subscriptionId);
        /*Sku sku = new Sku().withName("GP_Gen5_4");*/
        Server result = manager.servers().define(serverName).withRegion(region.name()).withExistingResourceGroup(resourceGroupName)
                .withProperties(properties)/*.withSku(sku)*/.create();
        return result;
    }

    public static void delete(final String subscriptionId, final String id) {
        final MySQLManager mySQLManager = AuthMethodManager.getInstance().getMySQLManager(subscriptionId);
        mySQLManager.servers().deleteByIds(id);
    }

    private static List<SqlServer> listMySQLServersBySubscriptionId(final String subscriptionId) throws IOException {
        return null;//getMySQLManager(subscriptionId).servers().list();
    }

    private static MySQLManager getMySQLManager(String sid) throws IOException {
        return AuthMethodManager.getInstance().getMySQLManager(sid);
    }

}
