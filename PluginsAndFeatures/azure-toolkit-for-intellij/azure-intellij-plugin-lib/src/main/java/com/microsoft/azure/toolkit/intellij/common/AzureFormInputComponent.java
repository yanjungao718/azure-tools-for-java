/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common;

import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;

import javax.swing.*;

public interface AzureFormInputComponent<T> extends AzureFormInput<T> {
    default JComponent getInputComponent() {
        return (JComponent) this;
    }

    default void setValidationInfo(AzureValidationInfo info) {
        this.set("validationInfo", info);
    }

    default AzureValidationInfo getValidationInfo() {
        return this.get("validationInfo");
    }
}
