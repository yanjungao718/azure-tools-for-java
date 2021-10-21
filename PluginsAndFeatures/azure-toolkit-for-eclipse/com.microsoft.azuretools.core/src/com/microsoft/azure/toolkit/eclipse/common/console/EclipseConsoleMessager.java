/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.eclipse.common.console;

import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessage;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.ui.console.MessageConsoleStream;

import java.io.IOException;

public class EclipseConsoleMessager implements IAzureMessager {
    private JobConsole console;
    private MessageConsoleStream errorStream;
    private MessageConsoleStream outputStream;
    private MessageConsoleStream successStream;

    public EclipseConsoleMessager(JobConsole console) {
        this.console = console;
        outputStream = this.console.newMessageStream();
        outputStream.setColor(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_OUT_COLOR));

        successStream = this.console.newMessageStream();
        successStream.setColor(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_IN_COLOR));

        errorStream = this.console.newMessageStream();
        errorStream.setColor(DebugUIPlugin.getPreferenceColor(IDebugPreferenceConstants.CONSOLE_SYS_ERR_COLOR));
    }

    @Override
    public boolean show(IAzureMessage raw) {
        if (raw.getType() == IAzureMessage.Type.INFO || raw.getType() == IAzureMessage.Type.WARNING) {
            print(outputStream, raw.getMessage().toString());
            return true;
        } else if (raw.getType() == IAzureMessage.Type.SUCCESS) {
            print(successStream, raw.getMessage().toString());
        } else if (raw.getType() == IAzureMessage.Type.ERROR) {
            print(errorStream, raw.getContent());
        }
        return true;
    }

    private void print(MessageConsoleStream errorStream, String content) {
        if (!errorStream.isClosed()) {
            errorStream.println(content);
        }
    }

    public void close() {
        try {
            errorStream.close();
            outputStream.close();
            successStream.close();
        } catch (IOException e) {
            AzureMessager.getDefaultMessager().error(e);
        }

    }
}
