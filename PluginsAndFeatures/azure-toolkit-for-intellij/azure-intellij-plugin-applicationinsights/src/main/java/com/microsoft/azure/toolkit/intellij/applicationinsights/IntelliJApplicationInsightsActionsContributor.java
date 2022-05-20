/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.applicationinsights;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.microsoft.azure.toolkit.ide.applicationinsights.ApplicationInsightsActionsContributor;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.intellij.applicationinsights.connection.ApplicationInsightsResourceDefinition;
import com.microsoft.azure.toolkit.intellij.applicationinsights.creation.CreateApplicationInsightsAction;
import com.microsoft.azure.toolkit.intellij.connector.AzureServiceResource;
import com.microsoft.azure.toolkit.intellij.connector.ConnectorDialog;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.applicationinsights.ApplicationInsight;
import com.microsoft.azure.toolkit.lib.applicationinsights.ApplicationInsightDraft;
import com.microsoft.azure.toolkit.lib.applicationinsights.AzureApplicationInsights;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.operation.OperationBundle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroup;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class IntelliJApplicationInsightsActionsContributor implements IActionsContributor {
    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiPredicate<Object, AnActionEvent> condition = (r, e) -> r instanceof AzureApplicationInsights;
        final BiConsumer<Object, AnActionEvent> handler = (c, e) -> CreateApplicationInsightsAction.create(e.getProject(), null);
        am.registerHandler(ResourceCommonActionsContributor.CREATE, condition, handler);

        final BiConsumer<ResourceGroup, AnActionEvent> groupCreateAccountHandler = (r, e) -> {
            final ApplicationInsightDraft draft = Azure.az(AzureApplicationInsights.class)
                    .applicationInsights(r.getSubscriptionId()).create(AzResource.NONE.getName(), r.getName());
            draft.setRegion(r.getRegion());
            CreateApplicationInsightsAction.create(e.getProject(), draft);
        };
        am.registerHandler(ApplicationInsightsActionsContributor.GROUP_CREATE_APPLICATIONINSIGHT, (r, e) -> true, groupCreateAccountHandler);

        final BiPredicate<AzResource<?, ?, ?>, AnActionEvent> connectCondition = (r, e) -> r instanceof ApplicationInsight;
        final BiConsumer<AzResource<?, ?, ?>, AnActionEvent> connectHandler = (r, e) -> AzureTaskManager.getInstance().runLater(
                OperationBundle.description("resource.connect_resource.resource", r.getName()), () -> {
                    final ConnectorDialog dialog = new ConnectorDialog(e.getProject());
                    dialog.setResource(new AzureServiceResource<>(((ApplicationInsight) r), ApplicationInsightsResourceDefinition.INSTANCE));
                    dialog.show();
                });
        am.registerHandler(ResourceCommonActionsContributor.CONNECT, connectCondition, connectHandler);
    }

    @Override
    public int getOrder() {
        return ApplicationInsightsActionsContributor.INITIALIZE_ORDER + 1;
    }
}
