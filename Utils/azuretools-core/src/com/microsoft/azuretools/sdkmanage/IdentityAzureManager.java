/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.sdkmanage;

import com.azure.core.management.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.resources.Tenant;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.auth.exception.AzureToolkitAuthenticationException;
import com.microsoft.azure.toolkit.lib.auth.model.AuthConfiguration;
import com.microsoft.azure.toolkit.lib.auth.model.AuthType;
import com.microsoft.azure.toolkit.lib.auth.util.AzureEnvironmentUtils;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azuretools.Constants;
import com.microsoft.azuretools.adauth.PromptBehavior;
import com.microsoft.azuretools.authmanage.AuthFile;
import com.microsoft.azuretools.authmanage.AuthMethod;
import com.microsoft.azuretools.authmanage.Environment;
import com.microsoft.azuretools.authmanage.models.AuthMethodDetails;
import com.microsoft.azuretools.authmanage.models.SubscriptionDetail;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class IdentityAzureManager extends AzureManagerBase {
    private IdentityAzureManager() {
        settings.setSubscriptionsDetailsFileName(Constants.FILE_NAME_SUBSCRIPTIONS_DETAILS_IDENTITY);
    }

    protected AzureTokenCredentials getCredentials(String tenantId) {
        return Azure.az(AzureAccount.class).account().getTokenCredentialV1(tenantId);
    }

    private static class LazyLoader {
        static final IdentityAzureManager INSTANCE = new IdentityAzureManager();
    }

    public static IdentityAzureManager getInstance() {
        return IdentityAzureManager.LazyLoader.INSTANCE;
    }

    /**
     * Override the getSubscriptionDetails since az account has already loaded the subscriptions
     */
    public java.util.List<SubscriptionDetail> getSubscriptionDetails() {
        return Azure.az(AzureAccount.class).account().getSubscriptions().stream().map(q -> new SubscriptionDetail(
                q.getId(),
                q.getName(),
                q.getTenantId(),
                q.isSelected())).collect(Collectors.toList());
    }

    public List<String> getSelectedSubscriptionIds() {
        if (!isSignedIn()) {
            return new ArrayList<>();
        }
        List<Subscription> selectedSubscriptions = Azure.az(AzureAccount.class).account().getSelectedSubscriptions();
        if (CollectionUtils.isNotEmpty(selectedSubscriptions)) {
            return selectedSubscriptions.stream().map(Subscription::getId).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    protected List<Tenant> getTenants(com.microsoft.azure.management.Azure.Authenticated authentication) {
        if (!isSignedIn()) {
            return new ArrayList<>();
        }
        final List<String> tenantIds = Azure.az(AzureAccount.class).account().getEntity().getTenantIds();
        // override the tenants from super
        return super.getTenants(authentication).stream().filter(tenant -> tenantIds.contains(tenant.tenantId())).collect(Collectors.toList());
    }

    public AuthMethodDetails signIn(AuthFile authFile) {
        AuthConfiguration auth = new AuthConfiguration();
        String environmentName = Environment.ENVIRONMENT_LIST.stream().filter(env -> StringUtils.contains(
                env.getAzureEnvironment().managementEndpoint(),
                authFile.getManagementURI()
        )).map(Environment::getName).findFirst().orElse(null);
        if (StringUtils.isBlank(environmentName)) {
            throw new AzureToolkitAuthenticationException(String.format("Bad managementURI %s in auth file: %s",
                    authFile.getManagementURI(), authFile.getFilePath()));
        }
        AzureEnvironment environment = AzureEnvironmentUtils.stringToAzureEnvironment(environmentName);
        auth.setEnvironment(environment);
        auth.setType(AuthType.SERVICE_PRINCIPAL);
        auth.setClient(authFile.getClient());
        auth.setKey(authFile.getKey());
        auth.setCertificate(authFile.getCertificate());
        auth.setCertificatePassword(authFile.getCertificatePassword());
        auth.setTenant(authFile.getTenant());
        signInInner(auth);
        AuthMethodDetails authMethodDetails = new AuthMethodDetails();
        authMethodDetails.setAuthMethod(AuthMethod.IDENTITY);
        authMethodDetails.setAuthType(AuthType.SERVICE_PRINCIPAL);
        authMethodDetails.setCredFilePath(authFile.getFilePath());
        authMethodDetails.setAzureEnv(environmentName);
        return authMethodDetails;
    }

    public boolean isSignedIn() {
        try {
            return Azure.az(AzureAccount.class).account().isAvailable();
        } catch (AzureToolkitAuthenticationException ex) {
            return false;
        }
    }

    private void signInInner(AuthConfiguration auth) {
        Azure.az(AzureAccount.class).login(auth);
    }

    @Override
    public String getCurrentUserId() {
        if (!isSignedIn()) {
            return null;
        }
        return StringUtils.firstNonBlank(Azure.az(AzureAccount.class).account().getEntity().getEmail(), "unknown");
    }

    @Override
    public String getAccessToken(String tid, String resource, PromptBehavior promptBehavior) throws IOException {
        return Azure.az(AzureAccount.class).account().getTokenCredentialV1(tid).getToken(resource);
    }

    @Override
    protected String getCurrentTenantId() {
        return "common";
    }

    @Override
    public void drop() {
        if (!isSignedIn()) {
            return;
        }
        Azure.az(AzureAccount.class).logout();
        super.drop();
    }
}
