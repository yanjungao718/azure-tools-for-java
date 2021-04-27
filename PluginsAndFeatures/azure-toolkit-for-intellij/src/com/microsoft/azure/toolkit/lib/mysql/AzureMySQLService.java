/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.mysql;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.mysql.v2020_01_01.Server;
import com.microsoft.azure.management.mysql.v2020_01_01.ServerPropertiesForDefaultCreate;
import com.microsoft.azure.management.mysql.v2020_01_01.implementation.FirewallRuleInner;
import com.microsoft.azure.management.mysql.v2020_01_01.implementation.MySQLManager;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.toolkit.intellij.common.Draft;
import com.microsoft.azure.toolkit.intellij.connector.mysql.JdbcUrl;
import com.microsoft.azure.toolkit.intellij.connector.mysql.MySQLConnectionUtils;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.azuretools.ActionConstants;
import com.microsoft.azuretools.authmanage.AuthMethodManager;
import com.microsoft.azuretools.core.mvp.model.mysql.MySQLMvpModel;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetrywrapper.*;
import com.microsoft.azuretools.utils.NetUtils;

import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AzureMySQLService {
    private static final AzureMySQLService instance = new AzureMySQLService();

    public static AzureMySQLService getInstance() {
        return AzureMySQLService.instance;
    }

    @AzureOperation(
        name = "mysql.create",
        params = {
            "config.getServerName()",
            "config.getSubscription().displayName()"
        },
        type = AzureOperation.Type.SERVICE
    )
    public Server createMySQL(final AzureMySQLConfig config) {
        final Operation operation = TelemetryManager.createOperation(ActionConstants.MySQL.CREATE);
        try {
            operation.start();
            final String subscriptionId = config.getSubscription().subscriptionId();
            EventUtil.logEvent(EventType.info, operation, Collections.singletonMap(TelemetryConstants.SUBSCRIPTIONID, subscriptionId));
            // create resource group if necessary.
            if (config.getResourceGroup() instanceof Draft) {
                Azure azure = AuthMethodManager.getInstance().getAzureClient(subscriptionId);
                ResourceGroup newResourceGroup = azure.resourceGroups().define(config.getResourceGroup().name()).withRegion(config.getRegion()).create();
                config.setResourceGroup(newResourceGroup);
            }
            // create mysql server
            ServerPropertiesForDefaultCreate parameters = new ServerPropertiesForDefaultCreate();
            parameters.withAdministratorLogin(config.getAdminUsername())
                    .withAdministratorLoginPassword(String.valueOf(config.getPassword()))
                    .withVersion(config.getVersion());
            Server server = MySQLMvpModel.create(subscriptionId, config.getResourceGroup().name(), config.getServerName(), config.getRegion(), parameters);
            // update access from azure services
            MySQLMvpModel.FirewallRuleMvpModel.updateAllowAccessFromAzureServices(subscriptionId, server, config.isAllowAccessFromAzureServices());
            // update access from local machine
            AzureMySQLService.FirewallRuleService.getInstance().updateAllowAccessToLocalMachine(subscriptionId, server, config.isAllowAccessFromLocalMachine());
            return server;
        } catch (final RuntimeException e) {
            EventUtil.logError(operation, ErrorType.systemError, e, null, null);
            throw e;
        } finally {
            operation.complete();
        }
    }

    public static class FirewallRuleService {

        private static final String NAME_PREFIX_ALLOW_ACCESS_TO_LOCAL = "ClientIPAddress_";
        private static final Pattern IPADDRESS_PATTERN = Pattern.compile("\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}");

        private static final FirewallRuleService instance = new FirewallRuleService();

        public static FirewallRuleService getInstance() {
            return FirewallRuleService.instance;
        }

        public boolean isAllowAccessFromLocalMachine(final String subscriptionId, final Server server) {
            final List<FirewallRuleInner> firewallRules = MySQLMvpModel.FirewallRuleMvpModel.listFirewallRules(subscriptionId, server);
            return isAllowAccessFromLocalMachine(firewallRules);
        }

        public boolean isAllowAccessFromLocalMachine(final List<FirewallRuleInner> firewallRules) {
            final String ruleName = getAccessFromLocalRuleName();
            return firewallRules.stream().filter(e -> StringUtils.equals(e.name(), ruleName)).count() > 0L;
        }

        public boolean updateAllowAccessToLocalMachine(final String subscriptionId, final Server server, final boolean enable) {
            if (enable) {
                return enableAllowAccessFromLocalMachine(subscriptionId, server);
            } else {
                return disableAllowAccessFromLocalMachine(subscriptionId, server);
            }
        }

        public boolean enableAllowAccessFromLocalMachine(final String subscriptionId, final Server server) {
            if (isAllowAccessFromLocalMachine(subscriptionId, server)) {
                return true;
            }
            MySQLConnectionUtils.ConnectResult connectResult = MySQLConnectionUtils.connectWithPing(JdbcUrl.mysql(server.fullyQualifiedDomainName()),
                    server.administratorLogin() + "@" + server.name(), StringUtils.EMPTY);
            if (StringUtils.isNotBlank(connectResult.getMessage())) {
                Matcher matcher = IPADDRESS_PATTERN.matcher(connectResult.getMessage());
                if (matcher.find()) {
                    final String publicIp = matcher.group();
                    final String ruleName = getAccessFromLocalRuleName();
                    final FirewallRuleInner firewallRule = new FirewallRuleInner();
                    firewallRule.withStartIpAddress(publicIp);
                    firewallRule.withEndIpAddress(publicIp);
                    final MySQLManager mySQLManager = AuthMethodManager.getInstance().getMySQLManager(subscriptionId);
                    mySQLManager.firewallRules().inner().createOrUpdate(server.resourceGroupName(), server.name(), ruleName, firewallRule);
                    return true;
                }
            }
            return false;
        }

        public boolean disableAllowAccessFromLocalMachine(final String subscriptionId, final Server server) {
            if (!isAllowAccessFromLocalMachine(subscriptionId, server)) {
                return true;
            }
            final String ruleName = getAccessFromLocalRuleName();
            final MySQLManager mySQLManager = AuthMethodManager.getInstance().getMySQLManager(subscriptionId);
            mySQLManager.firewallRules().inner().delete(server.resourceGroupName(), server.name(), ruleName);
            return true;
        }

        private String getAccessFromLocalRuleName() {
            final String hostname = com.intellij.util.net.NetUtils.getLocalHostString();
            final String macAddress = NetUtils.getMacAddressString();
            final String ruleName = NAME_PREFIX_ALLOW_ACCESS_TO_LOCAL + hostname + macAddress;
            return ruleName;
        }

    }

}
