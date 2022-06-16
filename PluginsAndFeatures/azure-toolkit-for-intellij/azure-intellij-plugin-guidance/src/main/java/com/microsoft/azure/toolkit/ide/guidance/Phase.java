/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.guidance;

import com.microsoft.azure.toolkit.ide.guidance.config.PhaseConfig;
import com.microsoft.azure.toolkit.ide.guidance.input.GuidanceInput;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessager;
import com.microsoft.azure.toolkit.lib.common.operation.OperationContext;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class Phase {
    @Nonnull
    private final Guidance guidance;

    @Nonnull
    private final String id;
    @Nonnull
    private final String type;
    @Nonnull
    private final String title;

    @Nullable
    private final String description;
    @Nonnull
    private final List<Step> steps;
    @Nonnull
    private Status status = Status.INITIAL;

    private Step currentStep;
    @Nullable
    private IAzureMessager output;

    private List<Consumer<Status>> listenerList = new ArrayList<>();

    public Phase(@Nonnull final PhaseConfig config, @Nonnull Guidance parent) {
        this.guidance = parent;
        this.id = UUID.randomUUID().toString();
        this.title = config.getTitle();
        this.type = config.getType();
        this.description = config.getDescription();
        this.steps = config.getSteps().stream().map(stepConfig -> new Step(stepConfig, this)).collect(Collectors.toList());
        initStepListener();
    }

    private void initStepListener() {
        this.steps.forEach(step -> step.addStatusListener(status -> {
            if (status == Status.RUNNING || status == Status.FAILED) {
                this.setStatus(status);
            } else if (status == Status.SUCCEED) {
                this.currentStep = getFollowingStep(step);
                if (currentStep == null) {
                    this.setStatus(Status.SUCCEED);
                } else {
                    currentStep.init();
                }
            }
        }));
    }

    public void init() {
        this.setStatus(Status.READY);
        if (CollectionUtils.isNotEmpty(steps)) {
            currentStep = steps.get(0);
            currentStep.init();
        } else {
            this.setStatus(Status.SUCCEED);
        }
    }

    public void setStatus(final Status status) {
        this.status = status;
        this.listenerList.forEach(listener -> listener.accept(status));
    }

    public void setOutput(@Nullable IAzureMessager output) {
        this.output = output;
        this.steps.forEach(step -> step.setOutput(output));
    }

    public List<GuidanceInput> getInputs() {
        return this.steps.stream().flatMap(step -> step.getInputs().stream()).collect(Collectors.toList());
    }

    public void execute() {
        AzureTaskManager.getInstance().runInBackground(new AzureTask<>(AzureString.format("run phase '%s'", this.getTitle()), () -> {
            final IAzureMessager currentMessager = AzureMessager.getMessager();
            OperationContext.current().setMessager(output);
            try {
                for (final Step step : steps) {
                    step.execute();
                }
            } catch (final Exception e) {
                AzureMessager.getMessager().error(e);
            } finally {
                OperationContext.current().setMessager(currentMessager);
            }
        }));
    }

    public Context getContext() {
        return this.getGuidance().getContext();
    }

    public String getRenderedDescription() {
        return getContext().render(this.getDescription());
    }

    public String getRenderedTitle() {
        return getContext().render(this.getTitle());
    }

    @Nullable
    private Step getFollowingStep(Step step) {
        for (int i = 0; i < steps.size() - 1; i++) {
            if (StringUtils.equals(step.getId(), steps.get(i).getId())) {
                return steps.get(i + 1);
            }
        }
        return null;
    }

    public void addStatusListener(Consumer<Status> listener) {
        listenerList.add(listener);
    }

    public void removeStatusListener(Consumer<Status> listener) {
        listenerList.remove(listener);
    }

}
