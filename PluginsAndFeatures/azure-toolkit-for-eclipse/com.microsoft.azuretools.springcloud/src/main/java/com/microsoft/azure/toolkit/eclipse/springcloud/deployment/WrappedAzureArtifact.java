/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.springcloud.deployment;

import com.microsoft.azure.toolkit.eclipse.common.artifact.AzureArtifact;
import com.microsoft.azure.toolkit.eclipse.common.artifact.AzureArtifactManager;
import com.microsoft.azure.toolkit.lib.common.model.IArtifact;

import javax.annotation.Nonnull;
import java.io.File;

public class WrappedAzureArtifact implements IArtifact {
    private final AzureArtifact artifact;

    public WrappedAzureArtifact(@Nonnull final AzureArtifact artifact) {
        this.artifact = artifact;
    }

    @Override
    public File getFile() {
        return AzureArtifactManager.getInstance().getFileForDeployment(artifact);
    }

    public AzureArtifact getArtifact() {
        return artifact;
    }

}
