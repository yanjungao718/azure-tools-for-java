/*
 * Copyright (c) Microsoft Corporation
 *
 * All rights reserved.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.mysql;

import com.microsoft.azure.management.mysql.v2020_01_01.Server;
import com.microsoft.azure.management.mysql.v2020_01_01.ServerState;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import com.microsoft.azuretools.core.mvp.model.mysql.MySQLMvpModel;
import com.microsoft.tooling.msservices.serviceexplorer.*;
import com.microsoft.tooling.msservices.serviceexplorer.listener.Backgroundable;
import com.microsoft.tooling.msservices.serviceexplorer.listener.Promptable;
import lombok.Getter;

import java.util.List;

public class MySQLNode extends Node {

    private static final ServerState SERVER_UPDATING = ServerState.fromString("Updating");

    public static final int OPERATE_GROUP = Groupable.DEFAULT_GROUP + 2;
    public static final int SHOW_PROPERTIES_PRIORITY = Sortable.DEFAULT_PRIORITY + 1;
    public static final int CONNECT_TO_SERVER_PRIORITY = Sortable.DEFAULT_PRIORITY + 2;

    @Getter
    private final String subscriptionId;
    @Getter
    private final Server server;
    private ServerState serverState;

    public MySQLNode(AzureRefreshableNode parent, String subscriptionId, Server server) {
        super(server.id(), server.name(), parent, true);
        this.subscriptionId = subscriptionId;
        this.server = server;
        this.serverState = server.userVisibleState();
        loadActions();
    }

    @Override
    public @Nullable AzureIconSymbol getIconSymbol() {
        boolean running = ServerState.READY.equals(serverState);
        boolean updating = SERVER_UPDATING.equals(serverState);
        return running ? AzureIconSymbol.MySQL.RUNNING : updating ? AzureIconSymbol.MySQL.UPDATING : AzureIconSymbol.MySQL.STOPPED;
    }

    @Override
    protected void loadActions() {
        addAction(new StartAzureMySQLAction().asGenericListener(AzureActionEnum.START));
        addAction(new StopAzureMySQLAction().asGenericListener(AzureActionEnum.STOP));
        addAction(new RestartAzureMySQLAction().asGenericListener(AzureActionEnum.RESTART));
        addAction(new DeleteAzureMySQLAction().asGenericListener(AzureActionEnum.DELETE));
        addAction(new OpenInBrowserAction().asGenericListener(AzureActionEnum.OPEN_IN_PORTAL));
        initActions();
    }

    @Override
    public List<NodeAction> getNodeActions() {
        boolean updating = SERVER_UPDATING.equals(serverState);
        boolean running = ServerState.READY.equals(serverState);
        getNodeActionByName(AzureActionEnum.START.getName()).setEnabled(!updating && !running);
        getNodeActionByName(AzureActionEnum.STOP.getName()).setEnabled(!updating && running);
        getNodeActionByName(AzureActionEnum.RESTART.getName()).setEnabled(!updating && running);
        getNodeActionByName(AzureActionEnum.DELETE.getName()).setEnabled(!updating);
        return super.getNodeActions();
    }

    private void refreshNode() {
        Server result = MySQLMvpModel.findServer(subscriptionId, server.resourceGroupName(), server.name());
        this.serverState = result.userVisibleState();
    }

    // Delete Azure MySQL action class
    private class DeleteAzureMySQLAction extends NodeActionListener implements Backgroundable, Promptable {

        private static final String DELETE_PROMPT_PATTERN = "This operation will delete your %s: %s. Are you sure you want to continue?";

        @Override
        protected void actionPerformed(NodeActionEvent e) {
            MySQLNode.this.serverState = SERVER_UPDATING;
            MySQLNode.this.getParent().removeNode(MySQLNode.this.getSubscriptionId(), MySQLNode.this.getId(), MySQLNode.this);
        }

        @Override
        public String getPromptMessage() {
            return Node.getPromptMessage(AzureActionEnum.DELETE.getName(), MySQLModule.MODULE_NAME, MySQLNode.this.name);
        }

        @Override
        public String getProgressMessage() {
            return Node.getProgressMessage(AzureActionEnum.DELETE.getDoingName(), MySQLModule.MODULE_NAME, MySQLNode.this.name);
        }
    }

    // Start Azure MySQL action class
    private class StartAzureMySQLAction extends NodeActionListener implements Backgroundable {

        @Override
        protected void actionPerformed(NodeActionEvent e) {
            MySQLNode.this.serverState = SERVER_UPDATING;
            MySQLMvpModel.start(MySQLNode.this.getSubscriptionId(), MySQLNode.this.getServer());
            MySQLNode.this.refreshNode();
        }

        @Override
        public String getProgressMessage() {
            return Node.getProgressMessage(AzureActionEnum.START.getDoingName(), MySQLModule.MODULE_NAME, MySQLNode.this.name);
        }
    }

    // Stop Azure MySQL action class
    private class StopAzureMySQLAction extends NodeActionListener implements Backgroundable {

        @Override
        protected void actionPerformed(NodeActionEvent e) {
            MySQLNode.this.serverState = SERVER_UPDATING;
            MySQLMvpModel.stop(MySQLNode.this.getSubscriptionId(), MySQLNode.this.getServer());
            MySQLNode.this.refreshNode();
        }

        @Override
        public String getProgressMessage() {
            return Node.getProgressMessage(AzureActionEnum.STOP.getDoingName(), MySQLModule.MODULE_NAME, MySQLNode.this.name);
        }
    }

    // Restart Azure MySQL action class
    private class RestartAzureMySQLAction extends NodeActionListener implements Backgroundable {

        @Override
        protected void actionPerformed(NodeActionEvent e) {
            MySQLNode.this.serverState = SERVER_UPDATING;
            MySQLMvpModel.restart(MySQLNode.this.getSubscriptionId(), MySQLNode.this.getServer());
            MySQLNode.this.refreshNode();
        }

        @Override
        public String getProgressMessage() {
            return Node.getProgressMessage(AzureActionEnum.RESTART.getDoingName(), MySQLModule.MODULE_NAME, MySQLNode.this.name);
        }
    }

    // Open in browser action class
    private class OpenInBrowserAction extends NodeActionListener implements Backgroundable {

        @Override
        protected void actionPerformed(NodeActionEvent e) {
            MySQLNode.this.openResourcesInPortal(MySQLNode.this.subscriptionId, MySQLNode.this.server.id());
        }

        @Override
        public String getProgressMessage() {
            return Node.getProgressMessage(AzureActionEnum.OPEN_IN_PORTAL.getDoingName(), MySQLModule.MODULE_NAME, MySQLNode.this.name);
        }
    }
}
