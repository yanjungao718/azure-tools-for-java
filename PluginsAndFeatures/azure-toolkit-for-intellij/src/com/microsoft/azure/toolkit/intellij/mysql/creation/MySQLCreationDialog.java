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

package com.microsoft.azure.toolkit.intellij.mysql.creation;

import com.intellij.openapi.project.Project;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.toolkit.intellij.common.AzureDialog;
import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.mysql.AzureMySQLConfig;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static com.microsoft.intellij.ui.messages.AzureBundle.message;

public class MySQLCreationDialog extends AzureDialog<AzureMySQLConfig> {
    private static final String DIALOG_TITLE = "Create Azure Database for MySQL";
    private JPanel rootPanel;
    private MySQLCreationBasic basic;
    private MySQLCreationAdvanced advanced;

    private boolean advancedMode;
    private JCheckBox checkboxMode;

    public MySQLCreationDialog(@Nullable Project project) {
        super(project);
        setOKActionEnabled(false);
        init();
        extendInit();
        initListeners();
    }

    private void extendInit() {
        advanced.setVisible(false);
        this.startTrackingValidation();
    }

    private void initListeners() {
        advanced.getSubscriptionComboBox().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Subscription) {
                    final Subscription subscription = (Subscription) e.getItem();
                    basic.getServerNameTextField().setSubscription(subscription);
                }
            }
        });
        advanced.getResourceGroupComboBox().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof ResourceGroup) {
                    final ResourceGroup resourceGroup = (ResourceGroup) e.getItem();
                    basic.getServerNameTextField().setResourceGroup(resourceGroup);
                }
            }
        });
    }

    @Override
    public AzureForm<AzureMySQLConfig> getForm() {
        return this.advancedMode ? advanced : basic;
    }

    @Override
    protected String getDialogTitle() {
        return DIALOG_TITLE;
    }

    @Override
    protected JComponent createDoNotAskCheckbox() {
        this.checkboxMode = new JCheckBox(message("common.moreSetting"));
        this.checkboxMode.setVisible(true);
        this.checkboxMode.setSelected(false);
        this.checkboxMode.addActionListener(e -> this.toggleAdvancedMode(this.checkboxMode.isSelected()));
        return this.checkboxMode;
    }

    protected void toggleAdvancedMode(boolean advancedMode) {
        this.advancedMode = advancedMode;
        if (advancedMode) {
            advanced.setData(basic.getData());
        } else {
            basic.setData(advanced.getData());
        }
        advanced.setVisible(advancedMode);
        basic.setVisible(!advancedMode);
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return advancedMode ? advanced.getServerNameTextField() : basic.getServerNameTextField();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        rootPanel.setPreferredSize(new Dimension(420, 168));
        return rootPanel;
    }

    private void createUIComponents() {
        AzureMySQLConfig config = AzureMySQLConfig.getDefaultAzureMySQLConfig();
        basic = new MySQLCreationBasic(config);
        advanced = new MySQLCreationAdvanced(config);
    }
}
