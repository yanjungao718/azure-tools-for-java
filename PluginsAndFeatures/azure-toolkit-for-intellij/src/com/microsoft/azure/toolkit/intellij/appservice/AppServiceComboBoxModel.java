/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.appservice;

import com.microsoft.azure.toolkit.lib.appservice.AppServiceConfig;
import com.microsoft.azure.toolkit.lib.appservice.model.JavaVersion;
import com.microsoft.azure.toolkit.lib.appservice.model.Runtime;
import com.microsoft.azure.toolkit.lib.appservice.model.WebContainer;
import com.microsoft.azure.toolkit.lib.appservice.service.IAppService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public abstract class AppServiceComboBoxModel<R extends IAppService, T extends AppServiceConfig> {
    @Setter
    protected boolean isNewCreateResource;
    protected String subscriptionId;
    protected String resourceGroup;
    protected String appName;
    protected String os;
    protected String resourceId;
    protected R resource;
    protected T config;

    public AppServiceComboBoxModel() {

    }

    public AppServiceComboBoxModel(R appService) {
        this.resource = appService;
        this.resourceId = appService.id();
        this.appName = appService.name();
        this.resourceGroup = appService.resourceGroup();
        this.os = appService.getRuntime().getOperatingSystem().getValue();
        this.subscriptionId = appService.subscriptionId();
        this.isNewCreateResource = false;
    }

    public static boolean isSameApp(AppServiceComboBoxModel first, AppServiceComboBoxModel second) {
        if (Objects.isNull(first) || Objects.isNull(second)) {
            return first == second;
        }
        return StringUtils.equalsIgnoreCase(first.resourceId, second.resourceId) ||
                (StringUtils.equalsIgnoreCase(first.appName, second.appName) &&
                        StringUtils.equalsIgnoreCase(first.resourceGroup, second.resourceGroup) &&
                        StringUtils.equalsIgnoreCase(first.subscriptionId, second.subscriptionId));
    }

    // todo: remove duplicate with runtime combobox
    public String getRuntime() {
        final Runtime runtime = config.getRuntime();
        final String os = runtime.getOperatingSystem().getValue();
        final String javaVersion = runtime.getJavaVersion() == JavaVersion.OFF ? null : String.format("Java %s", runtime.getJavaVersion().getValue());
        final String webContainer = runtime.getWebContainer() == WebContainer.JAVA_OFF ? null : runtime.getWebContainer().getValue();
        return Stream.of(os, javaVersion, webContainer)
                .filter(StringUtils::isNotEmpty)
                .map(StringUtils::capitalize).collect(Collectors.joining("-"));
    }
}
