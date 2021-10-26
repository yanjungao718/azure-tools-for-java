/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.component;

import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
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
        this.debouncer = new TailingDebouncer(() -> AzureTaskManager.getInstance().runLater(() -> {
            this.fireValueChangedEvent(this.getValue());
            AzureTaskManager.getInstance().runInBackground("validating " + this.getLabel(), this::revalidateAndDecorate);
        }), DEBOUNCE_DELAY);
        this.addModifyListener(this);
    }

    public AzureTextInput(Composite parent) {
        this(parent, SWT.NONE);
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public void setValue(final String val) {
        if (this.isDisposed()) {
            return;
        }
        this.setText(val);
    }

    @Override
    public void modifyText(ModifyEvent modifyEvent) {
        this.value = this.getText();
        this.debouncer.debounce();
    }

    @Override
    protected void checkSubclass() {
    }
}
