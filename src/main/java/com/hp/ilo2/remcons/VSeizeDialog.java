package com.hp.ilo2.remcons;

import com.hp.ilo2.intgapp.locinfo;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class VSeizeDialog extends JDialog implements ActionListener {
    
    JButton cancel;
    JButton seize;
    JLabel txt;
    JPanel mainPanel;
    boolean disp;
    byte userInput;
    remcons remconsObj;
    
    public VSeizeDialog(remcons remconsVar) {
        super(null == remconsVar.ParentApp.dispFrame ? new JFrame() : remconsVar.ParentApp.dispFrame, locinfo.STATUSSTR_3112, true);
        remconsObj = remconsVar;
        ui_init();
    }

    protected void ui_init() {
        txt = new JLabel(locinfo.STATUSSTR_3113);

        mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEtchedBorder(0));
        mainPanel.add(txt);
        mainPanel.setPreferredSize(mainPanel.getPreferredSize());

        seize = new JButton(locinfo.STATUSSTR_3114);
        seize.addActionListener(this);

        cancel = new JButton(locinfo.STATUSSTR_3115);
        cancel.addActionListener(this);

        GridBagLayout gridBagLayout = new GridBagLayout();
        setLayout(gridBagLayout);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(mainPanel, gridBagConstraints);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        jPanel.add(cancel);
        jPanel.add(seize);

        gridBagConstraints.fill = 0;
        gridBagConstraints.anchor = 13;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        add(jPanel, gridBagConstraints);

        setSize(mainPanel.getPreferredSize().width + 40, mainPanel.getPreferredSize().height + 100);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == seize) {
            userInput = (byte) 2;

            dispose();

            disp = true;
        } else if (actionEvent.getSource() == cancel) {
            userInput = (byte) 0;

            dispose();

            disp = true;
        }
    }
    
    public byte getUserInput() {
        return userInput;
    }
}
