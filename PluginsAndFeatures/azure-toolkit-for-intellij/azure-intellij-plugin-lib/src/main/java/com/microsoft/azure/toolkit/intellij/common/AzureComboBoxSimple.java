/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

@Setter
@Getter
public class AzureComboBoxSimple<T> extends AzureComboBox<T> {

    private Validator validator;

    public AzureComboBoxSimple(@Nonnull final Supplier<? extends List<? extends T>> supplier) {
        super(supplier, true);
    }
}
