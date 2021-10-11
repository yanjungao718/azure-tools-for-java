package com.microsoft.azure.toolkit.intellij.connector.aad;

import com.intellij.util.PathUtil;

import javax.annotation.Nonnull;

enum ApplicationTemplateType {
    ApplicationProperties("/code-templates/application.properties"),
    ApplicationMain("/code-templates/Application.java"),
    ApplicationController("/code-templates/SpringController.java");

    @Nonnull
    private final String resourcePath;

    ApplicationTemplateType(@Nonnull String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @Nonnull
    String getResourcePath() {
        return resourcePath;
    }

    @Nonnull
    String getFilename() {
        return PathUtil.getFileName(resourcePath);
    }
}
