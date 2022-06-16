package com.microsoft.azure.toolkit.intellij.appservice;

import com.microsoft.azure.toolkit.ide.guidance.ComponentContext;
import com.microsoft.azure.toolkit.ide.guidance.Context;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceTask;
import com.microsoft.azure.toolkit.ide.guidance.config.TaskConfig;
import com.microsoft.azure.toolkit.ide.guidance.task.GuidanceTaskProvider;
import com.microsoft.azure.toolkit.intellij.appservice.task.CreateWebAppTask;
import com.microsoft.azure.toolkit.intellij.appservice.task.DeployWebAppTask;
import com.microsoft.azure.toolkit.intellij.appservice.task.OpenInBrowserTask;

import javax.annotation.Nonnull;

public class AppServiceTaskProvider implements GuidanceTaskProvider {
    @Override
    public GuidanceTask createTask(@Nonnull TaskConfig config, @Nonnull Context context) {
        final ComponentContext taskContext = new ComponentContext(config, context);
        switch (config.getName()) {
            case "task.webapp.create":
                return new CreateWebAppTask(taskContext);
            case "task.webapp.deploy":
                return new DeployWebAppTask(taskContext);
            case "task.webapp.open_in_browser":
                return new OpenInBrowserTask(taskContext);
            default:
                return null;
        }
    }
}
