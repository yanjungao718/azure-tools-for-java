/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.arm;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.account.IAccount;
import com.microsoft.azure.toolkit.lib.account.IAzureAccount;
import com.microsoft.azure.toolkit.lib.auth.IAccountActions;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.resource.ResourcesServiceSubscription;

import java.util.Optional;
import java.util.function.Consumer;

import static com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle.title;

public class ResourceGroupActionsContributor implements IActionsContributor {
    public static final int INITIALIZE_ORDER = ResourceCommonActionsContributor.INITIALIZE_ORDER + 1;

    public static final String SERVICE_ACTIONS = "actions.appCentric.service";
    public static final String SUBSCRIPTION_ACTIONS = "actions.appCentric.subscription";
    public static final String RESOURCE_GROUP_ACTIONS = "actions.appCentric.group";

    public static final Action.Id<ResourcesServiceSubscription> CREATE_RESOURCE_GROUP = Action.Id.of("action.resource_group.create");
    public static final Action.Id<Object> CREATE_SUBSCRIPTION = Action.Id.of("action.subscription.create");

    @Override
    public void registerActions(AzureActionManager am) {
        final ActionView.Builder createResourceGroup = new ActionView.Builder("Create Resource Group", "/icons/action/create.svg")
            .title(s -> Optional.ofNullable(s).map(r -> title("resource_group.create.subscription", ((ResourcesServiceSubscription) r).getName())).orElse(null))
            .enabled(s -> s instanceof ResourcesServiceSubscription);
        final Action<ResourcesServiceSubscription> createAction = new Action<>(createResourceGroup);
        createAction.setShortcuts(am.getIDEDefaultShortcuts().add());
        am.registerAction(CREATE_RESOURCE_GROUP, createAction);

        final Consumer<Object> openSubsCreationUrl = s -> {
            final IAccount account = Azure.az(IAzureAccount.class).account();
            final String url = String.format("%s/#blade/Microsoft_Azure_Billing/CatalogBlade/appId/AddSubscriptionButton", account.portalUrl());
            am.getAction(ResourceCommonActionsContributor.OPEN_URL).handle(url);
        };
        final ActionView.Builder addSubscriptionView = new ActionView.Builder("Create Subscription", "/icons/action/create.svg").enabled(s -> true);
        final Action<Object> createSubsAction = new Action<>(openSubsCreationUrl, addSubscriptionView);
        createSubsAction.setShortcuts(am.getIDEDefaultShortcuts().add());
        am.registerAction(CREATE_SUBSCRIPTION, createSubsAction);
    }

    @Override
    public void registerGroups(AzureActionManager am) {
        final ActionGroup serviceActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.REFRESH,
            "---",
            IAccountActions.SELECT_SUBS,
            CREATE_SUBSCRIPTION
        );
        am.registerGroup(SERVICE_ACTIONS, serviceActionGroup);

        final ActionGroup subscriptionActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.PIN,
            "---",
            ResourceCommonActionsContributor.REFRESH,
            ResourceCommonActionsContributor.OPEN_PORTAL_URL,
            "---",
            CREATE_RESOURCE_GROUP // TODO: create resource group in this subscription.
        );
        am.registerGroup(SUBSCRIPTION_ACTIONS, subscriptionActionGroup);

        final ActionGroup resourceGroupActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.PIN,
            "---",
            ResourceCommonActionsContributor.REFRESH,
            ResourceCommonActionsContributor.OPEN_PORTAL_URL,
            "---",
            ResourceCommonActionsContributor.DELETE,
            ResourceCommonActionsContributor.RESOURCE_GROUP_CREATE_ACTIONS // TODO: create any resource in this resource group.
        );
        am.registerGroup(RESOURCE_GROUP_ACTIONS, resourceGroupActionGroup);
    }

    @Override
    public int getOrder() {
        return INITIALIZE_ORDER;
    }
}
