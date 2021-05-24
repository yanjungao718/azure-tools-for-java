/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.mysql;

import com.microsoft.azure.toolkit.intellij.database.ServerNameTextField;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azuretools.core.mvp.model.mysql.MySQLMvpModel;

import java.util.function.Function;

public class MySQLNameValidator implements Function<ServerNameTextField, AzureValidationInfo> {

    @Override
    public AzureValidationInfo apply(ServerNameTextField textField) {
        final String value = textField.getValue();
        // validate availability
        if (!MySQLMvpModel.checkNameAvailabilitys(textField.getSubscriptionId(), value)) {
            return AzureValidationInfo.builder().input(textField).message(value + " already existed.").type(AzureValidationInfo.Type.ERROR).build();
        }
        return AzureValidationInfo.OK;
    }
}
