/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.appservice.file.legacy;

import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.management.appservice.WebAppBase;
import com.microsoft.azure.toolkit.lib.appservice.file.AppServiceFileService;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import com.microsoft.azuretools.telemetry.AppInsightsConstants;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetry.TelemetryProperties;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.serviceexplorer.AzureRefreshableNode;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.WebAppModule;

import javax.swing.*;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Deprecated
public class AppServiceUserFilesRootNode extends AzureRefreshableNode implements TelemetryProperties {
    private static final String MODULE_ID = WebAppModule.class.getName();
    private static final String MODULE_NAME = "Files";
    private static final String ROOT_PATH = "/site/wwwroot";

    protected WebAppBase app;

    protected final String subscriptionId;
    private AppServiceFileService fileService;

    public AppServiceUserFilesRootNode(final Node parent, final String subscriptionId, final WebAppBase app) {
        this(MODULE_NAME, parent, subscriptionId, app);
    }

    public AppServiceUserFilesRootNode(final String name, final Node parent, final String subscriptionId, final WebAppBase app) {
        super(MODULE_ID, name, parent, null);
        this.subscriptionId = subscriptionId;
        this.app = app;
    }

    @Override
    public void removeNode(final String sid, final String name, Node node) {
    }

    @Override
    @AzureOperation(name = "appservice|file.list", params = {"this.app.name()"}, type = AzureOperation.Type.ACTION)
    protected void refreshItems() {
        EventUtil.executeWithLog(getServiceName(), TelemetryConstants.LIST_FILE, operation -> {
            operation.trackProperty(TelemetryConstants.SUBSCRIPTIONID, subscriptionId);
            final AppServiceFileService service = this.getFileService();
            service.getFilesInDirectory(getRootPath()).stream()
                    .map(file -> new AppServiceFileNode(file, this, service))
                    .forEach(this::addChildNode);
        });
    }

    @NotNull
    protected String getRootPath() {
        return ROOT_PATH;
    }

    public AppServiceFileService getFileService() {
        if (Objects.isNull(this.fileService)) {
            this.fileService = AppServiceFileService.forApp(app);
        }
        return this.fileService;
    }

    @Override
    public String getServiceName() {
        return (app != null && app instanceof FunctionApp) ? TelemetryConstants.FUNCTION : TelemetryConstants.WEBAPP;
    }

    @Override
    public Map<String, String> toProperties() {
        return Collections.singletonMap(AppInsightsConstants.SubscriptionId, subscriptionId);
    }

    @Override
    public @Nullable Icon getIcon() {
        return DefaultLoader.getIdeHelper().getFileTypeIcon("/", true);
    }
}
