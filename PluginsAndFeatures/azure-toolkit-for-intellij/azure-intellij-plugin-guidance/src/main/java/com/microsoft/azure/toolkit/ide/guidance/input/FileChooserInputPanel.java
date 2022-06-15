package com.microsoft.azure.toolkit.ide.guidance.input;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.microsoft.azure.toolkit.intellij.common.component.AzureFileInput;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import lombok.Getter;

import javax.swing.*;

public class FileChooserInputPanel {
    public static final String SELECT_PATH_TO_SAVE_THE_PROJECT = "Select path to save the project";
    public static final String PATH_TO_SAVE_THE_DEMO_PROJECT = "Please select the target path to save the demo project";
    @Getter
    private JPanel rootPanel;
    private AzureFileInput fileInput;

    public FileChooserInputPanel() {
        $$$setupUI$$$();
        init();
    }

    private void init() {
        fileInput.addActionListener(new ComponentWithBrowseButton.BrowseFolderActionListener<>(SELECT_PATH_TO_SAVE_THE_PROJECT, PATH_TO_SAVE_THE_DEMO_PROJECT, fileInput,
                null, FileChooserDescriptorFactory.createSingleFolderDescriptor(), TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT));
    }

    public String getValue() {
        return fileInput.getValue();
    }

    public void setValue(final String value) {
        this.fileInput.setValue(value);
    }

    public AzureValidationInfo getValidationInfo() {
        return this.fileInput.getValidationInfo();
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    void $$$setupUI$$$() {
    }
}
