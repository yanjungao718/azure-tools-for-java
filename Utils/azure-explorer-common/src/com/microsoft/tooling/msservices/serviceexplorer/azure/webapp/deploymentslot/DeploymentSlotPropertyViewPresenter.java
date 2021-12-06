/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.deploymentslot;

import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.AzureWebApp;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.WebApp;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.WebAppDeploymentSlot;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.base.WebAppBasePropertyViewPresenter;

import java.util.Map;
import java.util.Set;

public class DeploymentSlotPropertyViewPresenter extends WebAppBasePropertyViewPresenter {
    @Override
    protected void updateAppSettings(@NotNull final String sid, @NotNull final String webAppId,
                                     @Nullable final String name, final Map toUpdate,
                                     final Set toRemove) {
        final WebAppDeploymentSlot slot = getWebAppBase(sid, webAppId, name);
        final WebAppDeploymentSlot.WebAppDeploymentSlotUpdater updater = slot.update();
        updater.withAppSettings(toUpdate);
        toRemove.forEach(key -> updater.withoutAppSettings((String) key));
        updater.commit();
    }

    @Override
    protected WebAppDeploymentSlot getWebAppBase(@NotNull final String sid, @NotNull final String webAppId,
                                                  @Nullable final String name) {
        final WebApp webApp = Azure.az(AzureWebApp.class).get(webAppId);
        return webApp.deploymentSlot(name);
    }
}
