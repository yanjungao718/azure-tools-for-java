/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.link.mysql;

import com.microsoft.azuretools.ActionConstants;
import com.microsoft.azuretools.telemetry.TelemetryConstants;
import com.microsoft.azuretools.telemetrywrapper.EventType;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;
import com.mysql.cj.jdbc.ConnectionImpl;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.*;
import java.util.Collections;

public class MySQLConnectionUtils {

    private static final String CONNECTION_ISSUE_MESSAGE = "%s Please follow https://docs.microsoft.com/en-us/azure/mysql/howto-manage-firewall-using-portal "
        + "to create a firewall rule to unblock your local access.";
    private static final int CONNECTION_ERROR_CODE = 9000;

    public static boolean connect(String url, String username, String password) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            DriverManager.getConnection(url, username, password);
            return true;
        } catch (ClassNotFoundException | SQLException exception) {
        }
        return false;
    }

    public static ConnectResult connectWithPing(String url, String username, String password) {
        boolean connected = false;
        String errorMessage = null;
        Long pingCost = null;
        String serverVersion = null;
        // refresh property
        try {
            Class.forName("com.mysql.jdbc.Driver");
            long start = System.currentTimeMillis();
            Connection connection = DriverManager.getConnection(url, username, password);
            connected = true;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select 'hi'");
            if (resultSet.next()) {
                String result = resultSet.getString(1);
                connected = "hi".equals(result);
            }
            pingCost = System.currentTimeMillis() - start;
            serverVersion = ((ConnectionImpl) connection).getServerVersion().toString();
        } catch (ClassNotFoundException | SQLException exception) {
            errorMessage = isConnectionIssue(exception) ? String.format(CONNECTION_ISSUE_MESSAGE, exception.getMessage()) : exception.getMessage();
        }
        EventUtil.logEvent(EventType.info, ActionConstants.parse(ActionConstants.MySQL.TEST_CONNECTION).getServiceName(),
                           ActionConstants.parse(ActionConstants.MySQL.TEST_CONNECTION).getOperationName(),
                           Collections.singletonMap("result", String.valueOf(connected)));
        return new ConnectResult(connected, errorMessage, pingCost, serverVersion);
    }

    private static boolean isConnectionIssue(final Exception exception){
        return exception instanceof SQLException && ((SQLException) exception).getErrorCode() == CONNECTION_ERROR_CODE;
    }

    @Getter
    @AllArgsConstructor
    public static class ConnectResult {
        private boolean connected;
        private String message;
        private Long pingCost;
        private String serverVersion;
    }
}
