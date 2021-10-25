/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common.artifact;


import org.apache.maven.project.MavenProject;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;

import javax.annotation.Nonnull;
import java.io.File;

public class AzureArtifact {
    private final String name;

    private final AzureArtifactType type;

    private final Object referencedObject;

    private AzureArtifact(final AzureArtifactType type, final String name, Object obj) {
        this.type = type;
        this.name = name;
        this.referencedObject = obj;
    }

    public static AzureArtifact createFromFile(@Nonnull String filePath) {
        File file = new File(filePath);
        return new AzureArtifact(AzureArtifactType.File, filePath, file);
    }

    public String getName() {
        return name;
    }

    public String getArtifactIdentifier() {
        switch (getType()) {
            case Maven:
                return ((MavenProject) getReferencedObject()).getId();
            case File:
                return ((File) getReferencedObject()).getAbsolutePath();
            default:
                return null;
        }
    }

    public AzureArtifactType getType() {
        return type;
    }

    public Object getReferencedObject() {
        return referencedObject;
    }

    public static AzureArtifact createFromMavenProject(MavenProject mavenProject) {
        return new AzureArtifact(AzureArtifactType.Maven,
                mavenProject.getId(),
                mavenProject);
    }

    public Image getIcon() {
        ISharedImages images = (ISharedImages) JavaUI.getSharedImages();
        switch (type) {
            case Maven:
                // TODO(andxu): get maven icon
                return null;
            case File:
                return images.getImageDescriptor(ISharedImages.IMG_OBJ_FILE).createImage();
            default:
                return null;
        }
    }
}
