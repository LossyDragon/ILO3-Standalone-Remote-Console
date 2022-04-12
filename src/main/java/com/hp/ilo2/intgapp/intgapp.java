package com.hp.ilo2.intgapp;

import com.hp.ilo2.remcons.URLDialog;
import com.hp.ilo2.remcons.remcons;
import com.hp.ilo2.virtdevs.MediaAccess;
import com.hp.ilo2.virtdevs.VErrorDialog;
import com.hp.ilo2.virtdevs.VFileDialog;
import com.hp.ilo2.virtdevs.virtdevs;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import javax.swing.*;

import util.Http;
import util.Utils;

@SuppressWarnings("deprecation")
public class intgapp extends JApplet implements Runnable, ActionListener, ItemListener {

    /* ILO3RemCon addition */
    private final String hostName;

    JCheckBoxMenuItem[] localKbdLayout;
    JCheckBoxMenuItem[] vdMenuItems;
    JMenu hlpMenu;
    JMenu kbAFMenu;
    JMenu kbCAFMenu;
    JMenu kbLangMenu;
    JMenu kbMenu;
    JMenu psMenu;
    JMenu vdMenu;
    JMenuBar mainMenuBar;
    JMenuItem aboutJirc;
    JMenuItem capsLock;
    JMenuItem ctlAltBack;
    JMenuItem ctlAltDel;
    JMenuItem hotKeys;
    JMenuItem momPress;
    JMenuItem numLock;
    JMenuItem powerCycle;
    JMenuItem pressHold;
    JMenuItem sysReset;
    JMenuItem[] AltFn;
    JMenuItem[] ctlAltFn;
    JPanel dispStatusBar;
    JScrollPane scroller;
    String rcErrMessage;
    int vdmenuIndx;
    private MediaAccess ma;
    private final int REMCONS_MAX_FN_KEYS = 12;
    private final int REMCONS_MAX_KBD_LAYOUT = 17;
    public JFrame dispFrame;
    public JMenuItem vdMenuItemCrImage;
    public String enc_key;
    public String enclosure;
    public String ilo_fqdn;
    public String optional_features;
    public String rc_port;
    public String server_name;
    public String vm_key;
    public String vm_port;
    public boolean cdSelected = false;
    public boolean exit = false;
    public boolean fdSelected = false;
    public boolean in_enclosure = false;
    public byte[] enc_key_val = new byte[16];
    public int bay = 0;
    public int blade = 0;
    public jsonparser jsonObj = new jsonparser(this);
    public remcons remconsObj = new remcons(this);
    public virtdevs virtdevsObj = new virtdevs(this);

    /* ILO3RemCon addition */
    public intgapp(String hostname) {
        hostName = hostname;
    }

