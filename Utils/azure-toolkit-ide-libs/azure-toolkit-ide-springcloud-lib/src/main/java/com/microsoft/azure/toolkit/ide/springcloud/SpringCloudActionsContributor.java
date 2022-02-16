/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.springcloud;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.model.AzResourceBase;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;

import java.util.Optional;
import java.util.function.Consumer;

import static com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle.title;

public class SpringCloudActionsContributor implements IActionsContributor {
    public static final int INITIALIZE_ORDER = ResourceCommonActionsContributor.INITIALIZE_ORDER + 1;

    public static final String APP_ACTIONS = "actions.springcloud.app";
    public static final String CLUSTER_ACTIONS = "actions.springcloud.cluster";
    public static final String SERVICE_ACTIONS = "actions.springcloud.service";
    public static final Action.Id<SpringCloudApp> OPEN_PUBLIC_URL = Action.Id.of("action.springcloud.app.open_public_url");
    public static final Action.Id<SpringCloudApp> OPEN_TEST_URL = Action.Id.of("action.springcloud.app.open_test_url");
    public static final Action.Id<SpringCloudApp> STREAM_LOG = Action.Id.of("action.springcloud.app.stream_log");

    @Override
    public void registerActions(AzureActionManager am) {
        final Consumer<SpringCloudApp> openPublicUrl = s -> am.getAction(ResourceCommonActionsContributor.OPEN_URL).handle(s.getApplicationUrl());
        final ActionView.Builder openPublicUrlView = new ActionView.Builder("Access Public Endpoint", "/icons/action/browser.svg")
                .title(s -> Optional.ofNullable(s).map(r -> title("springcloud.open_public_url.app", ((SpringCloudApp) r).name())).orElse(null))
                .enabled(s -> s instanceof SpringCloudApp && ((SpringCloudApp) s).isPublicEndpointEnabled());
        am.registerAction(OPEN_PUBLIC_URL, new Action<>(openPublicUrl, openPublicUrlView));

        final Consumer<SpringCloudApp> openTestUrl = s -> am.getAction(ResourceCommonActionsContributor.OPEN_URL).handle(s.getTestUrl());
        final ActionView.Builder openTestUrlView = new ActionView.Builder("Access Test Endpoint", "/icons/action/browser.svg")
                .title(s -> Optional.ofNullable(s).map(r -> title("springcloud.open_test_url.app", ((SpringCloudApp) r).name())).orElse(null))
                .enabled(s -> s instanceof SpringCloudApp);
        am.registerAction(OPEN_TEST_URL, new Action<>(openTestUrl, openTestUrlView));

        final ActionView.Builder streamLogView = new ActionView.Builder("Streaming Log", "/icons/action/log.svg")
                .title(s -> Optional.ofNullable(s).map(r -> title("springcloud.open_stream_log.app", ((SpringCloudApp) r).name())).orElse(null))
                .enabled(s -> s instanceof SpringCloudApp && ((AzResourceBase) s).getFormalStatus().isRunning());
        am.registerAction(STREAM_LOG, new Action<>(streamLogView));
    }

    @Override
    public void registerGroups(AzureActionManager am) {
        final ActionGroup serviceActionGroup = new ActionGroup(
                ResourceCommonActionsContributor.CREATE,
                "---",
                ResourceCommonActionsContributor.SERVICE_REFRESH
        );
        am.registerGroup(SERVICE_ACTIONS, serviceActionGroup);

        final ActionGroup clusterActionGroup = new ActionGroup(
                ResourceCommonActionsContributor.OPEN_PORTAL_URL,
                ResourceCommonActionsContributor.CREATE,
                ResourceCommonActionsContributor.REFRESH
        );
        am.registerGroup(CLUSTER_ACTIONS, clusterActionGroup);

        final ActionGroup appActionGroup = new ActionGroup(
                ResourceCommonActionsContributor.OPEN_PORTAL_URL,
                SpringCloudActionsContributor.OPEN_PUBLIC_URL,
                SpringCloudActionsContributor.OPEN_TEST_URL,
                "---",
                ResourceCommonActionsContributor.START,
                ResourceCommonActionsContributor.STOP,
                ResourceCommonActionsContributor.RESTART,
                ResourceCommonActionsContributor.DELETE,
                "---",
                ResourceCommonActionsContributor.DEPLOY,
                "---",
                ResourceCommonActionsContributor.SHOW_PROPERTIES,
                ResourceCommonActionsContributor.REFRESH,
                "---",
                SpringCloudActionsContributor.STREAM_LOG
        );
        am.registerGroup(APP_ACTIONS, appActionGroup);
    }

    @Override
    public int getOrder() {
        return INITIALIZE_ORDER;
    }
}
