/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice.file;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.lib.appservice.model.AppServiceFile;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.event.AzureEventBus;

import java.util.Optional;
import java.util.function.Consumer;

import static com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle.title;

public class AppServiceFileActionsContributor implements IActionsContributor {
    public static final String APP_SERVICE_FILE_ACTIONS = "actions.appservice.file";
    public static final String APP_SERVICE_DIRECTORY_ACTIONS = "actions.appservice.directory";

    public static final Action.Id<AppServiceFile> APP_SERVICE_DIRECTORY_REFRESH = Action.Id.of("action.appservice.directory.refresh");
    public static final Action.Id<AppServiceFile> APP_SERVICE_FILE_VIEW = Action.Id.of("action.appservice.file.view");
    public static final Action.Id<AppServiceFile> APP_SERVICE_FILE_DOWNLOAD = Action.Id.of("action.appservice.file.download");

    @Override
    public void registerGroups(AzureActionManager am) {
        final ActionGroup directoryActions = new ActionGroup(
                APP_SERVICE_DIRECTORY_REFRESH
        );
        am.registerGroup(APP_SERVICE_DIRECTORY_ACTIONS, directoryActions);

        final ActionGroup fileActions = new ActionGroup(
                APP_SERVICE_FILE_VIEW,
                APP_SERVICE_FILE_DOWNLOAD
        );
        am.registerGroup(APP_SERVICE_FILE_ACTIONS, fileActions);
    }

    @Override
    public void registerActions(AzureActionManager am) {
        final Consumer<AppServiceFile> refresh = file -> AzureEventBus.emit("common|resource.refresh", file);
        final ActionView.Builder refreshView = new ActionView.Builder("Refresh", "/icons/action/refresh.svg")
                .title(s -> Optional.ofNullable(s).map(r -> title("common|resource.refresh", ((AppServiceFile) r).getName())).orElse(null))
                .enabled(s -> s instanceof AppServiceFile);
        am.registerAction(APP_SERVICE_DIRECTORY_REFRESH, new Action<>(refresh, refreshView));
    }

    public int getOrder() {
        return 1; //after azure resource common actions registered
    }
}
