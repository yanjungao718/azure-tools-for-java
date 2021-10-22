/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.common.component;

import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.microsoft.azure.toolkit.intellij.common.AzureFormInputComponent;

public class AzureFileInput extends TextFieldWithBrowseButton implements AzureFormInputComponent<String> {
    @Override
    public String getValue() {
        return this.getText();
    }

    @Override
    public void setValue(String val) {
        this.setText(val);
    }
}
