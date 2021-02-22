/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azuretools.sdkmanage;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

// todo: replace with new common auth library
public class Track2Manager {
    private static final Map<String, AzureResourceManager> resourceMap = new HashMap<>();

    public static AzureResourceManager getAzureResourceManager(String subsId) {
        // Todo: replace with identity implement
        if (resourceMap.containsKey(subsId)) {
            return resourceMap.get(subsId);
        }
        final AzureManager azureManager = AuthMethodManager.getInstance().getAzureManager();
        if (azureManager == null) {
            return null;
        }
        final AzureEnvironment azureEnvironment = AzureEnvironment.knownEnvironments().stream()
                .filter(environment -> StringUtils.equalsIgnoreCase(environment.getManagementEndpoint(),
                        azureManager.getEnvironment().getAzureEnvironment().managementEndpoint()))
                .findFirst().orElse(AzureEnvironment.AZURE);
        final AzureProfile azureProfile = new AzureProfile(azureEnvironment);
        final HttpClient httpClient = new NettyAsyncHttpClientBuilder().build();
        return AzureResourceManager.configure()
                .withHttpClient(httpClient)
                .authenticate(tokenRequestContext -> {
                    try {
                        final String token = azureManager.getAccessToken(azureManager.getTenantIdBySubscription(subsId));
                        return Mono.just(new AccessToken(token, OffsetDateTime.MAX));
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                }, azureProfile).withSubscription(subsId);
    }
}
