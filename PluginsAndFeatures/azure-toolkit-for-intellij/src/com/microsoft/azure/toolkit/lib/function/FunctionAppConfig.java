/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.function;

import com.microsoft.azure.toolkit.lib.appservice.AppServiceConfig;
import com.microsoft.azure.toolkit.lib.appservice.model.PricingTier;
import com.microsoft.azure.toolkit.lib.appservice.model.Runtime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
public class FunctionAppConfig extends AppServiceConfig {
    public static final Runtime DEFAULT_RUNTIME = Runtime.FUNCTION_WINDOWS_JAVA8;
    @Builder.Default
    protected Runtime runtime = DEFAULT_RUNTIME;

    public static FunctionAppConfig getFunctionAppDefaultConfig() {
        return FunctionAppConfig.builder()
                                .runtime(FunctionAppConfig.DEFAULT_RUNTIME)
                                .pricingTier(PricingTier.CONSUMPTION)
                                .region(AppServiceConfig.DEFAULT_REGION).build();
    }
}
