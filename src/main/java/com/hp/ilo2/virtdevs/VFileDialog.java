package com.hp.ilo2.virtdevs;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


public class VFileDialog extends Frame implements WindowListener {
    FileDialog fd;

    public VFileDialog(String str, String str2) {
        try {
            addWindowListener(this);
            this.fd = new FileDialog(new Frame(), str);
            if (str2 != null) {
                this.fd.setFile(str2);
            }
            this.fd.setVisible(true);
            this.fd.setFocusable(true);
        } catch (Exception e) {
            System.out.println("Un able to open virtual drive select");
        }
    }

    public String getString() {
        String str = null;
        if (!(this.fd.getDirectory() == null || this.fd.getFile() == null)) {
            str = new StringBuffer().append(this.fd.getDirectory()).append(this.fd.getFile()).toString();
        }
        return str;
    }

    public void windowClosing(WindowEvent windowEvent) {
        setVisible(false);
    }

    public void windowActivated(WindowEvent windowEvent) {
    }

    public void windowClosed(WindowEvent windowEvent) {
    }

    public void windowDeactivated(WindowEvent windowEvent) {
    }

    public void windowDeiconified(WindowEvent windowEvent) {
    }

    public void windowIconified(WindowEvent windowEvent) {
    }

    public void windowOpened(WindowEvent windowEvent) {
    }
}
