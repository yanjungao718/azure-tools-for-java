/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common.operation;

import com.intellij.CommonBundle;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class IntellijAzureOperationTitleProvider implements AzureOperationBundle.Provider {

    private static final Map<String, Optional<ResourceBundle>> libBundles = new ConcurrentHashMap<>();
    private static final Map<String, Optional<ResourceBundle>> intellijBundles = new ConcurrentHashMap<>();
    private static final IntellijAzureOperationTitleProvider INSTANCE = new IntellijAzureOperationTitleProvider();

    @Override
    public @NotNull String getMessage(@NotNull final String key, final Object @NotNull ... params) {
        final String notFound = String.format("!%s!", key);
        final String subGroup = key.split("\\.")[0].replaceAll("\\|", "_");
        final String supGroup = key.split("[|.]")[0];
        final ArrayList<Supplier<String>> suppliers = new ArrayList<>();
        suppliers.add(() -> this.getIjOperationTitle(supGroup, key, params));
        suppliers.add(() -> this.getIjOperationTitle(subGroup, key, params));
        suppliers.add(() -> this.getLibOperationTitle(supGroup, key, params));
        suppliers.add(() -> this.getLibOperationTitle(subGroup, key, params));
        for (final Supplier<String> supplier : suppliers) {
            final String title = supplier.get();
            if (Objects.nonNull(title)) {
                return title;
            }
        }
        return notFound;
    }

    public String getLibOperationTitle(@Nonnull final String group, @NotNull final String key, final Object @NotNull ... params) {
        return libBundles.computeIfAbsent(group, k -> {
            final String bundleName = String.format("com.microsoft.azure.toolkit.operation.titles_%s", group);
            return Optional.ofNullable(ResourceBundle.getBundle(bundleName));
        }).map(b -> CommonBundle.messageOrNull(b, key, params)).orElse(null);
    }

    public String getIjOperationTitle(@Nonnull final String group, @NotNull final String key, final Object @NotNull ... params) {
        return libBundles.computeIfAbsent(group, k -> {
            final String bundleName = String.format("com.microsoft.azure.toolkit.operation.titles_%s_intellij", group);
            return Optional.ofNullable(ResourceBundle.getBundle(bundleName));
        }).map(b -> CommonBundle.messageOrNull(b, key, params)).orElse(null);
    }

    public static void register() {
        AzureOperationBundle.register(INSTANCE);
    }
}
