/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.storage;

import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.event.AzureEventBus;
import com.microsoft.azure.toolkit.lib.common.event.AzureOperationEvent;
import com.microsoft.azure.toolkit.lib.storage.AzureStorageAccount;
import com.microsoft.azuretools.azurecommons.helpers.AzureCmdException;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import com.microsoft.tooling.msservices.serviceexplorer.AzureIconSymbol;
import com.microsoft.tooling.msservices.serviceexplorer.AzureRefreshableNode;
import com.microsoft.tooling.msservices.serviceexplorer.Node;

public class StorageModule extends AzureRefreshableNode {
    private static final String STORAGE_MODULE_ID = StorageModule.class.getName();
    private static final String BASE_MODULE_NAME = "Storage Accounts";
    public static final String MODULE_NAME = "Storage Account";

    public StorageModule(Node parent) {
        super(STORAGE_MODULE_ID, BASE_MODULE_NAME, parent, null);
        AzureEventBus.after("storage.create_account.account", this::onCreatedOrRemoved);
        AzureEventBus.after("storage.delete_account.account", this::onCreatedOrRemoved);
    }

    @Override
    public @Nullable AzureIconSymbol getIconSymbol() {
        return AzureIconSymbol.StorageAccount.MODULE;
    }

    @Override
    protected void refreshItems() throws AzureCmdException {
        Azure.az(AzureStorageAccount.class).list().stream().flatMap(m -> m.storageAccounts().list().stream())
                .map(account -> new StorageNode(this, account))
                .forEach(this::addChildNode);
    }

    private void onCreatedOrRemoved(AzureOperationEvent.Source source) {
        this.load(true);
    }

}
