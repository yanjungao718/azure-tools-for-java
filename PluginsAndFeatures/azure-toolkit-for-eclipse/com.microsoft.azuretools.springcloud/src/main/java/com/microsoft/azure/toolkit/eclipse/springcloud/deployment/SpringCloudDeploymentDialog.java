/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.springcloud.deployment;

import com.microsoft.azure.toolkit.eclipse.common.component.AzureDialog;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.springcloud.config.SpringCloudAppConfig;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SpringCloudDeploymentDialog extends AzureDialog<SpringCloudAppConfig> {

    private SpringCloudDeploymentConfigurationPanel deploymentPanel;
    private Button buildArtifact;

    public SpringCloudDeploymentDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) container.getLayout();
        gridLayout.marginWidth = 5;
        deploymentPanel = new SpringCloudDeploymentConfigurationPanel(container);
        deploymentPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        Composite composite = new Composite(container, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        composite.setLayout(new GridLayout(2, false));

        Label lblNewLabel = new Label(composite, SWT.NONE);
        GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblNewLabel.widthHint = 100;
        lblNewLabel.setLayoutData(gd_lblNewLabel);
        lblNewLabel.setText("Before deploy:");

        this.buildArtifact = new Button(composite, SWT.CHECK);
        this.buildArtifact.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        this.buildArtifact.setText("Build Maven artifact");
        return container;
    }

    @Override
    protected String getDialogTitle() {
        return "Deploy to Azure Spring Apps";
    }

    public boolean getBuildArtifact() {
        return this.buildArtifact.getSelection();
    }

    @Override
    public AzureForm<SpringCloudAppConfig> getForm() {
        return deploymentPanel;
    }

}
