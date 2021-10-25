/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azuretools.core.ui.login;

import com.microsoft.azure.toolkit.eclipse.common.component.AzureTextInput;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import org.eclipse.swt.widgets.Composite;

import java.util.regex.Pattern;

public class GuidAzureTextInput extends AzureTextInput {
    private static final String GUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"; //UUID v1-v5
    private static final Pattern GUID_PATTERN = Pattern.compile(GUID_REGEX, Pattern.CASE_INSENSITIVE);

    public GuidAzureTextInput(Composite parent) {
        super(parent);
        this.setValidator(this::doValidateValue);
        this.setRequired(true);
    }

    public AzureValidationInfo doValidateValue() {
        final String value = this.getValue();
        // validate length
        if (!GUID_PATTERN.matcher(value).matches()) {
            return AzureValidationInfo.builder().input(this)
                    .message("Guid must match regex:" + GUID_REGEX)
                    .type(AzureValidationInfo.Type.ERROR).build();
        }
        return AzureValidationInfo.OK;
    }
}
