/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.handler.IntelliJAzureExceptionHandler;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitException;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azuretools.core.mvp.ui.base.SchedulerProviderFactory;
import com.microsoft.azuretools.telemetrywrapper.ErrorType;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import com.microsoft.azuretools.telemetrywrapper.Operation;
import com.microsoft.intellij.RunProcessHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import rx.Observable;

import java.util.HashMap;
import java.util.Map;

public abstract class AzureRunProfileState<T> implements RunProfileState {
    protected final Project project;

    public AzureRunProfileState(@NotNull Project project) {
        this.project = project;
    }

    @Nullable
    @Override
    public ExecutionResult execute(Executor executor, @NotNull ProgramRunner programRunner) throws ExecutionException {
        final RunProcessHandler processHandler = new RunProcessHandler();
        processHandler.addDefaultListener();
        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(this.project).getConsole();
        processHandler.startNotify();
        consoleView.attachToProcess(processHandler);
        Map<String, String> telemetryMap = new HashMap<>();
        final Operation operation = createOperation();
        Observable.fromCallable(
            () -> {
                try {
                    return this.executeSteps(processHandler, telemetryMap);
                } finally {
                    // Once the operation done, whether success or not, `setText` should not throw new exception
                    processHandler.setProcessTerminatedHandler(RunProcessHandler.DO_NOTHING);
                }
            }).subscribeOn(SchedulerProviderFactory.getInstance().getSchedulerProvider().io()).subscribe(
                (res) -> {
                    this.sendTelemetry(operation, telemetryMap, null);
                    this.onSuccess(res, processHandler);
                },
                (err) -> {
                    err.printStackTrace();
                    this.sendTelemetry(operation, telemetryMap, err);
                    this.onFail(err, processHandler);
                });
        return new DefaultExecutionResult(consoleView, processHandler);
    }

    protected void setText(RunProcessHandler runProcessHandler, String text) {
        if (runProcessHandler.isProcessRunning()) {
            runProcessHandler.setText(text);
        }
    }

    private void sendTelemetry(Operation operation, @NotNull Map<String, String> telemetryMap, Throwable exception) {
        updateTelemetryMap(telemetryMap);
        operation.trackProperties(telemetryMap);
        if (exception != null) {
            EventUtil.logError(operation, ErrorType.userError, new Exception(exception.getMessage(), exception), telemetryMap, null);
        }
        operation.complete();
    }

    protected abstract T executeSteps(@NotNull RunProcessHandler processHandler
        , @NotNull Map<String, String> telemetryMap) throws Exception;

    @NotNull
    protected abstract Operation createOperation();

    protected abstract void updateTelemetryMap(@NotNull Map<String, String> telemetryMap);

    protected abstract void onSuccess(T result, @NotNull RunProcessHandler processHandler);

    protected void onFail(@NotNull Throwable error, @NotNull RunProcessHandler processHandler) {
        final String errorMessage = (error instanceof AzureToolkitRuntimeException || error instanceof AzureToolkitException) ?
                                    String.format("Failed to %s", error.getMessage()) : error.getMessage();
        processHandler.println(errorMessage, ProcessOutputTypes.STDERR);
        processHandler.notifyComplete();
        IntelliJAzureExceptionHandler.getInstance().handleException(project, new AzureToolkitRuntimeException("execute run configuration", error), true);
    }
}
