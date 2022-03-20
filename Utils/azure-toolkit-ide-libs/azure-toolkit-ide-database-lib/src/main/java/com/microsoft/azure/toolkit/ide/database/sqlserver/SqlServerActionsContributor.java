/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.database.sqlserver;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.entity.IAzureBaseResource;
import com.microsoft.azure.toolkit.lib.common.model.AzResourceBase;
import com.microsoft.azure.toolkit.lib.sqlserver.MicrosoftSqlServer;

import java.util.Optional;

import static com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle.title;

public class SqlServerActionsContributor implements IActionsContributor {
    public static final int INITIALIZE_ORDER = ResourceCommonActionsContributor.INITIALIZE_ORDER + 1;

    public static final String SERVICE_ACTIONS = "actions.sqlserver.service";
    public static final String SERVER_ACTIONS = "actions.sqlserver.server";

    private static final String NAME_PREFIX = "SqlServer Server - %s";
    public static final Action.Id<IAzureBaseResource<?, ?>> OPEN_DATABASE_TOOL = Action.Id.of("action.sqlserver.open_database_tool");

    @Override
    public void registerActions(AzureActionManager am) {
        final ActionView.Builder openDatabaseTool = new ActionView.Builder("Open by Database Tools", "icons/action/open_database_tool.svg")
                .title(s -> Optional.ofNullable(s).map(r -> title("sqlserver.connect_server.server", ((IAzureBaseResource<?, ?>) r).name())).orElse(null))
                .enabled(s -> s instanceof MicrosoftSqlServer && ((AzResourceBase) s).getFormalStatus().isRunning());
        final Action<IAzureBaseResource<?, ?>> action = new Action<>(openDatabaseTool);
        action.setShortcuts("control alt D");
        am.registerAction(OPEN_DATABASE_TOOL, action);
    }

    public int getOrder() {
        return INITIALIZE_ORDER;
    }

    @Override
    public void registerGroups(AzureActionManager am) {
        final ActionGroup serviceActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.REFRESH,
            "---",
            ResourceCommonActionsContributor.CREATE
        );
        am.registerGroup(SERVICE_ACTIONS, serviceActionGroup);

        final ActionGroup serverActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.PIN,
            "---",
            ResourceCommonActionsContributor.REFRESH,
            ResourceCommonActionsContributor.OPEN_PORTAL_URL,
            ResourceCommonActionsContributor.SHOW_PROPERTIES,
            "---",
            SqlServerActionsContributor.OPEN_DATABASE_TOOL,
            ResourceCommonActionsContributor.CONNECT,
            "---",
            ResourceCommonActionsContributor.DELETE
        );
        am.registerGroup(SERVER_ACTIONS, serverActionGroup);
    }
}
