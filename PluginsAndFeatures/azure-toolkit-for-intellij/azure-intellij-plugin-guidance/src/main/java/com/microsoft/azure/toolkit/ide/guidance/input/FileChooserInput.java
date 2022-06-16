package com.microsoft.azure.toolkit.ide.guidance.input;

import com.microsoft.azure.toolkit.ide.guidance.ComponentContext;
import com.microsoft.azure.toolkit.ide.guidance.config.InputConfig;

import javax.annotation.Nonnull;
import javax.swing.*;

public class FileChooserInput implements GuidanceInput {
    public static final String DIRECTORY = "directory";
    public static final String FILE_CHOOSER = "input.common.file-chooser";

    private FileChooserInputPanel inputPanel;
    private final ComponentContext context;
    private final InputConfig config;

    public FileChooserInput(@Nonnull InputConfig config, @Nonnull ComponentContext inputContext) {
        this.context = inputContext;
        this.config = config;
        this.inputPanel = new FileChooserInputPanel();

        this.context.addPropertyListener("value", value -> inputPanel.setValue((String) value));
    }

    @Override
    public String getDescription() {
        return config.getDescription();
    }

    @Override
    public JComponent getComponent() {
        return inputPanel.getRootPanel();
    }

    @Override
    public void applyResult() {
        context.applyResult(DIRECTORY, inputPanel.getValue());
    }
}
