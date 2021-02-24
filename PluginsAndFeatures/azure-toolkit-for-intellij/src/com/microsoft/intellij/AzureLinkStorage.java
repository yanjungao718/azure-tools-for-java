/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.link.base.LinkType;
import com.microsoft.azure.toolkit.intellij.link.po.LinkPO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom.Content;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class AzureLinkerStorage {

    private Set<LinkPO> linkers = new LinkedHashSet<>();

    private AzureLinkerStorage() {
    }

    public static AzureLinkerStorage getStorage() {
        return ServiceManager.getService(AzureLinkerStorage.App.class);
    }

    public static AzureLinkerStorage getProjectStorage(Project project) {
        return ServiceManager.getService(project, AzureLinkerStorage.Prj.class);
    }

    public boolean addLinker(LinkPO linker) {
        if (!linkers.add(linker)) {
            linkers.remove(linker);
            return linkers.add(linker);
        }
        return true;
    }

    public Set<LinkPO> getLinkers() {
        return this.linkers;
    }

    public List<LinkPO> getLinkersByType(LinkType type) {
        return linkers.stream().filter(e -> Objects.equals(e.getType(), type)).collect(Collectors.toList());
    }

    /**
     * For type = SERVICE_TO_PROJECT: sourceId is the id of the service, and targetId is the id of project or module.
     * For type = SERVICE_TO_SERVICE: sourceId is the id of the first service, and targetId is the id of the second service.
     */
    public List<LinkPO> getLinkersBySourceId(String sourceId) {
        return linkers.stream().filter(e -> StringUtils.equals(sourceId, e.getServiceId())).collect(Collectors.toList());
    }

    /**
     * For type = SERVICE_TO_PROJECT: targetId is the id of the service, and targetId is the id of project or module.
     * For type = SERVICE_TO_SERVICE: targetId is the id of the first service, and targetId is the id of the second service.
     */
    public List<LinkPO> getLinkersByTargetId(String targetId) {
        return linkers.stream().filter(e -> StringUtils.equals(targetId, e.getModuleId())).collect(Collectors.toList());
    }

    public static class AzureLinkerStorageStateComponent extends AzureLinkerStorage {

        public Element getState() {
            Element rootElement = new Element("azureLinks");
            // Element linkersElement = new Element("linkers");
            // rootElement.addContent(linkersElement);
            this.writeState(rootElement);
            return rootElement;
        }

        private void writeState(Element linkersElement) {
            for (LinkPO linker : super.getLinkers()) {
                Element linkerElement = new Element("link");
                linkerElement.setAttribute("type", linker.getType().name());
                if (StringUtils.isNotBlank(linker.getEnvPrefix())) {
                    linkerElement.setAttribute("envPrefix", linker.getEnvPrefix());
                }
                linkerElement.addContent(new Element("serviceId").setText(linker.getServiceId()));
                linkerElement.addContent(new Element("moduleId").setText(linker.getModuleId()));
                linkersElement.addContent(linkerElement);
            }
        }

        public void loadState(@NotNull Element state) {
            this.readState(state);
            /*List<Content> contentList = state.getContent();
            contentList.forEach(e -> {
                if (e instanceof Element) {
                    Element contentElement = (Element) e;
                    if ("linkers".equals(contentElement.getName())) {
                        this.readState(contentElement);
                    }
                }
            });*/
        }

        private void readState(Element linkersElement) {
            if (CollectionUtils.isEmpty(linkersElement.getContent())) {
                return;
            }
            for (Content content : linkersElement.getContent()) {
                if (!(content instanceof Element)) {
                    continue;
                }
                Element linkerElement = (Element) content;
                String envPrefix = linkerElement.getAttributeValue("envPrefix");
                String linkTypeName = linkerElement.getAttributeValue("type");
                LinkType linkType = LinkType.valueOf(linkTypeName);
                if (CollectionUtils.size(linkerElement.getContent()) != 2) {
                    continue;
                }
                String sourceId = null;
                String targetId = null;
                for (Content innerContent : linkerElement.getContent()) {
                    if (!(content instanceof Element)) {
                        continue;
                    }
                    Element innerElement = (Element) innerContent;
                    if ("serviceId".equals(innerElement.getName())) {
                        sourceId = innerElement.getText();
                    } else if ("moduleId".equals(innerElement.getName())) {
                        targetId = innerElement.getText();
                    }
                }
                final String finalSourceId = sourceId;
                final String finalTargetId = targetId;
                if (super.getLinkers().stream()
                        .filter(e -> StringUtils.equals(e.getServiceId(), finalSourceId) && StringUtils.equals(e.getModuleId(), finalTargetId))
                        .count() <= 0L) {
                    LinkPO linkerPO = new LinkPO(finalSourceId, finalTargetId, linkType, envPrefix);
                    super.getLinkers().add(linkerPO);
                }
            }
        }
    }

    @State(
            name = "azureLinks",
            storages = {@Storage("azure/azureLinks.xml")}
    )
    public static class App extends AzureLinkerStorageStateComponent implements PersistentStateComponent<Element> {
    }

    @State(
            name = "azureLinks",
            storages = {@Storage("azure/azureLinks.xml")}
    )
    public static class Prj extends AzureLinkerStorageStateComponent implements PersistentStateComponent<Element> {
    }

}
