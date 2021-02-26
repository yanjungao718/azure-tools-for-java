/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.link.mysql;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.management.mysql.v2020_01_01.Server;
import com.microsoft.azure.management.mysql.v2020_01_01.implementation.DatabaseInner;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.toolkit.intellij.appservice.subscription.SubscriptionComboBox;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.intellij.common.AzureFormPanel;
import com.microsoft.azure.toolkit.intellij.common.ModuleComboBox;
import com.microsoft.azure.toolkit.intellij.link.LinkComposite;
import com.microsoft.azure.toolkit.intellij.link.ModuleLinkConfig;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azuretools.azurecommons.util.Utils;
import com.microsoft.intellij.ui.components.AzureWizardStep;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Getter
@Setter
public class BasicLinkMySQLPanel<T extends LinkComposite<MySQLLinkConfig, ModuleLinkConfig>> extends JPanel implements AzureFormPanel<T> {
    private SubscriptionComboBox subscriptionComboBox;
    private ServerComboBox serverComboBox;
    private DatabaseComboBox databaseComboBox;
    private UsernameComboBox usernameComboBox;
    private JPasswordField inputPasswordField;
    private JPanel rootPanel;
    private JTextField envPrefixTextField;
    private PasswordSaveComboBox passwordSaveComboBox;
    private JTextField urlTextField;
    private JButton testConnectionButton;
    private ModuleComboBox moduleComboBox;
    private JTextPane testResultTextPane;
    private JButton testResultButton;
    private JLabel testResultLabel;
    private JTextField portTextField;

    private final Supplier<T> supplier;
    private AzureWizardStep wizardStep;
    private Project project;

    private String urlHead = "jdbc:mysql://";
    private String urlMiddle = ":3306/";
    private String urlTail = "?serverTimezone=UTC&useSSL=true&requireSSL=false";

    public BasicLinkMySQLPanel(Project project, final Supplier<T> supplier) {
        super();
        this.project = project;
        this.supplier = supplier;
        init();
        initListeners();
    }

    private void init() {
        Dimension lastColumnSize = new Dimension(106, 30);
        passwordSaveComboBox.setPreferredSize(lastColumnSize);
        passwordSaveComboBox.setMaximumSize(lastColumnSize);
        passwordSaveComboBox.setSize(lastColumnSize);
        envPrefixTextField.setPreferredSize(lastColumnSize);
        envPrefixTextField.setMaximumSize(lastColumnSize);
        envPrefixTextField.setSize(lastColumnSize);
        T config = supplier.get();
        Optional.ofNullable(config.getService().getSubscription())
                .ifPresent((subscription -> {
                    this.subscriptionComboBox.setValue(new AzureComboBox.ItemReference<>(subscription.subscriptionId(), Subscription::subscriptionId));
                    subscriptionComboBox.setForceDisable(true);
                    subscriptionComboBox.setEditable(false);
                }));
        Optional.ofNullable(config.getService().getServer())
                .ifPresent((server -> {
                    this.serverComboBox.setValue(new AzureComboBox.ItemReference<>(server.fullyQualifiedDomainName(), Server::fullyQualifiedDomainName));
                    serverComboBox.setForceDisable(true);
                    serverComboBox.setEditable(false);
                }));
        Optional.ofNullable(config.getModule().getModule())
                .ifPresent((module -> {
                    this.moduleComboBox.setValue(new AzureComboBox.ItemReference<>(module.getName(), Module::getName));
                    moduleComboBox.setForceDisable(true);
                    moduleComboBox.setEditable(false);
                }));
        if (Objects.nonNull(config.getService().getPasswordSaveType())) {
            passwordSaveComboBox.setValue(config.getService().getPasswordSaveType());
        }
        testResultLabel.setVisible(false);
        testResultButton.setVisible(false);
        testResultTextPane.setEditable(false);
        testConnectionButton.setEnabled(false);
    }

    protected void initListeners() {
        this.subscriptionComboBox.addItemListener(this::onSubscriptionChanged);
        this.serverComboBox.addItemListener(this::onServerChanged);
        this.databaseComboBox.addItemListener(this::onDatabaseChanged);
        this.inputPasswordField.addKeyListener(this.onInputPasswordFieldChanged());
        this.envPrefixTextField.addFocusListener(this.onEnvPrefixTextFieldChanged());
        this.urlTextField.addFocusListener(this.onUrlTextFieldChanged());
        this.testConnectionButton.addActionListener(this::onTestConnectionButtonClicked);
        this.testResultButton.addActionListener(this::onCopyButtonClicked);
    }

