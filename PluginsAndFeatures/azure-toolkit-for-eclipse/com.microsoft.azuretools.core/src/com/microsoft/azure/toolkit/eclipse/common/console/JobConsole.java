/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.console;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.MessageConsole;

public class JobConsole extends MessageConsole {
    private static final String TYPE = "com.microsoft.azure.toolkit.eclipse.job.consoleType";
    private Job job;

    public JobConsole(String name, Job job) {
        super(name, null);
        setType(TYPE);
        this.job = job;
    }

    public Job getJob() {
        return job;
    }
}
