/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.component;

import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import javax.accessibility.AccessibleRelation;
import java.util.Optional;

public interface AzureFormInputControl<T> extends AzureFormInput<T> {
    String ACCESSIBLE_LABEL = "accessbile.label";

    default Control getInputControl() {
        return (Control) this;
    }

    default void setLabeledBy(Label label) {
        this.getInputControl().setData(AccessibleRelation.LABELED_BY, label);
        this.setLabel(label.getText());
    }

    default Label getLabeledBy() {
        return (Label) this.getInputControl().getData(AccessibleRelation.LABELED_BY);
    }

    default void setLabel(String text) {
        final Control control = this.getInputControl();
        control.setData(ACCESSIBLE_LABEL, text);
        control.getAccessible().addAccessibleListener(new AccessibleAdapter() {
            @Override
            public void getName(AccessibleEvent e) {
                e.result = getLabel();
            }
        });
    }

    default String getLabel() {
        return Optional.ofNullable((String) this.getInputControl().getData(ACCESSIBLE_LABEL))
            .map(t -> t.endsWith(":") ? t.substring(0, t.length() - 1) : t)
            .orElse(AzureFormInput.super.getLabel());
    }
}
