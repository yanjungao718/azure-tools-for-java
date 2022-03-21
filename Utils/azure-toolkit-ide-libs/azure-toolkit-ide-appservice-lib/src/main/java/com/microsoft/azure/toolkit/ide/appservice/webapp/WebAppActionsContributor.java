/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice.webapp;

import com.microsoft.azure.toolkit.ide.appservice.AppServiceActionsContributor;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.appservice.AppServiceAppBase;
import com.microsoft.azure.toolkit.lib.appservice.webapp.WebApp;
import com.microsoft.azure.toolkit.lib.appservice.webapp.WebAppDeploymentSlot;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.event.AzureEventBus;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import static com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle.title;

public class WebAppActionsContributor implements IActionsContributor {
    public static final int INITIALIZE_ORDER = AppServiceActionsContributor.INITIALIZE_ORDER + 1;

    public static final String SERVICE_ACTIONS = "actions.webapp.service";
    public static final String WEBAPP_ACTIONS = "actions.webapp.management";
    public static final String DEPLOYMENT_SLOTS_ACTIONS = "actions.webapp.deployment_slots";
    public static final String DEPLOYMENT_SLOT_ACTIONS = "actions.webapp.deployment_slot";

    public static final Action.Id<WebApp> REFRESH_DEPLOYMENT_SLOTS = Action.Id.of("actions.webapp.deployment_slots.refresh");
    public static final Action.Id<WebAppDeploymentSlot> SWAP_DEPLOYMENT_SLOT = Action.Id.of("actions.webapp.deployment_slot.swap");

    @Override
    public void registerGroups(AzureActionManager am) {
        final ActionGroup serviceActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.REFRESH,
            "---",
            ResourceCommonActionsContributor.CREATE
        );
        am.registerGroup(SERVICE_ACTIONS, serviceActionGroup);

        final ActionGroup webAppActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.PIN,
            "---",
            ResourceCommonActionsContributor.REFRESH,
            ResourceCommonActionsContributor.OPEN_PORTAL_URL,
            AppServiceActionsContributor.OPEN_IN_BROWSER,
            ResourceCommonActionsContributor.SHOW_PROPERTIES,
            "---",
            ResourceCommonActionsContributor.DEPLOY,
            "---",
            ResourceCommonActionsContributor.START,
            ResourceCommonActionsContributor.STOP,
            ResourceCommonActionsContributor.RESTART,
            ResourceCommonActionsContributor.DELETE,
            "---",
            AppServiceActionsContributor.PROFILE_FLIGHT_RECORD,
            AppServiceActionsContributor.SSH_INTO_WEBAPP,
            AppServiceActionsContributor.START_STREAM_LOG,
            AppServiceActionsContributor.STOP_STREAM_LOG
        );
        am.registerGroup(WEBAPP_ACTIONS, webAppActionGroup);

        final ActionGroup deploymentSlotActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.REFRESH,
            ResourceCommonActionsContributor.OPEN_PORTAL_URL,
            AppServiceActionsContributor.OPEN_IN_BROWSER,
            ResourceCommonActionsContributor.SHOW_PROPERTIES,
            "---",
            SWAP_DEPLOYMENT_SLOT,
            "---",
            ResourceCommonActionsContributor.START,
            ResourceCommonActionsContributor.STOP,
            ResourceCommonActionsContributor.RESTART,
            ResourceCommonActionsContributor.DELETE,
            "---",
            AppServiceActionsContributor.START_STREAM_LOG,
            AppServiceActionsContributor.STOP_STREAM_LOG
        );
        am.registerGroup(DEPLOYMENT_SLOT_ACTIONS, deploymentSlotActionGroup);

        am.registerGroup(DEPLOYMENT_SLOTS_ACTIONS, new ActionGroup(REFRESH_DEPLOYMENT_SLOTS));
    }

    @Override
    public void registerActions(AzureActionManager am) {
        final Consumer<WebApp> refresh = webApp -> AzureEventBus.emit("appservice|webapp.slot.refresh", webApp);
        final ActionView.Builder refreshView = new ActionView.Builder("Refresh", "/icons/action/refresh.svg")
                .title(s -> Optional.ofNullable(s).map(r -> title("webapp.list_deployments.app", ((WebApp) r).name())).orElse(null))
                .enabled(s -> s instanceof WebApp);
        final Action<WebApp> refreshAction = new Action<>(refresh, refreshView);
        refreshAction.setShortcuts(am.getIDEDefaultShortcuts().refresh());
        am.registerAction(REFRESH_DEPLOYMENT_SLOTS, refreshAction);

        final Consumer<WebAppDeploymentSlot> swap = slot -> slot.getParent().swap(slot.getName());
        final ActionView.Builder swapView = new ActionView.Builder("Swap With Production", "/icons/action/refresh.svg")
            .title(s -> Optional.ofNullable(s).map(r -> title("webapp.swap_deployment.deployment|app",
                ((WebAppDeploymentSlot) s).name(), ((WebAppDeploymentSlot) s).getParent().getName())).orElse(null))
            .enabled(s -> s instanceof WebAppDeploymentSlot && StringUtils.equals(((WebAppDeploymentSlot) s).status(), AzResource.Status.RUNNING));
        am.registerAction(SWAP_DEPLOYMENT_SLOT, new Action<>(swap, swapView));
    }

    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiPredicate<AzResource<?, ?, ?>, Object> startCondition = (r, e) -> r instanceof AppServiceAppBase &&
            StringUtils.equals(r.status(), AzResource.Status.STOPPED);
        final BiConsumer<AzResource<?, ?, ?>, Object> startHandler = (c, e) -> ((AppServiceAppBase<?, ?, ?>) c).start();
        am.registerHandler(ResourceCommonActionsContributor.START, startCondition, startHandler);

        final BiPredicate<AzResource<?, ?, ?>, Object> stopCondition = (r, e) -> r instanceof AppServiceAppBase &&
            StringUtils.equals(r.status(), AzResource.Status.RUNNING);
        final BiConsumer<AzResource<?, ?, ?>, Object> stopHandler = (c, e) -> ((AppServiceAppBase<?, ?, ?>) c).stop();
        am.registerHandler(ResourceCommonActionsContributor.STOP, stopCondition, stopHandler);

        final BiPredicate<AzResource<?, ?, ?>, Object> restartCondition = (r, e) -> r instanceof AppServiceAppBase &&
            StringUtils.equals(r.status(), AzResource.Status.RUNNING);
        final BiConsumer<AzResource<?, ?, ?>, Object> restartHandler = (c, e) -> ((AppServiceAppBase<?, ?, ?>) c).restart();
        am.registerHandler(ResourceCommonActionsContributor.RESTART, restartCondition, restartHandler);
    }

    @Override
    public int getOrder() {
        return INITIALIZE_ORDER; //after azure resource common actions registered
    }
}
