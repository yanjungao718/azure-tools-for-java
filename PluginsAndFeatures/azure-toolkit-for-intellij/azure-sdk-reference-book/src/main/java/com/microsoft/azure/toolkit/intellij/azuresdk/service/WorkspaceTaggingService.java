/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.azuresdk.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.microsoft.azure.toolkit.intellij.azuresdk.model.AzureJavaSdkEntity;
import com.microsoft.azure.toolkit.intellij.azuresdk.model.WorkspaceTagEntity;
import com.microsoft.azure.toolkit.lib.common.cache.Cacheable;
import com.microsoft.azure.toolkit.lib.common.cache.Preload;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class WorkspaceTaggingService {
    private static final String SDK_METADATA_URL = "https://raw.githubusercontent.com/Azure/azure-sdk/master/_data/releases/latest/java-packages.csv";

    private static final ObjectMapper CSV_MAPPER = new CsvMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final ObjectMapper JSON_MAPPER = new JsonMapper().configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    private static final String WORKSPACE_TAG_JSON = "/workspaceTag.json";

    @Nullable
    public static String getWorkspaceTag(String groupId, final String artifactId) {
        if (StringUtils.isAnyEmpty(groupId, artifactId)) {
            return null;
        }
        return ObjectUtils.firstNonNull(getAzureArtifactTag(groupId, artifactId), getDependencyTag(groupId, artifactId));
    }

    private static String getAzureArtifactTag(final String groupId, final String artifactId) {
        try {
            return getAzureSDKArtifacts()
                .stream()
                .filter(entity -> StringUtils.isNotEmpty(entity.getType())
                    && StringUtils.equalsIgnoreCase(entity.getGroupId(), groupId)
                    && StringUtils.equalsIgnoreCase(entity.getPackageName(), artifactId))
                .map(AzureJavaSdkEntity::getType)
                .findFirst().orElse(null);
        } catch (IOException e) {
            // swallow exception for workspace tagging
            return null;
        }
    }

    private static String getDependencyTag(final String groupId, final String artifactId) {
        try {
            return getWorkspaceTagEntities()
                .stream()
                .filter(entity -> (StringUtils.equalsIgnoreCase(entity.getGroupId(), groupId) || entity.getGroupId().matches(groupId))
                    && (StringUtils.equalsIgnoreCase(entity.getArtifactId(), artifactId) || entity.getArtifactId().matches(artifactId)))
                .map(WorkspaceTagEntity::getTag)
                .findFirst().orElse(null);
        } catch (IOException e) {
            // swallow exception for workspace tagging
            return null;
        }
    }

    @Preload
    @Cacheable(value = "workspace-tag", condition = "!(force&&force[0])")
    public static List<WorkspaceTagEntity> getWorkspaceTagEntities(boolean... force) throws IOException {
        try (final InputStream stream = WorkspaceTaggingService.class.getResourceAsStream(WORKSPACE_TAG_JSON)) {
            final MappingIterator<WorkspaceTagEntity> iterator = JSON_MAPPER.readerFor(WorkspaceTagEntity.class).readValues(stream);
            return iterator.readAll();
        }
    }

    @Preload
    @Cacheable(value = "workspace-tag-azure", condition = "!(force&&force[0])")
    public static List<AzureJavaSdkEntity> getAzureSDKArtifacts(boolean... force) throws IOException {
        final URL destination = new URL(SDK_METADATA_URL);
        final CsvSchema schema = CsvSchema.emptySchema().withHeader();
        final MappingIterator<AzureJavaSdkEntity> mappingIterator = CSV_MAPPER.readerFor(AzureJavaSdkEntity.class).with(schema).readValues(destination);
        return mappingIterator.readAll();
    }
}
