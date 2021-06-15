/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.lib.function;

import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.toolkit.intellij.function.FunctionAppComboBoxModel;
import com.microsoft.azure.toolkit.intellij.function.runner.deploy.FunctionDeployModel;
import com.microsoft.azure.toolkit.intellij.function.runner.library.function.CreateFunctionHandler;
import com.microsoft.azuretools.telemetrywrapper.EventUtil;

import static com.microsoft.azuretools.telemetry.TelemetryConstants.CREATE_FUNCTION_APP;
import static com.microsoft.azuretools.telemetry.TelemetryConstants.FUNCTION;


public class FunctionAppService {

    private static final FunctionAppService instance = new FunctionAppService();

    public static FunctionAppService getInstance() {
        return FunctionAppService.instance;
    }

    public FunctionApp createFunctionApp(final FunctionAppConfig config) {
        return EventUtil.executeWithLog(FUNCTION, CREATE_FUNCTION_APP, operation -> {
            final FunctionDeployModel functionDeployModel = new FunctionDeployModel();
            functionDeployModel.setFunctionAppConfig(config);
            operation.trackProperties(functionDeployModel.getTelemetryProperties());

            final CreateFunctionHandler createFunctionHandler = new CreateFunctionHandler(functionDeployModel, operation);
            return createFunctionHandler.execute();
        });
    }
}
