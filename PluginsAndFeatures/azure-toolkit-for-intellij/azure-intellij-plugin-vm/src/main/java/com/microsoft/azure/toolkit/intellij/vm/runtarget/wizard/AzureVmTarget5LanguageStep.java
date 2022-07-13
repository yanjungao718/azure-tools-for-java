/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.vm.runtarget.wizard;

import com.intellij.openapi.util.NlsContexts;
import com.jetbrains.plugins.remotesdk.target.ssh.target.wizard.SshTargetLanguageStep;
import com.microsoft.azure.toolkit.intellij.vm.runtarget.AzureVmTargetEnvironmentConfiguration;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class AzureVmTarget5LanguageStep extends AzureVmTargetStepBase<SshTargetLanguageStep> {
    private final Object stepId = AzureVmTarget5LanguageStep.class;
    private final Object previousStepId = AzureVmTarget3IntrospectionStep.class;
    private final Object nextStepId = null;
    private final AzureVmTargetEnvironmentConfiguration config;

    public AzureVmTarget5LanguageStep(AzureVmTargetEnvironmentConfiguration config, SshTargetLanguageStep originStep) {
        super("Step 4", originStep);
        this.config = config;
    }

    @Override
    public @Nullable @NlsContexts.DialogTitle String getTitle() {
        return this.config.getDisplayName();
    }
}
