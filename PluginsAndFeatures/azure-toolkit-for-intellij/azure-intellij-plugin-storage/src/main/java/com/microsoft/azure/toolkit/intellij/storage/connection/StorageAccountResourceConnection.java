/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.storage.connection;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.connector.AzureServiceResource;
import com.microsoft.azure.toolkit.intellij.connector.Connection;
import com.microsoft.azure.toolkit.intellij.connector.ConnectionDefinition;
import com.microsoft.azure.toolkit.intellij.connector.ModuleResource;
import com.microsoft.azure.toolkit.intellij.connector.Resource;
import com.microsoft.azure.toolkit.intellij.connector.ResourceDefinition;
import com.microsoft.azure.toolkit.intellij.connector.spring.SpringSupportedConnection;
import com.microsoft.azure.toolkit.intellij.connector.spring.SpringSupportedConnectionDefinition;
import com.microsoft.azure.toolkit.lib.storage.service.StorageAccount;
import lombok.Getter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageAccountResourceConnection extends Connection<StorageAccount, String> implements SpringSupportedConnection {
    public static final Definition DEFINITION = Definition.MODULE_STORAGE;

    public StorageAccountResourceConnection(AzureServiceResource<StorageAccount> resource, ModuleResource consumer, Definition definition) {
        super(resource, consumer, definition);
    }

    protected Map<String, String> initEnv(@Nonnull final Project project) {
        final StorageAccount account = this.resource.getData();
        final String conString = account.getConnectionString();
        final HashMap<String, String> env = new HashMap<>();
        final String prefix = this.getEnvPrefix();
        env.put(prefix + "CONNECTION_STRING", conString);
        env.put(prefix + "ACCOUNT_NAME", account.name());
        env.put(prefix + "ACCOUNT_KEY", account.getKey());
        return env;
    }

    @Override
    public List<Pair<String, String>> getSpringProperties() {
        final List<Pair<String, String>> properties = new ArrayList<>();
        properties.add(new ImmutablePair<>("azure.storage.accountName", String.format("${%s}", this.getEnvPrefix() + "ACCOUNT_NAME")));
        properties.add(new ImmutablePair<>("azure.storage.accountKey", String.format("${%s}", this.getEnvPrefix() + "ACCOUNT_KEY")));
        return properties;
    }

    @Getter
    public static class Definition extends ConnectionDefinition<StorageAccount, String> implements SpringSupportedConnectionDefinition {
        public static final Definition MODULE_STORAGE = new Definition(StorageAccountResource.DEFINITION, ModuleResource.Definition.IJ_MODULE);

        public Definition(ResourceDefinition<StorageAccount> rd, ResourceDefinition<String> cd) {
            super(rd, cd);
        }

        @Nonnull
        @Override
        public Connection<StorageAccount, String> define(Resource<StorageAccount> resource, Resource<String> consumer) {
            return new StorageAccountResourceConnection((AzureServiceResource<StorageAccount>) resource, (ModuleResource) consumer, this);
        }

        @Override
        public String getKeyProperty() {
            return "azure.storage.accountName";
        }
    }
}
