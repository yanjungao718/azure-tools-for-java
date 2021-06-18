/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.mysql.component;

import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.lib.mysql.service.MySqlServer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UsernameComboBox extends AzureComboBox<String> {

    private static final String SEPARATOR = "@";

    private MySqlServer server;

    public UsernameComboBox() {
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
    protected List<? extends String> loadItems() {
        if (server == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(server.entity().getAdministratorLoginName() + SEPARATOR + server.name());
    }

    @Override
    public boolean isRequired() {
        return true;
    }
}
