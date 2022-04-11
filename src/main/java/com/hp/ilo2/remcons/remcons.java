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
    private JFrame parent_frame;
    private JLabel pwrEncLabel;
    private JPanel pwrEncCanvas;
    private JPanel pwrHealthCanvas;
    private JPanel pwrPowerCanvas;
    private JPanel vmActCanvas;
    private final LocaleTranslator lt = new LocaleTranslator();
    private String login;
    private String rcErrMessage;
    private String session_ip = null;
    private String term_svcs_label = "Terminal Svcs";
    private Timer keyBoardTimer;
    private Timer timer;
    private boolean debug_msg = false;
    private boolean launchTerminalServices = false;
    private final boolean translate = false;
    private final int keyTimerTick = 20;
    private int localKbdLayoutId = 0;
    private int mouse_mode = 0;
    private int num_cursors = 0;
    private int port_num = 23;
    private int session_timeout = SESSION_TIMEOUT_DEFAULT;
    private int terminalServicesPort = 3389;
    private final int ts_param = 0;
    private static boolean dialogIsOpen = false;
    private static final char[] base64 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '>', 0, 0, 0, '?', '4', '5', '6', '7', '8', '9', ':', ';', '<', '=', 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, '\b', '\t', '\n', 11, '\f', '\r', 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 0, 0, 0, 0, 0, 0, 26, 27, 28, 29, 30, 31, ' ', '!', '\"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', '0', '1', '2', '3', 0, 0, 0, 0, 0};
    private static final int INFINITE_TIMEOUT = 2147483640;
    private static final int KEEP_ALIVE_INTERVAL = 30;
    private static final int LICENSE_RC = 1;
    private static final int REMCONS_MAX_FN_KEYS = 12;
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
    public static final int RETRY_CONNECTION_MAX = 3;
    static final int ImageDone = 39;

    static {
        try {
            prop.load(new FileInputStream(System.getProperty("user.home") + System.getProperty("file.separator") + ".java" + System.getProperty("file.separator") + "hp.properties"));
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    public remcons(intgapp intgappVar) {
        this.ParentApp = intgappVar;
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
        this.img = new Image[22];
        this.img[0] = Utils.getResourceImage(ParentApp, "blank_cd.png");
        this.img[1] = Utils.getResourceImage(ParentApp, "blue.png");
        this.img[2] = Utils.getResourceImage(ParentApp, "CD_Drive.png");
        this.img[3] = Utils.getResourceImage(ParentApp, "FloppyDisk.png");
        this.img[4] = Utils.getResourceImage(ParentApp, "Folder.png");
        this.img[5] = Utils.getResourceImage(ParentApp, "green.png");
        this.img[6] = Utils.getResourceImage(ParentApp, "hold.png");
        this.img[7] = Utils.getResourceImage(ParentApp, "hp.jpg");
        this.img[8] = Utils.getResourceImage(ParentApp, "hp_logo.png");
        this.img[9] = Utils.getResourceImage(ParentApp, "hp_logo_blue_lgs.png");
        this.img[10] = null;
        this.img[11] = Utils.getResourceImage(ParentApp, "irc.png");
        this.img[12] = Utils.getResourceImage(ParentApp, "Keyboard.png");
        this.img[13] = Utils.getResourceImage(ParentApp, "off.png");
        this.img[14] = Utils.getResourceImage(ParentApp, "press.png");
        this.img[15] = Utils.getResourceImage(ParentApp, "ProtectFormHS.png");
        this.img[16] = Utils.getResourceImage(ParentApp, "pwr.png");
        this.img[17] = Utils.getResourceImage(ParentApp, "pwr_off.png");
        this.img[18] = Utils.getResourceImage(ParentApp, "red.png");
        this.img[19] = Utils.getResourceImage(ParentApp, "UnProtectFormHS.png");
        this.img[20] = Utils.getResourceImage(ParentApp, "Warning.png");
        this.img[21] = Utils.getResourceImage(ParentApp, "yellow.png");
        this.locale_setter = new Thread(this);
        this.locale_setter.start();
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
                this.kHook = new KeyboardHook(str);
                if (this.kHook == null) {
                    System.out.println("remcons: kHook = null, Failed to initialize and load kHook");
                } else {
                    this.kbHookAvailable = true;
                    this.kHook.clearKeymap();
                }
            } else {
                System.out.println("ExtractKeyboardDll() returns false");
            }

        }
        this.session = new cim(this);
        this.telnetObj = new cmd();
        if (this.session_encryption_enabled) {
            this.session.setup_encryption(this.session_encrypt_key, this.session_key_index);
            this.session.setup_decryption(this.session_decrypt_key);
        }
        this.session.set_mouse_protocol(this.mouse_mode);
        for (int i = 0; i < 12; i++) {
            this.rndm_nums[i] = ((int) (Math.random() * 4.0d)) * 85;
        }
        this.session.set_sig_colors(this.rndm_nums);
        if (this.debug_msg) {
            this.session.enable_debug();
        } else {
            this.session.disable_debug();
        }
        this.pwrStatusPanel = new JPanel(new BorderLayout());
        this.ledStatusPanel = new JPanel(new BorderLayout());
        this.pwrHealthImgGreen = this.img[5];
        prepareImage(this.pwrHealthImgGreen, this.ledStatusPanel);
        this.pwrHealthImgYellow = this.img[21];
        prepareImage(this.pwrHealthImgYellow, this.ledStatusPanel);
        this.pwrHealthImgRed = this.img[18];
        prepareImage(this.pwrHealthImgRed, this.ledStatusPanel);
        this.pwrHealthImgOff = this.img[13];
        prepareImage(this.pwrHealthImgOff, this.ledStatusPanel);
        this.pwrEncImgLock = this.img[15];
        prepareImage(this.pwrEncImgLock, this.ledStatusPanel);
        this.pwrEncImgUnlock = this.img[19];
        prepareImage(this.pwrEncImgUnlock, this.ledStatusPanel);
        this.pwrEncImg = this.pwrEncImgUnlock;
        this.pwrHealthImg = this.pwrHealthImgOff;
        this.vmActImgOn = this.img[1];
        prepareImage(this.vmActImgOn, this.ledStatusPanel);
        this.vmActImgOff = this.img[13];
        prepareImage(this.vmActImgOff, this.ledStatusPanel);
        this.pwrPowerImgOn = this.img[16];
        prepareImage(this.pwrPowerImgOn, this.ledStatusPanel);
        this.pwrPowerImgOff = this.img[17];
        prepareImage(this.pwrPowerImgOff, this.ledStatusPanel);
        this.vmActImg = this.vmActImgOff;
        this.pwrPowerImg = this.pwrPowerImgOff;
        JPanel jPanel = this.pwrStatusPanel;
        this.pwrEncCanvas = new JPanel() {

            public void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (remcons.this.pwrEncImg != null) {
                    remcons.this.waitImage(remcons.this.pwrEncImg, this);
                    graphics.drawImage(remcons.this.pwrEncImg, 1, 4, null);
                } else {
                    System.out.println("pwrEncCanvas Image not found");
                }
            }
        };
        jPanel.add(this.pwrEncCanvas, "West");
        setToolTipRecursively(this.pwrEncCanvas, locinfo.TOOLSTR_4003);
        this.pwrEncCanvas.setPreferredSize(new Dimension(20, 20));
        this.pwrEncCanvas.setVisible(true);
        JPanel jPanel3 = this.pwrStatusPanel;
        JLabel jLabel = new JLabel();
        this.pwrEncLabel = jLabel;
        jPanel3.add(jLabel);
        this.pwrEncLabel.setText("         ");
        JPanel jPanel4 = this.ledStatusPanel;
        this.pwrHealthCanvas = new JPanel() {

            public void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (remcons.this.pwrHealthImg != null) {
                    remcons.this.waitImage(remcons.this.pwrHealthImg, this);
                    graphics.drawImage(remcons.this.pwrHealthImg, 1, 4, null);
                } else {
                    System.out.println("pwrHealthCanvas Image not found");
                }
            }
        };
        this.ledStatusPanel.add(this.pwrHealthCanvas, "West");
        setToolTipRecursively(this.pwrHealthCanvas, locinfo.TOOLSTR_4002);
        this.pwrHealthCanvas.setPreferredSize(new Dimension(18, 25));
        this.pwrHealthCanvas.setVisible(true);
        JPanel jPanel6 = this.ledStatusPanel;
        this.vmActCanvas = new JPanel() {

            public void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (remcons.this.vmActImg != null) {
                    remcons.this.waitImage(remcons.this.vmActImg, this);
                    graphics.drawImage(remcons.this.vmActImg, 1, 4, null);
                } else {
                    System.out.println("vmActCanvas Image not found");
                }
            }
        };
        jPanel6.add(this.vmActCanvas, "Center");
        setToolTipRecursively(this.vmActCanvas, locinfo.TOOLSTR_4004);
        this.vmActCanvas.setPreferredSize(new Dimension(18, 25));
        this.vmActCanvas.setVisible(true);
        JPanel jPanel8 = this.ledStatusPanel;
        this.pwrPowerCanvas = new JPanel() {

            public void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (remcons.this.pwrPowerImg != null) {
                    remcons.this.waitImage(remcons.this.pwrPowerImg, this);
                    graphics.drawImage(remcons.this.pwrPowerImg, 1, 4, null);
                } else {
                    System.out.println("pwrPowerCanvas Image not found");
                }
            }
        };
        jPanel8.add(this.pwrPowerCanvas, "East");
        setToolTipRecursively(this.pwrPowerCanvas, locinfo.TOOLSTR_4001);
        this.pwrPowerCanvas.setPreferredSize(new Dimension(18, 25));
        this.pwrPowerCanvas.setVisible(true);
        this.pwrStatusPanel.add(this.ledStatusPanel, "East");
        this.session.enable_keyboard();
        if (this.kbHookAvailable) {
            this.keyBoardTimer = new Timer(this.keyTimerTick, false, this.session);
            this.keyBoardTimer.setListener(new keyBoardTimerListener(this), null);
            this.keyBoardTimer.start();
            System.out.println("Keyboard Hook available and timer started...");
        }
        this.initialized = 1;
    }

    public void start() {
        this.timeout_countdown = this.session_timeout;
        start_session();
        if (this.session_timeout == INFINITE_TIMEOUT) {
            System.out.println("Remote Console inactivity timeout = infinite.");
        } else {
            System.out.println("Remote Console inactivity timeout = " + this.session_timeout / 60 + " minutes.");
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
        if (this.locale_setter != null && this.locale_setter.isAlive()) {
            this.locale_setter.stop();
        }
        this.locale_setter = null;
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
        if (this.session.UI_dirty) {
            this.session.UI_dirty = false;
            this.timeout_countdown = this.session_timeout;
            return;
        }
        this.timeout_countdown -= 30;
        if (this.timeout_countdown <= 0 && System.getProperty("java.version", "0").compareTo("1.2") < 0) {
            stop_session();
        }
    }

    private void start_session() {
        this.session.connect(this.session_ip, this.login, this.port_num, this.ts_param, this.terminalServicesPort, this);
        this.timer = new Timer(30000, false, this.session);
        this.timer.setListener(this, null);
        this.timer.start();
    }

    private void stop_session() {
        if (this.timer != null) {
            this.timer.stop();
            this.timer = null;
        }
        this.session.disconnect();
    }

    public void setPwrStatusEnc(int i) {
        if (i == 0) {
            this.pwrEncImg = this.pwrEncImgUnlock;
        } else {
            this.pwrEncImg = this.pwrEncImgLock;
        }
        this.pwrEncCanvas.invalidate();
        this.pwrEncCanvas.repaint();
    }

    public void setPwrStatusEncLabel(String str) {
        this.pwrEncLabel.setText(str + "       ");
    }

    public void setPwrStatusHealth(int i) {
        switch (i) {
            case 0:
                this.pwrHealthImg = this.pwrHealthImgGreen;
                break;
            case 1:
                this.pwrHealthImg = this.pwrHealthImgYellow;
                break;
            case 2:
                this.pwrHealthImg = this.pwrHealthImgRed;
                break;
            default:
                this.pwrHealthImg = this.pwrHealthImgOff;
                break;
        }
        this.pwrHealthCanvas.invalidate();
        this.pwrHealthCanvas.repaint();
    }

    public void setPwrStatusPower(int i) {
        if (i == 0 && this.pwrPowerImgOff != this.pwrPowerImg) {
            this.pwrPowerImg = this.pwrPowerImgOff;
            this.ParentApp.updatePsMenu(i);
            this.pwrPowerCanvas.invalidate();
            this.pwrPowerCanvas.repaint();
            System.out.println("Moving Power to Off state");
        } else if (i != 0 && this.pwrPowerImgOn != this.pwrPowerImg) {
            this.pwrPowerImg = this.pwrPowerImgOn;
            this.ParentApp.updatePsMenu(i);
            this.pwrPowerCanvas.invalidate();
            this.pwrPowerCanvas.repaint();
            System.out.println("Moving Power to ON state");
        }
    }

    public void setvmAct(int i) {
        if (this.vmActImg == this.vmActImgOn || i == 0) {
            this.vmActImg = this.vmActImgOff;
            this.vmActCanvas.invalidate();
            this.vmActCanvas.repaint();
        } else if (this.vmActImg == this.vmActImgOff) {
            this.vmActImg = this.vmActImgOn;
            this.vmActCanvas.invalidate();
            this.vmActCanvas.repaint();
        }
    }

    public int seize_dialog(String str, String str2, int i) {
        System.out.println("seize dialog invoked" + i);
        return new VSeizeWaitDialog(this, str, str2, i).getUserInput();
    }

    public void seize_confirmed() {
        this.ParentApp.moveUItoInit(false);
        this.ParentApp.virtdevsObj.stop();
        remconsUnInstallKeyboardHook();
        this.ParentApp.dispFrame.setVisible(false);
        this.session.seize();
        this.ParentApp.stop();
    }

    public void shared(String str, String str2) {
        System.out.println("shared notification invoked");
        new VErrorDialog(this.ParentApp.dispFrame, locinfo.DIALOGSTR_2026, locinfo.DIALOGSTR_2027 + " " + str2 + "@" + str + locinfo.DIALOGSTR_2028, false);
    }

    public void unAuthorized(String str) {
        new VErrorDialog(this.ParentApp.dispFrame, locinfo.DIALOGSTR_2026, locinfo.DIALOGSTR_2029 + str + locinfo.DIALOGSTR_202a, false);
        if (str.contains("for remote console")) {
            this.session.unAuthAccess();
        }
    }

    public void firmwareUpgrade() {
        System.out.println("Firmware upgrade notification invoked");
        VErrorDialog vErrorDialog = new VErrorDialog(this.ParentApp.dispFrame, locinfo.DIALOGSTR_2026, locinfo.DIALOGSTR_202b, false);
        this.ParentApp.moveUItoInit(false);
        this.ParentApp.virtdevsObj.stop();
        this.session.fwUpgrade();
        this.ParentApp.stop();
        if (vErrorDialog.getBoolean()) {
            System.exit(0);
        }
    }

    public void ack(byte b, byte b2, byte b3, byte b4) {
        if (b != 0) {
            return;
        }
        if (b2 == 1) {
            if (b4 == 1 && !this.ParentApp.fdSelected) {
                this.ParentApp.fdSelected = true;
                this.ParentApp.lockFdMenu(false, locinfo.MENUSTR_1023 + locinfo.MENUSTR_100A);
            } else if (b4 == 0 && this.ParentApp.fdSelected) {
                this.ParentApp.fdSelected = false;
                this.ParentApp.lockFdMenu(true, "");
            }
        } else if (b2 != 2) {
            /* no-op */
        } else {
            if (b4 == 1 && !this.ParentApp.cdSelected) {
                this.ParentApp.cdSelected = true;
                this.ParentApp.lockCdMenu(false, locinfo.MENUSTR_1023 + locinfo.MENUSTR_100B);
            } else if (b4 == 0 && this.ParentApp.cdSelected) {
                this.ParentApp.cdSelected = false;
                this.ParentApp.lockCdMenu(true, "");
            }
        }
    }

    protected void init_params() {
        this.login = null;
        this.port_num = 23;
        this.mouse_mode = 0;
        this.session_timeout = SESSION_TIMEOUT_DEFAULT;
        this.session_encryption_enabled = true;
        this.session_key_index = 0;
        this.launchTerminalServices = false;
        this.terminalServicesPort = 0;
        this.debug_msg = true;
        this.session_ip = this.ParentApp.getCodeBase().getHost();
        this.num_cursors = 0;
        if (!this.session_encryption_enabled) {
            this.session_decrypt_key = null;
            this.session_encrypt_key = null;
        } else if (null != this.ParentApp.enc_key) {
            System.arraycopy(this.ParentApp.enc_key_val, 0, this.session_decrypt_key, 0, this.session_decrypt_key.length);
            System.arraycopy(this.ParentApp.enc_key_val, 0, this.session_encrypt_key, 0, this.session_encrypt_key.length);
        }
    }

    private String parse_login(String str) {
        if (!str.startsWith("Compaq-RIB-Login=")) {
            return base64_decode(str);
        }
        try {
            return "\u001b[!" + str.substring(17, 73) + '\r' + str.substring(74, 106) + '\r';
        } catch (StringIndexOutOfBoundsException e) {
            return null;
        }
    }

    private String base64_decode(String str) {
        int i = 0;
        StringBuilder str2 = new StringBuilder();
        for (int i2 = 0; i2 + 3 < str.length() && i == 0; i2 += 4) {
            char c = base64[str.charAt(i2) & 127];
            char c2 = base64[str.charAt(i2 + 1) & 127];
            char c3 = base64[str.charAt(i2 + 2) & 127];
            char c4 = (char) ((c << 2) + (c2 >> 4));
            char c5 = (char) (c4 & 255);
            char c6 = (char) (((char) ((c2 << 4) + (c3 >> 2))) & 255);
            char c7 = (char) (((char) ((c3 << 6) + base64[str.charAt(i2 + 3) & 127])) & 255);
            if (c5 == ':') {
                c5 = '\r';
            }
            if (c6 == ':') {
                c6 = '\r';
            }
            if (c7 == ':') {
                c7 = '\r';
            }
            str2.append(c5);
            if (str.charAt(i2 + 2) == '=') {
                i++;
            } else {
                str2.append(c6);
            }
            if (str.charAt(i2 + 3) == '=') {
                i++;
            } else {
                str2.append(c7);
            }
        }
        if (str2.length() != 0) {
            str2.append('\r');
        }
        return str2.toString();
    }

    public void paint(Graphics graphics) {
    }

    public int getTimeoutValue() {
        return this.timeout_countdown;
    }

    @Override
    public void run() {
        if (System.getProperty("os.name").toLowerCase().startsWith("windows") && !this.lt.windows) {
            Locale.setDefault(Locale.US);
        }
        while (true) {
            if (this.retry_connection_flag && 3 >= this.retry_connection_count) {
                System.out.println("Retrying connection" + this.retry_connection_count);
                this.retry_connection_flag = false;
                this.retry_connection_count++;
                if (!this.fdCachedConnState) {
                    this.fdCachedConnState = this.fdConnState;
                }
                if (!this.cdCachedConnState) {
                    this.cdCachedConnState = this.cdConnState;
                }
                System.out.println("fd conn:" + this.fdConnState + " cd conn:" + this.cdConnState);
                System.out.println("fdcache:" + this.fdCachedConnState + " cdcache:" + this.cdCachedConnState);
                stop_session();
                try {
                    sleepAtLeast(5000L);
                } catch (InterruptedException e) {
                    System.out.println("Thread interrupted..");
                }
                this.ParentApp.jsonObj.getJSONRequest("rc_info");
                if (this.session_encryption_enabled && null != this.ParentApp.enc_key) {
                    System.arraycopy(this.ParentApp.enc_key_val, 0, this.session_decrypt_key, 0, this.session_decrypt_key.length);
                    System.arraycopy(this.ParentApp.enc_key_val, 0, this.session_encrypt_key, 0, this.session_encrypt_key.length);
                }
                this.session.setup_decryption(this.session_decrypt_key);
                this.session.setup_encryption(this.session_encrypt_key, this.session_key_index);
                start_session();
                try {
                    sleepAtLeast(2500L);
                } catch (InterruptedException e2) {
                    System.out.println("Thread interrupted..");
                }
                if (null == this.session.receiver || this.retry_connection_flag) {
                    this.retry_connection_flag = true;
                } else {
                    this.retry_connection_count = 0;
                }
            } else if (this.retry_connection_flag) {
                System.out.println("Retry connection  - video maximum attempts exhausted");
                stop_session();
                this.retry_connection_flag = false;
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
        this.licensed = (i & 1) != 0;
        System.out.println("SetLicensed: " + this.licensed);
    }


    public void SetFlags(int i) {
        if ((i & 8) == 0) {
            this.halfHeightCapable = false;
            System.out.println("halfHeightCapable false");
            return;
        }
        this.halfHeightCapable = true;
        System.out.println("halfHeightCapable true");
    }

    public void UnlicensedShutdown() {
        String stringBuffer = "<html>" + locinfo.DIALOGSTR_2015 + " " + locinfo.DIALOGSTR_2017 + " " + locinfo.DIALOGSTR_202d + "<br><br>" + locinfo.DIALOGSTR_202e + "</html>";
        System.out.println("Unlicensed notification invoked");
        new VErrorDialog(this.ParentApp.dispFrame, locinfo.DIALOGSTR_202c, stringBuffer, true);
        this.ParentApp.moveUItoInit(false);
        this.ParentApp.stop();
    }

    public void resetShutdown() {
        new VErrorDialog(this.ParentApp.dispFrame, locinfo.MENUSTR_1007, locinfo.DIALOGSTR_2061, true);
        this.ParentApp.moveUItoInit(false);
        this.ParentApp.stop();
    }

    public int getInitialized() {
        return this.initialized;
    }

    private void get_terminal_svcs_label(int i) {
        String str;
        if (i == 0) {
            str = "mstsc";
        } else if (i == 1) {
            str = "vnc";
        } else {
            str = "type" + i;
        }
        this.term_svcs_label = prop.getProperty(str + ".label", "Terminal Svcs");
    }

    public void remconsInstallKeyboardHook() {
        String lowerCase = System.getProperty("os.name").toLowerCase();
        if (this.kHook == null) {
            System.out.println("remconsInstallKeyboardHook:KB Hook dll not loaded");
        } else if (!this.kbHookInstalled && this.kbHookAvailable && !dialogIsOpen) {
            this.kHook.clearKeymap();
            int InstallKeyboardHook = this.kHook.InstallKeyboardHook();
            if (lowerCase.startsWith("windows") || -1412584499 != InstallKeyboardHook) {
                this.kHook.setKeyboardLayoutId(InstallKeyboardHook);
                this.kbHookInstalled = true;
                this.keyData = 0;
                this.prevKeyData = 0;
                if (!lowerCase.startsWith("windows")) {
                    this.keyBoardTimer.start();
                    this.kHook.setLocalKbdLayout(this.localKbdLayoutId);
                    return;
                }
                return;
            }
            this.kbHookInstalled = false;
            this.keyBoardTimer.stop();
            System.out.println("remconsInstallKeyboardHook: KB Hook install failed");
        }
    }

    public void remconsUnInstallKeyboardHook() {
        if (this.kHook != null && this.kbHookInstalled && this.kbHookAvailable) {
            int UnInstallKeyboardHook = this.kHook.UnInstallKeyboardHook();
            if (UnInstallKeyboardHook == 0) {
                this.kbHookInstalled = false;
                this.keyData = 0;
                this.prevKeyData = 0;
                this.kHook.clearKeymap();
                return;
            }
            System.out.println("remconsUnInstallKeyboardHook: uninstall failed:" + UnInstallKeyboardHook);
        }
    }

    public void setLocalKbdLayout(int i) {
        if (this.kHook == null || !this.kbHookInstalled) {
            System.out.println("setKbdLayoutHandler: kHook not available. dbg caching..");
            this.localKbdLayoutId = i;
            return;
        }
        System.out.println("setKbdLayoutHandler: set Layout - " + i);
        this.kHook.setLocalKbdLayout(i);
    }


    public static class keyBoardTimerListener implements TimerListener {
        private final remcons this$0;

        keyBoardTimerListener(remcons remconsVar) {
            this.this$0 = remconsVar;
        }

        @Override
        public synchronized void timeout(Object obj) {
            boolean z;
            boolean z2 = false;
            byte[] bArr = new byte[10];
            int i = 995;
            if (this.this$0.kHook != null && this.this$0.kbHookInstalled) {
                do {
                    this.this$0.prevKeyData = this.this$0.keyData;
                    this.this$0.keyData = this.this$0.kHook.GetKeyData();
                    if (!(this.this$0.keyData == this.this$0.prevKeyData || 0 == this.this$0.keyData)) {
                        int i2 = (this.this$0.keyData & 16711680) >> 16;
                        int i3 = (this.this$0.keyData & 65280) >> 8;
                        int i4 = this.this$0.keyData & telnet.TELNET_IAC;
                        if ((i2 & 144) == 144) {
                            z = true;
                        } else if ((i2 & 128) == 128) {
                            z2 = false;
                            z = false;
                        } else {
                            z2 = true;
                            z = false;
                        }
                        byte[] HandleHookKey = this.this$0.kHook.HandleHookKey(i4, i3, z2, z);
                        if (this.this$0.kHook.kcmdValid) {
                            if (!this.this$0.kbHookDataRcvd) {
                                this.this$0.kbHookDataRcvd = true;
                            }
                            this.this$0.session.transmitb(HandleHookKey, HandleHookKey.length);
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
