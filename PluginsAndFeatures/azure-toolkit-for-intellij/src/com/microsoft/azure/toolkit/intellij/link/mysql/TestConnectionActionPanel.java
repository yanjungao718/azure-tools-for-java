/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.link.mysql;

import lombok.Getter;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

@Getter
public class TestConnectionActionPanel extends JPanel {
    private JButton copyButton;
    private JLabel iconLabel;
    private JPanel root;

    private Border rolloverBorder;
    private Border emptyBorder;

    public TestConnectionActionPanel() {
        super();
        rolloverBorder = copyButton.getBorder();
        emptyBorder = BorderFactory.createEmptyBorder();
        copyButton.setBorder(emptyBorder);
        copyButton.setContentAreaFilled(false);
        copyButton.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {
                if (copyButton.isRolloverEnabled()) {
                    copyButton.setBorder(rolloverBorder);
                    copyButton.setContentAreaFilled(true);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (copyButton.isRolloverEnabled()) {
                    copyButton.setBorder(emptyBorder);
                    copyButton.setContentAreaFilled(false);
                }
            }
        });
    }

    @Override
    public void setVisible(boolean visible) {
        root.setVisible(visible);
    }
}
