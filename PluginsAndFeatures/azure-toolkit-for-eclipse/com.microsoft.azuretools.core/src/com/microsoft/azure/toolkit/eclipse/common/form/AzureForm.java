/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.eclipse.common.form;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import java.util.List;
import java.util.stream.Collectors;
public interface AzureForm<T> {
    T getFormData();
    void setFormData(T var1);
    List<AzureFormInput<?>> getInputs();
    default List<AzureValidationInfo> validateData() {
        return (List) this.getInputs().stream().map(AzureFormInput::doValidate).collect(Collectors.toList());
    }
}