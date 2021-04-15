/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector;

import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import lombok.Getter;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;

@Log
public class ConnectionRunnerForRunConfiguration extends BeforeRunTaskProvider<ConnectionRunnerForRunConfiguration.BeforeRunTask> {
    @Getter
    public String name = BeforeRunTask.NAME;
    @Getter
    public Key<BeforeRunTask> id = BeforeRunTask.ID;
    @Getter
    public Icon icon = BeforeRunTask.ICON;

    @Override
    public @Nullable Icon getTaskIcon(BeforeRunTask task) {
        return BeforeRunTask.ICON;
    }

    @Override
    public String getDescription(BeforeRunTask task) {
        return BeforeRunTask.DESCRIPTION;
    }

    @Nullable
    @Override
    public ConnectionRunnerForRunConfiguration.BeforeRunTask createTask(@NotNull RunConfiguration config) {
        return isApplicableFor(config) ? new BeforeRunTask(getId()) : null;
    }

    @Override
    public boolean executeTask(@NotNull DataContext dataContext, @NotNull RunConfiguration configuration,
                               @NotNull ExecutionEnvironment executionEnvironment, @NotNull BeforeRunTask beforeRunTask) {
        return beforeRunTask.execute(dataContext, configuration);
    }

    private static boolean isApplicableFor(@NotNull RunConfiguration config) {
        return ConnectionManager.getDefinitions().stream().anyMatch(d -> d.isApplicableFor(config));
    }

    public static class BeforeRunTask extends com.intellij.execution.BeforeRunTask<BeforeRunTask> {
        private static final String NAME = "Connect Azure Resource";
        private static final String DESCRIPTION = "Connect Azure Resource";
        private static final Icon ICON = IconLoader.getIcon("/icons/Common/Azure.svg");// AzureIconLoader.loadIcon(AzureIconSymbol.Common.AZURE);
        private static final Key<BeforeRunTask> ID = Key.create("ConnectionRunnerForConfigurationId");
        private List<Connection<? extends Resource, ? extends Resource>> connections;

        protected BeforeRunTask(@NotNull Key<BeforeRunTask> providerId) {
            super(providerId);
            setEnabled(true);
        }

        public boolean execute(@NotNull DataContext dataContext, @NotNull RunConfiguration configuration) {
            // find connections at runtime since connections may be created after before task added into RC.
            this.connections = configuration.getProject().getService(ConnectionManager.class).getConnections().stream()
                    .filter(c -> c.isApplicableFor(configuration)).collect(Collectors.toList());
            return this.connections.stream().allMatch(c -> c.prepareBeforeRun(configuration, dataContext));
        }
    }

    public static class RunConfigurationExtension extends com.intellij.execution.RunConfigurationExtension {

        @Override
        public <T extends RunConfigurationBase> void updateJavaParameters(@NotNull T configuration, @NotNull JavaParameters params, RunnerSettings settings) {
            final @NotNull List<?> beforeTasks = configuration.getBeforeRunTasks();
            beforeTasks.stream().filter(t -> t instanceof BeforeRunTask).map(t -> (BeforeRunTask) t)
                    .flatMap(t -> t.connections.stream())
                    .forEach(c -> c.updateJavaParametersAtRun(configuration, params));
        }

        @Override
        public boolean isApplicableFor(@NotNull RunConfigurationBase<?> configuration) {
            return ConnectionRunnerForRunConfiguration.isApplicableFor(configuration);
        }
    }
}
