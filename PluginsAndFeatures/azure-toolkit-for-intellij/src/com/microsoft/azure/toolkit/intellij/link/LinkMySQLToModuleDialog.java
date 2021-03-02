/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.link;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.AzureDialog;
import com.microsoft.azure.toolkit.intellij.link.base.LinkType;
import com.microsoft.azure.toolkit.intellij.link.mysql.BasicLinkMySQLPanel;
import com.microsoft.azure.toolkit.intellij.link.mysql.MySQLConnectionUtils;
import com.microsoft.azure.toolkit.intellij.link.mysql.MySQLLinkConfig;
import com.microsoft.azure.toolkit.intellij.link.po.LinkPO;
import com.microsoft.azure.toolkit.intellij.link.po.ModulePO;
import com.microsoft.azure.toolkit.intellij.link.po.MySQLServicePO;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.intellij.AzureLinkStorage;
import com.microsoft.intellij.AzureMySQLStorage;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.serviceexplorer.azure.mysql.MySQLNode;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class LinkMySQLToModuleDialog extends AzureDialog<LinkComposite<MySQLLinkConfig, ModuleLinkConfig>> {
    private JPanel rootPanel;
    private BasicLinkMySQLPanel basicPanel;

    private MySQLNode node;
    private Project project;
    private Module module;

    public LinkMySQLToModuleDialog(Project project, MySQLNode node, Module module) {
        super(project);
        this.project = project;
        this.node = node;
        this.module = module;
        this.init();
        // listener
        this.setOkActionListener((data) -> doLink(data, project, this));
    }

    @Override
    public AzureForm<LinkComposite<MySQLLinkConfig, ModuleLinkConfig>> getForm() {
        return basicPanel;
    }

    @Override
    protected String getDialogTitle() {
        return "Link Azure Database for MySQL";
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return rootPanel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        MySQLLinkConfig source = MySQLLinkConfig.getDefaultConfig(node);
        ModuleLinkConfig target = ModuleLinkConfig.getDefaultConfig(module);
        LinkComposite<MySQLLinkConfig, ModuleLinkConfig> linker = new LinkComposite<>(source, target);
        basicPanel = new BasicLinkMySQLPanel(project, () -> linker);
    }

    private void doLink(LinkComposite<MySQLLinkConfig, ModuleLinkConfig> linker, Project project, LinkMySQLToModuleDialog dialog) {
        final Runnable runnable = () -> {
            final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
            indicator.setIndeterminate(true);
            DefaultLoader.getIdeHelper().invokeLater(dialog::close);
            ModulePO modulePO = createModulePO(linker.getModule());
            MySQLServicePO mysqlPO = createMySQLPO(linker.getService());
            LinkPO linkerPO = new LinkPO(mysqlPO.getId(), modulePO.getId(), LinkType.SERVICE_WITH_MODULE, linker.getEnvPrefix());
            AzureMySQLStorage.getStorage().addService(mysqlPO);
            if (ArrayUtils.isNotEmpty(linker.getService().getPasswordConfig().getPassword())) {
                String inputPassword = String.valueOf(linker.getService().getPasswordConfig().getPassword());
                if (MySQLConnectionUtils.connect(mysqlPO.getUrl(), mysqlPO.getUsername(), inputPassword)) {
                    AzureMySQLStorage.getStorage().savePassword(mysqlPO, mysqlPO.getPasswordSave(), mysqlPO.getUsername(), inputPassword);
                }
            }
            AzureLinkStorage.getProjectStorage(project).addLinker(linkerPO);
        };
        String progressMessage = "Linking Azure Database for MySQL with Module...";
        final AzureTask task = new AzureTask(null, progressMessage, false, runnable);
        AzureTaskManager.getInstance().runInBackground(task);
        System.out.println("Link MySQL to Project...OK--action....");
    }

    private ModulePO createModulePO(ModuleLinkConfig config) {
        return new ModulePO(config.getModule().getName());
    }

    private MySQLServicePO createMySQLPO(MySQLLinkConfig config) {
        MySQLServicePO mysqlPO = new MySQLServicePO.Builder()
                .id(config.getId())
                .url(config.getUrl())
                .username(config.getUsername())
                .passwordSave(config.getPasswordConfig().getPasswordSaveType())
                .build();
        return mysqlPO;
    }

}
