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
import javax.swing.JLabel;
import javax.swing.JPanel;

public class telnet extends JPanel implements Runnable, MouseListener, FocusListener, KeyListener {

    final LocaleTranslator translator = new LocaleTranslator();
    int ts_type;
    private Aes aes128decrypter;
    private Aes aes256decrypter;
    private RC4 RC4decrypter;
    private boolean screenFocusLost = false;
    private boolean seized = false;
    private final int[] keyMap = new int[256];
    private final int[] winkey_to_hid = {0, 0, 0, 0, 0, 0, 0, 0, 42, 43, 40, 0, 0, 40, 0, 0, 225, 224, 226, 72, 57, 0, 0, 0, 0, 0, 41, 41, 138, 139, 0, 0, 44, 75, 78, 77, 74, 80, 82, 79, 81, 0, 0, 0, 54, 45, 55, 56, 39, 30, 31, 32, 33, 34, 35, 36, 37, 38, 0, 51, 0, 46, 0, 0, 0, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 47, 49, 48, 0, 0, 98, 89, 90, 91, 92, 93, 94, 95, 96, 97, 85, 87, 159, 86, 99, 84, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 0, 0, 0, 76, 0, 0, 35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 71, 0, 0, 0, 0, 36, 37, 52, 54, 70, 73, 0, 0, 0, 0, 55, 47, 48, 228, 226, 230, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 51, 46, 54, 45, 55, 56, 53, 135, 48, 137, 50, 52, 135, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 47, 49, 48, 52, 0, 96, 90, 92, 94, 0, 0, 0, 0, 0, 0, 0, 139, 0, 0, 0, 0, 136, 136, 136, 53, 53, 0, 0, 0, 0, 0, 0, 0, 0, 138, 0, TELNET_IAC};
    private int japanese_kbd;
    protected DataInputStream in;
    protected DataOutputStream out;
    protected Socket s;
    protected String host = "";
    protected String login = "";
    protected Thread receiver;
    protected boolean dvc_encryption = false;
    protected boolean dvc_mode = false;
    protected boolean encryption_enabled = false;
    protected final byte[] decrypt_key = new byte[16];
    protected final dvcwin screen;
    protected int connected = 0;
    protected int port = 23;
    public String st_fld1 = "";
    public String st_fld2 = "";
    public String st_fld3 = "";
    public String st_fld4 = "";
    public boolean post_complete = false;
    public byte[] sessionKey = new byte[32];
    public final JLabel status_box = new JLabel();
    public final cmd cmdObj = new cmd();
    public int cipher = 0;
    public int dbg_print = 0;
    public remcons remconsObj;
    public static final int TELNET_BRK = 243;
    public static final int TELNET_ENCRYPT = 192;
    public static final int TELNET_IAC = 255;
    public static final int TELNET_IP = 244;
    public static final int TELNET_SB = 250;
    public static final int TELNET_SE = 240;

    public telnet(remcons remconsVar) {
        japanese_kbd = 0;
        remconsObj = remconsVar;

        addFocusListener(this);

        screen = new dvcwin(1024, 768, remconsObj);
        screen.addMouseListener(this);
        screen.addFocusListener(this);
        screen.addKeyListener(this);
        System.out.println("Screen: " + screen);

        focusTraversalKeysDisable(screen);
        focusTraversalKeysDisable(this);

        setBackground(Color.black);
        setLayout(new BorderLayout());

        add(screen, "North");

        set_status(1, locinfo.STATUSSTR_300d);
        set_status(2, "          ");
        set_status(3, "          ");
        set_status(4, "          ");

        if (System.getProperty("os.name").toLowerCase().startsWith("windows") && !translator.windows) {
            translator.selectLocale("en_US");
        }

        for (int i = 0; i < 256; i++) {
            keyMap[i] = 0;
        }

        Locale lo = Locale.getDefault();
        String keyboardLayout = lo.toString();

        System.out.println("telent lang: Keyboard layout is " + keyboardLayout);

        if (keyboardLayout.startsWith("ja")) {
            System.out.println("JAPANESE LANGUAGE \n");
            japanese_kbd = 1;
            return;
        }

        japanese_kbd = 0;
    }

    public void enable_debug() {
    }

