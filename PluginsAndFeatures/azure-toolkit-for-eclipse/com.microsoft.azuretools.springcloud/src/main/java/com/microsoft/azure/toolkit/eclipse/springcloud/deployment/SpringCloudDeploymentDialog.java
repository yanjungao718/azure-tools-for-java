package com.microsoft.azure.toolkit.eclipse.springcloud.deployment;

import com.microsoft.azure.toolkit.eclipse.common.component.AzureDialog;
import com.microsoft.azure.toolkit.eclipse.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.springcloud.config.SpringCloudAppConfig;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class SpringCloudDeploymentDialog extends AzureDialog<SpringCloudAppConfig> {

    private SpringCloudDeploymentConfigurationPanel deploymentPanel;

    public SpringCloudDeploymentDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected String getDialogTitle() {
        return "Deploy to Azure Spring Cloud";
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        deploymentPanel = new SpringCloudDeploymentConfigurationPanel(container);
        deploymentPanel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        return container;
    }

    @Override
    public AzureForm<SpringCloudAppConfig> getForm() {
        return deploymentPanel;
    }

}
