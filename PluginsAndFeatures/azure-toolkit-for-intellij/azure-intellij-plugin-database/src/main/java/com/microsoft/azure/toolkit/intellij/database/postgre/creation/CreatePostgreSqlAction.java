/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.database.postgre.creation;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.common.model.Draft;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.database.DatabaseServerConfig;
import com.microsoft.azure.toolkit.lib.postgre.AzurePostgreSql;
import com.microsoft.azure.toolkit.lib.postgre.PostgreSqlServerDraft;
import com.microsoft.azure.toolkit.lib.resource.task.CreateResourceGroupTask;

public class CreatePostgreSqlAction {
    public static void create(Project project) {
        Azure.az(AzureAccount.class).account();
        AzureTaskManager.getInstance().runLater(() -> {
            final PostgreSqlCreationDialog dialog = new PostgreSqlCreationDialog(project);
            dialog.setOkActionListener(config -> {
                doCreate(config, project);
                dialog.close();
            });
            dialog.show();
        });

    }

    @AzureOperation(name = "postgre.create_server.server", params = {"config.getServerName()"}, type = AzureOperation.Type.ACTION)
    private static void doCreate(final DatabaseServerConfig config, final Project project) {
        final AzureString title = AzureOperationBundle.title("postgre.create_server.server", config.getName());
        AzureTaskManager.getInstance().runInBackground(title, () -> {
            final ResourceGroup rg = config.getResourceGroup();
            if (rg instanceof Draft) {
                new CreateResourceGroupTask(rg.getSubscriptionId(), rg.getName(), config.getRegion()).execute();
            }
            final PostgreSqlServerDraft draft = Azure.az(AzurePostgreSql.class).servers(config.getSubscription().getId())
                .create(config.getName(), config.getResourceGroup().getName());
            draft.setConfig(config);
            draft.commit();
        });
    }
}
