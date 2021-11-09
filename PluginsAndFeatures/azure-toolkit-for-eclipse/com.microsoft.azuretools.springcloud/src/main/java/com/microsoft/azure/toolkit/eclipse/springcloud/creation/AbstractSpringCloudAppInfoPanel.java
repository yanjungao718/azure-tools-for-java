/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.eclipse.springcloud.creation;

import com.microsoft.azure.toolkit.eclipse.common.component.AzureComboBox.ItemReference;
import com.microsoft.azure.toolkit.eclipse.common.component.AzureTextInput;
import com.microsoft.azure.toolkit.eclipse.common.component.SubscriptionComboBox;
import com.microsoft.azure.toolkit.eclipse.common.form.AzureFormPanel;
import com.microsoft.azure.toolkit.eclipse.springcloud.component.SpringCloudClusterComboBox;
import com.microsoft.azure.toolkit.lib.common.entity.IAzureResource;
import com.microsoft.azure.toolkit.lib.common.form.AzureValidationInfo;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessageBundle;
import com.microsoft.azure.toolkit.lib.common.model.Subscription;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudApp;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudAppEntity;
import com.microsoft.azure.toolkit.lib.springcloud.SpringCloudCluster;
import com.microsoft.azure.toolkit.lib.springcloud.config.SpringCloudAppConfig;
import com.microsoft.azure.toolkit.lib.springcloud.config.SpringCloudDeploymentConfig;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Getter(AccessLevel.PROTECTED)
public abstract class AbstractSpringCloudAppInfoPanel extends Composite implements AzureFormPanel<SpringCloudAppConfig> {
    private static final String SPRING_CLOUD_APP_NAME_PATTERN = "^[a-z][a-z0-9-]{2,30}[a-z0-9]$";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    @Nullable
    private final SpringCloudCluster cluster;
    private final String defaultAppName;
    private SpringCloudAppConfig originalConfig;

    public AbstractSpringCloudAppInfoPanel(Composite parent, @Nullable final SpringCloudCluster cluster) {
        super(parent, SWT.NONE);
        this.cluster = cluster;
        this.defaultAppName = String.format("springcloud-app-%s", DATE_FORMAT.format(new Date()));
    }

    protected void init() {
        final SubscriptionComboBox selectorSubscription = this.getSelectorSubscription();
        final SpringCloudClusterComboBox selectorCluster = this.getSelectorCluster();
        final AzureTextInput textName = this.getTextName();
        selectorSubscription.setRequired(true);
        selectorSubscription.addValueChangedListener(this::onSubscriptionChanged);
        selectorCluster.setRequired(true);
        selectorCluster.addValueChangedListener(this::onClusterChanged);
        textName.setRequired(true);
        textName.setValue(this.defaultAppName);
        textName.setValidator(() -> {
            final String name = textName.getValue();
            if (!name.matches(SPRING_CLOUD_APP_NAME_PATTERN)) {
                return AzureValidationInfo.error(AzureMessageBundle.message("springcloud.app.name.validate.invalid").toString(), textName);
            } else if (Objects.nonNull(cluster) && cluster.app(name).exists()) {
                return AzureValidationInfo.error(AzureMessageBundle.message("springcloud.app.name.validate.exist", name).toString(), textName);
            }
            return AzureValidationInfo.success(textName);
        });
        if (Objects.nonNull(this.cluster)) {
            selectorSubscription.setValue(new ItemReference<>(this.cluster.subscriptionId(), Subscription::getId));
            selectorCluster.setValue(new ItemReference<>(this.cluster.name(), IAzureResource::name));
        }
    }

    private void onSubscriptionChanged(@Nullable final Subscription subscription) {
        this.getSelectorCluster().setSubscription(subscription);
    }

    private void onClusterChanged(@Nullable final SpringCloudCluster c) {
        final String appName = StringUtils.firstNonBlank(this.getTextName().getValue(), this.defaultAppName);
        if (Objects.nonNull(c)) {
            final SpringCloudApp app = c.app(new SpringCloudAppEntity(appName, c.entity()));
            this.onAppChanged(app);
        }
    }

    protected void onAppChanged(SpringCloudApp app) {
        if (Objects.isNull(this.originalConfig)) {
            AzureTaskManager.getInstance().runOnPooledThread(() -> {
                this.originalConfig = SpringCloudAppConfig.fromApp(app);
                AzureTaskManager.getInstance().runLater(() -> this.setValue(this.originalConfig));
            });
        }
    }

    protected SpringCloudAppConfig getValue(SpringCloudAppConfig config) {
        config.setSubscriptionId(Optional.ofNullable(this.getSelectorSubscription().getValue()).map(Subscription::getId).orElse(null));
        config.setClusterName(Optional.ofNullable(this.getSelectorCluster().getValue()).map(IAzureResource::name).orElse(null));
        config.setAppName(this.getTextName().getValue());
        return config;
    }

    public SpringCloudAppConfig getValue() {
        final SpringCloudAppConfig config = Optional.ofNullable(this.originalConfig)
            .orElse(SpringCloudAppConfig.builder().deployment(SpringCloudDeploymentConfig.builder().build()).build());
        return getValue(config);
    }

    @Override
    public synchronized void setValue(final SpringCloudAppConfig config) {
        final Integer count = config.getDeployment().getInstanceCount();
        config.getDeployment().setInstanceCount(Objects.isNull(count) || count == 0 ? 1 : count);
        this.originalConfig = config;
        this.getTextName().setValue(config.getAppName());
        if (Objects.nonNull(config.getClusterName())) {
            this.getSelectorCluster().setValue(new ItemReference<>(config.getClusterName(), IAzureResource::name));
        }
        if (Objects.nonNull(config.getSubscriptionId())) {
            this.getSelectorSubscription().setValue(new ItemReference<>(config.getSubscriptionId(), Subscription::getId));
        }
    }

    @Override
    public void setVisible(final boolean visible) {
        super.setVisible(visible);
    }

    protected abstract SubscriptionComboBox getSelectorSubscription();

    protected abstract SpringCloudClusterComboBox getSelectorCluster();

    protected abstract AzureTextInput getTextName();
}
