/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.appservice;

import com.microsoft.azure.toolkit.intellij.common.AzureTextInput;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.intellij.util.ValidationUtils;

import javax.annotation.Nonnull;
import java.util.Objects;

public class AppNameInput extends AzureTextInput {
    private Subscription subscription;

    public AppNameInput() {
        this.setValidator(this::doValidateValue);
    }

    public void setSubscription(Subscription subscription) {
        if (!Objects.equals(subscription, this.subscription)) {
            this.subscription = subscription;
            this.revalidateValue();
        }
    }

    @Nonnull
    public AzureValidationInfo doValidateValue() {
        try {
            ValidationUtils.validateAppServiceName(subscription != null ? subscription.getId() : null, this.getValue());
        } catch (final IllegalArgumentException e) {
            final AzureValidationInfo.AzureValidationInfoBuilder builder = AzureValidationInfo.builder();
            return builder.input(this).message(e.getMessage()).type(AzureValidationInfo.Type.ERROR).build();
        }
        return AzureValidationInfo.success(this);
    }
}
