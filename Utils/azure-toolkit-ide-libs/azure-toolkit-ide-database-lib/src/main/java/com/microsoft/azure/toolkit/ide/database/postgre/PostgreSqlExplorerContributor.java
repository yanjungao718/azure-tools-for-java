/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.database.postgre;

import com.microsoft.azure.toolkit.ide.common.IExplorerContributor;
import com.microsoft.azure.toolkit.ide.common.component.AzureResourceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.AzureServiceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.lib.postgre.AzurePostgreSql;
import com.microsoft.azure.toolkit.lib.postgre.PostgreSqlServer;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.microsoft.azure.toolkit.lib.Azure.az;

public class PostgreSqlExplorerContributor implements IExplorerContributor {
    private static final String NAME = "Azure Database for PostgreSQL";
    private static final String ICON = "/icons/Microsoft.DBforPostgreSQL/default.svg";

    @Override
    public Node<?> getModuleNode() {
        final AzurePostgreSql service = az(AzurePostgreSql.class);
        final Function<AzurePostgreSql, List<PostgreSqlServer>> servers = s -> s.list().stream()
            .flatMap(m -> m.servers().list().stream()).collect(Collectors.toList());
        return new Node<>(service).view(new AzureServiceLabelView<>(service, NAME, ICON))
            .actions(PostgreSqlActionsContributor.SERVICE_ACTIONS)
            .addChildren(servers, (server, serviceNode) -> new Node<>(server)
                .view(new AzureResourceLabelView<>(server))
                .actions(PostgreSqlActionsContributor.SERVER_ACTIONS));
    }
}
