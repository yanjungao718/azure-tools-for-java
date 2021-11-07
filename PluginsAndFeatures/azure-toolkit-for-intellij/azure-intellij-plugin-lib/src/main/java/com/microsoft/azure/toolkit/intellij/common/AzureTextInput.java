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

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class AzureTextInput extends ExtendableTextField
    implements AzureFormInputComponent<String>, TextDocumentListenerAdapter {
    protected static final int DEBOUNCE_DELAY = 500;
    private final Debouncer debouncer;
    private static final Extension VALIDATING = Extension.create(AnimatedIcon.Default.INSTANCE, "Validating...", null);
    private static final Extension SUCCESS = Extension.create(AllIcons.General.InspectionsOK, "Validation passed.", null);
    private static final Map<AzureValidationInfo.Type, Function<AzureValidationInfo, Extension>> extensions = ImmutableMap.of(
        AzureValidationInfo.Type.PENDING, (i) -> VALIDATING,
        AzureValidationInfo.Type.SUCCESS, (i) -> SUCCESS,
        AzureValidationInfo.Type.ERROR, (i) -> Extension.create(AllIcons.General.BalloonError, i.getMessage(), null),
        AzureValidationInfo.Type.WARNING, (i) -> Extension.create(AllIcons.General.BalloonWarning, i.getMessage(), null)
    );

    public AzureTextInput() {
        this(null);
    }

    public AzureTextInput(@Nullable JTextField comp) {
        super();
        this.debouncer = new TailingDebouncer(() -> this.fireValueChangedEvent(this.getValue()), DEBOUNCE_DELAY);
        Optional.ofNullable(comp).or(() -> Optional.of(this.getInputComponent()))
            .ifPresent(t -> t.getDocument().addDocumentListener(this));
        this.trackValidation();
    }

    @Override
    public String getValue() {
        return this.getText();
    }

    @Override
    public void setValue(final String val) {
        this.setText(val);
    }

    public void setValidationInfo(AzureValidationInfo info) {
        AzureFormInputComponent.super.setValidationInfo(info);
        final Extension ex = extensions.getOrDefault(info.getType(), (i) -> SUCCESS).apply(info);
        this.setExtensions(ex);
    }

    public void onDocumentChanged() {
        this.setValidationInfo(AzureValidationInfo.pending(this));
        this.debouncer.debounce();
    }

    @Override
    public JTextField getInputComponent() {
        return this;
    }
}
