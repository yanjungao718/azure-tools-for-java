/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.vm.runtarget;

import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.remote.AuthType;
import com.intellij.testFramework.LightVirtualFile;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.ConnectionData;
import com.microsoft.azure.toolkit.intellij.common.AzureTextInput;
import com.microsoft.azure.toolkit.intellij.common.component.AzureFileInput;
import com.microsoft.azure.toolkit.intellij.common.component.AzurePasswordFieldInput;
import com.microsoft.azure.toolkit.intellij.common.component.SubscriptionComboBox;
import com.microsoft.azure.toolkit.intellij.vm.creation.VMCreationDialog;
import com.microsoft.azure.toolkit.intellij.vm.creation.component.VirtualMachineComboBox;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.compute.virtualmachine.VirtualMachine;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class VirtualMachineConnectionConfigurable {
    private final Project project;
    @Getter
    private JPanel contentPanel;
    private SubscriptionComboBox cbSubscription;
    private VirtualMachineComboBox cbVirtualMachine;
    private JRadioButton rdoKeyPair;
    private JRadioButton rdoPassword;
    private AzureFileInput txtCertificate;
    private AzureTextInput txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblPassword;
    private JLabel lblKey;
    private JCheckBox cbxSavePassword;
    private AzurePasswordFieldInput passwordFieldInput;

    public VirtualMachineConnectionConfigurable(Project project) {
        this.project = project;
        this.$$$setupUI$$$();
        this.init();
    }

    public void init() {
        passwordFieldInput = new AzurePasswordFieldInput(txtPassword);
        txtUsername.setRequired(true);
        final ButtonGroup authenticationGroup = new ButtonGroup();
        authenticationGroup.add(rdoPassword);
        authenticationGroup.add(rdoKeyPair);
        cbSubscription.addItemListener(this::onSubscriptionChanged);
        cbVirtualMachine.addItemListener(this::onVirtualMachineChanged);
        rdoPassword.addItemListener(e -> toggleAuthenticationType(false));
        rdoKeyPair.addItemListener(e -> toggleAuthenticationType(true));
        rdoKeyPair.setSelected(true);

        final Path defaultKeyPath = Path.of(System.getProperty("user.home"), ".ssh", "id_rsa");
        txtCertificate.setToolTipText(defaultKeyPath.toString());
        File defaultBrowserPath = new File(System.getProperty("user.home") + File.separator + ".ssh");
        if (!defaultBrowserPath.exists()) {
            defaultBrowserPath = new File(System.getProperty("user.home"));
        }
        txtCertificate.addActionListener(new ComponentWithBrowseButton.BrowseFolderActionListener<>(
            VMCreationDialog.SELECT_CERT_TITLE,
            VMCreationDialog.SSH_PUBLIC_KEY_DESCRIPTION, txtCertificate,
            this.project,
            FileChooserDescriptorFactory.createSingleLocalFileDescriptor(),
            TextComponentAccessor.TEXT_FIELD_WHOLE_TEXT));
    }

    public void apply(@Nonnull ConnectionData connectionData, Consumer<VirtualMachine> callback) throws CommitStepException {
        final VirtualMachine vm = this.cbVirtualMachine.getValue();
        if (Objects.isNull(vm)) {
            throw new CommitStepException("no vm is selected.");
        }
        connectionData.setUseExistingConfig(false);
        connectionData.setSavePassphrase(true);
        connectionData.setOpenSshAgentConnectionState(ConnectionData.OpenSshAgentConnectionState.NOT_STARTED);
        connectionData.setUsername(txtUsername.getValue());
        if (rdoPassword.isSelected()) {
            connectionData.setAuthType(AuthType.PASSWORD);
            connectionData.setPassword(String.valueOf(txtPassword.getPassword()));
            connectionData.setSavePassword(cbxSavePassword.isSelected());
        } else if (rdoKeyPair.isSelected()) {
            connectionData.setAuthType(AuthType.KEY_PAIR);
            connectionData.setPrivateKey(txtCertificate.getValue());
            if (StringUtils.isBlank(txtCertificate.getValue())) {
                connectionData.setPrivateKey(System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "id_rsa");
            }
        }
        AzureTaskManager.getInstance().runOnPooledThread(() -> {
            final String hostIp = vm.getHostIp();
            if (StringUtils.isBlank(hostIp)) {
                AzureMessager.getMessager().alert("ssh is not enabled for the selected vm.");
                return;
            }
            connectionData.setHost(Optional.of(hostIp).orElse(""));
            callback.accept(vm);
            connectionData.checkAgentConnection(project, ModalityState.any());
        });
    }

    private void toggleAuthenticationType(boolean isSSH) {
        txtPassword.setVisible(!isSSH);
        lblPassword.setVisible(!isSSH);
        cbxSavePassword.setVisible(!isSSH);
        passwordFieldInput.setRequired(!isSSH);

        lblKey.setVisible(isSSH);
        txtCertificate.setVisible(isSSH);
    }

    private void onSubscriptionChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Subscription) {
            final Subscription subscription = (Subscription) e.getItem();
            this.cbVirtualMachine.setSubscription(subscription);
        }
    }

    private void onVirtualMachineChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Subscription) {
            final VirtualMachine vm = (VirtualMachine) e.getItem();
        }
    }

    public JComponent getPreferredFocusedComponent() {
        return this.cbVirtualMachine;
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    private void $$$setupUI$$$() {
    }
}
