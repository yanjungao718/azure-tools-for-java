/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.legacy.appservice;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.microsoft.azure.toolkit.ide.appservice.model.AppServiceConfig;
import com.microsoft.azure.toolkit.intellij.common.AzureComboBox;
import com.microsoft.azure.toolkit.lib.appservice.model.JavaVersion;
import com.microsoft.azure.toolkit.lib.appservice.model.OperatingSystem;
import com.microsoft.azure.toolkit.lib.common.model.ResourceGroup;
import com.microsoft.azure.toolkit.lib.legacy.webapp.WebAppService;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import rx.Subscription;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AppServiceComboBox<T extends AppServiceConfig> extends AzureComboBox<T> {

    protected Project project;
    protected Subscription subscription;

    @Setter
    protected T configModel;

    public AppServiceComboBox(final Project project) {
        super(false);
        this.project = project;
        this.setRenderer(new AppComboBoxRender());
    }

    @Nonnull
    @Override
    protected List<? extends T> loadItems() throws Exception {
        final List<T> items = loadAppServiceModels();
        if (isDraftResource(configModel)) {
            final boolean exist = items.stream().anyMatch(item -> AppServiceConfig.isSameApp(item, configModel));
            if (!exist) {
                items.add(configModel);
            }
        }
        return items;
    }

    @Override
    public T getValue() {
        if (value instanceof ItemReference && ((ItemReference<?>) value).is(configModel)) {
            return configModel;
        }
        return super.getValue();
    }

    protected abstract List<T> loadAppServiceModels() throws Exception;

    @Nonnull
    @Override
    protected ExtendableTextComponent.Extension getExtension() {
        return ExtendableTextComponent.Extension.create(AllIcons.General.Add, "Create", this::createResource);
    }

    @Override
    protected String getItemText(final Object item) {
        if (item instanceof AppServiceConfig) {
            final AppServiceConfig selectedItem = (AppServiceConfig) item;
            return isDraftResource(selectedItem) ? String.format("(New) %s", selectedItem.getName()) : selectedItem.getName();
        } else {
            return Objects.toString(item, StringUtils.EMPTY);
        }
    }

    protected abstract void createResource();

    public static class AppComboBoxRender extends SimpleListCellRenderer {

        @Override
        public void customize(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof AppServiceConfig) {
                final AppServiceConfig app = (AppServiceConfig) value;
                final boolean isJavaApp = app.getRuntime() != null && !Objects.equals(app.getRuntime().getJavaVersion(), JavaVersion.OFF) &&
                        app.getRuntime().getOperatingSystem() != OperatingSystem.DOCKER;
                setEnabled(isJavaApp);
                setFocusable(isJavaApp);

                if (index >= 0) {
                    setText(getAppServiceLabel(app));
                } else {
                    setText(app.getName());
                }
                this.repaint();
            }
        }

        private String getAppServiceLabel(AppServiceConfig appServiceModel) {
            final String appServiceName = isDraftResource(appServiceModel) ?
                    String.format("(New) %s", appServiceModel.getName()) : appServiceModel.getName();
            final String runtime = appServiceModel.getRuntime() == null ?
                    "Loading:" : WebAppService.getInstance().getRuntimeDisplayName(appServiceModel.getRuntime());
            final String resourceGroup = Optional.ofNullable(appServiceModel.getResourceGroup()).map(ResourceGroup::getName).orElse(StringUtils.EMPTY);
            return String.format("<html><div>%s</div></div><small>Runtime: %s | Resource Group: %s</small></html>",
                    appServiceName, runtime, resourceGroup);
        }
    }

    private static boolean isDraftResource(final AppServiceConfig config) {
        return config != null && StringUtils.isEmpty(config.getResourceId());
    }
}
