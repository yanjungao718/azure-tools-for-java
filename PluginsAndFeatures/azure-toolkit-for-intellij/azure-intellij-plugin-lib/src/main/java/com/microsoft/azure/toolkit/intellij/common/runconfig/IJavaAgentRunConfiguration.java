/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.common.runconfig;

import javax.annotation.Nonnull;
import java.io.File;

public interface IJavaAgentRunConfiguration {
    void setJavaAgent(@Nonnull final File javaAgent);
}
