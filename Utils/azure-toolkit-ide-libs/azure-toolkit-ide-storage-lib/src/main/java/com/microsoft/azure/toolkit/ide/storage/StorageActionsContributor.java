/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.storage;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.ide.storage.action.OpenAzureStorageExplorerAction;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.storage.StorageAccount;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Optional;
import java.util.function.Consumer;

import static com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle.title;

public class StorageActionsContributor implements IActionsContributor {
    public static final int INITIALIZE_ORDER = ResourceCommonActionsContributor.INITIALIZE_ORDER + 1;

    public static final String SERVICE_ACTIONS = "actions.storage.service";
    public static final String ACCOUNT_ACTIONS = "actions.storage.account";

    public static final Action.Id<StorageAccount> OPEN_AZURE_STORAGE_EXPLORER = Action.Id.of("storage.open_azure_storage_explorer.account");
    public static final Action.Id<StorageAccount> COPY_CONNECTION_STRING = Action.Id.of("storage.copy_connection_string.account");
    public static final Action.Id<StorageAccount> COPY_PRIMARY_KEY = Action.Id.of("storage.copy_primary_key.account");

    @Override
    public void registerActions(AzureActionManager am) {
        final Consumer<StorageAccount> openAzureStorageExplorer = resource -> new OpenAzureStorageExplorerAction().openResource(resource);
        final ActionView.Builder openAzureStorageExplorerView = new ActionView.Builder("Open Azure Storage Explorer")
                .title(s -> Optional.ofNullable(s).map(r -> title("storage.open_azure_storage_explorer.account", ((StorageAccount) r).getName())).orElse(null))
                .enabled(s -> s instanceof StorageAccount);
        final Action<StorageAccount> openAzureStorageExplorerAction = new Action<>(openAzureStorageExplorer, openAzureStorageExplorerView);
        openAzureStorageExplorerAction.setShortcuts(am.getIDEDefaultShortcuts().view());
        am.registerAction(OPEN_AZURE_STORAGE_EXPLORER, openAzureStorageExplorerAction);

        final Consumer<StorageAccount> copyConnectionString = resource -> copyContentToClipboard(resource.getConnectionString());
        final ActionView.Builder copyConnectionStringView = new ActionView.Builder("Copy Connection String")
                .title(s -> Optional.ofNullable(s).map(r -> title("storage.copy_connection_string.account", ((StorageAccount) r).getName())).orElse(null))
                .enabled(s -> s instanceof StorageAccount);
        final Action<StorageAccount> copyConnectionStringAction = new Action<>(copyConnectionString, copyConnectionStringView);
        am.registerAction(COPY_CONNECTION_STRING, copyConnectionStringAction);

        final Consumer<StorageAccount> copyPrimaryKey = resource -> copyContentToClipboard(resource.getKey());
        final ActionView.Builder copyPrimaryView = new ActionView.Builder("Copy Primary Key")
                .title(s -> Optional.ofNullable(s).map(r -> title("storage.copy_primary_key.account", ((StorageAccount) r).getName())).orElse(null))
                .enabled(s -> s instanceof StorageAccount);
        final Action<StorageAccount> copyPrimaryKeyAction = new Action<>(copyPrimaryKey, copyPrimaryView);
        am.registerAction(COPY_PRIMARY_KEY, copyPrimaryKeyAction);
    }

    @Override
    public void registerGroups(AzureActionManager am) {
        final ActionGroup serviceActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.SERVICE_REFRESH,
            "---",
            ResourceCommonActionsContributor.CREATE
        );
        am.registerGroup(SERVICE_ACTIONS, serviceActionGroup);

        final ActionGroup accountActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.PIN,
            "---",
            ResourceCommonActionsContributor.REFRESH,
            ResourceCommonActionsContributor.OPEN_PORTAL_URL,
            StorageActionsContributor.OPEN_AZURE_STORAGE_EXPLORER,
            "---",
            StorageActionsContributor.COPY_CONNECTION_STRING,
            StorageActionsContributor.COPY_PRIMARY_KEY,
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

    public static void copyContentToClipboard(final String content) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(content), null);
    }
}
