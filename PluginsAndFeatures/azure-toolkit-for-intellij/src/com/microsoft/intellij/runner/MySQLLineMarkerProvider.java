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
import org.jetbrains.yaml.psi.impl.YAMLKeyValueImpl;

import java.awt.event.MouseEvent;
import java.util.Objects;

public class MySQLLineMarkerProvider implements LineMarkerProvider {

    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PropertyImpl) {
            PropertyImpl property = (PropertyImpl) element;
            if (StringUtils.startsWith(property.getText(), "spring.datasource.url=${AZURE")) {
                System.out.println(element.getText());
            }
            String value = property.getValue();
            if (StringUtils.equals(property.getKey(), "spring.datasource.url") && StringUtils.startsWith(value, "${") && StringUtils.endsWith(value, "URL}")) {
                String envPrefix = value.replace("${", StringUtils.EMPTY).replace("URL}", StringUtils.EMPTY);
                Module module = ModuleUtil.findModuleForFile(element.getContainingFile().getVirtualFile(), element.getProject());
                LinkPO linker = AzureLinkStorage.getProjectStorage(element.getProject()).getLinkersByTargetId(module.getName())
                        .stream()
                        .filter(e -> LinkType.SERVICE_WITH_MODULE.equals(e.getType()) && StringUtils.equals(envPrefix, e.getEnvPrefix()))
                        .findFirst().orElse(null);
                if (Objects.nonNull(linker)) {
                    MySQLServicePO service = AzureMySQLStorage.getStorage().getServicesById(linker.getServiceId());
                    if (Objects.nonNull(service)) {
                        LineMarkerInfo lineMarkerInfo = new LineMarkerInfo<>(element, element.getTextRange(),
                                AzureIconLoader.loadIcon(AzureIconSymbol.MySQL.BIND_INTO),
                                element2 -> "THis is ...",
                                new AppMgmtNavigationHandler(service.getId()),
                                GutterIconRenderer.Alignment.LEFT);
                        return lineMarkerInfo;
                    }
                }
            }
        }
        if (element instanceof YAMLKeyValueImpl) {
            if (StringUtils.startsWith(element.getText(), "url: ${AZURE")) {
                System.out.println(element.getText());
                LineMarkerInfo lineMarkerInfo = new LineMarkerInfo<>(element, element.getTextRange(),
                        AzureIconLoader.loadIcon(AzureIconSymbol.MySQL.BIND_INTO),
                        element2 -> "THis is ...",
                        new AppMgmtNavigationHandler(null),
                        GutterIconRenderer.Alignment.LEFT);
                return lineMarkerInfo;
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
