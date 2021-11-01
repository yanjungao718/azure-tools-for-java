/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector.aad;

import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.graph.models.Application;
import lombok.Data;

import javax.annotation.Nonnull;

/**
 * A pair of subscription and application. The application must belong to the subscription.
 */
@Data
class SubscriptionApplicationPair {
    @Nonnull
    private final Subscription subscription;
    @Nonnull
    private final Application application;
}
