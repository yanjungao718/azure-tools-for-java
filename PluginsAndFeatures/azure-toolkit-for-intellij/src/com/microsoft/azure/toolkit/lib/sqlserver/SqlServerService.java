/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.sqlserver;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.toolkit.intellij.common.Draft;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.sqlserver.model.SqlFirewallRuleEntity;
import com.microsoft.azure.toolkit.lib.sqlserver.model.SqlServerEntity;
import com.microsoft.azure.toolkit.lib.sqlserver.service.AzureSqlServer;
import com.microsoft.azure.toolkit.lib.sqlserver.service.ISqlServer;
import com.microsoft.azuretools.ActionConstants;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetrywrapper.ErrorType;
import com.microsoft.azuretools.telemetrywrapper.EventType;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import com.microsoft.azuretools.telemetrywrapper.Operation;
import com.microsoft.azuretools.telemetrywrapper.TelemetryManager;

import java.util.Collections;

public class SqlServerService {
    private static final SqlServerService instance = new SqlServerService();

    public static SqlServerService getInstance() {
        return SqlServerService.instance;
    }

    @AzureOperation(
        name = "sqlserver.create",
        params = {
            "config.getServerName()",
            "config.getSubscription().displayName()"
        },
        type = AzureOperation.Type.SERVICE
    )
    public ISqlServer create(final SqlServerConfig config) {
        final Operation operation = TelemetryManager.createOperation(ActionConstants.MySQL.CREATE);
        try {
            operation.start();
            final String subscriptionId = config.getSubscription().subscriptionId();
            EventUtil.logEvent(EventType.info, operation, Collections.singletonMap(TelemetryConstants.SUBSCRIPTIONID, subscriptionId));
            // create resource group if necessary.
            if (config.getResourceGroup() instanceof Draft) {
                Azure azure = AuthMethodManager.getInstance().getAzureClient(subscriptionId);
                ResourceGroup newResourceGroup = azure.resourceGroups().define(config.getResourceGroup().name()).withRegion(config.getRegion().getName()).create();
                config.setResourceGroup(newResourceGroup);
            }
            // create sql server
            SqlServerEntity entity = SqlServerEntity.builder().name(config.getServerName()).subscriptionId(config.getSubscription().subscriptionId())
                    .resourceGroup(config.getResourceGroup().name()).region(Region.fromName(config.getRegion().getName())).administratorLoginName(config.getAdminUsername())
                    .enableAccessFromAzureServices(config.isAllowAccessFromAzureServices()).enableAccessFromLocalMachine(config.isAllowAccessFromLocalMachine())
                    .build();
            return com.microsoft.azure.toolkit.lib.Azure.az(AzureSqlServer.class).sqlServer(entity).create()
                    .withAdministratorLoginPassword(String.valueOf(config.getPassword()))
                    .commit();
        } catch (final RuntimeException e) {
            EventUtil.logError(operation, ErrorType.systemError, e, null, null);
            throw e;
        } finally {
            operation.complete();
        }
    }

}
