/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.vm.runtarget.wizard;

import com.intellij.openapi.util.NlsContexts;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshTargetIntrospectionStep;
import com.microsoft.azure.toolkit.intellij.vm.runtarget.AzureVmTargetEnvironmentConfiguration;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class AzureVmTarget3IntrospectionStep extends AzureVmTargetStepBase<SshTargetIntrospectionStep> {
    private final Object stepId = AzureVmTarget3IntrospectionStep.class;
    private final Object previousStepId = AzureVmTarget2AuthStep.class;
    private final AzureVmTargetEnvironmentConfiguration config;

    public AzureVmTarget3IntrospectionStep(AzureVmTargetEnvironmentConfiguration config, SshTargetIntrospectionStep sshStep) {
        super("Step 3", sshStep);
        this.config = config;
    }

    @Override
    public @Nullable Object getNextStepId() {
        return this.getOriginStep().getModel().isCustomToolConfiguration$intellij_remoteRun() ? AzureVmTarget4ConfigureCustomToolStep.class : AzureVmTarget5LanguageStep.class;
    }

    @Override
    public @Nullable @NlsContexts.DialogTitle String getTitle() {
        return this.config.getDisplayName();
    }
}
