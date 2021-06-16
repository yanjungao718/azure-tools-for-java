/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.lib.function;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.exception.ManagementException;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.applicationinsights.ApplicationInsights;
import com.microsoft.azure.toolkit.lib.applicationinsights.ApplicationInsightsEntity;
import com.microsoft.azure.toolkit.lib.appservice.ApplicationInsightsConfig;
import com.microsoft.azure.toolkit.lib.appservice.AzureAppService;
import com.microsoft.azure.toolkit.lib.appservice.entity.AppServicePlanEntity;
import com.microsoft.azure.toolkit.lib.appservice.service.IAppServicePlan;
import com.microsoft.azure.toolkit.lib.appservice.service.IFunctionApp;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azure.toolkit.lib.common.telemetry.AzureTelemetry;
import com.microsoft.azure.toolkit.lib.resource.AzureGroup;
import com.microsoft.azuretools.sdkmanage.IdentityAzureManager;
import org.apache.commons.lang3.StringUtils;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;

public class FunctionAppService {
    private static final String CREATE_NEW_FUNCTION_APP = "isCreateNewFunctionApp";
    private static final String CREATE_NEW_RESOURCE_GROUP = "createNewResourceGroup";
    private static final String CREATE_NEW_APP_SERVICE_PLAN = "createNewAppServicePlan";

    private static final String CREATE_RESOURCE_GROUP = "Creating resource group %s in region %s...";
    private static final String CREATE_RESOURCE_GROUP_DONE = "Successfully created resource group %s.";
    private static final String CREATE_APP_SERVICE_PLAN = "Creating app service plan...";
    private static final String CREATE_APP_SERVICE_DONE = "Successfully created app service plan %s.";
    private static final String CREATE_FUNCTION_APP = "Creating function app %s...";
    private static final String CREATE_FUNCTION_APP_DONE = "Successfully created function app %s.";
    private static final String APPINSIGHTS_INSTRUMENTATION_KEY = "APPINSIGHTS_INSTRUMENTATIONKEY";
    private static final String APPLICATION_INSIGHTS_CREATE_START = "Creating application insights...";
    private static final String APPLICATION_INSIGHTS_CREATED = "Successfully created the application insights %s " +
            "for this Function App. You can visit %s/#@/resource%s/overview to view your " +
            "Application Insights component.";
    private static final String APPLICATION_INSIGHTS_CREATE_FAILED = "Unable to create the Application Insights " +
            "for the Function App due to error %s. Please use the Azure Portal to manually create and configure the " +
            "Application Insights if needed.";
    private static final FunctionAppService instance = new FunctionAppService();

    public static FunctionAppService getInstance() {
        return FunctionAppService.instance;
    }

    public IFunctionApp createFunctionApp(final FunctionAppConfig config) {
        AzureTelemetry.getContext().setProperty(CREATE_NEW_FUNCTION_APP, String.valueOf(true));
        final ResourceGroup resourceGroup = getOrCreateResourceGroup(config.getResourceGroup());
        final IAppServicePlan appServicePlan = getOrCreateAppServicePlan(config.getServicePlan());
        AzureMessager.getMessager().info(String.format(CREATE_FUNCTION_APP, config.getName()));
        final Map<String, String> appSettings = config.getAppSettings();
        // get/create ai instances only if user didn't specify ai connection string in app settings
        bindApplicationInsights(appSettings, config);
        final IFunctionApp result = Azure.az(AzureAppService.class).functionApp(resourceGroup.getName(), config.getName()).create()
                .withName(config.getName())
                .withResourceGroup(resourceGroup.getName())
                .withPlan(appServicePlan.id())
                .withRuntime(config.getRuntime())
                .withAppSettings(appSettings)
                .withDiagnosticConfig(config.getMonitorConfig().getDiagnosticConfig())
                .commit();
        AzureMessager.getMessager().info(String.format(CREATE_FUNCTION_APP_DONE, result.name()));
        return result;
    }

