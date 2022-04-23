/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.sdkmanage;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azuretools.authmanage.CommonSettings;
import com.microsoft.azuretools.authmanage.Environment;
import com.microsoft.azuretools.authmanage.SubscriptionManager;

import java.io.IOException;
import java.util.List;

public interface AzureManager {
    Azure getAzure(String sid);

    Subscription getSubscriptionById(String sid);

    List<Subscription> getSubscriptions();

    List<Subscription> getSelectedSubscriptions();

    SubscriptionManager getSubscriptionManager();

    void drop();

    String getAccessToken(String tid, String resource) throws IOException;

    String getManagementURI();

    String getStorageEndpointSuffix();

    Environment getEnvironment();

    String getPortalUrl();

    default String getAccessToken(String tid) throws IOException {
        return getAccessToken(tid, CommonSettings.getAdEnvironment().resourceManagerEndpoint());
    }
}
