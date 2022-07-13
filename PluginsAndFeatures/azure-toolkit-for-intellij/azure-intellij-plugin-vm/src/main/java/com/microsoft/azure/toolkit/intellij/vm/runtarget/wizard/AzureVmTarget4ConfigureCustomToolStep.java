/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.vm.runtarget.wizard;

import com.intellij.openapi.util.NlsContexts;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshConfigureCustomToolStep;
import com.microsoft.azure.toolkit.intellij.vm.runtarget.AzureVmTargetEnvironmentConfiguration;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class AzureVmTarget4ConfigureCustomToolStep extends AzureVmTargetStepBase<SshConfigureCustomToolStep> {
    private final Object stepId = AzureVmTarget4ConfigureCustomToolStep.class;
    private final Object previousStepId = AzureVmTarget3IntrospectionStep.class;
    private final Object nextStepId = null;
    private final AzureVmTargetEnvironmentConfiguration config;

    public AzureVmTarget4ConfigureCustomToolStep(AzureVmTargetEnvironmentConfiguration config, SshConfigureCustomToolStep sshStep) {
        super("Step 4", sshStep);
        this.config = config;
    }

    @Override
    public @Nullable @NlsContexts.DialogTitle String getTitle() {
        return this.config.getDisplayName();
    }
}
