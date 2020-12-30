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

import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class TelemetryParameter2 {

    // This needs to be at the beginning for the initialization to happen correctly
    private static final ConcurrentMap<String, TelemetryParameter2> VALUES_BY_NAME = new ConcurrentHashMap<>();
    @Getter
    private final String serviceName;
    @Getter
    private final String operateName;

    private TelemetryParameter2(String serviceName, String operateName) {
        this.serviceName = serviceName;
        this.operateName = operateName;
        VALUES_BY_NAME.put(this.operateName, this);
    }

    private static String buildOperateName(String head, String tail) {
        return head + "-" + tail;
    }

    public interface WebApp {
        String MODULE = "webapp";

        /*public static final String CREATE_WEBAPP = "create-webapp";
        public static final String DELETE_WEBAPP = "delete-webapp";
        public static final String DEPLOY_WEBAPP = "deploy-webapp";
        public static final String START_WEBAPP = "start-webapp";
        public static final String STOP_WEBAPP = "stop-webapp";
        public static final String RESTART_WEBAPP = "restart-webapp";
        public static final String WEBAPP_SHOWPROP = "showprop-webapp";
        public static final String WEBAPP_SSHINTO = "sshinto-webapp";
        public static final String WEBAPP_OPEN_INBROWSER = "open-inbrowser-webapp";
        public static final String START_STREAMING_LOG_WEBAPP = "start-streaming-log-webapp";
        public static final String STOP_STREAMING_LOG_WEBAPP = "stop-streaming-log-webapp";

        public static final String DEPLOY_WEBAPP_DOCKERLOCAL = "deploy-webapp-dockerlocal";
        public static final String DEPLOY_WEBAPP_DOCKERHOST = "deploy-webapp-dockerhost";
        public static final String DEPLOY_WEBAPP_CONTAINER = "deploy-webapp-container";
        public static final String DEPLOY_WEBAPP_SLOT = "deploy-webapp-slot";
        public static final String CREATE_WEBAPP_SLOT = "create-webapp-slot";
        public static final String STOP_WEBAPP_SLOT = "stop-webapp-slot";
        public static final String START_WEBAPP_SLOT = "start-webapp-slot";
        public static final String RESTART_WEBAPP_SLOT = "restart-webapp-slot";
        public static final String DELETE_WEBAPP_SLOT = "delete-webapp-slot";
        public static final String SWAP_WEBAPP_SLOT = "swap-webapp-slot";
        public static final String SHOW_WEBAPP_SLOT_PROP = "show-webapp-slot-prop";
        public static final String OPERN_WEBAPP_SLOT_BROWSER = "open-webappslot-inbrowser";
        public static final String START_STREAMING_LOG_WEBAPP_SLOT = "start-streaming-log-webapp-slot";
        public static final String STOP_STREAMING_LOG_WEBAPP_SLOT = "stop-streaming-log-webapp-slot";
        public static final String OPEN_CREATEWEBAPP_DIALOG = "open-create-webapp-dialog";

        public static final String WEBAPP_DEPLOY_TO_SLOT = "webappDeployToSlot";

        public static final String CREATE_NEWWEBAPP = "createNewWebapp";*/

        TelemetryParameter2 CREATE = new TelemetryParameter2(MODULE, "create-webapp");
        TelemetryParameter2 DELETE = new TelemetryParameter2(MODULE, "delete-webapp");
        TelemetryParameter2 DEPLOY = new TelemetryParameter2(MODULE, "deploy-webapp");
        TelemetryParameter2 START = new TelemetryParameter2(MODULE, "start-webapp");
        TelemetryParameter2 STOP = new TelemetryParameter2(MODULE, "stop-webapp");
        TelemetryParameter2 RESTART = new TelemetryParameter2(MODULE, "restart-webapp");
        TelemetryParameter2 SHOW_PROPERTIES = new TelemetryParameter2(MODULE, "showprop-webapp");
        TelemetryParameter2 SHINTO = new TelemetryParameter2(MODULE, "sshinto-webapp");
        TelemetryParameter2 OPEN_IN_PORTAL = new TelemetryParameter2(MODULE, "open-inbrowser-webapp");
        TelemetryParameter2 START_STREAMING_LOG = new TelemetryParameter2(MODULE, "start-streaming-log-webapp");
        TelemetryParameter2 STOP_STREAMING_LOG = new TelemetryParameter2(MODULE, "stop-streaming-log-webapp");

        TelemetryParameter2 DEPLOY_DOCKERLOCAL = new TelemetryParameter2(MODULE, "deploy-webapp-dockerlocal");
        TelemetryParameter2 DEPLOY_DOCKERHOST = new TelemetryParameter2(MODULE, "deploy-webapp-dockerhost");
        TelemetryParameter2 DEPLOY_CONTAINER = new TelemetryParameter2(MODULE, "deploy-webapp-container");
        // deployment slot
        TelemetryParameter2 OPEN_CREATEWEBAPP_DIALOG = new TelemetryParameter2(MODULE, "open-create-webapp-dialog");

        TelemetryParameter2 DEPLOY_TO_SLOT = new TelemetryParameter2(MODULE, "webappDeployToSlot");
        TelemetryParameter2 CREATE_NEWWEBAPP = new TelemetryParameter2(MODULE, "createNewWebapp");

        interface DeploymentSlot {
            TelemetryParameter2 DEPLOY = new TelemetryParameter2(MODULE, "deploy-webapp-slot");
            TelemetryParameter2 CREATE = new TelemetryParameter2(MODULE, "create-webapp-slot");
            TelemetryParameter2 STOP = new TelemetryParameter2(MODULE, "stop-webapp-slot");
            TelemetryParameter2 START = new TelemetryParameter2(MODULE, "start-webapp-slot");
            TelemetryParameter2 RESTART = new TelemetryParameter2(MODULE, "restart-webapp-slot");
            TelemetryParameter2 DELETE = new TelemetryParameter2(MODULE, "delete-webapp-slot");
            TelemetryParameter2 SWAP = new TelemetryParameter2(MODULE, "swap-webapp-slot");
            TelemetryParameter2 SHOW_PROPERTIES = new TelemetryParameter2(MODULE, "show-webapp-slot-prop");
            TelemetryParameter2 OPEN_IN_PORTAL = new TelemetryParameter2(MODULE, "open-webappslot-inbrowser");
            TelemetryParameter2 START_STREAMING_LOG = new TelemetryParameter2(MODULE, "start-streaming-log-webapp-slot");
            TelemetryParameter2 STOP_STREAMING_LOG = new TelemetryParameter2(MODULE, "stop-streaming-log-webapp-slot");
        }
    }

    public interface FunctionApp {
        String MODULE = "function";

        /*public static final String TRIGGER_FUNCTION = "trigger-function";
        public static final String ENABLE_FUNCTION = "enable-function";
        public static final String DISABLE_FUNCTION = "disable-function";
        public static final String RUN_FUNCTION_APP = "run-function-app";
        public static final String CREATE_FUNCTION_TRIGGER = "create-function-trigger";
        public static final String CREATE_FUNCTION_PROJECT = "create-function-project";
        public static final String CREATE_FUNCTION_APP = "create-function-app";
        public static final String DEPLOY_FUNCTION_APP = "deploy-function-app";
        public static final String DELETE_FUNCTION_APP = "delete-function-app";
        public static final String START_FUNCTION_APP = "start-function-app";
        public static final String STOP_FUNCTION_APP = "stop-function-app";
        public static final String RESTART_FUNCTION_APP = "restart-function-app";
        public static final String SHOWPROP_FUNCTION_APP = "showprop-function-app";
        public static final String START_STREAMING_LOG_FUNCTION_APP = "start-streaming-log-function-app";
        public static final String STOP_STREAMING_LOG_FUNCTION_APP = "stop-streaming-log-function-app";
        public static final String OPEN_INBROWSER_FUNCTION_APP = "open-inbrowser-function-app";*/

        TelemetryParameter2 TRIGGER = new TelemetryParameter2(MODULE, buildOperateName("trigger", MODULE));
        TelemetryParameter2 ENABLE = new TelemetryParameter2(MODULE, buildOperateName("enable", MODULE));
        TelemetryParameter2 DISABLE = new TelemetryParameter2(MODULE, buildOperateName("disable", MODULE));
        TelemetryParameter2 RUN = new TelemetryParameter2(MODULE, "run-function-app");
        TelemetryParameter2 CREATE_TRIGGER = new TelemetryParameter2(MODULE, "create-function-trigger");
        TelemetryParameter2 CREATE_PROJECT = new TelemetryParameter2(MODULE, "create-function-project");
        TelemetryParameter2 CREATE = new TelemetryParameter2(MODULE, "create-function-app");
        TelemetryParameter2 DEPLOY = new TelemetryParameter2(MODULE, "deploy-function-app");
        TelemetryParameter2 DELETE = new TelemetryParameter2(MODULE, "delete-function-app");
        TelemetryParameter2 START = new TelemetryParameter2(MODULE, "start-function-app");
        TelemetryParameter2 STOP = new TelemetryParameter2(MODULE, "stop-function-app");
        TelemetryParameter2 RESTART = new TelemetryParameter2(MODULE, "restart-function-app");
        TelemetryParameter2 SHOW_PROPERTIES = new TelemetryParameter2(MODULE, "showprop-function-app");
        TelemetryParameter2 START_STREAMING_LOG = new TelemetryParameter2(MODULE, "start-streaming-log-function-app");
        TelemetryParameter2 STOP_STREAMING_LOG = new TelemetryParameter2(MODULE, "stop-streaming-log-function-app");
        TelemetryParameter2 OPEN_IN_PORTAL = new TelemetryParameter2(MODULE, "open-inbrowser-function-app");
    }

    public interface MySQL {
        String MODULE = "azure-mysql";

        TelemetryParameter2 CREATE = new TelemetryParameter2(MODULE, buildOperateName(MODULE, "create"));
        TelemetryParameter2 DELETE = new TelemetryParameter2(MODULE, buildOperateName(MODULE, "delete"));
        TelemetryParameter2 START = new TelemetryParameter2(MODULE, buildOperateName(MODULE, "start"));
        TelemetryParameter2 STOP = new TelemetryParameter2(MODULE, buildOperateName(MODULE, "stop"));
        TelemetryParameter2 RESTART = new TelemetryParameter2(MODULE, buildOperateName(MODULE, "restart"));
        TelemetryParameter2 OPEN_IN_PORTAL = new TelemetryParameter2(MODULE, buildOperateName(MODULE, "open-in-portal"));
        TelemetryParameter2 SHOW_PROPERTIES = new TelemetryParameter2(MODULE, buildOperateName(MODULE, "show-properties"));
        TelemetryParameter2 CONNECT_TO_SERVER = new TelemetryParameter2(MODULE, buildOperateName(MODULE, "connect-to-server"));
        TelemetryParameter2 BIND_INTO = new TelemetryParameter2(MODULE, buildOperateName(MODULE, "bind-into"));
    }

    @Override
    public int hashCode() {
        return this.operateName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (!(obj instanceof TelemetryParameter2)) {
            return false;
        } else if (obj == this) {
            return true;
        } else {
            TelemetryParameter2 rhs = (TelemetryParameter2) obj;
            return this.operateName.equalsIgnoreCase(rhs.operateName);
        }
    }

}
