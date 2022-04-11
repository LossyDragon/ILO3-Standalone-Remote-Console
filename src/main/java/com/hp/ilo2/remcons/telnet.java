package com.hp.ilo2.remcons;

import com.hp.ilo2.intgapp.locinfo;
import com.hp.ilo2.virtdevs.MediaAccess;
import com.hp.ilo2.virtdevs.SCSI;
import com.hp.ilo2.virtdevs.VErrorDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Objects;


public class telnet extends JPanel implements Runnable, MouseListener, FocusListener, KeyListener {
    
    LocaleTranslator translator = new LocaleTranslator();
    int ts_type;
    private Aes aes128decrypter;
    private Aes aes256decrypter;
    private final Locale lo;
    private Process rdpProc = null;
    private RC4 RC4decrypter;
    private final String keyboardLayout;
    private final boolean crlf_enabled = false;
    private boolean decryption_active = false;
    private boolean enable_terminal_services = false;
    private boolean screenFocusLost = false;
    private boolean seized = false;
    private final boolean tbm_mode = false;
    private int japanese_kbd;
    private int terminalServicesPort = 3389;
    private final int total_count = 0;
    private final int[] keyMap = new int[256];
    private final int[] winkey_to_hid = {0, 0, 0, 0, 0, 0, 0, 0, 42, 43, 40, 0, 0, 40, 0, 0, 225, 224, 226, 72, 57, 0, 0, 0, 0, 0, 41, 41, 138, 139, 0, 0, 44, 75, 78, 77, 74, 80, 82, 79, 81, 0, 0, 0, 54, 45, 55, 56, 39, 30, 31, 32, 33, 34, 35, 36, 37, 38, 0, 51, 0, 46, 0, 0, 0, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 47, 49, 48, 0, 0, 98, 89, 90, 91, 92, 93, 94, 95, 96, 97, 85, 87, 159, 86, 99, 84, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 0, 0, 0, 76, 0, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 71, 0, 0, 0, 0, 36, 37, 52, 54, 70, 73, 0, 0, 0, 0, 55, 47, 48, 228, 226, 230, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 51, 46, 54, 45, 55, 56, 53, 135, 48, 137, 50, 52, 135, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 47, 49, 48, 52, 0, 96, 90, 92, 94, 0, 0, 0, 0, 0, 0, 0, 139, 0, 0, 0, 0, 136, 136, 136, 53, 53, 0, 0, 0, 0, 0, 0, 0, 0, 138, 0, TELNET_IAC};
    private static final int CMD_TS_AVAIL = 194;
    private static final int CMD_TS_NOT_AVAIL = 195;
    private static final int CMD_TS_STARTED = 196;
    private static final int CMD_TS_STOPPED = 197;
    protected DataInputStream in;
    protected DataOutputStream out;
    protected Socket s;
    protected String curr_num;
    protected String escseq;
    protected String host = "";
    protected String login = "";
    protected Thread receiver;
    protected boolean dvc_encryption = false;
    protected boolean dvc_mode = false;
    protected boolean encryption_enabled = false;
    protected byte[] decrypt_key = new byte[16];
    protected dvcwin screen;
    protected int back;
    protected int connected = 0;
    protected int escseq_val_count = 0;
    protected int fore;
    protected int hi_back;
    protected int hi_fore;
    protected int port = 23;
    protected int[] escseq_val = new int[10];
    public JLabel status_box = new JLabel();
    public String st_fld1 = "";
    public String st_fld2 = "";
    public String st_fld3 = "";
    public String st_fld4 = "";
    public boolean mirror = false;
    public boolean post_complete = false;
    public byte[] sessionKey = new byte[32];
    public cmd cmdObj = new cmd();
    public final int AES_BITSIZE_128 = 0;
    public final int AES_BITSIZE_192 = 1;
    public final int AES_BITSIZE_256 = 2;
    public final int CIPHER_AES128 = 2;
    public final int CIPHER_AES256 = 3;
    public final int CIPHER_NONE = 0;
    public final int CIPHER_RC4 = 1;
    public final int CONNECT_CANCEL = 0;
    public final int CONNECT_SEIZE = 1;
    public final int CONNECT_SHARE = 2;
    public final int KEY_STATE_PRESSED = 0;
    public final int KEY_STATE_RELEASED = 2;
    public final int KEY_STATE_TYPED = 1;
    public final int PWR_OPTION_CYCLE = 2;
    public final int PWR_OPTION_HOLD = 1;
    public final int PWR_OPTION_PULSE = 0;
    public final int PWR_OPTION_RESET = 3;
    public final int REQ_DONE = 4;
    public final int REQ_GET_AUTH = 1;
    public final int REQ_LOGIN_KEY = 0;
    public final int REQ_SEIZE = 3;
    public final int REQ_SHARE = 2;
    public int cipher = 0;
    public int dbg_print = 0;
    public remcons remconsObj;
    public static final int JAP_VK_BACK_SLASH = 195;
    public static final int JAP_VK_CLOSE_BRACKET = 196;
    public static final int JAP_VK_COLON = 197;
    public static final int JAP_VK_OPEN_BRACKET = 194;
    public static final int JAP_VK_RO = 198;
    public static final int TELNET_AO = 245;
    public static final int TELNET_AYT = 246;
    public static final int TELNET_BRK = 243;
    public static final int TELNET_CHG_ENCRYPT_KEYS = 193;
    public static final int TELNET_DM = 242;
    public static final int TELNET_DO = 253;
    public static final int TELNET_DONT = 254;
    public static final int TELNET_EC = 247;
    public static final int TELNET_EL = 248;
    public static final int TELNET_ENCRYPT = 192;
    public static final int TELNET_GA = 249;
    public static final int TELNET_IAC = 255;
    public static final int TELNET_IP = 244;
    public static final int TELNET_NOP = 241;
    public static final int TELNET_PORT = 23;
    public static final int TELNET_SB = 250;
    public static final int TELNET_SE = 240;
    public static final int TELNET_WILL = 251;
    public static final int TELNET_WONT = 252;

