/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.guidance.view.components;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.GraphicsConfig;
import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.JBColor;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.Consumer;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.JBUI;
import com.microsoft.azure.toolkit.ide.guidance.Phase;
import com.microsoft.azure.toolkit.ide.guidance.Status;
import com.microsoft.azure.toolkit.ide.guidance.Step;
import com.microsoft.azure.toolkit.ide.guidance.input.GuidanceInput;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessage;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

public class PhasePanel extends JPanel {
    // JBUI.CurrentTheme.Tree.Hover.background(true)
    // com.intellij.notification.impl.NotificationComponent.NEW_COLOR
    private static final JBColor BACKGROUND_COLOR = JBColor.namedColor("NotificationsToolwindow.newNotification.background", new JBColor(15134455, 4540746));
    private final Phase phase;
    private JPanel contentPanel;
    private JButton actionButton;
    private JLabel toggleIcon;
    private JLabel statusIcon;
    private JLabel titleLabel;
    private JPanel detailsPanel;
    private JTextPane descPanel;
    private JTextPane outputPanel;
    private JPanel inputsPanel;
    private JPanel stepsPanel;
    private JSeparator detailsSeparator;
    private boolean focused;

    public PhasePanel(@Nonnull Phase phase) {
        super();
        this.phase = phase;
        $$$setupUI$$$();
        init();
    }

    private void init() {
        final GridLayoutManager layout = new GridLayoutManager(1, 1);
        final Insets margin = JBUI.insets(8, 8, 10, 8);
        layout.setMargin(margin);
        this.setLayout(layout);
        this.add(this.contentPanel, new GridConstraints(0, 0, 1, 1, 0, GridConstraints.ALIGN_FILL, 3, 3, null, null, null, 0));

        this.phase.addStatusListener(this::updateStatus);
        //https://stackoverflow.com/questions/7115065/jlabel-vertical-alignment-not-working-as-expected
        this.titleLabel.setBorder(BorderFactory.createEmptyBorder(-2 /*top*/, 0, 0, 0));
        this.titleLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        final Icon icon = IconUtil.scale(AllIcons.Actions.Execute, this.actionButton, 0.75f);
        this.actionButton.setIcon(AllIcons.Actions.Execute);
        this.actionButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.actionButton.addActionListener(e -> this.phase.execute(true));
        this.toggleIcon.setIcon(AllIcons.Actions.FindAndShowNextMatches);
        this.toggleIcon.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        final MouseAdapter listener = toggleDetails();
        this.toggleIcon.addMouseListener(listener);
        this.titleLabel.addMouseListener(listener);
        this.descPanel.setBorder(null);
        this.descPanel.setVisible(StringUtils.isNotBlank(this.phase.getDescription()));
        this.initOutputPanel();
        this.detailsPanel.setVisible(false);
        this.initInputsPanel();
        this.initStepsPanel();
        this.renderDescription();
        this.toggleIcon.setVisible(this.inputsPanel.isVisible() || this.stepsPanel.isVisible());
        this.updateStatus(this.phase.getStatus());
        this.phase.getContext().addContextListener(ignore -> this.renderDescription());
    }

    private void initInputsPanel() {
        final List<GuidanceInput<?>> inputs = phase.getInputs();
        if (CollectionUtils.isEmpty(inputs)) {
            this.inputsPanel.setVisible(false);
            return;
        }
        final GridLayoutManager layout = new GridLayoutManager(inputs.size(), 1);
        this.inputsPanel.setLayout(layout);
        for (int i = 0; i < inputs.size(); i++) {
            final GuidanceInput<?> component = inputs.get(i);
            final GridConstraints gridConstraints = new GridConstraints(i, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0);
            this.inputsPanel.add(component.getComponent().getContentPanel(), gridConstraints);
        }
    }

    private void renderDescription() {
        titleLabel.setText(phase.getRenderedTitle());
        descPanel.setText(phase.getRenderedDescription());
    }

    private void initOutputPanel() {
        final IAzureMessager messager = new ConsoleTextMessager();
        this.phase.setOutput(messager);
        this.outputPanel.setBorder(null);
        this.outputPanel.setVisible(false);
    }