    public void disable_debug() {
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

    public synchronized void focusGained(FocusEvent focusEvent) {
        if (focusEvent.getComponent() != screen) {
            screen.requestFocus();
        } else if (screenFocusLost) {
            remconsObj.remconsInstallKeyboardHook();
            screenFocusLost = false;
        }
    }

    public synchronized void focusLost(FocusEvent focusEvent) {
        if (focusEvent.getComponent() == screen && focusEvent.isTemporary()) {
            remconsObj.remconsUnInstallKeyboardHook();
            screenFocusLost = true;
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
                st_fld1 = str;
                break;
            case 2:
                st_fld2 = str;
                break;
            case 3:
                st_fld3 = str;
                break;
            case 4:
                st_fld4 = str;
                break;
        }

        status_box.setText(st_fld1 + " " + st_fld2 + "      " + st_fld3 + "      " + st_fld4);
    }

    public void reinit_vars() {
        dvc_encryption = false;
    }

    public void setup_decryption(byte[] bArr) {
        System.arraycopy(bArr, 0, decrypt_key, 0, 16);

        RC4decrypter = new RC4(bArr);
        encryption_enabled = true;
        aes128decrypter = new Aes(0, bArr);
        aes256decrypter = new Aes(0, bArr);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public synchronized void connect(String var1, String var2, int var3, int var4, int var5, remcons var6) {
        ts_type = var4 >> 8;

        if (connected == 0) {
            screen.start_updates();
            connected = 1;
            host = var1;
            login = var2;
            port = var3;
            remconsObj = var6;

            requestFocus();

            sessionKey = var6.ParentApp.getParameter("RCINFO1").getBytes();
            String var7 = var6.ParentApp.rc_port;

            if (var7 != null) {
                try {
                    port = Integer.parseInt(var7);
                    System.out.println("RC port number " + port);
                } catch (NumberFormatException var16) {
                    System.out.println("Failed to read rcport from parameters");
                    port = 23;
                }
            }

            try {
                set_status(1, locinfo.STATUSSTR_3008);
                System.out.println("updated: connecting to " + host + ":" + port);

                try {
                    Thread.currentThread();
                    Thread.sleep(1000L);
                } catch (InterruptedException var12) {
                    System.out.println("connect Thread interrupted..");
                }

                s = new Socket(host, port);

                try {
                    s.setSoLinger(true, 0);
                    System.out.println("set TcpNoDelay");
                    s.setTcpNoDelay(true);
                } catch (SocketException var11) {
                    System.out.println("telnet.connect() linger SocketException: " + var11);
                }

                in = new DataInputStream(s.getInputStream());
                out = new DataOutputStream(s.getOutputStream());

                byte var8 = in.readByte();

                if (var8 == 80) {
                    set_status(1, locinfo.STATUSSTR_3009);

                    boolean var9;

                    System.out.println("Received hello byte. Requesting remote connection...");

                    var9 = requestRemoteConnection(8193); // oooooooooookay...

                    if (var9) {
                        receiver = new Thread(this);
                        receiver.setName("telnet_rcvr");
                        receiver.start();
                        cmdObj.connectCmd(remconsObj, host, port);
                    } else {
                        var6.ParentApp.stop();
                    }
                } else {
                    set_status(1, locinfo.STATUSSTR_300a);

                    System.out.println("Socket connection failure... ");
                }
            } catch (SocketException var13) {
                System.out.println("telnet.connect() SocketException: " + var13);

                set_status(1, var13.toString());

                s = null;
                in = null;
                out = null;
                receiver = null;
                connected = 0;
            } catch (UnknownHostException var14) {
                System.out.println("telnet.connect() UnknownHostException: " + var14);

                set_status(1, var14.toString());

                s = null;
                in = null;
                out = null;
                receiver = null;
                connected = 0;
            } catch (IOException var15) {
                System.out.println("telnet.connect() IOException: " + var15);

                set_status(1, var15.toString());

                s = null;
                in = null;
                out = null;
                receiver = null;
                connected = 0;
            }
        } else {
            requestFocus();
        }
    }

    public boolean requestRemoteConnection(int var1) {
        boolean var2 = false;
        byte var3 = 0;
        byte[] var4 = new byte[2];

        while (var3 != 4) {
            byte var16;
            switch (var3) {
                case 0:
                    var4[0] = (byte) (var1 & 255);
                    var4[1] = (byte) ((var1 & '\uff00') >>> 8);
                    if (remconsObj.ParentApp.optional_features.contains("ENCRYPT_KEY")) {
                        for (int var6 = 0; var6 < sessionKey.length; ++var6) {
                            sessionKey[var6] ^= (byte) remconsObj.ParentApp.enc_key.charAt(var6 % remconsObj.ParentApp.enc_key.length());
                        }

                        if (remconsObj.ParentApp.optional_features.contains("ENCRYPT_VMKEY")) {
                            var4[1] = (byte) (var4[1] | 64);
                        } else {
                            var4[1] = (byte) (var4[1] | 128);
                        }
                    }

                    byte[] var17 = new byte[var4.length + sessionKey.length];
                    System.arraycopy(var4, 0, var17, 0, var4.length);
                    System.arraycopy(sessionKey, 0, var17, var4.length, sessionKey.length);

                    String var7 = new String(var17);
                    transmit(var7);

                    var3 = 1;

                    break;
                case 1:
                    try {
                        var16 = in.readByte();
                    } catch (IOException var15) {
                        var3 = 4;

                        System.out.println("Socket Read failed.");

                        break;
                    }

                    switch (var16) {
                        case 81:
                            System.out.println("Access denied.");

                            set_status(1, locinfo.STATUSSTR_300b);

                            if (null != remconsObj.ParentApp.dispFrame) {
                                new VErrorDialog(remconsObj.ParentApp.dispFrame, locinfo.DIALOGSTR_202f, locinfo.DIALOGSTR_205f, true);
                                remconsObj.ParentApp.dispFrame.setVisible(false);
                            } else {
                                new VErrorDialog(locinfo.DIALOGSTR_205f, true);
                            }

                            var3 = 4;
                            remconsObj.ParentApp.stop();

                            continue;
                        case 82:
                            set_status(1, locinfo.STATUSSTR_300c);

                            System.out.println("Authenticated");

                            var2 = true;
                            remconsObj.licensed = true;
                            var3 = 4;

                            continue;
                        case 83:
                        case 89:
                            System.out.println("Authenticated, but busy, negotiating");
                            int var8;
                            if (0 == remconsObj.retry_connection_count) {
                                var8 = negotiateBusy();

                                System.out.println("negotiateResult:" + var8);
                            } else {
                                System.out.println("Overriding seize option for internal retry");

                                var8 = 1;
                            }

                            switch (var8) {
                                case 0:
                                    System.out.println("Connection cancelled by user");

                                    if (null != remconsObj.ParentApp.dispFrame) {
                                        remconsObj.ParentApp.dispFrame.setVisible(false);
                                    }

                                    var3 = 4;

                                    continue;
                                case 1:
                                    var4[0] = 85;
                                    var4[1] = 0;

                                    byte[] var9 = new byte[var4.length];
                                    System.arraycopy(var4, 0, var9, 0, var4.length);

                                    System.out.println("Seizing connection, sending command 0x0055");

                                    String var10 = new String(var9);
                                    transmit(var10);

                                    var3 = 3;

                                    set_status(1, locinfo.STATUSSTR_3118);

                                    continue;
                                case 2:
                                    var4[0] = 86;
                                    var4[1] = 0;

                                    System.out.println("Sharing connection, sending command 0x0056");

                                    byte[] var11 = new byte[var4.length];
                                    System.arraycopy(var4, 0, var11, 0, var4.length);

                                    String var12 = new String(var11);
                                    transmit(var12);

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

                            remconsObj.licensed = false;
                            var3 = 4;

                            continue;
                        case 88:
                            System.out.println("No free Sessions Notification");

                            if (null != remconsObj.ParentApp.dispFrame) {
                                new VErrorDialog(remconsObj.ParentApp.dispFrame, locinfo.DIALOGSTR_202f, locinfo.DIALOGSTR_2030, true);
                                remconsObj.ParentApp.dispFrame.setVisible(false);
                            } else {
                                new VErrorDialog(locinfo.DIALOGSTR_2030, true);
                            }

                            var3 = 4;
                            remconsObj.ParentApp.stop();
                            continue;
                    }
                case 2:
                    var3 = 4;
                    break;
                case 3:
                    var3 = 4;

                    try {
                        var16 = in.readByte();
                    } catch (IOException var14) {
                        System.out.println("Socket Read failed.");
                        continue;
                    }

                    switch (var16) {
                        case 81:
                            if (null != remconsObj.ParentApp.dispFrame) {
                                new VErrorDialog(remconsObj.ParentApp.dispFrame, locinfo.DIALOGSTR_202f, locinfo.DIALOGSTR_2047, true);
                                remconsObj.ParentApp.dispFrame.setVisible(false);
                            } else {
                                new VErrorDialog(locinfo.DIALOGSTR_2047, true);
                            }

                            break;
                        case 82:
                            remconsObj.ParentApp.moveUItoInit(true);
                            var2 = true;
                    }
            }
        }

        return var2;
    }

    public int negotiateBusy() {
        byte var1 = 0;
        remconsObj.ParentApp.moveUItoInit(false);
        VSeizeDialog var2 = new VSeizeDialog(remconsObj);

        switch (var2.getUserInput()) {
            case 0:
                break;
            case 2:
                var1 = 1;
        }

        return var1;
    }

    public synchronized void disconnect() {
        remconsObj.remconsUnInstallKeyboardHook();

        if (connected == 1) {
            screen.stop_updates();
            connected = 0;

            if (receiver != null && receiver.isAlive()) {
                receiver.stop();
            }

            receiver = null;

            if (s != null) {
                try {
                    System.out.println("Closing socket");
                    s.close();
                } catch (IOException e) {
                    System.out.println("telnet.disconnect() IOException: " + e);
                    set_status(1, e.toString());
                }
            }

            s = null;
            in = null;
            out = null;

            if (cmdObj != null) {
                cmdObj.disconnectCmd();
            }

            set_status(1, locinfo.STATUSSTR_300d);
            reinit_vars();
        }
    }

    public synchronized void transmit(String str) {
        if (out != null && str.length() != 0) {
            byte[] bArr = new byte[str.length()];
            for (int i = 0; i < str.length(); i++) {
                bArr[i] = (byte) str.charAt(i);
            }
            try {
                out.write(bArr, 0, bArr.length);
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
                str = translator.translate(keyChar);
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

    boolean process_dvc(char c) {
        return true;
    }

    @Override
    public void run() {
        byte var3 = 0;
        byte[] var6 = new byte[1024];
        int var10 = 0;
        dvc_mode = true;
        System.out.println("Starting receiver run");

        try {
            while (true) {
                int var7;
                try {
                    if (s == null || in == null) {
                        System.out.println("telnet.run() s or in is null");
                        break;
                    }

                    s.setSoTimeout(1000);
                    var7 = in.read(var6);
                } catch (InterruptedIOException var18) {
                    continue;
                } catch (Exception var19) {
                    var7 = -1;
                    ++var10;
                }

                if (var7 < 0) {
                    if (var10 > 1) {
                        System.out.println("Reading from stream failed for  " + var10 + "times");
                        break;
                    }
                } else {
                    for (int var8 = 0; var8 < var7; ++var8) {
                        if (dbg_print == 1000) {
                            dbg_print = 0;
                        }

                        ++dbg_print;
                        remconsObj.fdConnState = remconsObj.ParentApp.virtdevsObj.fdConnected;
                        remconsObj.cdConnState = remconsObj.ParentApp.virtdevsObj.cdConnected;

                        char var1 = (char) var6[var8];
                        var1 = (char) (var1 & 255);

                        if (dvc_mode) {
                            if (dvc_encryption) {
                                switch (cipher) {
                                    case 1:
                                        char var24 = (char) (RC4decrypter.randomValue() & 255);
                                        var1 ^= var24;
                                        break;
                                    case 2:
                                        char var23 = (char) (aes128decrypter.randomValue() & 255);
                                        var1 ^= var23;
                                        break;
                                    case 3:
                                        char var9 = (char) (aes256decrypter.randomValue() & 255);
                                        var1 ^= var9;
                                        break;
                                    default:
                                        System.out.println("Unknown encryption");
                                }

                                var1 = (char) (var1 & 255);
                            }

                            dvc_mode = process_dvc(var1);

                            if (!dvc_mode) {
                                System.out.println("DVC mode turned off");

                                set_status(1, locinfo.STATUSSTR_300e);
                            }
                        } else if (var1 == 27) {
                            var3 = 1;
                        } else if (var3 == 1 && var1 == '[') {
                            var3 = 2;
                        } else if (var3 == 2 && var1 == 'R') {
                            dvc_mode = true;
                            dvc_encryption = true;

                            set_status(1, locinfo.STATUSSTR_300f);
                        } else if (var3 == 2 && var1 == 'r') {
                            dvc_mode = true;
                            dvc_encryption = false;

                            set_status(1, locinfo.STATUSSTR_3004);
                        } else {
                            var3 = 0;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("telnet.run() Exception, class:" + e.getClass() + "  msg:" + e.getMessage());
            e.printStackTrace();
        } finally {
            if (!seized) {
                if (remconsObj.retry_connection_count < 3) {
                    screen.clearScreen();
                    System.out.println("Retrying connection");
                    set_status(1, locinfo.STATUSSTR_3011);
                } else {
                    screen.clearScreen();
                    System.out.println("offline");
                    set_status(1, locinfo.STATUSSTR_300d);
                }

                set_status(2, "");
                set_status(3, "");
                set_status(4, "");

                System.out.println("Actually Retrying connection");
                remconsObj.retry_connection_flag = true;
            }
        }

        System.out.println("Completed receiver run");
    }

    public void change_key() {
        RC4decrypter.update_key();
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

    public void seize() {
        System.out.println("Received seize command. halting RC.");

        seized = true;
        screen.clearScreen();

        set_status(1, locinfo.STATUSSTR_3012);
        set_status(2, "");
        set_status(3, "");
        set_status(4, "");
    }

    public void fwUpgrade() {
        System.out.println("Received FW Upgrade notification. Halting RC.");

        seized = true;
        screen.clearScreen();

        set_status(1, locinfo.STATUSSTR_3013);
        set_status(2, "");
        set_status(3, "");
        set_status(4, "");
    }

    public void unAuthAccess() {
        System.out.println("Received unAuthAccess notification. Halting RC.");

        seized = true;
        screen.clearScreen();

        set_status(1, locinfo.STATUSSTR_3014);
        set_status(2, "");
        set_status(3, "");
        set_status(4, "");
    }

    public synchronized void sendKey(KeyEvent keyEvent, int i) {
        if (!remconsObj.kbHookInstalled || !remconsObj.kbHookDataRcvd) {
            handleKey(keyEvent, i);
        }
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    public void handleKey(KeyEvent keyEvent, int i) {
        byte[] bArr = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        boolean z = false;
        int keyCode = keyEvent.getKeyCode();

        if (japanese_kbd != 1 || (keyCode != 92 && keyCode != 91 && keyCode != 93 && keyCode != 513)) {
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
                keyMap[keyCode] = 1;
            } else if (9 != keyCode || 0 != keyMap[keyCode]) {
                keyMap[keyCode] = 0;

                if (keyEvent.isAltDown() && keyCode == 154) {
                    sendAltSysReq();
                }
            } else {
                return;
            }

            if (!keyEvent.isAltDown() && 0 != keyMap[18]) {
                keyMap[18] = 0;
            }

            if (isSpecialReleaseKey(keyCode)) {
                keyMap[keyCode] = 1;
            }

            int i2 = 0;

            for (int i3 = 0; i3 < 256; i3++) {
                if (keyMap[i3] == 1) {
                    byte b = (byte) winkey_to_hid[i3];

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
                    keyMap[i4] = 0;
                }
            } else {
                transmitb(bArr, bArr.length);

                if (isSpecialReleaseKey(keyCode)) {
                    keyMap[keyCode] = 0;
                    bArr[9] = 0;
                    bArr[8] = 0;
                    bArr[7] = 0;
                    bArr[6] = 0;
                    bArr[5] = 0;
                    bArr[4] = 0;
                    int i5 = 0;

                    for (int i6 = 0; i6 < 256; i6++) {
                        if (keyMap[i6] == 1) {
                            bArr[4 + i5] = (byte) winkey_to_hid[i6];
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void sendAltSysReq() {
        byte[] bArr = {1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        bArr[2] = 4;
        bArr[4] = 70;
        remconsObj.session.transmitb(bArr, bArr.length);

        try {
            Thread.currentThread();
            Thread.sleep(250L);
        } catch (Exception e) {
            System.out.println("sendAltSysReq: Failed wait");
        }

        bArr[4] = 0;
        remconsObj.session.transmitb(bArr, bArr.length);
    }
}
