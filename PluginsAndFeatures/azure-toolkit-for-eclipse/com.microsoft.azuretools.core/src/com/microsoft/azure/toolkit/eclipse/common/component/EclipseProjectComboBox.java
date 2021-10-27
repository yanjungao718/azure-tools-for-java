/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.Composite;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;

public class EclipseProjectComboBox extends AzureComboBox<IProject> {

    public EclipseProjectComboBox(Composite parent) {
        super(parent, false);
    }

    @Override
    public String getItemText(Object item) {
        // TODO Auto-generated method stub
        return item instanceof IProject ? ((IProject) item).getName() : super.getItemText(item);
    }

    @Override
    @AzureOperation(name = "common|region.list.subscription", // TODO: add properties
            params = { "this.subscription.getId()" }, type = AzureOperation.Type.SERVICE)
    protected List<? extends IProject> loadItems() {
        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        return Optional.ofNullable(workspace).map(IWorkspace::getRoot).map(IWorkspaceRoot::getProjects)
                .map(projects -> Arrays.asList(projects))
                .orElse(Collections.emptyList());
    }

}