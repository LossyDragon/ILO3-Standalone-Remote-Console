package com.hp.ilo2.remcons;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class aboutJircDialog extends JDialog implements ActionListener, WindowListener {
    JPanel mainPanel;
    JLabel txt1;
    JLabel txt2;
    JLabel txt3;
    JButton close;
    remcons remconsObj;

    public aboutJircDialog(remcons remconsVar) {
        super(remconsVar.ParentApp.dispFrame, "About", false);
        this.remconsObj = remconsVar;
        ui_init();
    }

    protected void ui_init() {
        this.txt1 = new JLabel("Java Integrated Remote Console");
        this.txt2 = new JLabel("Version 231");
        this.txt3 = new JLabel("Copyright 2009, 2016 Hewlett Packard Enterprise Development, LP");
        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(new GridBagLayout());
        this.close = new JButton("Close");
        this.close.addActionListener(this);
        setBackground(Color.lightGray);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        this.mainPanel.add(this.txt1, gridBagConstraints);
        gridBagConstraints.gridy = 1;
        this.mainPanel.add(this.txt2, gridBagConstraints);
        gridBagConstraints.gridy = 2;
        this.mainPanel.add(this.txt3, gridBagConstraints);
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        jPanel.add(this.close);
        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.fill = 2;
        gridBagConstraints2.anchor = 17;
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 0;
        add(this.mainPanel, gridBagConstraints2);
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 1;
        add(jPanel, gridBagConstraints2);
        setSize(this.mainPanel.getPreferredSize().width + 40, this.mainPanel.getPreferredSize().height + 100);
        addWindowListener(this);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == this.close) {
            dispose();
        }
    }

    public void windowClosing(WindowEvent windowEvent) {
        dispose();
    }

    public void windowOpened(WindowEvent windowEvent) {
    }

    public void windowDeiconified(WindowEvent windowEvent) {
    }

    public void windowIconified(WindowEvent windowEvent) {
    }

    public void windowActivated(WindowEvent windowEvent) {
    }

    public void windowClosed(WindowEvent windowEvent) {
    }

    public void windowDeactivated(WindowEvent windowEvent) {
    }
}
