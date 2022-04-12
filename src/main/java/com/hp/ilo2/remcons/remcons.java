package com.hp.ilo2.remcons;

import com.hp.ilo2.intgapp.intgapp;
import com.hp.ilo2.intgapp.locinfo;
import com.hp.ilo2.virtdevs.VErrorDialog;
import util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.io.*;
import java.util.Locale;
import java.util.Properties;

public class remcons extends JPanel implements TimerListener, Runnable {

    Image[] img;
    Thread locale_setter;
    boolean cdCachedConnState = false;
    boolean cdConnState = false;
    boolean fdCachedConnState = false;
    boolean fdConnState = false;
    private Image pwrEncImg;
    private Image pwrEncImgLock;
    private Image pwrEncImgUnlock;
    private Image pwrHealthImg;
    private Image pwrHealthImgGreen;
    private Image pwrHealthImgOff;
    private Image pwrHealthImgRed;
    private Image pwrHealthImgYellow;
    private Image pwrPowerImg;
    private Image pwrPowerImgOff;
    private Image pwrPowerImgOn;
    private Image vmActImg;
    private Image vmActImgOff;
    private Image vmActImgOn;
    private JLabel pwrEncLabel;
    private JPanel pwrEncCanvas;
    private JPanel pwrHealthCanvas;
    private JPanel pwrPowerCanvas;
    private JPanel vmActCanvas;
    private String login;
    private String session_ip = null;
    private Timer keyBoardTimer;
    private Timer timer;
    private boolean debug_msg = false;
    private final LocaleTranslator lt = new LocaleTranslator();
    private int localKbdLayoutId = 0;
    private int port_num = 23;
    private int session_timeout = SESSION_TIMEOUT_DEFAULT;
    private int terminalServicesPort = 3389;
    private static boolean dialogIsOpen = false;
    private static final int INFINITE_TIMEOUT = 2147483640;
    private static final int SESSION_TIMEOUT_DEFAULT = 900;
    public JPanel ledStatusPanel;
    public JPanel pwrStatusPanel;
    public KeyboardHook kHook = null;
    public boolean halfHeightCapable = false;
    public boolean kbHookAvailable = false;
    public boolean kbHookDataRcvd = false;
    public boolean kbHookInstalled = false;
    public boolean licensed = false;
    public boolean retry_connection_flag = false;
    public boolean session_encryption_enabled = false;
    public byte[] session_decrypt_key = new byte[16];
    public byte[] session_encrypt_key = new byte[16];
    public cim session;
    public cmd telnetObj;
    public int initialized = 0;
    public int keyData = 0;
    public int prevKeyData = 0;
    public int retry_connection_count = 0;
    public int session_key_index = 0;
    public int timeout_countdown;
    public int[] rndm_nums = new int[12];
    public intgapp ParentApp;
    public static Properties prop = new Properties();
    static final int ImageDone = 39;

