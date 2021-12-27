/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.function.runner.localrun.ui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.packaging.artifacts.Artifact;
import com.intellij.ui.ListCellRendererWrapper;
import com.microsoft.azure.toolkit.intellij.legacy.common.AzureSettingPanel;
import com.microsoft.azure.toolkit.intellij.legacy.function.runner.component.table.AppSettingsTable;
import com.microsoft.azure.toolkit.intellij.legacy.function.runner.component.table.AppSettingsTableUtils;
import com.microsoft.azure.toolkit.intellij.legacy.function.runner.core.FunctionUtils;
import com.microsoft.azure.toolkit.intellij.legacy.function.runner.localrun.FunctionRunConfiguration;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.legacy.function.FunctionCoreToolsCombobox;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProject;

import javax.swing.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static com.microsoft.azure.toolkit.intellij.common.AzureBundle.message;

public class FunctionRunPanel extends AzureSettingPanel<FunctionRunConfiguration> {

    private JPanel settings;
    private JPanel pnlMain;
    private FunctionCoreToolsCombobox txtFunc;
    private JPanel pnlAppSettings;
    private JComboBox<Module> cbFunctionModule;
    private AppSettingsTable appSettingsTable;
    private String appSettingsKey = UUID.randomUUID().toString();

    private FunctionRunConfiguration functionRunConfiguration;

    public FunctionRunPanel(@NotNull Project project, FunctionRunConfiguration functionRunConfiguration) {
        super(project);
        this.functionRunConfiguration = functionRunConfiguration;

        cbFunctionModule.setRenderer(new ListCellRendererWrapper<Module>() {
            @Override
            public void customize(JList list, Module module, int i, boolean b, boolean b1) {
                if (module != null) {
                    setText(module.getName());
                    setIcon(AllIcons.Nodes.Module);
                }
            }
        });


        fillModules();
    }

    @NotNull
    @Override
    public String getPanelName() {
        return message("function.run.title");
    }

    @Override
    public void disposeEditor() {
    }

    @Override
    protected void resetFromConfig(@NotNull FunctionRunConfiguration configuration) {
        if (MapUtils.isNotEmpty(configuration.getAppSettings())) {
            appSettingsTable.setAppSettings(configuration.getAppSettings());
        }
        if (StringUtils.isNotEmpty(configuration.getAppSettingsKey())) {
            this.appSettingsKey = configuration.getAppSettingsKey();
            appSettingsTable.setAppSettings(FunctionUtils.loadAppSettingsFromSecurityStorage(appSettingsKey));
        }
        // In case `FUNCTIONS_WORKER_RUNTIME` or `AZURE_WEB_JOB_STORAGE_KEY` was missed in configuration
        appSettingsTable.loadRequiredSettings();
        if (StringUtils.isNotEmpty(configuration.getFuncPath())) {
            txtFunc.setValue(configuration.getFuncPath());
        }
        for (int i = 0; i < cbFunctionModule.getItemCount(); i++) {
            final Module module = cbFunctionModule.getItemAt(i);
            if (StringUtils.equals(configuration.getModuleName(), module.getName())) {
                cbFunctionModule.setSelectedIndex(i);
                break;
            }
        }
    }

    @Override
    protected void apply(@NotNull FunctionRunConfiguration configuration) {
        configuration.setFuncPath(txtFunc.getItem());
        configuration.saveModule((Module) cbFunctionModule.getSelectedItem());
        FunctionUtils.saveAppSettingsToSecurityStorage(appSettingsKey, appSettingsTable.getAppSettings());
        // save app settings storage key instead of real value
        configuration.setAppSettings(Collections.EMPTY_MAP);
        configuration.setAppSettingsKey(appSettingsKey);
    }

    @NotNull
    @Override
    public JPanel getMainPanel() {
        return pnlMain;
    }

    @NotNull
    @Override
    protected JComboBox<Artifact> getCbArtifact() {
        return new JComboBox<Artifact>();
    }

    @NotNull
    @Override
    protected JLabel getLblArtifact() {
        return new JLabel();
    }

    @NotNull
    @Override
    protected JComboBox<MavenProject> getCbMavenProject() {
        return new JComboBox<MavenProject>();
    }

    @NotNull
    @Override
    protected JLabel getLblMavenProject() {
        return new JLabel();
    }

    private void createUIComponents() {
        txtFunc = new FunctionCoreToolsCombobox(project, true);
        final String localSettingPath = Paths.get(project.getBasePath(), "local.settings.json").toString();
        appSettingsTable = new AppSettingsTable(localSettingPath);
        pnlAppSettings = AppSettingsTableUtils.createAppSettingPanel(appSettingsTable);
        appSettingsTable.loadLocalSetting();
    }

    private void fillModules() {
        AzureTaskManager.getInstance()
                .runOnPooledThreadAsObservable(new AzureTask<>(() -> FunctionUtils.listFunctionModules(project)))
                .subscribe(modules -> AzureTaskManager.getInstance().runLater(() ->
                        Arrays.stream(modules).forEach(cbFunctionModule::addItem), AzureTask.Modality.ANY));
    }
}
