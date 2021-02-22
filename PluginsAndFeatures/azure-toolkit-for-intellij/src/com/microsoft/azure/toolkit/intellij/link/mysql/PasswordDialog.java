/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.link.mysql;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.AzureDialog;
import com.microsoft.azure.toolkit.intellij.link.JdbcUrlUtils;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azuretools.azurecommons.util.Utils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.List;

public class PasswordDialog extends AzureDialog<PasswordConfig> implements AzureForm<PasswordConfig> {

    private static final String TITLE = "Credential for Azure Database for MySQL";
    private static final String HEADER_PATTERN = "Please provide credential for user (%s) to access database (%s) on server (%s).";

    private JPanel root;
    private JLabel headerIconLabel;
    private JTextPane headerTextPane;
    private JTextPane testResultTextPane;
    private JLabel testResultLabel;
    private JButton testConnectionButton;
    private JButton testResultButton;
    private JPasswordField inputPasswordField;
    private PasswordSaveComboBox passwordSaveComboBox;

    private Project project;
    private String username;
    private String url;

    public PasswordDialog(Project project, String username, String url) {
        super(project);
        this.project = project;
        this.username = username;
        this.url = url;
        setTitle(TITLE);
        headerTextPane.setText(String.format(HEADER_PATTERN, username, JdbcUrlUtils.parseDatabase(url), JdbcUrlUtils.parseHostname(url)));
        testConnectionButton.setEnabled(false);
        testResultLabel.setVisible(false);
        testResultButton.setVisible(false);
        testResultTextPane.setEditable(false);
        testResultTextPane.setText(StringUtils.EMPTY);
        Dimension lastColumnSize = new Dimension(106, 38);
        passwordSaveComboBox.setPreferredSize(lastColumnSize);
        passwordSaveComboBox.setMaximumSize(lastColumnSize);
        passwordSaveComboBox.setSize(lastColumnSize);
        this.init();
        this.initListener();
    }

    private void initListener() {
        this.inputPasswordField.addKeyListener(this.onInputPasswordFieldChanged());
        this.testConnectionButton.addActionListener(this::onTestConnectionButtonClicked);
        this.testResultButton.addActionListener(this::onCopyButtonClicked);

    }

    private KeyListener onInputPasswordFieldChanged() {
        KeyListener listener = new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (ArrayUtils.isNotEmpty(inputPasswordField.getPassword())) {
                    testConnectionButton.setEnabled(true);
                } else {
                    testConnectionButton.setEnabled(false);
                }
            }

        };
        return listener;
    }

    private void onTestConnectionButtonClicked(ActionEvent e) {
        testConnectionButton.setEnabled(false);
        String password = String.valueOf(inputPasswordField.getPassword());
        TestConnectionUtils.testConnection(url, username, password, testResultLabel,
                testResultButton, testResultTextPane);
        testConnectionButton.setEnabled(true);
    }

    private void onCopyButtonClicked(ActionEvent e) {
        try {
            Utils.copyToSystemClipboard(testResultTextPane.getText());
        } catch (Exception exception) {
            String error = "copy test result error";
            String action = "try again later.";
            throw new AzureToolkitRuntimeException(error, action);
        }
    }

    @Override
    public AzureForm<PasswordConfig> getForm() {
        return this;
    }

    @Override
    protected String getDialogTitle() {
        return TITLE;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return root;
    }

    @Override
    public PasswordConfig getData() {
        PasswordConfig config = PasswordConfig.getDefaultConfig();
        config.setPasswordSaveType(passwordSaveComboBox.getValue());
        config.setPassword(inputPasswordField.getPassword());
        return config;
    }

    @Override
    public void setData(PasswordConfig data) {
        // TODO
        passwordSaveComboBox.setValue(data.getPasswordSaveType());
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        final AzureFormInput<?>[] inputs = {this.passwordSaveComboBox};
        return Arrays.asList(inputs);
    }

}
