package com.hp.ilo2.remcons;

import java.awt.*;
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

public class hotKeysDialog extends JDialog implements ActionListener, WindowListener {

    JButton close;
    String jsonString;
    String[] ctrl = new String[]{"Ctrl-T:", "Ctrl-U:", "Ctrl-V:", "Ctrl-W:", "Ctrl-X:", "Ctrl-Y:"};
    Hashtable<String, String> map = new Hashtable<>() {
        {
            put("0", "NONE");
            put("41", "ESC");
            put("226", "L_ALT");
            put("230", "R_ALT");
            put("225", "L_SHIFT");
            put("229", "R_SHIFT");
            put("224", "L_CTRL");
            put("228", "R-CTRL");
            put("227", "L_GUI");
            put("231", "R_GUI");
            put("73", "INS");
            put("76", "DEL");
            put("74", "HOME");
            put("77", "END");
            put("75", "PG_UP");
            put("78", "PG_DN");
            put("88", "ENTER");
            put("43", "TAB");
            put("72", "BREAK");
            put("42", "BACKSPACE");
            put("87", "NUM_PLUS");
            put("86", "NUM_MINUS");
            put("71", "SCRL_LCK");
            put("154", "SYS_RQ");
            put("70", "PRINT_SCRN");
            put("58", "F1");
            put("59", "F2");
            put("60", "F3");
            put("61", "F4");
            put("62", "F5");
            put("63", "F6");
            put("64", "F7");
            put("65", "F8");
            put("66", "F9");
            put("67", "F10");
            put("68", "F11");
            put("69", "F12");
            put("44", "SPACE");
            put("52", "'");
            put("54", ",");
            put("45", "-");
            put("55", ".");
            put("56", "/");
            put("39", "0");
            put("30", "1");
            put("31", "2");
            put("32", "3");
            put("33", "4");
            put("34", "5");
            put("35", "6");
            put("36", "7");
            put("37", "8");
            put("38", "9");
            put("51", ";");
            put("46", "=");
            put("47", "[");
            put("49", "\\");
            put("48", "]");
            put("53", "'");
            put("4", "a");
            put("5", "b");
            put("6", "c");
            put("7", "d");
            put("8", "e");
            put("9", "f");
            put("10", "g");
            put("11", "h");
            put("12", "i");
            put("13", "j");
            put("14", "k");
            put("15", "l");
            put("16", "m");
            put("17", "n");
            put("18", "o");
            put("19", "p");
            put("20", "q");
            put("21", "r");
            put("22", "s");
            put("23", "t");
            put("24", "u");
            put("25", "v");
            put("26", "w");
            put("27", "x");
            put("28", "y");
            put("29", "z");
        }
    };

    public hotKeysDialog(remcons remcons2) {
        super(remcons2.ParentApp.dispFrame, "Programmed Hot Keys", false);

        ui_init(remcons2);
    }

    protected void ui_init(remcons remcons2) {
        Serializable serializable;
        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        close = new JButton("Close");
        close.addActionListener(this);

        JPanel jPanel = new JPanel(new GridLayout(6, 6));
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.fill = 2;

        try {
            jsonString = remcons2.ParentApp.jsonObj.getJSONRequest("hot_keys");
            jsonString = jsonString.trim();
            jsonString = jsonString.substring(1, jsonString.length() - 1);
            serializable = Pattern.compile("-?\\d+");
            Matcher matcher = ((Pattern) serializable).matcher(jsonString);

            int n = 0;
            while (n < 6) {
                JLabel jLabel = new JLabel("        " + ctrl[n] + "        ");
                gridBagConstraints2.gridx = n;
                gridBagConstraints2.gridy = 0;
                gridBagConstraints2.fill = 2;

                jPanel.add(jLabel, gridBagConstraints2);

                int n2 = 1;
                while (n2 < 6 && matcher.find()) {
                    JLabel jLabel2 = new JLabel(map.get(matcher.group()));

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
            dispose();
            return;
        }

        jPanel.setBorder(BorderFactory.createEtchedBorder(0));

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(jPanel, gridBagConstraints);

        serializable = new JPanel();

        ((Container) serializable).setLayout(new FlowLayout(FlowLayout.RIGHT));
        ((Container) serializable).add(close);

        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 13;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;

        add((Component) serializable, gridBagConstraints);
        pack();

        setSize(getPreferredSize().width + 20, getPreferredSize().height + 10);
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == close) {
            dispose();
        }
    }

    public void windowClosing(WindowEvent windowEvent) {
        dispose();
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
 