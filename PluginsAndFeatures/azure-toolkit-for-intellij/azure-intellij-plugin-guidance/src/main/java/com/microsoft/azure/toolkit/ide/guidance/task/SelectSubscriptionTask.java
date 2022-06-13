package com.microsoft.azure.toolkit.ide.guidance.task;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.guidance.Context;
import com.microsoft.azure.toolkit.ide.guidance.InputComponent;
import com.microsoft.azure.toolkit.ide.guidance.Task;

public class SelectSubscriptionTask implements Task {

    private final Project project;

    public SelectSubscriptionTask(Project project) {
        this.project = project;
    }

    @Override
    public InputComponent getInput() {
        return null;
    }

    @Override
    public void execute(Context context) {

    }
}
