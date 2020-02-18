/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.intellij.runner.functions.library;

public interface IProviderContext {
    void registerProvider(Class<? extends Object> clazz, Object provider);

    <T> T getProvider(Class<T> clazz);
}
