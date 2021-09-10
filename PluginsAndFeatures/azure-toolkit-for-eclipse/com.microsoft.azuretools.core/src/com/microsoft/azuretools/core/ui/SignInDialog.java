/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.core.ui;

import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.Account;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.auth.AzureCloud;
import com.microsoft.azure.toolkit.lib.auth.core.devicecode.DeviceCodeAccount;
import com.microsoft.azure.toolkit.lib.auth.model.AccountEntity;
import com.microsoft.azure.toolkit.lib.auth.model.AuthConfiguration;
import com.microsoft.azure.toolkit.lib.auth.model.AuthType;
import com.microsoft.azure.toolkit.lib.auth.util.AzureEnvironmentUtils;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azuretools.adauth.IDeviceLoginUI;
import com.microsoft.azuretools.authmanage.AuthMethod;
import com.microsoft.azuretools.authmanage.models.AuthMethodDetails;
import com.microsoft.azuretools.core.components.AzureTitleAreaDialogWrapper;
import com.microsoft.azuretools.sdkmanage.IdentityAzureManager;
import com.microsoft.azuretools.telemetrywrapper.ErrorType;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import com.microsoft.azuretools.telemetrywrapper.Operation;
import com.microsoft.azuretools.telemetrywrapper.TelemetryManager;
import lombok.Getter;
import lombok.Lombok;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import rx.Single;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.microsoft.azuretools.telemetry.TelemetryConstants.*;

public class SignInDialog extends AzureTitleAreaDialogWrapper {
    private static final String AZURE_SIGN_IN = "Azure Sign In";
    private Button btnAzureCli;
    private Button btnDeviceCode;
    private Button btnSPRadio;
    private Label lblAzureCli;
    private Label lblDeviceInfo;
    private Label lblSP;

    private AuthMethodDetails authMethodDetails;
    private String accountEmail;
    FileDialog fileDialog;

    private AuthConfiguration data = new AuthConfiguration();

    @Getter
    private CompletableFuture<AuthMethodDetails> authMethodDetailFuture = new CompletableFuture<>();

    private static AuthMethodDetails apply(Account ac) {
        return fromAccountEntity(ac.getEntity());
    }

    public AuthMethodDetails getAuthMethodDetails() {
        return authMethodDetails;
    }

