/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.aad;

import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.graph.models.Application;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

/**
 * Combobox listing Azure AD applications.
 */
class AzureApplicationComboBox extends AzureComboBox<Application> {
    private final Supplier<List<Application>> supplier;

    /**
     * Create a new application combo box.
     *
     * @param supplier Supplies the applications to show. This allows to adjust the applications to show,
     *                 depending on the context.
     */
    AzureApplicationComboBox(@NotNull Supplier<List<Application>> supplier) {
        super(false);
        this.supplier = supplier;

        this.setEditable(false);
    }

    @Override
    protected String getItemText(Object item) {
        if (item instanceof Application) {
            return ((Application) item).displayName;
        }
        return super.getItemText(item);
    }

    @NotNull
    @Override
    protected List<? extends Application> loadItems() throws Exception {
        return supplier.get();
    }
}
