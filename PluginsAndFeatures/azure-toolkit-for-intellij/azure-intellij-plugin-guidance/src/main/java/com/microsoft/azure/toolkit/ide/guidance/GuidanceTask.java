/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.guidance;

import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessager;
import com.microsoft.azure.toolkit.lib.common.operation.OperationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface GuidanceTask {

    void execute() throws Exception;

    default void init() {
    }

    default boolean isDone() {
        return false;
    }

    @Nonnull
    String getName();

    @Nullable
    default String getDescription() {
        return this.getName() + "desc";
    }

    @Nullable
    default String getDoc() {
        return null;
    }

    default void execute(IAzureMessager messager) throws Exception {
        final IAzureMessager currentMessager = AzureMessager.getMessager();
        OperationContext.current().setMessager(messager);
        try {
            execute();
        } finally {
            OperationContext.current().setMessager(currentMessager);
        }
    }
}
