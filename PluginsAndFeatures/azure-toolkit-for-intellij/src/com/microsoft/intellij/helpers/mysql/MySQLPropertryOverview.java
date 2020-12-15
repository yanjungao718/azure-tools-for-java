package com.microsoft.intellij.helpers.mysql;

import com.microsoft.azure.toolkit.intellij.mysql.MockUtils;
import lombok.Getter;

import javax.swing.*;

public class MySQLPropertryOverview extends JPanel {
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
    private JTextField mysqlVersionTextField;
    @Getter
    private JTextField subscriptionTextField;
    @Getter
    private JTextField performanceConfigurationsTextField;
    @Getter
    private JTextField subscriptionIDTextField;
    @Getter
    private JTextField sslEnforceStatusTextField;

    MySQLPropertryOverview() {
        super();

        disableTxtBoard();
        makeTxtOpaque();
        // mock
        /*
        MockUtils.mockResourceGroup4TextField(resourceGroupTextField);
        MockUtils.mockMySQLServer4TextFiled(serverNameTextField);
        statusTextField.setText("Running");
        serverAdminLoginNameTextField.setText("qianjin@qianjin-mysql-01");
        MockUtils.mockLocaltion4Textfield(locationTextField);
        MockUtils.mockMySQLVersion4TextField(mysqlVersionTextField);
        MockUtils.mockSubscription4TextField(subscriptionTextField);
        subscriptionIDTextField.setText("685ba005-af8d-4b04-8f16-a7bf38b2eb5a");
        sslEnforceStatusTextField.setText("ENABLE");
//        MockUtils.mockDatabase(databaseComboBox);
        performanceConfigurationsTextField.setText("General Purpose, 4 vCore(s), 100 GB");
         */
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        rootPanel.setVisible(aFlag);
    }

    private void disableTxtBoard() {
        resourceGroupTextField.setBorder(BorderFactory.createEmptyBorder());
        serverNameTextField.setBorder(BorderFactory.createEmptyBorder());
        statusTextField.setBorder(BorderFactory.createEmptyBorder());
        serverAdminLoginNameTextField.setBorder(BorderFactory.createEmptyBorder());
        locationTextField.setBorder(BorderFactory.createEmptyBorder());
        mysqlVersionTextField.setBorder(BorderFactory.createEmptyBorder());
        subscriptionTextField.setBorder(BorderFactory.createEmptyBorder());
        performanceConfigurationsTextField.setBorder(BorderFactory.createEmptyBorder());
        subscriptionIDTextField.setBorder(BorderFactory.createEmptyBorder());
        sslEnforceStatusTextField.setBorder(BorderFactory.createEmptyBorder());
    }

    private void makeTxtOpaque() {
        resourceGroupTextField.setBackground(null);
        serverNameTextField.setBackground(null);
        statusTextField.setBackground(null);
        serverAdminLoginNameTextField.setBackground(null);
        locationTextField.setBackground(null);
        mysqlVersionTextField.setBackground(null);
        subscriptionTextField.setBackground(null);
        performanceConfigurationsTextField.setBackground(null);
        subscriptionIDTextField.setBackground(null);
        sslEnforceStatusTextField.setBackground(null);
//        txtNameValue.setBackground(null);
//        txtTypeValue.setBackground(null);
//        txtResGrpValue.setBackground(null);
//        txtSubscriptionValue.setBackground(null);
//        txtRegionValue.setBackground(null);
//        txtHostNameValue.setBackground(null);
//        txtSslPortValue.setBackground(null);
//        txtNonSslPortValue.setBackground(null);
//        txtVersionValue.setBackground(null);
    }
}
