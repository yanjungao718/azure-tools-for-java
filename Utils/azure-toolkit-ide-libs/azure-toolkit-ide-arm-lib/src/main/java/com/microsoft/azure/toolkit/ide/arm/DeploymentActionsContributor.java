/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.ide.arm;

import com.microsoft.azure.toolkit.ide.common.IActionsContributor;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.ActionGroup;
import com.microsoft.azure.toolkit.lib.common.action.ActionView;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.action.IActionGroup;
import com.microsoft.azure.toolkit.lib.resource.ResourceDeployment;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroup;

import java.util.Optional;

import static com.microsoft.azure.toolkit.lib.common.operation.AzureOperationBundle.title;

public class DeploymentActionsContributor implements IActionsContributor {
    public static final int INITIALIZE_ORDER = ResourceCommonActionsContributor.INITIALIZE_ORDER + 1;

    public static final String DEPLOYMENT_ACTIONS = "actions.resourceDeployments.deployment";
    public static final String DEPLOYMENTS_ACTIONS = "actions.resourceDeployments.deployments";

    public static final Action.Id<ResourceDeployment> EDIT = Action.Id.of("action.resourceDeployment.edit");
    public static final Action.Id<ResourceDeployment> UPDATE = Action.Id.of("action.resourceDeployment.update");
    public static final Action.Id<ResourceDeployment> EXPORT_TEMPLATE = Action.Id.of("action.resourceDeployment.export_template");
    public static final Action.Id<ResourceDeployment> EXPORT_PARAMETER = Action.Id.of("action.resourceDeployment.export_parameter");
    public static final Action.Id<ResourceGroup> GROUP_CREATE_DEPLOYMENT = Action.Id.of("action.arm.create_deployment.group");

    @Override
    public void registerActions(AzureActionManager am) {
        final ActionView.Builder editDeployment = new ActionView.Builder("Edit Deployment", "/icons/action/edit")
            .title(s -> Optional.ofNullable(s).map(r -> title("arm.edit_deployment.deployment", ((ResourceDeployment) r).getName())).orElse(null))
            .enabled(s -> s instanceof ResourceDeployment && ((ResourceDeployment) s).getFormalStatus().isWritable());
        final ActionView.Builder updateDeployment = new ActionView.Builder("Update Deployment", "/icons/action/update")
            .title(s -> Optional.ofNullable(s).map(r -> title("arm.update_deployment.deployment", ((ResourceDeployment) r).getName())).orElse(null))
            .enabled(s -> s instanceof ResourceDeployment && ((ResourceDeployment) s).getFormalStatus().isWritable());
        final ActionView.Builder exportTemplate = new ActionView.Builder("Export Template File", "/icons/action/export")
            .title(s -> Optional.ofNullable(s).map(r -> title("arm.export_template.deployment", ((ResourceDeployment) r).getName())).orElse(null))
            .enabled(s -> s instanceof ResourceDeployment && ((ResourceDeployment) s).getFormalStatus().isConnected());
        final ActionView.Builder exportParameter = new ActionView.Builder("Export Parameter File", "/icons/action/export")
            .title(s -> Optional.ofNullable(s).map(r -> title("arm.export_parameter.deployment", ((ResourceDeployment) r).getName())).orElse(null))
            .enabled(s -> s instanceof ResourceDeployment && ((ResourceDeployment) s).getFormalStatus().isConnected());
        final Action<ResourceDeployment> editAction = new Action<>(editDeployment);
        final Action<ResourceDeployment> exportTemplateAction = new Action<>(exportTemplate);
        final Action<ResourceDeployment> exportParameterAction = new Action<>(exportParameter);
        editAction.setShortcuts(am.getIDEDefaultShortcuts().view());
        exportTemplateAction.setShortcuts("control alt E");
        am.registerAction(EDIT, editAction);
        am.registerAction(UPDATE, new Action<>(updateDeployment));
        am.registerAction(EXPORT_TEMPLATE, exportTemplateAction);
        am.registerAction(EXPORT_PARAMETER, exportParameterAction);

        final ActionView.Builder createDeploymentView = new ActionView.Builder("Deployment")
            .title(s -> Optional.ofNullable(s).map(r -> title("arm.create_deployment.group", ((ResourceGroup) r).getName())).orElse(null))
            .enabled(s -> s instanceof ResourceGroup);
        am.registerAction(GROUP_CREATE_DEPLOYMENT, new Action<>(createDeploymentView));
    }

    @Override
    public void registerGroups(AzureActionManager am) {
        final ActionGroup deploymentsActions = new ActionGroup(
            ResourceCommonActionsContributor.REFRESH,
            "---",
            ResourceCommonActionsContributor.CREATE
        );
        am.registerGroup(DEPLOYMENTS_ACTIONS, deploymentsActions);

        final ActionGroup deploymentActions = new ActionGroup(
            ResourceCommonActionsContributor.PIN,
            "---",
            ResourceCommonActionsContributor.REFRESH,
            ResourceCommonActionsContributor.OPEN_PORTAL_URL,
            ResourceCommonActionsContributor.SHOW_PROPERTIES,
            "---",
            DeploymentActionsContributor.EDIT,
            DeploymentActionsContributor.UPDATE,
            ResourceCommonActionsContributor.DELETE,
            "---",
            DeploymentActionsContributor.EXPORT_TEMPLATE,
            DeploymentActionsContributor.EXPORT_PARAMETER
        );
        am.registerGroup(DEPLOYMENT_ACTIONS, deploymentActions);

        final IActionGroup group = am.getGroup(ResourceCommonActionsContributor.RESOURCE_GROUP_CREATE_ACTIONS);
        group.addAction(GROUP_CREATE_DEPLOYMENT);
    }

    @Override
    public int getOrder() {
        return INITIALIZE_ORDER;
    }
}
