/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.AnimatedIcon;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.Account;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.auth.core.devicecode.DeviceCodeAccount;
import com.microsoft.azure.toolkit.lib.auth.model.AccountEntity;
import com.microsoft.azure.toolkit.lib.auth.model.AuthConfiguration;
import com.microsoft.azure.toolkit.lib.auth.model.AuthType;
import com.microsoft.azure.toolkit.lib.auth.util.AzureEnvironmentUtils;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle;
import com.microsoft.azure.toolkit.lib.common.operation.IAzureOperationTitle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azuretools.adauth.IDeviceLoginUI;
import com.microsoft.azuretools.authmanage.*;
import com.microsoft.azuretools.authmanage.models.AuthMethodDetails;
import com.microsoft.azuretools.sdkmanage.IdentityAzureManager;
import com.microsoft.azuretools.telemetrywrapper.*;
import com.microsoft.intellij.secure.IdeaSecureStore;
import com.microsoft.intellij.ui.components.AzureDialogWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdesktop.swingx.JXHyperlink;
import org.jetbrains.annotations.Nullable;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import rx.Single;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.microsoft.azuretools.telemetry.TelemetryConstants.*;

public class SignInWindow extends AzureDialogWrapper {
    private static final Logger LOGGER = Logger.getInstance(SignInWindow.class);
    private static final String SIGN_IN_ERROR = "Sign In Error";
    private static final IdeaSecureStore secureStore = IdeaSecureStore.getInstance();
    private JPanel contentPane;

    private JRadioButton deviceLoginRadioButton;
    private JRadioButton spRadioButton;
    private JLabel servicePrincipalCommentLabel;
    private JLabel deviceLoginCommentLabel;
    private JRadioButton azureCliRadioButton;
    private JLabel azureCliCommentLabel;
    private JRadioButton oauthLoginRadioButton;
    private JLabel labelOAuthLogin;

    private AuthMethodDetails authMethodDetails;
    private AuthMethodDetails authMethodDetailsResult;

    private String accountEmail;

    private Project project;
    private Map<AbstractButton, JComponent> radioButtonComponentsMap = new HashMap<>(3);

    public SignInWindow(AuthMethodDetails authMethodDetails, Project project) {
        super(project, true, IdeModalityType.PROJECT);
        this.project = project;
        setModal(true);
        setTitle("Azure Sign In");
        setOKButtonText("Sign in");

        this.authMethodDetails = authMethodDetails;

        oauthLoginRadioButton.addItemListener(e -> refreshAuthControlElements());

        spRadioButton.addActionListener(e -> refreshAuthControlElements());

        deviceLoginRadioButton.addActionListener(e -> refreshAuthControlElements());

        azureCliRadioButton.addActionListener(e -> refreshAuthControlElements());


        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(oauthLoginRadioButton);
        buttonGroup.add(deviceLoginRadioButton);
        buttonGroup.add(spRadioButton);
        buttonGroup.add(azureCliRadioButton);
        azureCliRadioButton.setSelected(true);

        radioButtonComponentsMap.put(spRadioButton, servicePrincipalCommentLabel);
        radioButtonComponentsMap.put(deviceLoginRadioButton, deviceLoginCommentLabel);
        radioButtonComponentsMap.put(oauthLoginRadioButton, labelOAuthLogin);
        radioButtonComponentsMap.put(azureCliRadioButton, azureCliCommentLabel);
        init();
        this.setOKActionEnabled(false);
        checkAccountAvailability();
    }

    public AuthMethodDetails getAuthMethodDetails() {
        return authMethodDetailsResult;
    }

    @Nullable
    public static SignInWindow go(AuthMethodDetails authMethodDetails, Project project) {
        SignInWindow signInWindow = new SignInWindow(authMethodDetails, project);
        signInWindow.show();
        if (signInWindow.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            return signInWindow;
        }

        return null;
    }

    @Override
    public void doCancelAction() {
        authMethodDetailsResult = authMethodDetails;
        super.doCancelAction();
    }

