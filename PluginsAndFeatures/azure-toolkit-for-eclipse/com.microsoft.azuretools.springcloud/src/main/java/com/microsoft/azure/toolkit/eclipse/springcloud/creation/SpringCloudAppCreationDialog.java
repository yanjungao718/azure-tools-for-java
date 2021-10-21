package com.microsoft.azure.toolkit.eclipse.springcloud.creation;

import com.microsoft.azure.toolkit.eclipse.common.component.AzureDialog;
import com.microsoft.azure.toolkit.eclipse.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudCluster;
import com.microsoft.azure.toolkit.lib.springcloud.config.SpringCloudAppConfig;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class SpringCloudAppCreationDialog extends AzureDialog<SpringCloudAppConfig> {
    private final SpringCloudCluster cluster;
    private SpringCloudAppInfoAdvancedPanel advancedPanel;

    public SpringCloudAppCreationDialog(SpringCloudCluster cluster, Shell shell) {
        super(shell);
        this.cluster = cluster;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        this.advancedPanel = new SpringCloudAppInfoAdvancedPanel(container, this.cluster);
        this.advancedPanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
        return container;
    }

    @Override
    protected String getDialogTitle() {
        return "Create Azure Spring Cloud app";
    }

    @Override
    public AzureForm<SpringCloudAppConfig> getForm() {
        return this.advancedPanel;
    }
}
