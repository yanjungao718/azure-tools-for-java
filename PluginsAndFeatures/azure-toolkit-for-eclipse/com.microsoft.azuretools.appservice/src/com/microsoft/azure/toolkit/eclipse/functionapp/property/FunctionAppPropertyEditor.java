/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.functionapp.property;

import java.util.Map;
import java.util.Set;

import com.microsoft.azure.toolkit.eclipse.appservice.property.AppServiceBasePropertyEditor;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.AzureAppService;
import com.microsoft.azure.toolkit.lib.appservice.function.FunctionApp;
import com.microsoft.azure.toolkit.lib.appservice.function.FunctionAppDraft;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.base.WebAppBasePropertyViewPresenter;

public class FunctionAppPropertyEditor extends AppServiceBasePropertyEditor {
    public static final String ID = "com.microsoft.azure.toolkit.eclipse.functionapp.property.FunctionAppPropertyEditor";

    public FunctionAppPropertyEditor() {
        super(new FunctionAppPropertyViewPresenter());
    }

    static class FunctionAppPropertyViewPresenter extends WebAppBasePropertyViewPresenter {

        @Override
        protected FunctionApp getWebAppBase(String subscriptionId, String functionAppId, String name) {
            return Azure.az(AzureAppService.class).functionApp(functionAppId);
        }

        @Override
        protected void updateAppSettings(String subscriptionId, String functionAppId, String name, Map toUpdate, Set toRemove) {
            final FunctionApp functionApp = getWebAppBase(subscriptionId, functionAppId, name);
            final FunctionAppDraft appServiceUpdater = (FunctionAppDraft) functionApp.update();
            appServiceUpdater.setAppSettings(toUpdate);
            toRemove.forEach(key -> appServiceUpdater.removeAppSetting((String) key));
            appServiceUpdater.commit();
        }
    }

}
