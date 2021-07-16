/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.connector;

import com.intellij.openapi.project.Project;

import javax.swing.*;

public interface ResourceView {

    /**
     * Icon in connector explorer
     */
    default Icon getIcon() {
        return null;
    }


    /**
     * show properties in file editor.
     */
    default void showProperties(Project project) {
    }

}
