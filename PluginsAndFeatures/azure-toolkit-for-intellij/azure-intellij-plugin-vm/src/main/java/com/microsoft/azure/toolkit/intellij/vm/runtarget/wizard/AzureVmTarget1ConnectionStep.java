package com.microsoft.azure.toolkit.intellij.vm.runtarget.wizard;

import com.intellij.execution.target.TargetEnvironmentWizard;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.remote.AuthType;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.ConnectionData;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshTargetStepBase;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshTargetWizardModel;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBoxSimple;
import com.microsoft.azure.toolkit.intellij.vm.creation.component.VirtualMachineComboBox;
import com.microsoft.azure.toolkit.intellij.vm.runtarget.AzureVmTargetEnvironmentConfiguration;
import com.microsoft.azure.toolkit.intellij.vm.runtarget.AzureVmTargetType;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.compute.AzureCompute;
import com.microsoft.azure.toolkit.lib.compute.virtualmachine.VirtualMachine;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @see com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshTargetConnectionStep
 */
@Getter
public class AzureVmTarget1ConnectionStep extends SshTargetStepBase implements TargetEnvironmentWizard.ValidationCallbackConsumer {
    private final Object previousStepId = null;
    private final Object stepId = AzureVmTarget1ConnectionStep.class;
    private final Object nextStepId = AzureVmTarget2AuthStep.class;
    private VirtualMachineComboBox vmSelector;

    public AzureVmTarget1ConnectionStep(AzureVmTargetEnvironmentConfiguration config, @Nonnull SshTargetWizardModel model) {
        super(model);
        this.setTitle(AzureVmTargetType.DISPLAY_NAME);
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    protected void doCommit(CommitType commitType) throws CommitStepException {
        System.out.println(commitType);
        if (commitType == CommitType.Next) {
            final ConnectionData connectionData = getModel().getConnectionData();
            final VirtualMachine vm = this.vmSelector.getValue();
            if (Objects.isNull(vm)) {
                throw new CommitStepException("no vm is selected.");
            }
            AzureTaskManager.getInstance().runOnPooledThread(() -> {
                final String hostIp = vm.getHostIp();
                if (StringUtils.isBlank(hostIp)) {
                    AzureMessager.getMessager().alert("ssh is not enabled for the selected vm.");
                    return;
                }
                getModel().getSubject().setDisplayName(vm.getName());
                connectionData.setAuthType(vm.isPasswordAuthenticationDisabled() ? AuthType.KEY_PAIR : AuthType.PASSWORD);
                connectionData.setHost(Optional.of(hostIp).orElse(""));
                connectionData.setUsername(vm.getAdminUserName());
                connectionData.checkAgentConnection(getModel().getProject(), ModalityState.any());
            });
        }
    }

    @Nonnull
    @Override
    protected JComponent createMainPanel() {
        final JPanel panel = new JPanel(new FlowLayout());
        this.vmSelector = new VirtualMachineComboBox();
        panel.add(vmSelector);
        return panel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Nonnull
    @Override
    public List<ValidationInfo> doValidateAll() {
        return Collections.emptyList();
    }

    @Override
    public void accept(Function0<? extends Unit> function0) {

    }
}
