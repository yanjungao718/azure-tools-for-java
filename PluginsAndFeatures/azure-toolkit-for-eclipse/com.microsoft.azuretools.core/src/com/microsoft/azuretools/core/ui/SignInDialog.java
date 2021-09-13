/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.core.ui;

import com.microsoft.azure.toolkit.lib.auth.model.AuthConfiguration;
import com.microsoft.azure.toolkit.lib.auth.model.AuthType;
import com.microsoft.azuretools.core.components.AzureTitleAreaDialogWrapper;
import lombok.SneakyThrows;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class SignInDialog extends AzureTitleAreaDialogWrapper {
    private static final String AZURE_SIGN_IN = "Azure Sign In";
    private Button btnAzureCli;
    private Button btnDeviceCode;
    private Button btnSPRadio;
    private Label lblAzureCli;
    private Label lblDeviceInfo;
    private Label lblSP;

    private AuthConfiguration data = new AuthConfiguration();

    /**
     * Create the dialog.
     * @param parentShell
     *
     */
    public SignInDialog(Shell parentShell) {
        super(parentShell);
        setHelpAvailable(false);
        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        Button okButton = getButton(IDialogConstants.OK_ID);
        okButton.setText("Sign in");
    }

    /**
     * Create contents of the dialog.
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        setMessage(AZURE_SIGN_IN);
        setTitle(AZURE_SIGN_IN);
        getShell().setText(AZURE_SIGN_IN);
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayout(new FillLayout(SWT.HORIZONTAL));
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite composite = new Composite(container, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        Group group = new Group(composite, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        group.setText("Authentication Method");
        group.setLayout(new GridLayout(1, false));

        btnAzureCli = createRadioButton(group, "Azure CLI", AuthType.AZURE_CLI);
        lblAzureCli = createDescriptionLabel(group, "Consume your existing Azure CLI credential..");

        btnDeviceCode = createRadioButton(group, "Device Login", AuthType.DEVICE_CODE);
        lblDeviceInfo = createDescriptionLabel(group, "You will need to open an external browser and sign in with a generated device code.");

        btnSPRadio = createRadioButton(group, "Service Principal", AuthType.SERVICE_PRINCIPAL);

        lblSP = createDescriptionLabel(group, "Use Azure Active Directory service principal for sign in.");

        return area;
    }

    private Button createRadioButton(Composite parent, String label, AuthType type) {
        final Button radioButton = new Button(parent, SWT.RADIO);
        radioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (radioButton.getSelection()) {
                    // Set the radio button to be focused or the default one will be selected when refresh
                    // For issue https://github.com/microsoft/azure-tools-for-java/issues/3543
                    radioButton.setFocus();
                    data.setType(type);
                    syncControlControls();
                }
            }
        });
        radioButton.setText(label);
        return radioButton;
    }

    private Label createDescriptionLabel(Composite parent, String description) {
        Composite compositeDevice = new Composite(parent, SWT.NONE);
        GridData gdCompositeDevice = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdCompositeDevice.heightHint = 38;
        gdCompositeDevice.widthHint = 66;
        compositeDevice.setLayoutData(gdCompositeDevice);
        compositeDevice.setLayout(new GridLayout(1, false));
        Label label = new Label(compositeDevice, SWT.WRAP);
        GridData gdLblDeviceInfo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gdLblDeviceInfo.horizontalIndent = 11;
        label.setLayoutData(gdLblDeviceInfo);
        label.setText(description);
        return label;
        //
    }

    private void syncControlControls() {
        setErrorMessage(null);
        AuthType type = data.getType();
        lblDeviceInfo.setEnabled(type == AuthType.DEVICE_CODE);
        lblAzureCli.setEnabled(type == AuthType.AZURE_CLI);
        boolean spLoginSelected = type == AuthType.SERVICE_PRINCIPAL;
        lblSP.setEnabled(spLoginSelected);
    }

    @SneakyThrows
    @Override
    public void okPressed() {
        if (btnAzureCli.getSelection()) {
            data.setType(AuthType.AZURE_CLI);
        } else if (btnDeviceCode.getSelection()) {
            data.setType(AuthType.DEVICE_CODE);

        } else if (btnSPRadio.getSelection()) {
            data.setType(AuthType.SERVICE_PRINCIPAL);
        }
        super.okPressed();
    }

    public AuthConfiguration getData() {
        return data;
    }

}
