/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.applicationinsights.creation;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.AzureDialog;
import com.microsoft.azure.toolkit.intellij.common.component.RegionComboBox;
import com.microsoft.azure.toolkit.intellij.common.component.SubscriptionComboBox;
import com.microsoft.azure.toolkit.intellij.common.component.resourcegroup.ResourceGroupComboBox;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.applicationinsights.ApplicationInsightDraft;
import com.microsoft.azure.toolkit.lib.applicationinsights.AzureApplicationInsights;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.common.model.AzResource;
import com.microsoft.azure.toolkit.lib.common.model.Region;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.resource.ResourceGroup;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.List;

public class ApplicationInsightsCreationDialog extends AzureDialog<ApplicationInsightDraft> implements AzureForm<ApplicationInsightDraft> {
    private static final String DIALOG_TITLE = "Create Application Insight";

    private JPanel pnlRoot;
    private SubscriptionComboBox subscriptionComboBox;
    private ResourceGroupComboBox resourceGroupComboBox;
    private RegionComboBox regionComboBox;
    private InsightNameTextField txtName;

    public ApplicationInsightsCreationDialog(Project project) {
        super(project);
        init();
        initListeners();
    }

    @Override
    public AzureForm<ApplicationInsightDraft> getForm() {
        return this;
    }

    @Override
    protected String getDialogTitle() {
        return DIALOG_TITLE;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return pnlRoot;
    }

    @Override
    public ApplicationInsightDraft getValue() {
        final Subscription subscription = subscriptionComboBox.getValue();
        final ResourceGroup resourceGroup = resourceGroupComboBox.getValue();
        final String name = txtName.getValue();
        final Region region = regionComboBox.getValue();
        final ApplicationInsightDraft result =
                Azure.az(AzureApplicationInsights.class).forSubscription(subscription.getId()).applicationInsights().create(name, resourceGroup.getName());
        result.setRegion(region);
        return result;
    }

    @Override
    public void setValue(ApplicationInsightDraft data) {
        final String resourceName = StringUtils.equals(data.getName(), AzResource.NONE.getName()) ? StringUtils.EMPTY : data.getName();
        this.txtName.setValue(resourceName);
        this.subscriptionComboBox.setValue(data.getSubscription());
        // todo: @hanli support draft resource group
        this.resourceGroupComboBox.setValue(data.getResourceGroup());
        this.regionComboBox.setValue(data.getRegion());
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        return Arrays.asList(subscriptionComboBox, resourceGroupComboBox, txtName, regionComboBox);
    }

    protected void initListeners() {
        this.subscriptionComboBox.addItemListener(this::onSubscriptionChanged);
    }

    private void onSubscriptionChanged(final ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            final Subscription subscription = (Subscription) e.getItem();
            this.resourceGroupComboBox.setSubscription(subscription);
            this.regionComboBox.setSubscription(subscription);
            this.txtName.setSubscriptionId(subscription.getId());
        } else if (e.getStateChange() == ItemEvent.DESELECTED) {
            this.resourceGroupComboBox.setSubscription(null);
            this.regionComboBox.setSubscription(null);
        }
    }

    private void createUIComponents() {
        this.txtName = new InsightNameTextField();
        // todo: @hanli get support region from library
    }
}
