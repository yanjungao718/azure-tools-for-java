/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.webapp.runner.webappconfig.slimui;

import com.intellij.icons.AllIcons;
import com.intellij.ide.IdeTooltipManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.HideableDecorator;
import com.intellij.ui.HyperlinkLabel;
import com.microsoft.azure.toolkit.ide.appservice.model.AzureArtifactConfig;
import com.microsoft.azure.toolkit.ide.appservice.webapp.model.WebAppConfig;
import com.microsoft.azure.toolkit.ide.appservice.webapp.model.WebAppDeployRunConfigurationModel;
import com.microsoft.azure.toolkit.intellij.common.AzureArtifact;
import com.microsoft.azure.toolkit.intellij.common.AzureArtifactComboBox;
import com.microsoft.azure.toolkit.intellij.common.AzureArtifactManager;
import com.microsoft.azure.toolkit.intellij.common.AzureArtifactType;
import com.microsoft.azure.toolkit.intellij.common.AzureFormPanel;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.AzureWebApp;
import com.microsoft.azure.toolkit.lib.appservice.model.WebContainer;
import com.microsoft.azure.toolkit.lib.appservice.service.IWebAppDeploymentSlot;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.intellij.ui.util.UIUtils;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenConstants;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.microsoft.azuretools.core.mvp.model.webapp.AzureWebAppMvpModel.DO_NOT_CLONE_SLOT_CONFIGURATION;
import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class WebAppDeployConfigurationPanel extends JPanel implements AzureFormPanel<WebAppDeployRunConfigurationModel> {
    private static final String[] FILE_NAME_EXT = {"war", "jar", "ear"};
    private static final String DEPLOYMENT_SLOT = "&Deployment Slot";
    private static final String DEFAULT_SLOT_NAME = "slot-%s";

    private JPanel pnlSlotCheckBox;
    private JTextField txtNewSlotName;
    private JComboBox cbxSlotConfigurationSource;
    private JCheckBox chkDeployToSlot;
    private JCheckBox chkToRoot;
    private JPanel pnlRoot;
    private JPanel pnlSlotDetails;
    private JRadioButton rbtNewSlot;
    private JRadioButton rbtExistingSlot;
    private JComboBox cbxSlotName;
    private JPanel pnlSlot;
    private JPanel pnlSlotHolder;
    private JPanel pnlCheckBox;
    private JPanel pnlSlotRadio;
    private JLabel lblSlotName;
    private JLabel lblSlotConfiguration;
    private JCheckBox chkOpenBrowser;
    private HyperlinkLabel lblNewSlot;
    private JPanel pnlExistingSlot;
    private JButton btnSlotHover;
    private AzureArtifactComboBox comboBoxArtifact;
    private JLabel lblArtifact;
    private JLabel lblWebApp;
    private WebAppComboBox comboBoxWebApp;
    private final HideableDecorator slotDecorator;

    private final Project project;

    public WebAppDeployConfigurationPanel(@NotNull Project project) {
        super();
        this.project = project;

        final ButtonGroup slotButtonGroup = new ButtonGroup();
        slotButtonGroup.add(rbtNewSlot);
        slotButtonGroup.add(rbtExistingSlot);
        rbtExistingSlot.addActionListener(e -> toggleSlotType(true));
        rbtNewSlot.addActionListener(e -> toggleSlotType(false));

        chkDeployToSlot.addActionListener(e -> toggleSlotPanel(chkDeployToSlot.isSelected()));

        final Icon informationIcon = AllIcons.General.ContextHelp;
        btnSlotHover.setIcon(informationIcon);
        btnSlotHover.setHorizontalAlignment(SwingConstants.CENTER);
        btnSlotHover.setPreferredSize(new Dimension(informationIcon.getIconWidth(), informationIcon.getIconHeight()));
        btnSlotHover.setToolTipText(message("webapp.deploy.hint.deploymentSlot"));
        btnSlotHover.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                btnSlotHover.setBorderPainted(true);
                final MouseEvent phantom = new MouseEvent(btnSlotHover, MouseEvent.MOUSE_ENTERED,
                                                          System.currentTimeMillis(), 0, 10, 10, 0, false);
                DefaultLoader.getIdeHelper().invokeLater(() -> IdeTooltipManager.getInstance().eventDispatched(phantom));
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                btnSlotHover.setBorderPainted(false);
                IdeTooltipManager.getInstance().dispose();
            }
        });

        final JLabel labelForNewSlotName = new JLabel("Slot Name");
        labelForNewSlotName.setLabelFor(txtNewSlotName);
        final JLabel labelForExistingSlotName = new JLabel("Slot Name");
        labelForExistingSlotName.setLabelFor(cbxSlotName);

        lblArtifact.setLabelFor(comboBoxArtifact);
        lblWebApp.setLabelFor(comboBoxWebApp);

        slotDecorator = new HideableDecorator(pnlSlotHolder, DEPLOYMENT_SLOT, true);
        slotDecorator.setContentComponent(pnlSlot);
    }

    public void toggleSlotVisibility(final boolean visible) {
        slotDecorator.setOn(visible);
    }

    private void setComboBoxDefaultValue(JComboBox comboBox, Object value) {
        UIUtils.listComboBoxItems(comboBox).stream().filter(item -> item.equals(value)).findFirst().ifPresent(defaultItem -> comboBox.setSelectedItem(value));
    }

    private WebAppConfig getSelectedWebApp() {
        return comboBoxWebApp.getValue();
    }

    private boolean isAbleToDeployToRoot(final AzureArtifact azureArtifact) {
        final WebAppConfig selectedWebApp = getSelectedWebApp();
        if (selectedWebApp == null || azureArtifact == null) {
            return false;
        }
        final WebContainer webContainer = selectedWebApp.getRuntime().getWebContainer();
        final String packaging = AzureArtifactManager.getInstance(project).getPackaging(azureArtifact);
        final boolean isDeployingWar = StringUtils.equalsAnyIgnoreCase(packaging, MavenConstants.TYPE_WAR, "ear");
        return isDeployingWar && StringUtils.containsAnyIgnoreCase(webContainer.getValue(), "tomcat", "jboss");
    }

    private void toggleSlotPanel(boolean slot) {
        final boolean isDeployToSlot = slot && (getSelectedWebApp() != null);
        rbtNewSlot.setEnabled(isDeployToSlot);
        rbtExistingSlot.setEnabled(isDeployToSlot);
        lblSlotName.setEnabled(isDeployToSlot);
        lblSlotConfiguration.setEnabled(isDeployToSlot);
        cbxSlotName.setEnabled(isDeployToSlot);
        txtNewSlotName.setEnabled(isDeployToSlot);
        cbxSlotConfigurationSource.setEnabled(isDeployToSlot);
    }

    private void toggleSlotType(final boolean isExistingSlot) {
        pnlExistingSlot.setVisible(isExistingSlot);
        pnlExistingSlot.setEnabled(isExistingSlot);
        txtNewSlotName.setVisible(!isExistingSlot);
        txtNewSlotName.setEnabled(!isExistingSlot);
        lblSlotConfiguration.setVisible(!isExistingSlot);
        cbxSlotConfigurationSource.setVisible(!isExistingSlot);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        lblNewSlot = new HyperlinkLabel(message("webapp.deploy.noDeploymentSlot"));
        lblNewSlot.addHyperlinkListener(e -> rbtNewSlot.doClick());

        comboBoxWebApp = new WebAppComboBox(project);
        comboBoxWebApp.addItemListener(e -> loadDeploymentSlot(getSelectedWebApp()));
        comboBoxWebApp.refreshItems();

        comboBoxArtifact = new AzureArtifactComboBox(this.project);
        comboBoxArtifact.setFileFilter(virtualFile -> {
            final String ext = FileNameUtils.getExtension(virtualFile.getPath());
            return ArrayUtils.contains(FILE_NAME_EXT, ext);
        });
        comboBoxArtifact.refreshItems();
    }

    private void loadDeploymentSlot(WebAppConfig selectedWebApp) {
        if (selectedWebApp == null) {
            return;
        }
        if (StringUtils.isEmpty(selectedWebApp.getResourceId())) {
            chkDeployToSlot.setEnabled(false);
            chkDeployToSlot.setSelected(false);
        } else {
            chkDeployToSlot.setEnabled(true);
            Mono.fromCallable(() -> Azure.az(AzureWebApp.class).get(selectedWebApp.getResourceId()).deploymentSlots())
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(slots -> fillDeploymentSlots(slots, selectedWebApp));
        }
    }

    private synchronized void fillDeploymentSlots(List<IWebAppDeploymentSlot> slotList, @NotNull final WebAppConfig selectedWebApp) {
        final String defaultSlot = (String) cbxSlotName.getSelectedItem();
        final String defaultConfigurationSource = (String) cbxSlotConfigurationSource.getSelectedItem();
        cbxSlotName.removeAllItems();
        cbxSlotConfigurationSource.removeAllItems();
        cbxSlotConfigurationSource.addItem(DO_NOT_CLONE_SLOT_CONFIGURATION);
        cbxSlotConfigurationSource.addItem(selectedWebApp.getName());
        slotList.stream().filter(Objects::nonNull).forEach(slot -> {
            cbxSlotName.addItem(slot.name());
            cbxSlotConfigurationSource.addItem(slot.name());
        });
        setComboBoxDefaultValue(cbxSlotName, defaultSlot);
        setComboBoxDefaultValue(cbxSlotConfigurationSource, defaultConfigurationSource);
        final boolean existDeploymentSlot = slotList.size() > 0;
        lblNewSlot.setVisible(!existDeploymentSlot);
        cbxSlotName.setVisible(existDeploymentSlot);
    }

    @Override
    public void setData(WebAppDeployRunConfigurationModel data) {
        // artifact
        Optional.ofNullable(data.getArtifactConfig()).map(config -> AzureArtifactManager.getInstance(this.project)
                .getAzureArtifactById(AzureArtifactType.valueOf(config.getArtifactType()), config.getArtifactIdentifier()))
                .ifPresent(artifact -> comboBoxArtifact.setArtifact(artifact));
        // web app
        Optional.ofNullable(data.getWebAppConfig()).ifPresent(webApp -> comboBoxWebApp.setValue(webApp));
        // configuration
        chkToRoot.setSelected(data.isDeployToRoot());
        chkOpenBrowser.setSelected(data.isOpenBrowserAfterDeployment());
    }

    @Override
    public WebAppDeployRunConfigurationModel getData() {
        final AzureArtifact artifact = comboBoxArtifact.getValue();
        final AzureArtifactConfig artifactConfig = AzureArtifactConfig.builder().artifactType(artifact.getType().name())
                .artifactIdentifier(AzureArtifactManager.getInstance(project).getArtifactIdentifier(artifact)).build();
        return WebAppDeployRunConfigurationModel.builder()
                .webAppConfig(comboBoxWebApp.getValue())
                .artifactConfig(artifactConfig)
                .openBrowserAfterDeployment(chkOpenBrowser.isSelected())
                .deployToRoot(chkToRoot.isSelected())
                .build();
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        return Arrays.asList(comboBoxWebApp, comboBoxArtifact);
    }
}
