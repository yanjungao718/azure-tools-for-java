/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.link.mysql;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Objects;

public class JdbcUrlUtils {

    public static String parseHostname(String jdbcUrl) {
        URI uri = parseJdbcUri(jdbcUrl);
        return Objects.nonNull(uri) ? uri.getHost() : null;
    }

    public static Integer parsePort(String jdbcUrl) {
        URI uri = parseJdbcUri(jdbcUrl);
        return Objects.nonNull(uri) ? uri.getPort() : null;
    }

    public static String parseDatabase(String jdbcUrl) {
        URI uri = parseJdbcUri(jdbcUrl);
        String path = Objects.nonNull(uri) ? uri.getPath() : null;
        return StringUtils.startsWith(path, "/") ? path.substring(1) : path;
    }

    private static URI parseJdbcUri(String jdbcUrl) {
        if (!StringUtils.startsWith(jdbcUrl, "jdbc:")) {
            return null;
        }
        return URI.create(jdbcUrl.substring(5));
    }

}
