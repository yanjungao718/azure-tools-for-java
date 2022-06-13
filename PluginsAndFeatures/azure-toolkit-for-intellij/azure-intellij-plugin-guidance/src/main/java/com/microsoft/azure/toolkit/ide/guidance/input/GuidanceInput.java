package com.microsoft.azure.toolkit.ide.guidance.input;

import javax.swing.*;

public interface GuidanceInput {

    public abstract JComponent getComponent();

    public abstract void applyResult();

}
