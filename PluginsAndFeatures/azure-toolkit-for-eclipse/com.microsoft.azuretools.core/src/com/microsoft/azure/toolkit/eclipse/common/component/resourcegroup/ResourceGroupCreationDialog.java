/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.eclipse.common.component.resourcegroup;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ResourceGroupCreationDialog extends Dialog {
    private Text textName;

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public ResourceGroupCreationDialog(Shell parentShell) {
        super(parentShell);
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

        Label lblNewLabel = new Label(container, SWT.WRAP);
        GridData gd_lblNewLabel = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        gd_lblNewLabel.widthHint = 160;
        lblNewLabel.setLayoutData(gd_lblNewLabel);
        lblNewLabel.setText("A resource group is a container that holds related resources for an Azure solution.");

        Label lblName = new Label(container, SWT.NONE);
        lblName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblName.setText("Name:");

        textName = new Text(container, SWT.BORDER);
        textName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        return container;
    }

    /**
     * Create contents of the button bar.
     *
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Return the initial size of the dialog.
     */
    @Override
    protected Point getInitialSize() {
        return new Point(450, 174);
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
}
