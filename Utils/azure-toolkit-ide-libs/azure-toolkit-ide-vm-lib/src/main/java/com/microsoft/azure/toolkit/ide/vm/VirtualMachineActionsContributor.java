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
import com.microsoft.azure.toolkit.lib.compute.virtualmachine.VirtualMachine;

import java.util.Optional;

import static com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle.title;

public class VirtualMachineActionsContributor implements IActionsContributor {
    public static final int INITIALIZE_ORDER = ResourceCommonActionsContributor.INITIALIZE_ORDER + 1;

    public static final String SERVICE_ACTIONS = "actions.vm.service";
    public static final String VM_ACTIONS = "actions.vm.management";

    public static final Action.Id<VirtualMachine> ADD_SSH_CONFIG = Action.Id.of("action.vm.add_ssh_configuration");

    @Override
    public void registerActions(AzureActionManager am) {
        final ActionView.Builder addSshConfigView = new ActionView.Builder("Add SSH Configuration", "/icons/action/add")
            .title(s -> Optional.ofNullable(s).map(r -> title("vm.add_ssh_config.vm", ((VirtualMachine) r).name())).orElse(null))
            .enabled(s -> s instanceof VirtualMachine && ((VirtualMachine) s).getFormalStatus().isRunning());
        am.registerAction(ADD_SSH_CONFIG, new Action<>(addSshConfigView));
    }

    @Override
    public void registerGroups(AzureActionManager am) {
        final ActionGroup serviceActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.REFRESH,
            "---",
            ResourceCommonActionsContributor.CREATE
        );
        am.registerGroup(SERVICE_ACTIONS, serviceActionGroup);

        final ActionGroup accountActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.PIN,
            "---",
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

    @Override
    public int getOrder() {
        return INITIALIZE_ORDER;
    }
}
