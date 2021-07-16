/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.mysql;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.AzureFormJPanel;
import com.microsoft.azure.toolkit.intellij.connector.ResourceDefinition;
import com.microsoft.azure.toolkit.intellij.connector.database.DatabaseResource;
import com.microsoft.azure.toolkit.intellij.connector.database.DatabaseResourcePanel;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.mysql.service.AzureMySql;
import com.microsoft.azure.toolkit.lib.mysql.service.MySqlServer;
import com.microsoft.intellij.helpers.AzureIconLoader;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.serviceexplorer.AzureIconSymbol;
import com.microsoft.tooling.msservices.serviceexplorer.azure.mysql.MySQLNode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.util.Objects;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class MySQLDatabaseResource extends DatabaseResource {

    public MySQLDatabaseResource(@Nonnull final String serverId, @Nullable final String databaseName) {
        super(Definition.AZURE_MYSQL.getType(), serverId, databaseName);
    }

    public MySQLDatabaseResource(@Nonnull final String databaseId) {
        super(Definition.AZURE_MYSQL.getType(), databaseId);
    }

    @Override
    public Icon getIcon() {
        return AzureIconLoader.loadIcon(AzureIconSymbol.MySQL.MODULE);
    }

    @Override
    public void showProperties(Project project) {
        final MySqlServer server = Azure.az(AzureMySql.class).subscription(getServerId().subscriptionId()).get(getServerId().id());
        if (Objects.nonNull(server)) {
            final MySQLNode node = new MySQLNode(null, server) {
                @Override
                public Object getProject() {
                    return project;
                }
            };
            DefaultLoader.getUIHelper().openMySQLPropertyView(node);
        }

    }

    @Override
    public String getTitle() {
        return Definition.AZURE_MYSQL.getTitle();
    }

    @Getter
    @RequiredArgsConstructor
    public enum Definition implements ResourceDefinition<DatabaseResource> {
        AZURE_MYSQL("Microsoft.DBforMySQL", "Azure Database for MySQL");
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
        public MySQLDatabaseResource read(@Nonnull final Element resourceEle) {
            final MySQLDatabaseResource resource = new MySQLDatabaseResource(resourceEle.getChildTextTrim("azureResourceId"));
            DatabaseResource.read(resourceEle, resource);
            return resource;
        }

    }
}
