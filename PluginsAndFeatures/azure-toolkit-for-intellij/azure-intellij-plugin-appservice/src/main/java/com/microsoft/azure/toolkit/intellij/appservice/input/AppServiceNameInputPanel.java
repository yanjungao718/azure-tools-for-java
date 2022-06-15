package com.microsoft.azure.toolkit.intellij.appservice.input;

import com.microsoft.azure.toolkit.intellij.legacy.appservice.AppNameInput;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.swing.*;

public class AppServiceNameInputPanel {
    @Getter
    private JPanel rootPanel;
    private AppNameInput appNameInput;

    public AppServiceNameInputPanel() {
        $$$setupUI$$$();
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    void $$$setupUI$$$() {
    }

    public String getValue() {
        return appNameInput.getValue();
    }

    public void setValue(final String value) {
        this.appNameInput.setValue(value);
    }

    public void setSubscriptionId(@Nonnull final String subscriptionId) {
        final Subscription subscription = Azure.az(AzureAccount.class).account().getSubscription(subscriptionId);
        this.appNameInput.setSubscription(subscription);
    }

    public AzureValidationInfo getValidationInfo() {
        return this.appNameInput.getValidationInfo();
    }
}