    private ResourceGroup getOrCreateResourceGroup(final ResourceGroup resourceGroup) {
        try {
            return Azure.az(AzureGroup.class).getByName(resourceGroup.getName());
        } catch (final ManagementException e) {
            AzureMessager.getMessager().info(String.format(CREATE_RESOURCE_GROUP, resourceGroup.getName(), resourceGroup.getRegion()));
            AzureTelemetry.getContext().setProperty(CREATE_NEW_RESOURCE_GROUP, String.valueOf(true));
            final ResourceGroup result = Azure.az(AzureGroup.class).create(resourceGroup.getName(), resourceGroup.getRegion());
            AzureMessager.getMessager().info(String.format(CREATE_RESOURCE_GROUP_DONE, result.getName()));
            return result;
        }
    }

    private IAppServicePlan getOrCreateAppServicePlan(final AppServicePlanEntity appServicePlanEntity) {
        final String servicePlanName = appServicePlanEntity.getName();
        final String servicePlanGroup = appServicePlanEntity.getResourceGroup();
        final IAppServicePlan appServicePlan = Azure.az(AzureAppService.class).subscription(appServicePlanEntity.getSubscriptionId())
                .appServicePlan(servicePlanGroup, servicePlanName);
        if (!appServicePlan.exists()) {
            AzureMessager.getMessager().info(CREATE_APP_SERVICE_PLAN);
            AzureTelemetry.getContext().setProperty(CREATE_NEW_APP_SERVICE_PLAN, String.valueOf(true));
            appServicePlan.create()
                    .withName(servicePlanName)
                    .withResourceGroup(servicePlanGroup)
                    .withRegion(Region.fromName(appServicePlanEntity.getRegion()))
                    .withPricingTier(appServicePlanEntity.getPricingTier())
                    .withOperatingSystem(appServicePlanEntity.getOperatingSystem())
                    .commit();
            AzureMessager.getMessager().info(String.format(CREATE_APP_SERVICE_DONE, appServicePlan.name()));
        }
        return appServicePlan;
    }

    private void bindApplicationInsights(final Map<String, String> appSettings, final FunctionAppConfig config) {
        // Skip app insights creation when user specify ai connection string in app settings
        if (appSettings.containsKey(APPINSIGHTS_INSTRUMENTATION_KEY)) {
            return;
        }
        String instrumentationKey = config.getMonitorConfig().getApplicationInsightsConfig().getInstrumentationKey();
        if (StringUtils.isNotEmpty(instrumentationKey)) {
            final ApplicationInsightsEntity applicationInsightsComponent = getOrCreateApplicationInsights(config);
            instrumentationKey = applicationInsightsComponent == null ? null : applicationInsightsComponent.getInstrumentationKey();
        }
        appSettings.put(APPINSIGHTS_INSTRUMENTATION_KEY, instrumentationKey);
    }

    @Nullable
    private ApplicationInsightsEntity getOrCreateApplicationInsights(final FunctionAppConfig config) {
        final ApplicationInsightsConfig insightsConfig = config.getMonitorConfig().getApplicationInsightsConfig();
        try {
            return Azure.az(ApplicationInsights.class).subscription(config.getSubscription())
                    .get(config.getResourceGroup().getName(), insightsConfig.getName());
        } catch (ManagementException e) {
           return createApplicationInsights(config);
        }
    }

    @Nullable
    private ApplicationInsightsEntity createApplicationInsights(final FunctionAppConfig config) {
        try {
            AzureMessager.getMessager().info(APPLICATION_INSIGHTS_CREATE_START);
            final AzureEnvironment environment = Azure.az(AzureAccount.class).account().getEnvironment();
            final ApplicationInsightsEntity resource = Azure.az(ApplicationInsights.class).create(config.getSubscription().getId(),
                    config.getResourceGroup().getName(), config.getRegion(), config.getMonitorConfig().getApplicationInsightsConfig().getName());
            AzureMessager.getMessager().info(String.format(APPLICATION_INSIGHTS_CREATED,
                    resource.getName(), IdentityAzureManager.getInstance().getPortalUrl(), resource.getId()));
            return resource;
        } catch (Exception e) {
            AzureMessager.getMessager().warning(String.format(APPLICATION_INSIGHTS_CREATE_FAILED, e.getMessage()));
            return null;
        }
    }

    public IFunctionApp deployFunctionApp(final IFunctionApp functionApp, final File stagingFolder) {
        return null;
    }
}
