/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.database.mysql;

import com.microsoft.azure.toolkit.ide.common.IExplorerContributor;
import com.microsoft.azure.toolkit.ide.common.component.AzureResourceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.AzureServiceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.lib.mysql.AzureMySql;
import com.microsoft.azure.toolkit.lib.mysql.MySqlServer;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.microsoft.azure.toolkit.lib.Azure.az;

public class MySqlExplorerContributor implements IExplorerContributor {
    private static final String NAME = "Azure Database for MySQL";
    private static final String ICON = "/icons/Microsoft.DBforMySQL/default.svg";

    @Override
    public Node<?> getModuleNode() {
        final AzureMySql service = az(AzureMySql.class);
        final Function<AzureMySql, List<MySqlServer>> servers = s -> s.list().stream()
            .flatMap(m -> m.servers().list().stream()).collect(Collectors.toList());
        return new Node<>(service).view(new AzureServiceLabelView<>(service, NAME, ICON))
            .actions(MySqlActionsContributor.SERVICE_ACTIONS)
            .addChildren(servers, (server, serviceNode) -> new Node<>(server)
                .view(new AzureResourceLabelView<>(server))
                .actions(MySqlActionsContributor.SERVER_ACTIONS));
    }
}
