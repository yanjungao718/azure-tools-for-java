/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.ui;

import com.google.gson.JsonSyntaxException;
import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.spellchecker.ui.SpellCheckingEditorCustomization;
import com.intellij.ui.EditorCustomization;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.EditorTextFieldProvider;
import com.intellij.ui.SoftWrapsEditorCustomization;
import com.microsoft.azure.toolkit.intellij.common.TextDocumentListenerAdapter;
import com.microsoft.azure.toolkit.lib.auth.model.AuthConfiguration;
import com.microsoft.azure.toolkit.lib.auth.model.AuthType;
import com.microsoft.azuretools.azurecommons.util.FileUtil;
import com.microsoft.azuretools.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ServicePrincipalLoginDialog extends DialogWrapper {
    private JTextField clientIdTextField;
    private JTextField tenantIdTextField;
    private JPanel rootPanel;
    private EditorTextField jsonDataEditor;
    private JRadioButton passwordRadioButton;
    private JPasswordField keyPasswordField;
    private JRadioButton certificateRadioButton;
    private TextFieldWithBrowseButton certFileTextField;
    private AzureCommentLabel comment;
    private final Project project;
    private String memo; // save current json to avoid infinite update loops
    private boolean intermediateState = false;

    private static final String GUID_REGEX = "^[{]?[0-9a-fA-F]{8}" + "-([0-9a-fA-F]{4}-)" + "{3}[0-9a-fA-F]{12}[}]?$";

    protected ServicePrincipalLoginDialog(@Nullable Project project) {
        super(project, true);
        this.project = project;
        init();
        $$$setupUI$$$(); // tell IntelliJ to call createUIComponents() here.
        this.setTitle("Sign in Service Principal");
        super.setOKButtonText("Sign In");

        pasteFromClipboard();
        uiTextComponents2Json();

        // initialize cert file select
        FileChooserDescriptor pem = FileChooserDescriptorFactory.createSingleFileDescriptor("pem");
        pem.withFileFilter(file -> StringUtils.equalsIgnoreCase(file.getExtension(), "pem"));
        certFileTextField.addActionListener(new ComponentWithBrowseButton.BrowseFolderActionListener<>("Select Certificate File", null, certFileTextField, null,
                pem, TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT) {
            @Nullable
            protected VirtualFile getInitialFile() {
                return LocalFileSystem.getInstance().findFileByPath(FileUtil.getDirectoryWithinUserHome("/").toString());
            }
        });

        Stream.of(clientIdTextField, tenantIdTextField, keyPasswordField, certFileTextField.getTextField()).map(JTextComponent::getDocument)
                .forEach(document -> document.addDocumentListener(new TextDocumentListenerAdapter() {
                    @Override
                    public void onDocumentChanged() {
                        uiTextComponents2Json();
                    }
                }));

        this.certificateRadioButton.addActionListener((e) -> uiTextComponents2Json());
        this.passwordRadioButton.addActionListener((e) -> uiTextComponents2Json());

        this.jsonDataEditor.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                if (jsonDataEditor.getText().equals(memo)) {
                    return;
                }
                json2UIComponents(jsonDataEditor.getText());
            }
        });
        certificateRadioButton.addActionListener(e -> syncComponentStatusWhenRadioButtonChanges());
        passwordRadioButton.addActionListener(e -> syncComponentStatusWhenRadioButtonChanges());
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return rootPanel;
    }

    @Override
    protected @NotNull List<ValidationInfo> doValidateAll() {
        List<ValidationInfo> res = new ArrayList<>();
        AuthConfiguration auth = getData();
        if (StringUtils.isBlank(auth.getTenant())) {
            res.add(new ValidationInfo("tenant is required.", tenantIdTextField));
        }
        if (!isGuid(auth.getTenant())) {
            res.add(new ValidationInfo("tenant should be a guild.", tenantIdTextField));
        }

        if (StringUtils.isBlank(auth.getClient())) {
            res.add(new ValidationInfo("clientId(appId) is required.", clientIdTextField));
        }
        if (!isGuid(auth.getClient())) {
            res.add(new ValidationInfo("clientId(appId) should be a guild.", clientIdTextField));
        }

        if (this.passwordRadioButton.isSelected()) {
            if (StringUtils.isBlank(auth.getKey())) {
                res.add(new ValidationInfo("Password is required.", keyPasswordField));
            }
        } else {
            if (StringUtils.isBlank(auth.getCertificate())) {
                res.add(new ValidationInfo("Please select a cert file.", certFileTextField));
            } else if (!new File(auth.getCertificate()).exists()) {
                res.add(new ValidationInfo(String.format("Cannot find cert file(%s).", certFileTextField.getText()), certFileTextField));
            }
        }
        return res;
    }

    private void syncComponentStatusWhenRadioButtonChanges() {
        certFileTextField.setEnabled(certificateRadioButton.isSelected());
        keyPasswordField.setEnabled(passwordRadioButton.isSelected());
    }

    private void createUIComponents() {
        this.jsonDataEditor = this.buildCodeViewer();
        this.comment = new AzureCommentLabel("You can copy the JSON result of 'az sp create ...' and paste it here");
    }

    private EditorTextField buildCodeViewer() {
        EditorTextFieldProvider service = ApplicationManager.getApplication().getService(EditorTextFieldProvider.class);
        Set<EditorCustomization> editorFeatures = new HashSet<>();
        editorFeatures.add(SoftWrapsEditorCustomization.ENABLED);
        editorFeatures.add(SpellCheckingEditorCustomization.getInstance(false));
        EditorTextField editorTextField = service.getEditorField(Objects.requireNonNull(Language.findLanguageByID("JSON")), project, editorFeatures);
        editorTextField.setMinimumSize(new Dimension(200, 100));
        return editorTextField;
    }

    public AuthConfiguration getData() {
        AuthConfiguration auth = new AuthConfiguration();

        auth.setClient(clientIdTextField.getText());
        auth.setTenant(tenantIdTextField.getText());
        if (passwordRadioButton.isSelected()) {
            auth.setKey(String.valueOf(keyPasswordField.getPassword()));
        } else {
            auth.setCertificate(this.certFileTextField.getText());
        }
        auth.setType(AuthType.SERVICE_PRINCIPAL);
        return auth;
    }

    private void pasteFromClipboard() {
        String textFromClip = findTextInClipboard(str ->
                StringUtils.contains(str, "appId") && StringUtils.contains(str, "tenant") && StringUtils.contains(str, "password")
        );
        if (StringUtils.isNotBlank(textFromClip)) {
            json2UIComponents(textFromClip);
        }
    }

    @javax.annotation.Nullable
    public static String findTextInClipboard(Predicate<String> func) {
        if (func == null) {
            return null;
        }
        for (Transferable currentItem : CopyPasteManager.getInstance().getAllContents()) {
            if (currentItem.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    String itemStr = currentItem.getTransferData(DataFlavor.stringFlavor).toString();
                    if (func.test(itemStr)) {
                        return itemStr;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private void uiTextComponents2Json() {
        if (intermediateState) {
            return;
        }
        Map<String, String> map = new LinkedHashMap<>();
        AuthConfiguration auth = getData();

        if (this.certificateRadioButton.isSelected()) {
            map.put("fileWithCertAndPrivateKey", auth.getCertificate());
        } else {
            String password = StringUtils.isNotBlank(auth.getKey()) ? "<hidden>" : "<empty>";
            map.put("password", password);
        }
        map.put("appId", auth.getClient());
        map.put("tenant", auth.getTenant());
        String text = JsonUtils.getGson().toJson(map);
        memo = text;
        this.jsonDataEditor.setText(text);
        this.jsonDataEditor.setCaretPosition(0);
    }

    private void json2UIComponents(String json) {
        intermediateState = true;
        try {
            if (StringUtils.isNotBlank(json)) {
                try {
                    Map<String, String> map = new HashMap<>();
                    map = JsonUtils.fromJson(json, map.getClass());
                    if (map != null) {
                        if (map.containsKey("appId")) {
                            this.clientIdTextField.setText(StringUtils.defaultString(map.get("appId")));
                        }

                        if (map.containsKey("tenant")) {
                            this.tenantIdTextField.setText(StringUtils.defaultString(map.get("tenant")));
                        }

                        if (map.containsKey("password") && !isPlaceHolder(map.get("password"))) {
                            this.passwordRadioButton.setSelected(true);
                            this.keyPasswordField.setText(StringUtils.defaultString(map.get("password")));
                        }

                        if (map.containsKey("fileWithCertAndPrivateKey")) {
                            this.certificateRadioButton.setSelected(true);
                            this.certFileTextField.setText(StringUtils.defaultString(map.get("fileWithCertAndPrivateKey")));
                        }
                    }

                } catch (JsonSyntaxException ex) {
                    // ignore all json errors
                }
            }
        } finally {
            intermediateState = false;
        }
    }

    private static boolean isPlaceHolder(String password) {
        return Arrays.asList("<hidden>", "<empty>").contains(password);
    }

    private static boolean isGuid(String str) {
        final Pattern p = Pattern.compile(GUID_REGEX);
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return p.matcher(str).matches();
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    void $$$setupUI$$$() {
    }
}
