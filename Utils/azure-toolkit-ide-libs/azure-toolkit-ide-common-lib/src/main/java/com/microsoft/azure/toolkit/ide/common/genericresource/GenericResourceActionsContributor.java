/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.common.genericresource;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;

public class GenericResourceActionsContributor implements IActionsContributor {
    public static final int INITIALIZE_ORDER = ResourceCommonActionsContributor.INITIALIZE_ORDER + 1;

    public static final String GENERIC_RESOURCE_ACTIONS = "actions.genericResource.resource";

    @Override
    public void registerGroups(AzureActionManager am) {
        final ActionGroup genericResourceActionGroup = new ActionGroup(
            ResourceCommonActionsContributor.OPEN_PORTAL_URL
        );
        am.registerGroup(GENERIC_RESOURCE_ACTIONS, genericResourceActionGroup);
    }

    @Override
    public int getOrder() {
        return INITIALIZE_ORDER;
    }
}