    public void setLocale(String str) {
        this.translator.selectLocale(str);
    }
    
    public telnet(remcons remconsVar) {
        this.japanese_kbd = 0;
        this.remconsObj = remconsVar;
        this.screen = new dvcwin(1024, 768, this.remconsObj);
        System.out.println("Screen: " + this.screen);
        this.screen.addMouseListener(this);
        addFocusListener(this);
        this.screen.addFocusListener(this);
        this.screen.addKeyListener(this);
        focusTraversalKeysDisable(this.screen);
        focusTraversalKeysDisable(this);
        setBackground(Color.black);
        setLayout(new BorderLayout());
        add(this.screen, "North");
        set_status(1, locinfo.STATUSSTR_300d);
        set_status(2, "          ");
        set_status(3, "          ");
        set_status(4, "          ");
        if (System.getProperty("os.name").toLowerCase().startsWith("windows") && !this.translator.windows) {
            this.translator.selectLocale("en_US");
        }
        for (int i = 0; i < 256; i++) {
            this.keyMap[i] = 0;
        }
        this.lo = Locale.getDefault();
        this.keyboardLayout = this.lo.toString();
        System.out.println("telent lang: Keyboard layout is " + this.keyboardLayout);
        if (this.keyboardLayout.startsWith("ja")) {
            System.out.println("JAPANESE LANGUAGE \n");
            this.japanese_kbd = 1;
            return;
        }
        this.japanese_kbd = 0;
    }

    public void enable_debug() {
    }

    public void disable_debug() {
    }

    public void startRdp() {
        String str;
        if (this.rdpProc == null) {
            Runtime runtime = Runtime.getRuntime();
            if (this.ts_type == 0) {
                str = "mstsc";
            } else if (this.ts_type == 1) {
                str = "vnc";
            } else {
                str = "type" + this.ts_type;
            }
            String property = remcons.prop.getProperty(str + ".program");
            System.out.println(str + " = " + property);
            if (property != null) {
                String percent_sub = percent_sub(property);
                System.out.println("exec: " + percent_sub);
                try {
                    this.rdpProc = runtime.exec(percent_sub);
                } catch (IOException e) {
                    System.out.println("IOException: " + e.getMessage() + ":: " + percent_sub);
                } catch (SecurityException e2) {
                    System.out.println("SecurityException: " + e2.getMessage() + ":: Attempting to launch " + percent_sub);
                }
            } else {
                boolean z = false;
                try {
                    System.out.println("Executing mstsc. Port is " + this.terminalServicesPort);
                    this.rdpProc = runtime.exec("mstsc /f /console /v:" + this.host + ":" + this.terminalServicesPort);
                } catch (IOException e3) {
                    System.out.println("IOException: " + e3.getMessage() + ":: mstsc not found in system directory. Looking in \\Program Files\\Remote Desktop.");
                    z = true;
                } catch (SecurityException e4) {
                    System.out.println("SecurityException: " + e4.getMessage() + ":: Attempting to launch mstsc.");
                }
                if (z) {
                    z = false;
                    try {
                        this.rdpProc = runtime.exec(new String[]{"\\Program Files\\Remote Desktop\\mstsc /f /console /v:" + this.host + ":" + this.terminalServicesPort});
                    } catch (IOException e5) {
                        System.out.println("IOException: " + e5.getMessage() + ":: Unable to find mstsc. Verify that Terminal Services client is installed.");
                        z = true;
                    } catch (SecurityException e6) {
                        System.out.println("SecurityException: " + e6.getMessage() + ":: Attempting to launch mstsc.");
                    }
                }
                if (z) {
                    try {
                        this.rdpProc = runtime.exec(new String[]{"\\Program Files\\Terminal Services Client\\mstsc"});
                    } catch (IOException e7) {
                        System.out.println("IOException: " + e7.getMessage() + ":: Unable to find mstsc. Verify that Terminal Services client is installed.");
                    } catch (SecurityException e8) {
                        System.out.println("SecurityException: " + e8.getMessage() + ":: Attempting to launch mstsc.");
                    }
                }
            }
        }
    }

