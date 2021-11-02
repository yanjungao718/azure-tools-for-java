/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.vm;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.compute.vm.VirtualMachine;

import java.util.Optional;

import static com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle.title;

public class VirtualMachineActionsContributor implements IActionsContributor {
    public static final String SERVICE_ACTIONS = "actions.vm.service";
    public static final String VM_ACTIONS = "actions.vm.management";

    public static final Action.Id<VirtualMachine> ADD_SSH_CONFIG = Action.Id.of("action.vm.add_ssh_configuration");

    @Override
    public void registerActions(AzureActionManager am) {
        final ActionView.Builder addSshConfigView = new ActionView.Builder("Add SSH Configuration", "/icons/action/add")
            .title(s -> Optional.ofNullable(s).map(r -> title("vm|ssh.add_config", ((VirtualMachine) r).name())).orElse(null))
            .enabled(s -> s instanceof VirtualMachine);
        am.registerAction(ADD_SSH_CONFIG, new Action<>(addSshConfigView));
    }

    @Override
    public void registerGroups(AzureActionManager am) {
        final ActionGroup serviceActionGroup = new ActionGroup(
                ResourceCommonActionsContributor.SERVICE_REFRESH,
                ResourceCommonActionsContributor.CREATE
        );
        am.registerGroup(SERVICE_ACTIONS, serviceActionGroup);

        final ActionGroup accountActionGroup = new ActionGroup(
                ResourceCommonActionsContributor.REFRESH,
                ResourceCommonActionsContributor.OPEN_PORTAL_URL,
                "---",
                VirtualMachineActionsContributor.ADD_SSH_CONFIG,
                "---",
                ResourceCommonActionsContributor.START,
                ResourceCommonActionsContributor.STOP,
                ResourceCommonActionsContributor.RESTART,
                ResourceCommonActionsContributor.DELETE
        );
        am.registerGroup(VM_ACTIONS, accountActionGroup);
    }
}
