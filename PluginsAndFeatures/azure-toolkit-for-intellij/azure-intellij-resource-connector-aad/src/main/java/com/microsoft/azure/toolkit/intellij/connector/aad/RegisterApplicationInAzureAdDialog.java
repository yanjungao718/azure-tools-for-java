/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.aad;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.AzureDialog;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;

/**
 * UI used by {@code com.microsoft.azure.toolkit.intellij.connector.aad.RegisterApplicationAction}.
 */
class RegisterApplicationInAzureAdDialog extends AzureDialog<ApplicationRegistrationModel> {
    private final RegisterAzureApplicationForm form;

    RegisterApplicationInAzureAdDialog(@Nonnull Project project) {
        super(project);
        form = new RegisterAzureApplicationForm(project);
        init();
    }

    @Override
    public AzureForm<ApplicationRegistrationModel> getForm() {
        return form;
    }

    @Override
    protected String getDialogTitle() {
        return MessageBundle.message("registerAppDialog.title");
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        return form.getContentPanel();
    }

    @Override
    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return form.getPreferredFocusedComponent();
    }

    @Nullable
    public Subscription getSubscription() {
        return form.getSubscription();
    }
}
