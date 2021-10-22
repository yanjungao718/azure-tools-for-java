/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common;

import com.google.common.collect.ImmutableMap;
import com.intellij.icons.AllIcons;
import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.utils.Debouncer;
import com.microsoft.azure.toolkit.lib.common.utils.TailingDebouncer;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Function;

public class AzureTextInput extends ExtendableTextField
    implements AzureFormInputComponent<String>, TextDocumentListenerAdapter {
    protected static final int DEBOUNCE_DELAY = 500;
    private final Debouncer debouncer;

    private static final Extension VALIDATING = Extension.create(AnimatedIcon.Default.INSTANCE, "Validating", null);
    private static final Extension SUCCESS = Extension.create(AllIcons.General.InspectionsOK, "Valid", null);
    private static final Map<AzureValidationInfo.Type, Function<AzureValidationInfo, Extension>> extensions = ImmutableMap.of(
        AzureValidationInfo.Type.PENDING, (i) -> VALIDATING,
        AzureValidationInfo.Type.INFO, (i) -> SUCCESS,
        AzureValidationInfo.Type.ERROR, (i) -> Extension.create(AllIcons.General.BalloonError, i.getMessage(), null),
        AzureValidationInfo.Type.WARNING, (i) -> Extension.create(AllIcons.General.BalloonWarning, i.getMessage(), null)
    );

    public AzureTextInput() {
        super();
        this.debouncer = new TailingDebouncer(() -> {
            this.fireValueChangedEvent(this.getValue());
            this.doValidate();
        }, DEBOUNCE_DELAY);
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
        final AzureValidationInfo validationInfo = AzureFormInputComponent.super.doValidate();
        this.setValidationInfo(validationInfo);
        return validationInfo;
    }

    public void revalidateValue() {
        this.setValidationInfo(AzureValidationInfo.PENDING);
        this.debouncer.debounce();
    }

    public void setValidationInfo(AzureValidationInfo info) {
        final Extension ex = extensions.getOrDefault(info.getType(), (i) -> SUCCESS).apply(info);
        this.setExtensions(ex);
    }

    public void onDocumentChanged() {
        this.revalidateValue();
    }
}
