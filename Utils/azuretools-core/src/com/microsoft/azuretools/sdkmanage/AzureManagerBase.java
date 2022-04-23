/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.sdkmanage;

import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.toolkit.lib.AzureConfiguration;
import com.microsoft.azure.toolkit.lib.account.IAzureAccount;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azuretools.authmanage.CommonSettings;
import com.microsoft.azuretools.authmanage.Environment;
import com.microsoft.azuretools.authmanage.SubscriptionManager;
import com.microsoft.azuretools.azurecommons.helpers.Nullable;
import com.microsoft.azuretools.telemetry.TelemetryInterceptor;
import com.microsoft.azuretools.utils.AzureRegisterProviderNamespaces;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.microsoft.azure.toolkit.lib.Azure.az;

/**
 * Created by vlashch on 1/27/17.
 */
public abstract class AzureManagerBase implements AzureManager {
    private static final String CHINA_PORTAL = "https://portal.azure.cn";
    private static final String GLOBAL_PORTAL = "https://ms.portal.azure.com";

    private static final Logger LOGGER = Logger.getLogger(AzureManagerBase.class.getName());

    protected Map<String, Azure> sidToAzureMap = new ConcurrentHashMap<>();
    protected final SubscriptionManager subscriptionManager;

    protected AzureManagerBase() {
        this.subscriptionManager = new SubscriptionManager();
    }

    @Override
    public String getPortalUrl() {
        final com.azure.core.management.AzureEnvironment azureEnvironment = getAzureEnvironment();
        if (azureEnvironment == null || azureEnvironment == com.azure.core.management.AzureEnvironment.AZURE) {
            return GLOBAL_PORTAL;
        } else if (azureEnvironment == com.azure.core.management.AzureEnvironment.AZURE_CHINA) {
            return CHINA_PORTAL;
        } else {
            return azureEnvironment.getPortal();
        }
    }

    @Override
    public List<Subscription> getSubscriptions() {
        return com.microsoft.azure.toolkit.lib.Azure.az(IAzureAccount.class).account().getSelectedSubscriptions();
    }

    @Override
    public @Nullable Azure getAzure(String sid) {
        if (!isSignedIn()) {
            return null;
        }
        if (sidToAzureMap.containsKey(sid)) {
            return sidToAzureMap.get(sid);
        }
        return sidToAzureMap.computeIfAbsent(sid, id -> {
            final AzureTokenCredentials credentials = com.microsoft.azure.toolkit.lib.Azure.az(AzureAccount.class).account().getTokenCredentialV1(sid);
            final Azure.Configurable configurable = Azure.configure()
                    .withInterceptor(new TelemetryInterceptor())
                    .withUserAgent(CommonSettings.USER_AGENT);
            Optional.ofNullable(createProxyFromConfig()).ifPresent(proxy -> {
                configurable.withProxy(proxy);
                Optional.ofNullable(createProxyAuthenticatorFromConfig()).ifPresent(configurable::withProxyAuthenticator);
            });
            final Azure azure = configurable.authenticate(credentials).withSubscription(sid);
            AzureRegisterProviderNamespaces.registerAzureNamespaces(azure);
            return azure;
        });
    }

    @Override
    public SubscriptionManager getSubscriptionManager() {
        return this.subscriptionManager;
    }

    @Override
    public Environment getEnvironment() {
        if (!isSignedIn()) {
            return null;
        }
        return CommonSettings.getEnvironment();
    }

    @Override
    public @Nullable String getManagementURI() {
        if (!isSignedIn()) {
            return null;
        }
        return getAzureEnvironment().getResourceManagerEndpoint();
    }

    @Override
    public String getStorageEndpointSuffix() {
        if (!isSignedIn()) {
            return null;
        }
        return getAzureEnvironment().getStorageEndpointSuffix();
    }

    @Override
    public void drop() {
        LOGGER.log(Level.INFO, "ServicePrincipalAzureManager.drop()");
        this.subscriptionManager.cleanSubscriptions();
    }

    protected boolean isSignedIn() {
        return false;
    }

    protected com.azure.core.management.AzureEnvironment getAzureEnvironment() {
        return com.microsoft.azure.toolkit.lib.Azure.az(AzureAccount.class).account().getEnvironment();
    }

    private static Proxy createProxyFromConfig() {
        final AzureConfiguration config = az().config();
        if (StringUtils.isNotBlank(config.getProxySource())) {
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(config.getHttpProxyHost(), config.getHttpProxyPort()));
        }
        return null;
    }

    private static Authenticator createProxyAuthenticatorFromConfig() {
        final AzureConfiguration az = az().config();
        if (StringUtils.isNoneBlank(az.getProxySource(), az.getProxyUsername(), az.getProxyPassword())) {
            return (route, response) -> {
                String credential = Credentials.basic(az.getProxyUsername(), az.getProxyPassword());
                return response.request().newBuilder()
                    .header("Proxy-Authorization", credential)
                    .build();
            };
        }
        return null;
    }
}
