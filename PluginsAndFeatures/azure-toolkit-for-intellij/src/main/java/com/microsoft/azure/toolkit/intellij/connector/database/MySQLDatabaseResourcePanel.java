/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.database;


import com.microsoft.azure.toolkit.intellij.connector.database.component.DatabaseComboBox;
import com.microsoft.azure.toolkit.intellij.connector.database.component.ServerComboBox;
import com.microsoft.azure.toolkit.intellij.connector.database.component.UsernameComboBox;
import com.microsoft.azure.toolkit.intellij.connector.mysql.MySQLDatabaseResource;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.entity.IAzureResourceEntity;
import com.microsoft.azure.toolkit.lib.database.JdbcUrl;
import com.microsoft.azure.toolkit.lib.mysql.AzureMySql;
import com.microsoft.azure.toolkit.lib.mysql.MySqlServer;
import com.microsoft.azure.toolkit.lib.mysql.model.MySqlDatabaseEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Objects;

public class MySQLDatabaseResourcePanel extends DatabaseResourcePanel {


    public MySQLDatabaseResourcePanel() {
        this.jdbcUrl = JdbcUrl.mysql(StringUtils.EMPTY);
        envPrefixTextField.setText("AZURE_MYSQL_");
        this.serverComboBox.setItemsLoader(() -> Objects.isNull(this.serverComboBox.getSubscription()) ? Collections.emptyList() :
                Azure.az(AzureMySql.class).subscription(this.serverComboBox.getSubscription().getId()).list());
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
