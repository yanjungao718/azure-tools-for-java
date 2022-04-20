/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.common.component;

import com.microsoft.azure.toolkit.ide.common.icon.AzureIcon;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.resource.ResourcesServiceSubscription;

import javax.annotation.Nonnull;

public class AzureSubscriptionLabelView<T extends ResourcesServiceSubscription> extends AzureResourceLabelView<T> {

    private final Subscription subscription;

    public AzureSubscriptionLabelView(@Nonnull T resource) {
        super(resource, t -> resource.getSubscriptionId(), t -> AzureIcon.builder().iconPath("/icons/Common/subscription.svg").build());
        this.subscription = Azure.az(AzureAccount.class).account().getSubscription(resource.getSubscriptionId());
    }

    @Override
    public String getLabel() {
        return this.subscription.getName();
    }
}
