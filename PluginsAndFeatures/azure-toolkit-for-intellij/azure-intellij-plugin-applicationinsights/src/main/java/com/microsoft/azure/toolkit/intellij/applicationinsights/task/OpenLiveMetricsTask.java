package com.microsoft.azure.toolkit.intellij.applicationinsights.task;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.toolkit.ide.guidance.ComponentContext;
import com.microsoft.azure.toolkit.ide.guidance.GuidanceTask;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.applicationinsights.ApplicationInsight;
import com.microsoft.azure.toolkit.lib.applicationinsights.AzureApplicationInsights;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;

import javax.annotation.Nonnull;

import static com.microsoft.azure.toolkit.ide.applicationinsights.ApplicationInsightsActionsContributor.LIVE_METRICS;

public class OpenLiveMetricsTask implements GuidanceTask {

    private final ComponentContext context;

    public OpenLiveMetricsTask(@Nonnull final ComponentContext context) {
        this.context = context;
    }

    @Override
    public void execute() throws Exception {
        final String applicationInsightsId = (String) context.getParameter("applicationInsightsId");
        final ResourceId resourceId = ResourceId.fromString(applicationInsightsId);
        final ApplicationInsight applicationInsight = Azure.az(AzureApplicationInsights.class)
                .applicationInsights(resourceId.subscriptionId()).get(applicationInsightsId);
        AzureActionManager.getInstance().getAction(LIVE_METRICS).handle(applicationInsight);
    }

    @Nonnull
    @Override
    public String getName() {
        return "task.common.open_metrics";
    }
}
