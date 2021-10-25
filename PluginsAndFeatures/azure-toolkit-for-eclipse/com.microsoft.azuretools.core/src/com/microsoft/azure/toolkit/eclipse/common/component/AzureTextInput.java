/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.component;

import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.common.utils.Debouncer;
import com.microsoft.azure.toolkit.lib.common.utils.TailingDebouncer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import javax.annotation.Nonnull;

public class AzureTextInput extends Text implements AzureFormInputControl<String>, ModifyListener {
    protected static final int DEBOUNCE_DELAY = 300;
    private final Debouncer debouncer;

    public AzureTextInput(Composite parent, int style) {
        super(parent, style);
        this.debouncer = new TailingDebouncer(() -> {
            this.fireValueChangedEvent(this.getValue());
            this.doValidate();
        }, DEBOUNCE_DELAY);
        this.addModifyListener(this);
    }

    public AzureTextInput(Composite parent) {
        this(parent, SWT.NONE);
    }

    @Override
    public String getValue() {
        final String[] value = new String[]{null};
        AzureTaskManager.getInstance().runAndWait(() -> {
            value[0] = this.getText();
        });
        return value[0];
    }

    @Override
    public void setValue(final String val) {
        AzureTaskManager.getInstance().runAndWait(() -> {
            this.setText(val);
        });
    }

    @Nonnull
    @Override
    public final AzureValidationInfo doValidate() {
        final AzureValidationInfo validationInfo = AzureFormInputControl.super.doValidate();
        this.setValidationInfo(validationInfo);
        return validationInfo;
    }

    public void revalidateValue() {
        this.setValidationInfo(AzureValidationInfo.PENDING);
        this.debouncer.debounce();
    }

    public void setValidationInfo(AzureValidationInfo info) {
    }

    @Override
    public void modifyText(ModifyEvent modifyEvent) {
        this.revalidateValue();
    }

    @Override
    protected void checkSubclass() {
    }
}
