package com.microsoft.azure.toolkit.ide.guidance.input;

import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;

import javax.annotation.Nonnull;
import javax.swing.*;

public interface GuidanceInput {
    String getDescription();

    @Nonnull
    abstract JComponent getComponent();

    abstract void applyResult();

    default AzureValidationInfo getValidationInfo() {
        return AzureValidationInfo.success(null);
    }

}
