/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.container;

import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.AzureAppService;
import com.microsoft.azure.toolkit.lib.appservice.model.OperatingSystem;
import com.microsoft.azure.toolkit.lib.appservice.model.PricingTier;
import com.microsoft.azure.toolkit.lib.appservice.webapp.AzureWebApp;
import com.microsoft.azure.toolkit.lib.appservice.webapp.WebApp;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.resource.AzureResources;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroup;
import com.microsoft.azuretools.core.mvp.ui.base.MvpPresenter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.microsoft.azure.toolkit.lib.Azure.az;
import static com.microsoft.azure.toolkit.lib.appservice.model.PricingTier.WEB_APP_PRICING;

public class WebAppOnLinuxDeployPresenter<V extends WebAppOnLinuxDeployView> extends MvpPresenter<V> {
    private static final String CANNOT_LIST_WEB_APP = "Failed to list web apps.";
    private static final String CANNOT_LIST_SUBSCRIPTION = "Failed to list subscriptions.";
    private static final String CANNOT_LIST_RESOURCE_GROUP = "Failed to list resource group.";
    private static final String CANNOT_LIST_PRICING_TIER = "Failed to list pricing tier.";
    private static final String CANNOT_LIST_LOCATION = "Failed to list location.";
    private static final String CANNOT_LIST_APP_SERVICE_PLAN = "Failed to list app service plan.";

    private List<WebApp> retrieveListOfWebAppOnLinux(boolean force) {
        return Azure.az(AzureWebApp.class).webApps().stream()
            .filter(app -> Objects.requireNonNull(app.getRuntime()).getOperatingSystem() != OperatingSystem.WINDOWS) // docker and linux
            .collect(Collectors.toList());
    }

    /**
     * Load list of Web App on Linux from cache (if exists).
     */
    public void onLoadAppList() {
        Observable.fromCallable(() -> retrieveListOfWebAppOnLinux(false))
                .subscribeOn(getSchedulerProvider().io())
                .subscribe(webAppList -> AzureTaskManager.getInstance().runLater(() -> {
                    if (isViewDetached()) {
                        return;
                    }
                    getMvpView().renderWebAppOnLinuxList(webAppList);
                }, AzureTask.Modality.ANY));
    }

    /**
     * Force to refresh list of Web App on Linux.
     */
    public void onRefreshList() {
        Observable.fromCallable(() -> retrieveListOfWebAppOnLinux(true))
                .subscribeOn(getSchedulerProvider().io())
                .subscribe(webAppList -> AzureTaskManager.getInstance().runLater(() -> {
                    if (isViewDetached()) {
                        return;
                    }
                    getMvpView().renderWebAppOnLinuxList(webAppList);
                }, AzureTask.Modality.ANY));
    }

    /**
     * Load list of Subscriptions.
     */
    public void onLoadSubscriptionList() {
        Observable.fromCallable(() -> az(AzureAccount.class).account().getSelectedSubscriptions())
                .subscribeOn(getSchedulerProvider().io())
                .subscribe(subscriptions -> AzureTaskManager.getInstance().runLater(() -> {
                    if (isViewDetached()) {
                        return;
                    }
                    getMvpView().renderSubscriptionList(subscriptions);
                }, AzureTask.Modality.ANY));
    }

    /**
     * Load List of Resource Group by subscription id.
     *
     * @param sid Subscription Id.
     */
    public void onLoadResourceGroup(String sid) {
        Observable.fromCallable(() -> az(AzureResources.class).groups(sid).list())
                .subscribeOn(getSchedulerProvider().io())
                .subscribe(resourceGroupList -> AzureTaskManager.getInstance().runLater(() -> {
                    if (isViewDetached()) {
                        return;
                    }
                    getMvpView().renderResourceGroupList(resourceGroupList);
                }, AzureTask.Modality.ANY));
    }

    /**
     * Load List of Location by subscription id.
     *
     * @param sid Subscription Id.
     */
    public void onLoadLocationList(String sid) {
        Observable.fromCallable(() -> new ArrayList<>(az(AzureAccount.class).listRegions(sid)))
                .subscribeOn(getSchedulerProvider().io())
                .subscribe(locationList -> AzureTaskManager.getInstance().runLater(() -> {
                    if (isViewDetached()) {
                        return;
                    }
                    getMvpView().renderLocationList(locationList);
                }, AzureTask.Modality.ANY));

    }

    /**
     * Load List of Pricing Tier.
     */
    public void onLoadPricingTierList() {
        Observable.fromCallable(() -> new ArrayList<>(WEB_APP_PRICING))
                .subscribeOn(getSchedulerProvider().io())
                .subscribe(pricingTierList -> AzureTaskManager.getInstance().runLater(() -> {
                    if (isViewDetached()) {
                        return;
                    }
                    getMvpView().renderPricingTierList(pricingTierList.stream()
                            .filter(item -> !item.equals(PricingTier.FREE_F1) && !item.equals(PricingTier.SHARED_D1))
                            .collect(Collectors.toList()));
                }, AzureTask.Modality.ANY));
    }

    /**
     * Load list of App Service Plan by Subscription and Resource Group.
     *
     * @param sid Subscription Id.
     * @param rg  Resource group name.
     */
    public void onLoadAppServicePlan(String sid, String rg) {
        Mono.fromCallable(() -> Azure.az(AzureAppService.class).plans(sid)
                .listByResourceGroup(rg)).flatMapMany(Flux::fromIterable)
            .filter(asp -> OperatingSystem.LINUX == asp.getOperatingSystem())
            .subscribeOn(Schedulers.boundedElastic())
            .collectList().subscribe(appServicePlans -> AzureTaskManager.getInstance().runLater(() -> {
                if (isViewDetached()) {
                    return;
                }
                getMvpView().renderAppServicePlanList(appServicePlans);
            }, AzureTask.Modality.ANY));
    }

    /**
     * Load list of App Service Plan by Subscription.
     * TODO: Blocked by SDK, it can only list Windows ASP now.
     *
     * @param sid Subscription Id.
     */
    public void onLoadAppServicePlan(String sid) {
        Mono.fromCallable(() -> Azure.az(AzureAppService.class)
                .plans(sid).list()).flatMapMany(Flux::fromIterable)
            .filter(asp -> OperatingSystem.LINUX == asp.getOperatingSystem())
            .subscribeOn(Schedulers.boundedElastic())
            .collectList().subscribe(appServicePlans -> AzureTaskManager.getInstance().runLater(() -> {
                if (isViewDetached()) {
                    return;
                }
                getMvpView().renderAppServicePlanList(appServicePlans);
            }, AzureTask.Modality.ANY));
    }
}
