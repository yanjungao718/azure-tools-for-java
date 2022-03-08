/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.storage.action.explorer;

import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.storage.StorageAccount;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public abstract class AbstractAzureStorageExplorerHandler {

    private static final String STORAGE_EXPLORER_DOWNLOAD_URL = "https://go.microsoft.com/fwlink/?LinkId=723579";

    public void openResource(@Nonnull final StorageAccount storageAccount) {
        // Get resource url
        final String resourceUrl = "storageexplorer://v=1&accountid=" + storageAccount.getId() + "&subscriptionid=" + storageAccount.getSubscriptionId();
        // Get storage explorer path
        final String storageExplorerExecutable = getStorageExplorerExecutable();
        // Launch storage explorer with resource url
        if (StringUtils.isEmpty(storageExplorerExecutable)) {
            throw new AzureToolkitRuntimeException("Cannot find Azure Storage Explorer.", (Object[]) getStorageNotFoundActions());
        }
        launchStorageExplorer(storageExplorerExecutable, resourceUrl);
    }

    protected String getStorageExplorerExecutable() {
        final String storageExplorerPath = Azure.az().config().getStorageExplorerPath();
        return StringUtils.isEmpty(storageExplorerPath) ? getStorageExplorerExecutableFromOS() : storageExplorerPath;
    }

    protected Action<?>[] getStorageNotFoundActions() {
        // Download Storage Explorer
        final Consumer<Void> downloadConsumer = ignore ->
                AzureActionManager.getInstance().getAction(ResourceCommonActionsContributor.OPEN_URL).handle(STORAGE_EXPLORER_DOWNLOAD_URL);
        final Action<Void> downloadAction = new Action<>(downloadConsumer, new ActionView.Builder("Download"));
        downloadAction.setAuthRequired(false);
        // Open Azure Settings Panel, and re-run
        Action<Object> openAzureSettingAction = AzureActionManager.getInstance().getAction(ResourceCommonActionsContributor.OPEN_AZURE_SETTINGS);
        return new Action[]{downloadAction, openAzureSettingAction};
    }

    protected abstract String getStorageExplorerExecutableFromOS();

    protected abstract void launchStorageExplorer(final String explorer, String storageUrl);
}
