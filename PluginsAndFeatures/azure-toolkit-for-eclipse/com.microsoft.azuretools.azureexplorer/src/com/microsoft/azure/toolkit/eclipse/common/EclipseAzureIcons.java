/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.common;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.program.Program;

import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcon;
import com.microsoft.azure.toolkit.ide.common.icon.AzureIcons;
import com.microsoft.azuretools.azureexplorer.Activator;

public class EclipseAzureIcons {
    public static final String FILE_EXTENSION_ICON_PREFIX = "file/";
    public static final String FOLDER_ICON_PATH = "/icons/storagefolder.png";
    private static final Map<String, ImageDescriptor> icons = new ConcurrentHashMap<>();
    private static final Map<AzureIcon, ImageDescriptor> azureIcons = new ConcurrentHashMap<AzureIcon, ImageDescriptor>() {
        {
            put(AzureIcons.Common.REFRESH_ICON, Activator.getImageDescriptor("/icons/refresh_action.png"));
            put(AzureIcons.Action.START, Activator.getImageDescriptor("/icons/run.png"));
            put(AzureIcons.Action.STOP, Activator.getImageDescriptor("/icons/stop_action.png"));
            put(AzureIcons.Action.RESTART, Activator.getImageDescriptor("/icons/restart.png"));
            put(AzureIcons.Action.REFRESH, Activator.getImageDescriptor("/icons/refresh_action.png"));
            put(AzureIcons.Action.DEPLOY, Activator.getImageDescriptor("/icons/upload.png"));
            put(AzureIcons.Action.CREATE, Activator.getImageDescriptor("/icons/create.png"));
            put(AzureIcons.Action.DELETE, Activator.getImageDescriptor("/icons/remove.png"));
            put(AzureIcons.Action.PORTAL, Activator.getImageDescriptor("/icons/action/portal.png"));
            put(AzureIcons.Action.BROWSER, Activator.getImageDescriptor("/icons/action/portal.png"));
            put(AzureIcons.Action.ADD, Activator.getImageDescriptor("/icons/create.png"));
            put(AzureIcons.Action.REMOVE, Activator.getImageDescriptor("/icons/remove.png"));
            put(AzureIcons.Action.EDIT, Activator.getImageDescriptor("/icons/edit.png")); //
            put(AzureIcons.Action.PROPERTIES, Activator.getImageDescriptor("/icons/properties.png"));
            put(AzureIcons.Action.SELECT_SUBSCRIPTION, Activator.getImageDescriptor("/icons/filter.png"));
            put(AzureIcons.Common.SELECT_SUBSCRIPTIONS, Activator.getImageDescriptor("/icons/filter.png"));
            put(AzureIcons.Common.DELETE, Activator.getImageDescriptor("/icons/remove.png"));
            put(AzureIcons.Common.RESTART, Activator.getImageDescriptor("/icons/restart.png"));
            put(AzureIcons.Common.SHOW_PROPERTIES, Activator.getImageDescriptor("/icons/properties.png"));
            put(AzureIcons.Common.UNKNOWN_ICON, Activator.getImageDescriptor("/icons/unknown.png"));
        }
    };

    static {
        azureIcons.forEach((key, value) -> icons.put(key.getIconPath(), value));
    }
    
    public static ImageDescriptor getIcon(String input) {
    	String fallback = null;
        if (input.contains(":")) {
            final String[] parts = input.split(":");
            input = parts[0];
            if (parts.length > 1) {
                fallback = parts[1];
            }
        }
        return getIcon(input, fallback);
    }
    
    public static ImageDescriptor getIcon(AzureIcon azureIcon) {
        return azureIcons.computeIfAbsent(azureIcon, EclipseAzureIcons::getAzureIcon);
    }
    
    public static ImageDescriptor getIcon(String input, String fallback) {
    	if (StringUtils.startsWith(input, FILE_EXTENSION_ICON_PREFIX)) {
            return getFileTypeIcon(input);
        }
        final String extension = FilenameUtils.getExtension(input);
        final String iconPath = StringUtils.equals(extension, "svg")
                ? FilenameUtils.removeExtension(input) + FilenameUtils.EXTENSION_SEPARATOR + "png"
                : input;
        return icons.computeIfAbsent(input, path -> Optional.ofNullable(Activator.getImageDescriptor(iconPath))
        		.orElseGet(() -> StringUtils.isEmpty(fallback) || StringUtils.equals(input, fallback) ? null : getIcon(fallback, null)));
    }
    
    private static ImageDescriptor getFileTypeIcon(String input) {
    	final String fileExtension = StringUtils.removeStart(input, FILE_EXTENSION_ICON_PREFIX);
        if (StringUtils.equalsAnyIgnoreCase(fileExtension, "root", "folder")) {
            return Activator.getImageDescriptor(FOLDER_ICON_PATH);
        }
        final Program program = org.eclipse.swt.program.Program.findProgram(fileExtension);
        return Optional.ofNullable(program).map(Program::getImageData).map(ImageDescriptor::createFromImageData).orElse(null);
    }
    
    private static ImageDescriptor getAzureIcon(AzureIcon azureIcon) {
        return getIcon(AzureIcon.getIconPathWithModifier(azureIcon), azureIcon.getIconPath());
    }
}
