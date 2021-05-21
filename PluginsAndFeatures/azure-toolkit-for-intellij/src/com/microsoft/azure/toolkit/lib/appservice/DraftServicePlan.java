/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.appservice;

import com.microsoft.azure.management.appservice.AppServicePlan;
import com.microsoft.azure.management.appservice.OperatingSystem;
import com.microsoft.azure.management.appservice.SkuDescription;
import com.microsoft.azure.toolkit.lib.appservice.model.PricingTier;
import com.microsoft.azure.management.appservice.implementation.AppServiceManager;
import com.microsoft.azure.management.appservice.implementation.AppServicePlanInner;
import com.microsoft.azure.toolkit.intellij.common.Draft;
import com.microsoft.azure.toolkit.lib.common.OperationNotSupportedException;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import rx.Observable;

import java.util.Map;

@Setter
@Builder
public class DraftServicePlan implements AppServicePlan, Draft {

    @Getter
    private Subscription subscription;
    private String name;
    private Region region;
    private OperatingSystem os;
    private PricingTier tier;

    @Override
    public @Nullable String id() {
        return null;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public com.microsoft.azure.management.resources.fluentcore.arm.Region region() {
        return this.region == null ? null : com.microsoft.azure.management.resources.fluentcore.arm.Region.fromName(this.region.getName());
    }

    @Override
    public com.microsoft.azure.management.appservice.PricingTier pricingTier() {
        if (tier == null) {
            return null;
        }
        return toPricingTier(this.tier);
    }

    @Override
    public OperatingSystem operatingSystem() {
        return this.os;
    }

    @Override
    public String resourceGroupName() {
        return null;
    }

    @Override
    public int maxInstances() {
        throw new OperationNotSupportedException();
    }

    @Override
    public int capacity() {
        throw new OperationNotSupportedException();
    }

    @Override
    public boolean perSiteScaling() {
        throw new OperationNotSupportedException();
    }

    @Override
    public int numberOfWebApps() {
        throw new OperationNotSupportedException();
    }

    @Override
    public AppServiceManager manager() {
        throw new OperationNotSupportedException();
    }

    @Override
    public String type() {
        throw new OperationNotSupportedException();
    }

    @Override
    public String regionName() {
        throw new OperationNotSupportedException();
    }

    @Override
    public Map<String, String> tags() {
        throw new OperationNotSupportedException();
    }

    @Override
    public AppServicePlanInner inner() {
        throw new OperationNotSupportedException();
    }

    @Override
    public String key() {
        throw new OperationNotSupportedException();
    }

    @Override
    public AppServicePlan refresh() {
        throw new OperationNotSupportedException();
    }

    @Override
    public Observable<AppServicePlan> refreshAsync() {
        throw new OperationNotSupportedException();
    }

    @Override
    public Update update() {
        throw new OperationNotSupportedException();
    }

    private static com.microsoft.azure.management.appservice.PricingTier toPricingTier(PricingTier pricingTier) {
        final SkuDescription skuDescription = new SkuDescription().withTier(pricingTier.getTier()).withSize(pricingTier.getSize());
        return com.microsoft.azure.management.appservice.PricingTier.fromSkuDescription(skuDescription);
    }
}
