/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.vm.runtarget.wizard;

import com.intellij.execution.target.TargetEnvironmentWizardStepKt;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.util.NlsContexts;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

@Getter
public abstract class AzureVmTargetStepBase<T extends TargetEnvironmentWizardStepKt> extends TargetEnvironmentWizardStepKt {

    //    @Delegate(
//        types = {TargetEnvironmentWizardStepKt.class},
//        excludes = Customizable.class
//    )
    private final T originStep;

    public AzureVmTargetStepBase(String title, T originStep) {
        super(title);
        this.originStep = originStep;
    }

    @Override
    public void _init() {
        originStep._init();
    }

    @SneakyThrows
    @Override
    @NotNull
    public JComponent createMainPanel() {
        return (JComponent) MethodUtils.invokeMethod(this.originStep, true, "createMainPanel");
    }

    @SneakyThrows
    @Override
    @NotNull
    public JComponent createPanel() {
        return (JComponent) MethodUtils.invokeMethod(this.originStep, true, "createPanel");
    }

    @SneakyThrows
    @Override
    public void fireStateChanged() {
        MethodUtils.invokeMethod(this.originStep, true, "fireStateChanged");
    }

    @SneakyThrows
    @Override
    public void fireGoNext() {
        MethodUtils.invokeMethod(this.originStep, true, "fireGoNext");
    }

    @SneakyThrows
    @Override
    protected void doCommit(CommitType commitType) throws CommitStepException {
        MethodUtils.invokeMethod(this.originStep, true, "doCommit", commitType);
    }

    @Override
    public boolean isComplete() {
        return originStep.isComplete();
    }

    @SneakyThrows
    @Override
    public void setTitle(@Nullable @NlsContexts.DialogTitle String title) {
        MethodUtils.invokeMethod(this.originStep, true, "setTitle", title);
    }

    @Override
    public Icon getIcon() {
        return originStep.getIcon();
    }

//    @Override
//    @NotNull
//    public Object getStepId() {
//        return originStep.getStepId();
//    }
//
//    @Override
//    @Nullable
//    public Object getNextStepId() {
//        return originStep.getNextStepId();
//    }
//
//    @Override
//    @Nullable
//    public Object getPreviousStepId() {
//        return originStep.getPreviousStepId();
//    }
//
//    @Override
//    @Nullable
//    @NlsContexts.DialogTitle
//    public String getTitle() {
//        return originStep.getTitle();
//    }

    @Override
    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return originStep.getPreferredFocusedComponent();
    }

    @Override
    @NonNls
    public String getHelpId() {
        return originStep.getHelpId();
    }

    @Override
    public void dispose() {
        originStep.dispose();
    }

    @Override
    public void addStepListener(Listener listener) {
        originStep.addStepListener(listener);
    }

//    @Override
//    public void _commit(boolean finishChosen) throws CommitStepException {
//        originStep._commit(finishChosen);
//    }
//
//    @Nonnull
//    @Override
//    public JComponent getComponent() {
//        return originStep.getComponent();
//    }
}
