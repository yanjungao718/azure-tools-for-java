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
import com.microsoft.azure.toolkit.lib.auth.core.devicecode.DeviceCodeAccount;
import com.microsoft.azure.toolkit.lib.auth.exception.AzureToolkitAuthenticationException;
import com.microsoft.azure.toolkit.lib.auth.model.AccountEntity;
import com.microsoft.azure.toolkit.lib.auth.model.AuthConfiguration;
import com.microsoft.azure.toolkit.lib.auth.model.AuthType;
import com.microsoft.azure.toolkit.lib.auth.util.AzureEnvironmentUtils;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azuretools.Constants;
import com.microsoft.azuretools.adauth.IDeviceLoginUI;
import com.microsoft.azuretools.adauth.PromptBehavior;
import com.microsoft.azuretools.authmanage.AuthFile;
import com.microsoft.azuretools.authmanage.AuthMethod;
import com.microsoft.azuretools.authmanage.CommonSettings;
import com.microsoft.azuretools.authmanage.Environment;
import com.microsoft.azuretools.authmanage.models.AuthMethodDetails;
import com.microsoft.azuretools.authmanage.models.SubscriptionDetail;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class IdentityAzureManager extends AzureManagerBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityAzureManager.class);

    private IdentityAzureManager() {
        settings.setSubscriptionsDetailsFileName(Constants.FILE_NAME_SUBSCRIPTIONS_DETAILS_IDENTITY);
    }

    protected AzureTokenCredentials getCredentials(String tenantId) {
        return Azure.az(AzureAccount.class).account().getTokenCredentialForTenantV1(tenantId);
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
        return Azure.az(AzureAccount.class).account().getSubscriptions().stream().map(subscription -> new SubscriptionDetail(
                subscription.getId(),
                subscription.getName(),
                subscription.getTenantId(),
                subscription.isSelected())).collect(Collectors.toList());
    }

    public void selectSubscriptionByIds(List<String> subscriptionIds) {
        Azure.az(AzureAccount.class).account().selectSubscription(subscriptionIds);
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

    public Mono<AuthMethodDetails> signInAzureCli() {
        AzureAccount az = com.microsoft.azure.toolkit.lib.Azure.az(AzureAccount.class);
        return az.loginAsync(AuthType.AZURE_CLI, false).map(account -> fromAccountEntity(account.getEntity()));
    }

    public Mono<AuthMethodDetails> signInOAuth() {
        AzureAccount az = com.microsoft.azure.toolkit.lib.Azure.az(AzureAccount.class);
        return az.loginAsync(AuthType.OAUTH2, true).map(account -> fromAccountEntity(account.getEntity()));
    }

    public Mono<AuthMethodDetails> signInDeviceCode(IDeviceLoginUI deviceLoginUI) {
        AzureAccount az = com.microsoft.azure.toolkit.lib.Azure.az(AzureAccount.class);
        return az.loginAsync(AuthType.DEVICE_CODE, true).flatMap(account -> {
            deviceLoginUI.promptDeviceCode(((DeviceCodeAccount) account).getDeviceCode());
            return ((DeviceCodeAccount) account).continueLogin();
        }).map(account -> {
            deviceLoginUI.closePrompt();
            return fromAccountEntity(account.getEntity());
        });
    }

    public Mono<AuthMethodDetails> restoreSignIn(AuthMethodDetails authMethodDetails) {
        if (authMethodDetails == null || authMethodDetails.getAuthMethod() == null) {
            return Mono.just(new AuthMethodDetails());
        }
        AuthType authType = authMethodDetails.getAuthType();
        try {
            if (authType == AuthType.SERVICE_PRINCIPAL) {
                AuthFile authFile = AuthFile.fromFile(authMethodDetails.getCredFilePath());
                return signInServicePrincipal(authFile).map(ac -> authMethodDetails);
            } else if (authType == AuthType.DEVICE_CODE || authType == AuthType.OAUTH2) {
                if (StringUtils.isNoneBlank(authMethodDetails.getClientId(), authMethodDetails.getTenantId())) {
                    AccountEntity entity = new AccountEntity();
                    entity.setEnvironment(AzureEnvironmentUtils.stringToAzureEnvironment(CommonSettings.getEnvironment().getName()));
                    entity.setType(authType);
                    entity.setEmail(authMethodDetails.getAccountEmail());
                    entity.setClientId(authMethodDetails.getClientId());
                    entity.setTenantIds(Collections.singletonList(authMethodDetails.getTenantId()));
                    return Azure.az(AzureAccount.class).loginAsync(entity).map(ac -> fromAccountEntity(ac.getEntity()));
                } else {
                    throw new AzureToolkitRuntimeException("Cannot restore credentials due to version change.");
                }
            } else {
                throw new AzureToolkitRuntimeException(String.format("Cannot restore credentials: unsupported auth type '%s'.", authType));
            }

        } catch (Throwable e) {
            drop();
            LOGGER.warn(String.format("Cannot restore credentials due to error: %s", e.getMessage()));
            return Mono.just(new AuthMethodDetails());
        }
    }

    public Mono<AuthMethodDetails> signInServicePrincipal(AuthFile authFile) {
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
        return Azure.az(AzureAccount.class).loginAsync(auth, false).map(account -> {
            AuthMethodDetails authMethodDetails = fromAccountEntity(account.getEntity());
            // special handle for SP
            authMethodDetails.setCredFilePath(authFile.getFilePath());
            return authMethodDetails;
        });
    }

    public boolean isSignedIn() {
        try {
            Azure.az(AzureAccount.class).account();
            return true;
        } catch (AzureToolkitAuthenticationException ex) {
            return false;
        }
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
        return Azure.az(AzureAccount.class).account().getTokenCredentialForTenantV1(tid).getToken(resource);
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

    private static AuthMethodDetails fromAccountEntity(AccountEntity entity) {
        AuthMethodDetails authMethodDetails = new AuthMethodDetails();
        authMethodDetails.setAuthMethod(AuthMethod.IDENTITY);
        authMethodDetails.setAuthType(entity.getType());
        authMethodDetails.setClientId(entity.getClientId());
        authMethodDetails.setTenantId(entity.getTenantIds().get(0));
        authMethodDetails.setAzureEnv(AzureEnvironmentUtils.getCloudNameForAzureCli(entity.getEnvironment()));
        authMethodDetails.setAccountEmail(entity.getEmail());
        return authMethodDetails;
    }
}
