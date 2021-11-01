/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.component;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AzureComboBoxViewer<T> extends ComboViewer {

    public AzureComboBoxViewer(AzureComboBox<T> parent) {
        super(parent);
    }

    public List<T> getItems() {
        final List<T> result = new ArrayList<>();
        final Object input = super.getInput();
        if (input instanceof Collection) {
            //noinspection unchecked
            result.addAll((Collection<T>) input);
        }
        return result;
    }

    @Override
    public void setSelection(ISelection selection) {
        super.setSelection(selection);
    }

    public void setItems(List<? extends T> items) {
        if (this.getControl().isDisposed()) {
            return;
        }
        super.setInput(items);
    }

    public void removeAllItems() {
        if (this.getControl().isDisposed()) {
            return;
        }
        super.setInput(Collections.emptyList());
    }

    public int getItemCount() {
        return super.listGetItemCount();
    }

    public int getSelectedIndex() {
        return super.listGetSelectionIndices()[0];
    }

    public void setSelectedIndex(int i) {
        super.listSetSelection(new int[]{i});
    }

    public Object getSelectedItem() {
        return this.getStructuredSelection().getFirstElement();
    }

    public void setSelectedItem(Object value) {
        if (Objects.nonNull(value)) {
            this.setSelection(new StructuredSelection(value));
        } else {
            this.setSelection(StructuredSelection.EMPTY);
        }
    }

    public void setEditable(boolean b) {
        if (this.getControl().isDisposed()) {
            return;
        }
        super.getControl().setEnabled(b);
    }

    public void setEnabled(boolean b) {
        if (this.getControl().isDisposed()) {
            return;
        }
        super.getControl().setEnabled(b);
    }

    public boolean isEnabled() {
        return super.getControl().isEnabled();
    }

    public void repaint() {
        this.getControl().redraw();
    }
}
