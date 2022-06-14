package com.microsoft.azure.toolkit.ide.guidance.task.create.webapp;

import com.microsoft.azure.toolkit.ide.appservice.webapp.model.WebAppConfig;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceTask;
import com.microsoft.azure.toolkit.ide.guidance.task.SignInTask;
import com.microsoft.azure.toolkit.ide.guidance.task.TaskContext;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.model.Runtime;
import com.microsoft.azure.toolkit.lib.appservice.webapp.WebApp;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.legacy.webapp.WebAppService;

import javax.annotation.Nonnull;
import java.util.Optional;

public class CreateWebAppTask implements GuidanceTask {
    public static final String WEBAPP_NAME = "webappName";
    public static final String WEBAPP_ID = "webappId";
    public static final String RESOURCE_ID = "webappId";

    private final TaskContext context;

    public CreateWebAppTask(@Nonnull final TaskContext context) {
        this.context = context;
    }

    @Override
    public void execute() throws Exception {
        final String name = (String) context.getParameter(WEBAPP_NAME);
        final Subscription subscription = Optional.ofNullable((String) context.getParameter(SignInTask.SUBSCRIPTION_ID))
                .map(id -> Azure.az(AzureAccount.class).account().getSubscription(id))
                .orElseGet(() -> Azure.az(AzureAccount.class).getSubscriptions().get(0));
        final WebAppConfig webAppConfig = WebAppConfig.getWebAppDefaultConfig(name);
        webAppConfig.setName(name);
        webAppConfig.setSubscription(subscription);
        webAppConfig.setRuntime(Runtime.LINUX_JAVA11);
        final WebApp webApp = WebAppService.getInstance().createWebApp(webAppConfig);
        context.applyResult(WEBAPP_ID, webApp.getId());
        context.applyResult(RESOURCE_ID, webApp.getId());
    }
}