    @Override
    public void doHelpAction() {
        final JXHyperlink helpLink = new JXHyperlink();
        helpLink.setURI(URI.create("https://docs.microsoft.com/en-us/azure/azure-toolkit-for-intellij-sign-in-instructions"));
        helpLink.doClick();
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "SignInWindow";
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    public Single<AuthMethodDetails> login() {
        final IAzureOperationTitle title = AzureOperationBundle.title("account.sign_in");
        final AzureTask<AuthMethodDetails> task = new AzureTask<>(null, title, true, () -> {
            final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
            indicator.setIndeterminate(true);
            return this.doLogin(indicator);
        });
        return AzureTaskManager.getInstance().runInModalAsObservable(task).toSingle();
    }

    private @Nullable AuthMethodDetails doLogin(ProgressIndicator indicator) {
        authMethodDetailsResult = new AuthMethodDetails();
        if (spRadioButton.isSelected()) { // automated
            final Map<String, String> properties = new HashMap<>();
            properties.put(AZURE_ENVIRONMENT, CommonSettings.getEnvironment().getName());
            properties.putAll(signInSPProp);
            EventUtil.logEvent(EventType.info, ACCOUNT, SIGNIN, properties, null);

            if (ApplicationManager.getApplication().isDispatchThread()) {
                call(this::doServicePrincipalLogin, "sp");
            } else {
                AzureTaskManager.getInstance().runAndWait(() -> call(this::doServicePrincipalLogin, "sp"));
            }
        } else if (deviceLoginRadioButton.isSelected()) {
            authMethodDetailsResult = call(this::doDeviceLogin, "dc");
        } else if (azureCliRadioButton.isSelected()) {
            authMethodDetailsResult = call(() -> checkCanceled(indicator, IdentityAzureManager.getInstance().signInAzureCli()), "az");
        } else if (oauthLoginRadioButton.isSelected()) {
            authMethodDetailsResult = call(() -> checkCanceled(indicator, IdentityAzureManager.getInstance().signInOAuth()), "oauth");
        }
        return authMethodDetailsResult;
    }

    private AuthMethodDetails doServicePrincipalLogin() {
        authMethodDetailsResult = new AuthMethodDetails();
        final ServicePrincipalLoginDialog dialog = new ServicePrincipalLoginDialog(project);
        if (dialog.showAndGet()) {
            AuthConfiguration data = dialog.getData();
            authMethodDetailsResult = doServicePrincipalLoginInternal(data);
            if (StringUtils.isNotBlank(data.getKey())) {
                secureStore.savePassword(StringUtils.joinWith("|", "account", data.getClient()), data.getKey());
            }
        }
        return authMethodDetailsResult;
    }

    private static AuthMethodDetails checkCanceled(ProgressIndicator indicator, Mono<? extends AuthMethodDetails> mono) {
        CompletableFuture<? extends AuthMethodDetails> future = mono.toFuture();
        Disposable disposable = Flux.interval(Duration.ofSeconds(1)).map(ts -> {
            if (indicator != null) {
                indicator.checkCanceled();
            }
            return 1;
        }).onErrorResume(e -> {
            if (e instanceof ProcessCanceledException) {
                future.cancel(true);
            }
            return Mono.empty();
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();
        try {
            return future.get();
        } catch (CancellationException e) {
            return null;
        } catch (Throwable e) {
            throw new AzureToolkitRuntimeException("Cannot login due to error: " + e.getMessage(), e);
        } finally {
            disposable.dispose();
        }
    }

    @Override
    protected void init() {
        super.init();
        azureCliRadioButton.setText("Azure CLI (checking...)");
        azureCliRadioButton.setEnabled(false);
        azureCliCommentLabel.setIcon(new AnimatedIcon.Default());
        azureCliCommentLabel.setEnabled(true);
        refreshAuthControlElements();
    }

    private void refreshAuthControlElements() {
        radioButtonComponentsMap.keySet().forEach(radio -> radioButtonComponentsMap.get(radio).setEnabled(radio.isSelected()));
        this.setOKActionEnabled(true);
    }

    private void checkAccountAvailability() {
        Flux.fromIterable(Azure.az(AzureAccount.class).accounts()).subscribeOn(Schedulers.boundedElastic())
            .flatMap(ac -> Mono.zip(Mono.just(ac), ac.checkAvailable().onErrorResume(e -> Mono.just(false))))
            .filter(Tuple2::getT2).map(Tuple2::getT1).collectList().subscribe(accounts -> {
                if (accounts.stream().anyMatch(ac -> ac.getAuthType() == AuthType.AZURE_CLI)) {
                    enableAzureCliLogin();
                } else {
                    disableAzureCliLogin();
                }
                if (accounts.stream().anyMatch(ac -> ac.getAuthType() == AuthType.OAUTH2)) {
                    oauthLoginRadioButton.setEnabled(true);
                    labelOAuthLogin.setEnabled(true);
                } else {
                    oauthLoginRadioButton.setEnabled(false);
                    labelOAuthLogin.setEnabled(false);
                }
                if (accounts.stream().anyMatch(ac -> ac.getAuthType() == AuthType.DEVICE_CODE)) {
                    deviceLoginRadioButton.setEnabled(true);
                    deviceLoginCommentLabel.setEnabled(true);
                } else {
                    deviceLoginRadioButton.setEnabled(false);
                    deviceLoginCommentLabel.setEnabled(false);
                }

                // if the selected radio button is disabled, select the first enabled button
                final JRadioButton firstSelected = Stream.of(azureCliRadioButton, oauthLoginRadioButton, deviceLoginRadioButton, spRadioButton).filter(
                    AbstractButton::isSelected).findFirst().orElse(null);
                if (firstSelected != null && !firstSelected.isEnabled()) {
                    Stream.of(azureCliRadioButton, oauthLoginRadioButton, deviceLoginRadioButton, spRadioButton)
                          .filter(Component::isEnabled).findFirst().ifPresent(button -> button.setSelected(true));
                }
                refreshAuthControlElements();
                this.setOKActionEnabled(true);
            });
    }

    private void enableAzureCliLogin() {
        azureCliCommentLabel.setIcon(null);
        azureCliRadioButton.setEnabled(true);
        azureCliRadioButton.setText("Azure CLI");
    }

    private void disableAzureCliLogin() {
        azureCliCommentLabel.setIcon(null);
        azureCliCommentLabel.setEnabled(false);
        azureCliRadioButton.setEnabled(false);
        azureCliRadioButton.setText("Azure CLI (Not logged in)");
    }

    private AuthMethodDetails doServicePrincipalLoginInternal(AuthConfiguration auth) {
        try {
            IdentityAzureManager authManager = IdentityAzureManager.getInstance();
            if (AuthMethodManager.getInstance().isSignedIn()) {
                doSignOut();
            }
            return authManager.signInServicePrincipal(auth).block();
        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorWindow.show(project, ex.getMessage(), SIGN_IN_ERROR);
        }
        return new AuthMethodDetails();
    }

    @Nullable
    private synchronized AuthMethodDetails doDeviceLogin() {
        CompletableFuture<AuthMethodDetails> deviceCodeLoginFuture = new CompletableFuture<>();
        try {
            if (AuthMethodManager.getInstance().isSignedIn()) {
                doSignOut();
            }
            final IDeviceLoginUI deviceLoginUI = CommonSettings.getUiFactory().getDeviceLoginUI();
            final AzureAccount az = com.microsoft.azure.toolkit.lib.Azure.az(AzureAccount.class);
            final Account account = az.loginAsync(AuthType.DEVICE_CODE, true).block();

            CompletableFuture<AuthMethodDetails> future =
                    account.continueLogin().map(ac -> fromAccountEntity(ac.getEntity())).doFinally(signal -> {
                        deviceLoginUI.closePrompt();
                    }).toFuture();
            deviceLoginUI.setFuture(future);
            if (ApplicationManager.getApplication().isDispatchThread()) {
                deviceLoginUI.promptDeviceCode(((DeviceCodeAccount) account).getDeviceCode());
            } else {
                AzureTaskManager.getInstance().runAndWait(() ->
                                                              deviceLoginUI.promptDeviceCode(((DeviceCodeAccount) account).getDeviceCode()));
            }
            return future.get();

        } catch (Exception ex) {
            if (!(ex instanceof CancellationException)) {
                ex.printStackTrace();
                ErrorWindow.show(project, ex.getMessage(), SIGN_IN_ERROR);
            }
        }
        return null;
    }

    private static AuthMethodDetails fromAccountEntity(AccountEntity entity) {
        AuthMethodDetails authMethodDetails = new AuthMethodDetails();
        authMethodDetails.setAuthMethod(AuthMethod.IDENTITY);
        authMethodDetails.setAuthType(entity.getType());
        authMethodDetails.setClientId(entity.getClientId());
        authMethodDetails.setTenantId(CollectionUtils.isEmpty(entity.getTenantIds()) ? "" : entity.getTenantIds().get(0));
        authMethodDetails.setAzureEnv(AzureEnvironmentUtils.getCloudNameForAzureCli(entity.getEnvironment()));
        authMethodDetails.setAccountEmail(entity.getEmail());
        return authMethodDetails;
    }

    private <T> T call(Callable<T> loginCallable, String authMethod) {
        final Operation operation = TelemetryManager.createOperation(ACCOUNT, SIGNIN);
        final Map<String, String> properties = new HashMap<>();
        properties.put(SIGNIN_METHOD, authMethod);
        Optional.ofNullable(ProgressManager.getInstance().getProgressIndicator()).ifPresent(indicator -> indicator.setText2("Signing in..."));

        try {
            operation.start();
            operation.trackProperties(properties);
            operation.trackProperty(AZURE_ENVIRONMENT, CommonSettings.getEnvironment().getName());
            return loginCallable.call();
        } catch (Exception e) {
            EventUtil.logError(operation, ErrorType.userError, e, properties, null);
            throw new AzureToolkitRuntimeException(e.getMessage(), e);
        } finally {
            operation.complete();
        }
    }

    private void doSignOut() {
        try {
            accountEmail = null;
            // AuthMethod.AD is deprecated.
            AuthMethodManager.getInstance().signOut();
        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorWindow.show(project, ex.getMessage(), "Sign Out Error");
        }
    }
}
