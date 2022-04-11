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
    Hashtable map = new Hashtable() {
        {
            this.put("0", new String("NONE"));
            this.put("41", new String("ESC"));
            this.put("226", new String("L_ALT"));
            this.put("230", new String("R_ALT"));
            this.put("225", new String("L_SHIFT"));
            this.put("229", new String("R_SHIFT"));
            this.put("224", new String("L_CTRL"));
            this.put("228", new String("R-CTRL"));
            this.put("227", new String("L_GUI"));
            this.put("231", new String("R_GUI"));
            this.put("73", new String("INS"));
            this.put("76", new String("DEL"));
            this.put("74", new String("HOME"));
            this.put("77", new String("END"));
            this.put("75", new String("PG_UP"));
            this.put("78", new String("PG_DN"));
            this.put("88", new String("ENTER"));
            this.put("43", new String("TAB"));
            this.put("72", new String("BREAK"));
            this.put("42", new String("BACKSPACE"));
            this.put("87", new String("NUM_PLUS"));
            this.put("86", new String("NUM_MINUS"));
            this.put("71", new String("SCRL_LCK"));
            this.put("154", new String("SYS_RQ"));
            this.put("70", new String("PRINT_SCRN"));
            this.put("58", new String("F1"));
            this.put("59", new String("F2"));
            this.put("60", new String("F3"));
            this.put("61", new String("F4"));
            this.put("62", new String("F5"));
            this.put("63", new String("F6"));
            this.put("64", new String("F7"));
            this.put("65", new String("F8"));
            this.put("66", new String("F9"));
            this.put("67", new String("F10"));
            this.put("68", new String("F11"));
            this.put("69", new String("F12"));
            this.put("44", new String("SPACE"));
            this.put("52", new String("'"));
            this.put("54", new String(","));
            this.put("45", new String("-"));
            this.put("55", new String("."));
            this.put("56", new String("/"));
            this.put("39", new String("0"));
            this.put("30", new String("1"));
            this.put("31", new String("2"));
            this.put("32", new String("3"));
            this.put("33", new String("4"));
            this.put("34", new String("5"));
            this.put("35", new String("6"));
            this.put("36", new String("7"));
            this.put("37", new String("8"));
            this.put("38", new String("9"));
            this.put("51", new String(";"));
            this.put("46", new String("="));
            this.put("47", new String("["));
            this.put("49", new String("\\"));
            this.put("48", new String("]"));
            this.put("53", new String("'"));
            this.put("4", new String("a"));
            this.put("5", new String("b"));
            this.put("6", new String("c"));
            this.put("7", new String("d"));
            this.put("8", new String("e"));
            this.put("9", new String("f"));
            this.put("10", new String("g"));
            this.put("11", new String("h"));
            this.put("12", new String("i"));
            this.put("13", new String("j"));
            this.put("14", new String("k"));
            this.put("15", new String("l"));
            this.put("16", new String("m"));
            this.put("17", new String("n"));
            this.put("18", new String("o"));
            this.put("19", new String("p"));
            this.put("20", new String("q"));
            this.put("21", new String("r"));
            this.put("22", new String("s"));
            this.put("23", new String("t"));
            this.put("24", new String("u"));
            this.put("25", new String("v"));
            this.put("26", new String("w"));
            this.put("27", new String("x"));
            this.put("28", new String("y"));
            this.put("29", new String("z"));
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
                jPanel.add((Component) jLabel, gridBagConstraints2);
                int n2 = 1;
                while (n2 < 6 && matcher.find()) {
                    JLabel jLabel2 = new JLabel((String) this.map.get(matcher.group()));
                    gridBagConstraints2.gridx = n;
                    gridBagConstraints2.gridy = n2++;
                    gridBagConstraints2.fill = 2;
                    jPanel.add((Component) jLabel2, gridBagConstraints2);
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
        this.add((Component) jPanel, gridBagConstraints);
        serializable = new JPanel();
        ((Container) serializable).setLayout(new FlowLayout(2));
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
 