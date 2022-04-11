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
    JMenuItem mdebug1;
    JMenuItem mdebug2;
    JMenuItem mdebug3;
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
    int cdMenuItems;
    int fdMenuItems;
    int vdmenuIndx;
    private MediaAccess ma;
    private final int REMCONS_MAX_FN_KEYS = 12;
    private final int REMCONS_MAX_KBD_LAYOUT = 17;
    public JFrame dispFrame;
    public JMenuItem vdMenuItemCrImage;
    public JPanel mainPanel;
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
    public int dheight;
    public int dwidth;
    public jsonparser jsonObj = new jsonparser(this);
    public remcons remconsObj = new remcons(this);
    public virtdevs virtdevsObj = new virtdevs(this);

    /* ILO3RemCon addition */
    public intgapp(String hostname) {
        this.hostName = hostname;
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
        String jSONRequest = this.jsonObj.getJSONRequest("rc_info");
        ApplyRcInfoParameters(jSONRequest);
        System.out.println("Completed Retrieving parameters from ILO");
        this.virtdevsObj.init();
        this.remconsObj.init();
        ui_init();
        if (null == jSONRequest) {
            System.out.println("Failed to retrive parameters from ILO");
            new VErrorDialog(this.dispFrame, locinfo.DIALOGSTR_2014, this.rcErrMessage, true);
            this.dispFrame.setVisible(false);
        }
    }

    public void start() {
        try {
            this.virtdevsObj.start();
            this.remconsObj.start();
            this.dispFrame.getContentPane().add(this.scroller, "Center");
            this.dispFrame.getContentPane().add(this.dispStatusBar, "South");
            this.scroller.validate();
            this.dispStatusBar.validate();
            this.dispFrame.validate();
            System.out.println("Set Inital focus for session..");
            this.remconsObj.session.requestFocus();
            run();
        } catch (Exception e) {
            System.out.println("FAILURE: exception starting applet");
            e.printStackTrace();
        }
    }

    public void stop() {
        this.exit = true;
        this.virtdevsObj.stop();
        this.remconsObj.remconsUnInstallKeyboardHook();
        this.remconsObj.stop();
    }

    public void destroy() {
        System.out.println("Destroying subsustems");
        this.exit = true;
        this.remconsObj.remconsUnInstallKeyboardHook();
        this.virtdevsObj.destroy();
        this.remconsObj.destroy();
    }

    @Override
    public synchronized void run() {
        while (true) {
            try {
                int i = 0;
                int i2 = 0;
                this.ma = new MediaAccess();
                String[] devices = this.ma.devices();
                for (int i3 = 0; devices != null && i3 < devices.length; i3++) {
                    int devtype = this.ma.devtype(devices[i3]);
                    if (devtype == 2 || devtype == 5) {
                        i2++;
                    }
                }
                if (i2 > this.vdmenuIndx - 4) {
                    ClassLoader classLoader = getClass().getClassLoader();
                    int i4 = 0;
                    while (true) {
                        if (devices == null || i4 >= devices.length) {
                            break;
                        }
                        boolean z = false;
                        int devtype2 = this.ma.devtype(devices[i4]);
                        for (int i5 = 0; i5 < this.vdmenuIndx - 4; i5++) {
                            if (devices[i4].equals(this.vdMenu.getItem(i5).getText())) {
                                z = true;
                                i++;
                            }
                        }
                        if (!z) {
                            if (devtype2 == 2) {
                                System.out.println("Device attached: " + devices[i4]);
                                this.vdMenuItems[this.vdmenuIndx] = new JCheckBoxMenuItem(devices[i4]);
                                this.vdMenuItems[this.vdmenuIndx].setActionCommand("fd" + devices[i4]);
                                this.vdMenuItems[this.vdmenuIndx].addItemListener(this);
                                if (devices[i4].equals("A:") || devices[i4].equals("B:")) {
                                    this.vdMenuItems[this.vdmenuIndx].setIcon(new ImageIcon(getImage(classLoader.getResource("com/hp/ilo2/remcons/images/FloppyDisk.png"))));
                                } else {
                                    this.vdMenuItems[this.vdmenuIndx].setIcon(new ImageIcon(getImage(classLoader.getResource("com/hp/ilo2/remcons/images/usb.png"))));
                                }
                                this.vdMenu.add(this.vdMenuItems[this.vdmenuIndx], i);
                                this.vdMenu.updateUI();
                                this.vdmenuIndx++;
                            } else if (devtype2 == 5) {
                                System.out.println("CDROM Hot plug device auto-update no supported at this time");
                            }
                        }
                        i4++;
                    }
                } else if (i2 < this.vdmenuIndx - 4) {
                    int i6 = 0;
                    while (true) {
                        if (i6 >= this.vdmenuIndx - 4) {
                            break;
                        }
                        boolean z2 = false;
                        for (int i7 = 0; devices != null && i7 < devices.length; i7++) {
                            int devtype3 = this.ma.devtype(devices[i7]);
                            if ((devtype3 == 2 || devtype3 == 5) && this.vdMenu.getItem(i6).getText().equals(devices[i7])) {
                                z2 = true;
                            }
                        }
                        if (!z2) {
                            System.out.println("Device removed: " + this.vdMenu.getItem(i6).getText());
                            this.vdMenu.remove(i6);
                            this.vdMenu.updateUI();
                            this.vdmenuIndx--;
                            break;
                        }
                        i6++;
                    }
                }
                this.ma = null;
                this.remconsObj.session.set_status(3, "");
                this.remconsObj.sleepAtLeast(5000L);
            } catch (InterruptedException e) {
                System.out.println("Exception on intgapp");
            }
            if (this.exit) {
                System.out.println("Intgapp stopped running");
                return;
            }
        }
    }

    public void paintComponent(Graphics graphics) {
        intgapp.super.paintComponents(graphics);
        graphics.drawString("Remote Console JApplet Loaded", 10, 50);
    }

    public void ui_init() {
        System.out.println("Message from ui_init55");
        this.dispFrame = new JFrame("JavaApplet IRC Window");
        this.dispFrame.getContentPane().setLayout(new BorderLayout());
        this.dispFrame.addWindowListener(new WindowCloser(this));
        this.mainMenuBar = new JMenuBar();
        this.dispStatusBar = new JPanel(new BorderLayout());
        this.dispStatusBar.add(this.remconsObj.session.status_box, "West");
        this.dispStatusBar.add(this.remconsObj.pwrStatusPanel, "East");
        String jSONRequest = this.jsonObj.getJSONRequest("session_info");
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        this.dispFrame.setJMenuBar(this.mainMenuBar);
        makePsMenu(this.mainMenuBar, this.jsonObj.getJSONNumber(jSONRequest, "reset_priv"));
        makeVdMenu(this.mainMenuBar, this.jsonObj.getJSONNumber(jSONRequest, "virtual_media_priv"));
        makeKbMenu(this.mainMenuBar);
        makeHlpMenu(this.mainMenuBar);
        this.scroller = new JScrollPane(this.remconsObj.session, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.scroller.setVisible(true);
        try {
            String stringBuffer = locinfo.MENUSTR_1024 + " " + this.server_name + " " + locinfo.MENUSTR_1025 + " " + this.ilo_fqdn;
            if (this.blade == 1 && this.in_enclosure) {
                stringBuffer = stringBuffer + " " + locinfo.MENUSTR_1026 + " " + this.enclosure + " " + locinfo.MENUSTR_1027 + " " + this.bay;
            }
            this.dispFrame.setTitle(stringBuffer);
        } catch (Exception e) {
            this.dispFrame.setTitle(locinfo.MENUSTR_1024 + " " + getCodeBase().getHost());
            System.out.println("IRC title not available");
        }
        int i = Toolkit.getDefaultToolkit().getScreenSize().width;
        int i2 = Toolkit.getDefaultToolkit().getScreenSize().height;
        int i3 = Math.min(i, 1054);
        int i4 = i2 < 874 ? i2 - 30 : 874;
        int i5 = i > 1054 ? (i - 1054) / 2 : 0;
        int i6 = i2 > 874 ? (i2 - 874) / 2 : 0;
        this.dispFrame.setSize(i3, i4);
        this.dispFrame.setLocation(i5, i6);
        System.out.println("check dimensions " + i3 + " " + i4 + " " + i5 + " " + i6);
        this.dispFrame.setVisible(true);
        try {
            this.dispFrame.getInsets();
            this.dispFrame.setIconImage(getImage(getClass().getClassLoader().getResource("com/hp/ilo2/remcons/images/hp_logo.png")));
            if (this.dispFrame.getIconImage() == null) {
                System.out.println("Dimage is null");
            }
        } catch (Exception e2) {
            System.out.println("JIRC icon not available");
        }
    }

    protected void makeHlpMenu(JMenuBar jMenuBar) {
        this.hlpMenu = new JMenu(locinfo.MENUSTR_1028);
        this.aboutJirc = new JMenuItem(locinfo.MENUSTR_1029);
        this.aboutJirc.addActionListener(this);
        this.hlpMenu.add(this.aboutJirc);
        jMenuBar.add(this.hlpMenu);
    }

    protected void makeVdMenu(JMenuBar jMenuBar, int i) {
        this.vdMenu = new JMenu(locinfo.MENUSTR_1002);
        if (i == 1) {
            jMenuBar.add(this.vdMenu);
        }
    }

    public void updateVdMenu() {
        this.ma = new MediaAccess();
        ClassLoader classLoader = getClass().getClassLoader();
        String jSONRequest = this.jsonObj.getJSONRequest("vm_status");
        String jSONArray = this.jsonObj.getJSONArray(jSONRequest, "options", 0);
        String jSONArray2 = this.jsonObj.getJSONArray(jSONRequest, "options", 1);
        String[] devices = this.ma.devices();
        this.vdmenuIndx = 0;
        if (devices != null) {
            this.vdMenuItems = new JCheckBoxMenuItem[devices.length + 5];
            for (String device : devices) {
                int devtype = this.ma.devtype(device);
                if (devtype == 5) {
                    this.vdMenuItems[this.vdmenuIndx] = new JCheckBoxMenuItem(device);
                    this.vdMenuItems[this.vdmenuIndx].setActionCommand("cd" + device);
                    this.vdMenuItems[this.vdmenuIndx].addItemListener(this);
                    this.vdMenuItems[this.vdmenuIndx].setIcon(new ImageIcon(getImage(classLoader.getResource("com/hp/ilo2/remcons/images/CD_Drive.png"))));
                    this.vdMenu.add(this.vdMenuItems[this.vdmenuIndx]);
                    this.vdmenuIndx++;
                } else if (devtype == 2) {
                    this.vdMenuItems[this.vdmenuIndx] = new JCheckBoxMenuItem(device);
                    this.vdMenuItems[this.vdmenuIndx].setActionCommand("fd" + device);
                    this.vdMenuItems[this.vdmenuIndx].addItemListener(this);
                    if (device.equals("A:") || device.equals("B:")) {
                        this.vdMenuItems[this.vdmenuIndx].setIcon(new ImageIcon(getImage(classLoader.getResource("com/hp/ilo2/remcons/images/FloppyDisk.png"))));
                    } else {
                        this.vdMenuItems[this.vdmenuIndx].setIcon(new ImageIcon(getImage(classLoader.getResource("com/hp/ilo2/remcons/images/usb.png"))));
                    }
                    this.vdMenu.add(this.vdMenuItems[this.vdmenuIndx]);
                    this.vdmenuIndx++;
                }
            }
        } else {
            this.vdMenuItems = new JCheckBoxMenuItem[5];
            System.out.println("Media Access not available...");
        }
        this.ma = null;
        this.vdMenuItems[this.vdmenuIndx] = new JCheckBoxMenuItem(locinfo.MENUSTR_1022 + " " + locinfo.MENUSTR_100A);
        this.vdMenuItems[this.vdmenuIndx].setActionCommand("fd" + locinfo.STATUSSTR_3117);
        this.vdMenuItems[this.vdmenuIndx].addItemListener(this);
        this.vdMenuItems[this.vdmenuIndx].setIcon(new ImageIcon(getImage(classLoader.getResource("com/hp/ilo2/remcons/images/Image_File.png"))));
        this.vdMenu.add(this.vdMenuItems[this.vdmenuIndx]);
        this.vdmenuIndx++;
        this.vdMenuItems[this.vdmenuIndx] = new JCheckBoxMenuItem(locinfo.MENUSTR_1023 + locinfo.MENUSTR_100A);
        this.vdMenuItems[this.vdmenuIndx].setActionCommand("FLOPPY");
        this.vdMenuItems[this.vdmenuIndx].setIcon(new ImageIcon(getImage(classLoader.getResource("com/hp/ilo2/remcons/images/Network.png"))));
        this.vdMenu.add(this.vdMenuItems[this.vdmenuIndx]);
        this.vdMenuItems[this.vdmenuIndx].addItemListener(this);
        this.vdmenuIndx++;
        if (this.jsonObj.getJSONNumber(jSONArray, "vm_url_connected") == 1 && this.jsonObj.getJSONNumber(jSONArray, "vm_connected") == 1) {
            this.fdSelected = true;
            lockFdMenu(false, "URL Removable Media");
        }
        this.vdMenuItems[this.vdmenuIndx] = new JCheckBoxMenuItem(locinfo.MENUSTR_1022 + " " + locinfo.MENUSTR_100B);
        this.vdMenuItems[this.vdmenuIndx].setActionCommand("cd" + locinfo.STATUSSTR_3117);
        this.vdMenuItems[this.vdmenuIndx].addItemListener(this);
        this.vdMenuItems[this.vdmenuIndx].setIcon(new ImageIcon(getImage(classLoader.getResource("com/hp/ilo2/remcons/images/Image_File.png"))));
        this.vdMenu.add(this.vdMenuItems[this.vdmenuIndx]);
        this.vdmenuIndx++;
        this.vdMenuItems[this.vdmenuIndx] = new JCheckBoxMenuItem(locinfo.MENUSTR_1023 + locinfo.MENUSTR_100B);
        this.vdMenuItems[this.vdmenuIndx].setActionCommand("CDROM");
        this.vdMenuItems[this.vdmenuIndx].setIcon(new ImageIcon(getImage(classLoader.getResource("com/hp/ilo2/remcons/images/Network.png"))));
        this.vdMenu.add(this.vdMenuItems[this.vdmenuIndx]);
        this.vdMenuItems[this.vdmenuIndx].addItemListener(this);
        this.vdmenuIndx++;
        if (this.jsonObj.getJSONNumber(jSONArray2, "vm_url_connected") == 1 && this.jsonObj.getJSONNumber(jSONArray2, "vm_connected") == 1) {
            this.cdSelected = true;
            lockCdMenu(false, "URL CD/DVD-ROM");
        }
        this.vdMenu.addSeparator();
        this.vdMenuItemCrImage = new JMenuItem(locinfo.MENUSTR_100D);
        this.vdMenuItemCrImage.setActionCommand("CreateDiskImage");
        this.vdMenuItemCrImage.addActionListener(this);
        this.vdMenu.add(this.vdMenuItemCrImage);
    }

    public void lockCdMenu(boolean z, String str) {
        for (int i = 0; i < this.vdmenuIndx; i++) {
            this.vdMenu.getItem(i).removeItemListener(this);
            if (this.vdMenu.getItem(i).getActionCommand().startsWith("cd") || this.vdMenu.getItem(i).getActionCommand().equals("CDROM")) {
                if (str.equals(this.vdMenu.getItem(i).getText())) {
                    this.vdMenu.getItem(i).setSelected(!z);
                } else {
                    this.vdMenu.getItem(i).setSelected(false);
                    this.vdMenu.getItem(i).setEnabled(z);
                }
            }
            this.vdMenu.getItem(i).addItemListener(this);
        }
    }

    public void lockFdMenu(boolean z, String str) {
        for (int i = 0; i < this.vdmenuIndx; i++) {
            this.vdMenu.getItem(i).removeItemListener(this);
            if (this.vdMenu.getItem(i).getActionCommand().startsWith("fd") || this.vdMenu.getItem(i).getActionCommand().equals("FLOPPY")) {
                if (str.equals(this.vdMenu.getItem(i).getText())) {
                    this.vdMenu.getItem(i).setSelected(!z);
                } else {
                    this.vdMenu.getItem(i).setSelected(false);
                    this.vdMenu.getItem(i).setEnabled(z);
                }
            }
            this.vdMenu.getItem(i).addItemListener(this);
        }
    }

    protected void makePsMenu(JMenuBar jMenuBar, int i) {
        ClassLoader classLoader = getClass().getClassLoader();
        this.psMenu = new JMenu(locinfo.MENUSTR_1001);
        this.momPress = new JMenuItem(locinfo.MENUSTR_1004);
        this.momPress.setIcon(new ImageIcon(getImage(classLoader.getResource("com/hp/ilo2/remcons/images/press.png"))));
        this.momPress.setActionCommand("psMomPress");
        this.momPress.addActionListener(this);
        this.psMenu.add(this.momPress);
        this.pressHold = new JMenuItem(locinfo.MENUSTR_1005);
        this.pressHold.setIcon(new ImageIcon(getImage(classLoader.getResource("com/hp/ilo2/remcons/images/hold.png"))));
        this.pressHold.setActionCommand("psPressHold");
        this.pressHold.addActionListener(this);
        this.powerCycle = new JMenuItem(locinfo.MENUSTR_1006);
        this.powerCycle.setIcon(new ImageIcon(getImage(classLoader.getResource("com/hp/ilo2/remcons/images/coldboot.png"))));
        this.powerCycle.setActionCommand("psPowerCycle");
        this.powerCycle.addActionListener(this);
        this.sysReset = new JMenuItem(locinfo.MENUSTR_1007);
        this.sysReset.setIcon(new ImageIcon(getImage(classLoader.getResource("com/hp/ilo2/remcons/images/reset.png"))));
        this.sysReset.setActionCommand("psSysReset");
        this.sysReset.addActionListener(this);
        if (i == 1) {
            jMenuBar.add(this.psMenu);
        }
    }

    public void updatePsMenu(int i) {
        if (0 == i) {
            this.psMenu.remove(this.pressHold);
            this.psMenu.remove(this.powerCycle);
            this.psMenu.remove(this.sysReset);
            return;
        }
        this.psMenu.remove(this.pressHold);
        this.psMenu.remove(this.powerCycle);
        this.psMenu.remove(this.sysReset);
        this.psMenu.add(this.pressHold);
        this.psMenu.add(this.powerCycle);
        this.psMenu.add(this.sysReset);
    }

    protected void makeKbMenu(JMenuBar jMenuBar) {
        ClassLoader classLoader = getClass().getClassLoader();
        this.kbMenu = new JMenu(locinfo.MENUSTR_1003);
        this.kbCAFMenu = new JMenu("CTRL-ALT-Fn");
        this.kbAFMenu = new JMenu("ALT-Fn");
        this.kbLangMenu = new JMenu(locinfo.MENUSTR_100E);
        this.ctlAltDel = new JMenuItem(locinfo.MENUSTR_1008);
        this.ctlAltDel.setIcon(new ImageIcon(getImage(classLoader.getResource("com/hp/ilo2/remcons/images/Keyboard.png"))));
        this.ctlAltDel.setActionCommand("kbCtlAltDel");
        this.ctlAltDel.addActionListener(this);
        this.kbMenu.add(this.ctlAltDel);
        this.numLock = new JMenuItem(locinfo.MENUSTR_1009);
        this.numLock.setIcon(new ImageIcon(getImage(classLoader.getResource("com/hp/ilo2/remcons/images/Keyboard.png"))));
        this.numLock.setActionCommand("kbNumLock");
        this.numLock.addActionListener(this);
        this.kbMenu.add(this.numLock);
        this.capsLock = new JMenuItem(locinfo.MENUSTR_1020);
        this.capsLock.setIcon(new ImageIcon(getImage(classLoader.getResource("com/hp/ilo2/remcons/images/Keyboard.png"))));
        this.capsLock.setActionCommand("kbCapsLock");
        this.capsLock.addActionListener(this);
        this.kbMenu.add(this.capsLock);
        this.ctlAltBack = new JMenuItem("CTRL-ALT-BACKSPACE");
        this.ctlAltBack.setIcon(new ImageIcon(getImage(classLoader.getResource("com/hp/ilo2/remcons/images/Keyboard.png"))));
        this.ctlAltBack.setActionCommand("kbCtlAltBack");
        this.ctlAltBack.addActionListener(this);
        this.ctlAltFn = new JMenuItem[this.REMCONS_MAX_FN_KEYS];
        for (int i = 0; i < this.REMCONS_MAX_FN_KEYS; i++) {
            this.ctlAltFn[i] = new JMenuItem("CTRL-ALT-F" + (i + 1));
            this.ctlAltFn[i].setActionCommand("kbCtrlAltFn" + i);
            this.ctlAltFn[i].addActionListener(this);
            this.kbCAFMenu.add(this.ctlAltFn[i]);
        }
        this.AltFn = new JMenuItem[this.REMCONS_MAX_FN_KEYS];
        for (int i2 = 0; i2 < this.REMCONS_MAX_FN_KEYS; i2++) {
            this.AltFn[i2] = new JMenuItem("ALT-F" + (i2 + 1));
            this.AltFn[i2].setActionCommand("kbAltFn" + i2);
            this.AltFn[i2].addActionListener(this);
            this.kbAFMenu.add(this.AltFn[i2]);
        }
        this.localKbdLayout = new JCheckBoxMenuItem[this.REMCONS_MAX_KBD_LAYOUT];
        for (int i3 = 0; i3 < this.REMCONS_MAX_KBD_LAYOUT; i3++) {
            this.localKbdLayout[i3] = new JCheckBoxMenuItem(locinfo.MENUSTR_100F + i3);
            this.localKbdLayout[i3].setActionCommand("localKbdLayout" + i3);
            this.localKbdLayout[i3].addItemListener(this);
            this.kbLangMenu.add(this.localKbdLayout[i3]);
        }
        this.localKbdLayout[0].setSelected(true);
        if (!System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            this.kbMenu.add(this.ctlAltBack);
            this.kbMenu.add(this.kbCAFMenu);
            this.kbMenu.add(this.kbAFMenu);
            this.kbMenu.add(this.kbLangMenu);
        }
        this.kbMenu.addSeparator();
        this.hotKeys = new JMenuItem(locinfo.MENUSTR_1021);
        this.hotKeys.addActionListener(this);
        this.kbMenu.add(this.hotKeys);
        jMenuBar.add(this.kbMenu);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == this.momPress) {
            this.remconsObj.session.sendMomPress();
        } else if (actionEvent.getSource() == this.pressHold) {
            this.remconsObj.session.sendPressHold();
        } else if (actionEvent.getSource() == this.powerCycle) {
            this.remconsObj.session.sendPowerCycle();
        } else if (actionEvent.getSource() == this.sysReset) {
            this.remconsObj.session.sendSystemReset();
        } else if (actionEvent.getSource() == this.ctlAltDel) {
            this.remconsObj.session.send_ctrl_alt_del();
        } else if (actionEvent.getSource() == this.numLock) {
            this.remconsObj.session.send_num_lock();
        } else if (actionEvent.getSource() == this.capsLock) {
            this.remconsObj.session.send_caps_lock();
        } else if (actionEvent.getSource() == this.ctlAltBack) {
            this.remconsObj.session.send_ctrl_alt_back();
        } else if (actionEvent.getSource() == this.hotKeys) {
            this.remconsObj.viewHotKeys();
        } else if (actionEvent.getSource() == this.vdMenuItemCrImage) {
            this.virtdevsObj.createImage();
        } else if (actionEvent.getSource() == this.aboutJirc) {
            this.remconsObj.viewAboutJirc();
        } else {
            int i = 0;
            while (true) {
                if (i >= this.REMCONS_MAX_FN_KEYS) {
                    break;
                } else if (actionEvent.getSource() == this.ctlAltFn[i]) {
                    this.remconsObj.session.send_ctrl_alt_fn(i);
                    break;
                } else if (actionEvent.getSource() == this.AltFn[i]) {
                    this.remconsObj.session.send_alt_fn(i);
                    break;
                } else {
                    i++;
                }
            }
            if (i >= this.REMCONS_MAX_FN_KEYS) {
                System.out.println("Unhandled ActionItem" + actionEvent.getActionCommand());
            }
        }
    }

    public void itemStateChanged(ItemEvent itemEvent) {
        AbstractButton abstractButton = null;
        String str = null;
        String str2 = null;
        int stateChange = itemEvent.getStateChange();
        for (int i = 0; i < this.REMCONS_MAX_KBD_LAYOUT; i++) {
            if (this.localKbdLayout[i] == itemEvent.getSource() && stateChange == 1) {
                System.out.println(i);
                this.localKbdLayout[i].setSelected(true);
                kbdLayoutMenuHandler(i);
                return;
            }
        }
        int i2 = 0;
        while (true) {
            if (i2 >= this.vdmenuIndx) {
                break;
            } else if (this.vdMenuItems[i2] == itemEvent.getSource()) {
                abstractButton = this.vdMenuItems[i2];
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
                this.virtdevsObj.do_floppy(str2);
                lockFdMenu(true, str2);
            } else if (stateChange == 1) {
                this.dispFrame.setVisible(false);
                String string = new VFileDialog(locinfo.DIALOGSTR_2045, "*.img").getString();
                this.dispFrame.setVisible(true);
                if (string != null) {
                    if (this.virtdevsObj.fdThread != null) {
                        this.virtdevsObj.change_disk(this.virtdevsObj.fdConnection, string);
                    }
                    System.out.println("Image file: " + string);
                    lockFdMenu(!this.virtdevsObj.do_floppy(string), str2);
                } else {
                    lockFdMenu(true, str2);
                }
            }
        } else if (str.equals("cd" + locinfo.STATUSSTR_3117)) {
            if (stateChange == 2) {
                this.virtdevsObj.do_cdrom(str2);
                lockCdMenu(true, str2);
            } else if (stateChange == 1) {
                this.dispFrame.setVisible(false);
                String string2 = new VFileDialog(locinfo.DIALOGSTR_2045, "*.iso").getString();
                this.dispFrame.setVisible(true);
                if (string2 != null) {
                    if (this.virtdevsObj.cdThread != null) {
                        this.virtdevsObj.change_disk(this.virtdevsObj.cdConnection, string2);
                    }
                    System.out.println("Image file: " + string2);
                    lockCdMenu(!this.virtdevsObj.do_cdrom(string2), str2);
                } else {
                    lockCdMenu(true, str2);
                }
            }
        } else if (str.startsWith("cd")) {
            if (this.virtdevsObj.do_cdrom(str2)) {
                lockCdMenu(stateChange != 1, str2);
            }
        } else if (str.startsWith("fd")) {
            if (this.virtdevsObj.do_floppy(str2)) {
                lockFdMenu(stateChange != 1, str2);
            }
        } else if (str.equals("FLOPPY") || str.equals("CDROM")) {
            boolean z = false;
            if (stateChange == 2) {
                this.jsonObj.postJSONRequest("vm_status", "{\"method\":\"set_virtual_media_options\", \"device\":\"" + str + "\", \"command\":\"EJECT\", \"session_key\":\"" + getParameter("RCINFO1") + "\"}");
                this.remconsObj.session.set_status(3, "Unmounted URL");
            } else if (stateChange == 1) {
                this.remconsObj.setDialogIsOpen(true);
                String userInput = new URLDialog(this.remconsObj).getUserInput();
                if (userInput.compareTo("userhitcancel") == 0 || userInput.compareTo("userhitclose") == 0) {
                    userInput = null;
                }
                if (userInput != null) {
                    userInput = userInput.replaceAll("[\\u0000-\u001F]", "");
                    System.out.println("url:  " + userInput);
                }
                this.remconsObj.setDialogIsOpen(false);
                if (userInput != null) {
                    String postJSONRequest = this.jsonObj.postJSONRequest("vm_status", "{\"method\":\"set_virtual_media_options\", \"device\":\"" + str + "\", \"command\":\"INSERT\", \"url\":\"" + userInput + "\", \"session_key\":\"" + getParameter("RCINFO1") + "\"}");
                    if (Objects.equals(postJSONRequest, "Success")) {
                        postJSONRequest = this.jsonObj.postJSONRequest("vm_status", "{\"method\":\"set_virtual_media_options\", \"device\":\"" + str + "\", \"boot_option\":\"CONNECT\", \"command\":\"SET\", \"url\":\"" + userInput + "\", \"session_key\":\"" + getParameter("RCINFO1") + "\"}");
                    }
                    if (Objects.equals(postJSONRequest, "SCSI_ERR_NO_LICENSE")) {
                        new VErrorDialog(this.dispFrame, locinfo.DIALOGSTR_202c, "<html>" + locinfo.DIALOGSTR_2015 + " " + locinfo.DIALOGSTR_2016 + " " + locinfo.DIALOGSTR_202d + "<br><br>" + locinfo.DIALOGSTR_202e + "</html>", true);
                    } else if (!Objects.equals(postJSONRequest, "Success")) {
                        new VErrorDialog(this.dispFrame, locinfo.DIALOGSTR_2014, locinfo.DIALOGSTR_2064, true);
                    } else {
                        z = true;
                        this.remconsObj.session.set_status(3, locinfo.STATUSSTR_3125);
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
        for (int i2 = 0; i2 < this.REMCONS_MAX_KBD_LAYOUT; i2++) {
            if (i2 != i) {
                this.localKbdLayout[i2].setSelected(false);
            }
        }
        this.remconsObj.setLocalKbdLayout(i);
    }

    public static class WindowCloser extends WindowAdapter {
        private final intgapp this$0;

        WindowCloser(intgapp intgappVar) {
            this.this$0 = intgappVar;
        }

        public void windowClosing(WindowEvent windowEvent) {
            this.this$0.stop();
            this.this$0.exit = true;
        }
    }

    private void ApplyRcInfoParameters(String str) {
        this.vm_port = null;
        this.vm_key = null;
        this.rc_port = null;
        this.enc_key = null;
        Arrays.fill(this.enc_key_val, (byte) 0);
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
                this.enc_key = trim3;
                for (int i = 0; i < this.enc_key_val.length; i++) {
                    try {
                        this.enc_key_val[i] = (byte) Integer.parseInt(this.enc_key.substring(i * 2, (i * 2) + 2), 16);
                    } catch (NumberFormatException e) {
                        System.out.println("Failed to Parse enc_key");
                    }
                }
            } else if (substring.compareToIgnoreCase("rc_port") == 0) {
                System.out.println("rc_port:" + trim3);
                this.rc_port = trim3;
            } else if (substring.compareToIgnoreCase("vm_key") == 0) {
                this.vm_key = trim3;
            } else if (substring.compareToIgnoreCase("vm_port") == 0) {
                System.out.println("vm_port:" + trim3);
                this.vm_port = trim3;
            } else if (substring.equalsIgnoreCase("optional_features")) {
                System.out.println("optional_features:" + trim3);
                this.optional_features = trim3;
            } else if (substring.compareToIgnoreCase("server_name") == 0) {
                System.out.println("server_name:" + trim3);
                this.server_name = trim3;
            } else if (substring.compareToIgnoreCase("ilo_fqdn") == 0) {
                System.out.println("ilo_fqdn:" + trim3);
                this.ilo_fqdn = trim3;
            } else if (substring.compareToIgnoreCase("blade") == 0) {
                this.blade = Integer.parseInt(trim3);
                System.out.println("blade:" + this.blade);
            } else if (this.blade == 1 && substring.compareToIgnoreCase("enclosure") == 0) {
                if (!trim3.equals("null")) {
                    this.in_enclosure = true;
                    System.out.println("enclosure:" + trim3);
                    this.enclosure = trim3;
                }
            } else if (this.blade == 1 && substring.compareToIgnoreCase("bay") == 0) {
                this.bay = Integer.parseInt(trim3);
                System.out.println("bay:" + this.bay);
            }
        }
    }

    public void moveUItoInit(boolean z) {
        System.out.println("Disable Menus\n");
        this.psMenu.setEnabled(z);
        this.vdMenu.setEnabled(z);
        this.kbMenu.setEnabled(z);
    }
}
