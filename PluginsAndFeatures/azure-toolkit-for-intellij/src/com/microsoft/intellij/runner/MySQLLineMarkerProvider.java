/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij.runner;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.lang.properties.psi.impl.PropertyImpl;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiElement;
import com.microsoft.azure.management.mysql.v2020_01_01.Server;
import com.microsoft.azure.toolkit.intellij.link.base.LinkType;
import com.microsoft.azure.toolkit.intellij.link.mysql.JdbcUrl;
import com.microsoft.azure.toolkit.intellij.link.po.LinkPO;
import com.microsoft.azure.toolkit.intellij.link.po.MySQLServicePO;
import com.microsoft.azuretools.core.mvp.model.AzureMvpModel;
import com.microsoft.azuretools.core.mvp.model.mysql.MySQLMvpModel;
import com.microsoft.intellij.AzureLinkStorage;
import com.microsoft.intellij.AzureMySQLStorage;
import com.microsoft.intellij.helpers.AzureIconLoader;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.serviceexplorer.AzureIconSymbol;
import com.microsoft.tooling.msservices.serviceexplorer.azure.mysql.MySQLNode;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.regex.Pattern;

public class MySQLLineMarkerProvider implements LineMarkerProvider {

    private static final Pattern MYSQL_PROPERTY_URL_VALUE = Pattern.compile("..");

    @Override
    public LineMarkerInBfo getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PropertyImpl) {
            PropertyImpl property = (PropertyImpl) element;
            String value = property.getValue();
            if (StringUtils.equals(property.getKey(), "spring.datasource.url") && StringUtils.startsWith(value, "${") && StringUtils.endsWith(value, "_URL}")) {
                String envPrefix = value.substring(2, value.length() - 4);
                Module module = ModuleUtil.findModuleForFile(element.getContainingFile().getVirtualFile(), element.getProject());
                LinkPO linker = AzureLinkStorage.getProjectStorage(element.getProject()).getLinkersByModuleId(module.getName())
                        .stream()
                        .filter(e -> LinkType.SERVICE_WITH_MODULE.equals(e.getType()) && StringUtils.equals(envPrefix, e.getEnvPrefix()))
                        .findFirst().orElse(null);
                if (Objects.nonNull(linker)) {
                    MySQLServicePO service = AzureMySQLStorage.getStorage().getServicesById(linker.getServiceId());
                    if (Objects.nonNull(service)) {
                        JdbcUrl url = JdbcUrl.from(service.getUrl());
                        LineMarkerInfo lineMarkerInfo = new LineMarkerInfo<>(element, element.getTextRange(),
                                AzureIconLoader.loadIcon(AzureIconSymbol.MySQL.BIND_INTO),
                                element2 -> String.format("Link to Azure Database for MySQL (%s)", url.getHostname()),
                                new AppMgmtNavigationHandler(service.getId()),
                                GutterIconRenderer.Alignment.LEFT);
                        return lineMarkerInfo;
                    }
                }
            }
        }
        return null;
    }

    public class AppMgmtNavigationHandler implements GutterIconNavigationHandler {

        private String serviceId;

        AppMgmtNavigationHandler(String serviceId) {
            this.serviceId = serviceId;
        }

        @Override
        public void navigate(MouseEvent mouseEvent, PsiElement psiElement) {
            String[] serviceIdSegments = serviceId.split("#");
            String subscriptionId = AzureMvpModel.getSegment(serviceIdSegments[0], "subscriptions");
            String resourceGroup = AzureMvpModel.getSegment(serviceIdSegments[0], "resourceGroups");
            String name = AzureMvpModel.getSegment(serviceIdSegments[0], "servers");
            Server server = MySQLMvpModel.findServer(subscriptionId, resourceGroup, name);
            final MySQLNode node = new MySQLNode(null, subscriptionId, server) {
                @Override
                public Object getProject() {
                    return psiElement.getProject();
                }
            };
            DefaultLoader.getUIHelper().openMySQLPropertyView(node);
        }
    }

}
