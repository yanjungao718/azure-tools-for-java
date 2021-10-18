/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.component;

import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.utils.Debouncer;
import com.microsoft.azure.toolkit.lib.common.utils.TailingDebouncer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class AzureTextInput extends Text implements AzureFormInputControl<String>, ModifyListener {
    protected static final int DEBOUNCE_DELAY = 300;
    private final Debouncer debouncer;
    private String value;

    public AzureTextInput(Composite parent, int style) {
        super(parent, style);
        this.addModifyListener(this);
        this.debouncer = new TailingDebouncer(() -> fireValueChangedEvent(this.value), DEBOUNCE_DELAY);
    }

    public AzureTextInput(Composite parent) {
        this(parent, SWT.NONE);
    }

    protected AzureValidationInfo doValidateValue() {
        return AzureFormInputControl.super.doValidate();
    }

    @Override
    public AzureValidationInfo doValidate() {
        return this.debouncer.isPending() ? AzureValidationInfo.PENDING : this.doValidateValue();
    }

    @Override
    public void modifyText(ModifyEvent modifyEvent) {
        this.value = this.getText();
        this.debouncer.debounce();
    }

    @Override
    public String getValue() {
        return this.getText();
    }

    @Override
    public void setValue(final String val) {
        this.setText(val);
    }

    @Override
    protected void checkSubclass() {
        //  allow subclass
        System.out.println("info   : checking menu subclass");
    }
}
