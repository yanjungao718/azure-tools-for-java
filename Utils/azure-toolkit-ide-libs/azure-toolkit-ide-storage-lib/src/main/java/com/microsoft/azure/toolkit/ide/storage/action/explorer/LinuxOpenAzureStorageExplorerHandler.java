/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.storage.action.explorer;

import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.storage.StorageAccount;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class LinuxOpenAzureStorageExplorerHandler extends AbstractAzureStorageExplorerHandler {

    @Override
    protected boolean launchStorageExplorerWithUri(@NotNull StorageAccount storageAccount, @NotNull String resourceUrl) {
        // Launch storage explorer from uri is not supported for Linux
        // Refers https://docs.microsoft.com/en-us/azure/storage/common/storage-explorer-direct-link
        return false;
    }

    @Override
    protected String getStorageExplorerExecutableFromOS() {
        return null;
    }

    @Override
    protected void launchStorageExplorer(String explorer, String storageUrl) {
        final ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(explorer, storageUrl);
        try {
            processBuilder.start();
        } catch (IOException e) {
            throw new AzureToolkitRuntimeException(e.getMessage(), e);
        }
    }
}
