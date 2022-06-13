package com.microsoft.azure.toolkit.ide.guidance;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.microsoft.azure.toolkit.ide.guidance.config.SequenceConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class GuidanceStartupListener implements StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        final SequenceConfig sequenceConfigFromWorkspace = GuidanceConfigManager.getInstance().getProcessConfigFromWorkspace(project);
        Optional.ofNullable(sequenceConfigFromWorkspace)
            .ifPresent(config -> GuidanceViewManager.getInstance().showGuidance(project, sequenceConfigFromWorkspace));
    }
}
