/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.eclipse.common.component;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.microsoft.azure.toolkit.eclipse.common.component.resourcegroup.ResourceGroupComboBox;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroup;

public class SubscriptionAndResourceGroupComposite extends Composite {
    public static final int MINIMUM_LABEL_WIDTH = 95;
    private final SubscriptionComboBox cbSubs;
    private final ResourceGroupComboBox cbResourceGroup;

    public SubscriptionAndResourceGroupComposite(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout(2, false));

        Label lblSubscription = new Label(this, SWT.NONE);
        lblSubscription.setText("Subscription:");

        cbSubs = new SubscriptionComboBox(this);
        cbSubs.setRequired(true);
        cbSubs.setLabeledBy(lblSubscription);
        cbSubs.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblResourceGroup = new Label(this, SWT.NONE);
        GridData resourceGroupGrid = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
        resourceGroupGrid.minimumWidth = MINIMUM_LABEL_WIDTH;
        resourceGroupGrid.widthHint = MINIMUM_LABEL_WIDTH;
        lblResourceGroup.setLayoutData(resourceGroupGrid);
        lblResourceGroup.setText("Resource Group:");

        cbResourceGroup = new ResourceGroupComboBox(this);
        cbResourceGroup.setRequired(true);
        cbResourceGroup.setLabeledBy(lblResourceGroup);
        cbResourceGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        cbSubs.addValueChangedListener(cbResourceGroup::setSubscription);
        cbSubs.refreshItems();
    }

    public Subscription getSubscription() {
        return cbSubs.getValue();
    }

    public ResourceGroup getResourceGroup() {
        return cbResourceGroup.getValue();
    }

    public List<AzureFormInput<?>> getInputs() {
        return Arrays.asList(cbSubs, cbResourceGroup);
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    public SubscriptionComboBox getSubscriptionComboBox() {
        return cbSubs;
    }

    public ResourceGroupComboBox getResourceGroupComboBox() {
        return cbResourceGroup;
    }
}
