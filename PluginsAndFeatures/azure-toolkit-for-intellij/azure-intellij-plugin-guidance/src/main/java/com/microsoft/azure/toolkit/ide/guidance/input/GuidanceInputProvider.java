package com.microsoft.azure.toolkit.ide.guidance.input;

import com.microsoft.azure.toolkit.ide.guidance.Context;
import com.microsoft.azure.toolkit.ide.guidance.config.InputConfig;

import javax.annotation.Nonnull;

public interface GuidanceInputProvider {
    GuidanceInput createInputComponent(@Nonnull final InputConfig config, @Nonnull final Context context);
}
