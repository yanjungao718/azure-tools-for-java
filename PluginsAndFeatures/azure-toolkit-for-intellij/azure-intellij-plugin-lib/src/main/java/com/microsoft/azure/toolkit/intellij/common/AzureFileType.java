/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.charset.StandardCharsets;

public class AzureFileType implements FileType {
    private String itemName;
    private Icon icon;

    public AzureFileType(String itemName, Icon icon) {
        this.itemName = itemName;
        this.icon = icon;
    }

    @NotNull
    @Override
    public String getName() {
        return itemName;
    }

    @NotNull
    @Override
    public String getDescription() {
        return itemName;
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public boolean isBinary() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getCharset(@NotNull VirtualFile virtualFile, @NotNull byte[] bytes) {
        return StandardCharsets.UTF_8.name();
    }
}
