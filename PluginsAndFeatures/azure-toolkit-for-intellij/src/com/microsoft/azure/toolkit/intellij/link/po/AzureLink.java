/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.link.po;

import com.microsoft.azure.toolkit.intellij.link.base.ServiceType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AzureLink {

    private List<LinkPO> linkers = new ArrayList<>();
    private Map<ServiceType, List<? extends BaseServicePO>> services = new HashMap<>();

}
