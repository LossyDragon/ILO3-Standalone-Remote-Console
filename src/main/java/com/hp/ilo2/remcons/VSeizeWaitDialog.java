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

public class VSeizeWaitDialog extends JDialog implements ActionListener {
    
    JButton cancel;
    JButton seize;
    JLabel txt;
    JPanel mainPanel;
    String saddr;
    String susr;
    boolean disp;
    byte userInput;
    int sflag;
    private Timer szWaitTimer;
    private final int szWaitTimerTick;
    remcons remconsObj;

    public VSeizeWaitDialog(remcons remconsVar, String str, String str2, int i) {
        super(null == remconsVar.ParentApp.dispFrame ?
                new JFrame() : remconsVar.ParentApp.dispFrame, locinfo.STATUSSTR_3112, true);

        remconsObj = remconsVar;
        saddr = str2;
        sflag = i;
        susr = str;
        szWaitTimerTick = 1000;

        ui_init();
    }

    protected void ui_init() {
        txt = new JLabel("<html>" + locinfo.DIALOGSTR_2048 + " " + susr + " " + locinfo.DIALOGSTR_2049 + " " + saddr + " " + locinfo.DIALOGSTR_205a + "<br><br>" + locinfo.DIALOGSTR_205b + sflag + locinfo.DIALOGSTR_205c + "</html>");

        mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEtchedBorder(0));
        mainPanel.add(txt);
        mainPanel.setPreferredSize(mainPanel.getPreferredSize());

        seize = new JButton(locinfo.DIALOGSTR_205d);
        seize.addActionListener(this);

        cancel = new JButton(locinfo.DIALOGSTR_205e);
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

        szWaitTimer = new Timer(szWaitTimerTick, false, remconsObj);
        szWaitTimer.setListener(new szWaitTimerListener(this), this);
        szWaitTimer.start();

        System.out.println("seize wait timer started...");

        setSize(mainPanel.getPreferredSize().width + 40, mainPanel.getPreferredSize().height + 100);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == seize) {
            userInput = (byte) 0;

            dispose();
            stop_szWaitTimer();

            disp = true;
        } else if (actionEvent.getSource() == cancel) {
            userInput = (byte) 2;

            dispose();
            stop_szWaitTimer();

            disp = true;
        }
    }

    public byte getUserInput() {
        return userInput;
    }

    private void stop_szWaitTimer() {
        if (szWaitTimer != null) {
            szWaitTimer.stop();
            szWaitTimer = null;
        }
    }

    public static class szWaitTimerListener implements TimerListener {
        private final VSeizeWaitDialog waitDialog;

        szWaitTimerListener(VSeizeWaitDialog vSeizeWaitDialog) {
            waitDialog = vSeizeWaitDialog;
        }

        @Override
        public synchronized void timeout(Object obj) {
            VSeizeWaitDialog vSeizeWaitDialog = (VSeizeWaitDialog) obj;
            vSeizeWaitDialog.sflag--;

            if (vSeizeWaitDialog.sflag > 0) {
                waitDialog.txt.setText("<html>" + locinfo.DIALOGSTR_2048 + " " + vSeizeWaitDialog.susr + " " + locinfo.DIALOGSTR_2049 + " " + vSeizeWaitDialog.saddr + " " + locinfo.DIALOGSTR_205a + "<br><br>" + locinfo.DIALOGSTR_205b + vSeizeWaitDialog.sflag + " " + locinfo.DIALOGSTR_205c + "</html>");
            } else {
                vSeizeWaitDialog.actionPerformed(new ActionEvent(vSeizeWaitDialog.seize, 1, "vobjyes"));
            }
        }
    }
}
