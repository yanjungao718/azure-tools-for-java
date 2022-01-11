/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.database.sqlserver.property;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.azure.toolkit.intellij.common.AzResourcePropertiesEditor;
import com.microsoft.azure.toolkit.intellij.common.AzureHideableTitledSeparator;
import com.microsoft.azure.toolkit.intellij.database.component.DatabaseComboBox;
import com.microsoft.azure.toolkit.intellij.database.ui.ConnectionSecurityPanel;
import com.microsoft.azure.toolkit.intellij.database.ui.ConnectionStringsOutputPanel;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.common.telemetry.AzureTelemetry;
import com.microsoft.azure.toolkit.lib.sqlserver.MicrosoftSqlDatabase;
import com.microsoft.azure.toolkit.lib.sqlserver.MicrosoftSqlServer;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Map;

public class SqlServerPropertiesEditor extends AzResourcePropertiesEditor<MicrosoftSqlServer> {

    public static final String ID = "com.microsoft.azure.toolkit.intellij.sqlserver.property.SqlServerPropertiesEditor";

    private AzureHideableTitledSeparator overviewSeparator;
    private SqlServerPropertyOverviewPanel overview;
    private AzureHideableTitledSeparator connectionSecuritySeparator;
    private ConnectionSecurityPanel connectionSecurity;
    private AzureHideableTitledSeparator connectionStringsSeparator;
    private ConnectionStringsOutputPanel connectionStringsJDBC;
    private ConnectionStringsOutputPanel connectionStringsSpring;
    private JPanel rootPanel;
    private JPanel contextPanel;
    private JScrollPane scrollPane;
    private SqlServerPropertyActionPanel propertyActionPanel;
    private DatabaseComboBox<MicrosoftSqlDatabase> databaseComboBox;
    private JLabel databaseLabel;
    public static final String SQL_SERVER_OUTPUT_TEXT_PATTERN_SPRING =
        "spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver" + System.lineSeparator() +
            "spring.datasource.url=jdbc:sqlserver://%s:1433;encrypt=true;trustServerCertificate=false;loginTimeout=30;database=%s;" + System.lineSeparator() +
            "spring.datasource.username=%s" + System.lineSeparator() + "spring.datasource.password={your_password}";

    public static final String SQL_SERVER_OUTPUT_TEXT_PATTERN_JDBC =
        "String url =\"jdbc:sqlserver://%s:1433;encrypt=true;trustServerCertificate=false;loginTimeout=30;database=%s;\";" + System.lineSeparator() +
            "myDbConn = DriverManager.getConnection(url, \"%s\", {your_password});";

    private Boolean originalAllowAccessToAzureServices;
    private Boolean originalAllowAccessToLocal;
    private final Project project;

    @Nonnull
    private final MicrosoftSqlServer server;

    private void rerender() {
        setData(this.server);
    }

    SqlServerPropertiesEditor(@Nonnull Project project, @Nonnull MicrosoftSqlServer server, @Nonnull final VirtualFile virtualFile) {
        super(virtualFile, server, project);
        this.project = project;
        this.server = server;

        overviewSeparator.addContentComponent(overview);
        connectionSecuritySeparator.addContentComponent(connectionSecurity);
        connectionStringsSeparator.addContentComponent(databaseLabel);
        connectionStringsSeparator.addContentComponent(databaseComboBox);
        connectionStringsSeparator.addContentComponent(connectionStringsJDBC);
        connectionStringsSeparator.addContentComponent(connectionStringsSpring);
        connectionStringsJDBC.getTitleLabel().setText("JDBC");
        connectionStringsJDBC.getOutputTextArea().setText(getConnectionString(SQL_SERVER_OUTPUT_TEXT_PATTERN_JDBC, null, null, null));
        connectionStringsSpring.getTitleLabel().setText("Spring");
        connectionStringsSpring.getOutputTextArea().setText(getConnectionString(SQL_SERVER_OUTPUT_TEXT_PATTERN_SPRING, null, null, null));
        this.rerender();
        this.initListeners();
    }

