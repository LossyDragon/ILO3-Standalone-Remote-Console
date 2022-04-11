package com.hp.ilo2.remcons;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class OkCancelDialog extends JDialog implements ActionListener, WindowListener {
    JPanel mainPanel;
    JLabel txt;
    JButton ok;
    JButton cancel;
    boolean rc;

    public OkCancelDialog(JFrame jFrame, String str) {
        super(jFrame, "Notice!", true);
        ui_init(str);
    }

    public OkCancelDialog(String str, boolean z) {
        super(new JFrame(), "Notice!", z);
        ui_init(str);
    }

    protected void ui_init(String str) {
        this.txt = new JLabel(str);
        this.mainPanel = new JPanel();
        this.mainPanel.setBorder(BorderFactory.createEtchedBorder(0));
        this.mainPanel.add(this.txt);
        this.mainPanel.setPreferredSize(this.mainPanel.getPreferredSize());
        this.ok = new JButton("    Ok    ");
        this.ok.addActionListener(this);
        this.cancel = new JButton("Cancel");
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
        jPanel.setLayout(new FlowLayout(2));
        jPanel.add(this.ok);
        jPanel.add(this.cancel);
        gridBagConstraints.fill = 0;
        gridBagConstraints.anchor = 13;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 1;
        add(jPanel, gridBagConstraints);
        addWindowListener(this);
        setSize(this.mainPanel.getPreferredSize().width + 40, this.txt.getPreferredSize().height + 100);
        setResizable(false);
        setLocationRelativeTo((Component) null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == this.ok) {
            dispose();
            this.rc = true;
        } else if (actionEvent.getSource() == this.cancel) {
            dispose();
            this.rc = false;
        }
    }

    public boolean result() {
        return this.rc;
    }

    public void append(String str) {
        this.txt.repaint();
    }

    public void windowClosing(WindowEvent windowEvent) {
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
