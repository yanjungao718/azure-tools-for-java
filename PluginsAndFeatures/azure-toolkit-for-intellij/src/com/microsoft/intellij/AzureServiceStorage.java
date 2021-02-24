/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.intellij;

import com.microsoft.azure.toolkit.intellij.link.base.ServiceType;
import com.microsoft.azure.toolkit.intellij.link.po.BaseServicePO;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AzureServiceStorage<T extends BaseServicePO> {

    protected static final String ELEMENT_NAME_SERVICES = "services";
    protected static final String ELEMENT_NAME_SERVICE = "service";

    private Set<T> services = new LinkedHashSet<>();

    public boolean addService(T service) {
        if (!services.add(service)) {
            services.remove(service);
            return services.add(service);
        }
        return true;
    }

    public Set<T> getServices() {
        return this.services;
    }

    public List<T> getServicesByType(ServiceType type) {
        return services.stream().filter(e -> Objects.equals(e.getType(), type)).collect(Collectors.toList());
    }

    public T getServicesById(String id) {
        return services.stream().filter(e -> StringUtils.equals(e.getId(), id)).findFirst().orElse(null);
    }

}
