/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.webapp;

import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.AzureWebApp;
import com.microsoft.azure.toolkit.lib.appservice.service.IAppServiceUpdater;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.WebApp;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.base.WebAppBasePropertyViewPresenter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class WebAppPropertyViewPresenter extends WebAppBasePropertyViewPresenter {
    @Override
    protected void updateAppSettings(@Nonnull final String sid, @Nonnull final String webAppId,
                                     @Nullable final String name, final Map toUpdate,
                                     final Set toRemove) {
        final WebApp webApp = getWebAppBase(sid, webAppId, name);
        final IAppServiceUpdater appServiceUpdater = webApp.update();
        appServiceUpdater.withAppSettings(toUpdate);
        toRemove.forEach(key -> appServiceUpdater.withoutAppSettings((String) key));
        appServiceUpdater.commit();
    }

    @Override
    protected WebApp getWebAppBase(@Nonnull final String sid, @Nonnull final String webAppId,
                                    @Nullable final String name) {
        return Azure.az(AzureWebApp.class).subscription(sid).get(webAppId);
    }
}
