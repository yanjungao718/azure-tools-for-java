/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.webapp;

import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.toolkit.intellij.appservice.AppServiceComboBoxModel;
import com.microsoft.azure.toolkit.lib.appservice.model.Runtime;
import com.microsoft.azure.toolkit.lib.appservice.service.IWebApp;
import com.microsoft.azuretools.core.mvp.model.AzureMvpModel;
import com.microsoft.azuretools.core.mvp.model.webapp.WebAppSettingModel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public class WebAppComboBoxModel extends AppServiceComboBoxModel<WebApp> {

    private String runtime;
    private IWebApp webApp;
    private WebAppSettingModel webAppSettingModel;

    // todo: migrate Base Model to service library
    public WebAppComboBoxModel(final IWebApp webApp) {
        this.webApp = webApp;
        this.resourceId = webApp.id();
        this.appName = webApp.name();
        this.resourceGroup = webApp.entity().getResourceGroup();
        this.subscriptionId = webApp.entity().getSubscriptionId();
        this.isNewCreateResource = false;
        this.runtime = getRuntimeDisplayName(webApp.getRuntime());
    }

    public WebAppComboBoxModel(WebAppSettingModel webAppSettingModel) {
        this.resourceId = webAppSettingModel.getWebAppId();
        // In case recover from configuration, get the app name from resource id
        this.appName =
            StringUtils.isEmpty(webAppSettingModel.getWebAppName()) && StringUtils.isNotEmpty(resourceId) ?
            AzureMvpModel.getSegment(resourceId, "sites") :
            webAppSettingModel.getWebAppName();
        this.resourceGroup = webAppSettingModel.getResourceGroup();
        this.subscriptionId = webAppSettingModel.getSubscriptionId();
        this.isNewCreateResource = webAppSettingModel.isCreatingNew();
        this.webAppSettingModel = webAppSettingModel;
        this.runtime = getRuntimeDisplayName(webAppSettingModel.getRuntime());
    }

    private static String getRuntimeDisplayName(Runtime runtime) {
        return String.format("%s-%s-%s", runtime.getOperatingSystem().getValue(),
                             runtime.getWebContainer().getValue(), runtime.getJavaVersion().getValue());
    }

}
