/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.appservice.AppServiceAppBase;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import static com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle.title;

public class AppServiceActionsContributor implements IActionsContributor {

    public static final int INITIALIZE_ORDER = ResourceCommonActionsContributor.INITIALIZE_ORDER + 1;
    public static final Action.Id<AppServiceAppBase<?, ?, ?>> OPEN_IN_BROWSER = Action.Id.of("actions.appservice.open_in_browser");
    public static final Action.Id<AppServiceAppBase<?, ?, ?>> START_STREAM_LOG = Action.Id.of("actions.appservice.stream_log.start");
    public static final Action.Id<AppServiceAppBase<?, ?, ?>> STOP_STREAM_LOG = Action.Id.of("actions.appservice.stream_log.stop");
    public static final Action.Id<AppServiceAppBase<?, ?, ?>> SSH_INTO_WEBAPP = Action.Id.of("actions.webapp.ssh");
    public static final Action.Id<AppServiceAppBase<?, ?, ?>> PROFILE_FLIGHT_RECORD = Action.Id.of("actions.webapp.flight_record");

    @Override
    public void registerActions(AzureActionManager am) {
        final Consumer<AppServiceAppBase<?, ?, ?>> openInBrowser = appService -> am.getAction(ResourceCommonActionsContributor.OPEN_URL)
            .handle("https://" + appService.getHostName());
        final ActionView.Builder openInBrowserView = new ActionView.Builder("Open In Browser", "/icons/action/portal.svg")
            .title(s -> Optional.ofNullable(s).map(r -> title("webapp.open_browser")).orElse(null))
            .enabled(s -> s instanceof AppServiceAppBase);
        am.registerAction(OPEN_IN_BROWSER, new Action<>(openInBrowser, openInBrowserView));

        final ActionView.Builder startStreamLogView = new ActionView.Builder("Start Streaming Logs", "/icons/action/log.svg")
            .title(s -> Optional.ofNullable(s).map(r -> title("appservice.open_log_stream.app", ((AppServiceAppBase<?, ?, ?>) r).getName())).orElse(null))
            .enabled(s -> s instanceof AppServiceAppBase<?, ?, ?> && ((AppServiceAppBase<?, ?, ?>) s).getFormalStatus().isRunning());
        am.registerAction(START_STREAM_LOG, new Action<>(startStreamLogView));

        final ActionView.Builder stopStreamLogView = new ActionView.Builder("Stop Streaming Logs", "/icons/action/log.svg")
            .title(s -> Optional.ofNullable(s).map(r -> title("appservice.close_log_stream.app", ((AppServiceAppBase<?, ?, ?>) r).getName())).orElse(null))
            .enabled(s -> s instanceof AppServiceAppBase<?, ?, ?> && ((AppServiceAppBase<?, ?, ?>) s).getFormalStatus().isRunning());
        am.registerAction(STOP_STREAM_LOG, new Action<>(stopStreamLogView));

        final ActionView.Builder profileFlightRecorderView = new ActionView.Builder("Profile Flight Recorder")
            .title(s -> Optional.ofNullable(s).map(r -> title("webapp.profile_flight_recorder.app", ((AppServiceAppBase<?, ?, ?>) r).getName())).orElse(null))
            .enabled(s -> s instanceof AppServiceAppBase<?, ?, ?> && ((AppServiceAppBase<?, ?, ?>) s).getFormalStatus().isRunning());
        am.registerAction(PROFILE_FLIGHT_RECORD, new Action<>(profileFlightRecorderView));

        final ActionView.Builder sshView = new ActionView.Builder("SSH into Web App")
            .title(s -> Optional.ofNullable(s).map(r -> title("webapp.connect_ssh.app", ((AppServiceAppBase<?, ?, ?>) r).getName())).orElse(null))
            .enabled(s -> s instanceof AppServiceAppBase<?, ?, ?> && ((AppServiceAppBase<?, ?, ?>) s).getFormalStatus().isRunning());
        am.registerAction(SSH_INTO_WEBAPP, new Action<>(sshView));
    }

    @Override
    public void registerHandlers(AzureActionManager am) {
        final BiPredicate<AzResource<?, ?, ?>, Object> startCondition = (r, e) -> r instanceof AppServiceAppBase<?, ?, ?> &&
            StringUtils.equals(r.getStatus(), AzResource.Status.STOPPED);
        final BiConsumer<AzResource<?, ?, ?>, Object> startHandler = (c, e) -> ((AppServiceAppBase<?, ?, ?>) c).start();
        am.registerHandler(ResourceCommonActionsContributor.START, startCondition, startHandler);

        final BiPredicate<AzResource<?, ?, ?>, Object> stopCondition = (r, e) -> r instanceof AppServiceAppBase<?, ?, ?> &&
            StringUtils.equals(r.getStatus(), AzResource.Status.RUNNING);
        final BiConsumer<AzResource<?, ?, ?>, Object> stopHandler = (c, e) -> ((AppServiceAppBase<?, ?, ?>) c).stop();
        am.registerHandler(ResourceCommonActionsContributor.STOP, stopCondition, stopHandler);

        final BiPredicate<AzResource<?, ?, ?>, Object> restartCondition = (r, e) -> r instanceof AppServiceAppBase<?, ?, ?> &&
            StringUtils.equals(r.getStatus(), AzResource.Status.RUNNING);
        final BiConsumer<AzResource<?, ?, ?>, Object> restartHandler = (c, e) -> ((AppServiceAppBase<?, ?, ?>) c).restart();
        am.registerHandler(ResourceCommonActionsContributor.RESTART, restartCondition, restartHandler);
    }

    @Override
    public int getOrder() {
        return INITIALIZE_ORDER; //after azure resource common actions registered
    }
}
