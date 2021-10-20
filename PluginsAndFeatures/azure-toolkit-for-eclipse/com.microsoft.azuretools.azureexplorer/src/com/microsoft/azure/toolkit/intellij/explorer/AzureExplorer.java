/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.explorer;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import com.microsoft.azure.toolkit.ide.common.IExplorerContributor;
import com.microsoft.azure.toolkit.ide.common.component.Node;


public class AzureExplorer {
    private static final String EXTENSION_POINT_ID = "com.microsoft.azure.toolkit.explorer";

    public static Node<?>[] getModules() {
        IConfigurationElement[] configurationElements = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(EXTENSION_POINT_ID);
        return Arrays.stream(configurationElements).map(element -> {
            try {
                return element.createExecutableExtension("implementation");
            } catch (CoreException e) {
                return null;
            }
        }).filter(object -> object instanceof IExplorerContributor)
                .map(object -> ((IExplorerContributor) object).getModuleNode())
                .toArray(Node<?>[]::new);
    }
}
