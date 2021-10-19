/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.component;

import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import org.eclipse.swt.widgets.Control;

public interface AzureFormInputControl<T> extends AzureFormInput<T> {
    default Control getInputControl() {
        return (Control) this;
    }
}
