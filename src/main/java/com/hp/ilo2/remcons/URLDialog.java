package com.hp.ilo2.remcons;

import com.hp.ilo2.intgapp.locinfo;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.*;

public class URLDialog extends JDialog implements ActionListener, WindowListener {

    JButton cancel;
    JButton ok;
    JLabel txt1;
    JPanel mainPanel;
    JScrollPane scroller;
    JTextArea txt3;
    String url;
    boolean rc;
    remcons remconsObj;

    public URLDialog(remcons remconsVar) {
        super(null == remconsVar.ParentApp.dispFrame ? new JFrame() : remconsVar.ParentApp.dispFrame, locinfo.DIALOGSTR_2062, true);

        remconsObj = remconsVar;

        ui_init();
    }

    protected void ui_init() {
        txt1 = new JLabel(locinfo.DIALOGSTR_2063 + "\n\n\n");
        txt3 = new JTextArea(1, 40);
        txt3.setEditable(true);

        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());

        ok = new JButton(locinfo.STATUSSTR_3121);
        ok.addActionListener(this);

        cancel = new JButton(locinfo.STATUSSTR_3115);
        cancel.addActionListener(this);

        setBackground(Color.lightGray);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        mainPanel.add(txt1, gridBagConstraints);

        scroller = new JScrollPane(txt3, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;

        mainPanel.add(scroller, gridBagConstraints);
        mainPanel.setPreferredSize(mainPanel.getPreferredSize());

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        jPanel.add(cancel);
        jPanel.add(ok);

        setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.fill = 2;
        gridBagConstraints2.anchor = 17;
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 0;
        add(mainPanel, gridBagConstraints2);
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 1;
        add(jPanel, gridBagConstraints2);

        setSize(mainPanel.getPreferredSize().width + 40, mainPanel.getPreferredSize().height + 100);
        addWindowListener(this);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == ok) {
            url = txt3.getText();

            dispose();
        } else if (actionEvent.getSource() == cancel) {
            url = "userhitcancel";

            dispose();
        }
    }

    public String getUserInput() {
        return url;
    }

    public void windowClosing(WindowEvent windowEvent) {
        url = "userhitclose";

        dispose();

        rc = false;
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
