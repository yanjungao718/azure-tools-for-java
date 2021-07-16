/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.sql;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.AzureFormJPanel;
import com.microsoft.azure.toolkit.intellij.connector.ResourceDefinition;
import com.microsoft.azure.toolkit.intellij.connector.database.DatabaseResource;
import com.microsoft.azure.toolkit.intellij.connector.database.DatabaseResourcePanel;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.sqlserver.service.AzureSqlServer;
import com.microsoft.azure.toolkit.lib.sqlserver.service.ISqlServer;
import com.microsoft.intellij.helpers.AzureIconLoader;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.serviceexplorer.AzureIconSymbol;
import com.microsoft.tooling.msservices.serviceexplorer.azure.sqlserver.SqlServerNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.util.Objects;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class SqlServerDatabaseResource extends DatabaseResource {

    public SqlServerDatabaseResource(@Nonnull final String serverId, @Nullable final String databaseName) {
        super(Definition.SQL_SERVER.getType(), serverId, databaseName);
    }

    public SqlServerDatabaseResource(@Nonnull final String databaseId) {
        super(Definition.SQL_SERVER.getType(), databaseId);
    }

    @Override
    public Icon getIcon() {
        return AzureIconLoader.loadIcon(AzureIconSymbol.MySQL.MODULE);
    }

    @Override
    public void showProperties(Project project) {
        ISqlServer server = Azure.az(AzureSqlServer.class).sqlServer(getServerId().subscriptionId(), getServerId().resourceGroupName(), getServerId().name());
        if (Objects.nonNull(server)) {
            final SqlServerNode node = new SqlServerNode(null, getServerId().subscriptionId(), server) {
                @Override
                public Object getProject() {
                    return project;
                }
            };
            DefaultLoader.getUIHelper().openSqlServerPropertyView(node);
        }
    }

    @Override
    public String getTitle() {
        return Definition.SQL_SERVER.getTitle();
    }

    @Getter
    @RequiredArgsConstructor
    public enum Definition implements ResourceDefinition<DatabaseResource> {
        SQL_SERVER("Microsoft.Sql", "SQL Server");
        private final String type;
        private final String title;

        @Override
        public AzureFormJPanel<DatabaseResource> getResourcesPanel(@Nonnull String type, final Project project) {
            return new DatabaseResourcePanel(this);
        }

        @Override
        public boolean write(@Nonnull final Element resourceEle, @Nonnull final DatabaseResource resource) {
            DatabaseResource.write(resourceEle, resource);
            return true;
        }

        @Override
        public SqlServerDatabaseResource read(@Nonnull final Element resourceEle) {
            final SqlServerDatabaseResource resource = new SqlServerDatabaseResource(resourceEle.getChildTextTrim("azureResourceId"));
            DatabaseResource.read(resourceEle, resource);
            return resource;
        }

    }
}
