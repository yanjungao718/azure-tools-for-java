/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.appservice.action;

import com.jediterm.terminal.TerminalDataStream;
import com.jediterm.terminal.emulator.Emulator;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;

import java.io.IOException;
import java.util.logging.Logger;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public enum SSHTerminalManager {
    INSTANCE;

    private static final Logger logger = Logger.getLogger(SSHTerminalManager.class.getName());

    private static final String SSH_INTO_WEB_APP_ERROR_DIALOG_TITLE = message("webapp.ssh.error.title");
    public static final String SSH_INTO_WEB_APP_ERROR_MESSAGE = message("webapp.ssh.error.message");
    private static final String SSH_INTO_WEB_APP_ERROR_MESSAGE_NOT_SUPPORT_WINDOWS = message("webapp.ssh.error.notSupport.Windows");
    private static final String SSH_INTO_WEB_APP_ERROR_MESSAGE_NOT_SUPPORT_DOCKER = message("webapp.ssh.error.notSupport.Docker");
    private static final String OS_LINUX = "linux";
    private static final String WEB_APP_DOCKER_PREFIX = "DOCKER|";
    private static final String CMD_SSH_TO_LOCAL_PROXY =
            "ssh -o StrictHostKeyChecking=no -o \"UserKnownHostsFile /dev/null\" -o \"LogLevel ERROR\" %s@127.0.0.1 -p %d";

    /**
     * ssh to connect to local proxy and open the terminal for remote container.
     *
     * @param shellTerminalWidget
     * @param connectionInfo
     */
    public void openConnectionInTerminal(ShellTerminalWidget shellTerminalWidget, CreateRemoteConnectionInfo connectionInfo) {
        if (connectionInfo == null || connectionInfo.getPort() <= 0 ||
                StringUtils.isAnyBlank(connectionInfo.getUsername(), connectionInfo.getPassword())) {
            DefaultLoader.getUIHelper().showError(SSH_INTO_WEB_APP_ERROR_MESSAGE, SSH_INTO_WEB_APP_ERROR_DIALOG_TITLE);
            return;
        }
        try {
            int count = 0;
            while ((shellTerminalWidget.getTtyConnector() == null) && count++ < 200) {
                Thread.sleep(100);
            }

            while ((shellTerminalWidget.getTerminalStarter() == null) && count++ < 200) {
                Thread.sleep(100);
            }
            shellTerminalWidget.executeCommand(String.format(CMD_SSH_TO_LOCAL_PROXY, connectionInfo.getUsername(), connectionInfo.getPort()));
            waitForInputPassword(shellTerminalWidget, 30000);
            shellTerminalWidget.executeCommand(connectionInfo.getPassword());
        } catch (IOException | InterruptedException | IllegalAccessException e) {
            DefaultLoader.getUIHelper().showError(SSH_INTO_WEB_APP_ERROR_MESSAGE, SSH_INTO_WEB_APP_ERROR_DIALOG_TITLE);
        }
    }

    private void waitForInputPassword(ShellTerminalWidget shellTerminalWidget, int timeout) throws IllegalAccessException {
        final Emulator emulator = (Emulator) FieldUtils.readField(shellTerminalWidget.getTerminalStarter(), "myEmulator", true);
        final TerminalDataStream terminalDataStream = (TerminalDataStream) FieldUtils.readField(emulator, "myDataStream", true);
        char[] myBuf = (char[]) FieldUtils.readField(terminalDataStream, "myBuf", true);
        int count = 0;
        int interval = 100;
        int countMax = timeout / interval;
        try {
            while (count++ < countMax) {
                if (myBuf != null && String.valueOf(myBuf).contains("password:")) {
                    return;
                }
                Thread.sleep(interval);
            }
        } catch (InterruptedException e) {
        }
        logger.info(message("webapp.ssh.hint.passwordNotReady"));
    }

    @Data
    public static class CreateRemoteConnectionInfo {
        private String username;
        private String password;
        private int port;
    }
}
