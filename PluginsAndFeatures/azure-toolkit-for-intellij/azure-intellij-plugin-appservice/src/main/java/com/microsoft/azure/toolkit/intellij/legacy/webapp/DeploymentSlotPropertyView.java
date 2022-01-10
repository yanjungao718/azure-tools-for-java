/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.webapp;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.azure.toolkit.lib.appservice.service.IAppService;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.WebAppDeploymentSlot;
import com.microsoft.azure.toolkit.lib.common.event.AzureEvent;
import com.microsoft.azure.toolkit.lib.common.event.AzureEventBus;
import com.microsoft.azure.toolkit.lib.common.event.AzureOperationEvent;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.base.WebAppBasePropertyViewPresenter;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.deploymentslot.DeploymentSlotPropertyViewPresenter;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;

public class DeploymentSlotPropertyView extends WebAppBasePropertyView {
    private static final String ID = "com.microsoft.intellij.helpers.webapp.DeploymentSlotPropertyView";
    private final AzureEventBus.EventListener<Object, AzureEvent<Object>> resourceDeleteListener;

    /**
     * Initialize the Web App Property View and return it.
     */
    public static WebAppBasePropertyView create(@Nonnull final Project project, @Nonnull final String sid,
                                                @Nonnull final String resId, @Nonnull final String slotName, @Nonnull final VirtualFile virtualFile) {
        DeploymentSlotPropertyView view = new DeploymentSlotPropertyView(project, sid, resId, slotName, virtualFile);
        view.onLoadWebAppProperty(sid, resId, slotName);
        return view;
    }

    private DeploymentSlotPropertyView(@Nonnull final Project project, @Nonnull final String sid,
                                       @Nonnull final String webAppId, @Nonnull final String slotName, @Nonnull final VirtualFile virtualFile) {
        super(project, sid, webAppId, slotName, virtualFile);
        resourceDeleteListener = new AzureEventBus.EventListener<>(event -> {
            if (event instanceof AzureOperationEvent && ((AzureOperationEvent) event).getStage() == AzureOperationEvent.Stage.AFTER &&
                    event.getSource() instanceof WebAppDeploymentSlot && StringUtils.equals(((WebAppDeploymentSlot) event.getSource()).id(), resourceId)) {
                closeEditor((IAppService) event.getSource());
            }
        });
        AzureEventBus.on("webapp.delete_slot.slot|app", resourceDeleteListener);
    }

    @Override
    protected String getId() {
        return this.ID;
    }

    @Override
    public void dispose() {
        super.dispose();
        AzureEventBus.off("webapp.delete_slot.slot|app", resourceDeleteListener);
    }

    @Override
    protected WebAppBasePropertyViewPresenter createPresenter() {
        return new DeploymentSlotPropertyViewPresenter();
    }
}
