/*
 * Copyright (c) Microsoft Corporation
 *
 * All rights reserved.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.tooling.msservices.serviceexplorer;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class BasicActionBuilder {

    private static final String FUZZY_PROGRESS_MESSAGE_PATTERN = "%s...";
    private static final String GENERIC_PROGRESS_MESSAGE_PATTERN = "%s %s...";
    private static final String FULL_PROGRESS_MESSAGE_PATTERN = "%s %s (%s)...";

    private static final String PROMPT_MESSAGE_PATTERN = "This operation will %s your %s: %s. Are you sure you want to continue?";

    private Runnable runnable;
    private AzureActionEnum action;
    private String doingName;

    private String moduleName;
    private String instanceName;

    private boolean backgroundable;
    private boolean backgroundCancellable;
    private boolean backgroundConditionalModal;

    private boolean promptable;

    private boolean telemetrable;
    private String actionString;

    public BasicActionBuilder(Runnable runnable) {
        Preconditions.checkNotNull(runnable);
        this.runnable = runnable;
    }

    public BasicActionBuilder withAction(final AzureActionEnum action) {
        this.action = action;
        return this;
    }

    public BasicActionBuilder withDoingName(final String doingName) {
        this.doingName = doingName;
        return this;
    }

    public BasicActionBuilder withModuleName(final String moduleName) {
        this.moduleName = moduleName;
        return this;
    }

    public BasicActionBuilder withInstanceName(final String instanceName) {
        this.instanceName = instanceName;
        return this;
    }

    public BasicActionBuilder withBackgroudable(final boolean backgroundRequired) {
        this.backgroundable = backgroundRequired;
        return this;
    }

    public BasicActionBuilder withBackgroudable(final boolean backgroundRequired, final boolean cancellable, final boolean conditionalModal) {
        this.backgroundable = backgroundRequired;
        this.backgroundCancellable = cancellable;
        this.backgroundConditionalModal = conditionalModal;
        return this;
    }

    public BasicActionBuilder withPromptable(final boolean promptRequired) {
        this.promptable = promptRequired;
        return this;
    }

    public BasicActionBuilder withTelemetrable(final boolean telemetryRequired, String actionString) {
        this.telemetrable = telemetryRequired;
        this.actionString = actionString;
        return this;
    }

    public DelegateActionListener.BasicActionListener build() {
        Preconditions.checkNotNull(action);
        NodeActionListener delegate = new NodeActionListener() {
            @Override
            protected void actionPerformed(NodeActionEvent e) {
                runnable.run();
            }
        };
        if (backgroundable) {
            delegate = new DelegateActionListener.BackgroundActionListener(delegate, getProgressMessage(), backgroundCancellable, backgroundConditionalModal);
        }
        if (promptable) {
            delegate = new DelegateActionListener.PromptActionListener(delegate, getPromptMessage());
        }
        if (telemetrable) {
            delegate = new DelegateActionListener.TelemetricActionListener(delegate, actionString);

        }
        return new DelegateActionListener.BasicActionListener(delegate, action);
    }

    private String getProgressMessage() {
        String realDoingName = getDoingName();
        if (StringUtils.isNotBlank(moduleName) && StringUtils.isNotBlank(instanceName)) {
            return String.format(FULL_PROGRESS_MESSAGE_PATTERN, realDoingName, moduleName, instanceName);
        }
        if (StringUtils.isNotBlank(moduleName)) {
            return String.format(GENERIC_PROGRESS_MESSAGE_PATTERN, realDoingName, moduleName);
        }
        if (StringUtils.isNotBlank(instanceName)) {
            return String.format(GENERIC_PROGRESS_MESSAGE_PATTERN, realDoingName, instanceName);
        }
        return String.format(FUZZY_PROGRESS_MESSAGE_PATTERN, realDoingName);
    }

    private String getPromptMessage() {
        String realDoingName = getDoingName();
        return String.format(PROMPT_MESSAGE_PATTERN, realDoingName, moduleName, instanceName);
    }

    private String getDoingName() {
        if (StringUtils.isNotBlank(doingName)) {
            return doingName;
        }
        if (Objects.nonNull(action)) {
            return action.getDoingName();
        }
        return StringUtils.EMPTY;
    }

}