    /**
     * Create the dialog.
     * @param parentShell
     */
    public SignInDialog(Shell parentShell) {
        super(parentShell);
        setHelpAvailable(false);
        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    public static SignInDialog go(Shell parentShell, AuthMethodDetails authMethodDetails) {
        SignInDialog d = new SignInDialog(parentShell);
        d.authMethodDetails = authMethodDetails;
        d.create();
        if (d.open() == Window.OK) {
            return d;
        }
        return null;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        Button okButton = getButton(IDialogConstants.OK_ID);
        okButton.setText("Sign in");
    }

    /**
     * Create contents of the dialog.
     * @param parent
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        setMessage(AZURE_SIGN_IN);
        setTitle(AZURE_SIGN_IN);
        getShell().setText(AZURE_SIGN_IN);
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayout(new FillLayout(SWT.HORIZONTAL));
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite composite = new Composite(container, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        Group group = new Group(composite, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        group.setText("Authentication Method");
        group.setLayout(new GridLayout(1, false));

        btnAzureCli = createRadioButton(group, "Azure CLI", AuthType.AZURE_CLI);
        lblAzureCli = createDescriptionLabel(group, "Consume your existing Azure CLI credential..");

        btnDeviceCode = createRadioButton(group, "Device Login", AuthType.DEVICE_CODE);
        lblDeviceInfo = createDescriptionLabel(group, "You will need to open an external browser and sign in with a generated device code.");

        btnSPRadio = createRadioButton(group, "Service Principal", AuthType.SERVICE_PRINCIPAL);

        lblSP = createDescriptionLabel(group, "Use Azure Active Directory service principal for sign in.");

        return area;
    }

    private Button createRadioButton(Composite parent, String label, AuthType type) {
        final Button radioButton = new Button(parent, SWT.RADIO);
        radioButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (radioButton.getSelection()) {
                    // Set the radio button to be focused or the default one will be selected when refresh
                    // For issue https://github.com/microsoft/azure-tools-for-java/issues/3543
                    radioButton.setFocus();
                    data.setType(type);
                    syncControlControls();
                }
            }
        });
        radioButton.setText(label);
        return radioButton;
    }

    private Label createDescriptionLabel(Composite parent, String description) {
        Composite compositeDevice = new Composite(parent, SWT.NONE);
        GridData gdCompositeDevice = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdCompositeDevice.heightHint = 38;
        gdCompositeDevice.widthHint = 66;
        compositeDevice.setLayoutData(gdCompositeDevice);
        compositeDevice.setLayout(new GridLayout(1, false));
        Label label = new Label(compositeDevice, SWT.WRAP);
        GridData gdLblDeviceInfo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gdLblDeviceInfo.horizontalIndent = 11;
        label.setLayoutData(gdLblDeviceInfo);
        label.setText(description);
        return label;
        //
    }

    private void syncControlControls() {
        setErrorMessage(null);
        AuthType type = data.getType();
        lblDeviceInfo.setEnabled(type == AuthType.DEVICE_CODE);
        lblAzureCli.setEnabled(type == AuthType.AZURE_CLI);
        boolean spLoginSelected = type == AuthType.SERVICE_PRINCIPAL;
        lblSP.setEnabled(spLoginSelected);
    }

    @SneakyThrows
    @Override
    public void okPressed() {
        AuthConfiguration auth = new AuthConfiguration();
        if (btnAzureCli.getSelection()) {
            auth.setType(AuthType.AZURE_CLI);
        } else if (btnDeviceCode.getSelection()) {
            auth.setType(AuthType.DEVICE_CODE);
            super.okPressed();
            doDeviceCodeLogin();
            return;

        } else if (btnSPRadio.getSelection()) {
            auth.setType(AuthType.SERVICE_PRINCIPAL);
            throw new UnsupportedOperationException("SP doesn't support by now");
        }
        loginNonDeviceCodeSingle(auth).subscribe(details -> {
            this.authMethodDetailFuture.complete(details);
        });
        super.okPressed();
    }

    private void doDeviceCodeLogin() {
        DeviceCodeAccount account = loginDeviceCodeSingle().toBlocking().value();
        final IDeviceLoginUI deviceLoginUI = new DeviceLoginWindow();
        new Thread(() -> {
            authMethodDetailFuture =
                    account.continueLogin()
                            .subscribeOn(Schedulers.boundedElastic())
                    .map(SignInDialog::apply)
                    .doFinally(signal -> deviceLoginUI.closePrompt())
                    .toFuture();
            deviceLoginUI.setFuture(authMethodDetailFuture);
        }).start();
        deviceLoginUI.promptDeviceCode(account.getDeviceCode());
    }

    private static AuthMethodDetails fromAccountEntity(AccountEntity entity) {
        final AuthMethodDetails authMethodDetails = new AuthMethodDetails();
        authMethodDetails.setAuthMethod(AuthMethod.IDENTITY);
        authMethodDetails.setAuthType(entity.getType());
        authMethodDetails.setClientId(entity.getClientId());
        authMethodDetails.setTenantId(CollectionUtils.isEmpty(entity.getTenantIds()) ? "" : entity.getTenantIds().get(0));
        authMethodDetails.setAzureEnv(AzureEnvironmentUtils.getCloudNameForAzureCli(entity.getEnvironment()));
        authMethodDetails.setAccountEmail(entity.getEmail());
        return authMethodDetails;
    }

    private static Single<DeviceCodeAccount> loginDeviceCodeSingle() {
        final AzureString title = AzureOperationBundle.title("account.sign_in");
        final AzureTask<DeviceCodeAccount> deviceCodeTask = new AzureTask<>(null, title, true, () -> {
            final AzureAccount az = Azure.az(AzureAccount.class);
            return (DeviceCodeAccount) checkCanceled(null, az.loginAsync(AuthType.DEVICE_CODE, true), () -> {
                throw Lombok.sneakyThrow(new InterruptedException("user cancel"));
            });
        });
        return AzureTaskManager.getInstance().runInBackgroundAsObservable(deviceCodeTask).toSingle();
    }

    private static Single<AuthMethodDetails> loginNonDeviceCodeSingle(AuthConfiguration auth) {
        final AzureString title = AzureOperationBundle.title("account.sign_in");
        final AzureTask<AuthMethodDetails> task = new AzureTask<>(null, title, true, () -> {
            // todo add indicator
            return doLogin(null, auth);
        });
        return AzureTaskManager.getInstance().runInBackgroundAsObservable(task).toSingle();
    }

    private static AuthMethodDetails doLogin(IProgressMonitor indicator, AuthConfiguration auth) {
        AuthMethodDetails authMethodDetailsResult = new AuthMethodDetails();
        switch (auth.getType()) {
            case SERVICE_PRINCIPAL:
                authMethodDetailsResult = call(() -> checkCanceled(indicator, IdentityAzureManager.getInstance().signInServicePrincipal(auth),
                        AuthMethodDetails::new), "sp");
                break;
            case AZURE_CLI:
                authMethodDetailsResult = call(() -> checkCanceled(indicator, IdentityAzureManager.getInstance().signInAzureCli(),
                        AuthMethodDetails::new), "az");
                break;
            case OAUTH2:
                authMethodDetailsResult = call(() -> checkCanceled(indicator, IdentityAzureManager.getInstance().signInOAuth(),
                        AuthMethodDetails::new), "oauth");
                break;
            default:
                break;
        }
        return authMethodDetailsResult;
    }

    private static <T> T call(Callable<T> loginCallable, String authMethod) {
        final Operation operation = TelemetryManager.createOperation(ACCOUNT, SIGNIN);
        final Map<String, String> properties = new HashMap<>();
        properties.put(SIGNIN_METHOD, authMethod);

        try {
            operation.start();
            operation.trackProperties(properties);
            operation.trackProperty(AZURE_ENVIRONMENT, Azure.az(AzureCloud.class).getName());
            return loginCallable.call();
        } catch (Exception e) {
            if (shouldNoticeErrorToUser(e)) {
                EventUtil.logError(operation, ErrorType.userError, e, properties, null);
            }
            throw new AzureToolkitRuntimeException(e.getMessage(), e);
        } finally {
            operation.complete();
        }
    }

    private static <T> T checkCanceled(IProgressMonitor indicator, Mono<? extends T> mono, Supplier<T> supplier) {
        if (indicator == null) {
            return mono.block();
        }
        final Mono<T> cancelMono = Flux.interval(Duration.ofSeconds(1)).map(ignore -> indicator.isCanceled())
                .any(cancel -> cancel).map(ignore -> supplier.get()).subscribeOn(Schedulers.boundedElastic());
        return Mono.firstWithSignal(cancelMono, mono.subscribeOn(Schedulers.boundedElastic())).block();
    }

    private static boolean shouldNoticeErrorToUser(Throwable cause) {
        if (cause instanceof InterruptedException) {
            return false;
        }

        if (cause instanceof MsalClientException && StringUtils.equals(cause.getMessage(), "No Authorization code was returned from the server")) {
            return false;
        }
        return true;
    }
}
