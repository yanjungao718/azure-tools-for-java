/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
package com.microsoft.azure.toolkit.intellij.vm.creation.component;

import com.microsoft.azure.toolkit.lib.common.form.AzureForm;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.compute.security.model.SecurityRule;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class InboundPortRulesForm extends JPanel implements AzureForm<List<SecurityRule>> {
    private JLabel lblPublicInboundPorts;
    private JPanel pnlPublicInboundsRadios;
    private JRadioButton rdoAllowSelectedInboundPorts;
    private JRadioButton rdoNoneInboundPorts;
    private JLabel lblSelectInboundPorts;
    private JPanel pnlInboundPorts;
    private JCheckBox chkHTTP;
    private JCheckBox chkHTTPS;
    private JCheckBox chkSSH;
    private JCheckBox chkRDP;
    private JPanel pnlRoot;

    public InboundPortRulesForm() {
        super();
        $$$setupUI$$$();
        init();
    }

    @Override
    public List<SecurityRule> getData() {
        if (rdoNoneInboundPorts.isSelected()) {
            return null;
        }
        final List<SecurityRule> policies = new ArrayList<>();
        Optional.ofNullable(chkHTTP.isSelected() ? SecurityRule.HTTP_RULE : null).ifPresent(policies::add);
        Optional.ofNullable(chkHTTPS.isSelected() ? SecurityRule.HTTPS_RULE : null).ifPresent(policies::add);
        Optional.ofNullable(chkSSH.isSelected() ? SecurityRule.SSH_RULE : null).ifPresent(policies::add);
        Optional.ofNullable(chkRDP.isSelected() ? SecurityRule.RDP_RULE : null).ifPresent(policies::add);
        return policies;
    }

    @Override
    public void setData(List<SecurityRule> data) {
        if (data == null) {
            rdoNoneInboundPorts.setSelected(true);
        } else {
            rdoAllowSelectedInboundPorts.setSelected(true);
            chkHTTP.setSelected(data.contains(SecurityRule.HTTP_RULE));
            chkHTTPS.setSelected(data.contains(SecurityRule.HTTPS_RULE));
            chkSSH.setSelected(data.contains(SecurityRule.SSH_RULE));
            chkRDP.setSelected(data.contains(SecurityRule.RDP_RULE));
        }
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        return Collections.emptyList();
    }

    public List<JLabel> getLabels() {
        return Arrays.asList(lblPublicInboundPorts, lblSelectInboundPorts);
    }

    public void addActionListenerToComponents(final ActionListener actionListener) {
        Stream.of(chkRDP, chkHTTP, chkHTTPS, chkSSH, rdoNoneInboundPorts, rdoAllowSelectedInboundPorts)
                .forEach(component -> component.addActionListener(actionListener));
    }

    @Override
    public void setVisible(boolean flag) {
        super.setVisible(flag);
        this.pnlRoot.setVisible(flag);
    }

    private void init() {
        final ButtonGroup inboundPortsGroup = new ButtonGroup();
        inboundPortsGroup.add(rdoNoneInboundPorts);
        inboundPortsGroup.add(rdoAllowSelectedInboundPorts);
        rdoNoneInboundPorts.addItemListener(e -> toggleInboundPortsPolicy(false));
        rdoAllowSelectedInboundPorts.addItemListener(e -> toggleInboundPortsPolicy(true));
        rdoNoneInboundPorts.setSelected(true);
    }

    private void toggleInboundPortsPolicy(boolean allowInboundPorts) {
        Stream.of(chkRDP, chkHTTP, chkHTTPS, chkSSH).forEach(chk -> {
            chk.setEnabled(allowInboundPorts);
            if (!allowInboundPorts) {
                chk.setSelected(false);
            }
        });
    }

    // CHECKSTYLE IGNORE check FOR NEXT 1 LINES
    private void $$$setupUI$$$() {
    }
}
