/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.component;

import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import javax.accessibility.AccessibleRelation;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public interface AzureFormInputControl<T> extends AzureFormInput<T> {
    String ACCESSIBLE_LABEL = "accessbile.label";
    String VALIDATION_DECORATOR = "azure_validation_decorator";

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
        String label = text.trim();
        label = label.endsWith(":") ? label.substring(0, label.length() - 1) : label;
        this.set(ACCESSIBLE_LABEL, label);
        final Control control = this.getInputControl();
        control.setData(ACCESSIBLE_LABEL, label);
        control.getAccessible().addAccessibleListener(new AccessibleAdapter() {
            @Override
            public void getName(AccessibleEvent e) {
                e.result = getLabel();
            }
        });
    }

    default String getLabel() {
        return Optional.ofNullable((String) this.get(ACCESSIBLE_LABEL)).orElse(AzureFormInput.super.getLabel());
    }

    default AzureValidationInfo revalidateAndDecorate() {
        AzureTaskManager.getInstance().runLater(() -> this.setValidationInfo(AzureValidationInfo.PENDING));
        final T value = this.getValue();
        final AzureValidationInfo validationInfo = AzureFormInput.super.doValidate();
        if (Objects.equals(value, this.getValue())) {
            AzureTaskManager.getInstance().runLater(() -> this.setValidationInfo(validationInfo));
        }
        return validationInfo;
    }

    default void setValidationInfo(@Nullable AzureValidationInfo info) {
        this.set("validationInfo", info);
        final Control input = this.getInputControl();
        if (input.isDisposed()) {
            return;
        }
        final ControlDecoration deco = getValidationDecorator(input);
        if ((Objects.isNull(info) || info.getType() == AzureValidationInfo.Type.INFO)) {
            deco.hide();
        } else {
            deco.setImage(getValidationInfoIcon(info.getType()));
            deco.setDescriptionText("PENDING".equals(info.getMessage()) ? "Validating..." : info.getMessage());
            deco.show();
        }
    }

    default AzureValidationInfo getValidationInfo() {
        return this.get("validationInfo");
    }

    @Nonnull
    static ControlDecoration getValidationDecorator(Control input) {
        ControlDecoration deco = (ControlDecoration) input.getData(VALIDATION_DECORATOR);
        if (Objects.isNull(deco)) {
            deco = new ControlDecoration(input, SWT.TOP | SWT.LEAD);
            input.setData(VALIDATION_DECORATOR, deco);
        }
        return deco;
    }

    static Image getValidationInfoIcon(AzureValidationInfo.Type type) {
        if (type == null) {
            return null;
        } else {
            String id = null;
            switch (type) {
                case INFO:
                    id = "DEC_INFORMATION";
                    break;
                case WARNING:
                case PENDING:
                    id = "DEC_WARNING";
                    break;
                case ERROR:
                    id = "DEC_ERROR";
                    break;
            }
            FieldDecoration decoration = FieldDecorationRegistry.getDefault().getFieldDecoration(id);
            return decoration == null ? null : decoration.getImage();
        }
    }
}
