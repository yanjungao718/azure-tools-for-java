/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.eclipse.common.component;

import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.utils.Debouncer;
import com.microsoft.azure.toolkit.lib.common.utils.TailingDebouncer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import javax.annotation.Nonnull;

public class AzureRadioButton extends Button implements AzureFormInputControl<Boolean>, SelectionListener {
    protected static final int DEBOUNCE_DELAY = 300;
    private Boolean value;

    private final Debouncer debouncer;

    public AzureRadioButton(Composite parent) {
        super(parent, SWT.RADIO);
        this.debouncer = new TailingDebouncer(() -> {
            this.fireValueChangedEvent(this.value);
            this.doValidate();
        }, DEBOUNCE_DELAY);
        this.addSelectionListener(this);
    }
    @Override
    public Boolean getValue() {
        return this.getSelection();
    }

    @Override
    public void setValue(Boolean value) {
        if (value != null) {
            this.setSelection(value);
        }
    }

    public void revalidateValue() {
        this.setValidationInfo(AzureValidationInfo.PENDING);
        this.debouncer.debounce();
    }

    @Nonnull
    @Override
    public final AzureValidationInfo doValidate() {
        final AzureValidationInfo validationInfo = AzureFormInputControl.super.doValidate();
        this.setValidationInfo(validationInfo);
        return validationInfo;
    }

    public void setValidationInfo(AzureValidationInfo info) {
    }

    @Override
    protected void checkSubclass() {
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        this.value = this.getSelection();
        this.revalidateValue();
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        this.value = this.getSelection();
        this.revalidateValue();
    }
}
