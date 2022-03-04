/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.helpers.azure.sdk;

import com.google.common.base.Strings;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerProperties;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.BlockEntry;
import com.microsoft.azure.storage.blob.BlockSearchMode;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ContainerListingDetails;
import com.microsoft.azure.storage.core.Base64;
import com.microsoft.azure.storage.core.Utility;
import com.microsoft.azuretools.azurecommons.helpers.AzureCmdException;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import com.microsoft.azuretools.utils.StorageAccoutUtils;
import com.microsoft.tooling.msservices.helpers.CallableSingleArg;
import com.microsoft.tooling.msservices.model.storage.BlobContainer;
import com.microsoft.tooling.msservices.model.storage.ClientStorageAccount;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class StorageClientSDKManager {
    private static StorageClientSDKManager apiManager;

    private StorageClientSDKManager() {
    }

    @NotNull
    public static StorageClientSDKManager getManager() {
        if (apiManager == null) {
            apiManager = new StorageClientSDKManager();
        }

        return apiManager;
    }

    @NotNull
    public ClientStorageAccount getStorageAccount(@NotNull String connectionString) {
        HashMap<String, String> settings = Utility.parseAccountString(connectionString);

        String name = settings.containsKey(ClientStorageAccount.ACCOUNT_NAME_KEY) ?
                settings.get(ClientStorageAccount.ACCOUNT_NAME_KEY) : "";

        ClientStorageAccount storageAccount = new ClientStorageAccount(name);

        if (settings.containsKey(ClientStorageAccount.ACCOUNT_KEY_KEY)) {
            storageAccount.setPrimaryKey(settings.get(ClientStorageAccount.ACCOUNT_KEY_KEY));
        }

        if (settings.containsKey(ClientStorageAccount.DEFAULT_ENDPOINTS_PROTOCOL_KEY)) {
            storageAccount.setUseCustomEndpoints(false);
            storageAccount.setProtocol(settings.get(ClientStorageAccount.DEFAULT_ENDPOINTS_PROTOCOL_KEY));
        } else {
            storageAccount.setUseCustomEndpoints(true);

            if (settings.containsKey(ClientStorageAccount.BLOB_ENDPOINT_KEY)) {
                storageAccount.setBlobsUri(settings.get(ClientStorageAccount.BLOB_ENDPOINT_KEY));
            }

            if (settings.containsKey(ClientStorageAccount.QUEUE_ENDPOINT_KEY)) {
                storageAccount.setQueuesUri(settings.get(ClientStorageAccount.QUEUE_ENDPOINT_KEY));
            }

            if (settings.containsKey(ClientStorageAccount.TABLE_ENDPOINT_KEY)) {
                storageAccount.setTablesUri(settings.get(ClientStorageAccount.TABLE_ENDPOINT_KEY));
            }
        }

        return storageAccount;
    }

    @NotNull
    public List<BlobContainer> getBlobContainers(@NotNull String connectionString)
            throws AzureCmdException {
        return getBlobContainers(connectionString, null);
    }

    public List<BlobContainer> getBlobContainers(@NotNull String connectionString, @Nullable BlobRequestOptions options)
            throws AzureCmdException {
        List<BlobContainer> bcList = new ArrayList<BlobContainer>();

        try {
            CloudBlobClient client = getCloudBlobClient(connectionString);
            for (CloudBlobContainer container : client.listContainers(null, ContainerListingDetails.ALL, options, null)) {
                String uri = container.getUri() != null ? container.getUri().toString() : "";
                String eTag = "";
                Calendar lastModified = new GregorianCalendar();
                BlobContainerProperties properties = container.getProperties();

                if (properties != null) {
                    eTag = Strings.nullToEmpty(properties.getEtag());

                    if (properties.getLastModified() != null) {
                        lastModified.setTime(properties.getLastModified());
                    }
                }

                String publicReadAccessType = "";
                BlobContainerPermissions blobContainerPermissions = container.downloadPermissions();

                if (blobContainerPermissions != null && blobContainerPermissions.getPublicAccess() != null) {
                    publicReadAccessType = blobContainerPermissions.getPublicAccess().toString();
                }

                bcList.add(new BlobContainer(Strings.nullToEmpty(container.getName()),
                        uri,
                        eTag,
                        lastModified,
                        publicReadAccessType));
            }

            return bcList;
        } catch (Throwable t) {
            throw new AzureCmdException("Error retrieving the Blob Container list", t);
        }

    }

    public void uploadBlobFileContent(@NotNull String connectionString,
                                      @NotNull BlobContainer blobContainer,
                                      @NotNull String filePath,
                                      @NotNull InputStream content,
                                      CallableSingleArg<Void, Long> processBlock,
                                      long maxBlockSize,
                                      long length)
            throws AzureCmdException {
        try {
            CloudBlobClient client = getCloudBlobClient(connectionString);
            String containerName = blobContainer.getName();

            CloudBlobContainer container = client.getContainerReference(containerName);
            final CloudBlockBlob blob = container.getBlockBlobReference(filePath);
            long uploadedBytes = 0;

            ArrayList<BlockEntry> blockEntries = new ArrayList<BlockEntry>();

            while (uploadedBytes < length) {
                String blockId = Base64.encode(UUID.randomUUID().toString().getBytes());
                BlockEntry entry = new BlockEntry(blockId, BlockSearchMode.UNCOMMITTED);

                long blockSize = maxBlockSize;
                if (length - uploadedBytes <= maxBlockSize) {
                    blockSize = length - uploadedBytes;
                }

                if (processBlock != null) {
                    processBlock.call(uploadedBytes);
                }

                entry.setSize(blockSize);

                blockEntries.add(entry);
                blob.uploadBlock(entry.getId(), content, blockSize);
                uploadedBytes += blockSize;
            }

            blob.commitBlockList(blockEntries);

        } catch (Throwable t) {
            throw new AzureCmdException("Error uploading the Blob File content", t);
        }
    }

    public static String getEndpointSuffix() {
        return StorageAccoutUtils.getEndpointSuffix();
    }

    @NotNull
    public static CloudStorageAccount getCloudStorageAccount(@NotNull String connectionString) throws URISyntaxException, InvalidKeyException {
        return CloudStorageAccount.parse(connectionString);
    }

    @NotNull
    private static CloudBlobClient getCloudBlobClient(@NotNull String connectionString) throws Exception {
        CloudStorageAccount csa = getCloudStorageAccount(connectionString);
        return csa.createCloudBlobClient();
    }
}
