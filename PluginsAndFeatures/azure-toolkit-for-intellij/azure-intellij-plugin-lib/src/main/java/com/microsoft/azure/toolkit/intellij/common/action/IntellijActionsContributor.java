/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common.action;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.microsoft.azure.toolkit.ide.arm.ResourceGroupActionsContributor;
import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.intellij.common.component.resourcegroup.FullResourceGroupCreationDialog;
import com.microsoft.azure.toolkit.intellij.common.properties.IntellijShowPropertiesViewAction;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.model.AzResourceBase;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.resource.ResourcesServiceSubscription;

import java.util.Objects;

public class IntellijActionsContributor implements IActionsContributor {
    @Override
    public void registerHandlers(AzureActionManager am) {
        am.<String>registerHandler(ResourceCommonActionsContributor.OPEN_URL, Objects::nonNull, BrowserUtil::browse);
        am.<AzResourceBase, AnActionEvent>registerHandler(ResourceCommonActionsContributor.SHOW_PROPERTIES,
            (s, e) -> Objects.nonNull(s) && Objects.nonNull(e.getProject()),
            (s, e) -> IntellijShowPropertiesViewAction.showPropertyView(s, Objects.requireNonNull(e.getProject())));
        am.registerHandler(ResourceGroupActionsContributor.CREATE_RESOURCE_GROUP, Objects::nonNull, this::createResourceGroup);
    }

    private void createResourceGroup(ResourcesServiceSubscription s) {
        AzureTaskManager.getInstance().runLater(() -> {
            final FullResourceGroupCreationDialog dialog = new FullResourceGroupCreationDialog(s.getSubscription());
            dialog.setOkActionListener((group) -> {
                dialog.close();
                group.createIfNotExist();
            });
            dialog.show();
        });
    }

    @Override
    public int getOrder() {
        return ResourceCommonActionsContributor.INITIALIZE_ORDER + 1; //after azure resource common actions registered
    }
}
