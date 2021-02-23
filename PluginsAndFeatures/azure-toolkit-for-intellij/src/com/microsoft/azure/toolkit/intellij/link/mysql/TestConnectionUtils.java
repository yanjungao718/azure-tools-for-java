/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.link.mysql;

import com.intellij.icons.AllIcons;
import com.microsoft.azure.toolkit.lib.common.task.AzureTask;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.mysql.cj.jdbc.ConnectionImpl;

import javax.swing.*;
import java.sql.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TestConnectionUtils {

    public static boolean testConnection(String url, String username, String password) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            DriverManager.getConnection(url, username, password);
            return true;
        } catch (ClassNotFoundException | SQLException exception) {
            System.out.println(String.format("Failed to connect to server (%s)", JdbcUrlUtils.parseHostname(url)));
            return false;
        }
    }

    public static void testConnection(String url, String username, String password, JLabel testResultLabel,
                                      JButton testResultButton, JTextPane testResultTextPane) {
        AtomicBoolean connected = new AtomicBoolean(false);
        AtomicReference<StringBuilder> testResult = new AtomicReference<>();
        Runnable runnable = () -> {
            StringBuilder resultBuilder = new StringBuilder();
            testResult.set(resultBuilder);
            // refresh property
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection connection = DriverManager.getConnection(url, username, password);
                Statement statement = connection.createStatement();
                long start = System.currentTimeMillis();
                ResultSet resultSet = statement.executeQuery("select 'hi'");
                if (resultSet.next()) {
                    String result = resultSet.getString(1);
                    connected.set("hi".equals(result));
                }
                long cost = System.currentTimeMillis() - start;
                resultBuilder.append("Connected successfully.").append(System.lineSeparator());
                resultBuilder.append("MySQL version: ").append(((ConnectionImpl) connection).getServerVersion()).append(System.lineSeparator());
                resultBuilder.append("Ping cost: ").append(cost).append("ms");

            } catch (ClassNotFoundException | SQLException exception) {
                resultBuilder.append("Failed to connect with above parameters.").append(System.lineSeparator());
                resultBuilder.append("Message: ").append(exception.getMessage());
            }
        };
        AzureTask task = new AzureTask(null, String.format("Connecting to Azure Database for MySQL (%s)...", JdbcUrlUtils.parseHostname(url)), false, runnable);
        AzureTaskManager.getInstance().runAndWait(task);
        // show result info
        testResultLabel.setVisible(true);
        testResultButton.setVisible(true);
        testResultTextPane.setText(testResult.get().toString());
        if (connected.get()) {
            testResultLabel.setIcon(AllIcons.General.InspectionsOK);
        } else {
            testResultLabel.setIcon(AllIcons.General.BalloonError);
        }
    }
}
