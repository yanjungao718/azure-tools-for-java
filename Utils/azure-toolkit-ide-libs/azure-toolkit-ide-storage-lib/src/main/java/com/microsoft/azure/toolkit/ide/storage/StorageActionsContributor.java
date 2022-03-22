/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.storage;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.ide.storage.action.OpenStorageExplorerAction;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.storage.StorageAccount;

import java.util.Optional;
import java.util.function.Consumer;

import static com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle.title;

public class StorageActionsContributor implements IActionsContributor {
    public static final int INITIALIZE_ORDER = ResourceCommonActionsContributor.INITIALIZE_ORDER + 1;

    public static final String SERVICE_ACTIONS = "actions.storage.service";
    public static final String ACCOUNT_ACTIONS = "actions.storage.account";

    public static final Action.Id<StorageAccount> OPEN_STORAGE_EXPLORER = Action.Id.of("action.storage.open_storage_explorer");

    @Override
    public void registerActions(AzureActionManager am) {
        final Consumer<StorageAccount> open = OpenStorageExplorerAction::openStorageBrowser;
        final ActionView.Builder openView = new ActionView.Builder("Open Storage Explorer", "/icons/action/portal.svg")
                .title(s -> Optional.ofNullable(s).map(r -> title("storage.open_storage_explorer.account", ((StorageAccount) r).name())).orElse(null))
                .enabled(s -> s instanceof StorageAccount && !((StorageAccount) s).getFormalStatus().isDeleted());
        final Action<StorageAccount> action = new Action<>(open, openView);
        action.setShortcuts(am.getIDEDefaultShortcuts().view());
        am.registerAction(OPEN_STORAGE_EXPLORER, action);
    }

    @Override
    public void registerGroups(AzureActionManager am) {
        final ActionGroup serviceActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.REFRESH,
            "---",
            ResourceCommonActionsContributor.CREATE
        );
        am.registerGroup(SERVICE_ACTIONS, serviceActionGroup);

        final ActionGroup accountActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.PIN,
            "---",
            ResourceCommonActionsContributor.REFRESH,
            ResourceCommonActionsContributor.OPEN_PORTAL_URL,
            "---",
            ResourceCommonActionsContributor.CONNECT,
            "---",
            ResourceCommonActionsContributor.DELETE
        );
        am.registerGroup(ACCOUNT_ACTIONS, accountActionGroup);
    }

    @Override
    public int getOrder() {
        return INITIALIZE_ORDER;
    }
}
