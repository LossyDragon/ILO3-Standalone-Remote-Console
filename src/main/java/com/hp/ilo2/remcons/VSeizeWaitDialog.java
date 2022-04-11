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


public class VSeizeWaitDialog extends JDialog implements ActionListener {
    public static final byte SELYES = 0;
    public static final byte SELNO = 2;
    JPanel mainPanel;
    JLabel txt;
    JButton seize;
    JButton cancel;
    boolean disp;
    byte userInput;
    remcons remconsObj;
    private Timer szWaitTimer;
    private final int szWaitTimerTick;
    String susr;
    String saddr;
    int sflag;

    public VSeizeWaitDialog(remcons remconsVar, String str, String str2, int i) {
        super(null == remconsVar.ParentApp.dispFrame ? new JFrame() : remconsVar.ParentApp.dispFrame, locinfo.STATUSSTR_3112, true);
        this.szWaitTimerTick = 1000;
        this.remconsObj = remconsVar;
        this.susr = str;
        this.saddr = str2;
        this.sflag = i;
        ui_init(remconsVar.ParentApp.dispFrame);
    }

    protected void ui_init(JFrame jFrame) {
        this.txt = new JLabel("<html>" + locinfo.DIALOGSTR_2048 + " " + this.susr + " " + locinfo.DIALOGSTR_2049 + " " + this.saddr + " " + locinfo.DIALOGSTR_205a + "<br><br>" + locinfo.DIALOGSTR_205b + this.sflag + locinfo.DIALOGSTR_205c + "</html>");
        this.mainPanel = new JPanel();
        this.mainPanel.setBorder(BorderFactory.createEtchedBorder(0));
        this.mainPanel.add(this.txt);
        this.mainPanel.setPreferredSize(this.mainPanel.getPreferredSize());
        this.seize = new JButton(locinfo.DIALOGSTR_205d);
        this.seize.addActionListener(this);
        this.cancel = new JButton(locinfo.DIALOGSTR_205e);
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
        this.szWaitTimer = new Timer(this.szWaitTimerTick, false, this.remconsObj);
        this.szWaitTimer.setListener(new szWaitTimerListener(this), this);
        this.szWaitTimer.start();
        System.out.println("seize wait timer started...");
        setSize(this.mainPanel.getPreferredSize().width + 40, this.mainPanel.getPreferredSize().height + 100);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == this.seize) {
            this.userInput = (byte) 0;
            dispose();
            stop_szWaitTimer();
            this.disp = true;
        } else if (actionEvent.getSource() == this.cancel) {
            this.userInput = (byte) 2;
            dispose();
            stop_szWaitTimer();
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

    private void stop_szWaitTimer() {
        if (this.szWaitTimer != null) {
            this.szWaitTimer.stop();
            this.szWaitTimer = null;
        }
    }


    public static class szWaitTimerListener implements TimerListener {
        private final VSeizeWaitDialog this$0;

        szWaitTimerListener(VSeizeWaitDialog vSeizeWaitDialog) {
            this.this$0 = vSeizeWaitDialog;
        }

        @Override // com.hp.ilo2.remcons.TimerListener
        public synchronized void timeout(Object obj) {
            VSeizeWaitDialog vSeizeWaitDialog = (VSeizeWaitDialog) obj;
            vSeizeWaitDialog.sflag--;
            if (vSeizeWaitDialog.sflag > 0) {
                this.this$0.txt.setText("<html>" + locinfo.DIALOGSTR_2048 + " " + vSeizeWaitDialog.susr + " " + locinfo.DIALOGSTR_2049 + " " + vSeizeWaitDialog.saddr + " " + locinfo.DIALOGSTR_205a + "<br><br>" + locinfo.DIALOGSTR_205b + vSeizeWaitDialog.sflag + " " + locinfo.DIALOGSTR_205c + "</html>");
            } else {
                vSeizeWaitDialog.actionPerformed(new ActionEvent(vSeizeWaitDialog.seize, 1, "vobjyes"));
            }
        }
    }
}
