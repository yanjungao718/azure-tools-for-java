/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.azuresdk.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.microsoft.azure.toolkit.intellij.azuresdk.model.AzureJavaSdkEntity;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AzureJavaSdkService {

    private static final ObjectMapper mapper = new CsvMapper();
    private static final String SDK_METADATA_URL = "https://raw.githubusercontent.com/Azure/azure-sdk/master/_data/releases/latest/java-packages.csv";

    private List<AzureJavaSdkEntity> artifacts = new ArrayList<>();

    public static AzureJavaSdkService getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void reloadAzureJavaSDKArtifacts() throws IOException {
        final URL destination = new URL(SDK_METADATA_URL);
        final CsvSchema schema = CsvSchema.emptySchema().withHeader();
        final MappingIterator<AzureJavaSdkEntity> mappingIterator = mapper.readerFor(AzureJavaSdkEntity.class).with(schema).readValues(destination);
        synchronized (this) {
            artifacts = mappingIterator.readAll();
        }
    }

    public List<AzureJavaSdkEntity> getAzureSDKArtifacts() {
        return Collections.unmodifiableList(artifacts);
    }

    private static class LazyHolder {
        static final AzureJavaSdkService INSTANCE = new AzureJavaSdkService();
    }
}
