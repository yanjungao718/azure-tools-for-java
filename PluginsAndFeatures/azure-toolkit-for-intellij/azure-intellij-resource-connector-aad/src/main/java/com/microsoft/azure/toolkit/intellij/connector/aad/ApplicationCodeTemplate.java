/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.aad;

import com.intellij.util.PathUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@RequiredArgsConstructor
enum ApplicationCodeTemplate {
    ApplicationProperties("/code-templates/application.properties"),
    ApplicationMain("/code-templates/Application.java"),
    ApplicationController("/code-templates/SpringController.java");

    @Nonnull
    private final String resourcePath;

    @Nonnull
    String getFilename() {
        return PathUtil.getFileName(resourcePath);
    }

    /**
     * @return The template's content with placeholders replaced with actual values.
     */
    String render(@Nonnull String tenantID, @Nonnull String clientID, @Nonnull String clientSecret, @Nonnull String groupNames) throws IOException {
        try (var stream = getClass().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IOException("unable to locate code template " + resourcePath);
            }

            var code = IOUtils.toString(stream, StandardCharsets.UTF_8);
            code = code.replaceAll(Pattern.quote("{{tenantID}}"), tenantID);
            code = code.replaceAll(Pattern.quote("{{clientID}}"), clientID);
            code = code.replaceAll(Pattern.quote("{{clientSecret}}"), clientSecret);
            code = code.replaceAll(Pattern.quote("{{groupNames}}"), groupNames);

            return code;
        }
    }
}
