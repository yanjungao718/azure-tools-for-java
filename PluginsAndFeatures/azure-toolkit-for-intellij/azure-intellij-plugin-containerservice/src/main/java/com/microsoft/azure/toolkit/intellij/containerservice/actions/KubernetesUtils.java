/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.containerservice.actions;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.updateSettings.impl.pluginsAdvertisement.PluginsAdvertiser;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.util.PlatformUtils;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class KubernetesUtils {
    private static final String KUBERNETES_PLUGIN_ID = "com.intellij.kubernetes";
    private static final String REDHAT_KUBERNETES_PLUGIN_ID = "com.redhat.devtools.intellij.kubernetes";
    private static final Map<String, String> KUBERNETES_TERMINAL_MAP = new LinkedHashMap<>() {{
        put(KUBERNETES_PLUGIN_ID, "Services");
        put(REDHAT_KUBERNETES_PLUGIN_ID, "Kubernetes");
    }};

    public static Action<?> getKubernetesConnectActions(@Nonnull final Project project) {
        // if kubernetes plugin installed, return open view action
        final String installedPluginId = getInstalledKubernetesPlugin();
        if (StringUtils.isNoneBlank(installedPluginId)) {
            return getOpenKubernetesPluginAction(project, installedPluginId);
        } else {
            return getRecommendKubernetesPluginAction(project);
        }
    }

    private static Action<?> getOpenKubernetesPluginAction(Project project, String installedPluginId) {
        final Consumer<Void> consumer = ignore -> openKubernetesPluginToolWindow(project, installedPluginId);
        final ActionView.Builder view = new ActionView.Builder("Open kubernetes plugin")
                .title(ignore -> AzureString.fromString("Open IntelliJ kubernetes plugin")).enabled(ignore -> true);
        final Action.Id<Void> id = Action.Id.of("kubernetes.open_kubernetes_plugin");
        return new Action<>(id, consumer, view);
    }

    private static void openKubernetesPluginToolWindow(Project project, String pluginId) {
        final String windowId = KUBERNETES_TERMINAL_MAP.get(pluginId);
        final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(windowId);
        if (toolWindow == null) {
            AzureMessager.getMessager().warning(AzureString.format("Failed to get tool window of kubernetes plugin, please check whether plugin %s is enabled", pluginId));
        } else {
            AzureTaskManager.getInstance().runLater(toolWindow::show);
        }
    }

    private static Action getRecommendKubernetesPluginAction(Project project) {
        final String recommendPlugin = PlatformUtils.isIdeaUltimate() ? KUBERNETES_PLUGIN_ID : REDHAT_KUBERNETES_PLUGIN_ID;
        final PluginId recommendPluginId = PluginId.getId(recommendPlugin);
        final Consumer<Void> consumer = ignore -> AzureTaskManager.getInstance().runLater(() ->
                PluginsAdvertiser.installAndEnable(project, Set.of(recommendPluginId), true, () -> {
                }));
        final ActionView.Builder view = new ActionView.Builder("Install kubernetes plugin")
                .title(ignore -> AzureString.fromString("Install kubernetes plugin")).enabled(ignore -> true);
        final Action.Id<Void> id = Action.Id.of("kubernetes.install_kubernetes_plugin");
        return new Action<>(id, consumer, view);
    }

    @Nullable
    private static String getInstalledKubernetesPlugin() {
        return KUBERNETES_TERMINAL_MAP.keySet().stream()
                .filter(id -> PluginManagerCore.isPluginInstalled(PluginId.getId(id)))
                .findFirst().orElse(null);
    }

}
