/*
 * Copyright (c) Microsoft Corporation
 *
 * All rights reserved.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.microsoft.azuretools.core.mvp.model.function;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;
import com.microsoft.azuretools.core.mvp.model.AzureMvpModel;
import com.microsoft.azuretools.core.mvp.model.ResourceEx;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AzureFunctionMvpModel {

    public static final String CANNOT_GET_FUNCTION_APP_WITH_ID = "Cannot get Function App with ID: ";
    private final Map<String, List<ResourceEx<FunctionApp>>> subscriptionIdToFunctionApps;

    private AzureFunctionMvpModel() {
        subscriptionIdToFunctionApps = new ConcurrentHashMap<>();
    }

    public static AzureFunctionMvpModel getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public FunctionApp getFunctionById(String sid, String id) throws IOException {
        Azure azure = AuthMethodManager.getInstance().getAzureClient(sid);
        FunctionApp app = azure.appServices().functionApps().getById(id);
        if (app == null) {
            throw new IOException(CANNOT_GET_FUNCTION_APP_WITH_ID + id); // TODO: specify the type of exception.
        }
        return app;
    }

    public void deleteFunction(String sid, String appId) throws IOException {
        AuthMethodManager.getInstance().getAzureClient(sid).appServices().functionApps().deleteById(appId);
        subscriptionIdToFunctionApps.remove(sid);
    }

    public void restartFunction(String sid, String appId) throws IOException {
        AuthMethodManager.getInstance().getAzureClient(sid).appServices().functionApps().getById(appId).restart();
    }

    public void startFunction(String sid, String appId) throws IOException {
        AuthMethodManager.getInstance().getAzureClient(sid).appServices().functionApps().getById(appId).start();
    }

    public void stopFunction(String sid, String appId) throws IOException {
        AuthMethodManager.getInstance().getAzureClient(sid).appServices().functionApps().getById(appId).stop();
    }

    /**
     * List app service plan by subscription id and resource group name.
     */
    public List<AppServicePlan> listAppServicePlanBySubscriptionIdAndResourceGroupName(String sid, String group)
            throws IOException {
        List<AppServicePlan> appServicePlans = new ArrayList<>();

        Azure azure = AuthMethodManager.getInstance().getAzureClient(sid);
        appServicePlans.addAll(azure.appServices().appServicePlans().listByResourceGroup(group));

        return appServicePlans;
    }

    /**
     * List app service plan by subscription id.
     */
    public List<AppServicePlan> listAppServicePlanBySubscriptionId(String sid) throws IOException {
        return AuthMethodManager.getInstance().getAzureClient(sid).appServices().appServicePlans().list();
    }

    /**
     * List all the Function Apps in selected subscriptions.
     *
     * @param forceReload flag indicating whether force to fetch latest data from server
     * @return list of Function App
     */
    public List<ResourceEx<FunctionApp>> listAllFunctions(final boolean forceReload) {
        final List<ResourceEx<FunctionApp>> functions = new ArrayList<>();
        List<Subscription> subs = AzureMvpModel.getInstance().getSelectedSubscriptions();
        if (subs.size() == 0) {
            return functions;
        }
        Observable.from(subs).flatMap((sd) ->
                Observable.create((subscriber) -> {
                    try {
                        List<ResourceEx<FunctionApp>> functionList = listFunctions(sd.subscriptionId(), forceReload);
                        synchronized (functions) {
                            functions.addAll(functionList);
                        }
                    } catch (IOException e) {
                        // swallow exception and skip error subscription
                    }
                    subscriber.onCompleted();
                }).subscribeOn(Schedulers.io()), subs.size()).subscribeOn(Schedulers.io()).toBlocking().subscribe();
        return functions;
    }

    /**
     * List all Function Apps by subscription id.
     */
    @NotNull
    public List<ResourceEx<FunctionApp>> listFunctions(final String subscriptionId, final boolean forceReload)
            throws IOException {
        if (!forceReload && subscriptionIdToFunctionApps.get(subscriptionId) != null) {
            return subscriptionIdToFunctionApps.get(subscriptionId);
        }

        List<ResourceEx<FunctionApp>> functions = new ArrayList<>();

        final Azure azure = AuthMethodManager.getInstance().getAzureClient(subscriptionId);
        functions = azure.appServices().functionApps()
                .list()
                .stream()
                .map(app -> new ResourceEx<FunctionApp>(app, subscriptionId))
                .collect(Collectors.toList());
        subscriptionIdToFunctionApps.put(subscriptionId, functions);

        return functions;
    }

    private static final class SingletonHolder {
        private static final AzureFunctionMvpModel INSTANCE = new AzureFunctionMvpModel();
    }
}
