/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.storage;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azuretools.ActionConstants;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.Name;
import com.microsoft.tooling.msservices.serviceexplorer.AzureIconSymbol;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.storage.StorageNode;

@Name(OpenStorageBrowserAction.ACTION_NAME)
public class OpenStorageBrowserAction extends NodeActionListener {

    public static final String ACTION_NAME = "Open Storage Browser";
    private static final String ACTION_PROGRESS_PATTERN = "Opening storage browser of storage account({0}) in portal";

    private final StorageNode node;

    public OpenStorageBrowserAction(StorageNode node) {
        this.node = node;
    }

    @Override
    public AzureIconSymbol getIconSymbol() {
        return AzureIconSymbol.Common.OPEN_IN_PORTAL;
    }

    @Override
    @AzureOperation(name = "storage|account.open_storage_browser", params = {"this.node.getName()"}, type = AzureOperation.Type.ACTION)
    public void actionPerformed(NodeActionEvent e) {
        AzureTaskManager.getInstance().runInBackground(new AzureTask<>(this.node.getProject(),
                String.format(ACTION_PROGRESS_PATTERN, this.node.getName()), false, () -> this.openStorageBrowser()));
    }

    private void openStorageBrowser() {
        final String portalUrl = AuthMethodManager.getInstance().getAzureManager().getPortalUrl();
        final ResourceId resourceId = ResourceId.fromString(this.node.getId());
        final Subscription subscription = Azure.az(AzureAccount.class).account().getSubscription(resourceId.subscriptionId());
        final String url = portalUrl
                + Node.REST_SEGMENT_JOB_MANAGEMENT_TENANTID
                + subscription.getTenantId()
                + Node.REST_SEGMENT_JOB_MANAGEMENT_RESOURCE
                + resourceId.id()
                + "/storageBrowser";
        DefaultLoader.getIdeHelper().openLinkInBrowser(url);
    }

    @Override
    protected String getServiceName(NodeActionEvent event) {
        return ActionConstants.parse(ActionConstants.StorageAccount.OPEN_STORAGE_BROWSER_IN_PORTAL).getServiceName();
    }

    @Override
    protected String getOperationName(NodeActionEvent event) {
        return ActionConstants.parse(ActionConstants.StorageAccount.OPEN_STORAGE_BROWSER_IN_PORTAL).getOperationName();
    }

}
