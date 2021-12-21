/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.eclipse.function.launch.local;

import com.microsoft.azure.toolkit.eclipse.common.launch.AzureLongDurationTaskRunnerWithConsole;
import com.microsoft.azure.toolkit.eclipse.common.launch.LaunchConfigurationUtils;
import com.microsoft.azure.toolkit.eclipse.function.core.EclipseFunctionProject;
import com.microsoft.azure.toolkit.eclipse.function.launch.model.FunctionLocalRunConfiguration;
import com.microsoft.azure.toolkit.eclipse.function.utils.FunctionUtils;
import com.microsoft.azure.toolkit.lib.appservice.function.core.AzureFunctionPackager;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.sourcelookup.advanced.AdvancedJavaLaunchDelegate;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;

public class AzureFunctionLocalLaunchDelegate extends AdvancedJavaLaunchDelegate {
    private static final int DEFAULT_FUNC_PORT = 7071;
    private static final int MAX_PORT = 65535;

    @Override
    public String verifyMainTypeName(ILaunchConfiguration configuration) throws CoreException {
        return String.format("Run Azure Function Project<%s>", configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "unknown project"));
    }

    @Override
    public IVMRunner getVMRunner(ILaunchConfiguration configuration, String mode) throws CoreException {
        FunctionLocalRunConfiguration config = LaunchConfigurationUtils.getFromConfiguration(configuration, FunctionLocalRunConfiguration.class);
        String projectName = config.getProjectName();
        IJavaProject project = Arrays.stream(FunctionUtils.listFunctionProjects()).filter(t -> StringUtils.equals(t.getElementName(), projectName)).findFirst().orElse(null);
        if (project == null) {
            AzureMessager.getMessager().error("Cannot find the specified project:" + projectName);
            throw new AzureToolkitRuntimeException("Cannot find the specified project:" + projectName);
        }

        if (StringUtils.isNotBlank(config.getLocalSettingsJsonPath()) && !new File(config.getLocalSettingsJsonPath()).exists()) {
            AzureMessager.getMessager().error("Cannot find the specified local.settings.json :" + config.getLocalSettingsJsonPath());
            throw new AzureToolkitRuntimeException("Cannot find the specified local.settings.json :" + config.getLocalSettingsJsonPath());
        }

        final File funcFile = new File(config.getFunctionCliPath());
        if (!funcFile.exists()) {
            AzureMessager.getMessager().error("Cannot find function cli:" + config.getFunctionCliPath());
            throw new AzureToolkitRuntimeException("Cannot find function cli:" + config.getFunctionCliPath());
        }
        File tempFolder = FunctionUtils.getTempStagingFolder().toFile();

        try {
            Mono.create(monoSink -> AzureLongDurationTaskRunnerWithConsole.getInstance().runTask("Launching function local run", () -> {
                try {
                    FileUtils.cleanDirectory(tempFolder);
                    File file = project.getProject().getFile("host.json").getLocation().toFile();
                    final EclipseFunctionProject eclipseFunctionProject = new EclipseFunctionProject(project, tempFolder);
                    eclipseFunctionProject.setHostJsonFile(file);
                    if (StringUtils.isNotBlank(config.getLocalSettingsJsonPath())) {
                        eclipseFunctionProject.setLocalSettingsJsonFile(new File(config.getLocalSettingsJsonPath()));
                    }
                    eclipseFunctionProject.buildJar();
                    AzureFunctionPackager.getInstance().packageProject(eclipseFunctionProject, true, config.getFunctionCliPath());
                    FileUtils.deleteQuietly(eclipseFunctionProject.getArtifactFile());
                    monoSink.success(true);
                } catch (Throwable e) {
                    monoSink.error(e);
                }
            }, true)).block();
        } catch (Exception e) {
            AzureMessager.getMessager().error(e);
            throw new CoreException(Status.error("Cannot prepare the staging folder for azure function.", e));
        }

        final int funcPort = findFreePortForApi(DEFAULT_FUNC_PORT);
        IVMInstall vm = getVMInstall(configuration);
        final String[] commandLine = {
            config.getFunctionCliPath(),
            "host",
            "start",
            "--port", String.valueOf(funcPort)
        };

        IVMRunner runner = getVMRunner(vm, mode, tempFolder.getAbsolutePath(), commandLine);
        if (runner == null) {
            abort("Local debug Azure Function is not supported by now.", null, IJavaLaunchConfigurationConstants.ERR_VM_RUNNER_DOES_NOT_EXIST);
        }
        return runner;
    }

    private IVMRunner getVMRunner(IVMInstall vm, String mode, String stagingFolder, String[] cmdLines) {
        if (ILaunchManager.RUN_MODE.equals(mode)) {
            return new AzureFunctionVMRunner(vm, stagingFolder, cmdLines);
        } else if (ILaunchManager.DEBUG_MODE.equals(mode)) {
            return new AzureFunctionVMDebugger(vm, stagingFolder, cmdLines);
        }
        return null;
    }

    public String[] getClasspath(ILaunchConfiguration configuration) {
        return new String[0];
    }

    @Override
    protected boolean supportsModule() {
        return false;
    }

    private static int findFreePortForApi(int startPort) {
        ServerSocket socket = null;
        for (int port = startPort; port <= MAX_PORT; port++) {
            try {
                socket = new ServerSocket(port);
                return socket.getLocalPort();
            } catch (IOException e) {
                // swallow this exception
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        // swallow this exception
                    }
                }
            }
        }
        return -1;
    }
}
