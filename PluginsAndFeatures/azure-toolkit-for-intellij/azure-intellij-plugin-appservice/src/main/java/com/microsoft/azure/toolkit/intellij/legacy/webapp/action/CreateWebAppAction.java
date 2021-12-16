/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.webapp.action;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.appservice.webapp.model.WebAppConfig;
import com.microsoft.azure.toolkit.intellij.legacy.webapp.WebAppCreationDialog;
import com.microsoft.azure.toolkit.lib.appservice.service.impl.WebApp;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.legacy.webapp.WebAppService;
import com.microsoft.azuretools.core.mvp.model.webapp.AzureWebAppMvpModel;
import com.microsoft.intellij.RunProcessHandler;
import com.microsoft.intellij.util.AzureLoginHelper;
import rx.Single;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;

import static com.microsoft.azure.toolkit.intellij.common.AzureBundle.message;
import static com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle.title;

public class CreateWebAppAction {
    private static final String NOTIFICATION_GROUP_ID = "Azure Plugin";
    private final WebAppService webappService;
    private final Project project;

    public CreateWebAppAction(final Project project) {
        super();
        this.project = project;
        this.webappService = WebAppService.getInstance();
    }

    @AzureOperation(name = "webapp.create", type = AzureOperation.Type.ACTION)
    public void execute() {
        AzureLoginHelper.requireSignedIn(project, () -> this.openDialog(project, null));
    }

    @AzureOperation(name = "webapp.open_creation_dialog", type = AzureOperation.Type.ACTION)
    private void openDialog(final Project project, @Nullable final WebAppConfig data) {
        final WebAppCreationDialog dialog = new WebAppCreationDialog(project);
        if (Objects.nonNull(data)) {
            dialog.setData(data);
        }
        dialog.setOkActionListener((config) -> {
            dialog.close();
            this.createWebApp(config)
                .subscribe(webapp -> {
                    final Path artifact = config.getApplication();
                    if (Objects.nonNull(artifact) && artifact.toFile().exists()) {
                        AzureTaskManager.getInstance().runLater("deploy", () -> deploy(webapp, artifact, project));
                    }
                }, (error) -> {
                    final String title = String.format("Reopen dialog \"%s\"", dialog.getTitle());
                    final Consumer<Object> act = t -> AzureTaskManager.getInstance().runLater("open dialog", () -> this.openDialog(project, config));
                    final Action<?> action = new Action<>(act, new ActionView.Builder(title));
                    AzureMessager.getMessager().error(error, null, action);
                });
        });
        dialog.show();
    }

    @AzureOperation(name = "webapp.create_app.app", params = {"config.getName()"}, type = AzureOperation.Type.ACTION)
    private Single<WebApp> createWebApp(final WebAppConfig config) {
        final AzureString title = title("webapp.create_app.app", config.getName());
        final AzureTask<WebApp> task = new AzureTask<>(null, title, false, () -> {
            final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
            indicator.setIndeterminate(true);
            return webappService.createWebApp(config);
        });
        return AzureTaskManager.getInstance().runInModalAsObservable(task).toSingle().doOnSuccess(this::notifyCreationSuccess);
    }

    @AzureOperation(name = "webapp.deploy_artifact.app", params = {"webapp.name()"}, type = AzureOperation.Type.ACTION)
    private void deploy(final WebApp webapp, final Path application, final Project project) {
        final AzureString title = title("webapp.deploy_artifact.app", webapp.name());
        final AzureTask<Void> task = new AzureTask<>(null, title, false, () -> {
            ProgressManager.getInstance().getProgressIndicator().setIndeterminate(true);
            final RunProcessHandler processHandler = new RunProcessHandler();
            processHandler.addDefaultListener();
            final ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
            processHandler.startNotify();
            consoleView.attachToProcess(processHandler);
            AzureWebAppMvpModel.getInstance().deployArtifactsToWebApp(webapp, application.toFile(), true, processHandler);
        });
        AzureTaskManager.getInstance().runInModalAsObservable(task).single().subscribe((none) -> this.notifyDeploymentSuccess(webapp)); // let root exception handler to show the error.
    }

    private void notifyCreationSuccess(final WebApp app) {
        final String title = message("webapp.create.success.title");
        final String message = message("webapp.create.success.message", app.name());
        final Notification notification = new Notification(NOTIFICATION_GROUP_ID, title, message, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
    }

    private void notifyDeploymentSuccess(final WebApp app) {
        final String title = message("webapp.deploy.success.title");
        final String message = message("webapp.deploy.success.message", app.name());
        final Notification notification = new Notification(NOTIFICATION_GROUP_ID, title, message, NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);
    }
}
