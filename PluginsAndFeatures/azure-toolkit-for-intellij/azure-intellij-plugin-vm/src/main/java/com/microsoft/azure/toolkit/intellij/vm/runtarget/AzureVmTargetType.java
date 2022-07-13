package com.microsoft.azure.toolkit.intellij.vm.runtarget;

import com.intellij.execution.Platform;
import com.intellij.execution.target.CustomToolLanguageRuntimeType;
import com.intellij.execution.target.LanguageRuntimeType;
import com.intellij.execution.target.TargetEnvironmentRequest;
import com.intellij.execution.target.TargetEnvironmentType;
import com.intellij.execution.target.TargetPlatform;
import com.intellij.icons.AllIcons;
import com.intellij.ide.wizard.AbstractWizardStepEx;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.jetbrains.plugins.remotesdk.target.ssh.target.SshRemoteEnvironmentRequest;
import com.jetbrains.plugins.remotesdk.target.ssh.target.SshTargetConfigurable;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.ConnectionData;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshConfigureCustomToolStep;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshTargetAuthStep;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshTargetIntrospectionStep;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshTargetLanguageStep;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshTargetWizardModel;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcons;
import com.microsoft.azure.toolkit.intellij.common.IntelliJAzureIcons;
import com.microsoft.azure.toolkit.intellij.vm.runtarget.wizard.AzureVmTarget1ConnectionStep;
import com.microsoft.azure.toolkit.intellij.vm.runtarget.wizard.AzureVmTarget2AuthStep;
import com.microsoft.azure.toolkit.intellij.vm.runtarget.wizard.AzureVmTarget3IntrospectionStep;
import com.microsoft.azure.toolkit.intellij.vm.runtarget.wizard.AzureVmTarget4ConfigureCustomToolStep;
import com.microsoft.azure.toolkit.intellij.vm.runtarget.wizard.AzureVmTarget5LanguageStep;
import kotlin.collections.CollectionsKt;
import lombok.Getter;
import org.jetbrains.annotations.Nls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.util.List;

@Getter
public class AzureVmTargetType extends TargetEnvironmentType<AzureVmTargetEnvironmentConfiguration> {
    public static final String TYPE_ID = "Microsoft.Compute/virtualMachines";
    public static final String DISPLAY_NAME = "Azure Virtual Machine";
    @Nls
    @Nonnull
    private final String displayName = DISPLAY_NAME;
    @Nonnull
    private final Icon icon = IntelliJAzureIcons.getIcon(AzureIcons.VirtualMachine.MODULE);

    public AzureVmTargetType() {
        super(TYPE_ID);
    }

    @Nonnull
    @Override
    public PersistentStateComponent<?> createSerializer(@Nonnull AzureVmTargetEnvironmentConfiguration config) {
        return config;
    }

    @Nonnull
    @Override
    public AzureVmTargetEnvironmentConfiguration createDefaultConfig() {
        return new AzureVmTargetEnvironmentConfiguration();
    }

    @Override
    public boolean providesNewWizard(@Nonnull Project project, @Nullable LanguageRuntimeType<?> runtimeType) {
        return true;
    }

    @Nullable
    @Override
    @SuppressWarnings("KotlinInternalInJava")
    public List<AbstractWizardStepEx> createStepsForNewWizard(@Nonnull Project project, @Nonnull AzureVmTargetEnvironmentConfiguration config, @Nullable LanguageRuntimeType<?> runtimeType) {
        final boolean isCustomToolConfiguration = runtimeType instanceof CustomToolLanguageRuntimeType;
        final ConnectionData data = new ConnectionData(false, null, "", 22, "", "", true, "", "", true, ConnectionData.OpenSshAgentConnectionState.NOT_STARTED);
        final SshTargetWizardModel model = new SshTargetWizardModel(project, config, data);
        model.setLanguageType$intellij_remoteRun(runtimeType);
        model.setCustomToolConfiguration$intellij_remoteRun(isCustomToolConfiguration);
        return CollectionsKt.listOf(
            new AzureVmTarget1ConnectionStep(config, model),
            new AzureVmTarget2AuthStep(config, new SshTargetAuthStep(model)),
            new AzureVmTarget3IntrospectionStep(config, new SshTargetIntrospectionStep(model)),
            isCustomToolConfiguration ?
                new AzureVmTarget4ConfigureCustomToolStep(config, new SshConfigureCustomToolStep(model)) :
                new AzureVmTarget5LanguageStep(config, new SshTargetLanguageStep(model)));
    }

    @Nonnull
    @Override
    public AzureVmTargetEnvironmentConfiguration duplicateConfig(@Nonnull AzureVmTargetEnvironmentConfiguration config) {
        return duplicateTargetConfiguration(this, config);
    }

    @Nonnull
    @Override
    public Configurable createConfigurable(@Nonnull Project project, @Nonnull AzureVmTargetEnvironmentConfiguration config, @Nullable LanguageRuntimeType<?> languageRuntimeType, @Nullable Configurable configurable) {
        return new SshTargetConfigurable(project, config);
    }

    @Nonnull
    @Override
    @SuppressWarnings("UnstableApiUsage")
    public TargetEnvironmentRequest createEnvironmentRequest(@Nonnull Project project, @Nonnull AzureVmTargetEnvironmentConfiguration config) {
        return new SshRemoteEnvironmentRequest(project, config, new TargetPlatform(Platform.UNIX));
    }
}
