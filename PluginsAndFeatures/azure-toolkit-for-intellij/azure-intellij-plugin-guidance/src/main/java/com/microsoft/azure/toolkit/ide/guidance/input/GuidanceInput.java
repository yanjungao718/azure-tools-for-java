package com.microsoft.azure.toolkit.ide.guidance.input;

import javax.annotation.Nullable;
import javax.swing.*;

public interface GuidanceInput {
    public String getDescription();

    @Nullable
    public abstract JComponent getComponent();

    public abstract void applyResult();

}
