/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.legacy.appservice.insights;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.microsoft.azure.toolkit.ide.appservice.model.ApplicationInsightsConfig;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.intellij.legacy.function.runner.component.CreateApplicationInsightsDialog;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.applicationinsights.ApplicationInsights;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import org.apache.commons.collections.ListUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.microsoft.azure.toolkit.intellij.common.AzureBundle.message;

public class ApplicationInsightsComboBox extends AzureComboBox<ApplicationInsightsConfig> {

    private Subscription subscription;

    public void setSubscription(Subscription subscription) {
        if (Objects.equals(subscription, this.subscription)) {
            return;
        }
        this.subscription = subscription;
        if (subscription == null) {
            this.clear();
            return;
        }
        this.refreshItems();
    }

    @Override
    public void setValue(final ApplicationInsightsConfig insightsConfig) {
        if (insightsConfig != null && insightsConfig.isNewCreate() && !getItems().contains(insightsConfig)) {
            addItem(insightsConfig);
        }
        super.setValue(insightsConfig);
    }

    @Nonnull
    @Override
    @AzureOperation(
        name = "ai.list_ais.subscription",
        params = {"this.subscription.getId()"},
        type = AzureOperation.Type.SERVICE
    )
    protected List<? extends ApplicationInsightsConfig> loadItems() throws Exception {
        final List<ApplicationInsightsConfig> newItems =
                getItems().stream().filter(ApplicationInsightsConfig::isNewCreate).collect(Collectors.toList());
        final List<ApplicationInsightsConfig> existingItems =
                subscription == null ? Collections.emptyList() :
                        Azure.az(ApplicationInsights.class).list().stream()
                                .map(instance -> new ApplicationInsightsConfig(instance.getName(), instance.getInstrumentationKey()))
                                .collect(Collectors.toList());
        return ListUtils.union(newItems, existingItems);
    }

    @Nullable
    @Override
    protected ExtendableTextComponent.Extension getExtension() {
        return ExtendableTextComponent.Extension.create(
            AllIcons.General.Add, message("appService.insights.create.tooltip"), this::onCreateApplicationInsights);
    }

    @Override
    protected String getItemText(final Object item) {
        if (!(item instanceof ApplicationInsightsConfig)) {
            return EMPTY_ITEM;
        }
        final ApplicationInsightsConfig model = (ApplicationInsightsConfig) item;
        return ((ApplicationInsightsConfig) item).isNewCreate() ? String.format("(New) %s", model.getName()) : model.getName();
    }

    private void onCreateApplicationInsights() {
        final CreateApplicationInsightsDialog dialog = new CreateApplicationInsightsDialog();
        dialog.pack();
        if (dialog.showAndGet()) {
            ApplicationInsightsConfig config = ApplicationInsightsConfig.builder().newCreate(true).name(dialog.getApplicationInsightsName()).build();
            addItem(config);
            setSelectedItem(config);
        }
    }
}
