/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.webapp;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.azure.toolkit.lib.appservice.AppServiceAppBase;
import com.microsoft.azure.toolkit.lib.appservice.webapp.WebApp;
import com.microsoft.azure.toolkit.lib.common.event.AzureEventBus;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.WebAppPropertyViewPresenter;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.base.WebAppBasePropertyViewPresenter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

public class WebAppPropertyView extends WebAppBasePropertyView {
    private static final String ID = "com.microsoft.intellij.helpers.webapp.WebAppBasePropertyView";
    private final AzureEventBus.EventListener resourceDeleteListener;

    /**
     * Initialize the Web App Property View and return it.
     */
    public static WebAppBasePropertyView create(@Nonnull final Project project, @Nonnull final String sid,
                                                @Nonnull final String webAppId, @Nonnull final VirtualFile virtualFile) {
        WebAppPropertyView view = new WebAppPropertyView(project, sid, webAppId, virtualFile);
        view.onLoadWebAppProperty(sid, webAppId, null);
        return view;
    }

    private WebAppPropertyView(@Nonnull final Project project, @Nonnull final String sid,
                               @Nonnull final String webAppId, @Nonnull final VirtualFile virtualFile) {
        super(project, sid, webAppId, null, virtualFile);

        resourceDeleteListener = new AzureEventBus.EventListener(event -> {
            // only invoke close listener after close operation was done
            // todo: investigate to remove duplicate within app service properties view
            final Object source = event.getSource();
            if (source instanceof WebApp && StringUtils.equals(((WebApp) source).id(), webAppId) && ((WebApp) source).getFormalStatus().isDeleted()) {
                closeEditor((AppServiceAppBase<?, ?, ?>) source);
            }
        });
        AzureEventBus.on("resource.status_changed.resource", resourceDeleteListener);
    }

    @Override
    protected String getId() {
        return this.ID;
    }

    @Override
    public void dispose() {
        super.dispose();
        AzureEventBus.off("resource.status_changed.resource", resourceDeleteListener);
    }

    @Override
    protected WebAppBasePropertyViewPresenter createPresenter() {
        return new WebAppPropertyViewPresenter();
    }
}
