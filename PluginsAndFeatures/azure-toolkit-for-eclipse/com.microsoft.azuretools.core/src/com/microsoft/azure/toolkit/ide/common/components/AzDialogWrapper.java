/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.common.components;

import com.microsoft.azure.toolkit.eclipse.common.component.AzureFormInputControl;
import com.microsoft.azure.toolkit.eclipse.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Shell;
import reactor.core.Disposable;

import java.util.List;
import java.util.Objects;

public abstract class AzDialogWrapper<T> extends TitleAreaDialog {

    public static final String CONTROL_DECORATOR = "azure_validation_decorator";
    private Disposable subscription;

    public AzDialogWrapper(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void setTitle(String newTitle) {
        super.setTitle(newTitle);
        this.setMessage(newTitle);
        this.getShell().setText(newTitle);
    }

    protected void doOkAction() {
        this.setReturnCode(0);
        this.close();
    }

    @Override
    public boolean close() {
        if (Objects.nonNull(this.subscription) && !this.subscription.isDisposed()) {
            this.subscription.dispose();
            this.subscription = null;
        }
        return super.close();
    }

    @Override
    protected final void okPressed() {
        final AzureTask<Void> task = new AzureTask<>(AzureString.fromString("validating..."), () -> {
            if (doValidateAll().isEmpty()) {
                AzureTaskManager.getInstance().runLater(this::doOkAction);
            }
        });
        task.setBackgroundable(false);
        AzureTaskManager.getInstance().runInModal(task);
    }

    protected final void setErrorInfoAll(List<AzureValidationInfo> infos) {
        final String titleErrorMessage = infos.isEmpty() ? null : infos.get(0).getMessage();
        this.setErrorMessage(titleErrorMessage);
        for (AzureValidationInfo info : infos) {
            final AzureFormInputControl<?> input = (AzureFormInputControl<?>) info.getInput();
            input.setValidationInfo(info);
        }
    }

    public abstract AzureForm<T> getForm();

    protected abstract List<AzureValidationInfo> doValidateAll();

    public void setOkButtonEnabled(boolean enabled) {
        this.getButton(0).setEnabled(enabled);
    }
}
