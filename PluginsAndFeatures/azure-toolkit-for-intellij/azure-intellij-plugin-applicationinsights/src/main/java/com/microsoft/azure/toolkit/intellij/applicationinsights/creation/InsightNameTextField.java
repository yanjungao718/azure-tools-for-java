/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.applicationinsights.creation;

import com.microsoft.azure.toolkit.intellij.common.AzureTextInput;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.applicationinsights.ApplicationInsight;
import com.microsoft.azure.toolkit.lib.applicationinsights.AzureApplicationInsights;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class InsightNameTextField extends AzureTextInput {

    private static final Pattern PATTERN = Pattern.compile("[a-zA-Z0-9_\\.\\-\\(\\)]{0,254}[a-zA-Z0-9_\\-\\(\\)]");
    private static final String REGEX_PATTERN_MESSAGE = "The resource name must contain between 1 to 255 characters inclusive. The name only allows " +
            "alphanumeric characters, periods, underscores, hyphens and parenthesis and cannot end in a period.";
    private static final String NAME_IS_ALREADY_TAKEN = "The specified application insights name is already taken.";
    @Setter
    private int minLength = 1;
    @Setter
    private int maxLength = 255;
    @Getter
    @Setter
    private String subscriptionId;

    public InsightNameTextField() {
        this.addValidator(this::doValidateValue);
        this.setRequired(true);
    }

    @Nonnull
    private AzureValidationInfo doValidateValue() {
        final String value = this.getValue();
        // validate length
        if (StringUtils.length(value) < minLength || StringUtils.length(value) > maxLength) {
            return AzureValidationInfo.builder().input(this)
                .message(String.format("Server name must be at least %s characters and at most %s characters.", minLength, maxLength))
                .type(AzureValidationInfo.Type.ERROR).build();
        }
        // validate special character
        if (!PATTERN.matcher(value).matches()) {
            return AzureValidationInfo.builder().input(this).message(REGEX_PATTERN_MESSAGE).type(AzureValidationInfo.Type.ERROR).build();
        }
        // validate availability
        final List<ApplicationInsight> list = StringUtils.isEmpty(subscriptionId) ? Collections.emptyList() :
                Azure.az(AzureApplicationInsights.class).forSubscription(subscriptionId).applicationInsights().list();
        final boolean isNameUsed = list.stream().anyMatch(insight -> StringUtils.equals(insight.getName(), value));
        return isNameUsed ? AzureValidationInfo.builder().input(this).message(NAME_IS_ALREADY_TAKEN).type(AzureValidationInfo.Type.ERROR).build() :
                AzureValidationInfo.success(this);
    }
}
