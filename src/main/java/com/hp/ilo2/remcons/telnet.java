package com.hp.ilo2.remcons;

import com.hp.ilo2.intgapp.locinfo;
import com.hp.ilo2.virtdevs.MediaAccess;
import com.hp.ilo2.virtdevs.SCSI;
import com.hp.ilo2.virtdevs.VErrorDialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Objects;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class telnet extends JPanel implements Runnable, MouseListener, FocusListener, KeyListener {

    final LocaleTranslator translator = new LocaleTranslator();
    int ts_type;
    private Aes aes128decrypter;
    private Aes aes256decrypter;
    private Process rdpProc = null;
    private RC4 RC4decrypter;
    private boolean screenFocusLost = false;
    private boolean seized = false;
    private final boolean crlf_enabled = false;
    private final boolean tbm_mode = false;
    private final int total_count = 0;
    private final int[] keyMap = new int[256];
    private final int[] winkey_to_hid = {0, 0, 0, 0, 0, 0, 0, 0, 42, 43, 40, 0, 0, 40, 0, 0, 225, 224, 226, 72, 57, 0, 0, 0, 0, 0, 41, 41, 138, 139, 0, 0, 44, 75, 78, 77, 74, 80, 82, 79, 81, 0, 0, 0, 54, 45, 55, 56, 39, 30, 31, 32, 33, 34, 35, 36, 37, 38, 0, 51, 0, 46, 0, 0, 0, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 47, 49, 48, 0, 0, 98, 89, 90, 91, 92, 93, 94, 95, 96, 97, 85, 87, 159, 86, 99, 84, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 0, 0, 0, 76, 0, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 71, 0, 0, 0, 0, 36, 37, 52, 54, 70, 73, 0, 0, 0, 0, 55, 47, 48, 228, 226, 230, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 51, 46, 54, 45, 55, 56, 53, 135, 48, 137, 50, 52, 135, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 47, 49, 48, 52, 0, 96, 90, 92, 94, 0, 0, 0, 0, 0, 0, 0, 139, 0, 0, 0, 0, 136, 136, 136, 53, 53, 0, 0, 0, 0, 0, 0, 0, 0, 138, 0, TELNET_IAC};
    private int japanese_kbd;
    private int terminalServicesPort = 3389;
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
    protected final byte[] decrypt_key = new byte[16];
    protected final dvcwin screen;
    protected int back;
    protected int connected = 0;
    protected int escseq_val_count = 0;
    protected int fore;
    protected int hi_back;
    protected int hi_fore;
    protected int port = 23;
    protected int[] escseq_val = new int[10];
    public String st_fld1 = "";
    public String st_fld2 = "";
    public String st_fld3 = "";
    public String st_fld4 = "";
    public boolean mirror = false;
    public boolean post_complete = false;
    public byte[] sessionKey = new byte[32];
    public final JLabel status_box = new JLabel();
    public final cmd cmdObj = new cmd();
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
        Locale lo = Locale.getDefault();
        String keyboardLayout = lo.toString();
        System.out.println("telent lang: Keyboard layout is " + keyboardLayout);
        if (keyboardLayout.startsWith("ja")) {
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

    public synchronized void connect(String var1, String var2, int var3, int var4, int var5, remcons var6) {
        boolean enable_terminal_services = (var4 & 1) == 1;
        this.ts_type = var4 >> 8;
        if (var5 != 0) {
            this.terminalServicesPort = var5;
        }

        if (this.connected == 0) {
            this.screen.start_updates();
            this.connected = 1;
            this.host = var1;
            this.login = var2;
            this.port = var3;
            this.remconsObj = var6;
            this.requestFocus();
            this.sessionKey = var6.ParentApp.getParameter("RCINFO1").getBytes();
            String var7 = var6.ParentApp.rc_port;
            if (var7 != null) {
                try {
                    this.port = Integer.parseInt(var7);
                    System.out.println("RC port number " + this.port);
                } catch (NumberFormatException var16) {
                    System.out.println("Failed to read rcport from parameters");
                    this.port = 23;
                }
            }

            try {
                this.set_status(1, locinfo.STATUSSTR_3008);
                System.out.println("updated: connecting to " + this.host + ":" + this.port);

                try {
                    Thread.currentThread();
                    Thread.sleep(1000L);
                } catch (InterruptedException var12) {
                    System.out.println("connect Thread interrupted..");
                }

                this.s = new Socket(this.host, this.port);

                try {
                    this.s.setSoLinger(true, 0);
                    System.out.println("set TcpNoDelay");
                    this.s.setTcpNoDelay(true);
                } catch (SocketException var11) {
                    System.out.println("telnet.connect() linger SocketException: " + var11);
                }

                this.in = new DataInputStream(this.s.getInputStream());
                this.out = new DataOutputStream(this.s.getOutputStream());
                byte var8 = this.in.readByte();
                if (var8 == 80) {
                    this.set_status(1, locinfo.STATUSSTR_3009);
                    boolean var9 = false;
                    System.out.println("Received hello byte. Requesting remote connection...");
                    var9 = this.requestRemoteConnection(8193); // oooooooooookay...
                    if (var9) {
                        this.receiver = new Thread(this);
                        this.receiver.setName("telnet_rcvr");
                        this.receiver.start();
                        this.cmdObj.connectCmd(this.remconsObj, this.host, this.port);
                    } else {
                        var6.ParentApp.stop();
                    }
                } else {
                    this.set_status(1, locinfo.STATUSSTR_300a);
                    System.out.println("Socket connection failure... ");
                }
            } catch (SocketException var13) {
                System.out.println("telnet.connect() SocketException: " + var13);
                this.set_status(1, var13.toString());
                this.s = null;
                this.in = null;
                this.out = null;
                this.receiver = null;
                this.connected = 0;
            } catch (UnknownHostException var14) {
                System.out.println("telnet.connect() UnknownHostException: " + var14);
                this.set_status(1, var14.toString());
                this.s = null;
                this.in = null;
                this.out = null;
                this.receiver = null;
                this.connected = 0;
            } catch (IOException var15) {
                System.out.println("telnet.connect() IOException: " + var15);
                this.set_status(1, var15.toString());
                this.s = null;
                this.in = null;
                this.out = null;
                this.receiver = null;
                this.connected = 0;
            }
        } else {
            this.requestFocus();
        }
    }

    public boolean requestRemoteConnection(int var1) {
        boolean var2 = false;
        byte var3 = 0;
        byte[] var4 = new byte[2];
        boolean var5 = false;

        while (var3 != 4) {
            byte var16;
            switch (var3) {
                case 0:
                    var4[0] = (byte) (var1 & 255);
                    var4[1] = (byte) ((var1 & '\uff00') >>> 8);
                    if (this.remconsObj.ParentApp.optional_features.contains("ENCRYPT_KEY")) {
                        for (int var6 = 0; var6 < this.sessionKey.length; ++var6) {
                            this.sessionKey[var6] ^= (byte) this.remconsObj.ParentApp.enc_key.charAt(var6 % this.remconsObj.ParentApp.enc_key.length());
                        }

                        if (this.remconsObj.ParentApp.optional_features.contains("ENCRYPT_VMKEY")) {
                            var4[1] = (byte) (var4[1] | 64);
                        } else {
                            var4[1] = (byte) (var4[1] | 128);
                        }
                    }

                    byte[] var17 = new byte[var4.length + this.sessionKey.length];
                    System.arraycopy(var4, 0, var17, 0, var4.length);
                    System.arraycopy(this.sessionKey, 0, var17, var4.length, this.sessionKey.length);
                    String var7 = new String(var17);
                    this.transmit(var7);
                    var3 = 1;
                    break;
                case 1:
                    try {
                        var16 = this.in.readByte();
                    } catch (IOException var15) {
                        var2 = false;
                        var3 = 4;
                        System.out.println("Socket Read failed.");
                        break;
                    }

                    switch (var16) {
                        case 81:
                            System.out.println("Access denied.");
                            this.set_status(1, locinfo.STATUSSTR_300b);
                            if (null != this.remconsObj.ParentApp.dispFrame) {
                                new VErrorDialog(this.remconsObj.ParentApp.dispFrame, locinfo.DIALOGSTR_202f, locinfo.DIALOGSTR_205f, true);
                                this.remconsObj.ParentApp.dispFrame.setVisible(false);
                            } else {
                                new VErrorDialog(locinfo.DIALOGSTR_205f, true);
                            }

                            var2 = false;
                            var3 = 4;
                            this.remconsObj.ParentApp.stop();
                            continue;
                        case 82:
                            this.set_status(1, locinfo.STATUSSTR_300c);
                            System.out.println("Authenticated");
                            var2 = true;
                            this.remconsObj.licensed = true;
                            var3 = 4;
                            continue;
                        case 83:
                        case 89:
                            System.out.println("Authenticated, but busy, negotiating");
                            int var8;
                            if (0 == this.remconsObj.retry_connection_count) {
                                var8 = this.negotiateBusy();
                                System.out.println("negotiateResult:" + var8);
                            } else {
                                System.out.println("Overriding seize option for internal retry");
                                var8 = 1;
                            }

                            switch (var8) {
                                case 0:
                                    System.out.println("Connection cancelled by user");
                                    if (null != this.remconsObj.ParentApp.dispFrame) {
                                        this.remconsObj.ParentApp.dispFrame.setVisible(false);
                                    }

                                    var2 = false;
                                    var3 = 4;
                                    continue;
                                case 1:
                                    var4[0] = 85;
                                    var4[1] = 0;
                                    byte[] var9 = new byte[var4.length];
                                    System.arraycopy(var4, 0, var9, 0, var4.length);
                                    System.out.println("Seizing connection, sending command 0x0055");
                                    String var10 = new String(var9);
                                    this.transmit(var10);
                                    var3 = 3;
                                    this.set_status(1, locinfo.STATUSSTR_3118);
                                    continue;
                                case 2:
                                    var4[0] = 86;
                                    var4[1] = 0;
                                    System.out.println("Sharing connection, sending command 0x0056");
                                    byte[] var11 = new byte[var4.length];
                                    System.arraycopy(var4, 0, var11, 0, var4.length);
                                    String var12 = new String(var11);
                                    this.transmit(var12);
                                    var3 = 2;
                                default:
                                    continue;
                            }
                        case 84:
                        case 85:
                        case 86:
                        default:
                            System.out.println("rqrmtconn default: " + var16);
                            var2 = true;
                            var3 = 4;
                            continue;
                        case 87:
                            System.out.println("Received No License Notification");
                            this.remconsObj.licensed = false;
                            var2 = false;
                            var3 = 4;
                            continue;
                        case 88:
                            System.out.println("No free Sessions Notification");
                            if (null != this.remconsObj.ParentApp.dispFrame) {
                                new VErrorDialog(this.remconsObj.ParentApp.dispFrame, locinfo.DIALOGSTR_202f, locinfo.DIALOGSTR_2030, true);
                                this.remconsObj.ParentApp.dispFrame.setVisible(false);
                            } else {
                                new VErrorDialog(locinfo.DIALOGSTR_2030, true);
                            }

                            var2 = false;
                            var3 = 4;
                            this.remconsObj.ParentApp.stop();
                            continue;
                    }
                case 2:
                    var2 = false;
                    var3 = 4;
                    break;
                case 3:
                    var3 = 4;

                    try {
                        var16 = this.in.readByte();
                    } catch (IOException var14) {
                        var2 = false;
                        var3 = 4;
                        System.out.println("Socket Read failed.");
                        continue;
                    }

                    switch (var16) {
                        case 81:
                            if (null != this.remconsObj.ParentApp.dispFrame) {
                                new VErrorDialog(this.remconsObj.ParentApp.dispFrame, locinfo.DIALOGSTR_202f, locinfo.DIALOGSTR_2047, true);
                                this.remconsObj.ParentApp.dispFrame.setVisible(false);
                            } else {
                                new VErrorDialog(locinfo.DIALOGSTR_2047, true);
                            }

                            var2 = false;
                            break;
                        case 82:
                            this.remconsObj.ParentApp.moveUItoInit(true);
                            var2 = true;
                    }
            }
        }

        return var2;
    }

    public int negotiateBusy() {
        byte var1 = 0;
        this.remconsObj.ParentApp.moveUItoInit(false);
        VSeizeDialog var2 = new VSeizeDialog(this.remconsObj);
        switch (var2.getUserInput()) {
            case 0:
                var1 = 0;
                break;
            case 2:
                var1 = 1;
        }

        return var1;
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
            boolean decryption_active = false;
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
                } else {
                    str = "\n";
                }
                break;
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
        if (keyEvent.getKeyCode() == MediaAccess.F5_180_512) { /* 9 */
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
        boolean var2 = false;
        byte var3 = 0;
        boolean var4 = false;
        boolean var5 = false;
        byte[] var6 = new byte[1024];
        int var10 = 0;
        this.dvc_mode = true;
        System.out.println("Starting receiver run");

        try {
            while (true) {
                int var7;
                try {
                    if (this.s == null || this.in == null) {
                        System.out.println("telnet.run() s or in is null");
                        break;
                    }

                    this.s.setSoTimeout(1000);
                    var7 = this.in.read(var6);
                } catch (InterruptedIOException var18) {
                    continue;
                } catch (Exception var19) {
                    var7 = -1;
                    ++var10;
                }

                if (var7 < 0) {
                    if (var10 > 1) {
                        System.out.println("Reading from stream failed for  " + var10 + "times");
                        var10 = 0;
                        break;
                    }
                } else {
                    for (int var8 = 0; var8 < var7; ++var8) {
                        if (this.dbg_print == 1000) {
                            this.dbg_print = 0;
                        }

                        ++this.dbg_print;
                        this.remconsObj.fdConnState = this.remconsObj.ParentApp.virtdevsObj.fdConnected;
                        this.remconsObj.cdConnState = this.remconsObj.ParentApp.virtdevsObj.cdConnected;
                        char var1 = (char) var6[var8];
                        var1 = (char) (var1 & 255);
                        if (this.dvc_mode) {
                            if (this.dvc_encryption) {
                                switch (this.cipher) {
                                    case 1:
                                        char var24 = (char) (this.RC4decrypter.randomValue() & 255);
                                        var1 ^= var24;
                                        break;
                                    case 2:
                                        char var23 = (char) (this.aes128decrypter.randomValue() & 255);
                                        var1 ^= var23;
                                        break;
                                    case 3:
                                        char var9 = (char) (this.aes256decrypter.randomValue() & 255);
                                        var1 ^= var9;
                                        break;
                                    default:
                                        boolean var25 = false;
                                        System.out.println("Unknown encryption");
                                }

                                var1 = (char) (var1 & 255);
                            }

                            this.dvc_mode = this.process_dvc(var1);
                            if (!this.dvc_mode) {
                                System.out.println("DVC mode turned off");
                                this.set_status(1, locinfo.STATUSSTR_300e);
                            }
                        } else if (var1 == 27) {
                            var3 = 1;
                        } else if (var3 == 1 && var1 == '[') {
                            var3 = 2;
                        } else if (var3 == 2 && var1 == 'R') {
                            this.dvc_mode = true;
                            this.dvc_encryption = true;
                            this.set_status(1, locinfo.STATUSSTR_300f);
                        } else if (var3 == 2 && var1 == 'r') {
                            this.dvc_mode = true;
                            this.dvc_encryption = false;
                            this.set_status(1, locinfo.STATUSSTR_3004);
                        } else {
                            var3 = 0;
                        }
                    }
                }
            }
        } catch (Exception var20) {
            System.out.println("telnet.run() Exception, class:" + var20.getClass() + "  msg:" + var20.getMessage());
            var20.printStackTrace();
        } finally {
            if (!this.seized) {
                remcons var10001 = this.remconsObj;
                if (this.remconsObj.retry_connection_count < 3) {
                    this.screen.clearScreen();
                    System.out.println("Retrying connection");
                    this.set_status(1, locinfo.STATUSSTR_3011);
                } else {
                    this.screen.clearScreen();
                    System.out.println("offline");
                    this.set_status(1, locinfo.STATUSSTR_300d);
                }

                this.set_status(2, "");
                this.set_status(3, "");
                this.set_status(4, "");
                System.out.println("Actually Retrying connection");
                this.remconsObj.retry_connection_flag = true;
            }

        }

        System.out.println("Completed receiver run");
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
