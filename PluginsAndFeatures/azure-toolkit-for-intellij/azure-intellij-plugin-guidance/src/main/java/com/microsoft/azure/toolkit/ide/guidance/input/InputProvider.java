package com.microsoft.azure.toolkit.ide.guidance.input;

import com.microsoft.azure.toolkit.ide.guidance.Context;
import com.microsoft.azure.toolkit.ide.guidance.config.InputConfig;

import javax.annotation.Nonnull;
import javax.swing.*;

public interface InputProvider {
    JComponent getInputComponent(@Nonnull final InputConfig config, @Nonnull final Context context);
}
