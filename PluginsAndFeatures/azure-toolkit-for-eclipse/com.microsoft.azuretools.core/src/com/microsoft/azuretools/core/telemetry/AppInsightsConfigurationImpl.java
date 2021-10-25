/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azuretools.core.telemetry;

import com.microsoft.azuretools.azurecommons.xmlhandling.DataOperations;
import com.microsoft.azuretools.core.utils.Messages;
import com.microsoft.azuretools.core.utils.PluginUtil;
import com.microsoft.azuretools.telemetry.AppInsightsConfiguration;
import org.apache.commons.lang3.BooleanUtils;

import java.io.File;
import java.util.UUID;

public class AppInsightsConfigurationImpl implements AppInsightsConfiguration {
    static final String EVENT_NAME_PREFIX = "AzurePlugin.Eclipse.";
    // event name used for new telemetry
    static final String EVENT_NAME = "AzurePlugin.Eclipse";
    static String pluginInstLoc = String.format("%s%s%s", PluginUtil.pluginFolder, File.separator,
        Messages.commonPluginID);
    static String dataFile = String.format("%s%s%s", pluginInstLoc, File.separator, Messages.dataFileName);
    static String sessionId = UUID.randomUUID().toString();
    static String buildId = "Eclipse " + System.getProperty("eclipse.buildId");
    private static String version;
    private static String instId;
    private static String prefVal;
    private static Boolean validated = null;

    @Override
    public String sessionId() {
        return sessionId;
    }

    @Override
    public String pluginVersion() {
        if (version == null) {
            version = DataOperations.getProperty(dataFile, Messages.version);
        }
        return version;
    }

    @Override
    public String installationId() {
        if (instId == null) {
            instId = DataOperations.getProperty(dataFile, Messages.instID);
        }
        return instId;
    }

    @Override
    public String preferenceVal() {
        if (prefVal == null) {
            prefVal = DataOperations.getProperty(dataFile, Messages.prefVal);
        }
        return prefVal;
    }

    @Override
    public boolean validated() {
        if (validated == null) {
            validated = new File(pluginInstLoc).exists() && new File(dataFile).exists();
        }
        return BooleanUtils.isTrue(validated);
    }

    @Override
    public String eventNamePrefix() {
        return EVENT_NAME_PREFIX;
    }

    @Override
    public String ide() {
        return buildId;
    }

    @Override
    public String eventName() {
        return EVENT_NAME;
    }

}
