/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.database.sqlserver.connection;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcons;
import com.microsoft.azure.toolkit.intellij.common.AzureFormJPanel;
import com.microsoft.azure.toolkit.intellij.connector.Resource;
import com.microsoft.azure.toolkit.intellij.database.connection.SqlDatabaseResourceDefinition;
import com.microsoft.azure.toolkit.intellij.database.connection.SqlDatabaseResourcePanel;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.sqlserver.AzureSqlServer;
import com.microsoft.azure.toolkit.lib.sqlserver.MicrosoftSqlDatabase;
import com.microsoft.azure.toolkit.lib.sqlserver.MicrosoftSqlServer;

import java.util.Objects;

public class SqlServerDatabaseResourceDefinition extends SqlDatabaseResourceDefinition<MicrosoftSqlDatabase> {
    public static final SqlServerDatabaseResourceDefinition INSTANCE = new SqlServerDatabaseResourceDefinition();

    public SqlServerDatabaseResourceDefinition() {
        super("Azure.SqlServer", "SQL Server", AzureIcons.SqlServer.MODULE.getIconPath());
    }

    @Override
    public MicrosoftSqlDatabase getResource(String dataId) {
        final ResourceId dbId = ResourceId.fromString(dataId);
        final ResourceId serverId = dbId.parent();
        final String databaseName = dbId.name();
        final String resourceGroup = dbId.resourceGroupName();
        final MicrosoftSqlServer server = Azure.az(AzureSqlServer.class).servers(dbId.subscriptionId()).get(serverId.name(), resourceGroup);
        return Objects.requireNonNull(server).databases().get(databaseName, resourceGroup);
    }

    @Override
    public AzureFormJPanel<Resource<MicrosoftSqlDatabase>> getResourcePanel(Project project) {
        return new SqlDatabaseResourcePanel<>(this, s -> Azure.az(AzureSqlServer.class).servers(s).list());
    }
}

