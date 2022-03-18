/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.appservice.function;

import com.microsoft.azure.toolkit.ide.appservice.file.AppServiceFileNode;
import com.microsoft.azure.toolkit.ide.appservice.function.node.FunctionsNode;
import com.microsoft.azure.toolkit.ide.appservice.webapp.WebAppNodeProvider;
import com.microsoft.azure.toolkit.ide.common.IExplorerNodeProvider;
import com.microsoft.azure.toolkit.ide.common.component.AzureResourceIconProvider;
import com.microsoft.azure.toolkit.ide.common.component.AzureResourceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.AzureServiceLabelView;
import com.microsoft.azure.toolkit.ide.common.component.Node;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcon;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIconProvider;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.AppServiceAppBase;
import com.microsoft.azure.toolkit.lib.appservice.function.AzureFunctions;
import com.microsoft.azure.toolkit.lib.appservice.function.FunctionApp;
import com.microsoft.azure.toolkit.lib.appservice.model.AppServiceFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

import static com.microsoft.azure.toolkit.ide.appservice.webapp.WebAppNodeProvider.WEBAPP_ICON_PROVIDER;

public class FunctionAppNodeProvider implements IExplorerNodeProvider {
    public static final AzureIconProvider<AppServiceAppBase<?, ?, ?>> FUNCTIONAPP_ICON_PROVIDER =
        new AzureResourceIconProvider<AppServiceAppBase<?, ?, ?>>()
            .withModifier(WebAppNodeProvider::getOperatingSystemModifier)
            .withModifier(app -> new AzureIcon.Modifier("functionapp", AzureIcon.ModifierLocation.OTHER));

    private static final String NAME = "Function App";
    private static final String ICON = "/icons/Microsoft.Web/functions.png";

    @Nullable
    @Override
    public Object getRoot() {
        return Azure.az(AzureFunctions.class);
    }

    @Override
    public boolean accept(@Nonnull Object data, @Nullable Node<?> parent) {
        return data instanceof AzureFunctions ||
            data instanceof FunctionApp ||
            data instanceof AppServiceFile;
    }

    @Nullable
    @Override
    public Node<?> createNode(@Nonnull Object data, @Nullable Node<?> parent, @Nonnull Manager manager) {
        if (data instanceof AzureFunctions) {
            final AzureFunctions service = Azure.az(AzureFunctions.class);
            return new Node<>(service)
                .view(new AzureServiceLabelView<>(service, NAME, ICON))
                .actions(FunctionAppActionsContributor.SERVICE_ACTIONS)
                .addChildren(AzureFunctions::functionApps, (d, p) -> this.createNode(d, p, manager));
        } else if (data instanceof FunctionApp) {
            final FunctionApp functionApp = (FunctionApp) data;
            return new Node<>(functionApp)
                .view(new AzureResourceLabelView<>(functionApp, FunctionApp::getStatus, FUNCTIONAPP_ICON_PROVIDER))
                .actions(FunctionAppActionsContributor.FUNCTION_APP_ACTIONS)
                .addChildren(Arrays::asList, (app, webAppNode) -> new FunctionsNode(app))
                .addChild(AppServiceFileNode::getRootFileNodeForAppService, (d, p) -> this.createNode(d, p, manager)) // Files
                .addChild(AppServiceFileNode::getRootLogNodeForAppService, (d, p) -> this.createNode(d, p, manager));
        } else if (data instanceof AppServiceFile) {
            final AppServiceFile file = (AppServiceFile) data;
            return new AppServiceFileNode(file);
        }
        return null;
    }
}
