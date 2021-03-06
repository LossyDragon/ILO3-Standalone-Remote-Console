package com.hp.ilo2.remcons;

import com.hp.ilo2.intgapp.locinfo;

import java.awt.Component;
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
    public static final byte SELCANCEL = 0;
    public static final byte SELSEIZE = 2;
    remcons remconsObj;
    
    public VSeizeDialog(remcons remconsVar) {
        super(null == remconsVar.ParentApp.dispFrame ? new JFrame() : remconsVar.ParentApp.dispFrame, locinfo.STATUSSTR_3112, true);
        this.remconsObj = remconsVar;
        ui_init(remconsVar.ParentApp.dispFrame);
    }

    protected void ui_init(JFrame jFrame) {
        this.txt = new JLabel(locinfo.STATUSSTR_3113);
        this.mainPanel = new JPanel();
        this.mainPanel.setBorder(BorderFactory.createEtchedBorder(0));
        this.mainPanel.add(this.txt);
        this.mainPanel.setPreferredSize(this.mainPanel.getPreferredSize());
        this.seize = new JButton(locinfo.STATUSSTR_3114);
        this.seize.addActionListener(this);
        this.cancel = new JButton(locinfo.STATUSSTR_3115);
        this.cancel.addActionListener(this);
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        setLayout(gridBagLayout);
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(this.mainPanel, gridBagConstraints);
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        jPanel.add(this.cancel);
        jPanel.add(this.seize);
        gridBagConstraints.fill = 0;
        gridBagConstraints.anchor = 13;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        add(jPanel, gridBagConstraints);
        setSize(this.mainPanel.getPreferredSize().width + 40, this.mainPanel.getPreferredSize().height + 100);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == this.seize) {
            this.userInput = (byte) 2;
            dispose();
            this.disp = true;
        } else if (actionEvent.getSource() == this.cancel) {
            this.userInput = (byte) 0;
            dispose();
            this.disp = true;
        }
    }

    public boolean disposed() {
        return this.disp;
    }

    public void append(String str) {
        this.txt.repaint();
    }

    public byte getUserInput() {
        return this.userInput;
    }
}
