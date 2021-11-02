/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.vm.ssh;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.remote.AuthType;
import com.intellij.ssh.config.unified.SshConfig;
import com.intellij.ssh.config.unified.SshConfigManager;
import com.intellij.ssh.ui.unified.SshConfigConfigurable;
import com.intellij.ssh.ui.unified.SshUiData;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.compute.vm.VirtualMachine;
import org.apache.commons.lang3.reflect.MethodUtils;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class AddSshConfigAction {

    public static void addSshConfig(VirtualMachine vm, @Nonnull Project project) {
        final Configurable configurable = new SshConfigConfigurable.Main(project);
        AzureTaskManager.getInstance().runLater(() -> openConfigDialog(vm, project, configurable));
    }

    @AzureOperation(name = "vm|ssh.open_ssh_config_dialog", type = AzureOperation.Type.TASK)
    private static void openConfigDialog(VirtualMachine vm, @Nonnull Project project, Configurable configurable) {
        ShowSettingsUtil.getInstance().editConfigurable(project, configurable, () -> {
            try {
                final SshUiData uiData = new SshUiData(toSshConfig(vm), true);
                final SshConfigManager manager = SshConfigManager.getInstance(project);
                if (Objects.isNull(manager.findConfigByName(uiData.getName()))) {
                    final Object merged = manager.getLastSavedAndCurrentData()
                        .createMerged(new SshConfigManager.ConfigsData(Collections.emptyList(), Collections.singletonList(uiData)));
                    MethodUtils.invokeMethod(configurable, true, "resetFromData", merged);
                }
                MethodUtils.invokeMethod(configurable, true, "select", uiData);
            } catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                AzureMessager.getMessager().error(e);
            }
        });
    }

    @Nonnull
    private static SshConfig toSshConfig(VirtualMachine vm) {
        final SshConfig config = new SshConfig(true);
        final String name = String.format("Azure: %s", vm.name());
        config.setCustomName(name);
        config.setId(UUID.nameUUIDFromBytes(name.getBytes()).toString());
        config.setUsername(vm.getAdminUserName());
        config.setAuthType(vm.isPasswordAuthenticationDisabled() ? AuthType.KEY_PAIR : AuthType.PASSWORD);
        config.setHost(vm.getHostIp());
        return config;
    }
}
