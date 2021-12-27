/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.function.runner.deploy.ui;

import com.microsoft.azure.toolkit.lib.appservice.service.impl.FunctionApp;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azuretools.core.mvp.ui.base.MvpPresenter;
import com.microsoft.intellij.util.RxJavaUtils;
import rx.Observable;
import rx.Subscription;

public class FunctionDeployViewPresenter<V extends FunctionDeployMvpView> extends MvpPresenter<V> {

    private Subscription loadAppSettingsSubscription;

    @AzureOperation(
        name = "function.load_setting.app",
        params = {"functionApp.name()"},
        type = AzureOperation.Type.SERVICE
    )
    public void loadAppSettings(FunctionApp functionApp) {
        if (functionApp == null) {
            return;
        }
        RxJavaUtils.unsubscribeSubscription(loadAppSettingsSubscription);
        loadAppSettingsSubscription =
            Observable.fromCallable(() -> functionApp.entity().getAppSettings()).subscribeOn(getSchedulerProvider().io())
                      .subscribe(appSettings -> AzureTaskManager.getInstance().runLater(() -> {
                          if (isViewDetached()) {
                              return;
                          }
                          getMvpView().fillAppSettings(appSettings);
                      }, AzureTask.Modality.ANY));
    }
}
