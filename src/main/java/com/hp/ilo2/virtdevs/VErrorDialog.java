package com.hp.ilo2.virtdevs;

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


public class VErrorDialog extends JDialog implements ActionListener {
    JPanel mainPanel;
    JLabel txt;
    JButton ok;
    boolean disp;
    virtdevs virtdevsObj;

    public String getLocalString(int i) {
        String str = "";
        try {
            str = this.virtdevsObj.ParentApp.locinfoObj.getLocString(i);
        } catch (Exception e) {
            System.out.println(new StringBuffer().append("VSeizeDialog:getLocalString").append(e.getMessage()).toString());
        }
        return str;
    }

    public VErrorDialog(JFrame jFrame, String str) {
        super(jFrame, "Error", true);
        ui_init(str);
    }

    public VErrorDialog(JFrame jFrame, String str, String str2) {
        super(jFrame, str, true);
        ui_init(str2);
    }

    public VErrorDialog(String str, boolean z) {
        super(new JFrame(), "Error", z);
        ui_init(str);
    }

    public VErrorDialog(JFrame jFrame, String str, String str2, boolean z) {
        super(jFrame, str, z);
        ui_init(str2);
    }

    protected void ui_init(String str) {
        this.txt = new JLabel(str);
        this.mainPanel = new JPanel();
        this.mainPanel.setBorder(BorderFactory.createEtchedBorder(0));
        this.mainPanel.add(this.txt);
        this.mainPanel.setPreferredSize(this.mainPanel.getPreferredSize());
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout(2));
        this.ok = new JButton("    OK    ");
        this.ok.addActionListener(this);
        jPanel.add(this.ok);
        getRootPane().setDefaultButton(this.ok);
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        setLayout(gridBagLayout);
        gridBagConstraints.fill = 0;
        gridBagConstraints.anchor = 10;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(this.mainPanel, gridBagConstraints);
        gridBagConstraints.fill = 0;
        gridBagConstraints.anchor = 13;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        add(jPanel, gridBagConstraints);
        setSize(this.mainPanel.getPreferredSize().width + 40, this.mainPanel.getPreferredSize().height + 100);
        setResizable(false);
        setLocationRelativeTo((Component) null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == this.ok) {
            this.disp = true;
            dispose();
        }
    }

    public boolean getBoolean() {
        return this.disp;
    }

    public void append(String str) {
        this.txt.repaint();
    }
}
