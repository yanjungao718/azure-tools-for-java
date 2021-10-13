/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.aad;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.ComponentsKt;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.microsoft.azure.toolkit.intellij.common.AzureFormJPanel;
import com.microsoft.azure.toolkit.intellij.common.AzureHideableTitledSeparator;
import com.microsoft.azure.toolkit.intellij.common.AzureTextInput;
import com.microsoft.azure.toolkit.intellij.common.component.SubscriptionComboBox;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.List;

class RegisterAzureApplicationForm implements AzureFormJPanel<ApplicationRegistrationModel> {
    private JPanel contentPanel;

    private AzureTextInput displayNameInput;
    private AzureTextInput domainInput;
    private JBCheckBox multiTenantInput;
    private AzureTextInput clientIdInput;
    private JPanel advancedSettingsContentPanel;
    private TitledSeparator advancedSettingsSeparator;
    private JComponent noteComponent;
    private JBLabel clientIdNote;
    private AzureEditableCallbackUrlsCombobox callbackUrlsInput;
    private SubscriptionComboBox subscriptionBox;

    RegisterAzureApplicationForm(@Nonnull Project project) {
        var separator = (AzureHideableTitledSeparator) advancedSettingsSeparator;
        separator.addContentComponent(advancedSettingsContentPanel);
        separator.collapse();

        clientIdNote.setAllowAutoWrapping(true);

        subscriptionBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateRegistrationModel(project, (Subscription) e.getItem());
            }
        });
    }

    JComponent getPreferredFocusedComponent() {
        return subscriptionBox;
    }

    @Override
    public JPanel getContentPanel() {
        return contentPanel;
    }

    @Override
    public ApplicationRegistrationModel getData() {
        var data = new ApplicationRegistrationModel();
        data.setDisplayName(displayNameInput.getText());
        data.setCallbackUrls(callbackUrlsInput.getItems());
        data.setDomain(domainInput.getText());
        data.setMultiTenant(multiTenantInput.isSelected());
        data.setClientId(clientIdInput.getText());
        return data;
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        return Arrays.asList(subscriptionBox, displayNameInput, callbackUrlsInput, domainInput, clientIdInput);
    }

    @Override
    public void setData(ApplicationRegistrationModel data) {
        var callbackUrls = data.getCallbackUrls();

        displayNameInput.setText(data.getDisplayName());
        callbackUrlsInput.setUrls(callbackUrls);
        domainInput.setText(data.getDomain());
        multiTenantInput.setSelected(data.isMultiTenant());
        clientIdInput.setText(data.getClientId());
    }

    private void createUIComponents() {
        noteComponent = ComponentsKt.noteComponent(MessageBundle.message("dialog.identity.ad.register_app.description"));
        noteComponent.setBorder(JBUI.Borders.emptyBottom(5));

        subscriptionBox = new SubscriptionComboBox();
        subscriptionBox.setEditable(false);
        subscriptionBox.setRequired(true);

        callbackUrlsInput = new AzureEditableCallbackUrlsCombobox();
        clientIdInput = new AzureClientIdInput();
        advancedSettingsSeparator = new AzureHideableTitledSeparator();
    }

    private void updateRegistrationModel(@Nonnull Project project, @Nonnull Subscription subscription) {
        var title = MessageBundle.message("action.azure.aad.registerApp.loadDefaultDomain");
        AzureTaskManager.getInstance().runInBackground(new AzureTask<>(project, title, false, () -> {
            ProgressManager.getInstance().getProgressIndicator().setIndeterminate(true);

            var client = AzureUtils.createGraphClient(subscription);
            var domain = AzureUtils.loadDomains(client)
                    .stream()
                    .filter(d -> d.isDefault)
                    .map(d -> d.id)
                    .findFirst()
                    .orElse("");

            var model = new ApplicationRegistrationModel();
            model.setDomain(domain);
            model.setCallbackUrls(List.of(ApplicationRegistrationModel.DEFAULT_CALLBACK_URL));

            AzureTaskManager.getInstance().runLater(() -> setData(model));
        }));
    }

    @Nullable
    public Subscription getSubscription() {
        return subscriptionBox.getValue();
    }
}
