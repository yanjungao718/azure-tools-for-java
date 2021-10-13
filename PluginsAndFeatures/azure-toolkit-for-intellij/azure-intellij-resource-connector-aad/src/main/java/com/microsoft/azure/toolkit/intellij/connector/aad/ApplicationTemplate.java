package com.microsoft.azure.toolkit.intellij.connector.aad;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Template for a single code file.
 * It manages placeholders {{...}} in the code template's content.
 */
@RequiredArgsConstructor
class ApplicationTemplate {
    @Nonnull
    private final String resourcePath;

    // placeholder values
    @Nonnull
    private final String tenantID;
    @Nonnull
    private final String clientID;
    @Nonnull
    private final String clientSecret;
    @Nonnull
    private final String groupNames;

    /**
     * @return The template's content with placeholders replaced with actual values.
     */
    String content() throws IOException {
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
