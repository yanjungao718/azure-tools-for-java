/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.link.mysql;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Objects;

public class JdbcUrlUtils {

    public static JdbcUrl parseUrl(String jdbcUrl) {
        Preconditions.checkArgument(StringUtils.startsWith(jdbcUrl, "jdbc:"), "jdbcUrl is not valid.");
        URI uri = URI.create(jdbcUrl.substring(5));
        String hostname = Objects.nonNull(uri) ? uri.getHost() : null;
        Integer port = Objects.nonNull(uri) ? uri.getPort() : null;
        String path = Objects.nonNull(uri) ? uri.getPath() : null;
        String database = StringUtils.startsWith(path, "/") ? path.substring(1) : path;
        return new JdbcUrl(hostname, port, database);
    }

    @Getter
    @AllArgsConstructor
    public static class JdbcUrl {
        private String hostname;
        private Integer port;
        private String database;
    }

}
