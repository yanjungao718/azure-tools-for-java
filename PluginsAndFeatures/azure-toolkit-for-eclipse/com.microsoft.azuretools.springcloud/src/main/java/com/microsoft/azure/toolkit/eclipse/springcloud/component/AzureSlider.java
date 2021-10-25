/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.springcloud.component;

import com.microsoft.azure.toolkit.eclipse.common.component.AzureFormInputControl;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;

public class AzureSlider extends Composite implements AzureFormInputControl<Integer> {
    private Scale numSlider;
    private Spinner numValue;
    private int realMin = Integer.MIN_VALUE;

    public AzureSlider(Composite parent, int style) {
        super(parent, style);
        this.setupUI();
        this.init();
    }

    private void init() {
        this.numSlider.addListener(SWT.Selection, e -> this.setValue(this.numSlider.getSelection()));
        this.numValue.addListener(SWT.Selection, e -> AzureSlider.this.setValue(numValue.getSelection()));
    }

    public void setMaximum(int max) {
        this.numSlider.setMaximum(max);
        this.numValue.setMaximum(max);
    }

    public void setMinimum(int min) {
        this.numSlider.setMinimum(min);
        this.numValue.setMinimum(min);
    }

    public void setMajorTickSpacing(int tick) {
        this.numSlider.setPageIncrement(tick);
    }

    public void setMinorTickSpacing(int tick) {
        this.numSlider.setIncrement(tick);
    }

    public void setValue(Integer value) {
        if (this.isDisposed()) {
            return;
        }
        boolean changed = false;
        final int val = Math.max(this.realMin, value);
        if (val != this.numSlider.getSelection()) {
            changed = true;
            this.numSlider.setSelection(val);
        }
        if (val != this.numValue.getSelection()) {
            changed = true;
            this.numValue.setSelection(val);
        }
        if (changed) {
            AzureTaskManager.getInstance().runLater(() -> this.fireValueChangedEvent(val));
        }
    }

    public Integer getValue() {
        if (this.isDisposed()) {
            return 0;
        }
        return this.numValue.getSelection();
    }

    public void setRealMin(int realMin) {
        this.realMin = realMin;
        this.setValue(this.getValue());
    }

    public void setEnabled(boolean isEnabled) {
        this.numSlider.setEnabled(isEnabled);
        this.numValue.setEnabled(isEnabled);
    }

    private void setupUI() {
        setLayout(new GridLayout(2, false));

        this.numSlider = new Scale(this, SWT.NONE);
        numSlider.setSelection(1);
        this.numSlider.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        this.numValue = new Spinner(this, SWT.BORDER);
        numValue.setSelection(1);
        this.numValue.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
