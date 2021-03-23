/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.azuresdk.referencebook;

import com.intellij.icons.AllIcons;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTabbedPane;
import com.microsoft.azure.toolkit.intellij.azuresdk.model.AzureSdkArtifactEntity;
import com.microsoft.azure.toolkit.intellij.azuresdk.model.AzureSdkFeatureEntity;
import com.microsoft.azure.toolkit.intellij.common.SwingUtils;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

public class AzureSdkFeatureDetailPanel {
    private final static String SPRING_TIP = "Spring libraries help you interact with already-provisioned services using native Spring idiomatic expressions.";
    private final static String MANAGEMENT_TIP = "Management libraries help you create, provision and otherwise manage Azure resources.";
    private final static String CLIENT_TIP = "Client libraries help you interact with already-provisioned services.";

    private JBLabel titleLabel;
    private JPanel descPanel;
    @Getter
    private JPanel contentPanel;
    private JBTabbedPane tabPane;
    private HyperlinkLabel featureDocLink;
    private JBLabel descLabel;

    public void setData(final AzureSdkFeatureEntity feature) {
        this.titleLabel.setText(feature.getName());
        SwingUtils.setTextAndEnableAutoWrap(this.descLabel, feature.getDescription());

        this.featureDocLink.setVisible(false);
        Optional.ofNullable(feature.getMsdocs()).ifPresent(link -> {
            this.featureDocLink.setHyperlinkText("Product documentation");
            this.featureDocLink.setHyperlinkTarget(link);
            this.featureDocLink.setVisible(true);
        });

        this.buildTabs(feature);
    }

    private void buildTabs(AzureSdkFeatureEntity feature) {
        final List<AzureSdkArtifactEntity> clientArtifacts = feature.getArtifacts(AzureSdkArtifactEntity.Type.CLIENT);
        final List<AzureSdkArtifactEntity> springArtifacts = feature.getArtifacts(AzureSdkArtifactEntity.Type.SPRING);
        final List<AzureSdkArtifactEntity> managementArtifacts = feature.getArtifacts(AzureSdkArtifactEntity.Type.MANAGEMENT);
        this.tabPane.removeAll();
        if (CollectionUtils.isNotEmpty(clientArtifacts)) {
            final int index = this.addGroup("Client SDK", clientArtifacts);
            this.setTips(index, CLIENT_TIP);
        }
        if (CollectionUtils.isNotEmpty(springArtifacts)) {
            final int index = this.addGroup("Spring", springArtifacts);
            this.setTips(index, SPRING_TIP);
        }
        if (CollectionUtils.isNotEmpty(managementArtifacts)) {
            final int index = this.addGroup("Management SDK", managementArtifacts);
            this.setTips(index, MANAGEMENT_TIP);
        }
    }

    private void setTips(final int index, final String tip) {
        final JLabel tabHeader = (JLabel) this.tabPane.getTabComponentAt(index);
        tabHeader.setIcon(AllIcons.General.ContextHelp);
        tabHeader.setToolTipText(SPRING_TIP);
        tabHeader.setHorizontalTextPosition(SwingConstants.LEFT);
        tabHeader.repaint();
    }

    private int addGroup(final String title, final List<? extends AzureSdkArtifactEntity> clientArtifacts) {
        final int index = this.tabPane.getTabCount();
        final AzureSdkArtifactGroupPanel clientPanel = new AzureSdkArtifactGroupPanel();
        this.tabPane.insertTab(title, null, clientPanel.getContentPanel(), "", index);
        clientPanel.setData(clientArtifacts);
        return index;
    }
}
