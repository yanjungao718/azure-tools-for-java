/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common.component;

import com.microsoft.azure.toolkit.intellij.common.AzureTextInput;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

public class AzurePasswordFieldInput extends AzureTextInput {

    private final JPasswordField delegate;

    public AzurePasswordFieldInput(JPasswordField delegate) {
        super(delegate);
        this.delegate = delegate;
        this.setRequired(true);
    }

    @Override
    public String getValue() {
        final char[] password = this.delegate.getPassword();
        return password != null ? String.valueOf(password) : StringUtils.EMPTY;
    }

    @Override
    public void setValue(final String val) {
        this.delegate.setText(val);
    }

    @Override
    public JPasswordField getInputComponent() {
        return delegate;
    }
}
