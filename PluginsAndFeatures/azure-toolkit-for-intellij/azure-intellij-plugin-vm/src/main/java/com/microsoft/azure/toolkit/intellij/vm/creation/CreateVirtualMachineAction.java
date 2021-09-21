/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.vm.creation;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.common.telemetry.AzureTelemetry;
import com.microsoft.azure.toolkit.lib.compute.vm.DraftVirtualMachine;
import com.microsoft.azure.toolkit.lib.compute.vm.task.CreateVirtualMachineTask;

public class CreateVirtualMachineAction {
    public static void createVirtualMachine(Project project) {
        Azure.az(AzureAccount.class).account();
        AzureTaskManager.getInstance().runLater(() -> {
            final VMCreationDialog dialog = new VMCreationDialog(project);
            dialog.setOkActionListener((config) -> {
                dialog.close();
                createVirtualMachine(config);
            });
            dialog.show();
        });
    }

    public static void createVirtualMachine(final DraftVirtualMachine draft) {
        AzureTaskManager.getInstance().runInBackground(AzureOperationBundle.title("vm.create", draft.getName()), () -> {
            AzureTelemetry.getActionContext().setProperty("subscriptionId", draft.getSubscriptionId());
            new CreateVirtualMachineTask(draft).execute();
        });
    }
}
