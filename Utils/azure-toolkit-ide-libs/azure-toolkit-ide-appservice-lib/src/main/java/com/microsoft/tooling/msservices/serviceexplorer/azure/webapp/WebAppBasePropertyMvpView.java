/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.tooling.msservices.serviceexplorer.azure.webapp;

import com.microsoft.azuretools.core.mvp.ui.base.MvpView;
import com.microsoft.azuretools.core.mvp.ui.webapp.WebAppProperty;

public interface WebAppBasePropertyMvpView extends MvpView {
    void onLoadWebAppProperty(String sid, String webAppId, String slotName);

    void showProperty(WebAppProperty property);

    void showPropertyUpdateResult(boolean isSuccess);

    void showGetPublishingProfileResult(boolean isSuccess);
}