    public void keyTyped(KeyEvent keyEvent) {
        sendKey(keyEvent, 1);
    }

    public void keyPressed(KeyEvent keyEvent) {
        sendKey(keyEvent, 0);
    }

    public void keyReleased(KeyEvent keyEvent) {
        sendKey(keyEvent, 2);
    }

    public void send_auto_alive_msg() {
    }

    public synchronized void focusGained(FocusEvent focusEvent) {
        if (focusEvent.getComponent() != this.screen) {
            this.screen.requestFocus();
        } else if (this.screenFocusLost) {
            this.remconsObj.remconsInstallKeyboardHook();
            this.screenFocusLost = false;
        }
    }

    public synchronized void focusLost(FocusEvent focusEvent) {
        if (focusEvent.getComponent() == this.screen && focusEvent.isTemporary()) {
            this.remconsObj.remconsUnInstallKeyboardHook();
            this.screenFocusLost = true;
        }
    }

    public synchronized void mouseClicked(MouseEvent mouseEvent) {
        telnet.super.requestFocus();
    }

    public synchronized void mousePressed(MouseEvent mouseEvent) {
    }

    public synchronized void mouseReleased(MouseEvent mouseEvent) {
    }

    public synchronized void mouseEntered(MouseEvent mouseEvent) {
    }

    public synchronized void mouseExited(MouseEvent mouseEvent) {
    }

    public synchronized void addNotify() {
        telnet.super.addNotify();
    }

    public synchronized void set_status(int i, String str) {
        switch (i) {
            case 1:
                this.st_fld1 = str;
                break;
            case 2:
                this.st_fld2 = str;
                break;
            case 3:
                this.st_fld3 = str;
                break;
            case 4:
                this.st_fld4 = str;
                break;
        }
        this.status_box.setText(this.st_fld1 + " " + this.st_fld2 + "      " + this.st_fld3 + "      " + this.st_fld4);
    }

    public void reinit_vars() {
        this.dvc_encryption = false;
    }

