/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.springcloud.streaminglog;

import com.azure.resourcemanager.appplatform.models.DeploymentInstance;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ListCellRendererWithRightAlignedComponent;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudDeploymentInstanceEntity;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class SpringCloudStreamingLogDialog extends DialogWrapper {

    private JPanel pnlRoot;
    private JComboBox<DeploymentInstance> cbInstances;

    private SpringCloudDeploymentInstanceEntity instance;

    public SpringCloudStreamingLogDialog(@Nullable final Project project, List<DeploymentInstance> instances) {
        super(project, false);
        setTitle("Select Instance");
        instances.forEach(instance -> cbInstances.addItem(instance));
        cbInstances.setRenderer(new ListCellRendererWithRightAlignedComponent<>() {
            @Override
            protected void customize(final DeploymentInstance deploymentInstance) {
                setLeftText(deploymentInstance.name());
            }
        });

        init();
    }

    public SpringCloudDeploymentInstanceEntity getInstance() {
        return instance;
    }

    @Override
    protected void doOKAction() {
        instance = (SpringCloudDeploymentInstanceEntity) cbInstances.getSelectedItem();
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        instance = null;
        super.doCancelAction();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return pnlRoot;
    }
}
