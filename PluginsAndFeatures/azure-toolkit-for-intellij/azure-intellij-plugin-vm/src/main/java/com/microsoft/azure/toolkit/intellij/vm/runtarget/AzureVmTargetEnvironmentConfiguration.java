/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.vm.runtarget;

import com.intellij.openapi.project.Project;
import com.intellij.remote.RemoteCredentials;
import com.intellij.ssh.config.unified.SshConfig;
import com.jetbrains.plugins.remotesdk.target.ssh.target.SshTargetEnvironmentConfiguration;
import com.jetbrains.plugins.remotesdk.target.ssh.target.SshTargetEnvironmentConfigurationBase;
import com.jetbrains.plugins.remotesdk.target.ssh.target.TempSshTargetEnvironmentConfigurationBase;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AzureVmTargetEnvironmentConfiguration extends SshTargetEnvironmentConfiguration {

    public AzureVmTargetEnvironmentConfiguration() {
        super();
        this.init();
    }

    @SneakyThrows
    private void init() {
        FieldUtils.writeField(this, "typeId", AzureVmTargetType.TYPE_ID, true);
    }
}
