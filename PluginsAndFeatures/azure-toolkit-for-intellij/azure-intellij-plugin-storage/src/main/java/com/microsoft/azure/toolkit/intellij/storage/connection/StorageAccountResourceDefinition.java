/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.storage.connection;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.AzureFormJPanel;
import com.microsoft.azure.toolkit.intellij.connector.AzureServiceResource;
import com.microsoft.azure.toolkit.intellij.connector.spring.SpringSupported;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.storage.service.AzureStorageAccount;
import com.microsoft.azure.toolkit.lib.storage.service.StorageAccount;
import lombok.Getter;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class StorageAccountResourceDefinition extends AzureServiceResource.Definition<StorageAccount> implements SpringSupported<StorageAccount> {
    public static final StorageAccountResourceDefinition INSTANCE = new StorageAccountResourceDefinition();

    private StorageAccountResourceDefinition() {
        super("Azure.Storage", "Azure Storage Account", "/icons/StorageAccount/StorageAccount.svg");
    }

    @Override
    public Map<String, String> initEnv(StorageAccount account, Project project) {
        final String conString = account.getConnectionString();
        final HashMap<String, String> env = new HashMap<>();
        env.put("%ENV_PREFIX%_CONNECTION_STRING", conString);
        env.put("%ENV_PREFIX%_ACCOUNT_NAME", account.name());
        env.put("%ENV_PREFIX%_ACCOUNT_KEY", account.getKey());
        return env;
    }

    @Override
    public List<Pair<String, String>> getSpringProperties() {
        final List<Pair<String, String>> properties = new ArrayList<>();
        properties.add(new MutablePair<>("azure.storage.accountName", "${%ENV_PREFIX%_ACCOUNT_NAME}"));
        properties.add(new MutablePair<>("azure.storage.accountKey", "${%ENV_PREFIX%_ACCOUNT_KEY}"));
        final String blobEndpoint = "https://${%ENV_PREFIX%_ACCOUNT_NAME}.blob.core.windows.net/<your-container-name>/<your-blob-name>";
        final String fileEndpoint = "https://${%ENV_PREFIX%_ACCOUNT_NAME}.file.core.windows.net/<your-fileshare-name>/<your-file-name>";
        properties.add(new MutablePair<>("# azure.storage.blob-endpoint", blobEndpoint));
        properties.add(new MutablePair<>("# azure.storage.file-endpoint", fileEndpoint));
        return properties;
    }

    @Override
    public StorageAccount getResource(String dataId) {
        return Azure.az(AzureStorageAccount.class).get(dataId);
    }

    @Override
    public AzureFormJPanel<StorageAccount> getResourcePanel(Project project) {
        return new StorageAccountResourcePanel();
    }
}
