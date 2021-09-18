/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.spring;

import com.microsoft.azure.toolkit.intellij.connector.Connection;
import com.microsoft.azure.toolkit.intellij.connector.ResourceDefinition;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;

public interface SpringSupported<T> extends ResourceDefinition<T> {
    static List<Pair<String, String>> getProperties(Connection<?, ?> c) {
        final ResourceDefinition<?> rd = c.getResource().getDefinition();
        if (rd instanceof SpringSupported) {
            final List<Pair<String, String>> properties = ((SpringSupported<?>) rd).getSpringProperties();
            properties.forEach(p -> p.setValue(p.getValue().replaceAll("%ENV_PREFIX%", c.getEnvPrefix())));
            return properties;
        }
        return Collections.emptyList();
    }

    List<Pair<String, String>> getSpringProperties();
}
