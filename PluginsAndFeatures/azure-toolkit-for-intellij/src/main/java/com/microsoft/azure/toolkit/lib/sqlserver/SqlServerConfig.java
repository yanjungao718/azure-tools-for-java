/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.sqlserver;

import com.microsoft.azure.toolkit.intellij.common.DraftResourceGroup;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.utils.Utils;
import com.microsoft.azure.toolkit.lib.auth.AzureAccount;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
public class SqlServerConfig {

    private Subscription subscription;
    private ResourceGroup resourceGroup;

    private String serverName;
    private Region region;

    private String adminUsername;
    private char[] password;
    private char[] confirmPassword;

    private boolean allowAccessFromAzureServices;
    private boolean allowAccessFromLocalMachine;

    public static SqlServerConfig getDefaultConfig(Subscription subscription) {
        final String defaultNameSuffix = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
        final SqlServerConfig config = new SqlServerConfig();
        config.setSubscription(subscription);
        DraftResourceGroup resourceGroup = new DraftResourceGroup(subscription, "rs-" + defaultNameSuffix);
        config.setResourceGroup(resourceGroup);
        config.setSubscription(subscription);
        config.setResourceGroup(resourceGroup);
        config.setRegion(Utils.selectFirstOptionIfCurrentInvalid("region",
            loadSupportedRegions(subscription.getId()),
            Region.US_EAST));
        config.setServerName("sqlserver-" + defaultNameSuffix);
        config.setAdminUsername(StringUtils.EMPTY);
        config.setPassword(StringUtils.EMPTY.toCharArray());
        config.setConfirmPassword(StringUtils.EMPTY.toCharArray());

        return config;
    }

    public static List<Region> loadSupportedRegions(String subId) {
        // this the sequence in listSupportedRegions is alphabetical order for mysql
        // we need to rearrange it according to: az account list-regions
        final List<Region> regions = Azure.az(AzureAccount.class).listRegions(subId);
        final List supportedRegions = Azure.az(AzureSqlServer.class).listSupportedRegions(subId);
        return regions.stream().filter(supportedRegions::contains).collect(Collectors.toList());
    }
}