    public void setup_decryption(byte[] bArr) {
        System.arraycopy(bArr, 0, this.decrypt_key, 0, 16);
        this.RC4decrypter = new RC4(bArr);
        this.encryption_enabled = true;
        this.aes128decrypter = new Aes(0, bArr);
        this.aes256decrypter = new Aes(0, bArr);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public synchronized void connect(String str, String str2, int i, int i2, int i3, remcons remconsVar) {
        this.enable_terminal_services = (i2 & 1) == 1;
        this.ts_type = i2 >> 8;
        if (i3 != 0) {
            this.terminalServicesPort = i3;
        }
        if (this.connected == 0) {
            this.screen.start_updates();
            this.connected = 1;
            this.host = str;
            this.login = str2;
            this.port = i;
            this.remconsObj = remconsVar;
            requestFocus();
            this.sessionKey = remconsVar.ParentApp.getParameter("RCINFO1").getBytes();
            String str3 = remconsVar.ParentApp.rc_port;
            if (str3 != null) {
                try {
                    this.port = Integer.parseInt(str3);
                    System.out.println("RC port number " + this.port);
                } catch (NumberFormatException e) {
                    System.out.println("Failed to read rcport from parameters");
                    this.port = 23;
                }
            }
            try {
                set_status(1, locinfo.STATUSSTR_3008);
                System.out.println("updated: connecting to " + this.host + ":" + this.port);
                try {
                    Thread.currentThread();
                    Thread.sleep(1000L);
                } catch (InterruptedException e2) {
                    System.out.println("connect Thread interrupted..");
                }
                this.s = new Socket(this.host, this.port);
                try {
                    this.s.setSoLinger(true, 0);
                    System.out.println("set TcpNoDelay");
                    this.s.setTcpNoDelay(true);
                } catch (SocketException e3) {
                    System.out.println("telnet.connect() linger SocketException: " + e3);
                }
                this.in = new DataInputStream(this.s.getInputStream());
                this.out = new DataOutputStream(this.s.getOutputStream());
                if (this.in.readByte() == 80) {
                    set_status(1, locinfo.STATUSSTR_3009);
                    System.out.println("Received hello byte. Requesting remote connection...");
                    if (requestRemoteConnection(/* Magic Number */ 8193)) {
                        this.receiver = new Thread(this);
                        this.receiver.setName("telnet_rcvr");
                        this.receiver.start();
                        this.cmdObj.connectCmd(this.remconsObj, this.host, this.port);
                    } else {
                        remconsVar.ParentApp.stop();
                    }
                } else {
                    set_status(1, locinfo.STATUSSTR_300a);
                    System.out.println("Socket connection failure... ");
                }
            } catch (SocketException e4) {
                System.out.println("telnet.connect() SocketException: " + e4);
                set_status(1, e4.toString());
                this.s = null;
                this.in = null;
                this.out = null;
                this.receiver = null;
                this.connected = 0;
            } catch (UnknownHostException e5) {
                System.out.println("telnet.connect() UnknownHostException: " + e5);
                set_status(1, e5.toString());
                this.s = null;
                this.in = null;
                this.out = null;
                this.receiver = null;
                this.connected = 0;
            } catch (IOException e6) {
                System.out.println("telnet.connect() IOException: " + e6);
                set_status(1, e6.toString());
                this.s = null;
                this.in = null;
                this.out = null;
                this.receiver = null;
                this.connected = 0;
            }
        } else {
            requestFocus();
        }
    }

    public boolean requestRemoteConnection(int i) {
        int i2;
        boolean z = false;
        boolean z2 = false;
        byte[] bArr = new byte[2];
        while (!z2) {
            if (!(z2)) {
                bArr[0] = (byte) (i & TELNET_IAC);
                bArr[1] = (byte) ((i & 65280) >>> 8);
                if (this.remconsObj.ParentApp.optional_features.contains("ENCRYPT_KEY")) {
                    for (int i3 = 0; i3 < this.sessionKey.length; i3++) {
                        byte[] bArr2 = this.sessionKey;
                        bArr2[i3] = (byte) (bArr2[i3] ^ ((byte) this.remconsObj.ParentApp.enc_key.charAt(i3 % this.remconsObj.ParentApp.enc_key.length())));
                    }
                    if (this.remconsObj.ParentApp.optional_features.contains("ENCRYPT_VMKEY")) {
                        bArr[1] = (byte) (bArr[1] | 64);
                    } else {
                        bArr[1] = (byte) (bArr[1] | 128);
                    }
                }
                byte[] bArr3 = new byte[bArr.length + this.sessionKey.length];
                System.arraycopy(bArr, 0, bArr3, 0, bArr.length);
                System.arraycopy(this.sessionKey, 0, bArr3, bArr.length, this.sessionKey.length);
                transmit(new String(bArr3));
                z2 = true;
            } else if (z2) {
                try {
                    byte readByte = this.in.readByte();
                    switch (readByte) {
                        case 81:
                            System.out.println("Access denied.");
                            set_status(1, locinfo.STATUSSTR_300b);
                            if (null != this.remconsObj.ParentApp.dispFrame) {
                                new VErrorDialog(this.remconsObj.ParentApp.dispFrame, locinfo.DIALOGSTR_202f, locinfo.DIALOGSTR_205f, true);
                                this.remconsObj.ParentApp.dispFrame.setVisible(false);
                            } else {
                                new VErrorDialog(locinfo.DIALOGSTR_205f, true);
                            }
                            z = false;
                            z2 = true;
                            this.remconsObj.ParentApp.stop();
                            continue;
                        case 82:
                            set_status(1, locinfo.STATUSSTR_300c);
                            System.out.println("Authenticated");
                            z = true;
                            this.remconsObj.licensed = true;
                            z2 = true;
                            continue;
                        case 83:
                        case 89:
                            System.out.println("Authenticated, but busy, negotiating");
                            if (0 == this.remconsObj.retry_connection_count) {
                                i2 = negotiateBusy();
                                System.out.println("negotiateResult:" + i2);
                            } else {
                                System.out.println("Overriding seize option for internal retry");
                                i2 = 1;
                            }
                            switch (i2) {
                                case 0:
                                    System.out.println("Connection cancelled by user");
                                    if (null != this.remconsObj.ParentApp.dispFrame) {
                                        this.remconsObj.ParentApp.dispFrame.setVisible(false);
                                    }
                                    z = false;
                                    z2 = true;
                                    continue;
                                case 1:
                                    bArr[0] = 85;
                                    bArr[1] = 0;
                                    byte[] bArr4 = new byte[bArr.length];
                                    System.arraycopy(bArr, 0, bArr4, 0, bArr.length);
                                    System.out.println("Seizing connection, sending command 0x0055");
                                    transmit(new String(bArr4));
                                    z2 = true;
                                    set_status(1, locinfo.STATUSSTR_3118);
                                    continue;
                                case 2:
                                    bArr[0] = 86;
                                    bArr[1] = 0;
                                    System.out.println("Sharing connection, sending command 0x0056");
                                    byte[] bArr5 = new byte[bArr.length];
                                    System.arraycopy(bArr, 0, bArr5, 0, bArr.length);
                                    transmit(new String(bArr5));
                                    z2 = true;
                                    continue;
                                default:
                                    continue;
                            }
                        case 84:
                        case SCSI.SCSI_MODE_SELECT /* 85 */:
                        case 86:
                        default:
                            System.out.println("rqrmtconn default: " + (int) readByte);
                            z = true;
                            z2 = true;
                            continue;
                        case 87:
                            System.out.println("Received No License Notification");
                            this.remconsObj.licensed = false;
                            z = false;
                            z2 = true;
                            continue;
                        case 88:
                            System.out.println("No free Sessions Notification");
                            if (null != this.remconsObj.ParentApp.dispFrame) {
                                new VErrorDialog(this.remconsObj.ParentApp.dispFrame, locinfo.DIALOGSTR_202f, locinfo.DIALOGSTR_2030, true);
                                this.remconsObj.ParentApp.dispFrame.setVisible(false);
                            } else {
                                new VErrorDialog(locinfo.DIALOGSTR_2030, true);
                            }
                            z = false;
                            z2 = true;
                            this.remconsObj.ParentApp.stop();
                    }
                } catch (IOException e) {
                    z = false;
                    z2 = true;
                    System.out.println("Socket Read failed.");
                }
            } else if (z2) {
                z = false;
                z2 = true;
            } else if (z2) {
                z2 = true;
                try {
                    switch (this.in.readByte()) {
                        case 81:
                            if (null != this.remconsObj.ParentApp.dispFrame) {
                                new VErrorDialog(this.remconsObj.ParentApp.dispFrame, locinfo.DIALOGSTR_202f, locinfo.DIALOGSTR_2047, true);
                                this.remconsObj.ParentApp.dispFrame.setVisible(false);
                            } else {
                                new VErrorDialog(locinfo.DIALOGSTR_2047, true);
                            }
                            z = false;
                            continue;
                        case 82:
                            this.remconsObj.ParentApp.moveUItoInit(true);
                            z = true;
                    }
                } catch (IOException e2) {
                    z = false;
                    z2 = true;
                    System.out.println("Socket Read failed.");
                }
            }
        }

        return z;
    }

    public int negotiateBusy() {
        int i = 0;
        this.remconsObj.ParentApp.moveUItoInit(false);
        switch (new VSeizeDialog(this.remconsObj).getUserInput()) {
            case 0:
                i = 0;
                break;
            case 2:
                i = 1;
                break;
        }
        return i;
    }

    public void connect(String str, String str2, int i, int i2, remcons remconsVar) {
        connect(str, str2, this.port, i, i2, remconsVar);
    }

    public void connect(String str, int i, int i2, remcons remconsVar) {
        connect(str, this.login, this.port, i, i2, remconsVar);
    }

    public synchronized void disconnect() {
        this.remconsObj.remconsUnInstallKeyboardHook();
        if (this.connected == 1) {
            this.screen.stop_updates();
            this.connected = 0;
            if (this.receiver != null && this.receiver.isAlive()) {
                this.receiver.stop();
            }
            this.receiver = null;
            if (this.s != null) {
                try {
                    System.out.println("Closing socket");
                    this.s.close();
                } catch (IOException e) {
                    System.out.println("telnet.disconnect() IOException: " + e);
                    set_status(1, e.toString());
                }
            }
            this.s = null;
            this.in = null;
            this.out = null;
            if (this.cmdObj != null) {
                this.cmdObj.disconnectCmd();
            }
            set_status(1, locinfo.STATUSSTR_300d);
            reinit_vars();
            this.decryption_active = false;
        }
    }

    public synchronized void transmit(String str) {
        if (this.out != null && str.length() != 0) {
            byte[] bArr = new byte[str.length()];
            for (int i = 0; i < str.length(); i++) {
                bArr[i] = (byte) str.charAt(i);
            }
            try {
                this.out.write(bArr, 0, bArr.length);
            } catch (IOException e) {
                System.out.println("telnet.transmit() IOException: " + e);
            }
        }
    }

    public synchronized void transmitb(byte[] bArr, int i) {
    }

    public synchronized String translate_key(KeyEvent keyEvent) {
        String str;
        char keyChar = keyEvent.getKeyChar();
        switch (keyChar) {
            case MediaAccess.F5_180_512 /* 9 */:
                str = "";
                break;
            case MediaAccess.F5_160_512 /* 10 */:
            case MediaAccess.F3_120M_512 /* 13 */:
                if (!keyEvent.isShiftDown()) {
                    str = "\r";
                    break;
                } else {
                    str = "\n";
                    break;
                }
            case MediaAccess.RemovableMedia /* 11 */:
            case MediaAccess.FixedMedia /* 12 */:
            default:
                str = this.translator.translate(keyChar);
                break;
        }
        return str;
    }

    public synchronized String translate_special_key(KeyEvent keyEvent) {
        String str = "";
        if (keyEvent.getKeyCode() == MediaAccess.F5_180_512) {
            keyEvent.consume();
            str = "\t";
        }
        return str;
    }

    protected synchronized String translate_special_key_release(KeyEvent keyEvent) {
        return "";
    }

    boolean process_dvc(char c) {
        return true;
    }

    @Override
    public void run() {
        int i;
        boolean z = false;
        byte[] bArr = new byte[1024];
        int i2 = 0;
        this.dvc_mode = true;
        System.out.println("Starting receiver run");
        try {
            while (true) {
                if (this.s == null || this.in == null) {
                    System.out.println("telnet.run() s or in is null");
                } else {
                    this.s.setSoTimeout(1000);
                    i = this.in.read(bArr);
                    if (i >= 0) {
                        for (int i4 = 0; i4 < i; i4++) {
                            if (this.dbg_print == 1000) {
                                this.dbg_print = 0;
                            }
                            this.dbg_print++;
                            this.remconsObj.fdConnState = this.remconsObj.ParentApp.virtdevsObj.fdConnected;
                            this.remconsObj.cdConnState = this.remconsObj.ParentApp.virtdevsObj.cdConnected;
                            char c = (char) (((char) bArr[i4]) & 255);
                            if (this.dvc_mode) {
                                if (this.dvc_encryption) {
                                    switch (this.cipher) {
                                        case 1:
                                            c = (char) (c ^ ((char) (this.RC4decrypter.randomValue() & TELNET_IAC)));
                                            break;
                                        case 2:
                                            c = (char) (c ^ ((char) (this.aes128decrypter.randomValue() & 255)));
                                            break;
                                        case 3:
                                            c = (char) (c ^ ((char) (this.aes256decrypter.randomValue() & 255)));
                                            break;
                                        default:
                                            System.out.println("Unknown encryption");
                                            break;
                                    }
                                    c = (char) (c & 255);
                                }
                                this.dvc_mode = process_dvc(c);
                                if (!this.dvc_mode) {
                                    System.out.println("DVC mode turned off");
                                    set_status(1, locinfo.STATUSSTR_300e);
                                }
                            } else if (c == 27) {
                                z = true;
                            } else if (z && c == '[') {
                                z = true;
                            } else if (z && c == 'R') {
                                this.dvc_mode = true;
                                this.dvc_encryption = true;
                                set_status(1, locinfo.STATUSSTR_300f);
                            } else if (z && c == 'r') {
                                this.dvc_mode = true;
                                this.dvc_encryption = false;
                                set_status(1, locinfo.STATUSSTR_3004);
                            } else {
                                z = false;
                            }
                        }
                    } else if (i2 > 1) {
                        System.out.println("Reading from stream failed for  " + i2 + "times");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("telnet.run() Exception, class:" + e.getClass() + "  msg:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (!this.seized) {
                int i6 = this.remconsObj.retry_connection_count;
                remcons remconsVar3 = this.remconsObj;
                this.screen.clearScreen();
                if (i6 < 3) {
                    System.out.println("Retrying connection");
                    set_status(1, locinfo.STATUSSTR_3011);
                } else {
                    System.out.println("offline");
                    set_status(1, locinfo.STATUSSTR_300d);
                }
                set_status(2, "");
                set_status(3, "");
                set_status(4, "");
                System.out.println("Actually Retrying connection");
                this.remconsObj.retry_connection_flag = true;
            }

            System.out.println("Completed receiver run");
        }
    }

    public void change_key() {
        this.RC4decrypter.update_key();
    }

    void focusTraversalKeysDisable(Object obj) {
        Class<?>[] clsArr = {Boolean.TYPE};
        Object[] objArr = {Boolean.TRUE};
        try {
            obj.getClass().getMethod("setFocusTraversalKeysEnabled", clsArr).invoke(obj, Boolean.FALSE);
        } catch (Throwable ignored) {
        }
        try {
            obj.getClass().getMethod("setFocusCycleRoot", clsArr).invoke(obj, objArr);
        } catch (Throwable ignored) {
        }
    }

    public void stop_rdp() {
        if (this.rdpProc != null) {
            try {
                this.rdpProc.exitValue();
            } catch (IllegalThreadStateException e) {
                System.out.println("IllegalThreadStateException thrown. Destroying TS.");
                this.rdpProc.destroy();
            }
            this.rdpProc = null;
        }
        System.out.println("TS stop.");
    }

    public void seize() {
        System.out.println("Received seize command. halting RC.");
        this.seized = true;
        this.screen.clearScreen();
        set_status(1, locinfo.STATUSSTR_3012);
        set_status(2, "");
        set_status(3, "");
        set_status(4, "");
    }

    public void fwUpgrade() {
        System.out.println("Received FW Upgrade notification. Halting RC.");
        this.seized = true;
        this.screen.clearScreen();
        set_status(1, locinfo.STATUSSTR_3013);
        set_status(2, "");
        set_status(3, "");
        set_status(4, "");
    }

    public void UnlicensedAccess() {
        System.out.println("Received UnlicensedAccess. Halting RC.");
        this.seized = true;
        this.screen.clearScreen();
        set_status(1, locinfo.DIALOGSTR_202c);
        set_status(2, "");
        set_status(3, "");
        set_status(4, "");
    }

    public void unAuthAccess() {
        System.out.println("Received unAuthAccess notification. Halting RC.");
        this.seized = true;
        this.screen.clearScreen();
        set_status(1, locinfo.STATUSSTR_3014);
        set_status(2, "");
        set_status(3, "");
        set_status(4, "");
    }

    public String percent_sub(String str) {
        StringBuilder stringBuffer = new StringBuilder();
        int i = 0;
        while (i < str.length()) {
            char charAt = str.charAt(i);
            if (charAt == '%') {
                i++;
                char charAt2 = str.charAt(i);
                if (charAt2 == 'h') {
                    stringBuffer.append(this.host);
                } else if (charAt2 == 'p') {
                    stringBuffer.append(this.terminalServicesPort);
                } else {
                    stringBuffer.append(charAt2);
                }
            } else {
                stringBuffer.append(charAt);
            }
            i++;
        }
        return stringBuffer.toString();
    }

    public byte[] getSessionKey() {
        return "0123456789abcdef0123456789abcdef".getBytes();
    }

    public byte[] getSessionKey(String str) {
        String parseParameter = parseParameter(str, "sessionKey");
        if (Objects.equals(parseParameter, "")) {
            System.out.println("Parsing failed.");
        }
        byte[] bytes = parseParameter.getBytes();
        System.out.println("sessionKey : " + parseParameter);
        return bytes;
    }

    public void sendHidKeyCode(KeyEvent keyEvent) {
        byte[] bArr = new byte[10];
        bArr[0] = 1;
        bArr[1] = 0;
        bArr[2] = 0;
        bArr[3] = 0;
        bArr[4] = 0;
        bArr[5] = 0;
        bArr[6] = 0;
        bArr[7] = 0;
        bArr[8] = 0;
        bArr[9] = 0;
        int keyCode = keyEvent.getKeyCode();
        int i = this.keyMap[keyCode];
        this.keyMap[keyCode] = 1;
        if (i != this.keyMap[keyCode]) {
            int i2 = 0;
            for (int i3 = 0; i3 < 256; i3++) {
                if (this.keyMap[i3] == 1) {
                    bArr[4 + i2] = (byte) this.winkey_to_hid[i3];
                    i2++;
                    if (i2 == 6) {
                        i2 = 5;
                    }
                }
            }
        }
        transmit(new String(bArr));
        keyEvent.consume();
    }

    public void sendHidSpecialKeyCode(KeyEvent keyEvent) {
        byte[] bArr = new byte[10];
        bArr[0] = 1;
        bArr[1] = 0;
        bArr[2] = 0;
        bArr[3] = 0;
        bArr[4] = 0;
        bArr[5] = 0;
        bArr[6] = 0;
        bArr[7] = 0;
        bArr[8] = 0;
        bArr[9] = 0;
        char keyChar = keyEvent.getKeyChar();
        int i = this.keyMap[keyChar];
        this.keyMap[keyChar] = 1;
        if (i != this.keyMap[keyChar]) {
            int i2 = 0;
            for (int i3 = 0; i3 < 256; i3++) {
                if (this.keyMap[i3] == 1) {
                    bArr[4 + i2] = (byte) this.winkey_to_hid[i3];
                    i2++;
                    if (i2 == 6) {
                        i2 = 5;
                    }
                }
            }
        }
        transmit(new String(bArr));
        keyEvent.consume();
    }

    public void clearKeyPress(KeyEvent keyEvent) {
        this.keyMap[keyEvent.getKeyCode()] = 0;
        transmit(new String(new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        keyEvent.consume();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void sendCtrlAltDel() {
        byte[] bArr = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        bArr[2] = 5;
        bArr[4] = 76;
        transmit(new String(bArr));
        try {
            Thread.currentThread();
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted..");
        }
        bArr[2] = 0;
        bArr[4] = 0;
        transmit(new String(bArr));
    }

    public void sendPower(int i) {
        byte[] bArr = new byte[4];
        bArr[0] = 0;
        bArr[1] = 0;
        switch (i) {
            case 0:
                bArr[2] = 0;
                break;
            case 1:
                bArr[2] = 1;
                break;
            case 2:
                bArr[2] = 2;
                break;
            case 3:
                bArr[2] = 3;
                break;
        }
        bArr[3] = 0;
        transmit(new String(bArr));
    }

    public synchronized void sendKey(KeyEvent keyEvent, int i) {
        if (!this.remconsObj.kbHookInstalled || !this.remconsObj.kbHookDataRcvd) {
            handleKey(keyEvent, i);
        }
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    public void handleKey(KeyEvent keyEvent, int i) {
        byte[] bArr = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        boolean z = false;
        int keyCode = keyEvent.getKeyCode();
        if (this.japanese_kbd != 1 || (keyCode != 92 && keyCode != 91 && keyCode != 93 && keyCode != 513)) {
            if (keyCode > 255) {
                switch (keyCode) {
                    case 259:
                        keyCode = 241;
                        break;
                    case 260:
                        keyCode = 242;
                        break;
                    case 512:
                        keyCode = 91;
                        break;
                    case 513:
                        keyCode = 93;
                        break;
                    case 514:
                        keyCode = 61;
                        break;
                    case 515:
                        keyCode = 52;
                        break;
                    case 517:
                        keyCode = 49;
                        break;
                    case 519:
                        keyCode = 57;
                        break;
                    case 520:
                        keyCode = 51;
                        break;
                    case 521:
                        keyCode = 61;
                        break;
                    case 522:
                        keyCode = 48;
                        break;
                    case 523:
                        keyCode = 45;
                        break;
                    default:
                        System.out.println("Unknown key " + keyCode);
                        keyCode = 0;
                        break;
                }
            }
        } else {
            switch (keyCode) {
                case 91:
                    keyCode = 194;
                    break;
                case 92:
                    if ('_' == keyEvent.getKeyChar()) {
                        keyCode = 198;
                    } else {
                        keyCode = 195;
                    }
                    break;
                case 93:
                    keyCode = 196;
                    break;
                case 513:
                    keyCode = 197;
                    break;
            }
        }
        if (!(keyCode == 0 || i == 1)) {
            if (i == 0) {
                this.keyMap[keyCode] = 1;
            } else if (9 != keyCode || 0 != this.keyMap[keyCode]) {
                this.keyMap[keyCode] = 0;
                if (keyEvent.isAltDown() && keyCode == 154) {
                    sendAltSysReq();
                }
            } else {
                return;
            }
            if (!keyEvent.isAltDown() && 0 != this.keyMap[18]) {
                this.keyMap[18] = 0;
            }
            if (isSpecialReleaseKey(keyCode)) {
                this.keyMap[keyCode] = 1;
            }
            int i2 = 0;
            for (int i3 = 0; i3 < 256; i3++) {
                if (this.keyMap[i3] == 1) {
                    byte b = (byte) this.winkey_to_hid[i3];
                    if (b == 224) {
                        z = true;
                    }
                    if (b == 226) {
                        z = true;
                    }
                    if (b == 76) {
                        z = true;
                    }
                    if ((b & 224) == 224) {
                        bArr[2] = (byte) (bArr[2] | ((byte) (1 << ((byte) (b ^ 224)))));
                    } else {
                        bArr[4 + i2] = b;
                        i2++;
                        if (i2 == 6) {
                            i2 = 5;
                        }
                    }
                }
            }
            if (z) {
                for (int i4 = 0; i4 < 256; i4++) {
                    this.keyMap[i4] = 0;
                }
            } else {
                transmitb(bArr, bArr.length);
                if (isSpecialReleaseKey(keyCode)) {
                    this.keyMap[keyCode] = 0;
                    bArr[9] = 0;
                    bArr[8] = 0;
                    bArr[7] = 0;
                    bArr[6] = 0;
                    bArr[5] = 0;
                    bArr[4] = 0;
                    int i5 = 0;
                    for (int i6 = 0; i6 < 256; i6++) {
                        if (this.keyMap[i6] == 1) {
                            bArr[4 + i5] = (byte) this.winkey_to_hid[i6];
                            i5++;
                            if (i5 == 6) {
                                i5 = 5;
                            }
                        }
                    }
                    transmitb(bArr, bArr.length);
                }
            }
        }
        keyEvent.consume();
    }

    public boolean isSpecialReleaseKey(int i) {
        boolean z = false;
        switch (i) {
            case 28:
            case SCSI.SCSI_SEND_DIAGNOSTIC /* 29 */:
            case TELNET_SE /* 240 */:
            case TELNET_BRK /* 243 */:
            case TELNET_IP /* 244 */:
                z = true;
                break;
        }
        return z;
    }

    static class statusUpdateTimer implements TimerListener {
        private final telnet this$0;

        statusUpdateTimer(telnet telnetVar) {
            this.this$0 = telnetVar;
        }

        @Override
        public void timeout(Object obj) {
            System.out.println("Video data reception timeout occurred. Clearing status.");
            this.this$0.set_status(1, " ");
        }
    }

    public void printByteArray(byte[] bArr, int i) {
        if (i >= 0) {
            for (int i2 = 0; i2 < i; i2++) {
                System.out.print("0x" + Integer.toHexString(bArr[i2]) + " ");
            }
            System.out.println("\n");
        }
    }

    public String parseParameter(String str, String str2) {
        String str3 = "";
        System.out.println("Invoking url's query: " + str);
        if (str == null) {
            return str3;
        }
        String[] split = str.split("[&]");
        int i = 0;
        while (true) {
            if (i >= split.length) {
                break;
            }
            String[] split2 = split[i].split("[=]");
            if (Objects.equals(split2[0], str2)) {
                str3 = split2[1];
                break;
            }
            i++;
        }
        return str3;
    }

    public void sendAltSysReq() {
        byte[] bArr = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        bArr[2] = 4;
        bArr[4] = 70;
        this.remconsObj.session.transmitb(bArr, bArr.length);
        try {
            Thread.currentThread();
            Thread.sleep(250L);
        } catch (Exception e) {
            System.out.println("sendAltSysReq: Failed wait");
        }
        bArr[4] = 0;
        this.remconsObj.session.transmitb(bArr, bArr.length);
    }
}