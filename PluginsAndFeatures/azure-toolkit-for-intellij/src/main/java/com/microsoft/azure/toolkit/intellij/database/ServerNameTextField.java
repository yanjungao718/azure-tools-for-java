/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.database;

import com.microsoft.azure.toolkit.intellij.common.AzureTextInput;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ServerNameTextField extends AzureTextInput {

    private static final Pattern PATTERN = Pattern.compile("^[a-z0-9][a-z0-9-]+[a-z0-9]$");
    @Setter
    private int minLength = 3;
    @Setter
    private int maxLength = 63;
    @Setter
    @Getter
    private String subscriptionId;
    @Setter
    private Function<ServerNameTextField, AzureValidationInfo> validateFunction;

    public ServerNameTextField() {
        this.setValidator(this::doValidateValue);
        this.setRequired(true);
    }

    @Nonnull
    public AzureValidationInfo doValidateValue() {
        if (StringUtils.isBlank(subscriptionId)) {
            return AzureValidationInfo.UNINITIALIZED;
        }
        final String value = this.getValue();
        // validate length
        if (StringUtils.length(value) < minLength || StringUtils.length(value) > maxLength) {
            return AzureValidationInfo.builder().input(this)
                .message(String.format("Server name must be at least %s characters and at most %s characters.", minLength, maxLength))
                .type(AzureValidationInfo.Type.ERROR).build();
        }
        // validate special character
        if (!PATTERN.matcher(value).matches()) {
            return AzureValidationInfo.builder().input(this)
                .message("Your server name can contain only lowercase letters, numbers, and '-', but can't start or end with '-'.")
                .type(AzureValidationInfo.Type.ERROR).build();
        }
        // validate availability
        if (Objects.nonNull(validateFunction)) {
            return validateFunction.apply(this);
        }
        return AzureValidationInfo.OK;
    }
}
