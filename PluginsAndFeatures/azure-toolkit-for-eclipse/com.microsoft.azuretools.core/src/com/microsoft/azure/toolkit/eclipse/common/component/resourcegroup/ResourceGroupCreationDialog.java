/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.eclipse.common.component.resourcegroup;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import com.microsoft.azure.toolkit.eclipse.common.component.AzureTextInput;
import com.microsoft.azure.toolkit.eclipse.common.component.AzureDialog;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;

public class ResourceGroupCreationDialog extends AzureDialog<DraftResourceGroup> implements AzureForm<DraftResourceGroup> {
    private AzureTextInput textName;

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public ResourceGroupCreationDialog(Shell parentShell) {
        super(parentShell);
        setShellStyle(SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE);
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
        GridData gd_lblNewLabel = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        gd_lblNewLabel.widthHint = 160;
        lblNewLabel.setLayoutData(gd_lblNewLabel);
        lblNewLabel.setText("A resource group is a container that holds related resources for an Azure solution.");

        Label lblName = new Label(container, SWT.NONE);
        lblName.setText("Name:");

        textName = new AzureTextInput(container, SWT.BORDER);
        textName.setRequired(true);
        textName.setLabeledBy(lblName);
        GridData textGrid = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        textGrid.widthHint = 257;
        textName.setLayoutData(textGrid);

        return container;
    }

    public DraftResourceGroup getData() {
        return data;
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            this.data = new DraftResourceGroup(this.textName.getText());
        }
        super.buttonPressed(buttonId);
    }

    private DraftResourceGroup data;

    @Override
    protected String getDialogTitle() {
        return "New Resource Group";
    }

    @Override
    public AzureForm<DraftResourceGroup> getForm() {
        return this;
    }

    @Override
    public DraftResourceGroup getValue() {
        return data;
    }

    @Override
    public void setValue(DraftResourceGroup draft) {
        Optional.ofNullable(draft).ifPresent(draftGroup -> textName.setValue(draft.getName()));
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        return Arrays.asList(textName);
    }
}
