/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.function.runner.component.table;

import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.AzureAppService;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.FunctionApp;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azuretools.core.mvp.ui.base.MvpPresenter;
import rx.Observable;

import java.nio.file.Path;

public class AppSettingsDialogPresenter<V extends ImportAppSettingsView> extends MvpPresenter<V> {
    public void onLoadFunctionApps() {
        Observable.fromCallable(() -> Azure.az(AzureAppService.class).functionApps())
                .subscribeOn(getSchedulerProvider().io())
                .subscribe(functionApps -> AzureTaskManager.getInstance().runLater(() -> {
                    if (isViewDetached()) {
                        return;
                    }
                    getMvpView().fillFunctionApps(functionApps);
                }));
    }

    public void onLoadFunctionAppSettings(FunctionApp functionApp) {
        Observable.fromCallable(() -> {
            getMvpView().beforeFillAppSettings();
            return functionApp.entity().getAppSettings();
        }).subscribeOn(getSchedulerProvider().io())
                .subscribe(appSettings -> AzureTaskManager.getInstance().runLater(() -> {
                    if (isViewDetached()) {
                        return;
                    }
                    getMvpView().fillFunctionAppSettings(appSettings);
                }));
    }

    public void onLoadLocalSettings(Path localSettingsJsonPath) {
        Observable.fromCallable(() -> {
            getMvpView().beforeFillAppSettings();
            return AppSettingsTableUtils.getAppSettingsFromLocalSettingsJson(localSettingsJsonPath.toFile());
        }).subscribeOn(getSchedulerProvider().io())
                .subscribe(appSettings -> AzureTaskManager.getInstance().runLater(() -> {
                    if (isViewDetached()) {
                        return;
                    }
                    getMvpView().fillFunctionAppSettings(appSettings);
                }));
    }

    private void errorHandler(Throwable e) {
        AzureTaskManager.getInstance().runLater(() -> getMvpView().onErrorWithException("Failed to load app settings", (Exception) e));
    }
}
