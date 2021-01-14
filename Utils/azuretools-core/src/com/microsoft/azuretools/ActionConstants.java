/*
 * Copyright (c) Microsoft Corporation
 *
 * All rights reserved.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azuretools.telemetry;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * TODO(qianjin): this class is designed to replace TelemetryConstants. action: replace all references to it and then delete it.
 */
public final class AzureAction {

    private static final String SEPARATOR = "#";
    private static final String OPERATION_SEPARATOR = "#";

    public static ActionEntity parse(String action) {
        String[] params = StringUtils.split(action, SEPARATOR);
        Preconditions.checkArgument(Objects.nonNull(params) && params.length == 2);
        return new ActionEntity(params[0], params[1]);
    }

    private static String build(String serviceName, String operationName) {
        Preconditions.checkArgument(!StringUtils.contains(serviceName, SEPARATOR));
        Preconditions.checkArgument(!StringUtils.contains(operationName, SEPARATOR));
        return serviceName + SEPARATOR + operationName;
    }

    private static String buildOperationName(String head, String tail) {
        return head + OPERATION_SEPARATOR + tail;
    }

    public interface WebApp {
        String MODULE = "webapp";

        String CREATE = build(MODULE, "create-webapp");
        String DELETE = build(MODULE, "delete-webapp");
        String DEPLOY = build(MODULE, "deploy-webapp");
        String START = build(MODULE, "start-webapp");
        String STOP = build(MODULE, "stop-webapp");
        String RESTART = build(MODULE, "restart-webapp");
        String SHOW_PROPERTIES = build(MODULE, "showprop-webapp");
        String SHINTO = build(MODULE, "sshinto-webapp");
        String OPEN_IN_PORTAL = build(MODULE, "webap-open-in-portal");
        String OPEN_IN_BROWSER = build(MODULE, "open-inbrowser-webapp");
        String START_STREAMING_LOG = build(MODULE, "start-streaming-log-webapp");
        String STOP_STREAMING_LOG = build(MODULE, "stop-streaming-log-webapp");

        String DEPLOY_DOCKERLOCAL = build(MODULE, "deploy-webapp-dockerlocal");
        String DEPLOY_DOCKERHOST = build(MODULE, "deploy-webapp-dockerhost");
        String DEPLOY_CONTAINER = build(MODULE, "deploy-webapp-container");
        // deployment slot
        String OPEN_CREATEWEBAPP_DIALOG = build(MODULE, "open-create-webapp-dialog");

        String DEPLOY_TO_SLOT = build(MODULE, "webappDeployToSlot");
        String CREATE_NEWWEBAPP = build(MODULE, "createNewWebapp");

        interface DeploymentSlot {
            String DEPLOY = build(MODULE, "deploy-webapp-slot");
            String CREATE = build(MODULE, "create-webapp-slot");
            String STOP = build(MODULE, "stop-webapp-slot");
            String START = build(MODULE, "start-webapp-slot");
            String RESTART = build(MODULE, "restart-webapp-slot");
            String DELETE = build(MODULE, "delete-webapp-slot");
            String SWAP = build(MODULE, "swap-webapp-slot");
            String SHOW_PROPERTIES = build(MODULE, "show-webapp-slot-prop");
            String OPEN_IN_BROWSER = build(MODULE, "open-webappslot-inbrowser");
            String OPEN_IN_PORTAL = build(MODULE, "open-webappslot-in-portal");
            String START_STREAMING_LOG = build(MODULE, "start-streaming-log-webapp-slot");
            String STOP_STREAMING_LOG = build(MODULE, "stop-streaming-log-webapp-slot");
        }
    }

    public interface FunctionApp {
        String MODULE = "function";

        String TRIGGER = build(MODULE, buildOperationName("trigger", MODULE));
        String ENABLE = build(MODULE, buildOperationName("enable", MODULE));
        String DISABLE = build(MODULE, buildOperationName("disable", MODULE));
        String RUN = build(MODULE, "run-function-app");
        String CREATE_TRIGGER = build(MODULE, "create-function-trigger");
        String CREATE_PROJECT = build(MODULE, "create-function-project");
        String CREATE = build(MODULE, "create-function-app");
        String DEPLOY = build(MODULE, "deploy-function-app");
        String DELETE = build(MODULE, "delete-function-app");
        String START = build(MODULE, "start-function-app");
        String STOP = build(MODULE, "stop-function-app");
        String RESTART = build(MODULE, "restart-function-app");
        String SHOW_PROPERTIES = build(MODULE, "showprop-function-app");
        String START_STREAMING_LOG = build(MODULE, "start-streaming-log-function-app");
        String STOP_STREAMING_LOG = build(MODULE, "stop-streaming-log-function-app");
        String OPEN_IN_PORTAL = build(MODULE, "open-inbrowser-function-app");
    }

    public interface MySQL {
        String MODULE = "azure-mysql";

        String CREATE = build(MODULE, buildOperationName(MODULE, "create"));
        String DELETE = build(MODULE, buildOperationName(MODULE, "delete"));
        String START = build(MODULE, buildOperationName(MODULE, "start"));
        String STOP = build(MODULE, buildOperationName(MODULE, "stop"));
        String RESTART = build(MODULE, buildOperationName(MODULE, "restart"));
        String OPEN_IN_PORTAL = build(MODULE, buildOperationName(MODULE, "open-in-portal"));
        String SHOW_PROPERTIES = build(MODULE, buildOperationName(MODULE, "show-properties"));
        String CONNECT_TO_SERVER = build(MODULE, buildOperationName(MODULE, "connect-to-server"));
        String BIND_INTO = build(MODULE, buildOperationName(MODULE, "bind-into"));
    }

    public interface SpringCloud {
        String MODULE = "springcloud";

        String CREATE = build(MODULE, "create-springcloud-app");
        String DELETE = build(MODULE, "delete-springcloud-app");
        String START = build(MODULE, "start-springcloud-app");
        String STOP = build(MODULE, "stop-springcloud-app");
        String RESTART = build(MODULE, "restart-springcloud-app");
        String OPEN_IN_PORTAL = build(MODULE, "open-inportal-springcloud-app");
        String OPEN_IN_BROWSER = build(MODULE, "open-inbrowser-springcloud-app");
        String SHOW_PROPERTIES = build(MODULE, "showprop-springcloud-app");

        String SAVE = build(MODULE, "save-springcloud-app");
        String REFRESH = build(MODULE, "refresh-springcloud-app");
        String DEPLOY = build(MODULE, "deploy-springcloud-app");
        String ADD_DEPENDENCY = build(MODULE, "add-dependency-springcloud-app");
        String START_STREAMING_LOG = build(MODULE, "start-streaming-log-springcloud-app");
        String STOP_STREAMING_LOG = build(MODULE, "stop-streaming-log-springcloud-app");
    }

    public interface RedisCache {
        String MODULE = "redis";

        String CREATE = build(MODULE, "create-redis");
        String DELETE = build(MODULE, "delete-redis");
        String SCAN = build(MODULE, "scan-redis");
        String GET = build(MODULE, "get-redis");
        String OPEN_IN_PORTAL = build(MODULE, "open-browser-redis");
        String SHOW_PROPERTIES = build(MODULE, "readprop-redis");
        String OPEN_EXPLORER = build(MODULE, "open-explorer-redis");
    }

    @AllArgsConstructor
    @Getter
    public static class ActionEntity {
        private final String serviceName;
        private final String operationName;
    }

}
