/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.storage.action.explorer;

import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;

import java.io.IOException;

public class MacOSOpenAzureStorageExplorerHandler extends AbstractAzureStorageExplorerHandler {

    private static final String MAC_OS_STORAGE_EXPLORER_PATH = "/Contents/MacOS/Microsoft\\ Azure\\ Storage\\ Explorer";

    @Override
    protected String getStorageExplorerExecutableFromOS() {
        return MAC_OS_STORAGE_EXPLORER_PATH;
    }

    @Override
    protected void launchStorageExplorer(String explorer, String storageUrl) {
        final ProcessBuilder processBuilder = new ProcessBuilder();
        final String[] commands = new String[]{"open", "-a", explorer, storageUrl};
        processBuilder.command(commands);
        try {
            processBuilder.start();
        } catch (IOException e) {
            throw new AzureToolkitRuntimeException(e.getMessage(), e);
        }
    }
}
