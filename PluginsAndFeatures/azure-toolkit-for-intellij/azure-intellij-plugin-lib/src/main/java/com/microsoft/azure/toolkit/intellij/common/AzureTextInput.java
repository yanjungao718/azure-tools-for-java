/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common;

import com.intellij.icons.AllIcons;
import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.utils.Debouncer;
import com.microsoft.azure.toolkit.lib.common.utils.TailingDebouncer;

import javax.annotation.Nonnull;

public class AzureTextInput extends ExtendableTextField
    implements AzureFormInputComponent<String>, TextDocumentListenerAdapter {
    protected static final int DEBOUNCE_DELAY = 500;
    private AzureValidationInfo validationInfo;
    private final Debouncer validator;
    private final Extension spinner = Extension.create(new AnimatedIcon.Default(), "Validating", null);
    private final Extension invalid = Extension.create(AllIcons.General.BalloonError, "Invalid", null);
    private final Extension valid = Extension.create(AllIcons.General.InspectionsOK, "Valid", null);

    public AzureTextInput() {
        super();
        this.validator = new TailingDebouncer(() -> this.validationInfo = this.doValidate(), DEBOUNCE_DELAY);
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

    @Nonnull
    @Override
    public final AzureValidationInfo doValidate() {
        AzureValidationInfo info = this.validationInfo;
        if (this.validator.isPending()) {
            info = AzureValidationInfo.PENDING;
        } else if (this.validationInfo == null) {
            this.validationInfo = AzureFormInputComponent.super.doValidate();
            info = this.validationInfo;
        }
        if (info == AzureValidationInfo.PENDING) {
            this.setExtensions(spinner);
        } else if (info == AzureValidationInfo.OK) {
            this.setExtensions(valid);
        } else if (info != AzureValidationInfo.UNINITIALIZED) {
            this.setExtensions(invalid);
        }
        return info;
    }

    protected void revalidateValue() {
        this.validator.debounce();
    }

    public void onDocumentChanged() {
        this.revalidateValue();
    }
}
