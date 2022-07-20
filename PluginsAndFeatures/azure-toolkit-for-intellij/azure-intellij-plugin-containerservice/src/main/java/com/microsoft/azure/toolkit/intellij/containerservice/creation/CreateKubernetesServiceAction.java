/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.containerservice.creation;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.operation.OperationBundle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.common.utils.Utils;
import com.microsoft.azure.toolkit.lib.containerservice.AzureContainerService;
import com.microsoft.azure.toolkit.lib.containerservice.KubernetesClusterDraft;
import com.microsoft.azure.toolkit.lib.containerservice.KubernetesClusterModule;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroup;
import com.microsoft.azure.toolkit.lib.resource.task.CreateResourceGroupTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class CreateKubernetesServiceAction {
    public static void create(@Nonnull Project project, @Nullable KubernetesClusterDraft.Config data) {
        Azure.az(AzureAccount.class).account();
        AzureTaskManager.getInstance().runLater(() -> {
            final KubernetesCreationDialog dialog = new KubernetesCreationDialog(project);
            if (Objects.nonNull(data)) {
                dialog.getForm().setValue(data);
            }
            dialog.setOkActionListener(config -> {
                doCreate(config, project);
                dialog.close();
            });
            dialog.show();
        });
    }

    public static KubernetesClusterDraft.Config getDefaultConfig(){
        final KubernetesClusterDraft.Config config = new KubernetesClusterDraft.Config();
        final String name = String.format("kubernetes-service-%s", Utils.getTimestamp());
        config.setName(name);
        config.setDnsPrefix(String.format("%s-dns", name));
        config.setMinVMCount(3);
        config.setMaxVMCount(5);
        return config;
    }

    @AzureOperation(name = "kubernetes.create_service.service", params = {"config.getName()"}, type = AzureOperation.Type.ACTION)
    private static void doCreate(final KubernetesClusterDraft.Config config, final Project project) {
        final AzureString title = OperationBundle.description("kubernetes.create_service.service", config.getName());
        AzureTaskManager.getInstance().runInBackground(title, () -> {
            final ResourceGroup rg = config.getResourceGroup();
            if (rg.isDraftForCreating()) {
                new CreateResourceGroupTask(rg.getSubscriptionId(), rg.getName(), config.getRegion()).execute();
            }
            final KubernetesClusterModule module = Azure.az(AzureContainerService.class).kubernetes(config.getSubscription().getId());
            final KubernetesClusterDraft draft = module.create(config.getName(), config.getResourceGroup().getName());
            draft.setConfig(config);
            draft.commit();
        });
    }
}
