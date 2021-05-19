/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.sqlserver.properties;

import lombok.Getter;

import javax.swing.*;

public class SqlServerPropertryOverviewPanel extends JPanel {
    private JPanel rootPanel;
    @Getter
    private JTextField resourceGroupTextField;
    @Getter
    private JTextField serverNameTextField;
    @Getter
    private JTextField statusTextField;
    @Getter
    private JTextField serverAdminLoginNameTextField;
    @Getter
    private JTextField locationTextField;
    @Getter
    private JTextField versionTextField;
    @Getter
    private JTextField subscriptionTextField;
    @Getter
    private JTextField subscriptionIDTextField;

    SqlServerPropertryOverviewPanel() {
        super();
        disableTxtBoard();
        makeTxtOpaque();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        rootPanel.setVisible(visible);
    }

    private void disableTxtBoard() {
        resourceGroupTextField.setBorder(BorderFactory.createEmptyBorder());
        serverNameTextField.setBorder(BorderFactory.createEmptyBorder());
        statusTextField.setBorder(BorderFactory.createEmptyBorder());
        serverAdminLoginNameTextField.setBorder(BorderFactory.createEmptyBorder());
        locationTextField.setBorder(BorderFactory.createEmptyBorder());
        versionTextField.setBorder(BorderFactory.createEmptyBorder());
        subscriptionTextField.setBorder(BorderFactory.createEmptyBorder());
        subscriptionIDTextField.setBorder(BorderFactory.createEmptyBorder());
    }

    private void makeTxtOpaque() {
        resourceGroupTextField.setBackground(null);
        serverNameTextField.setBackground(null);
        statusTextField.setBackground(null);
        serverAdminLoginNameTextField.setBackground(null);
        locationTextField.setBackground(null);
        versionTextField.setBackground(null);
        subscriptionTextField.setBackground(null);
        subscriptionIDTextField.setBackground(null);
    }
}
