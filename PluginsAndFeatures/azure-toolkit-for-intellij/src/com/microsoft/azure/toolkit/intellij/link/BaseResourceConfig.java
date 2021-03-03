/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.link;

import com.microsoft.azure.toolkit.intellij.link.base.ResourceType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;


@Getter
@Setter
@ToString
public abstract class BaseResourceConfig {

    private String id = UUID.randomUUID().toString().replace("-", StringUtils.EMPTY);

    protected abstract ResourceType getType();

}
