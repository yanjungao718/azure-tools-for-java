/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common;

import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;

import javax.accessibility.AccessibleRelation;
import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.Optional;

public interface AzureFormInputComponent<T> extends AzureFormInput<T> {
    default JComponent getInputComponent() {
        return (JComponent) this;
    }

    /**
     * NOTE: don't override
     */
    @Nonnull
    @Override
    default AzureValidationInfo validateInternal(T value) {
        if (!this.getInputComponent().isEnabled() || !this.getInputComponent().isVisible()) {
            return AzureValidationInfo.success(this);
        }
        return AzureFormInput.super.validateInternal(value);
    }

    @Override
    default String getLabel() {
        final JLabel label = (JLabel) this.getInputComponent().getClientProperty(AccessibleRelation.LABELED_BY);
        return Optional.ofNullable(label).map(JLabel::getText)
            .map(t -> t.endsWith(":") ? t.substring(0, t.length() - 1) : t)
            .orElse(this.getClass().getSimpleName());
    }
}
