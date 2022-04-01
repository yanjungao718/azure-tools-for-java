/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.serviceexplorer.azure;

import com.intellij.openapi.project.Project;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.intellij.actions.SelectSubscriptionsAction;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.azuretools.azurecommons.helpers.AzureCmdException;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.serviceexplorer.AzureIconSymbol;
import com.microsoft.tooling.msservices.serviceexplorer.NodeAction;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.AzureModule;

public class ManageSubscriptionsAction extends NodeAction {
    public ManageSubscriptionsAction(AzureModule azureModule) {
        super(azureModule, "Select Subscriptions");
        addListener(new NodeActionListener() {
            @Override
            protected void actionPerformed(NodeActionEvent e) throws AzureCmdException {
                SelectSubscriptionsAction.selectSubscriptions((Project) azureModule.getProject()).subscribe();
            }
        });
    }

    @Override
    public AzureIconSymbol getIconSymbol() {
        return AzureIconSymbol.Common.SELECT_SUBSCRIPTIONS;
    }

    public boolean isEnabled() {
        try {
            return super.isEnabled() && AuthMethodManager.getInstance().isSignedIn();
        } catch (Exception e) {
            AzurePlugin.log("Error signing in", e);
            return false;
        }
    }
}
