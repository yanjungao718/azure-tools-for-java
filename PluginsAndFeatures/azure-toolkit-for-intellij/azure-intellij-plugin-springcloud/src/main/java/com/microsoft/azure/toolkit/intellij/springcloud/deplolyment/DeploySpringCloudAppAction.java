/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */


package com.microsoft.azure.toolkit.intellij.springcloud.deplolyment;

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.impl.RunDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Objects;

public class DeploySpringCloudAppAction extends AnAction {
    private static final String DEPLOY_SPRING_CLOUD_APP_TITLE = "Deploy Azure Spring Cloud App";
    private static final SpringCloudDeploymentConfigurationType configType = SpringCloudDeploymentConfigurationType.getInstance();

    @AzureOperation(name = "springcloud.deploy", type = AzureOperation.Type.ACTION)
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        final Project project = anActionEvent.getProject();
        if (project != null) {
            deploy(null, project);
        }
    }

    public static void deploy(@Nullable SpringCloudApp app, @Nonnull Project project) {
        final RunManagerEx manager = RunManagerEx.getInstanceEx(project);
        final ConfigurationFactory factory = configType.getConfigurationFactories()[0];
        final String configurationName = String.format("%s: %s", factory.getName(), project.getName());
        final RunnerAndConfigurationSettings existed = manager.findConfigurationByName(configurationName);
        final RunnerAndConfigurationSettings settings = Objects.nonNull(existed) ? existed : manager.createConfiguration(configurationName, factory);
        final SpringCloudDeploymentConfiguration configuration = ((SpringCloudDeploymentConfiguration) settings.getConfiguration());
        configuration.setApp(app);
        AzureTaskManager.getInstance().runLater(() -> {
            if (RunDialog.editConfiguration(project, settings, DEPLOY_SPRING_CLOUD_APP_TITLE, DefaultRunExecutor.getRunExecutorInstance())) {
                settings.storeInLocalWorkspace();
                manager.addConfiguration(settings);
                manager.setBeforeRunTasks(configuration, new ArrayList<>(manager.getBeforeRunTasks(settings.getConfiguration())));
                manager.setSelectedConfiguration(settings);
                ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance());
            }
        });
    }
}
