/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.component;

import com.microsoft.azure.toolkit.lib.common.DataStore;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.form.Validatable;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.widgets.Control;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public interface AzureFormInputControl<T> extends AzureFormInput<T>, DataStore {
    default Control getInputControl() {
        return (Control) this;
    }

    default void addValueChangedListener(Consumer<T> listener) {
        final ListenerList valueChangedListeners = this.get("valueChangedListeners", new ListenerList());
        if (Arrays.stream(valueChangedListeners.getListeners()).noneMatch(l -> l.equals(listener))) {
            valueChangedListeners.add(listener);
        }
    }

    default void removeValueChangedListener(Consumer<T> listener) {
        final ListenerList valueChangedListeners = this.get("valueChangedListeners", new ListenerList());
        valueChangedListeners.remove(listener);
    }

    default List<Consumer<T>> getValueChangedListeners() {
        final ListenerList valueChangedListeners = this.get("valueChangedListeners", new ListenerList());
        return Arrays.stream(valueChangedListeners.getListeners()).map(l -> (Consumer<T>) l).collect(Collectors.toList());
    }

    default void fireValueChangedEvent(T val) {
        AzureTaskManager.getInstance().runLater(() -> this.getValueChangedListeners().forEach(l -> l.accept(val)));
    }

    @Nullable
    @Override
    default Validatable.Validator getValidator() {
        return this.get("validator");
    }

    default void setValidator(Validator validator) {
        this.set("validator", validator);
    }

    @Override
    default boolean isRequired() {
        return this.get("required", false);
    }

    default void setRequired(boolean required) {
        this.set("required", required);
    }
}