    /* ILO3RemCon addition */
    @Override
    public URL getCodeBase() {
        try {
            return new URL(Http.getLoginUrl(hostName));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /* ILO3RemCon addition */
    @Override
    public URL getDocumentBase() {
        try {
            return new URL(Http.getJavaAppletUrl(hostName, Http.getSessionKey()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /* ILO3RemCon addition */
    // TODO: getParameter("INFO0") wasn't found
    // TODO: getParameter("INFO2") wasn't found
    // TODO: getParameter("floppy") wasn't found
    // TODO: getParameter("cdrom") wasn't found
    // TODO: getParameter("device") wasn't found
    // TODO: getParameter("config") wasn't found
    // TODO: getParameter("UNIQUE_FEATURES") wasn't found
    @Override
    public String getParameter(String name) {
        switch (name) {
            case "hostAddress":
                return hostName;
            case "RCINFO1":
                return Http.getSessionKey();
            default:
                System.out.println("-- getParameter(" + name + ") wasn't found --");
                return null;
        }
    }

    public void init() {
        System.out.println("Started Retrieving parameters from ILO..");

        String jSONRequest = jsonObj.getJSONRequest("rc_info");
        ApplyRcInfoParameters(jSONRequest);

        System.out.println("Completed Retrieving parameters from ILO");

        virtdevsObj.init();
        remconsObj.init();

        ui_init();

        if (jSONRequest == null) {
            System.out.println("Failed to retrive parameters from ILO");
            new VErrorDialog(dispFrame, locinfo.DIALOGSTR_2014, rcErrMessage, true);
            dispFrame.setVisible(false);
        }
    }

    public void start() {
        try {
            virtdevsObj.start();
            remconsObj.start();

            dispFrame.getContentPane().add(scroller, "Center");
            dispFrame.getContentPane().add(dispStatusBar, "South");
            scroller.validate();
            dispStatusBar.validate();
            dispFrame.validate();

            System.out.println("Set Inital focus for session..");
            remconsObj.session.requestFocus();

            run();
        } catch (Exception e) {
            System.out.println("FAILURE: exception starting applet");
            e.printStackTrace();
        }
    }

    public void stop() {
        exit = true;

        virtdevsObj.stop();
        remconsObj.remconsUnInstallKeyboardHook();
        remconsObj.stop();
    }

    public void destroy() {
        System.out.println("Destroying subsustems");

        exit = true;

        remconsObj.remconsUnInstallKeyboardHook();
        virtdevsObj.destroy();
        remconsObj.destroy();
    }

    @Override
    public synchronized void run() {
        while (true) {
            try {
                int i = 0;
                int i2 = 0;
                ma = new MediaAccess();
                String[] devices = ma.devices();

                for (int i3 = 0; devices != null && i3 < devices.length; i3++) {
                    int devtype = ma.devtype(devices[i3]);

                    if (devtype == 2 || devtype == 5) {
                        i2++;
                    }
                }

                if (i2 > vdmenuIndx - 4) {
                    int i4 = 0;

                    while (true) {
                        if (devices == null || i4 >= devices.length) {
                            break;
                        }

                        boolean z = false;
                        int devtype2 = ma.devtype(devices[i4]);

                        for (int i5 = 0; i5 < vdmenuIndx - 4; i5++) {
                            if (devices[i4].equals(vdMenu.getItem(i5).getText())) {
                                z = true;
                                i++;
                            }
                        }

                        if (!z) {
                            if (devtype2 == 2) {
                                System.out.println("Device attached: " + devices[i4]);

                                vdMenuItems[vdmenuIndx] = new JCheckBoxMenuItem(devices[i4]);
                                vdMenuItems[vdmenuIndx].setActionCommand("fd" + devices[i4]);
                                vdMenuItems[vdmenuIndx].addItemListener(this);

                                if (devices[i4].equals("A:") || devices[i4].equals("B:")) {
                                    vdMenuItems[vdmenuIndx].setIcon(
                                            new ImageIcon(Utils.getResourceImage(this, "FloppyDisk.png")));
                                } else {
                                    vdMenuItems[vdmenuIndx].setIcon(
                                            new ImageIcon(Utils.getResourceImage(this, "usb.png")));
                                }

                                vdMenu.add(vdMenuItems[vdmenuIndx], i);
                                vdMenu.updateUI();
                                vdmenuIndx++;
                            } else if (devtype2 == 5) {
                                System.out.println("CDROM Hot plug device auto-update no supported at this time");
                            }
                        }

                        i4++;
                    }
                } else if (i2 < vdmenuIndx - 4) {
                    int i6 = 0;

                    while (true) {
                        if (i6 >= vdmenuIndx - 4) {
                            break;
                        }

                        boolean z2 = false;

                        for (int i7 = 0; devices != null && i7 < devices.length; i7++) {
                            int devtype3 = ma.devtype(devices[i7]);

                            if ((devtype3 == 2 || devtype3 == 5)
                                    && vdMenu.getItem(i6).getText().equals(devices[i7])) {
                                z2 = true;
                            }
                        }

                        if (!z2) {
                            System.out.println("Device removed: " + vdMenu.getItem(i6).getText());

                            vdMenu.remove(i6);
                            vdMenu.updateUI();
                            vdmenuIndx--;

                            break;
                        }

                        i6++;
                    }
                }

                ma = null;
                remconsObj.session.set_status(3, "");
                remconsObj.sleepAtLeast(5000L);
            } catch (InterruptedException e) {
                System.out.println("Exception on intgapp");
            }

            if (exit) {
                System.out.println("Intgapp stopped running");
                return;
            }
        }
    }

    public void ui_init() {
        System.out.println("Message from ui_init55");

        dispFrame = new JFrame("JavaApplet IRC Window");
        dispFrame.getContentPane().setLayout(new BorderLayout());
        dispFrame.addWindowListener(new WindowCloser(this));

        mainMenuBar = new JMenuBar();

        dispStatusBar = new JPanel(new BorderLayout());
        dispStatusBar.add(remconsObj.session.status_box, "West");
        dispStatusBar.add(remconsObj.pwrStatusPanel, "East");

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        dispFrame.setJMenuBar(mainMenuBar);

        String jSONRequest = jsonObj.getJSONRequest("session_info");
        makePsMenu(mainMenuBar, jsonObj.getJSONNumber(jSONRequest, "reset_priv"));
        makeVdMenu(mainMenuBar, jsonObj.getJSONNumber(jSONRequest, "virtual_media_priv"));
        makeKbMenu(mainMenuBar);
        makeHlpMenu(mainMenuBar);

        scroller = new JScrollPane(
                remconsObj.session,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        scroller.setVisible(true);

        try {
            String stringBuffer = locinfo.MENUSTR_1024
                    + " "
                    + server_name
                    + " "
                    + locinfo.MENUSTR_1025
                    + " "
                    + ilo_fqdn;

            if (blade == 1 && in_enclosure) {
                stringBuffer = stringBuffer
                        + " "
                        + locinfo.MENUSTR_1026
                        + " "
                        + enclosure
                        + " "
                        + locinfo.MENUSTR_1027
                        + " "
                        + bay;
            }

            dispFrame.setTitle(stringBuffer);
        } catch (Exception e) {
            dispFrame.setTitle(locinfo.MENUSTR_1024 + " " + getCodeBase().getHost());

            System.out.println("IRC title not available");
        }

        int i = Toolkit.getDefaultToolkit().getScreenSize().width;
        int i2 = Toolkit.getDefaultToolkit().getScreenSize().height;
        int i3 = Math.min(i, 1054);
        int i4 = i2 < 874 ? i2 - 30 : 874;
        int i5 = i > 1054 ? (i - 1054) / 2 : 0;
        int i6 = i2 > 874 ? (i2 - 874) / 2 : 0;

        dispFrame.setSize(i3, i4);
        dispFrame.setLocation(i5, i6);

        System.out.println("check dimensions " + i3 + " " + i4 + " " + i5 + " " + i6);

        dispFrame.setVisible(true);

        try {
            dispFrame.getInsets();
            dispFrame.setIconImage(Utils.getResourceImage(this, "hp_logo.png"));

            if (dispFrame.getIconImage() == null) {
                System.out.println("Dimage is null");
            }
        } catch (Exception e2) {
            System.out.println("JIRC icon not available");
        }
    }

    protected void makeHlpMenu(JMenuBar jMenuBar) {
        hlpMenu = new JMenu(locinfo.MENUSTR_1028);
        aboutJirc = new JMenuItem(locinfo.MENUSTR_1029);

        aboutJirc.addActionListener(this);
        hlpMenu.add(aboutJirc);
        jMenuBar.add(hlpMenu);
    }

    protected void makeVdMenu(JMenuBar jMenuBar, int i) {
        vdMenu = new JMenu(locinfo.MENUSTR_1002);

        if (i == 1) {
            jMenuBar.add(vdMenu);
        }
    }

    public void updateVdMenu() {
        ma = new MediaAccess();
        String jSONRequest = jsonObj.getJSONRequest("vm_status");
        String jSONArray = jsonObj.getJSONArray(jSONRequest, "options", 0);
        String jSONArray2 = jsonObj.getJSONArray(jSONRequest, "options", 1);
        String[] devices = ma.devices();

        vdmenuIndx = 0;

        if (devices != null) {
            vdMenuItems = new JCheckBoxMenuItem[devices.length + 5];

            for (String device : devices) {
                int devtype = ma.devtype(device);

                if (devtype == 5) {
                    vdMenuItems[vdmenuIndx] = new JCheckBoxMenuItem(device);
                    vdMenuItems[vdmenuIndx].setActionCommand("cd" + device);
                    vdMenuItems[vdmenuIndx].addItemListener(this);
                    vdMenuItems[vdmenuIndx].setIcon(
                            new ImageIcon(Utils.getResourceImage(this, "CD_Drive.png")));
                    vdMenu.add(vdMenuItems[vdmenuIndx]);
                    vdmenuIndx++;
                } else if (devtype == 2) {
                    vdMenuItems[vdmenuIndx] = new JCheckBoxMenuItem(device);
                    vdMenuItems[vdmenuIndx].setActionCommand("fd" + device);
                    vdMenuItems[vdmenuIndx].addItemListener(this);

                    if (device.equals("A:") || device.equals("B:")) {
                        vdMenuItems[vdmenuIndx].setIcon(
                                new ImageIcon(Utils.getResourceImage(this, "FloppyDisk.png")));
                    } else {
                        vdMenuItems[vdmenuIndx].setIcon(
                                new ImageIcon(Utils.getResourceImage(this, "usb.png")));
                    }

                    vdMenu.add(vdMenuItems[vdmenuIndx]);

                    vdmenuIndx++;
                }
            }
        } else {
            vdMenuItems = new JCheckBoxMenuItem[5];
            System.out.println("Media Access not available...");
        }

        ma = null;

        vdMenuItems[vdmenuIndx] = new JCheckBoxMenuItem(locinfo.MENUSTR_1022 + " " + locinfo.MENUSTR_100A);
        vdMenuItems[vdmenuIndx].setActionCommand("fd" + locinfo.STATUSSTR_3117);
        vdMenuItems[vdmenuIndx].addItemListener(this);
        vdMenuItems[vdmenuIndx].setIcon(new ImageIcon(Utils.getResourceImage(this, "Image_File.png")));
        vdMenu.add(vdMenuItems[vdmenuIndx]);
        vdmenuIndx++;

        vdMenuItems[vdmenuIndx] = new JCheckBoxMenuItem(locinfo.MENUSTR_1023 + locinfo.MENUSTR_100A);
        vdMenuItems[vdmenuIndx].setActionCommand("FLOPPY");
        vdMenuItems[vdmenuIndx].setIcon(new ImageIcon(Utils.getResourceImage(this, "Network.png")));
        vdMenu.add(vdMenuItems[vdmenuIndx]);
        vdMenuItems[vdmenuIndx].addItemListener(this);
        vdmenuIndx++;

        if (jsonObj.getJSONNumber(jSONArray, "vm_url_connected") == 1
                && jsonObj.getJSONNumber(jSONArray, "vm_connected") == 1
        ) {
            fdSelected = true;
            lockFdMenu(false, "URL Removable Media");
        }

        vdMenuItems[vdmenuIndx] = new JCheckBoxMenuItem(locinfo.MENUSTR_1022 + " " + locinfo.MENUSTR_100B);
        vdMenuItems[vdmenuIndx].setActionCommand("cd" + locinfo.STATUSSTR_3117);
        vdMenuItems[vdmenuIndx].addItemListener(this);
        vdMenuItems[vdmenuIndx].setIcon(new ImageIcon(Utils.getResourceImage(this, "Image_File.png")));
        vdMenu.add(vdMenuItems[vdmenuIndx]);
        vdmenuIndx++;

        vdMenuItems[vdmenuIndx] = new JCheckBoxMenuItem(locinfo.MENUSTR_1023 + locinfo.MENUSTR_100B);
        vdMenuItems[vdmenuIndx].setActionCommand("CDROM");
        vdMenuItems[vdmenuIndx].setIcon(new ImageIcon(Utils.getResourceImage(this, "Network.png")));
        vdMenu.add(vdMenuItems[vdmenuIndx]);
        vdMenuItems[vdmenuIndx].addItemListener(this);
        vdmenuIndx++;

        if (jsonObj.getJSONNumber(jSONArray2, "vm_url_connected") == 1
                && jsonObj.getJSONNumber(jSONArray2, "vm_connected") == 1
        ) {
            cdSelected = true;
            lockCdMenu(false, "URL CD/DVD-ROM");
        }

        vdMenu.addSeparator();
        vdMenuItemCrImage = new JMenuItem(locinfo.MENUSTR_100D);
        vdMenuItemCrImage.setActionCommand("CreateDiskImage");
        vdMenuItemCrImage.addActionListener(this);
        vdMenu.add(vdMenuItemCrImage);
    }

    public void lockCdMenu(boolean z, String str) {
        for (int i = 0; i < vdmenuIndx; i++) {
            vdMenu.getItem(i).removeItemListener(this);

            if (vdMenu.getItem(i).getActionCommand().startsWith("cd")
                    || vdMenu.getItem(i).getActionCommand().equals("CDROM")
            ) {
                if (str.equals(vdMenu.getItem(i).getText())) {
                    vdMenu.getItem(i).setSelected(!z);
                } else {
                    vdMenu.getItem(i).setSelected(false);
                    vdMenu.getItem(i).setEnabled(z);
                }
            }

            vdMenu.getItem(i).addItemListener(this);
        }
    }

    public void lockFdMenu(boolean z, String str) {
        for (int i = 0; i < vdmenuIndx; i++) {
            vdMenu.getItem(i).removeItemListener(this);

            if (vdMenu.getItem(i).getActionCommand().startsWith("fd")
                    || vdMenu.getItem(i).getActionCommand().equals("FLOPPY")
            ) {
                if (str.equals(vdMenu.getItem(i).getText())) {
                    vdMenu.getItem(i).setSelected(!z);
                } else {
                    vdMenu.getItem(i).setSelected(false);
                    vdMenu.getItem(i).setEnabled(z);
                }
            }

            vdMenu.getItem(i).addItemListener(this);
        }
    }

    protected void makePsMenu(JMenuBar jMenuBar, int i) {
        psMenu = new JMenu(locinfo.MENUSTR_1001);

        momPress = new JMenuItem(locinfo.MENUSTR_1004);
        momPress.setIcon(new ImageIcon(Utils.getResourceImage(this, "press.png")));
        momPress.setActionCommand("psMomPress");
        momPress.addActionListener(this);
        psMenu.add(momPress);

        pressHold = new JMenuItem(locinfo.MENUSTR_1005);
        pressHold.setIcon(new ImageIcon(Utils.getResourceImage(this, "hold.png")));
        pressHold.setActionCommand("psPressHold");
        pressHold.addActionListener(this);

        powerCycle = new JMenuItem(locinfo.MENUSTR_1006);
        powerCycle.setIcon(new ImageIcon(Utils.getResourceImage(this, "coldboot.png")));
        powerCycle.setActionCommand("psPowerCycle");
        powerCycle.addActionListener(this);

        sysReset = new JMenuItem(locinfo.MENUSTR_1007);
        sysReset.setIcon(new ImageIcon(Utils.getResourceImage(this, "reset.png")));
        sysReset.setActionCommand("psSysReset");
        sysReset.addActionListener(this);

        if (i == 1) {
            jMenuBar.add(psMenu);
        }
    }

    public void updatePsMenu(int i) {
        if (0 == i) {
            psMenu.remove(pressHold);
            psMenu.remove(powerCycle);
            psMenu.remove(sysReset);
            return;
        }

        psMenu.remove(pressHold);
        psMenu.remove(powerCycle);
        psMenu.remove(sysReset);

        psMenu.add(pressHold);
        psMenu.add(powerCycle);
        psMenu.add(sysReset);
    }

    protected void makeKbMenu(JMenuBar jMenuBar) {
        kbMenu = new JMenu(locinfo.MENUSTR_1003);
        kbCAFMenu = new JMenu("CTRL-ALT-Fn");
        kbAFMenu = new JMenu("ALT-Fn");
        kbLangMenu = new JMenu(locinfo.MENUSTR_100E);

        ctlAltDel = new JMenuItem(locinfo.MENUSTR_1008);
        ctlAltDel.setIcon(new ImageIcon(Utils.getResourceImage(this, "Keyboard.png")));
        ctlAltDel.setActionCommand("kbCtlAltDel");
        ctlAltDel.addActionListener(this);
        kbMenu.add(ctlAltDel);

        numLock = new JMenuItem(locinfo.MENUSTR_1009);
        numLock.setIcon(new ImageIcon(Utils.getResourceImage(this, "Keyboard.png")));
        numLock.setActionCommand("kbNumLock");
        numLock.addActionListener(this);
        kbMenu.add(numLock);

        capsLock = new JMenuItem(locinfo.MENUSTR_1020);
        capsLock.setIcon(new ImageIcon(Utils.getResourceImage(this, "Keyboard.png")));
        capsLock.setActionCommand("kbCapsLock");
        capsLock.addActionListener(this);
        kbMenu.add(capsLock);

        ctlAltBack = new JMenuItem("CTRL-ALT-BACKSPACE");
        ctlAltBack.setIcon(new ImageIcon(Utils.getResourceImage(this, "Keyboard.png")));
        ctlAltBack.setActionCommand("kbCtlAltBack");
        ctlAltBack.addActionListener(this);
        ctlAltFn = new JMenuItem[REMCONS_MAX_FN_KEYS];

        for (int i = 0; i < REMCONS_MAX_FN_KEYS; i++) {
            ctlAltFn[i] = new JMenuItem("CTRL-ALT-F" + (i + 1));
            ctlAltFn[i].setActionCommand("kbCtrlAltFn" + i);
            ctlAltFn[i].addActionListener(this);
            kbCAFMenu.add(ctlAltFn[i]);
        }

        AltFn = new JMenuItem[REMCONS_MAX_FN_KEYS];

        for (int i2 = 0; i2 < REMCONS_MAX_FN_KEYS; i2++) {
            AltFn[i2] = new JMenuItem("ALT-F" + (i2 + 1));
            AltFn[i2].setActionCommand("kbAltFn" + i2);
            AltFn[i2].addActionListener(this);
            kbAFMenu.add(AltFn[i2]);
        }

        localKbdLayout = new JCheckBoxMenuItem[REMCONS_MAX_KBD_LAYOUT];

        for (int i3 = 0; i3 < REMCONS_MAX_KBD_LAYOUT; i3++) {
            localKbdLayout[i3] = new JCheckBoxMenuItem(locinfo.MENUSTR_100F + i3);
            localKbdLayout[i3].setActionCommand("localKbdLayout" + i3);
            localKbdLayout[i3].addItemListener(this);
            kbLangMenu.add(localKbdLayout[i3]);
        }

        localKbdLayout[0].setSelected(true);

        if (!System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            kbMenu.add(ctlAltBack);
            kbMenu.add(kbCAFMenu);
            kbMenu.add(kbAFMenu);
            kbMenu.add(kbLangMenu);
        }

        kbMenu.addSeparator();

        hotKeys = new JMenuItem(locinfo.MENUSTR_1021);

        hotKeys.addActionListener(this);
        kbMenu.add(hotKeys);

        jMenuBar.add(kbMenu);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == momPress) {
            remconsObj.session.sendMomPress();
        } else if (actionEvent.getSource() == pressHold) {
            remconsObj.session.sendPressHold();
        } else if (actionEvent.getSource() == powerCycle) {
            remconsObj.session.sendPowerCycle();
        } else if (actionEvent.getSource() == sysReset) {
            remconsObj.session.sendSystemReset();
        } else if (actionEvent.getSource() == ctlAltDel) {
            remconsObj.session.send_ctrl_alt_del();
        } else if (actionEvent.getSource() == numLock) {
            remconsObj.session.send_num_lock();
        } else if (actionEvent.getSource() == capsLock) {
            remconsObj.session.send_caps_lock();
        } else if (actionEvent.getSource() == ctlAltBack) {
            remconsObj.session.send_ctrl_alt_back();
        } else if (actionEvent.getSource() == hotKeys) {
            remconsObj.viewHotKeys();
        } else if (actionEvent.getSource() == vdMenuItemCrImage) {
            virtdevsObj.createImage();
        } else if (actionEvent.getSource() == aboutJirc) {
            remconsObj.viewAboutJirc();
        } else {
            int i = 0;

            while (true) {
                if (i >= REMCONS_MAX_FN_KEYS) {
                    break;
                } else if (actionEvent.getSource() == ctlAltFn[i]) {
                    remconsObj.session.send_ctrl_alt_fn(i);
                    break;
                } else if (actionEvent.getSource() == AltFn[i]) {
                    remconsObj.session.send_alt_fn(i);
                    break;
                } else {
                    i++;
                }
            }

            if (i >= REMCONS_MAX_FN_KEYS) {
                System.out.println("Unhandled ActionItem" + actionEvent.getActionCommand());
            }
        }
    }

    public void itemStateChanged(ItemEvent itemEvent) {
        AbstractButton abstractButton = null;
        String str = null;
        String str2 = null;
        int stateChange = itemEvent.getStateChange();

        for (int i = 0; i < REMCONS_MAX_KBD_LAYOUT; i++) {
            if (localKbdLayout[i] == itemEvent.getSource() && stateChange == 1) {
                System.out.println(i);
                localKbdLayout[i].setSelected(true);
                kbdLayoutMenuHandler(i);
                return;
            }
        }

        int i2 = 0;

        while (true) {
            if (i2 >= vdmenuIndx) {
                break;
            } else if (vdMenuItems[i2] == itemEvent.getSource()) {
                abstractButton = vdMenuItems[i2];
                str = abstractButton.getActionCommand();
                str2 = abstractButton.getLabel();

                break;
            } else {
                i2++;
            }
        }

        if (abstractButton == null || str == null) {
            System.out.println("Unhandled item event");
        } else if (str.equals("fd" + locinfo.STATUSSTR_3117)) {
            if (stateChange == 2) {
                virtdevsObj.do_floppy(str2);
                lockFdMenu(true, str2);
            } else if (stateChange == 1) {
                dispFrame.setVisible(false);
                String string = new VFileDialog(locinfo.DIALOGSTR_2045, "*.img").getString();
                dispFrame.setVisible(true);

                if (string != null) {
                    if (virtdevsObj.fdThread != null) {
                        virtdevsObj.change_disk(virtdevsObj.fdConnection, string);
                    }

                    System.out.println("Image file: " + string);
                    lockFdMenu(!virtdevsObj.do_floppy(string), str2);
                } else {
                    lockFdMenu(true, str2);
                }
            }
        } else if (str.equals("cd" + locinfo.STATUSSTR_3117)) {
            if (stateChange == 2) {
                virtdevsObj.do_cdrom(str2);
                lockCdMenu(true, str2);
            } else if (stateChange == 1) {
                dispFrame.setVisible(false);
                String string2 = new VFileDialog(locinfo.DIALOGSTR_2045, "*.iso").getString();
                dispFrame.setVisible(true);

                if (string2 != null) {
                    if (virtdevsObj.cdThread != null) {
                        virtdevsObj.change_disk(virtdevsObj.cdConnection, string2);
                    }
                    System.out.println("Image file: " + string2);
                    lockCdMenu(!virtdevsObj.do_cdrom(string2), str2);
                } else {
                    lockCdMenu(true, str2);
                }
            }
        } else if (str.startsWith("cd")) {
            if (virtdevsObj.do_cdrom(str2)) {
                lockCdMenu(stateChange != 1, str2);
            }
        } else if (str.startsWith("fd")) {
            if (virtdevsObj.do_floppy(str2)) {
                lockFdMenu(stateChange != 1, str2);
            }
        } else if (str.equals("FLOPPY") || str.equals("CDROM")) {
            boolean z = false;
            if (stateChange == 2) {
                jsonObj.postJSONRequest(
                        "vm_status",
                        "{\"method\":\"set_virtual_media_options\", \"device\":\""
                                + str
                                + "\", \"command\":\"EJECT\", \"session_key\":\""
                                + getParameter("RCINFO1")
                                + "\"}"
                );
                remconsObj.session.set_status(3, "Unmounted URL");
            } else if (stateChange == 1) {
                remconsObj.setDialogIsOpen(true);
                String userInput = new URLDialog(remconsObj).getUserInput();

                if (userInput.compareTo("userhitcancel") == 0 || userInput.compareTo("userhitclose") == 0) {
                    userInput = null;
                }

                if (userInput != null) {
                    userInput = userInput.replaceAll("[\\u0000-\u001F]", "");
                    System.out.println("url:  " + userInput);
                }

                remconsObj.setDialogIsOpen(false);

                if (userInput != null) {
                    String postJSONRequest = jsonObj.postJSONRequest(
                            "vm_status",
                            "{\"method\":\"set_virtual_media_options\", \"device\":\""
                                    + str
                                    + "\", \"command\":\"INSERT\", \"url\":\""
                                    + userInput
                                    + "\", \"session_key\":\""
                                    + getParameter("RCINFO1")
                                    + "\"}"
                    );

                    if (Objects.equals(postJSONRequest, "Success")) {
                        postJSONRequest = jsonObj.postJSONRequest(
                                "vm_status",
                                "{\"method\":\"set_virtual_media_options\", \"device\":\""
                                        + str
                                        + "\", \"boot_option\":\"CONNECT\", \"command\":\"SET\", \"url\":\""
                                        + userInput
                                        + "\", \"session_key\":\""
                                        + getParameter("RCINFO1")
                                        + "\"}"
                        );
                    }
                    if (Objects.equals(postJSONRequest, "SCSI_ERR_NO_LICENSE")) {
                        new VErrorDialog(
                                dispFrame,
                                locinfo.DIALOGSTR_202c,
                                "<html>"
                                        + locinfo.DIALOGSTR_2015
                                        + " "
                                        + locinfo.DIALOGSTR_2016
                                        + " "
                                        + locinfo.DIALOGSTR_202d
                                        + "<br><br>"
                                        + locinfo.DIALOGSTR_202e
                                        + "</html>",
                                true
                        );
                    } else if (!Objects.equals(postJSONRequest, "Success")) {
                        new VErrorDialog(dispFrame, locinfo.DIALOGSTR_2014, locinfo.DIALOGSTR_2064, true);
                    } else {
                        z = true;

                        remconsObj.session.set_status(3, locinfo.STATUSSTR_3125);
                    }
                }
            }

            if (str.equals("FLOPPY")) {
                lockFdMenu(!z, str2);
            } else if (str.equals("CDROM")) {
                lockCdMenu(!z, str2);
            }
        }
    }

    public void kbdLayoutMenuHandler(int i) {
        for (int i2 = 0; i2 < REMCONS_MAX_KBD_LAYOUT; i2++) {
            if (i2 != i) {
                localKbdLayout[i2].setSelected(false);
            }
        }

        remconsObj.setLocalKbdLayout(i);
    }

    public static class WindowCloser extends WindowAdapter {
        private final intgapp app;

        WindowCloser(intgapp intgappVar) {
            app = intgappVar;
        }

        public void windowClosing(WindowEvent windowEvent) {
            app.stop();
            app.exit = true;
        }
    }

    private void ApplyRcInfoParameters(String str) {
        vm_port = null;
        vm_key = null;
        rc_port = null;
        enc_key = null;
        Arrays.fill(enc_key_val, (byte) 0);
        String trim = str.trim();

        for (String str2 : trim.substring(1, trim.length() - 1).split(",")) {
            String[] split = str2.split(":");
            if (split.length != 2) {
                System.out.println("Error in ApplyRcInfoParameters");
                return;
            }

            String trim2 = split[0].trim();
            String substring = trim2.substring(1, trim2.length() - 1);
            String trim3 = split[1].trim();

            if (trim3.charAt(0) == '\"') {
                trim3 = trim3.substring(1, trim3.length() - 1);
            }

            if (substring.compareToIgnoreCase("enc_key") == 0) {
                enc_key = trim3;
                for (int i = 0; i < enc_key_val.length; i++) {
                    try {
                        enc_key_val[i] =
                                (byte) Integer.parseInt(enc_key.substring(i * 2, (i * 2) + 2), 16);
                    } catch (NumberFormatException e) {
                        System.out.println("Failed to Parse enc_key");
                    }
                }
            } else if (substring.compareToIgnoreCase("rc_port") == 0) {
                System.out.println("rc_port:" + trim3);
                rc_port = trim3;
            } else if (substring.compareToIgnoreCase("vm_key") == 0) {
                vm_key = trim3;
            } else if (substring.compareToIgnoreCase("vm_port") == 0) {
                System.out.println("vm_port:" + trim3);
                vm_port = trim3;
            } else if (substring.equalsIgnoreCase("optional_features")) {
                System.out.println("optional_features:" + trim3);
                optional_features = trim3;
            } else if (substring.compareToIgnoreCase("server_name") == 0) {
                System.out.println("server_name:" + trim3);
                server_name = trim3;
            } else if (substring.compareToIgnoreCase("ilo_fqdn") == 0) {
                System.out.println("ilo_fqdn:" + trim3);
                ilo_fqdn = trim3;
            } else if (substring.compareToIgnoreCase("blade") == 0) {
                blade = Integer.parseInt(trim3);
                System.out.println("blade:" + blade);
            } else if (blade == 1 && substring.compareToIgnoreCase("enclosure") == 0) {
                if (!trim3.equals("null")) {
                    in_enclosure = true;
                    System.out.println("enclosure:" + trim3);
                    enclosure = trim3;
                }
            } else if (blade == 1 && substring.compareToIgnoreCase("bay") == 0) {
                bay = Integer.parseInt(trim3);
                System.out.println("bay:" + bay);
            }
        }
    }

    public void moveUItoInit(boolean z) {
        System.out.println("Disable Menus\n");
        psMenu.setEnabled(z);
        vdMenu.setEnabled(z);
        kbMenu.setEnabled(z);
    }
}
