/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner;

import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configuration.AbstractRunConfiguration;
import com.intellij.execution.configurations.ModuleBasedConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationModule;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.microsoft.azure.toolkit.intellij.link.base.LinkType;
import com.microsoft.azure.toolkit.intellij.link.base.ServiceType;
import com.microsoft.azure.toolkit.intellij.link.mysql.PasswordConfig;
import com.microsoft.azure.toolkit.intellij.link.mysql.PasswordDialog;
import com.microsoft.azure.toolkit.intellij.link.mysql.TestConnectionUtils;
import com.microsoft.azure.toolkit.intellij.link.po.BaseServicePO;
import com.microsoft.azure.toolkit.intellij.link.po.LinkPO;
import com.microsoft.azure.toolkit.intellij.link.po.MySQLServicePO;
import com.microsoft.azure.toolkit.intellij.webapp.runner.webappconfig.IntelliJWebAppSettingModel;
import com.microsoft.azure.toolkit.intellij.webapp.runner.webappconfig.WebAppConfiguration;
import com.microsoft.azuretools.core.mvp.model.webapp.AzureWebAppMvpModel;
import com.microsoft.intellij.AzureLinkStorage;
import com.microsoft.intellij.AzureMySQLStorage;
import com.microsoft.intellij.actions.SelectSubscriptionsAction;
import com.microsoft.intellij.helpers.AzureIconLoader;
import com.microsoft.tooling.msservices.serviceexplorer.AzureIconSymbol;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class LinkAzureServiceBeforeRunProvider extends BeforeRunTaskProvider<LinkAzureServiceBeforeRunTask> {
    private static final Logger LOGGER = Logger.getInstance(SelectSubscriptionsAction.class);
    private static final String NAME = "Link Azure Service";
    private static final String DESCRIPTION = "Link Azure Service Task";
    private static final String SPRING_BOOT_CONFIGURATION_REF = "com.intellij.spring.boot.run.SpringBootApplicationRunConfiguration";
    public static final Key<LinkAzureServiceBeforeRunTask> ID = Key.create("LinkAzureServiceBeforeRunProviderId");
    public static final Key<Boolean> LINK_AZURE_SERVICE = Key.create("LinkAzureService");
    public static final Key<Map<String, String>> LINK_AZURE_SERVICE_ENVS = Key.create("LinkAzureServiceEnvs");

    @Override
    public Key<LinkAzureServiceBeforeRunTask> getId() {
        return ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription(LinkAzureServiceBeforeRunTask task) {
        return DESCRIPTION;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return AzureIconLoader.loadIcon(AzureIconSymbol.Common.AZURE);
    }

    @Nullable
    @Override
    public LinkAzureServiceBeforeRunTask createTask(@NotNull RunConfiguration runConfiguration) {
        boolean alwaysEnable = false;
        if (StringUtils.equals(runConfiguration.getClass().getName(), SPRING_BOOT_CONFIGURATION_REF)
        /*|| runConfiguration instanceof com.intellij.javaee.appServers.run.configuration.CommonStrategy*/) {
            alwaysEnable = true;
        }
        return new LinkAzureServiceBeforeRunTask(getId(), alwaysEnable);
    }

    @Override
    public boolean executeTask(@NotNull DataContext dataContext, @NotNull RunConfiguration runConfiguration,
                               @NotNull ExecutionEnvironment executionEnvironment, @NotNull LinkAzureServiceBeforeRunTask linkAzureServiceBeforeRunTask) {
        String moduleName = this.getModuleName(runConfiguration);
        if (StringUtils.isBlank(moduleName)) {
            return true;
        }
        Map<String, String> linkedEnvMap = new LinkedHashMap<>();
        retrieveEnvMap(runConfiguration.getProject(), linkedEnvMap, moduleName);
        if (MapUtils.isNotEmpty(linkedEnvMap)) {
            // set envs for remote deploy
            if (runConfiguration instanceof WebAppConfiguration) {
                IntelliJWebAppSettingModel model = ((WebAppConfiguration) runConfiguration).getModel();
                AzureWebAppMvpModel.getInstance().updateWebAppSettings(model.getSubscriptionId(), model.getWebAppId(), linkedEnvMap, new HashSet<>());
            }
            // set envs for local run
            if (runConfiguration instanceof AbstractRunConfiguration
                    || StringUtils.equals(runConfiguration.getClass().getName(), SPRING_BOOT_CONFIGURATION_REF)
                    || runConfiguration instanceof ApplicationConfiguration) {
                ((ModuleBasedConfiguration<?, ?>) runConfiguration).putUserData(LINK_AZURE_SERVICE_ENVS, linkedEnvMap);
            }
        }
        return true;
    }

    private String getModuleName(@NotNull RunConfiguration runConfiguration) {
        if (runConfiguration instanceof WebAppConfiguration) {
            String name = ((WebAppConfiguration) runConfiguration).getName();
            name = name.substring(name.indexOf(":") + 1);
            name = name.substring(name.indexOf(":") + 1);
            return name;
        }
        if (runConfiguration instanceof AbstractRunConfiguration
                || StringUtils.equals(runConfiguration.getClass().getName(), SPRING_BOOT_CONFIGURATION_REF)) {
            RunConfigurationModule module = ((ModuleBasedConfiguration<?, ?>) runConfiguration).getConfigurationModule();
            return module.getModule().getName();
        }
        if (runConfiguration instanceof ApplicationConfiguration) {
            return ((ApplicationConfiguration) runConfiguration).getConfigurationModule().getModule().getName();
        }
        return null;
    }

    private void retrieveEnvMap(Project project, Map<String, String> linkedEnvMap, String moduleName) {
        List<LinkPO> moduleRelatedLinkerList = AzureLinkStorage.getProjectStorage(project).getLinkersByTargetId(moduleName)
                .stream()
                .filter(e -> LinkType.SERVICE_WITH_MODULE.equals(e.getType()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(moduleRelatedLinkerList)) {
            return;
        }
        // services in application level
        Set<? extends BaseServicePO> serviceSet = AzureMySQLStorage.getStorage().getServices();
        for (BaseServicePO service : serviceSet) {
            for (LinkPO linker : moduleRelatedLinkerList) {
                if (!StringUtils.equals(linker.getServiceId(), service.getId())) {
                    continue;
                }
                String envPrefix = linker.getEnvPrefix();
                if (ServiceType.AZURE_DATABASE_FOR_MYSQL.equals(service.getType())) {
                    MySQLServicePO mysql = (MySQLServicePO) service;
                    String password = readPasswordCredentials(project, mysql);
                    linkedEnvMap.put(envPrefix + "URL", mysql.getUrl());
                    linkedEnvMap.put(envPrefix + "USERNAME", mysql.getUsername());
                    linkedEnvMap.put(envPrefix + "PASSWORD", password);
                }
            }
        }
    }

    private String readPasswordCredentials(Project project, MySQLServicePO service) {
        String storagedPassword = AzureMySQLStorage.getStorage().loadPassword(service, service.getPasswordSave(), service.getUsername());
        if (StringUtils.isNotBlank(storagedPassword)) {
            if (TestConnectionUtils.testConnection(service.getUrl(), service.getUsername(), storagedPassword)) {
                return storagedPassword;
            }
        }
        // re-input password
        AtomicReference<PasswordConfig> passwordConfigReference = new AtomicReference<>();
        String url = service.getUrl();
        ApplicationManager.getApplication().invokeAndWait(() -> {
            PasswordDialog dialog = new PasswordDialog(project, service.getUsername(), service.getUrl());
            dialog.setOkActionListener(data -> {
                dialog.close();
                String inputPassword = String.valueOf(data.getPassword());
                if (TestConnectionUtils.testConnection(service.getUrl(), service.getUsername(), inputPassword)) {
                    AzureMySQLStorage.getStorage().savePassword(service, data.getPasswordSaveType(), service.getUsername(), inputPassword);
                    if (!Objects.equals(service.getPasswordSave(), data.getPasswordSaveType())) {
                        service.setPasswordSave(data.getPasswordSaveType());
                    }
                }
                passwordConfigReference.set(data);
            });
            dialog.show();
        });
        PasswordConfig passwordConfig = passwordConfigReference.get();
        String inputPassword = String.valueOf(passwordConfig.getPassword());
        return inputPassword;
    }

}
