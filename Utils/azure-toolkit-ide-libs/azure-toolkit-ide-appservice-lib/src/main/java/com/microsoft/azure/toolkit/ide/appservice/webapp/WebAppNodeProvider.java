/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice.webapp;

import com.microsoft.azure.toolkit.ide.appservice.file.AppServiceFileNode;
import com.microsoft.azure.toolkit.ide.appservice.webapp.node.WebAppDeploymentSlotsNode;
import com.microsoft.azure.toolkit.ide.common.IExplorerNodeProvider;
import com.microsoft.azure.toolkit.ide.common.component.AzureResourceIconProvider;
import com.microsoft.azure.toolkit.ide.common.component.AzureResourceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.AzureServiceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcon;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIconProvider;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.AppServiceAppBase;
import com.microsoft.azure.toolkit.lib.appservice.AzureAppService;
import com.microsoft.azure.toolkit.lib.appservice.model.AppServiceFile;
import com.microsoft.azure.toolkit.lib.appservice.model.OperatingSystem;
import com.microsoft.azure.toolkit.lib.appservice.webapp.WebApp;
import com.microsoft.azure.toolkit.lib.appservice.webapp.WebAppModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

public class WebAppNodeProvider implements IExplorerNodeProvider {
    public static final AzureIconProvider<AppServiceAppBase<?, ?, ?>> APP_SERVICE_ICON_PROVIDER =
        new AzureResourceIconProvider<AppServiceAppBase<?, ?, ?>>().withModifier(WebAppNodeProvider::getOperatingSystemModifier);

    private static final String NAME = "Web Apps";
    private static final String ICON = "/icons/webapp.svg";

    @Nullable
    @Override
    public Object getRoot() {
        return WebAppModule.class;
    }

    @Override
    public boolean accept(@Nonnull Object data, @Nullable Node<?> parent) {
        return data == WebAppModule.class ||
            data instanceof WebApp ||
            data instanceof AppServiceFile;
    }

    @Nullable
    @Override
    public Node<?> createNode(@Nonnull Object data, @Nullable Node<?> parent, @Nonnull Manager manager) {
        if (data == WebAppModule.class) {
            final AzureAppService service = Azure.az(AzureAppService.class);
            return new Node<>(service)
                .view(new AzureServiceLabelView<>(service, NAME, ICON))
                .actions(WebAppActionsContributor.SERVICE_ACTIONS)
                .addChildren(AzureAppService::webApps, (d, p) -> this.createNode(d, p, manager));
        } else if (data instanceof WebApp) {
            final WebApp webApp = (WebApp) data;
            return new Node<>(webApp)
                .view(new AzureResourceLabelView<>(webApp, WebApp::getStatus, APP_SERVICE_ICON_PROVIDER))
                .actions(WebAppActionsContributor.WEBAPP_ACTIONS)
                .addChildren(Arrays::asList, (app, webAppNode) -> new WebAppDeploymentSlotsNode(app))
                .addChild(AppServiceFileNode::getRootFileNodeForAppService, (d, p) -> this.createNode(d, p, manager)) // Files
                .addChild(AppServiceFileNode::getRootLogNodeForAppService, (d, p) -> this.createNode(d, p, manager));
        } else if (data instanceof AppServiceFile) {
            final AppServiceFile file = (AppServiceFile) data;
            return new AppServiceFileNode(file);
        }
        return null;
    }

    @Nullable
    private static AzureIcon.Modifier getOperatingSystemModifier(AppServiceAppBase<?, ?, ?> resource) {
        return resource.getFormalStatus().isWaiting() ? null :
            resource.getRuntime().getOperatingSystem() != OperatingSystem.WINDOWS ? AzureIcon.Modifier.LINUX : null;
    }
}
