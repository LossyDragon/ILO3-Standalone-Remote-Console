package com.hp.ilo2.remcons;

import com.hp.ilo2.intgapp.locinfo;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class URLDialog extends JDialog implements ActionListener, WindowListener {
    JPanel mainPanel;
    JLabel txt1;
    JLabel txt2;
    JTextArea txt3;
    JScrollPane scroller;
    JButton ok;
    JButton cancel;
    String url;
    boolean rc;
    remcons remconsObj;

    public String getLocalString(int i) {
        String str = "";
        try {
            str = this.remconsObj.ParentApp.locinfoObj.getLocString(i);
        } catch (Exception e) {
            System.out.println(new StringBuffer().append("VSeizeDialog:getLocalString").append(e.getMessage()).toString());
        }
        return str;
    }

    public URLDialog(remcons remconsVar) {
        super(null == remconsVar.ParentApp.dispFrame ? new JFrame() : remconsVar.ParentApp.dispFrame, remconsVar.getLocalString(locinfo.DIALOGSTR_2062), true);
        this.remconsObj = remconsVar;
        ui_init();
    }

    protected void ui_init() {
        this.txt1 = new JLabel(new StringBuffer().append(getLocalString(locinfo.DIALOGSTR_2063)).append("\n\n\n").toString());
        this.txt3 = new JTextArea(1, 40);
        this.txt3.setEditable(true);
        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(new GridBagLayout());
        this.ok = new JButton(getLocalString(locinfo.STATUSSTR_3121));
        this.ok.addActionListener(this);
        this.cancel = new JButton(getLocalString(locinfo.STATUSSTR_3115));
        this.cancel.addActionListener(this);
        setBackground(Color.lightGray);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        this.mainPanel.add(this.txt1, gridBagConstraints);
        this.scroller = new JScrollPane(this.txt3, 21, 31);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        this.mainPanel.add(this.scroller, gridBagConstraints);
        this.mainPanel.setPreferredSize(this.mainPanel.getPreferredSize());
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout(2));
        jPanel.add(this.cancel);
        jPanel.add(this.ok);
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
        setLocationRelativeTo((Component) null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == this.ok) {
            this.url = this.txt3.getText();
            dispose();
        } else if (actionEvent.getSource() == this.cancel) {
            this.url = "userhitcancel";
            dispose();
        }
    }

    public String getUserInput() {
        return this.url;
    }

    public void windowClosing(WindowEvent windowEvent) {
        this.url = "userhitclose";
        dispose();
        this.rc = false;
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
