/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azuretools.core.ui.login;

import com.microsoft.azure.toolkit.eclipse.common.component.AzureDialog;
import com.microsoft.azure.toolkit.eclipse.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.auth.model.AuthConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class ServicePrincipalLoginDialog extends AzureDialog<AuthConfiguration> {
    private ServicePrincipalLoginPanel loginPanel;

    public ServicePrincipalLoginDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));
        loginPanel = new ServicePrincipalLoginPanel(container, SWT.NONE);
        loginPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        return container;
    }

    @Override
    protected String getDialogTitle() {
        return "Sign In - Service Principal";
    }

    @Override
    public AzureForm<AuthConfiguration> getForm() {
        return this.loginPanel;
    }

    @Override
    protected Point getInitialSize() {
        this.getShell().layout();
        return this.getShell().computeSize(450, 440, true);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE);
        newShell.setMinimumSize(300, 340);
    }
}
