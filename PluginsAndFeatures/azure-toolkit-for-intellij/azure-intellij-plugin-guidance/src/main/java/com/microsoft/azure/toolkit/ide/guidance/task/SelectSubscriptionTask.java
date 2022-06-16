package com.microsoft.azure.toolkit.ide.guidance.task;

import com.microsoft.azure.toolkit.ide.guidance.ComponentContext;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceTask;

import javax.annotation.Nonnull;

public class SelectSubscriptionTask implements GuidanceTask {

    public SelectSubscriptionTask(@Nonnull final ComponentContext context) {
    }

    @Override
    public void execute() {

    }

    @Nonnull
    @Override
    public String getName() {
        return "task.auth.select_subscription";
    }

}
