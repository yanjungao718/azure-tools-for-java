/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.appservice;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.microsoft.azure.toolkit.ide.appservice.function.FunctionAppActionsContributor;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.intellij.appservice.actions.TriggerFunctionAction;
import com.microsoft.azure.toolkit.lib.appservice.entity.FunctionEntity;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class AppServiceIntelliJActionsContributorForUltimate implements IActionsContributor {
    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiPredicate<FunctionEntity, AnActionEvent> triggerFunctionWithHttpClientPredicate = (r, e) -> r instanceof FunctionEntity;
        final BiConsumer<FunctionEntity, AnActionEvent> triggerFunctionWithHttpClientHandler = (entity, e) ->
                AzureTaskManager.getInstance().runLater(() -> TriggerFunctionAction.triggerFunction(entity, e.getProject()));
        am.registerHandler(FunctionAppActionsContributor.TRIGGER_FUNCTION_WITH_HTTP_CLIENT,
                triggerFunctionWithHttpClientPredicate, triggerFunctionWithHttpClientHandler);
    }
}
