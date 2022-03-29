/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.explorer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import com.microsoft.azure.toolkit.ide.common.IExplorerNodeProvider;
import com.microsoft.azure.toolkit.ide.common.component.Node;

public class AzureExplorer {
    private static final AzureExplorerNodeProviderManager manager = new AzureExplorerNodeProviderManager();

    public static Node<?>[] getModules() {
        return manager.getRootNodes().stream().toArray(Node<?>[]::new);
    }

    private static class AzureExplorerNodeProviderManager implements IExplorerNodeProvider.Manager {
        private static final String EXTENSION_POINT_ID = "com.microsoft.azure.toolkit.eclipse.explorer";

        public List<Node<?>> getRootNodes() {
            IConfigurationElement[] configurationElements = Platform.getExtensionRegistry()
                    .getConfigurationElementsFor(EXTENSION_POINT_ID);
            return Arrays.stream(configurationElements).map(element -> {
                try {
                    return element.createExecutableExtension("implementation");
                } catch (CoreException e) {
                    return null;
                }
            }).filter(object -> object instanceof IExplorerNodeProvider)
                    .map(object -> ((IExplorerNodeProvider) object).getModuleNode(null, this))
                    .collect(Collectors.toList());
        }

        @Override
        public Node<?> createNode(Object o, Node<?> parent) {
            IConfigurationElement[] configurationElements = Platform.getExtensionRegistry()
                    .getConfigurationElementsFor(EXTENSION_POINT_ID);
            return Arrays.stream(configurationElements).map(element -> {
                try {
                    return element.createExecutableExtension("implementation");
                } catch (CoreException e) {
                    return null;
                }
            }).filter(object -> object instanceof IExplorerNodeProvider).map(object -> (IExplorerNodeProvider) object)
                    .filter(p -> p.accept(o, parent)).findAny().map(p -> p.createNode(o, parent, this)).orElse(null);
        }
    }
}