    class ConsoleTextMessager implements IAzureMessager {
        @Override
        public boolean show(IAzureMessage message) {
            PhasePanel.this.outputPanel.setText(message.getContent());
            return true;
        }
    }

    private void updateStatus(Status status) {
        this.statusIcon.setIcon(getStatusIcon(status));
        this.descPanel.setVisible(StringUtils.isNotBlank(this.descPanel.getText()) && (status == Status.INITIAL || status == Status.READY));
        this.outputPanel.setVisible(status == Status.RUNNING || (StringUtils.isNotBlank(this.outputPanel.getText()) && (status == Status.SUCCEED || status == Status.FAILED)));
        this.focused = status == Status.READY || status == Status.RUNNING || status == Status.FAILED;
        this.actionButton.setEnabled(status == Status.READY || status == Status.FAILED);
        this.actionButton.setVisible(this.focused);
        final Color bgColor = this.focused ? BACKGROUND_COLOR : JBUI.CurrentTheme.ToolWindow.background();
        doForOffsprings(this.contentPanel, c -> c.setBackground(bgColor));
        doForOffsprings(this.inputsPanel, c -> c.setEnabled(status != Status.RUNNING && status != Status.SUCCEED));
        if (status == Status.FAILED) {
            this.actionButton.setText("Retry");
            this.toggleDetails(true);
        }
    }

    protected void paintComponent(@NotNull Graphics g) {
        super.paintComponent(g);
        if (this.focused) {
            final Graphics2D graphics = (Graphics2D) g;
            final GraphicsConfig config = GraphicsUtil.setupAAPainting(g);
            g.setColor(BACKGROUND_COLOR);
            g.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 10, 10);
            config.restore();
        }
    }

    private void initStepsPanel() {
        final List<Step> steps = phase.getSteps();
        if (CollectionUtils.isEmpty(steps) || steps.size() == 1) {
            // skip render steps panel for single task
            this.stepsPanel.setVisible(false);
            return;
        }
        this.stepsPanel.setLayout(new GridLayoutManager(steps.size(), 1));
        for (int i = 0; i < steps.size(); i++) {
            final Step step = steps.get(i);
            final JPanel stepPanel = new StepPanel(step);
            final GridConstraints gridConstraints = new GridConstraints(i, 0, 1, 1, 0, 3, 3, 3, null, null, null, 0);
            this.stepsPanel.add(stepPanel, gridConstraints);
        }
    }

    @Nonnull
    private MouseAdapter toggleDetails() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                final boolean expanded = PhasePanel.this.toggleIcon.getIcon() == AllIcons.Actions.FindAndShowPrevMatches;
                toggleDetails(!expanded);
            }
        };
    }

    private void toggleDetails(boolean expanded) {
        this.toggleIcon.setIcon(expanded ? AllIcons.Actions.FindAndShowPrevMatches : AllIcons.Actions.FindAndShowNextMatches);
        this.detailsPanel.setVisible(expanded);
        this.detailsSeparator.setVisible(expanded && this.stepsPanel.isVisible() && this.actionButton.isVisible());
    }

    static void doForOffsprings(JComponent c, Consumer<Component> func) {
        func.consume(c);
        Arrays.stream(c.getComponents()).filter(component -> component instanceof JPanel).forEach(child -> doForOffsprings((JComponent) child, func));
        Arrays.stream(c.getComponents()).filter(component -> component instanceof JTextPane || component instanceof JButton).forEach(func::consume);
    }

    @Nonnull
    static Icon getStatusIcon(final Status status) {
        if (status == Status.RUNNING) {
            return AnimatedIcon.Default.INSTANCE;
        } else if (status == Status.SUCCEED) {
            return AllIcons.RunConfigurations.ToolbarPassed;
        } else if (status == Status.FAILED) {
            return AllIcons.RunConfigurations.ToolbarError;
        } else {
            return IconUtil.resizeSquared(AllIcons.Debugger.Db_muted_disabled_breakpoint, 14);
        }
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    void $$$setupUI$$$() {
    }
}
