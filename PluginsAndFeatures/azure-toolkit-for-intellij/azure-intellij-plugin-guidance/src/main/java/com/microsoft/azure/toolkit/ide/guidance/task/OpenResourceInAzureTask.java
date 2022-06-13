package com.microsoft.azure.toolkit.ide.guidance.task;

import com.microsoft.azure.toolkit.ide.guidance.GuidanceTask;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.webapp.AzureWebApp;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;

import javax.annotation.Nonnull;

import static com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor.OPEN_URL;
import static com.microsoft.azure.toolkit.ide.guidance.task.create.webapp.CreateWebAppTask.RESOURCE_ID;

public class OpenResourceInAzureTask implements GuidanceTask {
    private final TaskContext taskContext;

    public OpenResourceInAzureTask(@Nonnull TaskContext taskContext) {
        this.taskContext = taskContext;
    }

    @Override
    public void execute() {
        final String id = (String) taskContext.getParameter(RESOURCE_ID);
        final String url = "https://" + Azure.az(AzureWebApp.class).webApp(id).getHostName();
//        final ResourceId resourceId = ResourceId.fromString(id);
//        final IAccount account = Azure.az(IAzureAccount.class).account();
//        final Subscription subscription = account.getSubscription(resourceId.subscriptionId());
//        final String url = String.format("%s/#@%s/resource%s", account.portalUrl(), subscription.getTenantId(), id);
        AzureActionManager.getInstance().getAction(OPEN_URL).handle(url);
    }
}
