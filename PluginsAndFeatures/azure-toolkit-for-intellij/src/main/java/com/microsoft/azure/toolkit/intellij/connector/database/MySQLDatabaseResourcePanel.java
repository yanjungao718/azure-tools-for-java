/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.database;


import com.microsoft.azure.toolkit.intellij.connector.database.component.DatabaseComboBox;
import com.microsoft.azure.toolkit.intellij.connector.database.component.ServerComboBox;
import com.microsoft.azure.toolkit.intellij.connector.database.component.UsernameComboBox;
import com.microsoft.azure.toolkit.intellij.connector.mysql.MySQLDatabaseResource;
import com.microsoft.azure.toolkit.lib.common.entity.IAzureResourceEntity;
import com.microsoft.azure.toolkit.lib.database.JdbcUrl;
import com.microsoft.azure.toolkit.lib.mysql.MySqlServer;
import com.microsoft.azure.toolkit.lib.mysql.model.MySqlDatabaseEntity;
import org.apache.commons.lang3.StringUtils;

public class MySQLDatabaseResourcePanel extends DatabaseResourcePanel {


    public MySQLDatabaseResourcePanel() {
        this.jdbcUrl = JdbcUrl.mysql(StringUtils.EMPTY);
        envPrefixTextField.setText("AZURE_MYSQL_");
    }

    @Override
    protected void createUIComponents() {
        // server
        this.serverComboBox = new ServerComboBox<MySqlServer>();
        // database
        this.databaseComboBox = new DatabaseComboBox<MySqlServer, MySqlDatabaseEntity>();
        // username
        this.usernameComboBox = new UsernameComboBox<MySqlServer>();
    }

    @Override
    protected DatabaseResource createDatabaseResource() {
        return new MySQLDatabaseResource(((IAzureResourceEntity) this.databaseComboBox.getValue()).getId());
    }
}