    private String getConnectionString(final String pattern, final String hostname, final String database, final String username) {
        final String newHostname = StringUtils.isNotBlank(hostname) ? hostname : "{your_hostname}";
        final String newDatabase = StringUtils.isNotBlank(database) ? database : "{your_database}";
        final String newUsername = StringUtils.isNotBlank(username) ? username : "{your_username}";
        return String.format(pattern, newHostname, newDatabase, newUsername);
    }

    private void setData(MicrosoftSqlServer server) {
        this.overview.setFormData(server);
        this.databaseComboBox.setServer(server);
        if (StringUtils.equalsIgnoreCase("READY", server.getState())) {
            connectionSecuritySeparator.expand();
            connectionSecuritySeparator.setEnabled(true);
            connectionStringsSeparator.expand();
            connectionStringsSeparator.setEnabled(true);
            originalAllowAccessToAzureServices = server.isAzureServiceAccessAllowed();
            connectionSecurity.getAllowAccessFromAzureServicesCheckBox().setSelected(originalAllowAccessToAzureServices);
            originalAllowAccessToLocal = server.isLocalMachineAccessAllowed();
            connectionSecurity.getAllowAccessFromLocalMachineCheckBox().setSelected(originalAllowAccessToLocal);
        } else {
            connectionSecuritySeparator.collapse();
            connectionSecuritySeparator.setEnabled(false);
            connectionStringsSeparator.collapse();
            connectionStringsSeparator.setEnabled(false);
        }
    }

    private void initListeners() {
        // update to trigger save/discard buttons
        connectionSecurity.getAllowAccessFromAzureServicesCheckBox().addItemListener(this::onCheckBoxChanged);
        connectionSecurity.getAllowAccessFromLocalMachineCheckBox().addItemListener(this::onCheckBoxChanged);
        // actions of copy buttons
        connectionStringsJDBC.getCopyButton().addActionListener(this::onJDBCCopyButtonClicked);
        connectionStringsSpring.getCopyButton().addActionListener(this::onSpringCopyButtonClicked);
        // save/discard buttons
        propertyActionPanel.getSaveButton().addActionListener(this::onSaveButtonClicked);
        propertyActionPanel.getDiscardButton().addActionListener(this::onDiscardButtonClicked);
        // database combobox changed
        databaseComboBox.addItemListener(this::onDatabaseComboBoxChanged);
    }

    private void onCheckBoxChanged(ItemEvent itemEvent) {
        if (itemEvent.getStateChange() == ItemEvent.SELECTED || itemEvent.getStateChange() == ItemEvent.DESELECTED) {
            final boolean changed = SqlServerPropertiesEditor.this.changed();
            SqlServerPropertiesEditor.this.propertyActionPanel.getSaveButton().setEnabled(changed);
            SqlServerPropertiesEditor.this.propertyActionPanel.getDiscardButton().setEnabled(changed);
        }
    }

    private void onJDBCCopyButtonClicked(ActionEvent e) {
        try {
            copyToSystemClipboard(SqlServerPropertiesEditor.this.connectionStringsJDBC.getOutputTextArea().getText());
        } catch (final Exception exception) {
            final String error = "copy JDBC connection strings";
            final String action = "try again later.";
            throw new AzureToolkitRuntimeException(error, action);
        }
    }

    private void copyToSystemClipboard(String text) {
        CopyPasteManager.getInstance().setContents(new StringSelection(text));
    }

    private void onSpringCopyButtonClicked(ActionEvent e) {
        try {
            copyToSystemClipboard(SqlServerPropertiesEditor.this.connectionStringsSpring.getOutputTextArea().getText());
        } catch (final Exception exception) {
            final String error = "copy Spring connection strings";
            final String action = "try again later.";
            throw new AzureToolkitRuntimeException(error, action);
        }
    }

