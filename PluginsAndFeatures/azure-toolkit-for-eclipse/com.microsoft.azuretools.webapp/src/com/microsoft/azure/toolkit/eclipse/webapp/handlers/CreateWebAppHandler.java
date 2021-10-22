/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.webapp.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import com.microsoft.azure.toolkit.eclipse.appservice.CreateWebAppDialog;
import com.microsoft.azure.toolkit.lib.appservice.task.CreateOrUpdateWebAppTask;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;

public class CreateWebAppHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveWorkbenchWindowChecked(event).getShell();
        CreateWebAppDialog createDialog = new CreateWebAppDialog(shell);
        createDialog.setOkActionListener(config -> {
            createDialog.close();
            AzureTaskManager.getInstance().runInBackground(AzureString.format("Creating web app %s", config.appName()),
                    () -> new CreateOrUpdateWebAppTask(config).execute());
        });
        createDialog.open();
        return null;
    }

}
