/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.auth.IAccountActions;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.intellij.AzureConfigurable;
import com.microsoft.intellij.actions.AzureSignInAction;
import com.microsoft.intellij.actions.SelectSubscriptionsAction;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Optional;
import java.util.function.BiConsumer;

public class LegacyIntellijAccountActionsContributor implements IActionsContributor {
    @Override
    public void registerActions(AzureActionManager am) {
        final AzureString authzTitle = AzureOperationBundle.title("account.authorize_action");
        final ActionView.Builder authzView = new ActionView.Builder("Authorize").title((s) -> authzTitle);
        final BiConsumer<Runnable, AnActionEvent> authzHandler = (Runnable r, AnActionEvent e) ->
            AzureSignInAction.requireSignedIn(Optional.ofNullable(e).map(AnActionEvent::getProject).orElse(null), r);
        am.registerAction(Action.REQUIRE_AUTH, new Action<>(authzHandler, authzView).setAuthRequired(false));

        final AzureString authnTitle = AzureOperationBundle.title("account.authenticate");
        final ActionView.Builder authnView = new ActionView.Builder("Sign in").title((s) -> authnTitle);
        final BiConsumer<Void, AnActionEvent> authnHandler = (Void v, AnActionEvent e) -> {
            final AuthMethodManager authMethodManager = AuthMethodManager.getInstance();
            if (authMethodManager.isSignedIn()) authMethodManager.signOut();
            AzureSignInAction.onAzureSignIn(e.getProject());
        };
        am.registerAction(Action.AUTHENTICATE, new Action<>(authnHandler, authnView).setAuthRequired(false));

        final AzureString selectSubsTitle = AzureOperationBundle.title("account.select_subscription");
        final ActionView.Builder selectSubsView = new ActionView.Builder("Select Subscriptions").title((s) -> authnTitle);
        final BiConsumer<Void, AnActionEvent> selectSubsHandler = (Void v, AnActionEvent e) ->
            SelectSubscriptionsAction.selectSubscriptions(e.getProject()).subscribe();
        am.registerAction(IAccountActions.SELECT_SUBS, new Action<>(selectSubsHandler, selectSubsView).setAuthRequired(true));
    }

    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiConsumer<Object, AnActionEvent> openSettingsHandler = (ignore, e) -> {
            final Project project = Optional.ofNullable(e).map(AnActionEvent::getProject).orElseGet(() -> {
                final Project[] openProjects = ProjectManagerEx.getInstance().getOpenProjects();
                return ArrayUtils.isEmpty(openProjects) ? null : openProjects[0];
            });
            final AzureString title = AzureOperationBundle.title("common.open_azure_settings");
            AzureTaskManager.getInstance().runAndWait(new AzureTask<>(title, () ->
                    ShowSettingsUtil.getInstance().showSettingsDialog(project, AzureConfigurable.AzureAbstractConfigurable.class)));
        };
        am.registerHandler(ResourceCommonActionsContributor.OPEN_AZURE_SETTINGS, (i, e) -> true, openSettingsHandler);
    }
}