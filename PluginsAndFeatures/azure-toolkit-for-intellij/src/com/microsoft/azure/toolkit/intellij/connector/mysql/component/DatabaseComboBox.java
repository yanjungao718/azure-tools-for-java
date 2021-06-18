/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.mysql.component;

import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.mysql.model.MySqlDatabaseEntity;
import com.microsoft.azure.toolkit.lib.mysql.service.MySqlServer;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DatabaseComboBox extends AzureComboBox<MySqlDatabaseEntity> {
    @Getter
    private MySqlServer server;

    public DatabaseComboBox() {
        super(false);
    }

    public void setServer(MySqlServer server) {
        if (Objects.equals(server, this.server)) {
            return;
        }
        this.server = server;
        if (server == null || !server.exists()) {
            this.clear();
            return;
        }
        this.refreshItems();
    }

    @Override
    protected String getItemText(Object item) {
        if (item instanceof MySqlDatabaseEntity) {
            return ((MySqlDatabaseEntity) item).getName();
        }
        return super.getItemText(item);
    }

    @Override
    @AzureOperation(
        name = "mysql|database.list.server|subscription",
        params = {"this.server.entity().getName()", "this.subscription.getId()"},
        type = AzureOperation.Type.SERVICE
    )
    protected List<? extends MySqlDatabaseEntity> loadItems() {
        if (Objects.isNull(server)) {
            return new ArrayList<>();
        }
        return server.databases();
    }

    @Override
    public boolean isRequired() {
        return true;
    }
}
