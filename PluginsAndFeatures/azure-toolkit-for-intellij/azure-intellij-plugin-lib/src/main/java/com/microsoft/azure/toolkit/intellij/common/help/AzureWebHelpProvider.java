/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.common.help;

import com.intellij.openapi.help.WebHelpProvider;
import com.microsoft.azure.toolkit.intellij.AzurePlugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class AzureWebHelpProvider extends WebHelpProvider {
    public static final String HELP_SIGN_IN = AzurePlugin.PLUGIN_ID + "." + "sign_in";

    private static final Map<String, String> HELP_URLS = Map.of(
        AzureWebHelpProvider.HELP_SIGN_IN, "https://docs.microsoft.com/en-us/azure/azure-toolkit-for-intellij-sign-in-instructions"
    );

    @Override
    @Nullable
    public String getHelpPageUrl(@Nonnull String helpTopicId) {
        return HELP_URLS.get(helpTopicId);
    }
}
