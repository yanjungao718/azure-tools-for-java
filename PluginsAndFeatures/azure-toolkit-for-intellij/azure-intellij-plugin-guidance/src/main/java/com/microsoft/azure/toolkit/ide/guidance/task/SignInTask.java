package com.microsoft.azure.toolkit.ide.guidance.task;

import com.microsoft.azure.toolkit.ide.guidance.ComponentContext;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceTask;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azuretools.authmanage.models.AuthMethodDetails;
import com.microsoft.azuretools.sdkmanage.IdentityAzureManager;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Nonnull;

import static com.microsoft.azure.toolkit.lib.auth.model.AuthType.AZURE_CLI;
import static com.microsoft.azure.toolkit.lib.auth.model.AuthType.OAUTH2;

public class SignInTask implements GuidanceTask {

    public static final String SUBSCRIPTION_ID = "subscriptionId";
    private final ComponentContext taskContext;

    public SignInTask(@Nonnull final ComponentContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public void execute() {
        final AzureAccount az = Azure.az(AzureAccount.class);
        if (az.isSignedIn()) {
            // if already signed in, finish directly
            AzureMessager.getMessager().info(AzureString.format("Sign in successfully"));
            return;
        }
        final AuthMethodDetails methodDetails;
        if (isAzureCliAuthenticated()) {
            methodDetails = IdentityAzureManager.getInstance().signInAzureCli().block();
        } else {
            methodDetails = IdentityAzureManager.getInstance().signInOAuth().block();
        }
        if (!az.isSignedIn() || CollectionUtils.isEmpty(az.getSubscriptions())) {
            AzureMessager.getMessager().warning("Failed to sign in or there is no subscription in your account");
        } else {
            AzureMessager.getMessager().info(AzureString.format("Sign in successfully with %s", methodDetails.getAccountEmail()));
        }
    }

    private boolean isAzureCliAuthenticated() {
        return Azure.az(AzureAccount.class).accounts().stream()
                .filter(a -> a.getAuthType() == AZURE_CLI)
                .findFirst()
                .map(account -> {
                    try {
                        return account.checkAvailable().block();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .orElse(false);
    }

    @Nonnull
    @Override
    public String getName() {
        return "task.auth.signin";
    }
}
