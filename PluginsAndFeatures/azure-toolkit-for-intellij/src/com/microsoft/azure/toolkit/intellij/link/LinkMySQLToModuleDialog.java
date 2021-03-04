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
import com.microsoft.azure.toolkit.intellij.link.mysql.BasicLinkMySQLPanel;
import com.microsoft.azure.toolkit.intellij.link.mysql.MySQLResourceConfig;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.link.AzureLinkService;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.serviceexplorer.azure.mysql.MySQLNode;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class LinkMySQLToModuleDialog extends AzureDialog<LinkConfig<MySQLResourceConfig, ModuleResourceConfig>> {
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
    public AzureForm<LinkConfig<MySQLResourceConfig, ModuleResourceConfig>> getForm() {
        return basicPanel;
    }

    public String showAndGetEnvPrefix() {
        boolean exitWithOkButton = this.showAndGet();
        return exitWithOkButton ? basicPanel.getData().getEnvPrefix() : StringUtils.EMPTY;
    }

    @Override
    protected String getDialogTitle() {
        return "Connect Azure Database for MySQL";
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return rootPanel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        MySQLResourceConfig source = MySQLResourceConfig.getDefaultConfig(node);
        ModuleResourceConfig target = ModuleResourceConfig.getDefaultConfig(module);
        LinkConfig<MySQLResourceConfig, ModuleResourceConfig> linkConfig = new LinkConfig<>(source, target);
        basicPanel = new BasicLinkMySQLPanel(project, () -> linkConfig);
    }

    private void doLink(LinkConfig<MySQLResourceConfig, ModuleResourceConfig> linkConfig, Project project, LinkMySQLToModuleDialog dialog) {
        dialog.close(0);
        final Runnable runnable = () -> {
            final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
            indicator.setIndeterminate(true);
            AzureLinkService.getInstance().link(project, linkConfig);
        };
        String progressMessage = "Linking Azure Database for MySQL with Module...";
        final AzureTask task = new AzureTask(null, progressMessage, false, runnable);
        AzureTaskManager.getInstance().runInBackground(task);
    }

}
