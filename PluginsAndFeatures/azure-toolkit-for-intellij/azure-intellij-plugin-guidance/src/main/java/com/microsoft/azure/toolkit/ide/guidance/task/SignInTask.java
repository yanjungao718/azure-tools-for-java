package com.microsoft.azure.toolkit.ide.guidance.task;

import com.microsoft.azure.toolkit.ide.guidance.ComponentContext;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceTask;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azuretools.sdkmanage.IdentityAzureManager;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nonnull;
import java.util.List;

public class SignInTask implements GuidanceTask {

    public static final String SUBSCRIPTION_ID = "subscriptionId";
    private final ComponentContext taskContext;

    public SignInTask(@Nonnull final ComponentContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public void execute() {
        IdentityAzureManager.getInstance().signInAzureCli().block();
        final AzureAccount az = Azure.az(AzureAccount.class);
        if (!az.isSignedIn() || CollectionUtils.isEmpty(az.getSubscriptions())) {
            AzureMessager.getMessager().warning("Failed to auth with azure cli, please make sure you have already signed in Azure CLI with subscription");
        } else {
            final Subscription subscription = az.getSubscriptions().get(0);
            az.account().selectSubscription(List.of(subscription.getId()));
            taskContext.applyResult(SUBSCRIPTION_ID, subscription.getId());
            AzureMessager.getMessager().info(AzureString.format("Sign in successfully with subscription %s", subscription.getId()));
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return "task.auth.signin";
    }
}
