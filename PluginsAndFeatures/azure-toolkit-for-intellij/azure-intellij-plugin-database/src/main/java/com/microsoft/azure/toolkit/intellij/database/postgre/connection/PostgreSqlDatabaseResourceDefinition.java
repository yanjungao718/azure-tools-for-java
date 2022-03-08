/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.database.postgre.connection;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.intellij.openapi.application.PreloadingActivity;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.AzureFormJPanel;
import com.microsoft.azure.toolkit.intellij.connector.Resource;
import com.microsoft.azure.toolkit.intellij.connector.ResourceManager;
import com.microsoft.azure.toolkit.intellij.database.connection.SqlDatabaseResourceDefinition;
import com.microsoft.azure.toolkit.intellij.database.connection.SqlDatabaseResourcePanel;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.postgre.AzurePostgreSql;
import com.microsoft.azure.toolkit.lib.postgre.PostgreSqlDatabase;
import com.microsoft.azure.toolkit.lib.postgre.PostgreSqlServer;

import javax.annotation.Nonnull;
import java.util.Objects;

public class PostgreSqlDatabaseResourceDefinition extends SqlDatabaseResourceDefinition<PostgreSqlDatabase> {
    public static final PostgreSqlDatabaseResourceDefinition INSTANCE = new PostgreSqlDatabaseResourceDefinition();

    public PostgreSqlDatabaseResourceDefinition() {
        super("Azure.PostgreSQL", "Azure Database for PostgreSQL", "/icons/Microsoft.DBforPostgreSQL/default.svg");
    }

    @Override
    public PostgreSqlDatabase getResource(String dataId) {
        final ResourceId dbId = ResourceId.fromString(dataId);
        final ResourceId serverId = dbId.parent();
        final String databaseName = dbId.name();
        final String resourceGroup = dbId.resourceGroupName();
        final PostgreSqlServer server = Azure.az(AzurePostgreSql.class).servers(dbId.subscriptionId()).get(serverId.name(), resourceGroup);
        return Objects.requireNonNull(server).databases().get(databaseName, resourceGroup);
    }

    @Override
    public AzureFormJPanel<Resource<PostgreSqlDatabase>> getResourcePanel(Project project) {
        return new SqlDatabaseResourcePanel<>(this, s -> Azure.az(AzurePostgreSql.class).servers(s).list());
    }

    public static class RegisterActivity extends PreloadingActivity {
        @Override
        public void preload(@Nonnull ProgressIndicator progressIndicator) {
            ResourceManager.registerDefinition(PostgreSqlDatabaseResourceDefinition.INSTANCE);
        }
    }
}

