package com.microsoft.azure.toolkit.ide.guidance.task;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.guidance.Context;
import com.microsoft.azure.toolkit.ide.guidance.InputComponent;
import com.microsoft.azure.toolkit.ide.guidance.Task;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azuretools.sdkmanage.IdentityAzureManager;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;

public class SignInTask implements Task {

    public static final String SUBSCRIPTION_ID = "subscriptionId";
    private Project project;

    public SignInTask(Project project) {
        this.project = project;
    }

    @Override
    public InputComponent getInput() {
        return null;
    }

    @Override
    public void execute(Context context) {
        IdentityAzureManager.getInstance().signInAzureCli().block();
        final AzureAccount az = Azure.az(AzureAccount.class);
        if (!az.isSignedIn() || CollectionUtils.isEmpty(az.getSubscriptions())) {
            AzureMessager.getMessager().warning("Failed to auth with azure cli, please make sure you have already signed in Azure CLI with subscription");
        } else {
            final Subscription subscription = az.getSubscriptions().get(0);
            az.account().selectSubscription(Arrays.asList(subscription.getId()));
            context.setProperty(SUBSCRIPTION_ID, subscription.getId());
            AzureMessager.getMessager().info(AzureString.format("Sign in successfully with subscription %s", subscription.getId()));
        }
    }
}
