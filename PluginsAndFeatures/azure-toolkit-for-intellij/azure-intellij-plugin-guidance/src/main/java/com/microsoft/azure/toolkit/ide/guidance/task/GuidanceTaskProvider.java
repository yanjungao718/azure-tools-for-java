package com.microsoft.azure.toolkit.ide.guidance.task;

import com.microsoft.azure.toolkit.ide.guidance.Phase;
import com.microsoft.azure.toolkit.ide.guidance.Task;

import javax.annotation.Nonnull;

public interface GuidanceTaskProvider {
    Task createTask(@Nonnull final String taskId, @Nonnull final Phase phase);
}
