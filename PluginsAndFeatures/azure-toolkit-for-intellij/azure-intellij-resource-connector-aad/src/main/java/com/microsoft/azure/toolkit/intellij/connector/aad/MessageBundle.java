/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.aad;

import com.intellij.AbstractBundle;
import org.jetbrains.annotations.PropertyKey;

import javax.annotation.Nonnull;

/**
 * Message bundle for the AAD features.
 */
public final class MessageBundle extends AbstractBundle {
    private static final MessageBundle ourInstance = new MessageBundle();
    private static final String BUNDLE = "messages.aadBundle";

    private MessageBundle() {
        super(BUNDLE);
    }

    public static String message(@Nonnull @PropertyKey(resourceBundle = BUNDLE) String key, @Nonnull Object... params) {
        return ourInstance.getMessage(key, params);
    }
}
