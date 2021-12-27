/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.function.runner.localrun;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.AzureIcons;
import com.microsoft.tooling.msservices.serviceexplorer.AzureIconSymbol;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class FunctionRunConfigurationFactory extends ConfigurationFactory {
    private static final String FACTORY_NAME = "Run Functions";

    public FunctionRunConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new FunctionRunConfiguration(project, this, project.getName());
    }

    @Override
    public RunConfiguration createConfiguration(String name, RunConfiguration template) {
        return new FunctionRunConfiguration(template.getProject(), this, name);
    }

    @Override
    public String getName() {
        return FACTORY_NAME;
    }

    @Override
    public Icon getIcon() {
        return AzureIcons.getIcon(AzureIconSymbol.FunctionApp.RUN.getPath());
    }

    @Override
    public @NotNull @NonNls String getId() {
        return FACTORY_NAME;
    }
}
