/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.mysql.component;

import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.mysql.service.AzureMySql;
import com.microsoft.azure.toolkit.lib.mysql.service.MySqlServer;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.sdkmanage.AzureManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServerComboBox extends AzureComboBox<MySqlServer> {

    private Subscription subscription;

    public ServerComboBox() {
        super(true);
    }

    public void setSubscription(Subscription subscription) {
        if (Objects.equals(subscription, this.subscription)) {
            return;
        }
        this.subscription = subscription;
        if (subscription == null) {
            this.clear();
            return;
        }
        this.refreshItems();
    }

    @Override
    protected String getItemText(Object item) {
        if (item instanceof MySqlServer) {
            return ((MySqlServer) item).name();
        }
        return super.getItemText(item);
    }

    @Override
    protected List<? extends MySqlServer> loadItems() throws Exception {
        if (Objects.isNull(subscription)) {
            return new ArrayList<>();
        }
        final AzureManager manager = AuthMethodManager.getInstance().getAzureManager();
        if (Objects.isNull(manager)) {
            return new ArrayList<>();
        }
        return Azure.az(AzureMySql.class).list();
    }

    @Override
    public boolean isRequired() {
        return true;
    }

}
