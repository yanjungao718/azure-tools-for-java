package com.microsoft.azure.toolkit.ide.guidance.view;

import com.intellij.openapi.project.Project;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBFont;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceConfigManager;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceViewManager;
import com.microsoft.azure.toolkit.ide.guidance.config.SequenceConfig;
import com.microsoft.azure.toolkit.ide.guidance.view.components.SequencePanel;
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
        AzureTaskManager.getInstance().runInBackgroundAsObservable("Loading lesson", () -> GuidanceConfigManager.getInstance().loadSequenceConfig())
            .subscribeOn(Schedulers.computation())
            .subscribe(processes -> AzureTaskManager.getInstance().runLater(() -> this.fillProcess(processes)));
    }

    private void fillProcess(final List<SequenceConfig> sequenceConfigs) {
        pnlProcesses.setLayout(new GridLayoutManager(sequenceConfigs.size(), 1));
        for (int i = 0; i < sequenceConfigs.size(); i++) {
            final SequenceConfig sequenceConfig = sequenceConfigs.get(i);
            final SequencePanel sequencePanel = new SequencePanel(sequenceConfig);
            sequencePanel.setStartListener(e -> GuidanceViewManager.getInstance().showGuidance(project, sequenceConfig));
            final GridConstraints gridConstraints = new GridConstraints(i, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0);
            pnlProcesses.add(sequencePanel, gridConstraints);
        }
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    void $$$setupUI$$$() {
    }

    public void setVisible(boolean visible) {
        this.pnlRoot.setVisible(visible);
    }
}
