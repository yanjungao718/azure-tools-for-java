/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.eclipse.function.launch.local;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.launching.StandardVMRunner;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

import java.io.File;
import java.util.List;

public class AzureFunctionVMRunner extends StandardVMRunner {
    private final String stagingFolder;
    private final String[] funcCommandLineWithoutDebugArgs;
    /**
     * Constructor
     *
     * @param vmInstance the VM
     */

    public AzureFunctionVMRunner(IVMInstall vmInstance, String stagingFolder, String [] funcCommandLineWithoutDebugArgs) {
        super(vmInstance);
        this.stagingFolder = stagingFolder;
        this.funcCommandLineWithoutDebugArgs = funcCommandLineWithoutDebugArgs;
    }

    @Override
    protected File getWorkingDir(VMRunnerConfiguration config) {
        // by pass the inner logic of StandardVMRunner
        return new File(stagingFolder);
    }

    @Override
    protected String constructProgramString(VMRunnerConfiguration config) {
        // by pass the inner logic of StandardVMRunner
        return funcCommandLineWithoutDebugArgs[0];
    }

    @Override
    protected String[] ensureEncoding(ILaunch launch, String[] vmargs) {
        // by pass the inner logic of StandardVMRunner
        return vmargs;
    }

    @Override
    protected void addBootClassPathArguments(List<String> arguments, VMRunnerConfiguration config) {
        // by pass the inner logic of StandardVMRunner
    }

    protected String[] validateCommandLine(ILaunchConfiguration configuration, String[] cmdLine) {
        if (cmdLine.length > 0 && StringUtils.equals(cmdLine[0], funcCommandLineWithoutDebugArgs[0])) {
            // change the duplicate command line to `func host start`
            return funcCommandLineWithoutDebugArgs;
        }
        return cmdLine;
    }

    protected String[] combineVmArgs(VMRunnerConfiguration configuration, IVMInstall vmInstall) {
        return new String[0];
    }
}