    static {
        try {
            prop.load(
                    new FileInputStream(
                            System.getProperty("user.home")
                                    + System.getProperty("file.separator")
                                    + ".java" + System.getProperty("file.separator")
                                    + "hp.properties"
                    )
            );
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    public remcons(intgapp intgappVar) {
        ParentApp = intgappVar;
    }

    void waitImage(Image image, ImageObserver imageObserver) {
        int checkImage;
        long currentTimeMillis = System.currentTimeMillis();

        do {
            checkImage = checkImage(image, imageObserver);

            if ((checkImage & telnet.TELNET_ENCRYPT) == 0) {
                Thread.yield();

                if (System.currentTimeMillis() - currentTimeMillis > 2000) {
                    return;
                }
            } else {
                return;
            }
        } while ((checkImage & ImageDone) != ImageDone);
    }

    public void init() {
        img = new Image[22];
        img[0] = Utils.getResourceImage(ParentApp, "blank_cd.png");
        img[1] = Utils.getResourceImage(ParentApp, "blue.png");
        img[2] = Utils.getResourceImage(ParentApp, "CD_Drive.png");
        img[3] = Utils.getResourceImage(ParentApp, "FloppyDisk.png");
        img[4] = Utils.getResourceImage(ParentApp, "Folder.png");
        img[5] = Utils.getResourceImage(ParentApp, "green.png");
        img[6] = Utils.getResourceImage(ParentApp, "hold.png");
        img[7] = Utils.getResourceImage(ParentApp, "hp.jpg");
        img[8] = Utils.getResourceImage(ParentApp, "hp_logo.png");
        img[9] = Utils.getResourceImage(ParentApp, "hp_logo_blue_lgs.png");
        img[10] = null;
        img[11] = Utils.getResourceImage(ParentApp, "irc.png");
        img[12] = Utils.getResourceImage(ParentApp, "Keyboard.png");
        img[13] = Utils.getResourceImage(ParentApp, "off.png");
        img[14] = Utils.getResourceImage(ParentApp, "press.png");
        img[15] = Utils.getResourceImage(ParentApp, "ProtectFormHS.png");
        img[16] = Utils.getResourceImage(ParentApp, "pwr.png");
        img[17] = Utils.getResourceImage(ParentApp, "pwr_off.png");
        img[18] = Utils.getResourceImage(ParentApp, "red.png");
        img[19] = Utils.getResourceImage(ParentApp, "UnProtectFormHS.png");
        img[20] = Utils.getResourceImage(ParentApp, "Warning.png");
        img[21] = Utils.getResourceImage(ParentApp, "yellow.png");

        locale_setter = new Thread(this);
        locale_setter.start();

        init_params();
        String lowerCase = System.getProperty("os.name").toLowerCase();
        String property = System.getProperty("java.vm.name");
        String str = "unknown";

        if (lowerCase.startsWith("windows") || lowerCase.startsWith("linux")) {
            if (lowerCase.startsWith("windows")) {
                if (property.contains("64")) {
                    System.out.println("kbhookdll Detected win 64bit jvm");
                    str = "HpqKbHook-x86-win64";
                } else {
                    System.out.println("kbhookdll Detected win 32bit jvm");
                    str = "HpqKbHook-x86-win32";
                }
            } else if (lowerCase.startsWith("linux")) {
                if (property.contains("64")) {
                    System.out.println("kbhookdll Detected 64bit linux jvm");
                    str = "HpqKbHook-x86-linux-64";
                } else {
                    System.out.println("kbhookdll Detected 32bit linux jvm");
                    str = "HpqKbHook-x86-linux-32";
                }
            }

            if (ExtractKeyboardDll(str)) {
                kHook = new KeyboardHook(str);
                if (kHook == null) {
                    System.out.println("remcons: kHook = null, Failed to initialize and load kHook");
                } else {
                    kbHookAvailable = true;
                    kHook.clearKeymap();
                }
            } else {
                System.out.println("ExtractKeyboardDll() returns false");
            }
        }

        session = new cim(this);
        telnetObj = new cmd();

        if (session_encryption_enabled) {
            session.setup_encryption(session_encrypt_key, session_key_index);
            session.setup_decryption(session_decrypt_key);
        }

        for (int i = 0; i < 12; i++) {
            rndm_nums[i] = ((int) (Math.random() * 4.0d)) * 85;
        }

        if (debug_msg) {
            session.enable_debug();
        } else {
            session.disable_debug();
        }

        pwrStatusPanel = new JPanel(new BorderLayout());
        ledStatusPanel = new JPanel(new BorderLayout());
        pwrHealthImgGreen = img[5];
        prepareImage(pwrHealthImgGreen, ledStatusPanel);

        pwrHealthImgYellow = img[21];
        prepareImage(pwrHealthImgYellow, ledStatusPanel);

        pwrHealthImgRed = img[18];
        prepareImage(pwrHealthImgRed, ledStatusPanel);

        pwrHealthImgOff = img[13];
        prepareImage(pwrHealthImgOff, ledStatusPanel);

        pwrEncImgLock = img[15];
        prepareImage(pwrEncImgLock, ledStatusPanel);

        pwrEncImgUnlock = img[19];
        prepareImage(pwrEncImgUnlock, ledStatusPanel);

        vmActImgOn = img[1];
        prepareImage(vmActImgOn, ledStatusPanel);

        vmActImgOff = img[13];
        prepareImage(vmActImgOff, ledStatusPanel);

        pwrPowerImgOn = img[16];
        prepareImage(pwrPowerImgOn, ledStatusPanel);

        pwrPowerImgOff = img[17];
        prepareImage(pwrPowerImgOff, ledStatusPanel);

        pwrEncImg = pwrEncImgUnlock;
        pwrHealthImg = pwrHealthImgOff;
        vmActImg = vmActImgOff;
        pwrPowerImg = pwrPowerImgOff;
        JPanel jPanel = pwrStatusPanel;

        pwrEncCanvas = new JPanel() {
            public void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (pwrEncImg != null) {
                    waitImage(pwrEncImg, this);
                    graphics.drawImage(pwrEncImg, 1, 4, null);
                } else {
                    System.out.println("pwrEncCanvas Image not found");
                }
            }
        };
        jPanel.add(pwrEncCanvas, "West");
        setToolTipRecursively(pwrEncCanvas, locinfo.TOOLSTR_4003);
        pwrEncCanvas.setPreferredSize(new Dimension(20, 20));
        pwrEncCanvas.setVisible(true);

        JPanel jPanel3 = pwrStatusPanel;
        JLabel jLabel = new JLabel();
        pwrEncLabel = jLabel;
        jPanel3.add(jLabel);
        pwrEncLabel.setText("         ");
        pwrHealthCanvas = new JPanel() {
            public void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (pwrHealthImg != null) {
                    waitImage(pwrHealthImg, this);
                    graphics.drawImage(pwrHealthImg, 1, 4, null);
                } else {
                    System.out.println("pwrHealthCanvas Image not found");
                }
            }
        };

        ledStatusPanel.add(pwrHealthCanvas, "West");
        setToolTipRecursively(pwrHealthCanvas, locinfo.TOOLSTR_4002);
        pwrHealthCanvas.setPreferredSize(new Dimension(18, 25));
        pwrHealthCanvas.setVisible(true);

        vmActCanvas = new JPanel() {
            public void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (vmActImg != null) {
                    waitImage(vmActImg, this);
                    graphics.drawImage(vmActImg, 1, 4, null);
                } else {
                    System.out.println("vmActCanvas Image not found");
                }
            }
        };
        JPanel jPanel6 = ledStatusPanel;
        jPanel6.add(vmActCanvas, "Center");
        setToolTipRecursively(vmActCanvas, locinfo.TOOLSTR_4004);
        vmActCanvas.setPreferredSize(new Dimension(18, 25));
        vmActCanvas.setVisible(true);

