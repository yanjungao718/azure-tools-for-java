/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.database;


import com.intellij.icons.AllIcons;
import com.intellij.ui.AnimatedIcon;
import com.microsoft.azure.toolkit.intellij.appservice.subscription.SubscriptionComboBox;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.intellij.common.AzureFormJPanel;
import com.microsoft.azure.toolkit.intellij.connector.Password;
import com.microsoft.azure.toolkit.intellij.connector.database.component.DatabaseComboBox;
import com.microsoft.azure.toolkit.intellij.connector.database.component.PasswordSaveComboBox;
import com.microsoft.azure.toolkit.intellij.connector.database.component.ServerComboBox;
import com.microsoft.azure.toolkit.intellij.connector.database.component.TestConnectionActionPanel;
import com.microsoft.azure.toolkit.intellij.connector.database.component.UsernameComboBox;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.entity.AbstractAzureEntityManager;
import com.microsoft.azure.toolkit.lib.common.entity.IAzureResourceEntity;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.database.JdbcUrl;
import com.microsoft.azure.toolkit.lib.database.entity.IDatabaseServer;
import com.microsoft.azure.toolkit.lib.mysql.AzureMySql;
import com.microsoft.azuretools.azurecommons.util.Utils;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

abstract class DatabaseResourcePanel implements AzureFormJPanel<DatabaseResource> {
    @Getter
    private JPanel contentPanel;
    private SubscriptionComboBox subscriptionComboBox;
    protected ServerComboBox serverComboBox;
    protected DatabaseComboBox databaseComboBox;
    protected UsernameComboBox usernameComboBox;
    private JPasswordField inputPasswordField;
    protected JTextField envPrefixTextField;
    private PasswordSaveComboBox passwordSaveComboBox;
    private JTextField urlTextField;
    private JButton testConnectionButton;
    private JTextPane testResultTextPane;
    private TestConnectionActionPanel testConnectionActionPanel;

    protected JdbcUrl jdbcUrl = null;

    public DatabaseResourcePanel() {
        super();
        init();
        initListeners();
    }

    private void init() {
        final Dimension passwordSaveComboBoxSize = new Dimension(106, passwordSaveComboBox.getPreferredSize().height);
        passwordSaveComboBox.setPreferredSize(passwordSaveComboBoxSize);
        passwordSaveComboBox.setMaximumSize(passwordSaveComboBoxSize);
        passwordSaveComboBox.setSize(passwordSaveComboBoxSize);
        final Dimension envPrefixTextFieldSize = new Dimension(106, envPrefixTextField.getPreferredSize().height);
        envPrefixTextField.setPreferredSize(envPrefixTextFieldSize);
        envPrefixTextField.setMaximumSize(envPrefixTextFieldSize);
        envPrefixTextField.setSize(envPrefixTextFieldSize);
        testConnectionActionPanel.setVisible(false);
        testResultTextPane.setEditable(false);
        testConnectionButton.setEnabled(false);
        this.passwordSaveComboBox.setValue(Arrays.stream(Password.SaveType.values())
                .filter(e -> StringUtils.equals(e.name(), Azure.az().config().getDatabasePasswordSaveType())).findAny()
                .orElse(Password.SaveType.UNTIL_RESTART));
        // server loader
        this.serverComboBox.setItemsLoader(() -> Objects.isNull(this.serverComboBox.getSubscription()) ? Collections.emptyList() :
                Azure.az(AzureMySql.class).subscription(this.serverComboBox.getSubscription().getId()).list());
        // database loader
        this.databaseComboBox.setItemsLoader(() -> Objects.isNull(this.databaseComboBox.getServer()) ||
                !StringUtils.equals("Ready", this.databaseComboBox.getServer().entity().getState()) ?
                Collections.emptyList() : this.databaseComboBox.getServer().databases());
        // username loader
        this.usernameComboBox.setItemsLoader(() -> Objects.isNull(this.databaseComboBox.getServer()) ? Collections.emptyList() :
                Collections.singletonList(this.databaseComboBox.getServer().entity().getAdministratorLoginName() + "@" +
                        this.databaseComboBox.getServer().entity().getName()));
    }

