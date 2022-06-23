package com.microsoft.azure.toolkit.ide.guidance.task;

import com.microsoft.azure.toolkit.ide.guidance.ComponentContext;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceTask;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;

import javax.annotation.Nonnull;
import java.util.List;

public class SelectSubscriptionTask implements GuidanceTask {
    public static final String SUBSCRIPTION_ID = "subscriptionId";

    private final ComponentContext context;

    public SelectSubscriptionTask(@Nonnull final ComponentContext context) {
        this.context = context;
    }

    @Override
    public void execute() {
        final AzureAccount az = Azure.az(AzureAccount.class);
        final Subscription subscription = az.getSubscriptions().get(0);
        az.account().selectSubscription(List.of(subscription.getId()));
        context.applyResult(SUBSCRIPTION_ID, subscription.getId());
        AzureMessager.getMessager().info(AzureString.format("Sign in successfully with subscription %s", subscription.getId()));
    }

    @Nonnull
    @Override
    public String getName() {
        return "task.auth.select_subscription";
    }

}
