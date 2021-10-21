/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.console;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

public class AzureJobConsoleParticipant implements IConsolePageParticipant {
    private Action terminateAction;
    private Action closeAction;

    private JobConsole console;

    @Override
    public void init(IPageBookViewPage page, IConsole console) {
        this.console = (JobConsole) console;

        IActionBars actionBars = page.getSite().getActionBars();
        configureToolBar(actionBars.getToolBarManager());
        // keep the order of adding a listener and then calling update() to ensure update
        // is called regardless of when the job finishes
        addJobChangeListener();
        updateButtonState();
    }

    private Action createTerminateAction() {
        Action terminate = new Action(("Stop")) {
            @Override
            public void run() {
                Job job = console.getJob();
                if (job != null) {
                    job.cancel();
                    updateButtonState();
                }
            }
        };
        terminate.setToolTipText("Terminate");
        final ImageDescriptor stopImage = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_STOP);
        terminate.setImageDescriptor(stopImage);
        terminate.setHoverImageDescriptor(stopImage);
        terminate.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_STOP_DISABLED));
        return terminate;
    }

    private void updateButtonState() {
        Job job = console.getJob();
        if (job != null) {
            if (terminateAction != null) {
                terminateAction.setEnabled(Job.NONE != job.getState());
            }

            if (closeAction != null) {
                closeAction.setEnabled(Job.NONE == job.getState());
            }
        }
    }

    private void addJobChangeListener() {
        Job job = console.getJob();
        if (job != null) {
            job.addJobChangeListener(new JobChangeAdapter() {
                @Override
                public void done(IJobChangeEvent event) {
                    updateButtonState();
                }
            });
        }
    }

    private void configureToolBar(IToolBarManager toolbarManager) {
        terminateAction = createTerminateAction();
        toolbarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, terminateAction);

        closeAction = createCloseAction();
        toolbarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, closeAction);
    }

    @Override
    public void dispose() {

    }

    @Override
    public void activated() {

    }

    @Override
    public void deactivated() {

    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

    private Action createCloseAction() {
        Action close = new Action("Remove") {
            @Override
            public void run() {
                ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] { console });
            }
        };
        close.setToolTipText("Remove launch");
        final ImageDescriptor removeImage = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_REMOVE);
        close.setImageDescriptor(removeImage);
        close.setHoverImageDescriptor(removeImage);
        close.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_REMOVE_DISABLED));
        return close;
    }
}
