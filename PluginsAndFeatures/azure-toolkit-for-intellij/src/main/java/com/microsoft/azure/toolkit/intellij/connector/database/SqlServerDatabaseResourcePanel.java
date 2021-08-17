/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.database;


import com.microsoft.azure.toolkit.intellij.connector.database.component.DatabaseComboBox;
import com.microsoft.azure.toolkit.intellij.connector.database.component.ServerComboBox;
import com.microsoft.azure.toolkit.intellij.connector.database.component.UsernameComboBox;
import com.microsoft.azure.toolkit.intellij.connector.sql.SqlServerDatabaseResource;
import com.microsoft.azure.toolkit.lib.common.entity.IAzureResourceEntity;
import com.microsoft.azure.toolkit.lib.database.JdbcUrl;
import com.microsoft.azure.toolkit.lib.sqlserver.SqlServer;
import com.microsoft.azure.toolkit.lib.sqlserver.model.SqlDatabaseEntity;
import org.apache.commons.lang3.StringUtils;

public class SqlServerDatabaseResourcePanel extends DatabaseResourcePanel {

    public SqlServerDatabaseResourcePanel() {
        this.jdbcUrl = JdbcUrl.sqlserver(StringUtils.EMPTY);
        envPrefixTextField.setText("AZURE_SQL_");

    }

    @Override
    protected void createUIComponents() {
        // server
        this.serverComboBox = new ServerComboBox<SqlServer>();
        // database
        this.databaseComboBox = new DatabaseComboBox<SqlServer, SqlDatabaseEntity>();
        // username
        this.usernameComboBox = new UsernameComboBox<SqlServer>();
    }

    @Override
    protected DatabaseResource createDatabaseResource() {
        return new SqlServerDatabaseResource(((IAzureResourceEntity) this.databaseComboBox.getValue()).getId());
    }
}
