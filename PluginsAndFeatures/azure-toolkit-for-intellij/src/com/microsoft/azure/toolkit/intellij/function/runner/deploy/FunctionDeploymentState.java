/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.function.runner.deploy;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.microsoft.azure.toolkit.intellij.common.AzureRunProfileState;
import com.microsoft.azure.toolkit.intellij.common.messager.IntellijAzureMessager;
import com.microsoft.azure.toolkit.intellij.function.runner.core.FunctionUtils;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.AzureAppService;
import com.microsoft.azure.toolkit.lib.appservice.service.IFunctionApp;
import com.microsoft.azure.toolkit.lib.common.exception.AzureExecutionException;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.common.telemetry.AzureTelemetry;
import com.microsoft.azure.toolkit.lib.function.FunctionAppService;
import com.microsoft.azure.toolkit.lib.legacy.function.configurations.FunctionConfiguration;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetrywrapper.Operation;
import com.microsoft.azuretools.telemetrywrapper.TelemetryManager;
import com.microsoft.azuretools.utils.AzureUIRefreshCore;
import com.microsoft.azuretools.utils.AzureUIRefreshEvent;
import com.microsoft.intellij.RunProcessHandler;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class FunctionDeploymentState extends AzureRunProfileState<IFunctionApp> {

    private final FunctionDeployConfiguration functionDeployConfiguration;
    private final FunctionDeployModel deployModel;
    private File stagingFolder;

    /**
     * Place to execute the Web App deployment task.
     */
    public FunctionDeploymentState(Project project, FunctionDeployConfiguration functionDeployConfiguration) {
        super(project);
        this.functionDeployConfiguration = functionDeployConfiguration;
        this.deployModel = functionDeployConfiguration.getModel();
    }

    @Nullable
    @Override
    @AzureOperation(name = "function.deploy.state", type = AzureOperation.Type.ACTION)
    public IFunctionApp executeSteps(@NotNull RunProcessHandler processHandler, @NotNull Operation operation) {
        final FunctionDeploymentMessenger messenger = new FunctionDeploymentMessenger(processHandler);
        AzureMessager.getContext().setMessager(messenger);
        final IFunctionApp functionApp;
        if (StringUtils.isEmpty(functionDeployConfiguration.getFunctionId())) {
            functionApp = createFunctionApp(processHandler);
        } else {
            functionApp = Azure.az(AzureAppService.class).subscription(functionDeployConfiguration.getSubscriptionId())
                    .functionApp(functionDeployConfiguration.getSubscriptionId());
        }
        // Deploy function to Azure
        stagingFolder = FunctionUtils.getTempStagingFolder();
        prepareStagingFolder(stagingFolder, processHandler, operation);
        FunctionAppService.getInstance().deployFunctionApp(functionApp, stagingFolder);
        operation.trackProperties(AzureTelemetry.getContext().getProperties());
        return functionApp;
    }

    private IFunctionApp createFunctionApp(@NotNull RunProcessHandler processHandler) {
        IFunctionApp functionApp = Azure.az(AzureAppService.class)
                .subscription(functionDeployConfiguration.getSubscriptionId())
                .functionApp(functionDeployConfiguration.getConfig().getResourceGroup().getName(), functionDeployConfiguration.getAppName());
        AzureTelemetry.getContext().setProperty("isCreateNewApp", String.valueOf(functionApp == null));
        if (functionApp != null) {
            return functionApp;
        }
        processHandler.setText(message("function.create.hint.creating", functionDeployConfiguration.getAppName()));
        functionApp = FunctionAppService.getInstance().createFunctionApp(deployModel.getFunctionAppConfig());
        functionDeployConfiguration.setFunctionId(functionApp.id()); // update run configuration
        processHandler.setText(message("function.create.hint.created", functionDeployConfiguration.getAppName()));
        return functionApp;
    }

    @AzureOperation(
        name = "function.prepare_staging_folder_detail",
        params = {"stagingFolder.getName()", "this.deployModel.getAppName()"},
        type = AzureOperation.Type.TASK
    )
    private void prepareStagingFolder(File stagingFolder, RunProcessHandler processHandler, final @NotNull Operation operation) {
        AzureTaskManager.getInstance().read(() -> {
            final Path hostJsonPath = FunctionUtils.getDefaultHostJson(project);
            final PsiMethod[] methods = FunctionUtils.findFunctionsByAnnotation(functionDeployConfiguration.getModule());
            final Path folder = stagingFolder.toPath();
            try {
                final Map<String, FunctionConfiguration> configMap =
                    FunctionUtils.prepareStagingFolder(folder, hostJsonPath, functionDeployConfiguration.getModule(), methods);
                operation.trackProperty(TelemetryConstants.TRIGGER_TYPE, StringUtils.join(FunctionUtils.getFunctionBindingList(configMap), ","));
            } catch (final AzureExecutionException | IOException e) {
                final String error = String.format("failed prepare staging folder[%s]", folder);
                throw new AzureToolkitRuntimeException(error, e);
            }
        });
    }

    @Override
    protected Operation createOperation() {
        return TelemetryManager.createOperation(TelemetryConstants.FUNCTION, TelemetryConstants.DEPLOY_FUNCTION_APP);
    }

    @Override
    @AzureOperation(
        name = "function.complete_deployment",
        params = {"this.deployModel.getAppName()"},
        type = AzureOperation.Type.TASK
    )
    protected void onSuccess(IFunctionApp result, @NotNull RunProcessHandler processHandler) {
        processHandler.setText(message("appService.deploy.hint.succeed"));
        processHandler.notifyComplete();
        FunctionUtils.cleanUpStagingFolder(stagingFolder);
        if (AzureUIRefreshCore.listeners != null) {
            AzureUIRefreshCore.execute(new AzureUIRefreshEvent(AzureUIRefreshEvent.EventType.REFRESH, result));
        }
    }

    @Override
    protected void onFail(@NotNull Throwable error, @NotNull RunProcessHandler processHandler) {
        super.onFail(error, processHandler);
        FunctionUtils.cleanUpStagingFolder(stagingFolder);
    }

    @Override
    protected Map<String, String> getTelemetryMap() {
        return functionDeployConfiguration.getModel().getTelemetryProperties();
    }

    // todo: create shared run state messenger for all run states
    @RequiredArgsConstructor
    private static class FunctionDeploymentMessenger extends IntellijAzureMessager {
        private final RunProcessHandler processHandler;

        @Override
        public void info(@Nonnull String message, String title) {
            processHandler.setText(message);
        }

        @Override
        public void success(@Nonnull String message, String title) {
            processHandler.println(message, ProcessOutputTypes.SYSTEM);
            super.success(message, title);
        }

        @Override
        public void error(@Nonnull String message, String title) {
            processHandler.println(message, ProcessOutputTypes.STDERR);
            super.error(message, title);
        }

        @Override
        public void warning(@Nonnull String message, String title) {
            processHandler.setText(message);
        }
    }
}
