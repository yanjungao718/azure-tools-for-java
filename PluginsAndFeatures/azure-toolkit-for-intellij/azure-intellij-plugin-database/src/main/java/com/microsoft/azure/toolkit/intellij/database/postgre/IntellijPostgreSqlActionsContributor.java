/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.database.postgre;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.ide.database.postgre.PostgreSqlActionsContributor;
import com.microsoft.azure.toolkit.intellij.connector.ConnectorDialog;
import com.microsoft.azure.toolkit.intellij.database.IntellijDatasourceService;
import com.microsoft.azure.toolkit.intellij.database.connection.SqlDatabaseResource;
import com.microsoft.azure.toolkit.intellij.database.postgre.connection.PostgreSqlDatabaseResourceDefinition;
import com.microsoft.azure.toolkit.intellij.database.postgre.creation.CreatePostgreSqlAction;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.database.JdbcUrl;
import com.microsoft.azure.toolkit.lib.postgre.AzurePostgreSql;
import com.microsoft.azure.toolkit.lib.postgre.PostgreSqlServer;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class IntellijPostgreSqlActionsContributor implements IActionsContributor {
    private static final String NAME_PREFIX = "PostgreSQL - %s";
    private static final String DEFAULT_DRIVER_CLASS_NAME = "org.postgresql.Driver";

    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiPredicate<Object, AnActionEvent> condition = (r, e) -> r instanceof AzurePostgreSql;
        final BiConsumer<Object, AnActionEvent> handler = (c, e) -> CreatePostgreSqlAction.create((e.getProject()));
        am.registerHandler(ResourceCommonActionsContributor.CREATE, condition, handler);

        am.<AzResource<?, ?, ?>, AnActionEvent>registerHandler(ResourceCommonActionsContributor.CONNECT, (r, e) -> r instanceof PostgreSqlServer,
            (o, e) -> AzureTaskManager.getInstance().runLater(() -> {
                final ConnectorDialog dialog = new ConnectorDialog(e.getProject());
                final PostgreSqlServer server = (PostgreSqlServer) o;
                dialog.setResource(new SqlDatabaseResource<>(server.databases().list().get(0),
                    server.getAdminName() + "@" + server.getName(), PostgreSqlDatabaseResourceDefinition.INSTANCE));
                dialog.show();
            }));

        final BiConsumer<AzResource<?, ?, ?>, AnActionEvent> openDatabaseHandler = (c, e) -> openDatabaseTool(e.getProject(), (PostgreSqlServer) c);
        am.registerHandler(PostgreSqlActionsContributor.OPEN_DATABASE_TOOL, (r, e) -> true, openDatabaseHandler);
    }

    @AzureOperation(name = "postgre.open_by_database_tools.server", params = {"server.getName()"}, type = AzureOperation.Type.ACTION)
    private void openDatabaseTool(Project project, @Nonnull PostgreSqlServer server) {
        final IntellijDatasourceService.DatasourceProperties properties = IntellijDatasourceService.DatasourceProperties.builder()
            .name(String.format(NAME_PREFIX, server.getName()))
            .driverClassName(DEFAULT_DRIVER_CLASS_NAME)
            .url(JdbcUrl.postgre(Objects.requireNonNull(server.getFullyQualifiedDomainName()), "postgres").toString())
            .username(server.getAdminName() + "@" + server.getName())
            .build();
        AzureTaskManager.getInstance().runLater(() -> IntellijDatasourceService.getInstance().openDataSourceManagerDialog(project, properties));
    }

    @Override
    public int getOrder() {
        return PostgreSqlActionsContributor.INITIALIZE_ORDER + 1;
    }
}
