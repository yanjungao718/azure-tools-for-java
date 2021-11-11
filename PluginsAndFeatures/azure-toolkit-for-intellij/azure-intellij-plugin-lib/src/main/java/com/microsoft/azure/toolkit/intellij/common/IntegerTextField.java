/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common;

import com.intellij.ui.components.JBTextField;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IntegerTextField extends JBTextField implements AzureFormInputComponent<Integer> {

    @Setter
    @Getter
    private Integer minValue;
    @Getter
    @Setter
    private Integer maxValue;

    @Nullable
    @Override
    public Integer getValue() {
        final String text = getText();
        return (StringUtils.isNotEmpty(text) && StringUtils.isNumeric(text)) ? Integer.valueOf(getText()) : null;
    }

    @Override
    public void setValue(final Integer val) {
        setText(val == null ? StringUtils.EMPTY : String.valueOf(val));
    }

    @Nonnull
    @Override
    public AzureValidationInfo doValidate(Integer value) {
        if ((minValue != null && value < minValue) || (maxValue != null && value > maxValue)) {
            return AzureValidationInfo.error(String.format("Value should be in range [%d, %d]", minValue, maxValue), this);
        } else {
            return AzureValidationInfo.success(this);
        }
    }
}
