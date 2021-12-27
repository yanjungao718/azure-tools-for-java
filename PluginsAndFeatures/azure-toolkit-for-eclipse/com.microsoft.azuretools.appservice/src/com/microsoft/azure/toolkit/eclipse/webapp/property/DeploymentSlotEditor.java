/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.webapp.property;

import com.microsoft.azure.toolkit.eclipse.appservice.property.AppServiceBasePropertyEditor;
import com.microsoft.tooling.msservices.serviceexplorer.azure.webapp.deploymentslot.DeploymentSlotPropertyViewPresenter;

public class DeploymentSlotEditor extends AppServiceBasePropertyEditor {

    public static final String ID = "com.microsoft.azure.toolkit.eclipse.webapp.property.DeploymentSlotEditor";

    public DeploymentSlotEditor() {
        super(new DeploymentSlotPropertyViewPresenter());
    }

}
