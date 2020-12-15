package com.microsoft.intellij.helpers.mysql;

import lombok.Getter;

import javax.swing.*;

public class MySQLPropertyActionPanel extends JPanel {
    @Getter
    private JButton saveButton;
    @Getter
    private JButton discardButton;
    private JPanel rootPanel;

}
