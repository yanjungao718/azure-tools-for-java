/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.util;

import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.AzureAppService;
import com.microsoft.azure.toolkit.lib.common.entity.CheckNameAvailabilityResultEntity;
import com.microsoft.azure.toolkit.lib.resource.AzureResources;
import org.apache.commons.lang3.StringUtils;

import static com.microsoft.azure.toolkit.intellij.common.AzureBundle.message;

public class ValidationUtils {
    private static final String PACKAGE_NAME_REGEX = "[a-zA-Z]([\\.a-zA-Z0-9_])*";
    private static final String GROUP_ARTIFACT_ID_REGEX = "[0-9a-zA-Z]([\\.a-zA-Z0-9\\-_])*";
    private static final String VERSION_REGEX = "[0-9]([\\.a-zA-Z0-9\\-_])*";
    private static final String APP_SERVICE_NAME_REGEX = "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,58}[a-zA-Z0-9]";
    private static final String APP_SERVICE_PLAN_NAME_PATTERN = "[a-zA-Z0-9\\-]{1,40}";
    // refer: https://dev.azure.com/msazure/AzureDMSS/_git/AzureDMSS-PortalExtension?path=%2Fsrc%2FSpringCloudPortalExt%2FClient%2FCreateApplication%2F
    // CreateApplicationBlade.ts&version=GBdev&line=463&lineEnd=463&lineStartColumn=25&lineEndColumn=55&lineStyle=plain&_a=contents
    private static final String SPRING_CLOUD_APP_NAME_PATTERN = "^[a-z][a-z0-9-]{2,30}[a-z0-9]$";
    private static final String APP_INSIGHTS_NAME_INVALID_CHARACTERS = "[*;/?:@&=+$,<>#%\\\"\\{}|^'`\\\\\\[\\]]";

    public static boolean isValidJavaPackageName(String packageName) {
        return packageName != null && packageName.matches(PACKAGE_NAME_REGEX);
    }

    public static boolean isValidGroupIdArtifactId(String name) {
        return name != null && name.matches(GROUP_ARTIFACT_ID_REGEX);
    }

    public static boolean isValidAppServiceName(String name) {
        return name != null && name.matches(APP_SERVICE_NAME_REGEX);
    }

    public static boolean isValidSpringCloudAppName(String name) {
        int len = name.trim().length();
        return name != null && name.matches(SPRING_CLOUD_APP_NAME_PATTERN) && len >= 4 && len <= 32;
    }

    public static boolean isValidVersion(String version) {
        return version != null && version.matches(VERSION_REGEX);
    }

    public static void validateAppServiceName(String subscriptionId, String appServiceName) {
        if (StringUtils.isEmpty(subscriptionId)) {
            throw new IllegalArgumentException(message("appService.subscription.validate.empty"));
        }
        if (StringUtils.isEmpty(appServiceName)) {
            throw new IllegalArgumentException(message("appService.name.validate.empty"));
        }
        if (appServiceName.length() < 2 || appServiceName.length() > 60) {
            throw new IllegalArgumentException(message("appService.name.validate.length"));
        }
        if (!isValidAppServiceName(appServiceName)) {
            throw new IllegalArgumentException(message("appService.name.validate.invalidName"));
        }
        final CheckNameAvailabilityResultEntity result = Azure.az(AzureAppService.class).forSubscription(subscriptionId).checkNameAvailability(appServiceName);
        if (!result.isAvailable()) {
            throw new IllegalArgumentException(result.getUnavailabilityMessage());
        }
    }

    public static void validateResourceGroupName(String subscriptionId, String resourceGroup) {
        if (StringUtils.isEmpty(subscriptionId)) {
            throw new IllegalArgumentException(message("appService.subscription.validate.empty"));
        }
        if (StringUtils.isEmpty(resourceGroup)) {
            throw new IllegalArgumentException(message("appService.resourceGroup.validate.empty"));
        }
        if (Azure.az(AzureResources.class).groups(subscriptionId).exists(resourceGroup)) {
            throw new IllegalArgumentException(message("appService.resourceGroup.validate.exist"));
        }
    }

    public static void validateAppServicePlanName(String appServicePlan) {
        if (StringUtils.isEmpty(appServicePlan)) {
            throw new IllegalArgumentException(message("appService.servicePlan.validate.empty"));
        } else if (!appServicePlan.matches(APP_SERVICE_PLAN_NAME_PATTERN)) {
            throw new IllegalArgumentException(message("appService.servicePlan.validate.invalidName", APP_SERVICE_PLAN_NAME_PATTERN));
        }
    }
}
