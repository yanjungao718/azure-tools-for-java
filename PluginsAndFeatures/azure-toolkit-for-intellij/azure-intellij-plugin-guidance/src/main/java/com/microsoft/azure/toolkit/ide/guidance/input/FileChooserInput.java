package com.microsoft.azure.toolkit.ide.guidance.input;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.microsoft.azure.toolkit.intellij.common.component.AzureFileInput;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.io.File;

public class FileChooserInput implements GuidanceInput {
    public static final String SELECT_PATH_TO_SAVE_THE_PROJECT = "Select path to save the project";
    public static final String PATH_TO_SAVE_THE_DEMO_PROJECT = "Please select the target path to save the demo project";
    public static final String FILE_CHOOSER = "file-chooser";

    private AzureFileInput input;
    private final InputContext context;

    public FileChooserInput(@Nonnull InputContext inputContext) {
        this.context = inputContext;
    }

    @Override
    public JComponent getComponent() {
        return input;
    }

    @Override
    public void applyResult() {
        input.getValue();
    }

    private void initComponent() {
        this.input = new AzureFileInput();
        input.setValue(new File(System.getProperty("user.home"), context.getGuidance().getName()).getAbsolutePath());
        input.addActionListener(new ComponentWithBrowseButton.BrowseFolderActionListener<>(SELECT_PATH_TO_SAVE_THE_PROJECT, PATH_TO_SAVE_THE_DEMO_PROJECT, input,
                null, FileChooserDescriptorFactory.createSingleFolderDescriptor(), TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT));
    }
}