    private void onSaveButtonClicked(ActionEvent e) {
        final String actionName = "Save";
        final String originalText = SqlServerPropertiesEditor.this.propertyActionPanel.getSaveButton().getText();
        SqlServerPropertiesEditor.this.propertyActionPanel.getSaveButton().setText(actionName);
        SqlServerPropertiesEditor.this.propertyActionPanel.getSaveButton().setEnabled(false);
        final Runnable runnable = () -> {
            final String subscriptionId = server.getSubscriptionId();
            this.server.refresh();
            originalAllowAccessToAzureServices = server.isAzureServiceAccessAllowed();
            originalAllowAccessToLocal = server.isLocalMachineAccessAllowed();
            final boolean allowAccessToAzureServices = connectionSecurity.getAllowAccessFromAzureServicesCheckBox().getModel().isSelected();
            if (!originalAllowAccessToAzureServices.equals(allowAccessToAzureServices)) {
                server.firewallRules().toggleAzureServiceAccess(allowAccessToAzureServices);
                originalAllowAccessToAzureServices = allowAccessToAzureServices;
            }
            final boolean allowAccessToLocal = connectionSecurity.getAllowAccessFromLocalMachineCheckBox().getModel().isSelected();
            if (!originalAllowAccessToLocal.equals(allowAccessToLocal)) {
                server.firewallRules().toggleLocalMachineAccess(allowAccessToLocal);
                originalAllowAccessToLocal = allowAccessToLocal;
            }
            SqlServerPropertiesEditor.this.propertyActionPanel.getSaveButton().setText(originalText);
            final boolean changed = SqlServerPropertiesEditor.this.changed();
            SqlServerPropertiesEditor.this.propertyActionPanel.getSaveButton().setEnabled(changed);
            SqlServerPropertiesEditor.this.propertyActionPanel.getDiscardButton().setEnabled(changed);
            final Map<String, String> properties = new HashMap<>();
            properties.put("subscriptionId", subscriptionId);
            properties.put("allowAccessToLocal", String.valueOf(allowAccessToLocal));
            properties.put("allowAccessToAzureServices", String.valueOf(allowAccessToAzureServices));
            AzureTelemetry.getActionContext().setProperties(properties);
        };
        AzureTaskManager.getInstance().runInBackground(new AzureTask<>(this.project, String.format("%s...", actionName), false, runnable));
    }

    private void onDiscardButtonClicked(ActionEvent e) {
        SqlServerPropertiesEditor.this.propertyActionPanel.getSaveButton().setEnabled(false);
        SqlServerPropertiesEditor.this.propertyActionPanel.getDiscardButton().setEnabled(false);
        connectionSecurity.getAllowAccessFromAzureServicesCheckBox().setSelected(originalAllowAccessToAzureServices);
        connectionSecurity.getAllowAccessFromLocalMachineCheckBox().setSelected(originalAllowAccessToLocal);
    }

    private void onDatabaseComboBoxChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof MicrosoftSqlDatabase) {
            final MicrosoftSqlDatabase database = (MicrosoftSqlDatabase) e.getItem();
            final String username = server.getAdminName() + "@" + server.name();
            connectionStringsJDBC.getOutputTextArea().setText(getConnectionString(SQL_SERVER_OUTPUT_TEXT_PATTERN_JDBC,
                server.getFullyQualifiedDomainName(), database.getName(), username));
            connectionStringsSpring.getOutputTextArea().setText(getConnectionString(SQL_SERVER_OUTPUT_TEXT_PATTERN_SPRING,
                server.getFullyQualifiedDomainName(), database.getName(), username));
        }
    }

    private boolean changed() {
        return originalAllowAccessToAzureServices != connectionSecurity.getAllowAccessFromAzureServicesCheckBox().getModel().isSelected() ||
            originalAllowAccessToLocal != connectionSecurity.getAllowAccessFromLocalMachineCheckBox().getModel().isSelected();
    }

    @Override
    public @Nonnull
    JComponent getComponent() {
        return rootPanel;
    }

    @Override
    public @Nonnull
    String getName() {
        return this.server.name();
    }

    @Override
    public void dispose() {

    }

    protected void refresh() {
        this.propertyActionPanel.getSaveButton().setEnabled(false);
        final String refreshTitle = String.format("Refreshing SQL server(%s)...", this.server.getName());
        AzureTaskManager.getInstance().runInBackground(refreshTitle, () -> {
            this.server.refresh();
            AzureTaskManager.getInstance().runLater(this::rerender);
        });
    }
}
