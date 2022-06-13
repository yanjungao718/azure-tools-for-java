package com.microsoft.azure.toolkit.ide.guidance.task;

import com.microsoft.azure.toolkit.ide.guidance.GuidanceTask;

import javax.annotation.Nonnull;

public class CleanUpResourceTask implements GuidanceTask {
    private final TaskContext taskContext;

    public CleanUpResourceTask(@Nonnull final TaskContext context) {
        this.taskContext = context;
    }

    @Override
    public void execute() {

    }
}
