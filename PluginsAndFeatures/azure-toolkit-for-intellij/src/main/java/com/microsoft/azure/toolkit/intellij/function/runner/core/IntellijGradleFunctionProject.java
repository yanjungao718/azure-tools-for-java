/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.function.runner.core;

import com.intellij.openapi.externalSystem.model.project.ExternalProjectPojo;
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.lib.appservice.function.core.FunctionMethod;
import com.microsoft.azure.toolkit.lib.appservice.function.core.FunctionProject;
import lombok.Getter;
import org.jetbrains.plugins.gradle.model.ExternalLibraryDependency;
import org.jetbrains.plugins.gradle.model.ExternalProject;
import org.jetbrains.plugins.gradle.model.ExternalProjectDependency;
import org.jetbrains.plugins.gradle.model.ExternalSourceSet;
import org.jetbrains.plugins.gradle.service.project.data.ExternalProjectDataCache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IntellijGradleFunctionProject extends FunctionProject {
    private final Project workspace;
    private final ExternalProject externalProject;
    @Getter
    private final boolean isValid;

    public IntellijGradleFunctionProject(Project workspace, Module module) {
        this.workspace = workspace;
        final String externalProjectPath = ExternalSystemApiUtil.getExternalProjectPath(module);
        externalProject = ExternalProjectDataCache.getInstance(workspace).getRootExternalProject(externalProjectPath);
        isValid = externalProject != null;
        init();
    }

    public IntellijGradleFunctionProject(Project workspace, ExternalProjectPojo project) {
        this.workspace = workspace;
        externalProject = ExternalProjectDataCache.getInstance(workspace).getRootExternalProject(project.getPath());
        isValid = externalProject != null;
        init();
    }

    private void init() {
        if (!isValid) {
            return;
        }

        this.setName(externalProject.getName());
        if (!externalProject.getArtifacts().isEmpty()) {
            this.setArtifactFile(externalProject.getArtifacts().get(0));
        }
        this.setBaseDirectory(externalProject.getProjectDir());

        final ExternalSourceSet main = externalProject.getSourceSets().get("main");
        final List<File> dependencies = new ArrayList<>();
        if (main != null) {
            main.getDependencies().forEach(t -> {
                if (t instanceof ExternalLibraryDependency) {
                    dependencies.add(((ExternalLibraryDependency) t).getFile());
                }
                if (t instanceof ExternalProjectDependency) {
                    final ExternalProject childProject = this.externalProject.getChildProjects().get(t.getName());
                    if (childProject != null) {
                        dependencies.addAll(childProject.getArtifacts());
                    }
                }

            });
        }
        setDependencies(dependencies);
    }

    @Override
    public List<FunctionMethod> findAnnotatedMethods() {
        throw new UnsupportedOperationException("findAnnotatedMethods in intellij is not supported by now.");
    }

    @Override
    public void installExtension(String funcPath) {
        throw new UnsupportedOperationException("installExtension in intellij is not supported by now.");
    }
}