    protected void initListeners() {
        this.subscriptionComboBox.addItemListener(this::onSubscriptionChanged);
        this.serverComboBox.addItemListener(this::onServerChanged);
        this.databaseComboBox.addItemListener(this::onDatabaseChanged);
        this.inputPasswordField.addKeyListener(this.onPasswordChanged());
        this.urlTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                onUrlEdited();
            }
        });
        this.testConnectionButton.addActionListener(this::onTestConnectionButtonClicked);
        this.testConnectionActionPanel.getCopyButton().addActionListener(this::onCopyButtonClicked);
    }

    private void onTestConnectionButtonClicked(ActionEvent e) {
        testConnectionButton.setEnabled(false);
        testConnectionButton.setIcon(new AnimatedIcon.Default());
        testConnectionButton.setDisabledIcon(new AnimatedIcon.Default());
        final String username = (String) usernameComboBox.getValue();
        final String password = String.valueOf(inputPasswordField.getPassword());
        final String title = String.format("Connecting to Azure Database for MySQL (%s)...", jdbcUrl.getServerHost());
        AzureTaskManager.getInstance().runInBackground(title, false, () -> {
            final DatabaseConnectionUtils.ConnectResult connectResult = DatabaseConnectionUtils.connectWithPing(this.jdbcUrl, username, password);
            // show result info
            testConnectionActionPanel.setVisible(true);
            testResultTextPane.setText(getConnectResultMessage(connectResult));
            final Icon icon = connectResult.isConnected() ? AllIcons.General.InspectionsOK : AllIcons.General.BalloonError;
            testConnectionActionPanel.getIconLabel().setIcon(icon);
            testConnectionButton.setIcon(null);
            testConnectionButton.setEnabled(true);
        });
    }

    private String getConnectResultMessage(DatabaseConnectionUtils.ConnectResult result) {
        final StringBuilder messageBuilder = new StringBuilder();
        if (result.isConnected()) {
            messageBuilder.append("Connected successfully.").append(System.lineSeparator());
            messageBuilder.append("Version: ").append(result.getServerVersion()).append(System.lineSeparator());
            messageBuilder.append("Ping cost: ").append(result.getPingCost()).append("ms");
        } else {
            messageBuilder.append("Failed to connect with above parameters.").append(System.lineSeparator());
            messageBuilder.append("Message: ").append(result.getMessage());
        }
        return messageBuilder.toString();
    }

    private void onSubscriptionChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            final Subscription subscription = (Subscription) e.getItem();
            this.serverComboBox.setSubscription(subscription);
        } else if (e.getStateChange() == ItemEvent.DESELECTED) {
            this.serverComboBox.setSubscription(null);
        }
    }

    private void onServerChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            final IDatabaseServer server = (IDatabaseServer) e.getItem();
            this.databaseComboBox.setServer(server);
            this.usernameComboBox.setServer(server);
        } else if (e.getStateChange() == ItemEvent.DESELECTED) {
            this.databaseComboBox.setServer(null);
            this.usernameComboBox.setServer(null);
        }
    }

    private void onDatabaseChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED) {
            String server = Optional.ofNullable(this.databaseComboBox.getServer())
                    .map(s -> s.entity().getFullyQualifiedDomainName()).orElse(null);
            String database = Optional.ofNullable((IAzureResourceEntity) e.getItem()).map(IAzureResourceEntity::getName).orElse(null);
            this.jdbcUrl.setServerHost(server).setDatabase(database);
            this.urlTextField.setText(this.jdbcUrl.toString());
            this.urlTextField.setCaretPosition(0);
        }
    }

    private void onUrlEdited() {
        try {
            this.jdbcUrl = JdbcUrl.from(this.urlTextField.getText());
            this.serverComboBox.setValue(new AzureComboBox.ItemReference<>(this.jdbcUrl.getServerHost(),
                    server -> ((IDatabaseServer) server).entity().getFullyQualifiedDomainName()));
            this.databaseComboBox.setValue(new AzureComboBox.ItemReference<>(this.jdbcUrl.getDatabase(), IAzureResourceEntity::getName));
        } catch (final Exception exception) {
            // TODO: messager.warning(...)
        }
    }

    private void onCopyButtonClicked(ActionEvent e) {
        try {
            Utils.copyToSystemClipboard(testResultTextPane.getText());
        } catch (final Exception exception) {
            final String error = "copy test result error";
            final String action = "try again later.";
            throw new AzureToolkitRuntimeException(error, action);
        }
    }

    private KeyListener onPasswordChanged() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (ArrayUtils.isNotEmpty(inputPasswordField.getPassword())) {
                    testConnectionButton.setEnabled(true);
                }
            }
        };
    }

    @Override
    public DatabaseResource getData() {
        final Password password = new Password();
        password.password(inputPasswordField.getPassword());
        password.saveType(passwordSaveComboBox.getValue());

        final DatabaseResource resource = createDatabaseResource();
        resource.setPassword(password);
        resource.setUsername((String) usernameComboBox.getValue());
        resource.setJdbcUrl(this.jdbcUrl);
        resource.setEnvPrefix(envPrefixTextField.getText());
        return resource;
    }

    @Override
    public void setData(DatabaseResource resource) {
        Optional.ofNullable(resource.getServerId()).ifPresent((serverId -> {
            Optional.ofNullable(serverId.subscriptionId()).ifPresent(subscriptionId ->
                this.subscriptionComboBox.setValue(new AzureComboBox.ItemReference<>(subscriptionId, Subscription::getId), true));
            Optional.ofNullable(serverId.name()).ifPresent(name ->
                this.serverComboBox.setValue(new AzureComboBox.ItemReference<>(name,
                        server -> ((AbstractAzureEntityManager) server).entity().getName()), true));
        }));
        Optional.ofNullable(resource.getPassword()).ifPresent(config ->
                Optional.ofNullable(config.password()).ifPresent(password -> this.inputPasswordField.setText(String.valueOf(password))));
        if (Objects.nonNull(resource.getPassword()) && Objects.nonNull(resource.getPassword().saveType())) {
            this.passwordSaveComboBox.setValue(resource.getPassword().saveType());
        } else {
            this.passwordSaveComboBox.setValue(Arrays.stream(Password.SaveType.values())
                .filter(e -> StringUtils.equals(e.name(), Azure.az().config().getDatabasePasswordSaveType())).findAny()
                .orElse(Password.SaveType.UNTIL_RESTART));
        }
        Optional.ofNullable(resource.getDatabaseName()).ifPresent(dbName ->
            this.databaseComboBox.setValue(new AzureComboBox.ItemReference<>(dbName, IAzureResourceEntity::getName), true));
        Optional.ofNullable(resource.getUsername())
            .ifPresent((username -> this.usernameComboBox.setValue(username)));
        Optional.ofNullable(resource.getEnvPrefix())
            .ifPresent(p -> this.envPrefixTextField.setText(p));
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

    protected void createUIComponents() {
    }

    protected abstract DatabaseResource createDatabaseResource();
}