        JPanel jPanel8 = ledStatusPanel;
        pwrPowerCanvas = new JPanel() {
            public void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (pwrPowerImg != null) {
                    waitImage(pwrPowerImg, this);
                    graphics.drawImage(pwrPowerImg, 1, 4, null);
                } else {
                    System.out.println("pwrPowerCanvas Image not found");
                }
            }
        };
        jPanel8.add(pwrPowerCanvas, "East");
        setToolTipRecursively(pwrPowerCanvas, locinfo.TOOLSTR_4001);
        pwrPowerCanvas.setPreferredSize(new Dimension(18, 25));
        pwrPowerCanvas.setVisible(true);

        pwrStatusPanel.add(ledStatusPanel, "East");

        session.enable_keyboard();

        if (kbHookAvailable) {
            int keyTimerTick = 20;

            keyBoardTimer = new Timer(keyTimerTick, false, session);
            keyBoardTimer.setListener(new keyBoardTimerListener(this), null);
            keyBoardTimer.start();

            System.out.println("Keyboard Hook available and timer started...");
        }

        initialized = 1;
    }

    public void start() {
        timeout_countdown = session_timeout;

        start_session();

        if (session_timeout == INFINITE_TIMEOUT) {
            System.out.println("Remote Console inactivity timeout = infinite.");
        } else {
            System.out.println("Remote Console inactivity timeout = " + session_timeout / 60 + " minutes.");
        }
    }


    public boolean ExtractKeyboardDll(String str) {
        boolean result;
        String tempDir = System.getProperty("java.io.tmpdir");
        String osName = System.getProperty("os.name").toLowerCase();
        String fileSeparator = System.getProperty("file.separator");

        if (osName.startsWith("windows") || osName.startsWith("linux")) {
            if (tempDir == null) {
                tempDir = osName.startsWith("windows") ? "C:\\TEMP" : "/tmp";
            }

            File file = new File(tempDir);
            if (!file.exists()) {
                file.mkdir();
            }

            if (!tempDir.endsWith(fileSeparator)) {
                tempDir = tempDir + fileSeparator;
            }

            String stringBuffer = tempDir + str + ".dll";
            System.out.println("checking for kbddll" + stringBuffer);

            if (new File(stringBuffer).exists()) {
                System.out.println(str + " already present ..");
                return true;
            }

            System.out.println("Extracting " + str + "...");
            byte[] bArr = new byte[4096];

            try {
                InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(str);
                FileOutputStream fileOutputStream = new FileOutputStream(stringBuffer);

                while (true) {
                    int read = resourceAsStream.read(bArr, 0, 4096);
                    if (read == -1) {
                        break;
                    }
                    fileOutputStream.write(bArr, 0, read);
                }

                System.out.println("Writing dll to " + stringBuffer + "complete");

                resourceAsStream.close();
                fileOutputStream.close();

                result = true;
            } catch (IOException e) {
                System.out.println("dllExtract: " + e);
                result = false;
            }
        } else {
            System.out.println("Cannot load keyboardHook DLL. Non Windows-Linux client system.");
            result = false;
        }

        return result;
    }

    public void stop() {
        if (locale_setter != null && locale_setter.isAlive()) {
            locale_setter.stop();
        }

        locale_setter = null;

        stop_session();

        System.out.println("Applet stopped...");
    }

    public void destroy() {
        System.out.println("Hiding applet.");

        if (isVisible()) {
            setVisible(false);
        }
    }

    @Override
    public void timeout(Object obj) {
        if (session.UI_dirty) {
            session.UI_dirty = false;
            timeout_countdown = session_timeout;

            return;
        }

        timeout_countdown -= 30;

        if (timeout_countdown <= 0 && System.getProperty("java.version", "0").compareTo("1.2") < 0) {
            stop_session();
        }
    }

    private void start_session() {
        int ts_param = 0;

        session.connect(session_ip, login, port_num, ts_param, terminalServicesPort, this);

        timer = new Timer(30000, false, session);
        timer.setListener(this, null);
        timer.start();
    }

    private void stop_session() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }

        session.disconnect();
    }

    public void setPwrStatusEnc(int i) {
        if (i == 0) {
            pwrEncImg = pwrEncImgUnlock;
        } else {
            pwrEncImg = pwrEncImgLock;
        }

        pwrEncCanvas.invalidate();
        pwrEncCanvas.repaint();
    }

    public void setPwrStatusEncLabel(String str) {
        pwrEncLabel.setText(str + "       ");
    }

    public void setPwrStatusHealth(int i) {
        switch (i) {
            case 0:
                pwrHealthImg = pwrHealthImgGreen;
                break;
            case 1:
                pwrHealthImg = pwrHealthImgYellow;
                break;
            case 2:
                pwrHealthImg = pwrHealthImgRed;
                break;
            default:
                pwrHealthImg = pwrHealthImgOff;
                break;
        }

        pwrHealthCanvas.invalidate();
        pwrHealthCanvas.repaint();
    }

    public void setPwrStatusPower(int i) {
        if (i == 0 && pwrPowerImgOff != pwrPowerImg) {
            pwrPowerImg = pwrPowerImgOff;

            ParentApp.updatePsMenu(i);
            pwrPowerCanvas.invalidate();
            pwrPowerCanvas.repaint();

            System.out.println("Moving Power to Off state");
        } else if (i != 0 && pwrPowerImgOn != pwrPowerImg) {
            pwrPowerImg = pwrPowerImgOn;

            ParentApp.updatePsMenu(i);
            pwrPowerCanvas.invalidate();
            pwrPowerCanvas.repaint();

            System.out.println("Moving Power to ON state");
        }
    }

    public void setvmAct(int i) {
        if (vmActImg == vmActImgOn || i == 0) {
            vmActImg = vmActImgOff;
            vmActCanvas.invalidate();
            vmActCanvas.repaint();
        } else if (vmActImg == vmActImgOff) {
            vmActImg = vmActImgOn;
            vmActCanvas.invalidate();
            vmActCanvas.repaint();
        }
    }

    public int seize_dialog(String str, String str2, int i) {
        System.out.println("seize dialog invoked" + i);
        return new VSeizeWaitDialog(this, str, str2, i).getUserInput();
    }

    public void seize_confirmed() {
        ParentApp.moveUItoInit(false);
        ParentApp.virtdevsObj.stop();
        remconsUnInstallKeyboardHook();
        ParentApp.dispFrame.setVisible(false);
        session.seize();
        ParentApp.stop();
    }

    public void shared(String str, String str2) {
        System.out.println("shared notification invoked");

        new VErrorDialog(ParentApp.dispFrame, locinfo.DIALOGSTR_2026, locinfo.DIALOGSTR_2027 + " " + str2 + "@" + str + locinfo.DIALOGSTR_2028, false);
    }

    public void unAuthorized(String str) {
        new VErrorDialog(ParentApp.dispFrame, locinfo.DIALOGSTR_2026, locinfo.DIALOGSTR_2029 + str + locinfo.DIALOGSTR_202a, false);

        if (str.contains("for remote console")) {
            session.unAuthAccess();
        }
    }

    public void firmwareUpgrade() {
        System.out.println("Firmware upgrade notification invoked");

        VErrorDialog vErrorDialog = new VErrorDialog(ParentApp.dispFrame, locinfo.DIALOGSTR_2026, locinfo.DIALOGSTR_202b, false);

        ParentApp.moveUItoInit(false);
        ParentApp.virtdevsObj.stop();
        session.fwUpgrade();
        ParentApp.stop();

        if (vErrorDialog.getBoolean()) {
            System.exit(0);
        }
    }

    @SuppressWarnings("unused")
    public void ack(byte b, byte b2, byte b3, byte b4) {
        if (b != 0) {
            return;
        }

        if (b2 == 1) {
            if (b4 == 1 && !ParentApp.fdSelected) {
                ParentApp.fdSelected = true;
                ParentApp.lockFdMenu(false, locinfo.MENUSTR_1023 + locinfo.MENUSTR_100A);
            } else if (b4 == 0 && ParentApp.fdSelected) {
                ParentApp.fdSelected = false;
                ParentApp.lockFdMenu(true, "");
            }
        } else if (b2 != 2) {
            /* no-op */
        } else {
            if (b4 == 1 && !ParentApp.cdSelected) {
                ParentApp.cdSelected = true;
                ParentApp.lockCdMenu(false, locinfo.MENUSTR_1023 + locinfo.MENUSTR_100B);
            } else if (b4 == 0 && ParentApp.cdSelected) {
                ParentApp.cdSelected = false;
                ParentApp.lockCdMenu(true, "");
            }
        }
    }

    protected void init_params() {
        debug_msg = true;
        login = null;
        port_num = 23;
        session_encryption_enabled = true;
        session_ip = ParentApp.getCodeBase().getHost();
        session_key_index = 0;
        session_timeout = SESSION_TIMEOUT_DEFAULT;
        terminalServicesPort = 0;

        if (!session_encryption_enabled) {
            session_decrypt_key = null;
            session_encrypt_key = null;
        } else if (null != ParentApp.enc_key) {
            System.arraycopy(ParentApp.enc_key_val, 0, session_decrypt_key, 0, session_decrypt_key.length);
            System.arraycopy(ParentApp.enc_key_val, 0, session_encrypt_key, 0, session_encrypt_key.length);
        }
    }

    public void paint(Graphics graphics) {
    }

    @Override
    public void run() {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows") && !lt.windows) {
            Locale.setDefault(Locale.US);
        }

        while (true) {
            if (retry_connection_flag && 3 >= retry_connection_count) {

                System.out.println("Retrying connection" + retry_connection_count);
                retry_connection_flag = false;
                retry_connection_count++;

                if (!fdCachedConnState) {
                    fdCachedConnState = fdConnState;
                }

                if (!cdCachedConnState) {
                    cdCachedConnState = cdConnState;
                }

                System.out.println("fd conn:" + fdConnState + " cd conn:" + cdConnState);
                System.out.println("fdcache:" + fdCachedConnState + " cdcache:" + cdCachedConnState);

                stop_session();

                try {
                    sleepAtLeast(5000L);
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted..");
                }

                ParentApp.jsonObj.getJSONRequest("rc_info");

                if (session_encryption_enabled && null != ParentApp.enc_key) {
                    System.arraycopy(ParentApp.enc_key_val, 0, session_decrypt_key, 0, session_decrypt_key.length);
                    System.arraycopy(ParentApp.enc_key_val, 0, session_encrypt_key, 0, session_encrypt_key.length);
                }

                session.setup_decryption(session_decrypt_key);
                session.setup_encryption(session_encrypt_key, session_key_index);

                start_session();

                try {
                    sleepAtLeast(2500L);
                } catch (InterruptedException e2) {
                    System.out.println("Thread interrupted..");
                }

                if (null == session.receiver || retry_connection_flag) {
                    retry_connection_flag = true;
                } else {
                    retry_connection_count = 0;
                }
            } else if (retry_connection_flag) {
                System.out.println("Retry connection  - video maximum attempts exhausted");

                stop_session();

                retry_connection_flag = false;
            } else {
                try {
                    sleepAtLeast(2500L);
                } catch (InterruptedException e3) {
                    System.out.println("Thread interrupted..");
                }
            }
        }
    }

    public void sleepAtLeast(long l) throws java.lang.InterruptedException {
        long l2 = System.currentTimeMillis();
        long l3 = l;
        while (l3 > 0L) {
            Thread.sleep(l3);
            long l4 = System.currentTimeMillis();
            l3 = l - (l4 - l2);
        }
    }

    public void setDialogIsOpen(boolean z) {
        dialogIsOpen = z;
    }

    public void SetLicensed(int i) {
        licensed = (i & 1) != 0;
        System.out.println("SetLicensed: " + licensed);
    }


    public void SetFlags(int i) {
        if ((i & 8) == 0) {
            halfHeightCapable = false;
            System.out.println("halfHeightCapable false");
            return;
        }
        halfHeightCapable = true;
        System.out.println("halfHeightCapable true");
    }

    public void UnlicensedShutdown() {
        String stringBuffer = "<html>" + locinfo.DIALOGSTR_2015 + " " + locinfo.DIALOGSTR_2017 + " " + locinfo.DIALOGSTR_202d + "<br><br>" + locinfo.DIALOGSTR_202e + "</html>";
        System.out.println("Unlicensed notification invoked");
        new VErrorDialog(ParentApp.dispFrame, locinfo.DIALOGSTR_202c, stringBuffer, true);
        ParentApp.moveUItoInit(false);
        ParentApp.stop();
    }

    public void resetShutdown() {
        new VErrorDialog(ParentApp.dispFrame, locinfo.MENUSTR_1007, locinfo.DIALOGSTR_2061, true);
        ParentApp.moveUItoInit(false);
        ParentApp.stop();
    }

    public void remconsInstallKeyboardHook() {
        String lowerCase = System.getProperty("os.name").toLowerCase();
        if (kHook == null) {
            System.out.println("remconsInstallKeyboardHook:KB Hook dll not loaded");
        } else if (!kbHookInstalled && kbHookAvailable && !dialogIsOpen) {
            kHook.clearKeymap();
            int InstallKeyboardHook = kHook.InstallKeyboardHook();

            if (lowerCase.startsWith("windows") || -1412584499 != InstallKeyboardHook) {
                kHook.setKeyboardLayoutId(InstallKeyboardHook);
                kbHookInstalled = true;
                keyData = 0;
                prevKeyData = 0;

                if (!lowerCase.startsWith("windows")) {
                    keyBoardTimer.start();
                    kHook.setLocalKbdLayout(localKbdLayoutId);

                    return;
                }

                return;
            }

            kbHookInstalled = false;
            keyBoardTimer.stop();

            System.out.println("remconsInstallKeyboardHook: KB Hook install failed");
        }
    }

    public void remconsUnInstallKeyboardHook() {
        if (kHook != null && kbHookInstalled && kbHookAvailable) {
            int UnInstallKeyboardHook = kHook.UnInstallKeyboardHook();

            if (UnInstallKeyboardHook == 0) {
                kbHookInstalled = false;
                keyData = 0;
                prevKeyData = 0;
                kHook.clearKeymap();

                return;
            }

            System.out.println("remconsUnInstallKeyboardHook: uninstall failed:" + UnInstallKeyboardHook);
        }
    }

    public void setLocalKbdLayout(int i) {
        if (kHook == null || !kbHookInstalled) {
            System.out.println("setKbdLayoutHandler: kHook not available. dbg caching..");
            localKbdLayoutId = i;

            return;
        }

        System.out.println("setKbdLayoutHandler: set Layout - " + i);

        kHook.setLocalKbdLayout(i);
    }


    public static class keyBoardTimerListener implements TimerListener {
        private final remcons listener;

        keyBoardTimerListener(remcons remconsVar) {
            listener = remconsVar;
        }

        @Override
        public synchronized void timeout(Object obj) {
            boolean z;
            boolean z2 = false;
            int i = 995;

            if (listener.kHook != null && listener.kbHookInstalled) {
                do {
                    listener.prevKeyData = listener.keyData;
                    listener.keyData = listener.kHook.GetKeyData();

                    if (!(listener.keyData == listener.prevKeyData || 0 == listener.keyData)) {
                        int i2 = (listener.keyData & 16711680) >> 16;
                        int i3 = (listener.keyData & 65280) >> 8;
                        int i4 = listener.keyData & telnet.TELNET_IAC;

                        if ((i2 & 144) == 144) {
                            z = true;
                        } else if ((i2 & 128) == 128) {
                            z2 = false;
                            z = false;
                        } else {
                            z2 = true;
                            z = false;
                        }

                        byte[] HandleHookKey = listener.kHook.HandleHookKey(i4, i3, z2, z);
                        if (listener.kHook.kcmdValid) {
                            if (!listener.kbHookDataRcvd) {
                                listener.kbHookDataRcvd = true;
                            }
                            listener.session.transmitb(HandleHookKey, HandleHookKey.length);
                        }

                        i = 0;
                    }

                    i++;
                } while (i < 1000);
            }
        }
    }

    public void setToolTipRecursively(JComponent jComponent, String str) {
        jComponent.setToolTipText(str);
    }

    public void viewHotKeys() {
        new hotKeysDialog(this);
    }

    public void viewAboutJirc() {
        new aboutJircDialog(this);
    }
}
