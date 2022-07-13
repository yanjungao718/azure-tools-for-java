/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.vm.runtarget.wizard;

import com.intellij.openapi.util.NlsContexts;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshTargetAuthStep;
import com.microsoft.azure.toolkit.intellij.vm.runtarget.AzureVmTargetEnvironmentConfiguration;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class AzureVmTarget2AuthStep extends AzureVmTargetStepBase<SshTargetAuthStep> {
    private final Object stepId = AzureVmTarget2AuthStep.class;
    private final Object previousStepId = AzureVmTarget1ConnectionStep.class;
    private final Object nextStepId = AzureVmTarget3IntrospectionStep.class;
    private final AzureVmTargetEnvironmentConfiguration config;

    public AzureVmTarget2AuthStep(AzureVmTargetEnvironmentConfiguration config, SshTargetAuthStep sshStep) {
        super("Step 2", sshStep);
        this.config = config;
    }

    @Override
    public @Nullable @NlsContexts.DialogTitle String getTitle() {
        return this.config.getDisplayName();
    }
}
