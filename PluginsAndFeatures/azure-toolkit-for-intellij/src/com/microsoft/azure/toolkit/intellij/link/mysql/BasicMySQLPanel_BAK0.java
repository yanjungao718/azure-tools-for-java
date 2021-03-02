///*
// * Copyright (c) Microsoft Corporation. All rights reserved.
// * Licensed under the MIT License. See License.txt in the project root for license information.
// */
//
//package com.microsoft.azure.toolkit.intellij.link.mysql;
//
//import com.intellij.icons.AllIcons;
//import com.intellij.openapi.project.Project;
//import com.microsoft.azure.management.mysql.v2020_01_01.Server;
//import com.microsoft.azure.management.mysql.v2020_01_01.implementation.DatabaseInner;
//import com.microsoft.azure.management.resources.Subscription;
//import com.microsoft.azure.toolkit.intellij.appservice.subscription.SubscriptionComboBox;
//import com.microsoft.azure.toolkit.intellij.common.AzureFormPanel;
//import com.microsoft.azure.toolkit.intellij.common.ModuleComboBox;
//import com.microsoft.azure.toolkit.intellij.link.LinkComposite;
//import com.microsoft.azure.toolkit.intellij.link.ModuleLinkConfig;
//import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
//import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
//import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
//import com.microsoft.intellij.ui.components.AzureWizardStep;
//import lombok.Getter;
//import lombok.Setter;
//import org.apache.commons.lang3.StringUtils;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.FocusEvent;
//import java.awt.event.FocusListener;
//import java.awt.event.ItemEvent;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Objects;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.function.Supplier;
//
//@Getter
//@Setter
//public class BasicMySQLPanel_BAK0<T extends LinkComposite<MySQLLinkConfig, ModuleLinkConfig>> extends JPanel implements AzureFormPanel<T> {
//    private SubscriptionComboBox subscriptionComboBox;
//    private ServerComboBox serverComboBox;
//    private DatabaseComboBox databaseComboBox;
//    private UsernameComboBox usernameComboBox;
//    private JPasswordField inputPasswordField;
//    private JPanel rootPanel;
//    private JTextField envPrefixTextField;
//    private PasswordSaveComboBox passwordSaveComboBox;
//    private JTextField urlTextField;
//    private JButton testConnectionButton;
//    private ModuleComboBox moduleComboBox;
//    private JLabel testConectionResultTextField;
//    private JTextField portTextField;
//
//    private final Supplier<T> supplier;
//    private AzureWizardStep wizardStep;
//    private Project project;
//
//    private String urlHead = "jdbc:mysql://";
//    private String urlMiddle = ":3306/";
//    private String urlTail = "?serverTimezone=UTC&useSSL=true&requireSSL=false";
//
//    public BasicMySQLPanel_BAK0(Project project, final Supplier<T> supplier) {
//        super();
//        this.project = project;
//        this.supplier = supplier;
//        init();
//        initListeners();
//    }
//
//    private void init() {
//        testConectionResultTextField.setText(StringUtils.EMPTY);
//        Dimension lastColumnSize = new Dimension(141, 38);
//        passwordSaveComboBox.setPreferredSize(lastColumnSize);
//        passwordSaveComboBox.setMaximumSize(lastColumnSize);
//        passwordSaveComboBox.setSize(lastColumnSize);
//        envPrefixTextField.setPreferredSize(lastColumnSize);
//        envPrefixTextField.setMaximumSize(lastColumnSize);
//        envPrefixTextField.setSize(lastColumnSize);
//        portTextField.setPreferredSize(lastColumnSize);
//        portTextField.setMaximumSize(lastColumnSize);
//        portTextField.setSize(lastColumnSize);
//        portTextField.setEnabled(false);
//        T config = supplier.get();
//        /*if (Objects.nonNull(config.getSubscription())) {
//            subscriptionComboBox.setValue(config.getSubscription());
//        }
//        if (Objects.nonNull(config.getServer())) {
//            serverComboBox.setValue(config.getServer());
//        }
//        if (Objects.nonNull(config.getPasswordSaveType())) {
//            passwordSaveComboBox.setValue(config.getPasswordSaveType());
//        }*/
//        /*if (LinkDirection.SERVICE_TO_PROJECT.equals(config.getLinkDirection())) {
//            subscriptionComboBox.setEnabled(false);
//            serverComboBox.setEnabled(false);
//        } else if (LinkDirection.PROJECT_TO_SERVICE.equals(config.getLinkDirection())) {
//            projectComboBox.setEnabled(false);
//        }*/
//        // passwordSaveComboBox.setPreferredSize(new Dimension(141, 38));
//        // passwordSaveComboBox.setMinimumAndPreferredWidth(130);
//    }
//
//    protected void initListeners() {
//        this.subscriptionComboBox.addItemListener(this::onSubscriptionChanged);
//        this.serverComboBox.addItemListener(this::onServerChanged);
//        this.databaseComboBox.addItemListener(this::onDatabaseChanged);
//        this.usernameComboBox.addItemListener(this::onUsernameChanged);
//        this.inputPasswordField.addFocusListener(this.onInputPasswordFieldChanged());
//        this.envPrefixTextField.addFocusListener(this.onEnvPrefixTextFieldChanged());
//        this.urlTextField.addFocusListener(this.onUrlTextFieldChanged());
//        this.testConnectionButton.addActionListener(this::onTestConnectionButtonClicked);
//    }
//
//    private void onTestConnectionButtonClicked(ActionEvent e) {
//        testConnectionButton.setEnabled(false);
//        AtomicBoolean connected = new AtomicBoolean(false);
//        String url = urlTextField.getText();
//        Runnable runnable = () -> {
//            String username = usernameComboBox.getValue();
//            String password = "a222222@"; // inputPasswordField.getPassword();
//            // refresh property
//            try {
//                Class.forName("com.mysql.jdbc.Driver");
//                Connection connection = DriverManager.getConnection(url, username, password);
//                Statement statement = connection.createStatement();
//                ResultSet resultSet = statement.executeQuery("select 'hi'");
//                if (resultSet.next()) {
//                    String result = resultSet.getString(1);
//                    connected.set("hi".equals(result));
//
//                }
//            } catch (ClassNotFoundException | SQLException exception) {
//                testConectionResultTextField.setText(exception.getMessage());
//            }
//        };
//        AzureTask task = new AzureTask(null, String.format("Connecting to Azure Database for MySQL (%s)...", this.parseHostnameFromUrl(url)), false, runnable);
//        AzureTaskManager.getInstance().runAndWait(task);
//        if (connected.get()) {
//            testConectionResultTextField.setIcon(AllIcons.General.InspectionsOK);
//            // testConectionResultTextField.setIcon(AllIcons.General.NotificationError);
//            testConectionResultTextField.setText("Connected Successfully!");
//        }
//        testConnectionButton.setEnabled(true);
//    }
//
//    private void onSubscriptionChanged(final ItemEvent e) {
//        if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Subscription) {
//            final Subscription subscription = (Subscription) e.getItem();
//            this.serverComboBox.setSubscription(subscription);
//            this.databaseComboBox.setSubscription(subscription);
//        }
//    }
//
//    private void onServerChanged(final ItemEvent e) {
//        if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Server) {
//            if (e.getItem() instanceof Server) {
//                final Server server = (Server) e.getItem();
//                this.databaseComboBox.setServer(server);
//                this.usernameComboBox.setServer(server);
//                this.urlTextField.setText(buildUrl(serverComboBox.getValue(), databaseComboBox.getValue()));
//            }
//        }
//    }
//
//    private void onDatabaseChanged(final ItemEvent e) {
//        if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof DatabaseInner) {
//            if (e.getItem() instanceof DatabaseInner) {
//                this.urlTextField.setText(buildUrl(serverComboBox.getValue(), databaseComboBox.getValue()));
//                /*final Server server = (Server) e.getItem();
//                this.databaseComboBox.setServer(server);
//                this.usernameComboBox.setServer(server);*/
//            }
//        }
//    }
//
//    private void onUsernameChanged(final ItemEvent e) {
//        if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof String) {
//            if (e.getItem() instanceof String) {
//                /*final Server server = (Server) e.getItem();
//                this.databaseComboBox.setServer(server);
//                this.usernameComboBox.setServer(server);*/
//            }
//        }
//    }
//
//    private FocusListener onEnvPrefixTextFieldChanged() {
//        FocusListener listener = new FocusListener() {
//            @Override
//            public void focusGained(FocusEvent e) {
//                System.out.println("env prefix starting.. input... " + inputPasswordField.getPassword());
//            }
//
//            @Override
//            public void focusLost(FocusEvent e) {
//                System.out.println("envPrefix = " + envPrefixTextField.getText());
//            }
//        };
//        return listener;
//    }
//
//    private FocusListener onInputPasswordFieldChanged() {
//        FocusListener listener = new FocusListener() {
//
//            @Override
//            public void focusGained(FocusEvent e) {
//                System.out.println("password starting.. input... " + inputPasswordField.getPassword());
//            }
//
//            @Override
//            public void focusLost(FocusEvent e) {
//                System.out.println("password = " + String.valueOf(inputPasswordField.getPassword()));
//            }
//        };
//        return listener;
//    }
//
//    private FocusListener onUrlTextFieldChanged() {
//        FocusListener listener = new FocusListener() {
//            @Override
//            public void focusGained(FocusEvent e) {
//                System.out.println("url starting.. input... " + inputPasswordField.getPassword());
//            }
//
//            @Override
//            public void focusLost(FocusEvent e) {
//                System.out.println("url = " + envPrefixTextField.getText());
//                parseUrl(urlTextField.getText());
//            }
//        };
//        return listener;
//    }
//
//    @Override
//    public T getData() {
//        final T config = supplier.get();
//        MySQLLinkConfig source = config.getService();
//        source.setSubscription(subscriptionComboBox.getValue());
//        source.setServer(serverComboBox.getValue());
//        source.setDatabase(databaseComboBox.getValue());
//        source.setUsername(usernameComboBox.getValue());
//        source.setPassword(inputPasswordField.getPassword());
//        source.setPasswordSaveType(passwordSaveComboBox.getValue());
//        source.setUrl(urlTextField.getText());
//        // source.setEnvPrefix(envPrefixTextField.getText());
//        ModuleLinkConfig target = config.getModule();
//        target.setModule(moduleComboBox.getValue());
//        // target.setEnvPrefix(envPrefixTextField.getText());
//        config.setEnvPrefix(envPrefixTextField.getText());
//        return config;
//    }
//
//    @Override
//    public void setData(T data) {
//        MySQLLinkConfig source = data.getService();
//        this.subscriptionComboBox.setValue(source.getSubscription());
//        this.serverComboBox.setValue(source.getServer());
//        this.databaseComboBox.setValue(source.getDatabase());
//        this.usernameComboBox.setValue(source.getUsername());
//        this.inputPasswordField.setText(String.valueOf(source.getPassword()));
//        this.passwordSaveComboBox.setValue(source.getPasswordSaveType());
//        ModuleLinkConfig target = data.getModule();
//        this.moduleComboBox.setValue(target.getModule());
//        // this.envPrefixTextField.setText(target.getEnvPrefix());
//        this.envPrefixTextField.setText(data.getEnvPrefix());
//    }
//
//    @Override
//    public List<AzureFormInput<?>> getInputs() {
//        final AzureFormInput<?>[] inputs = {
//            this.subscriptionComboBox,
//            this.serverComboBox,
//            this.databaseComboBox,
//            this.usernameComboBox
//            // this.inputPasswordField,
//
//        };
//        return Arrays.asList(inputs);
//    }
//
//    private String buildUrl(Server server, DatabaseInner database) {
//        StringBuilder builder = new StringBuilder();
//        if (Objects.nonNull(server) && StringUtils.isNotBlank(server.fullyQualifiedDomainName())) {
//            builder.append(urlHead).append(server.fullyQualifiedDomainName());
//        }
//        if (Objects.nonNull(database) && StringUtils.isNotBlank(database.name())) {
//            builder.append(urlMiddle).append(database.name());
//        }
//        builder.append(urlTail);
//        return builder.toString();
//    }
//
//    private void parseUrl(String url) {
//        String hostname = serverComboBox.getValue().fullyQualifiedDomainName();
//        urlHead = url.substring(0, url.indexOf(hostname));
//        String databaseName = databaseComboBox.getValue().name();
//        urlTail = url.substring(url.indexOf(databaseName) + databaseName.length());
//    }
//
//    private String parseHostnameFromUrl(String url) {
//        String tailPartial = url.substring(url.indexOf("//"));
//        String hostname = tailPartial.substring(0, tailPartial.indexOf(":"));
//        return hostname;
//    }
//
//    private void createUIComponents() {
//        // TODO: place custom component creation code here
//        moduleComboBox = new ModuleComboBox(project);
//    }
//}
