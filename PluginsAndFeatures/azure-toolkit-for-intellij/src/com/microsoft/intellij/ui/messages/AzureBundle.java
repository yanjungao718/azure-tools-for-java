/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.ui.messages;

import com.intellij.AbstractBundle;
import com.microsoft.azure.toolkit.lib.common.operation.IAzureOperationTitle;
import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.Objects;

public class AzureBundle extends AbstractBundle {

    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
        return ourInstance.getMessage(key, params);
    }

    public static IAzureOperationTitle operation(@NotNull @PropertyKey(resourceBundle = BUNDLE) String name, @NotNull Object... params) {
        return MessageBundleBasedOperationTitle.builder().name(name).params(params).build();
    }

    @NonNls
    private static final String BUNDLE = "com.microsoft.intellij.ui.messages.messages";
    private static final AzureBundle ourInstance = new AzureBundle();

    private AzureBundle() {
        super(BUNDLE);
    }

    @Builder
    @Getter
    public static class MessageBundleBasedOperationTitle implements IAzureOperationTitle {
        private final String name;
        private final Object[] params;
        private String title;

        public String toString() {
            if (Objects.isNull(this.title)) {
                this.title = AzureBundle.ourInstance.getMessage(this.name, params);
            }
            return this.title;
        }
    }
}
