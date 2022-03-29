/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.vm;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.ide.vm.VirtualMachineActionsContributor;
import com.microsoft.azure.toolkit.intellij.vm.creation.CreateVirtualMachineAction;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.compute.AzureCompute;
import com.microsoft.azure.toolkit.lib.compute.virtualmachine.VirtualMachine;
import org.apache.commons.lang3.StringUtils;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class IntelliJVMActionsContributor implements IActionsContributor {
    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiPredicate<Object, AnActionEvent> createCondition = (r, e) -> r instanceof AzureCompute;
        final BiConsumer<Object, AnActionEvent> createHandler = (c, e) -> CreateVirtualMachineAction.createVirtualMachine((e.getProject()));
        am.registerHandler(ResourceCommonActionsContributor.CREATE, createCondition, createHandler);

        final BiPredicate<AzResource<?, ?, ?>, AnActionEvent> startCondition = (r, e) -> r instanceof VirtualMachine &&
            StringUtils.equals(r.getStatus(), AzResource.Status.STOPPED);
        final BiConsumer<AzResource<?, ?, ?>, AnActionEvent> startHandler = (c, e) -> ((VirtualMachine) c).start();
        am.registerHandler(ResourceCommonActionsContributor.START, startCondition, startHandler);

        final BiPredicate<AzResource<?, ?, ?>, AnActionEvent> stopCondition = (r, e) -> r instanceof VirtualMachine &&
            StringUtils.equals(r.getStatus(), AzResource.Status.RUNNING);
        final BiConsumer<AzResource<?, ?, ?>, AnActionEvent> stopHandler = (c, e) -> ((VirtualMachine) c).stop();
        am.registerHandler(ResourceCommonActionsContributor.STOP, stopCondition, stopHandler);

        final BiPredicate<AzResource<?, ?, ?>, AnActionEvent> restartCondition = (r, e) -> r instanceof VirtualMachine &&
            StringUtils.equals(r.getStatus(), AzResource.Status.RUNNING);
        final BiConsumer<AzResource<?, ?, ?>, AnActionEvent> restartHandler = (c, e) -> ((VirtualMachine) c).restart();
        am.registerHandler(ResourceCommonActionsContributor.RESTART, restartCondition, restartHandler);
    }

    @Override
    public int getOrder() {
        return VirtualMachineActionsContributor.INITIALIZE_ORDER + 1;
    }
}
