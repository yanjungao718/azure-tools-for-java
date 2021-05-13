/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.sqlserver.common;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Location;
import com.microsoft.azure.management.resources.RegionType;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.sqlserver.service.AzureSqlServer;
import com.microsoft.azuretools.azurecommons.helpers.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SqlServerRegionComboBox extends AzureComboBox<Region> {

    private static final String REGION_UNAVAILABLE_MESSAGE = "Your subscription does not have access to create a server "
        + "in the selected region. For the latest information about region availability for your subscription, go to "
        + "aka.ms/sqlcapacity. Please try another region or create a support ticket to request access.";

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

    @NotNull
    @Override
    @AzureOperation(
        name = "mysql|region.list.supported",
        type = AzureOperation.Type.SERVICE
    )
    protected List<? extends Region> loadItems() {
        if (Objects.isNull(subscription)) {
            return new ArrayList<>();
        }
        return loadRegions(subscription);
    }

    @Override
    protected String getItemText(Object item) {
        if (item instanceof Region) {
            return ((Region) item).getLabel();
        }
        return super.getItemText(item);
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @NotNull
    @Override
    public AzureValidationInfo doValidate() {
        final AzureValidationInfo info = super.doValidate();
        if (!AzureValidationInfo.OK.equals(info)) {
            return info;
        }
        AzureSqlServer service = com.microsoft.azure.toolkit.lib.Azure.az(AzureSqlServer.class);
        if (!service.checkRegionCapability(subscription.subscriptionId(), getValue().getName())) {
            final AzureValidationInfo.AzureValidationInfoBuilder builder = AzureValidationInfo.builder();
            return builder.input(this).message(REGION_UNAVAILABLE_MESSAGE).type(AzureValidationInfo.Type.ERROR).build();
        }
        return info;
    }

    /**
     * TODO: replace codes after merge andy's code.
     */
    private List<Region> loadRegions(Subscription subscription) {
        PagedList<Location> locationList = subscription.listLocations();
        locationList.loadAll();
        return locationList.stream()
            .filter((e) -> RegionType.PHYSICAL.equals(e.regionType()))
            .map(e -> Region.fromName(e.name()))
            .distinct()
            .collect(Collectors.toList());
    }
}
