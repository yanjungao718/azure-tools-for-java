/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.azureexplorer.helpers;


import java.util.Collections;

import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.resource.task.CreateResourceGroupTask;
import com.microsoft.azure.toolkit.lib.storage.AzureStorageAccount;
import com.microsoft.azure.toolkit.lib.storage.StorageAccount;
import com.microsoft.azure.toolkit.lib.storage.StorageAccountDraft;
import com.microsoft.azure.toolkit.lib.storage.model.StorageAccountConfig;
import com.microsoft.azuretools.ActionConstants;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetrywrapper.ErrorType;
import com.microsoft.azuretools.telemetrywrapper.EventType;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import com.microsoft.azuretools.telemetrywrapper.Operation;
import com.microsoft.azuretools.telemetrywrapper.TelemetryManager;

public class CreateStorageAccountTask {

    private final StorageAccountConfig config;

    public CreateStorageAccountTask(StorageAccountConfig config) {
        this.config = config;
    }

    public StorageAccount execute() {
        final Operation operation = TelemetryManager.createOperation(ActionConstants.StorageAccount.CREATE);
        try {
            operation.start();
            final String subscriptionId = config.getSubscription().getId();
            EventUtil.logEvent(EventType.info, operation, Collections.singletonMap(TelemetryConstants.SUBSCRIPTIONID, subscriptionId));
            // create resource group if necessary.
            new CreateResourceGroupTask(subscriptionId, config.getResourceGroup().getName(), config.getRegion()).execute();

            // create storage account
            final AzureStorageAccount az = Azure.az(AzureStorageAccount.class);
            final StorageAccountDraft draft = az.accounts(config.getSubscriptionId()).create(config.getName(), config.getResourceGroupName());
            draft.setConfig(config);
            return draft.commit();
        } catch (final RuntimeException e) {
            EventUtil.logError(operation, ErrorType.systemError, e, null, null);
            throw e;
        } finally {
            operation.complete();
        }
    }

}
