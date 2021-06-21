/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.function;

import com.microsoft.azure.toolkit.intellij.appservice.AppServiceComboBoxModel;
import com.microsoft.azure.toolkit.lib.appservice.entity.AppServicePlanEntity;
import com.microsoft.azure.toolkit.lib.appservice.service.IFunctionApp;
import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.function.FunctionAppConfig;
import lombok.Getter;
import org.parboiled.common.StringUtils;

@Getter
public class FunctionAppComboBoxModel extends AppServiceComboBoxModel<IFunctionApp, FunctionAppConfig> {
    private FunctionAppConfig functionAppConfig;

    public FunctionAppComboBoxModel(IFunctionApp functionApp) {
        super(functionApp);
        this.config = FunctionAppConfig.builder()
                .resourceId(this.resourceId)
                .name(this.appName)
                .region(functionApp.entity().getRegion())
                .resourceGroup(ResourceGroup.builder().name(functionApp.resourceGroup()).build())
                .subscription(Subscription.builder().id(functionApp.subscriptionId()).build())
                .servicePlan(AppServicePlanEntity.builder().id(functionApp.entity().getAppServicePlanId()).build()).build();
    }

    public FunctionAppComboBoxModel(FunctionAppConfig functionAppConfig) {
        this.isNewCreateResource = true;
        this.appName = functionAppConfig.getName();
        this.resourceGroup = functionAppConfig.getResourceGroup().getName();
        this.subscriptionId = functionAppConfig.getSubscription().getId();
        this.functionAppConfig = functionAppConfig;
        this.resourceId = functionAppConfig.getResourceId();
        this.isNewCreateResource = StringUtils.isEmpty(resourceId);
        this.config = functionAppConfig;
    }
}
