/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.form;

import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface AzureFormPanel<T> extends AzureForm<T> {
    default void setVisible(boolean visible) {
        // do nothing
    }

    @Override
    default T getFormData() {
        throw new AzureToolkitRuntimeException("method not implemented");
    }

    @Override
    default List<AzureFormInput<?>> getInputs() {
        Collections.emptyList().addAll(new ArrayList<>());
        return new ArrayList<>();
    }

}

