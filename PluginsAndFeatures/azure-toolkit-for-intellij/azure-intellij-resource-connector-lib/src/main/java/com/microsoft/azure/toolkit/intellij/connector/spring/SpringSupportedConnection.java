/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.spring;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface SpringSupportedConnection {
    List<Pair<String, String>> getSpringProperties();
}
