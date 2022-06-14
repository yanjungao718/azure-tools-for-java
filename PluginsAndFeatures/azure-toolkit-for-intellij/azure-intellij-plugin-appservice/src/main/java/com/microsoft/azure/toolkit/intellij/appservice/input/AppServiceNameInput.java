package com.microsoft.azure.toolkit.intellij.appservice.input;

import com.microsoft.azure.toolkit.ide.guidance.config.InputConfig;
import com.microsoft.azure.toolkit.ide.guidance.input.GuidanceInput;
import com.microsoft.azure.toolkit.ide.guidance.input.InputContext;
import com.microsoft.azure.toolkit.intellij.legacy.appservice.AppNameInput;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.swing.*;

public class AppServiceNameInput implements GuidanceInput {
    public static final String SUBSCRIPTION_ID = "subscriptionId";
    public static final String APP_SERVICE_NAME = "appServiceName";
    private final InputConfig config;
    private final InputContext context;

    private AppNameInput input;

    public AppServiceNameInput(@Nonnull InputConfig config, @Nonnull InputContext context) {
        this.config = config;
        this.context = context;
        initComponent();
    }

    @Override
    public String getDescription() {
        return config.getDescription();
    }

    @Override
    public JComponent getComponent() {
        return input;
    }

    @Override
    public void applyResult() {
        context.applyResult(APP_SERVICE_NAME, input.getValue());
    }

    private void initComponent() {
        input = new AppNameInput();
        updateSubscriptionId(context.getParameter(SUBSCRIPTION_ID));
        context.addPropertyListener(SUBSCRIPTION_ID, this::updateSubscriptionId);
    }

    private void updateSubscriptionId(Object subscriptionId) {
        if (!(subscriptionId instanceof String) || StringUtils.isBlank((String) subscriptionId)) {
            return;
        }
        final Subscription subscription = Azure.az(AzureAccount.class).account().getSubscription((String) subscriptionId);
        input.setSubscription(subscription);
    }
}
