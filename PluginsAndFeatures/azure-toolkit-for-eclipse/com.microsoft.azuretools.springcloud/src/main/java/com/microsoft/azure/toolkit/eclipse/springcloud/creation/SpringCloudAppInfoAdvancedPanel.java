/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.springcloud.creation;

import com.microsoft.azure.toolkit.eclipse.common.component.AzureTextInput;
import com.microsoft.azure.toolkit.eclipse.common.component.SubscriptionComboBox;
import com.microsoft.azure.toolkit.eclipse.springcloud.component.SpringCloudAppConfigPanel;
import com.microsoft.azure.toolkit.eclipse.springcloud.component.SpringCloudClusterComboBox;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudCluster;
import com.microsoft.azure.toolkit.lib.springcloud.config.SpringCloudAppConfig;
import lombok.AccessLevel;
import lombok.Getter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

@Getter(AccessLevel.PROTECTED)
public class SpringCloudAppInfoAdvancedPanel extends AbstractSpringCloudAppInfoPanel {
    private SubscriptionComboBox selectorSubscription;
    private SpringCloudClusterComboBox selectorCluster;
    private AzureTextInput textName;
    private SpringCloudAppConfigPanel formConfig;

    public SpringCloudAppInfoAdvancedPanel(Composite parent, @Nullable final SpringCloudCluster cluster) {
        super(parent, cluster);
        $$$setupUI$$$();
        this.init();
    }

    @Override
    protected void init() {
        super.init();
        this.textName.setRequired(true);
        this.selectorSubscription.refreshItems();
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    private void $$$setupUI$$$() {
        setLayout(new GridLayout(1, false));

        Group grpTests = new Group(this, SWT.NONE);
        grpTests.setLayout(new GridLayout(2, false));
        GridData gd_grpTests = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gd_grpTests.minimumHeight = 86;
        grpTests.setLayoutData(gd_grpTests);
        grpTests.setText("App Basics");

        Label lblSubscription = new Label(grpTests, SWT.NONE);
        GridData gd_lblSubscription = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblSubscription.widthHint = 100;
        lblSubscription.setLayoutData(gd_lblSubscription);
        lblSubscription.setText("Subscription:");
        this.selectorSubscription = new SubscriptionComboBox(grpTests);
        this.selectorSubscription.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        this.selectorSubscription.setLabeledBy(lblSubscription);

        Label lblService = new Label(grpTests, SWT.NONE);
        lblService.setText("Service:");
        this.selectorCluster = new SpringCloudClusterComboBox(grpTests);
        this.selectorCluster.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        this.selectorCluster.setLabeledBy(lblService);

        Label lblAppName = new Label(grpTests, SWT.NONE);
        lblAppName.setText("App name:");
        this.textName = new AzureTextInput(grpTests);
        this.textName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        this.textName.setLabeledBy(lblAppName);

        this.formConfig = new SpringCloudAppConfigPanel(this);
        GridLayout gridLayout = (GridLayout) this.formConfig.getLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        this.formConfig.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
    }

    protected void onAppChanged(SpringCloudApp app) {
        AzureTaskManager.getInstance().runLater(() -> this.formConfig.updateForm(app));
        super.onAppChanged(app);
    }

    @Override
    public SpringCloudAppConfig getValue() {
        final SpringCloudAppConfig config = this.formConfig.getValue();
        return super.getValue(config);
    }

    @Override
    public void setValue(final SpringCloudAppConfig config) {
        super.setValue(config);
        this.formConfig.setValue(config);
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        final List<AzureFormInput<?>> inputs = this.formConfig.getInputs();
        inputs.addAll(Arrays.asList(this.getSelectorSubscription(), this.getSelectorCluster(), this.getTextName()));
        return inputs;
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    public SubscriptionComboBox getSelectorSubscription() {
        return this.selectorSubscription;
    }

    public SpringCloudClusterComboBox getSelectorCluster() {
        return selectorCluster;
    }

    public AzureTextInput getTextName() {
        return textName;
    }

    public SpringCloudAppConfigPanel getFormConfig() {
        return formConfig;
    }

    public void setFormConfig(SpringCloudAppConfigPanel formConfig) {
        this.formConfig = formConfig;
    }
}
