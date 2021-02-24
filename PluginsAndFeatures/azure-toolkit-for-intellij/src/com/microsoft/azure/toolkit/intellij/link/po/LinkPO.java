/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.link.po;

import com.microsoft.azure.toolkit.intellij.link.base.LinkType;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(exclude = {"serviceId"})
public class LinkPO {

    private String serviceId;
    private String moduleId;
    private LinkType type;
    private String envPrefix;

    public LinkPO(String serviceId, String moduleId, LinkType type, String envPrefix) {
        this.serviceId = serviceId;
        this.moduleId = moduleId;
        this.type = type;
        this.envPrefix = envPrefix;
    }

}
