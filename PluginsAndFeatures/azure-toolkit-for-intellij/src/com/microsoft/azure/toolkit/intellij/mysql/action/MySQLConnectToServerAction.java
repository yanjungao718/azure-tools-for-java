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

package com.microsoft.azure.toolkit.intellij.mysql.action;

import com.intellij.database.autoconfig.DataSourceDetector;
import com.intellij.database.autoconfig.DataSourceRegistry;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.view.ui.DataSourceManagerDialog;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.project.Project;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.ijidea.actions.AzureSignInAction;
import com.microsoft.intellij.AzurePlugin;
import com.microsoft.intellij.util.AzureLoginHelper;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.Name;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionEvent;
import com.microsoft.tooling.msservices.serviceexplorer.NodeActionListener;
import com.microsoft.tooling.msservices.serviceexplorer.azure.mysql.MySQLNode;
import org.apache.commons.lang3.StringUtils;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

@Name(MySQLConnectToServerAction.ACTION_NAME)
public class MySQLConnectToServerAction extends NodeActionListener {

    public static final String ACTION_NAME = "Connect to Server";
    private static final String MYSQL_PATTERN_NAME = "Azure Database for MySQL - %s";
    private static final String MYSQL_DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String MYSQL_PATTERN_URL = "jdbc:mysql://%s:3306?serverTimezone=UTC&useSSL=true&requireSSL=false";
    private static final String NOT_SUPPORT_IU_DIALOG_MESSAGE = "This action is only supported for Intellij Ultimate.";
    private static final String NOT_SUPPORT_IU_DIALOG_TITLE = "Azure Toolkit Error";

    private final MySQLNode node;
    private final Project project;

    public MySQLConnectToServerAction(MySQLNode node) {
        super();
        AllIcons.Debugger.Db_db_object.toString();
        this.node = node;
        this.project = (Project) node.getProject();
    }

    @Override
    public void actionPerformed(NodeActionEvent e) {
        try {
            if (!AzureSignInAction.doSignIn(AuthMethodManager.getInstance(), project) ||
                !AzureLoginHelper.isAzureSubsAvailableOrReportError(message("common.error.signIn"))) {
                return;
            }
        } catch (final Exception ex) {
            AzurePlugin.log(message("common.error.signIn"), ex);
            DefaultLoader.getUIHelper().showException(message("common.error.signIn"), ex, message("common.error.signIn"), false, true);
        }
        // PluginManager.getPlugin(PluginId.findId(SCALA_PLUGIN_ID));
        ApplicationInfo applicationInfo = ApplicationInfo.getInstance();
        if (!StringUtils.containsIgnoreCase(applicationInfo.getFullVersion(), "IU")
                && !StringUtils.containsIgnoreCase(applicationInfo.getBuild().asString(), "IU")) {
            DefaultLoader.getUIHelper().showError(NOT_SUPPORT_IU_DIALOG_MESSAGE, NOT_SUPPORT_IU_DIALOG_TITLE);
        }
        DataSourceRegistry registry = new DataSourceRegistry(project);
        // registry.setImportedFlag(false);
        DataSourceDetector.Builder build = registry.getBuilder()
                .withName(String.format(MYSQL_PATTERN_NAME, node.getServer().name()))
                .withDriverClass(MYSQL_DEFAULT_DRIVER)
                .withUrl(String.format(MYSQL_PATTERN_URL, node.getServer().fullyQualifiedDomainName()))
                .withUser(node.getServer().administratorLogin() + "@" + node.getServer().name())
                .commit();
        DataSourceManagerDialog.showDialog(DbPsiFacade.getInstance(project), registry);
    }
}