    private void onTestConnectionButtonClicked(ActionEvent e) {
        testConnectionButton.setEnabled(false);
        String url = urlTextField.getText();
        String username = usernameComboBox.getValue();
        String password = String.valueOf(inputPasswordField.getPassword());
        TestConnectionUtils.testConnection(url, username, password, testResultLabel,
                testResultButton, testResultTextPane);
        testConnectionButton.setEnabled(true);
    }

    private void onSubscriptionChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Subscription) {
            final Subscription subscription = (Subscription) e.getItem();
            this.serverComboBox.setSubscription(subscription);
            this.databaseComboBox.setSubscription(subscription);
        }
    }

    private void onServerChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Server) {
            if (e.getItem() instanceof Server) {
                final Server server = (Server) e.getItem();
                this.databaseComboBox.setServer(server);
                this.usernameComboBox.setServer(server);
                this.urlTextField.setText(buildUrl(serverComboBox.getValue(), databaseComboBox.getValue()));
                this.urlTextField.setCaretPosition(0);
            }
        }
    }

    private void onDatabaseChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof DatabaseInner) {
            if (e.getItem() instanceof DatabaseInner) {
                this.urlTextField.setText(buildUrl(serverComboBox.getValue(), databaseComboBox.getValue()));
                this.urlTextField.setCaretPosition(0);
            }
        }
    }

    private FocusListener onEnvPrefixTextFieldChanged() {
        FocusListener listener = new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                System.out.println("env prefix starting.. input... " + inputPasswordField.getPassword());
            }

            @Override
            public void focusLost(FocusEvent e) {
                System.out.println("envPrefix = " + envPrefixTextField.getText());
            }
        };
        return listener;
    }

    private KeyListener onInputPasswordFieldChanged() {
        KeyListener listener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (ArrayUtils.isNotEmpty(inputPasswordField.getPassword())) {
                    testConnectionButton.setEnabled(true);
                }
            }
        };
        return listener;
    }

    private FocusListener onUrlTextFieldChanged() {
        FocusListener listener = new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                System.out.println("url starting.. input... " + inputPasswordField.getPassword());
            }

            @Override
            public void focusLost(FocusEvent e) {
                System.out.println("url = " + envPrefixTextField.getText());
                parseUrl(urlTextField.getText());
            }
        };
        return listener;
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
    public T getData() {
        final T config = supplier.get();
        MySQLLinkConfig source = config.getService();
        source.setSubscription(subscriptionComboBox.getValue());
        source.setServer(serverComboBox.getValue());
        source.setDatabase(databaseComboBox.getValue());
        source.setUsername(usernameComboBox.getValue());
        source.setPassword(inputPasswordField.getPassword());
        source.setPasswordSaveType(passwordSaveComboBox.getValue());
        source.setUrl(urlTextField.getText());
        source.setId(serverComboBox.getValue().id() + "#" + databaseComboBox.getValue().name());
        ModuleLinkConfig target = config.getModule();
        target.setModule(moduleComboBox.getValue());
        config.setEnvPrefix(envPrefixTextField.getText());
        config.setId(serverComboBox.getValue().id());
        return config;
    }

    @Override
    public void setData(T data) {
        MySQLLinkConfig source = data.getService();
        this.subscriptionComboBox.setValue(source.getSubscription());
        this.serverComboBox.setValue(source.getServer());
        this.databaseComboBox.setValue(source.getDatabase());
        this.usernameComboBox.setValue(source.getUsername());
        this.inputPasswordField.setText(String.valueOf(source.getPassword()));
        this.passwordSaveComboBox.setValue(source.getPasswordSaveType());
        ModuleLinkConfig target = data.getModule();
        this.moduleComboBox.setValue(target.getModule());
        this.envPrefixTextField.setText(data.getEnvPrefix());
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        final AzureFormInput<?>[] inputs = {
            this.subscriptionComboBox,
            this.serverComboBox,
            this.databaseComboBox,
            this.usernameComboBox
        };
        return Arrays.asList(inputs);
    }

    private String buildUrl(Server server, DatabaseInner database) {
        StringBuilder builder = new StringBuilder();
        if (Objects.nonNull(server) && StringUtils.isNotBlank(server.fullyQualifiedDomainName())) {
            builder.append(urlHead).append(server.fullyQualifiedDomainName());
        }
        if (Objects.nonNull(database) && StringUtils.isNotBlank(database.name())) {
            builder.append(urlMiddle).append(database.name());
        }
        builder.append(urlTail);
        return builder.toString();
    }

    private void parseUrl(String url) {
        String hostname = serverComboBox.getValue().fullyQualifiedDomainName();
        urlHead = url.substring(0, url.indexOf(hostname));
        String databaseName = databaseComboBox.getValue().name();
        urlTail = url.substring(url.indexOf(databaseName) + databaseName.length());
    }

    private void createUIComponents() {
        moduleComboBox = new ModuleComboBox(project);
    }
}
