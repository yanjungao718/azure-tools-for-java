/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.sqlserver;

import com.google.common.base.Preconditions;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.toolkit.lib.appservice.DraftResourceGroup;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azuretools.core.mvp.model.AzureMvpModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.List;

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

    public static SqlServerConfig getDefaultConfig() {
        final String defaultNameSuffix = DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
        final SqlServerConfig config = new SqlServerConfig();
        final List<Subscription> selectedSubscriptions = AzureMvpModel.getInstance().getSelectedSubscriptions();
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(selectedSubscriptions), "There is no subscription in your account.");
        Subscription subscription = selectedSubscriptions.get(0);
        config.setSubscription(subscription);
        DraftResourceGroup resourceGroup = DraftResourceGroup.builder().subscription(subscription).name("rs-" + defaultNameSuffix).build();
        config.setResourceGroup(resourceGroup);
        config.setRegion(Region.US_EAST);
        config.setServerName("sqlserver-" + defaultNameSuffix);
        config.setAdminUsername(StringUtils.EMPTY);
        config.setPassword(StringUtils.EMPTY.toCharArray());
        config.setConfirmPassword(StringUtils.EMPTY.toCharArray());

        return config;
    }

}
