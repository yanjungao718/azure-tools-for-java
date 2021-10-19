/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common;

import com.intellij.ui.components.fields.ExtendableTextField;

public class AzureTextInput extends ExtendableTextField
    implements AzureFormInputComponent<String>, TextDocumentListenerAdapter {

    public AzureTextInput() {
        super();
        this.getDocument().addDocumentListener(this);
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
