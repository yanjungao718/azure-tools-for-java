/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.database.mysql.connection;

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
import com.microsoft.azure.toolkit.lib.mysql.AzureMySql;
import com.microsoft.azure.toolkit.lib.mysql.MySqlDatabase;
import com.microsoft.azure.toolkit.lib.mysql.MySqlServer;

import javax.annotation.Nonnull;
import java.util.Objects;

public class MySqlDatabaseResourceDefinition extends SqlDatabaseResourceDefinition<MySqlDatabase> {
    public static final MySqlDatabaseResourceDefinition INSTANCE = new MySqlDatabaseResourceDefinition();

    public MySqlDatabaseResourceDefinition() {
        super("Azure.MySQL", "Azure Database for MySQL", "/icons/Microsoft.DBforMySQL/default.svg");
    }

    @Override
    public MySqlDatabase getResource(String dataId) {
        final ResourceId dbId = ResourceId.fromString(dataId);
        final ResourceId serverId = dbId.parent();
        final String databaseName = dbId.name();
        final String resourceGroup = dbId.resourceGroupName();
        final MySqlServer server = Azure.az(AzureMySql.class).servers(dbId.subscriptionId()).get(serverId.name(), resourceGroup);
        return Objects.requireNonNull(server).databases().get(databaseName, resourceGroup);
    }

    @Override
    public AzureFormJPanel<Resource<MySqlDatabase>> getResourcePanel(Project project) {
        return new SqlDatabaseResourcePanel<>(this, s -> Azure.az(AzureMySql.class).servers(s).list());
    }

    public static class RegisterActivity extends PreloadingActivity {
        @Override
        public void preload(@Nonnull ProgressIndicator progressIndicator) {
            ResourceManager.registerDefinition(MySqlDatabaseResourceDefinition.INSTANCE);
        }
    }
}

