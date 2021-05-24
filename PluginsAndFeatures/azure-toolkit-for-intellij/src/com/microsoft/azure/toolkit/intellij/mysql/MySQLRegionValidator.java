/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.mysql;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.mysql.v2020_01_01.implementation.MySQLManager;
import com.microsoft.azure.management.mysql.v2020_01_01.implementation.PerformanceTierPropertiesInner;
import com.microsoft.azure.toolkit.intellij.database.RegionComboBox;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.function.Function;

public class MySQLRegionValidator implements Function<RegionComboBox, AzureValidationInfo> {

    private static final String REGION_UNAVAILABLE_MESSAGE = "Currently, the service is not available in this location for your subscription.";

    @Override
    public AzureValidationInfo apply(RegionComboBox comboBox) {
        MySQLManager manager = AuthMethodManager.getInstance().getMySQLManager(comboBox.getSubscription().getId());
        try {
            List<PerformanceTierPropertiesInner> tiers = manager.locationBasedPerformanceTiers().inner().list(comboBox.getValue().getName());
            if (tiers.stream().anyMatch(e -> CollectionUtils.isNotEmpty(e.serviceLevelObjectives()))) {
                return AzureValidationInfo.OK;
            }
        } catch (CloudException e) {
            return AzureValidationInfo.builder().input(comboBox).message(e.getMessage()).type(AzureValidationInfo.Type.ERROR).build();
        }
        final AzureValidationInfo.AzureValidationInfoBuilder builder = AzureValidationInfo.builder();
        return builder.input(comboBox).message(REGION_UNAVAILABLE_MESSAGE).type(AzureValidationInfo.Type.ERROR).build();
    }
}
