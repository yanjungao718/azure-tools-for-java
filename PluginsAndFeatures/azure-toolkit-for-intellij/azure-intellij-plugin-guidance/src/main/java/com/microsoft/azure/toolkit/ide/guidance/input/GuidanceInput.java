package com.microsoft.azure.toolkit.ide.guidance.input;

import javax.annotation.Nonnull;
import javax.swing.*;

public interface GuidanceInput {
    public String getDescription();

    @Nonnull
    public abstract JComponent getComponent();

    public abstract void applyResult();

}
