/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common;

import javax.annotation.Nullable;
import javax.swing.*;

public class AzureTextInput extends AbstractAzureTextInput<String> {
    public AzureTextInput() {
        this(null);
    }

    public AzureTextInput(@Nullable JTextField comp) {
        super(comp);
    }

    @Override
    public String getValue() {
        return this.getText();
    }

    @Override
    public void setValue(final String val) {
        this.setText(val);
    }
}
