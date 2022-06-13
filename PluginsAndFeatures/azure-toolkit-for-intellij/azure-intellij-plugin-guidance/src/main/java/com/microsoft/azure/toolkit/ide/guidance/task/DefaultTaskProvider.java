package com.microsoft.azure.toolkit.ide.guidance.task;

import com.microsoft.azure.toolkit.ide.guidance.Phase;
import com.microsoft.azure.toolkit.ide.guidance.Task;
import com.microsoft.azure.toolkit.ide.guidance.task.clone.GitCloneTask;
import com.microsoft.azure.toolkit.ide.guidance.task.create.webapp.CreateWebAppTask;
import com.microsoft.azure.toolkit.ide.guidance.task.deploy.DeployWebAppTask;

import javax.annotation.Nonnull;

public class DefaultTaskProvider implements GuidanceTaskProvider {
    @Override
    public Task createTask(@Nonnull String taskId, @Nonnull Phase phase) {
        switch (taskId) {
            case "tasks.clone":
                return new GitCloneTask(phase.getGuidance());
            case "task.signin":
                return new SignInTask(phase.getGuidance().getProject());
            case "task.select_subscription":
                return new SelectSubscriptionTask(phase.getGuidance().getProject());
            case "task.webapp.create":
                return new CreateWebAppTask();
            case "task.webapp.deploy":
                return new DeployWebAppTask(phase.getGuidance());
            case "task.resource.open_in_portal":
                return new OpenResourceInAzureAction();
            case "task.resource.clean_up":
                return new CleanUpResourceTask();
            default:
                return new EmptyTask();
        }
    }
}
