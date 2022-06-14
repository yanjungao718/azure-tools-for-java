package com.microsoft.azure.toolkit.ide.guidance.view.components;

import com.intellij.icons.AllIcons;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.JBUI;
import com.microsoft.azure.toolkit.ide.guidance.Status;
import com.microsoft.azure.toolkit.ide.guidance.Step;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessage;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessager;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.CompoundBorder;

public class StepPanel extends JPanel {
    private JPanel contentPanel;
    private JLabel statusIcon;
    private JLabel titleLabel;
    private JTextPane descPanel;
    private JTextArea outputPanel;
    private HyperlinkLabel actionButton;

    private final Step step;

    public StepPanel(@Nonnull final Step step) {
        super();
        this.step = step;
        $$$setupUI$$$();
        init();
    }

    private void init() {
        this.setLayout(new GridLayoutManager(1, 1));
        this.add(this.contentPanel, new GridConstraints(0, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0));
        this.step.addStatusListener(this::updateStatus);
        this.titleLabel.setText(this.step.getTitle());
        this.actionButton.setHyperlinkText("run");
        this.actionButton.setHyperlinkTarget(null);
        this.actionButton.addHyperlinkListener(e -> {
            this.descPanel.setVisible(false);
            this.outputPanel.setVisible(true);
            this.step.execute();
        });
        this.descPanel.setBorder(null);
        this.descPanel.setText(this.step.getDescription());
        this.descPanel.setVisible(StringUtils.isNotBlank(this.step.getDescription()));
        this.initOutputPanel();
        this.updateStatus(this.step.getStatus());
    }

    private void initOutputPanel() {
        final IAzureMessager messager = new ConsoleTextMessager();
        this.step.setOutput(messager);
        this.outputPanel.setVisible(false);
        final CompoundBorder border = BorderFactory.createCompoundBorder(this.outputPanel.getBorder(), BorderFactory.createEmptyBorder(2, 4, 4, 4));
        this.outputPanel.setBorder(border);
        this.outputPanel.setBackground(JBUI.CurrentTheme.EditorTabs.background());
    }

    class ConsoleTextMessager implements IAzureMessager {
        @Override
        public boolean show(IAzureMessage message) {
            StepPanel.this.outputPanel.setText(message.getContent());
            return true;
        }
    }

    private void updateStatus(Status status) {
        final Icon icon = IconUtil.scale(PhasePanel.getStatusIcon(status), this.statusIcon, 0.875f);
        this.statusIcon.setIcon(icon);
//        this.actionButton.setVisible(status == Status.READY || status == Status.RUNNING);
        this.actionButton.setVisible(true);
        this.actionButton.setEnabled(true);
    }

    void $$$setupUI$$$() {
    }
}
