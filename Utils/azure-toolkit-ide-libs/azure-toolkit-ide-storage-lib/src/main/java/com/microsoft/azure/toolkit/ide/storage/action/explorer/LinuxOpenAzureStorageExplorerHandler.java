/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.storage.action.explorer;

import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;

import java.io.IOException;

public class LinuxOpenAzureStorageExplorerHandler extends AbstractAzureStorageExplorerHandler {
    @Override
    protected String getStorageExplorerExecutableFromOS() {
        return null;
    }

    @Override
    protected void launchStorageExplorer(String explorer, String storageUrl) {
        final ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(explorer, storageUrl);
        try {
            processBuilder.start().waitFor();
        } catch (IOException | InterruptedException e) {
            throw new AzureToolkitRuntimeException(e.getMessage(), e);
        }
    }
}
