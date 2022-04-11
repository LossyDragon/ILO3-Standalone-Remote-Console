package com.hp.ilo2.remcons;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class hotKeysDialog
        extends JDialog
        implements ActionListener,
        WindowListener {
    JButton close;
    String jsonString;
    String[] ctrl = new String[]{"Ctrl-T:", "Ctrl-U:", "Ctrl-V:", "Ctrl-W:", "Ctrl-X:", "Ctrl-Y:"};
    Hashtable<String, String> map = new Hashtable<String, String>() {
        {
            this.put("0", "NONE");
            this.put("41", "ESC");
            this.put("226", "L_ALT");
            this.put("230", "R_ALT");
            this.put("225", "L_SHIFT");
            this.put("229", "R_SHIFT");
            this.put("224", "L_CTRL");
            this.put("228", "R-CTRL");
            this.put("227", "L_GUI");
            this.put("231", "R_GUI");
            this.put("73", "INS");
            this.put("76", "DEL");
            this.put("74", "HOME");
            this.put("77", "END");
            this.put("75", "PG_UP");
            this.put("78", "PG_DN");
            this.put("88", "ENTER");
            this.put("43", "TAB");
            this.put("72", "BREAK");
            this.put("42", "BACKSPACE");
            this.put("87", "NUM_PLUS");
            this.put("86", "NUM_MINUS");
            this.put("71", "SCRL_LCK");
            this.put("154", "SYS_RQ");
            this.put("70", "PRINT_SCRN");
            this.put("58", "F1");
            this.put("59", "F2");
            this.put("60", "F3");
            this.put("61", "F4");
            this.put("62", "F5");
            this.put("63", "F6");
            this.put("64", "F7");
            this.put("65", "F8");
            this.put("66", "F9");
            this.put("67", "F10");
            this.put("68", "F11");
            this.put("69", "F12");
            this.put("44", "SPACE");
            this.put("52", "'");
            this.put("54", ",");
            this.put("45", "-");
            this.put("55", ".");
            this.put("56", "/");
            this.put("39", "0");
            this.put("30", "1");
            this.put("31", "2");
            this.put("32", "3");
            this.put("33", "4");
            this.put("34", "5");
            this.put("35", "6");
            this.put("36", "7");
            this.put("37", "8");
            this.put("38", "9");
            this.put("51", ";");
            this.put("46", "=");
            this.put("47", "[");
            this.put("49", "\\");
            this.put("48", "]");
            this.put("53", "'");
            this.put("4", "a");
            this.put("5", "b");
            this.put("6", "c");
            this.put("7", "d");
            this.put("8", "e");
            this.put("9", "f");
            this.put("10", "g");
            this.put("11", "h");
            this.put("12", "i");
            this.put("13", "j");
            this.put("14", "k");
            this.put("15", "l");
            this.put("16", "m");
            this.put("17", "n");
            this.put("18", "o");
            this.put("19", "p");
            this.put("20", "q");
            this.put("21", "r");
            this.put("22", "s");
            this.put("23", "t");
            this.put("24", "u");
            this.put("25", "v");
            this.put("26", "w");
            this.put("27", "x");
            this.put("28", "y");
            this.put("29", "z");
        }
    };

    public hotKeysDialog(remcons remcons2) {
        super(remcons2.ParentApp.dispFrame, "Programmed Hot Keys", false);
        this.ui_init(remcons2);
    }

    protected void ui_init(remcons remcons2) {
        Serializable serializable;
        this.setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        this.close = new JButton("Close");
        this.close.addActionListener(this);
        JPanel jPanel = new JPanel(new GridLayout(6, 6));
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.fill = 2;
        try {
            this.jsonString = remcons2.ParentApp.jsonObj.getJSONRequest("hot_keys");
            this.jsonString = this.jsonString.trim();
            this.jsonString = this.jsonString.substring(1, this.jsonString.length() - 1);
            serializable = Pattern.compile("-?\\d+");
            Matcher matcher = ((Pattern) serializable).matcher(this.jsonString);
            int n = 0;
            while (n < 6) {
                JLabel jLabel = new JLabel("        " + this.ctrl[n] + "        ");
                gridBagConstraints2.gridx = n;
                gridBagConstraints2.gridy = 0;
                gridBagConstraints2.fill = 2;
                jPanel.add(jLabel, gridBagConstraints2);
                int n2 = 1;
                while (n2 < 6 && matcher.find()) {
                    JLabel jLabel2 = new JLabel(this.map.get(matcher.group()));
                    gridBagConstraints2.gridx = n;
                    gridBagConstraints2.gridy = n2++;
                    gridBagConstraints2.fill = 2;
                    jPanel.add(jLabel2, gridBagConstraints2);
                }
                ++n;
            }
        } catch (Exception exception) {
            System.out.println("Error Parsing the JSON Requets");
            exception.printStackTrace();
            this.dispose();
            return;
        }
        jPanel.setBorder(BorderFactory.createEtchedBorder(0));
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        this.add(jPanel, gridBagConstraints);
        serializable = new JPanel();
        ((Container) serializable).setLayout(new FlowLayout(FlowLayout.RIGHT));
        ((Container) serializable).add(this.close);
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 13;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        this.add((Component) serializable, gridBagConstraints);
        this.pack();
        ((Component) this).setSize(this.getPreferredSize().width + 20, this.getPreferredSize().height + 10);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        ((Component) this).setVisible(true);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == this.close) {
            this.dispose();
        }
    }

    public void windowClosing(WindowEvent windowEvent) {
        this.dispose();
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
 