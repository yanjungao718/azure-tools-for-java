/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.guidance;

import com.microsoft.azure.toolkit.ide.guidance.config.StepConfig;
import com.microsoft.azure.toolkit.ide.guidance.input.GuidanceInput;
import com.microsoft.azure.toolkit.ide.guidance.input.InputManager;
import com.microsoft.azure.toolkit.ide.guidance.task.TaskManager;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessager;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class Step {
    @Nonnull
    private final String id;
    @Nonnull
    private final String title;

    @Nullable
    private final String description;

    @Nonnull
    private final GuidanceTask task;

    @Nonnull
    private final List<GuidanceInput> inputs;

    @Nonnull
    private final Phase phase;

    @Nonnull
    private Status status = Status.INITIAL;

    private IAzureMessager output;

    private List<Consumer<Status>> listenerList = new ArrayList<>();

    public Step(@Nonnull final StepConfig config, @Nonnull Phase phase) {
        this.phase = phase;
        this.id = UUID.randomUUID().toString();
        this.title = config.getTitle();
        this.description = config.getDescription();
        this.task = TaskManager.createTask(config.getTask(), phase.getGuidance().getContext());
        this.inputs = Optional.ofNullable(config.getInputs())
            .map(configs -> configs.stream().map(inputConfig ->
                InputManager.createInputComponent(inputConfig, phase.getGuidance().getContext())).collect(Collectors.toList()))
            .orElse(Collections.emptyList());
    }

    public void setStatus(final Status status) {
        this.status = status;
        this.listenerList.forEach(listener -> listener.accept(status));
    }

    public void execute() {
        try {
            setStatus(Status.RUNNING);
            this.task.execute(this.output);
            setStatus(Status.SUCCEED);
        } catch (final Exception e) {
            setStatus(Status.FAILED);
        }
    }

    public String getRenderedDescription() {
        return getContext().render(this.getDescription());
    }

    public String getRenderedTitle() {
        return getContext().render(this.getTitle());
    }

    public Context getContext() {
        return this.getPhase().getContext();
    }

    public void addStatusListener(Consumer<Status> listener) {
        listenerList.add(listener);
    }

    public void removeStatusListener(Consumer<Status> listener) {
        listenerList.remove(listener);
    }

    public void init() {
        task.init();
        this.setStatus(task.isDone() ? Status.SUCCEED : Status.READY);
    }
}
