/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.eclipse.function.launch.local;

import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.launching.StandardVMDebugger;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class AzureFunctionVMDebugger extends StandardVMDebugger {
    private final String stagingFolder;
    private final String[] funcCommandLineWithoutDebugArgs;

    public AzureFunctionVMDebugger(IVMInstall vmInstance, String stagingFolder, String [] funcCommandLineWithoutDebugArgs) {
        super(vmInstance);
        this.stagingFolder = stagingFolder;
        this.funcCommandLineWithoutDebugArgs = funcCommandLineWithoutDebugArgs;
    }

    protected String[] validateCommandLine(ILaunchConfiguration configuration, String[] cmdLine) {
        if (cmdLine.length > 0 && cmdLine[0].contains("func")) {
            // change the duplicate command line to `func host start`
            String cmd = Arrays.stream(cmdLine).filter(args -> args.contains("-agentlib:jdwp=transport=dt_socket")).findFirst().orElse(null);
            if (cmd != null) {
                return Stream.concat(Arrays.stream(funcCommandLineWithoutDebugArgs),
                        Stream.of("--language-worker", "--", cmd)).toArray(String[]::new);
            } else {
                throw new AzureToolkitRuntimeException("Cannot find -agentlib in command line arguments:" + StringUtils.join(cmdLine, " "));
            }
        }
        return cmdLine;
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

    @Override
    protected String convertClassPath(String[] cp) {
        return StringUtils.EMPTY;
    }

    protected String[] combineVmArgs(VMRunnerConfiguration configuration, IVMInstall vmInstall) {
        return new String[0];
    }
}
