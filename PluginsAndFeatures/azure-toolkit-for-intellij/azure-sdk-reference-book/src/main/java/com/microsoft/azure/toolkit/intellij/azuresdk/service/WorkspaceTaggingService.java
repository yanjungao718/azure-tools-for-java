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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class WorkspaceTaggingService {
    private static final String SDK_METADATA_URL = "https://raw.githubusercontent.com/Azure/azure-sdk/master/_data/releases/latest/java-packages.csv";

    private static final ObjectMapper CSV_MAPPER = new CsvMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final ObjectMapper JSON_MAPPER = new JsonMapper().configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    private static final String WORKSPACE_TAG_JSON = "/workspaceTag.json";

    @Nullable
    public static String getWorkspaceTag(@Nonnull String groupId, @Nonnull final String artifactId) {
        if (StringUtils.isAnyEmpty(groupId, artifactId)) {
            return null;
        }
        return ObjectUtils.firstNonNull(getAzureDependencyTag(groupId, artifactId), getExternalDependencyTag(groupId, artifactId));
    }

    private static String getAzureDependencyTag(final String groupId, final String artifactId) {
        return getAzureSDKEntities()
            .stream()
            .filter(entity -> StringUtils.isNotEmpty(entity.getType())
                && StringUtils.equalsIgnoreCase(entity.getGroupId(), groupId)
                && StringUtils.equalsIgnoreCase(entity.getPackageName(), artifactId))
            .map(AzureJavaSdkEntity::getType)
            .findFirst().orElse(null);
    }

    private static String getExternalDependencyTag(final String groupId, final String artifactId) {
        return getWorkspaceTagEntities()
            .stream()
            .filter(entity -> (StringUtils.isEmpty(entity.getGroupId()) || StringUtils.equalsIgnoreCase(entity.getGroupId(), groupId))
                && (StringUtils.isEmpty(entity.getArtifactId()) || StringUtils.equalsIgnoreCase(entity.getArtifactId(), artifactId)))
            .map(WorkspaceTagEntity::getTag)
            .findFirst().orElse(null);
    }

    @Preload
    @Cacheable(value = "workspace-tag", condition = "!(force&&force[0])")
    public static List<WorkspaceTagEntity> getWorkspaceTagEntities(boolean... force) {
        try (final InputStream stream = WorkspaceTaggingService.class.getResourceAsStream(WORKSPACE_TAG_JSON)) {
            final MappingIterator<WorkspaceTagEntity> iterator = JSON_MAPPER.readerFor(WorkspaceTagEntity.class).readValues(stream);
            return iterator.readAll();
        } catch (IOException exception) {
            return Collections.emptyList();
        }
    }

    @Preload
    @Cacheable(value = "workspace-tag-azure", condition = "!(force&&force[0])")
    public static List<AzureJavaSdkEntity> getAzureSDKEntities(boolean... force) {
        try {
            final URL destination = new URL(SDK_METADATA_URL);
            final CsvSchema schema = CsvSchema.emptySchema().withHeader();
            final MappingIterator<AzureJavaSdkEntity> mappingIterator = CSV_MAPPER.readerFor(AzureJavaSdkEntity.class).with(schema).readValues(destination);
            return mappingIterator.readAll();
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}
