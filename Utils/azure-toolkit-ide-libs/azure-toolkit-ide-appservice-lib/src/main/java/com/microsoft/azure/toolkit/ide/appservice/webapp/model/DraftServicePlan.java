/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.ide.appservice.webapp.model;

import com.microsoft.azure.toolkit.ide.common.model.Draft;
import com.microsoft.azure.toolkit.lib.appservice.entity.AppServicePlanEntity;
import com.microsoft.azure.toolkit.lib.appservice.model.OperatingSystem;
import com.microsoft.azure.toolkit.lib.appservice.model.PricingTier;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

@Setter
@Getter
public class DraftServicePlan extends AppServicePlanEntity implements Draft {
    @Getter
    @Setter
    private Subscription subscription;

    public DraftServicePlan(@Nullable Subscription subscription,
                            @Nonnull String name,
                            @Nullable Region region,
                            @Nullable OperatingSystem os,
                            @Nonnull PricingTier pricingTier) {
        super(builder().subscriptionId(Optional.ofNullable(subscription).map(Subscription::getId).orElse(null))
                .name(name)
                .region(Optional.ofNullable(region).map(Region::getName).orElse(null))
                .pricingTier(pricingTier)
                .operatingSystem(os));
        this.subscription = subscription;
    }
}
