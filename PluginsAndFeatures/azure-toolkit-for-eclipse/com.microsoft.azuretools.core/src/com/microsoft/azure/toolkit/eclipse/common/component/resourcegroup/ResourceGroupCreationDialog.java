/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.eclipse.common.component.resourcegroup;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.toolkit.eclipse.common.component.AzureDialog;
import com.microsoft.azure.toolkit.eclipse.common.component.AzureTextInput;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessageBundle;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.resource.AzureResources;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroupDraft;

public class ResourceGroupCreationDialog extends AzureDialog<ResourceGroupDraft> implements AzureForm<ResourceGroupDraft> {
    private static final Pattern PATTERN = Pattern.compile("^[-\\w._()]+$");
    public static final String CONFLICT_NAME = "A resource group with the same name already exists in the selected subscription %s";

    private AzureTextInput textName;
    private Subscription subscription;

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public ResourceGroupCreationDialog(Shell parentShell, Subscription subscription) {
        super(parentShell);
        setShellStyle(SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
        this.subscription = subscription;
    }

    /**
     * Create contents of the dialog.
     *
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) container.getLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginWidth = 5;

        Label lblNewLabel = new Label(container, SWT.WRAP);
        GridData gdLblNewLabel = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        gdLblNewLabel.widthHint = 160;
        lblNewLabel.setLayoutData(gdLblNewLabel);
        lblNewLabel.setText("A resource group is a container that holds related resources for an Azure solution.");

        Label lblName = new Label(container, SWT.NONE);
        lblName.setText("Name:");

        textName = new AzureTextInput(container, SWT.BORDER);
        textName.setRequired(true);
        textName.setLabeledBy(lblName);
        textName.addValidator(() -> validateResourceGroupName());
        GridData textGrid = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        textGrid.widthHint = 257;
        textName.setLayoutData(textGrid);

        return container;
    }

    public ResourceGroupDraft getData() {
        return data;
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            final String rgName = this.textName.getValue();
            this.data = Azure.az(AzureResources.class).groups(this.subscription.getId()).create(rgName, rgName);
        }
        super.buttonPressed(buttonId);
    }

    private ResourceGroupDraft data;

    @Override
    protected String getDialogTitle() {
        return "New Resource Group";
    }

    @Override
    public AzureForm<ResourceGroupDraft> getForm() {
        return this;
    }

    @Override
    public ResourceGroupDraft getValue() {
        return data;
    }

    @Override
    public void setValue(ResourceGroupDraft draft) {
        Optional.ofNullable(draft).ifPresent(draftGroup -> textName.setValue(draft.getName()));
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        return Arrays.asList(textName);
    }

    // Copied from com.microsoft.azure.toolkit.intellij.common.component.resourcegroup.ResourceGroupNameTextField
    private AzureValidationInfo validateResourceGroupName() {
        final String value = textName.getValue();
        // validate length
        int minLength = 1;
        int maxLength = 90;
        if (StringUtils.length(value) < minLength) {
            return AzureValidationInfo.builder().input(this)
                .message("The value must not be empty.")
                .type(AzureValidationInfo.Type.ERROR).build();
        } else if (StringUtils.length(value) > maxLength) {
            return AzureValidationInfo.error(AzureMessageBundle.message("common.resourceGroup.validate.length", maxLength).toString(), textName);
        }
        // validate special character
        if (!PATTERN.matcher(value).matches()) {
            return AzureValidationInfo.error(AzureMessageBundle.message("common.resourceGroup.validate.invalid").toString(), textName);
        }
        // validate availability
        try {
            if (Azure.az(AzureResources.class).groups(subscription.getId()).exists(value)) {
                return AzureValidationInfo.error(String.format(CONFLICT_NAME, subscription.getName()), this);
            }
        } catch (CloudException e) {
            return AzureValidationInfo.builder().input(this).message(e.getMessage()).type(AzureValidationInfo.Type.ERROR).build();
        }
        return AzureValidationInfo.success(textName);
    }
}
