/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.sqlserver.common;

import com.microsoft.azure.toolkit.intellij.database.ServerNameTextField;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.entity.CheckNameAvailabilityResultEntity;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.sqlserver.service.AzureSqlServer;

import java.util.function.Function;
import java.util.regex.Pattern;

public class SqlServerNameValidator implements Function<ServerNameTextField, AzureValidationInfo> {

    private static final Pattern PATTERN = Pattern.compile("^[a-z0-9][a-z0-9-]+[a-z0-9]$");

    @Override
    public AzureValidationInfo apply(ServerNameTextField textField) {
        final String value = textField.getValue();
        // validate availability
        CheckNameAvailabilityResultEntity resultEntity = Azure.az(AzureSqlServer.class).checkNameAvailability(textField.getSubscriptionId(), value);
        if (!resultEntity.isAvailable()) {
            String message = resultEntity.getUnavailabilityReason();
            if ("AlreadyExists".equalsIgnoreCase(resultEntity.getUnavailabilityReason())) {
                message = "The specified server name is already in use.";
            }
            return AzureValidationInfo.builder().input(textField).message(message).type(AzureValidationInfo.Type.ERROR).build();
        }
        return AzureValidationInfo.OK;
    }
}
