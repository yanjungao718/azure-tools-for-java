package com.microsoft.azure.toolkit.ide.guidance.view;

import com.intellij.openapi.project.Project;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBFont;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceConfigManager;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceViewManager;
import com.microsoft.azure.toolkit.ide.guidance.config.ProcessConfig;
import com.microsoft.azure.toolkit.ide.guidance.view.components.ProcessPanel;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import rx.schedulers.Schedulers;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.List;

public class WelcomeView {
    private JPanel pnlRoot;
    private JPanel pnlProcesses;
    private JLabel lblTitle;

    private final Project project;

    public WelcomeView(@Nonnull Project project) {
        this.project = project;
        $$$setupUI$$$();
        init();
    }

    private void init() {
        this.lblTitle.setFont(JBFont.h1());
        AzureTaskManager.getInstance().runInBackgroundAsObservable("Loading lesson", () -> GuidanceConfigManager.getInstance().loadProcessConfig())
                .subscribeOn(Schedulers.computation())
                .subscribe(processes -> AzureTaskManager.getInstance().runLater(() -> this.fillProcess(processes)));
    }

    private void fillProcess(final List<ProcessConfig> processConfigs) {
        pnlProcesses.setLayout(new GridLayoutManager(processConfigs.size(), 1));
        for (int i = 0; i < processConfigs.size(); i++) {
            final ProcessConfig processConfig = processConfigs.get(i);
            final ProcessPanel processPanel = new ProcessPanel(processConfig);
            processPanel.setStartListener(e -> GuidanceViewManager.getInstance().showGuidance(project, processConfig));
            final GridConstraints gridConstraints = new GridConstraints(i, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0);
            pnlProcesses.add(processPanel, gridConstraints);
        }
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    void $$$setupUI$$$() {
    }

    public void setVisible(boolean visible) {
        this.pnlRoot.setVisible(visible);
    }
}
