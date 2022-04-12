package com.hp.ilo2.remcons;

import com.hp.ilo2.intgapp.locinfo;
import com.hp.ilo2.virtdevs.VErrorDialog;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class cim extends telnet implements MouseSyncListener {

    private Aes Aes128encrypter;
    private Aes Aes256encrypter;
    private RC4 RC4encrypter;
    private boolean altlock = false;
    private boolean disable_kbd = false;
    private boolean ignore_next_key = false;
    private boolean unsupportedVideoModeWarned = false;
    private byte mouseBtnState = 0;
    private final int blockWidth = 16;
    private int bitsPerColor = 5;
    private int blockHeight = 16;
    private int screen_x = 1;
    private int screen_y = 1;
    private static String printstring;
    private static boolean debug_msgs;
    private static boolean dvc_process_inhibit;
    private static boolean video_detected;
    private static final int[] bits_to_read = new int[]{0, 1, 1, 1, 1, 1, 2, 3, 5, 5, 1, 1, 3, 3, 8, 1, 1, 7, 1, 1, 3, 7, 1, 1, 8, 1, 7, 0, 1, 3, 7, 1, 4, 0, 0, 0, 1, 0, 1, 7, 7, 5, 5, 1, 8, 8, 1, 4};
    private static final int[] block;
    private static final int[] cmd_p_buff;
    private static final int[] dvc_cc_block = new int[17];
    private static final int[] dvc_cc_color = new int[17];
    private static final int[] dvc_cc_usage = new int[17];
    private static final int[] dvc_getmask = new int[]{0, 1, 3, 7, 15, 31, 63, 127, 255};
    private static final int[] dvc_left = new int[256];
    private static final int[] dvc_reversal = new int[256];
    private static final int[] dvc_right = new int[256];
    private static final int[] next_0 = new int[]{1, 2, 31, 2, 2, 10, 10, 10, 10, 41, 2, 33, 2, 2, 2, 16, 19, 39, 22, 20, 1, 1, 34, 25, 46, 26, 40, 1, 29, 1, 1, 36, 10, 2, 1, 35, 8, 37, 38, 1, 47, 42, 10, 43, 45, 45, 1, 1};
    private static final int[] next_1 = new int[]{1, 15, 3, 11, 11, 10, 10, 10, 10, 41, 11, 12, 2, 2, 2, 17, 18, 39, 23, 21, 1, 1, 28, 24, 46, 27, 40, 1, 30, 1, 1, 35, 10, 2, 1, 35, 9, 37, 38, 1, 47, 42, 10, 0, 45, 45, 24, 1};
    private static int cmd_last;
    private static int cmd_p_count;
    private static int dvc_cc_active = 0;
    private static int dvc_code;
    private static int dvc_decoder_state;
    private static int dvc_green;
    private static int dvc_ib_acc;
    private static int dvc_ib_bcnt;
    private static int dvc_last_color;
    private static int dvc_lastx;
    private static int dvc_lasty;
    private static int dvc_newx;
    private static int dvc_next_state;
    private static int dvc_pixcode;
    private static int dvc_pixel_count;
    private static int dvc_red;
    private static int dvc_size_x;
    private static int dvc_size_y;
    private static int dvc_y_clipped;
    private static int dvc_zero_count;
    private static int fatal_count;
    private static int framerate;
    private static int printchan;
    private static long count_bytes;
    private static long timeout_count;
    protected Cursor current_cursor;
    protected MouseSync mouse_sync = new MouseSync(this);
    public Point mousePrevPosn = new Point(0, 0);
    public boolean UI_dirty = false;
    public byte[] encrypt_key = new byte[16];
    public int[] color_remap_table = new int[32768];

    public cim(remcons remcons2) {
        super(remcons2);

        cim.dvc_reversal[255] = 0;
        current_cursor = Cursor.getDefaultCursor();
        screen.addMouseListener(mouse_sync);
        screen.addMouseMotionListener(mouse_sync);
        screen.addMouseWheelListener(mouse_sync);
        mouse_sync.setListener(this);
    }

    @SuppressWarnings("unused")
    public void setup_encryption(byte[] byArray, int n) {
        System.arraycopy(byArray, 0, encrypt_key, 0, 16);

        RC4encrypter = new RC4(byArray);
        Aes128encrypter = new Aes(0, byArray);
        Aes256encrypter = new Aes(0, byArray);
    }

    public void reinit_vars() {
        super.reinit_vars();

        altlock = false;
        cim.dvc_reversal[255] = 0;
        disable_kbd = false;
        dvc_code = 0;
        dvc_ib_acc = 0;
        dvc_ib_bcnt = 0;
        dvc_process_inhibit = false;
        mouse_sync.restart();
    }

    public void enable_debug() {
        debug_msgs = true;
        super.enable_debug();
        mouse_sync.enableDebug();
    }

    public void disable_debug() {
        debug_msgs = false;
        super.disable_debug();
        mouse_sync.disableDebug();
    }

    public void serverMove(int n, int n2, int n3, int n4) {
        UI_dirty = true;

        if (screen_x > 0 && screen_y > 0) {
            n3 = 3000 * n3 / screen_x;
            n4 = 3000 * n4 / screen_y;
        } else {
            n3 = 3000 * n3;
            n4 = 3000 * n4;
        }

        byte[] byArray = new byte[]{2, 0, (byte) (n3 & 0xFF), (byte) (n3 >> 8), (byte) (n4 & 0xFF), (byte) (n4 >> 8), 0, 0, mouseBtnState, 0};
        String string = new String(byArray);
        transmit(string);
    }

    public synchronized void mouseEntered(MouseEvent mouseEvent) {
        UI_dirty = true;
        super.mouseEntered(mouseEvent);
    }

    public void serverPress(int n) {
        UI_dirty = true;
        send_mouse_press(n);
    }

    public void serverRelease(int n) {
        UI_dirty = true;
        send_mouse_release(n);
    }

    public void serverClick(int n, int n2) {
        UI_dirty = true;
        send_mouse_click(n, n2);
        mouseBtnState = mouseButtonState(n);
    }

    public synchronized void mouseExited(MouseEvent mouseEvent) {
        super.mouseExited(mouseEvent);
        setCursor(Cursor.getDefaultCursor());
    }

    public void enable_keyboard() {
        disable_kbd = false;
    }

    public synchronized void connect(String string, String string2, int n, int n2, int n3, remcons remcons2) {
        super.connect(string, string2, n, n2, n3, remcons2);
    }

    public synchronized void transmit(String string) {
        block10:
        {
            if (out == null || string == null) {
                return;
            }

            if (string.length() == 0)
                break block10;

            byte[] byArray = new byte[string.length()];
            int n = 0;

            while (n < string.length()) {
                byArray[n] = (byte) string.charAt(n);

                if (dvc_encryption) {
                    switch (cipher) {
                        case 1: {
                            char c = (char) (RC4encrypter.randomValue() & 0xFF);
                            byArray[n] = (byte) (byArray[n] ^ c);
                            break;
                        }
                        case 2: {
                            char c = (char) (Aes128encrypter.randomValue() & 0xFF);
                            byArray[n] = (byte) (byArray[n] ^ c);
                            break;
                        }
                        case 3: {
                            char c = (char) (Aes256encrypter.randomValue() & 0xFF);
                            byArray[n] = (byte) (byArray[n] ^ c);
                            break;
                        }
                        default: {
                            System.out.println("Unknown encryption");
                        }
                    }

                    byArray[n] = (byte) (byArray[n] & 0xFF);
                }

                ++n;
            }

            try {
                out.write(byArray, 0, byArray.length);
            } catch (IOException iOException) {
                System.out.println("telnet.transmit() IOException: " + iOException);
            }
        }
    }

    public synchronized void transmitb(byte[] byArray, int n) {
        byte[] byArray2 = new byte[n];
        System.arraycopy(byArray, 0, byArray2, 0, n);
        int n2 = 0;

        while (n2 < n) {
            if (dvc_encryption) {
                switch (cipher) {
                    case 1: {
                        char c = (char) (RC4encrypter.randomValue() & 0xFF);
                        byArray2[n2] = (byte) (byArray2[n2] ^ c);
                        break;
                    }
                    case 2: {
                        char c = (char) (Aes128encrypter.randomValue() & 0xFF);
                        byArray2[n2] = (byte) (byArray2[n2] ^ c);
                        break;
                    }
                    case 3: {
                        char c = (char) (Aes256encrypter.randomValue() & 0xFF);
                        byArray2[n2] = (byte) (byArray2[n2] ^ c);
                        break;
                    }
                    default: {
                        System.out.println("Unknown encryption");
                    }
                }

                byArray2[n2] = (byte) (byArray2[n2] & 0xFF);
            }

            ++n2;
        }

        try {
            if (null != out) {
                out.write(byArray2, 0, n);
            }
        } catch (IOException iOException) {
            System.out.println("telnet.transmitb() IOException: " + iOException);
        }
    }

    public String translate_key(KeyEvent keyEvent) {
        String string = "";
        char c = keyEvent.getKeyChar();
        int n = 0;
        boolean bl = true;

        if (disable_kbd) {
            return "";
        }

        if (ignore_next_key) {
            ignore_next_key = false;
            return "";
        }

        UI_dirty = true;

        if (keyEvent.isShiftDown()) {
            n = 1;
        } else if (keyEvent.isControlDown()) {
            n = 2;
        } else if (altlock || keyEvent.isAltDown()) {
            n = 3;
            if (keyEvent.isAltDown()) {
                keyEvent.consume();
            }
        }

        switch (c) {
            case '': {
                bl = false;
                break;
            }
            case '\n':
            case '\r': {
                switch (n) {
                    case 0: {
                        string = "\r";
                        break;
                    }
                    case 1: {
                        string = "[3\r";
                        break;
                    }
                    case 2: {
                        string = "\n";
                        break;
                    }
                    case 3: {
                        string = "[1\r";
                    }
                }
                bl = false;
                break;
            }
            case '\b': {
                switch (n) {
                    case 0: {
                        string = "\b";
                        break;
                    }
                    case 1: {
                        string = "[3\b";
                        break;
                    }
                    case 2: {
                        string = "";
                        break;
                    }
                    case 3: {
                        string = "[1\b";
                    }
                }
                bl = false;
                break;
            }
            default: {
                string = super.translate_key(keyEvent);
            }
        }

        if (bl && string.length() != 0 && n == 3) {
            string = "[1" + string;
        }

        return string;
    }

    public String translate_special_key(KeyEvent keyEvent) {
        String string = "";
        boolean bl = true;
        int n = 0;

        if (disable_kbd) {
            return "";
        }

        UI_dirty = true;

        if (keyEvent.isShiftDown()) {
            n = 1;
        } else if (keyEvent.isControlDown()) {
            n = 2;
        } else if (altlock || keyEvent.isAltDown()) {
            n = 3;
        }

        switch (keyEvent.getKeyCode()) {
            case 27: {
                string = "";
                break;
            }
            case 9: {
                keyEvent.consume();
                string = "\t";
                break;
            }
            case 127: {
                if (keyEvent.isControlDown() && (altlock || keyEvent.isAltDown())) {
                    send_ctrl_alt_del();
                    return "";
                }
                if (System.getProperty("java.version", "0").compareTo("1.4.2") >= 0) break;
                string = "";
                break;
            }
            case 36: {
                string = "[H";
                break;
            }
            case 35: {
                string = "[F";
                break;
            }
            case 33: {
                string = "[I";
                break;
            }
            case 34: {
                string = "[G";
                break;
            }
            case 155: {
                string = "[L";
                break;
            }
            case 38: {
                string = "[A";
                break;
            }
            case 40: {
                string = "[B";
                break;
            }
            case 37: {
                string = "[D";
                break;
            }
            case 39: {
                string = "[C";
                break;
            }
            case 112: {
                switch (n) {
                    case 0: {
                        string = "[M";
                        break;
                    }
                    case 1: {
                        string = "[Y";
                        break;
                    }
                    case 2: {
                        string = "[k";
                        break;
                    }
                    case 3: {
                        string = "[w";
                    }
                }
                keyEvent.consume();
                bl = false;
                break;
            }
            case 113: {
                switch (n) {
                    case 0: {
                        string = "[N";
                        break;
                    }
                    case 1: {
                        string = "[Z";
                        break;
                    }
                    case 2: {
                        string = "[l";
                        break;
                    }
                    case 3: {
                        string = "[x";
                    }
                }
                keyEvent.consume();
                bl = false;
                break;
            }
            case 114: {
                switch (n) {
                    case 0: {
                        string = "[O";
                        break;
                    }
                    case 1: {
                        string = "[a";
                        break;
                    }
                    case 2: {
                        string = "[m";
                        break;
                    }
                    case 3: {
                        string = "[y";
                    }
                }
                keyEvent.consume();
                bl = false;
                break;
            }
            case 115: {
                switch (n) {
                    case 0: {
                        string = "[P";
                        break;
                    }
                    case 1: {
                        string = "[b";
                        break;
                    }
                    case 2: {
                        string = "[n";
                        break;
                    }
                    case 3: {
                        string = "[z";
                    }
                }
                keyEvent.consume();
                bl = false;
                break;
            }
            case 116: {
                switch (n) {
                    case 0: {
                        string = "[Q";
                        break;
                    }
                    case 1: {
                        string = "[c";
                        break;
                    }
                    case 2: {
                        string = "[o";
                        break;
                    }
                    case 3: {
                        string = "[@";
                    }
                }
                keyEvent.consume();
                bl = false;
                break;
            }
            case 117: {
                switch (n) {
                    case 0: {
                        string = "[R";
                        break;
                    }
                    case 1: {
                        string = "[d";
                        break;
                    }
                    case 2: {
                        string = "[p";
                        break;
                    }
                    case 3: {
                        string = "[[";
                    }
                }
                keyEvent.consume();
                bl = false;
                break;
            }
            case 118: {
                switch (n) {
                    case 0: {
                        string = "[S";
                        break;
                    }
                    case 1: {
                        string = "[e";
                        break;
                    }
                    case 2: {
                        string = "[q";
                        break;
                    }
                    case 3: {
                        string = "[\\";
                    }
                }
                keyEvent.consume();
                bl = false;
                break;
            }
            case 119: {
                switch (n) {
                    case 0: {
                        string = "[T";
                        break;
                    }
                    case 1: {
                        string = "[f";
                        break;
                    }
                    case 2: {
                        string = "[r";
                        break;
                    }
                    case 3: {
                        string = "[]";
                    }
                }
                keyEvent.consume();
                bl = false;
                break;
            }
            case 120: {
                switch (n) {
                    case 0: {
                        string = "[U";
                        break;
                    }
                    case 1: {
                        string = "[g";
                        break;
                    }
                    case 2: {
                        string = "[s";
                        break;
                    }
                    case 3: {
                        string = "[^";
                    }
                }
                keyEvent.consume();
                bl = false;
                break;
            }
            case 121: {
                switch (n) {
                    case 0: {
                        string = "[V";
                        break;
                    }
                    case 1: {
                        string = "[h";
                        break;
                    }
                    case 2: {
                        string = "[t";
                        break;
                    }
                    case 3: {
                        string = "[_";
                    }
                }
                keyEvent.consume();
                bl = false;
                break;
            }
            case 122: {
                switch (n) {
                    case 0: {
                        string = "[W";
                        break;
                    }
                    case 1: {
                        string = "[i";
                        break;
                    }
                    case 2: {
                        string = "[u";
                        break;
                    }
                    case 3: {
                        string = "[`";
                    }
                }
                keyEvent.consume();
                bl = false;
                break;
            }
            case 123: {
                switch (n) {
                    case 0: {
                        string = "[X";
                        break;
                    }
                    case 1: {
                        string = "[j";
                        break;
                    }
                    case 2: {
                        string = "[v";
                        break;
                    }
                    case 3: {
                        string = "['";
                    }
                }
                keyEvent.consume();
                bl = false;
                break;
            }
            default: {
                bl = false;
                string = super.translate_special_key(keyEvent);
            }
        }

        if (string.length() != 0 && bl) {
            switch (n) {
                case 1: {
                    string = "[3" + string;
                    break;
                }
                case 2: {
                    string = "[2" + string;
                    break;
                }
                case 3: {
                    string = "[1" + string;
                }
            }
        }

        return string;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void send_ctrl_alt_del() {
        byte[] byArray = new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byArray[2] = 5;
        byArray[4] = 76;
        String string = new String(byArray);
        transmit(string);

        try {
            Thread.currentThread();
            Thread.sleep(250L);
        } catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }

        byArray[4] = 0;
        String string2 = new String(byArray);
        transmit(string2);

        try {
            Thread.currentThread();
            Thread.sleep(250L);
        } catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }

        byArray[2] = 0;
        String string3 = new String(byArray);
        transmit(string3);

        requestFocus();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void send_num_lock() {
        System.out.println("sending num lock");

        byte[] byArray = new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byArray[4] = 83;
        String string = new String(byArray);
        transmit(string);

        try {
            Thread.currentThread();
            Thread.sleep(250L);
        } catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }

        byArray[4] = 0;
        String string2 = new String(byArray);
        transmit(string2);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void send_caps_lock() {
        System.out.println("sending caps lock");

        byte[] byArray = new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byArray[4] = 57;
        String string = new String(byArray);
        transmit(string);

        try {
            Thread.currentThread();
            Thread.sleep(250L);
        } catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }

        byArray[4] = 0;
        String string2 = new String(byArray);
        transmit(string2);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void send_ctrl_alt_back() {
        byte[] byArray = new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byArray[2] = 5;
        byArray[4] = 42;
        String string = new String(byArray);
        transmit(string);

        try {
            Thread.currentThread();
            Thread.sleep(250L);
        } catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }

        byArray[4] = 0;
        String string2 = new String(byArray);
        transmit(string2);

        try {
            Thread.currentThread();
            Thread.sleep(250L);
        } catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }

        byArray[2] = 0;
        String string3 = new String(byArray);
        transmit(string3);

        requestFocus();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void send_ctrl_alt_fn(int n) {
        byte[] byArray = new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int n2;

        switch (n + 1) {
            case 1: {
                n2 = 58;
                break;
            }
            case 2: {
                n2 = 59;
                break;
            }
            case 3: {
                n2 = 60;
                break;
            }
            case 4: {
                n2 = 61;
                break;
            }
            case 5: {
                n2 = 62;
                break;
            }
            case 6: {
                n2 = 63;
                break;
            }
            case 7: {
                n2 = 64;
                break;
            }
            case 8: {
                n2 = 65;
                break;
            }
            case 9: {
                n2 = 66;
                break;
            }
            case 10: {
                n2 = 67;
                break;
            }
            case 11: {
                n2 = 68;
                break;
            }
            case 12: {
                n2 = 69;
                break;
            }
            default: {
                n2 = 64;
            }
        }

        byArray[2] = 5;
        byArray[4] = (byte) n2;
        String string = new String(byArray);
        transmit(string);

        try {
            Thread.currentThread();
            Thread.sleep(250L);
        } catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }

        byArray[4] = 0;
        String string2 = new String(byArray);
        transmit(string2);

        try {
            Thread.currentThread();
            Thread.sleep(250L);
        } catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }

        byArray[2] = 0;
        String string3 = new String(byArray);
        transmit(string3);

        requestFocus();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void send_alt_fn(int n) {
        byte[] byArray = new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int n2;

        switch (n + 1) {
            case 1: {
                n2 = 58;
                break;
            }
            case 2: {
                n2 = 59;
                break;
            }
            case 3: {
                n2 = 60;
                break;
            }
            case 4: {
                n2 = 61;
                break;
            }
            case 5: {
                n2 = 62;
                break;
            }
            case 6: {
                n2 = 63;
                break;
            }
            case 7: {
                n2 = 64;
                break;
            }
            case 8: {
                n2 = 65;
                break;
            }
            case 9: {
                n2 = 66;
                break;
            }
            case 10: {
                n2 = 67;
                break;
            }
            case 11: {
                n2 = 68;
                break;
            }
            case 12: {
                n2 = 69;
                break;
            }
            default: {
                n2 = 64;
            }
        }

        byArray[2] = 4;
        byArray[4] = (byte) n2;
        String string = new String(byArray);
        transmit(string);

        try {
            Thread.currentThread();
            Thread.sleep(250L);
        } catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }

        byArray[4] = 0;
        String string2 = new String(byArray);
        transmit(string2);

        try {
            Thread.currentThread();
            Thread.sleep(250L);
        } catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }

        byArray[2] = 0;
        String string3 = new String(byArray);
        transmit(string3);

        requestFocus();
    }

    public void sendMomPress() {
        post_complete = false;
        byte[] byArray = new byte[]{0, 0, 0, 0};
        String string = new String(byArray);
        transmit(string);
    }

    public void sendPressHold() {
        post_complete = false;
        byte[] byArray = new byte[]{0, 0, 1, 0};
        String string = new String(byArray);
        transmit(string);
    }

    public void sendPowerCycle() {
        post_complete = false;
        byte[] byArray = new byte[]{0, 0, 2, 0};
        String string = new String(byArray);
        transmit(string);
    }

    public void sendSystemReset() {
        post_complete = false;
        byte[] byArray = new byte[]{0, 0, 3, 0};
        String string = new String(byArray);
        transmit(string);
    }

    @SuppressWarnings("unused")
    public void send_mouse_press(int n) {
    }

    @SuppressWarnings("unused")
    public void send_mouse_release(int n) {
    }

    @SuppressWarnings("unused")
    public void send_mouse_click(int n, int n2) {
    }

    public void refresh_screen() {
        byte[] byArray = new byte[]{5, 0};
        String string = new String(byArray);
        transmit(string);
        requestFocus();
    }

    protected synchronized void set_framerate(int n) {
        framerate = n;

        screen.set_framerate(n);
        set_status(3, "" + framerate);
    }

    final void cache_reset() {
        dvc_cc_active = 0;
    }

    final int cache_lru(int n) {
        int n2 = dvc_cc_active;
        int n3 = 0;
        int n4 = 0;
        int n5 = 0;

        while (n5 < n2) {
            if (n == dvc_cc_color[n5]) {
                n3 = n5;
                n4 = 1;

                break;
            }

            if (dvc_cc_usage[n5] == n2 - 1) {
                n3 = n5;
            }

            ++n5;
        }

        int n6 = dvc_cc_usage[n3];

        if (n4 == 0) {
            if (n2 < 17) {
                n3 = n2;
                n6 = n2++;
                dvc_cc_active = n2;
                dvc_pixcode = dvc_cc_active < 2 ? 38 : (dvc_cc_active == 2 ? 4 : (dvc_cc_active == 3 ? 5 : (dvc_cc_active < 6 ? 6 : (dvc_cc_active < 10 ? 7 : 32))));
                cim.next_1[31] = dvc_pixcode;
            }

            cim.dvc_cc_color[n3] = n;
        }

        cim.dvc_cc_block[n3] = 1;
        n5 = 0;

        while (n5 < n2) {
            if (dvc_cc_usage[n5] < n6) {
                dvc_cc_usage[n5] = dvc_cc_usage[n5] + 1;
            }
            ++n5;
        }

        cim.dvc_cc_usage[n3] = 0;

        return n4;
    }

    final int cache_find(int n) {
        int n2 = dvc_cc_active;
        int n3 = 0;

        while (n3 < n2) {
            if (n == dvc_cc_usage[n3]) {
                int n4 = dvc_cc_color[n3];
                int n5 = n3;
                n3 = 0;

                while (n3 < n2) {
                    if (dvc_cc_usage[n3] < n) {
                        dvc_cc_usage[n3] = dvc_cc_usage[n3] + 1;
                    }
                    ++n3;
                }

                cim.dvc_cc_usage[n5] = 0;
                cim.dvc_cc_block[n5] = 1;

                return n4;
            }

            ++n3;
        }

        return -1;
    }

    final void cache_prune() {
        int n = dvc_cc_active;
        int n2 = 0;

        while (n2 < n) {
            int n3 = dvc_cc_block[n2];
            if (n3 == 0) {
                cim.dvc_cc_block[n2] = dvc_cc_block[--n];
                cim.dvc_cc_color[n2] = dvc_cc_color[n];
                cim.dvc_cc_usage[n2] = dvc_cc_usage[n];

                continue;
            }

            int n4 = n2++;
            dvc_cc_block[n4] = dvc_cc_block[n4] - 1;
        }

        dvc_cc_active = n;
        dvc_pixcode = dvc_cc_active < 2 ? 38 : (dvc_cc_active == 2 ? 4 : (dvc_cc_active == 3 ? 5 : (dvc_cc_active < 6 ? 6 : (dvc_cc_active < 10 ? 7 : 32))));
        cim.next_1[31] = dvc_pixcode;
    }

    protected void next_block(int n) {
        int n2;
        boolean bl = video_detected;

        if (dvc_pixel_count != 0 && dvc_y_clipped > 0 && dvc_lasty == dvc_size_y) {
            int n3 = color_remap_table[0];
            n2 = dvc_y_clipped;

            while (n2 < 256) {
                cim.block[n2] = n3;
                ++n2;
            }
        }

        dvc_pixel_count = 0;
        dvc_next_state = 1;
        int n4 = dvc_lastx * blockWidth;
        n2 = dvc_lasty * blockHeight;

        while (n != 0) {
            if (bl) {
                screen.paste_array(block, n4, n2, 16, blockHeight);
            }

            n4 += 16;

            if (++dvc_lastx >= dvc_size_x)
                break;

            --n;
        }
    }

    protected void init_reversal() {
        int n = 0;
        while (n < 256) {
            int n2 = 8;
            int n3 = 8;
            int n4 = n;
            int n5 = 0;
            int n6 = 0;

            while (n6 < 8) {
                n5 <<= 1;
                if ((n4 & 1) == 1) {
                    if (n2 > n6) {
                        n2 = n6;
                    }
                    n5 |= 1;
                    n3 = 7 - n6;
                }
                n4 >>= 1;
                ++n6;
            }

            cim.dvc_reversal[n] = n5;
            cim.dvc_right[n] = n2;
            cim.dvc_left[n] = n3;

            ++n;
        }
    }

    final void add_bits(char c) {
        dvc_ib_acc |= c << dvc_ib_bcnt;
        dvc_ib_bcnt += 8;

        if ((dvc_zero_count += dvc_right[c]) > 30) {
            dvc_next_state = 43;
            dvc_decoder_state = 43;

            return;
        }

        if (c != '\u0000') {
            dvc_zero_count = dvc_left[c];
        }
    }

    final void get_bits(int n) {
        if (n == 1) {
            dvc_code = dvc_ib_acc & 1;
            dvc_ib_acc >>= 1;
            --dvc_ib_bcnt;

            return;
        }

        if (n == 0) {
            return;
        }

        int n2 = dvc_ib_acc & dvc_getmask[n];
        dvc_ib_bcnt -= n;
        dvc_ib_acc >>= n;
        n2 = dvc_reversal[n2];
        dvc_code = n2 >> (8 - n);
    }

    int process_bits(char c) {
        int n = 0;
        add_bits(c);
        ++count_bytes;
        int n2;
        while (n == 0) {
            n2 = bits_to_read[dvc_decoder_state];

            if (n2 > dvc_ib_bcnt) {
                break;
            }

            int n3;

            dvc_next_state = dvc_code == 0 ? next_0[dvc_decoder_state] : next_1[dvc_decoder_state];
            int dvc_blue;
            int dvc_color;

            block0:
            switch (dvc_decoder_state) {
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 32: {
                    if (dvc_cc_active == 1) {
                        dvc_code = dvc_cc_usage[0];
                    } else if (dvc_decoder_state == 4) {
                        dvc_code = 0;
                    } else if (dvc_decoder_state == 3) {
                        dvc_code = 1;
                    } else if (dvc_code != 0) {
                        ++dvc_code;
                    }
                    dvc_color = cache_find(dvc_code);
                    if (dvc_color == -1) {
                        dvc_next_state = 38;
                        break;
                    }
                    dvc_last_color = color_remap_table[dvc_color];
                    if (dvc_pixel_count >= blockHeight * blockWidth) {
                        dvc_next_state = 38;
                        break;
                    }
                    cim.block[cim.dvc_pixel_count] = dvc_last_color;
                    ++dvc_pixel_count;
                    break;
                }
                case 12: {
                    if (dvc_code == 7) {
                        dvc_next_state = 14;
                        break;
                    }
                    if (dvc_code == 6) {
                        dvc_next_state = 13;
                        break;
                    }
                    dvc_code += 2;
                    int n4 = 0;
                    while (n4 < dvc_code) {
                        if (dvc_pixel_count >= blockHeight * blockWidth) {
                            dvc_next_state = 38;
                            break block0;
                        }
                        cim.block[cim.dvc_pixel_count] = dvc_last_color;
                        ++dvc_pixel_count;
                        ++n4;
                    }
                    break;
                }
                case 13: {
                    dvc_code += 8;
                }
                case 14: {
                    int n4 = 0;
                    while (n4 < dvc_code) {
                        if (dvc_pixel_count >= blockHeight * blockWidth) {
                            dvc_next_state = 38;
                            break block0;
                        }
                        cim.block[cim.dvc_pixel_count] = dvc_last_color;
                        ++dvc_pixel_count;
                        ++n4;
                    }

                    break;
                }
                case 33: {
                    if (dvc_pixel_count >= blockHeight * blockWidth) {
                        dvc_next_state = 38;
                        break;
                    }

                    cim.block[cim.dvc_pixel_count] = dvc_last_color;
                    ++dvc_pixel_count;

                    break;
                }
                case 1:
                case 2:
                case 10:
                case 11:
                case 22:
                case 28:
                case 31:
                case 36:
                case 15:
                case 16:
                case 18:
                case 19:
                case 23:
                case 25: {
                    break;
                }
                case 35: {
                    dvc_next_state = dvc_pixcode;
                    break;
                }
                case 9: {
                    dvc_red = dvc_code << bitsPerColor * 2;
                    break;
                }
                case 41: {
                    dvc_green = dvc_code << bitsPerColor;
                    break;
                }
                case 8: {
                    dvc_red = dvc_code << bitsPerColor * 2;
                    dvc_green = dvc_code << bitsPerColor;
                }
                case 42: {
                    dvc_blue = dvc_code;
                    dvc_color = dvc_red | dvc_green | dvc_blue;
                    n3 = cache_lru(dvc_color);

                    if (n3 != 0) {
                        dvc_next_state = 38;
                        break;
                    }

                    dvc_last_color = color_remap_table[dvc_color];

                    if (dvc_pixel_count >= blockHeight * blockWidth) {
                        dvc_next_state = 38;
                        break;
                    }

                    cim.block[cim.dvc_pixel_count] = dvc_last_color;
                    ++dvc_pixel_count;

                    break;
                }
                case 17:
                case 26: {
                    dvc_newx = dvc_code;

                    if (dvc_decoder_state != 17 || dvc_newx <= dvc_size_x)
                        break;

                    dvc_newx = 0;

                    break;
                }
                case 39: {
                    int dvc_newy = dvc_code;

                    if (blockHeight == 16) {
                        dvc_newy &= 0x7F;
                    }

                    dvc_lastx = dvc_newx;
                    dvc_lasty = dvc_newy;
                    screen.repaint_it(1);

                    break;
                }
                case 20: {
                    dvc_code = dvc_lastx + dvc_code + 1;
                }
                case 21: {
                    dvc_lastx = dvc_code;

                    if (blockHeight == 16) {
                        dvc_lastx &= 0x7F;
                    }

                    if (dvc_lastx <= dvc_size_x || !debug_msgs)
                        break;

                    break;
                }
                case 27: {
                    if (timeout_count == count_bytes - 1L) {
                        dvc_next_state = 38;
                    }

                    if ((dvc_ib_bcnt & 7) != 0) {
                        get_bits(dvc_ib_bcnt & 7);
                    }

                    timeout_count = count_bytes;
                    screen.repaint_it(1);

                    break;
                }
                case 24: {
                    if (cmd_p_count != 0) {
                        cim.cmd_p_buff[cim.cmd_p_count - 1] = cmd_last;
                    }

                    ++cmd_p_count;
                    cmd_last = dvc_code;

                    break;
                }
                case 46: {
                    if (dvc_code != 0) break;
                    switch (cmd_last) {
                        case 1: {
                            dvc_next_state = 37;
                            break;
                        }
                        case 2: {
                            dvc_next_state = 44;
                            break;
                        }
                        case 3: {
                            if (cmd_p_count != 0) {
                                set_framerate(cmd_p_buff[0]);
                                break;
                            }

                            set_framerate(0);

                            break;
                        }
                        case 4: {
                            remconsObj.setPwrStatusPower(1);
                            break;
                        }
                        case 5: {
                            remconsObj.setPwrStatusPower(0);
                            screen.clearScreen();

                            dvc_newx = 50;
                            dvc_code = 38;

                            break;
                        }
                        case 6: {
                            screen.clearScreen();

                            if (!video_detected) {
                                screen.clearScreen();
                            }

                            set_status(2, locinfo.STATUSSTR_3002);
                            set_status(1, " ");
                            set_status(3, " ");
                            set_status(4, " ");

                            post_complete = false;
                            break;
                        }
                        case 7: {
                            ts_type = cmd_p_buff[0];
                            break;
                        }
                        case 8: {
                            break;
                        }
                        case 9: {
                            System.out.println("received keychg and cleared bits\n");

                            if ((dvc_ib_bcnt & 7) == 0)
                                break;

                            get_bits(dvc_ib_bcnt & 7);
                            break;
                        }
                        case 10: {
                            seize();
                            break;
                        }
                        case 11: {
                            System.out.println("Setting bpc to  " + cmd_p_buff[0]);
                            setBitsPerColor(cmd_p_buff[0]);
                            break;
                        }
                        case 12: {
                            System.out.println("Setting encryption to  " + cmd_p_buff[0]);
                            setVideoDecryption(cmd_p_buff[0]);
                            break;
                        }
                        case 13: {
                            System.out.println("Header received ");

                            setBitsPerColor(cmd_p_buff[0]);
                            setVideoDecryption(cmd_p_buff[1]);
                            remconsObj.SetLicensed(cmd_p_buff[2]);
                            remconsObj.SetFlags(cmd_p_buff[3]);

                            break;
                        }
                        case 16: {
                            sendAck();
                            break;
                        }
                        case 128: {
                            screen.invalidate();
                            screen.repaint();

                            break;
                        }
                    }
                    cmd_p_count = 0;
                    break;
                }
                case 44: {
                    printchan = dvc_code;
                    printstring = "";

                    break;
                }
                case 45: {
                    if (dvc_code != 0) {
                        printstring = printstring + (char) dvc_code;
                        break;
                    }

                    switch (printchan) {
                        case 1:
                        case 2: {
                            set_status(2 + printchan, printstring);
                            break;
                        }
                        case 3: {
                            System.out.println(printstring);
                            break;
                        }
                        case 4: {
                            screen.show_text(printstring);
                        }
                    }

                    dvc_next_state = 1;

                    break;
                }
                case 0: {
                    cache_reset();

                    dvc_pixel_count = 0;
                    dvc_lastx = 0;
                    dvc_lasty = 0;
                    dvc_red = 0;
                    dvc_green = 0;
                    fatal_count = 0;
                    timeout_count = -1L;
                    cmd_p_count = 0;

                    break;
                }
                case 38: {
                    if (fatal_count == 40) {
                        refresh_screen();
                    }

                    if (fatal_count == 11680) {
                        refresh_screen();
                    }

                    if (++fatal_count == 120000) {
                        refresh_screen();
                    }

                    if (fatal_count != 12000000)
                        break;

                    refresh_screen();

                    fatal_count = 41;

                    break;
                }
                case 34: {
                    next_block(1);
                    break;
                }
                case 29: {
                    dvc_code += 2;
                }
                case 30: {
                    next_block(dvc_code);
                    break;
                }
                case 40: {
                    dvc_size_x = dvc_newx;
                    dvc_size_y = dvc_code;
                    break;
                }
                case 47: {
                    dvc_lastx = 0;
                    dvc_lasty = 0;
                    dvc_pixel_count = 0;

                    cache_reset();

                    screen_x = dvc_size_x * blockWidth;
                    screen_y = dvc_size_y * 16 + dvc_code;
                    video_detected = screen_x != 0 && screen_y != 0;
                    dvc_y_clipped = dvc_code > 0 ? 256 - 16 * dvc_code : 0;

                    if (!video_detected) {
                        screen.clearScreen();
                        set_status(2, locinfo.STATUSSTR_3002);
                        set_status(1, " ");
                        set_status(3, " ");
                        set_status(4, " ");
                        System.out.println("No video. image_source = " + screen.image_source);
                        post_complete = false;
                        break;
                    }

                    screen.set_abs_dimensions(screen_x, screen_y);
                    SetHalfHeight();
                    mouse_sync.serverScreen(screen_x, screen_y);

                    set_status(2, locinfo.STATUSSTR_3003 + screen_x + "x" + screen_y);
                    set_status(1, " ");

                    break;
                }
                case 43: {
                    if (dvc_next_state == dvc_decoder_state)
                        break;

                    dvc_ib_bcnt = 0;
                    dvc_ib_acc = 0;
                    dvc_zero_count = 0;
                    count_bytes = 0L;

                    break;
                }
                case 37: {
                    return 1;
                }
            }

            if (dvc_next_state == 2 && dvc_pixel_count == blockHeight * blockWidth) {
                next_block(1);
                cache_prune();
            }

            if (dvc_decoder_state == dvc_next_state && dvc_decoder_state != 45 && dvc_decoder_state != 38 && dvc_decoder_state != 43) {
                System.out.println("Machine hung in state " + dvc_decoder_state);
                n = 6;

                continue;
            }

            dvc_decoder_state = dvc_next_state;
        }

        return n;
    }

    @SuppressWarnings("ConstantConditions")
    boolean process_dvc(char c) {
        boolean bl;
        int n;

        if (dvc_reversal[255] == 0) {
            System.out.println("dvc initializing");

            init_reversal();
            cache_reset();

            dvc_decoder_state = 0;
            dvc_next_state = 0;
            dvc_zero_count = 0;
            dvc_ib_acc = 0;
            dvc_ib_bcnt = 0;

            buildPixelTable(bitsPerColor);
            SetHalfHeight();
        }

        if ((n = !dvc_process_inhibit ? process_bits(c) : 0) == 0) {
            bl = true;
        } else {
            System.out.println("Exit from DVC mode status =" + n);
            System.out.println("Current block at " + dvc_lastx + " " + dvc_lasty);
            System.out.println("Byte count " + count_bytes);

            bl = true;
            dvc_decoder_state = 38;
            dvc_next_state = 38;
            fatal_count = 0;

            refresh_screen();
        }

        return bl;
    }

    public void change_key() {
        RC4encrypter.update_key();
        super.change_key();
    }

    private void SetHalfHeight() {
        if (screen_x > 1616) {
            if (remconsObj.halfHeightCapable) {
                if (8 != blockHeight) {
                    System.out.println("Setting halfheight mode on supported system");

                    blockHeight = 8;
                    cim.bits_to_read[21] = 8;
                    cim.bits_to_read[17] = 8;
                    cim.bits_to_read[39] = 8;
                    cim.bits_to_read[30] = 8;
                }
            } else if (!unsupportedVideoModeWarned) {
                new VErrorDialog(remconsObj.ParentApp.dispFrame, locinfo.DIALOGSTR_2021, locinfo.DIALOGSTR_2022, false);
                unsupportedVideoModeWarned = true;
            }
        } else if (16 != blockHeight) {
            System.out.println("Setting non-halfheight mode");

            blockHeight = 16;
            cim.bits_to_read[21] = 7;
            cim.bits_to_read[17] = 7;
            cim.bits_to_read[39] = 7;
            cim.bits_to_read[30] = 7;
        }
    }

    void buildPixelTable(int n) {
        int n2 = 1 << n * 3;
        switch (n) {
            case 5: {
                int n3 = 0;
                while (n3 < n2) {
                    color_remap_table[n3] = (n3 & 0x1F) << 3;

                    int n4 = n3;
                    color_remap_table[n4] = color_remap_table[n4] | (n3 & 0x3E0) << 6;

                    int n5 = n3;
                    color_remap_table[n5] = color_remap_table[n5] | (n3 & 0x7C00) << 9;

                    ++n3;
                }

                break;
            }
            case 4: {
                int n6 = 0;
                while (n6 < n2) {
                    color_remap_table[n6] = (n6 & 0xF) << 4;

                    int n7 = n6;
                    color_remap_table[n7] = color_remap_table[n7] | (n6 & 0xF0) << 8;

                    int n8 = n6;
                    color_remap_table[n8] = color_remap_table[n8] | (n6 & 0xF00) << 12;

                    ++n6;
                }

                break;
            }
            case 3: {
                int n9 = 0;
                while (n9 < n2) {
                    color_remap_table[n9] = (n9 & 0xF) << 5;

                    int n10 = n9;
                    color_remap_table[n10] = color_remap_table[n10] | (n9 & 0xF0) << 11;

                    int n11 = n9;
                    color_remap_table[n11] = color_remap_table[n11] | (n9 & 0xF00) << 15;

                    ++n9;
                }

                break;
            }
            case 2: {
                int n12 = 0;
                while (n12 < n2) {
                    color_remap_table[n12] = (n12 & 0xF) << 6;

                    int n13 = n12;
                    color_remap_table[n13] = color_remap_table[n13] | (n12 & 0xF0) << 15;

                    int n14 = n12;
                    color_remap_table[n14] = color_remap_table[n14] | (n12 & 0xF00) << 18;

                    ++n12;
                }

                break;
            }
        }
    }

    void setBitsPerColor(int n) {
        cim.bits_to_read[8] = bitsPerColor = 5 - (n & 3);
        cim.bits_to_read[9] = bitsPerColor;
        cim.bits_to_read[41] = bitsPerColor;
        cim.bits_to_read[42] = bitsPerColor;

        buildPixelTable(bitsPerColor);
    }

    void setVideoDecryption(int n) {
        switch (n) {
            case 0: {
                dvc_encryption = false;
                cipher = 0;
                remconsObj.setPwrStatusEncLabel(locinfo.STATUSSTR_3004);
                remconsObj.setPwrStatusEnc(0);

                System.out.println("Setting encryption -> None");

                break;
            }
            case 1: {
                dvc_encryption = true;
                remconsObj.setPwrStatusEncLabel(locinfo.STATUSSTR_3005);
                remconsObj.setPwrStatusEnc(1);
                dvc_mode = true;
                cipher = 1;

                System.out.println("Setting encryption -> RC4 - 128 bit");

                break;
            }
            case 2: {
                dvc_encryption = true;
                remconsObj.setPwrStatusEncLabel(locinfo.STATUSSTR_3006);
                remconsObj.setPwrStatusEnc(1);
                dvc_mode = true;
                cipher = 2;

                System.out.println("Setting encryption -> AES - 128 bit");

                break;
            }
            case 3: {
                dvc_encryption = true;
                remconsObj.setPwrStatusEncLabel(locinfo.STATUSSTR_3007);
                remconsObj.setPwrStatusEnc(1);
                dvc_mode = true;
                cipher = 3;

                System.out.println("Setting encryption -> AES - 256 bit");

                break;
            }
            default: {
                dvc_encryption = false;
                remconsObj.setPwrStatusEncLabel(locinfo.STATUSSTR_3004);
                remconsObj.setPwrStatusEnc(0);

                System.out.println("Unsupported encryption");
            }
        }
    }

    public byte mouseButtonState(int n) {
        byte by = 0;

        switch (n) {
            case 4: {
                by = (byte) (by | 1);
                break;
            }
            case 2: {
                by = (byte) (by | 4);
                break;
            }
            case 1: {
                by = (byte) (by | 2);
            }
        }

        return by;
    }

    public byte getMouseButtonState(MouseEvent mouseEvent) {
        byte by = 0;

        if ((((InputEvent) mouseEvent).getModifiersEx() & 0x1000) != 0) {
            by = (byte) (by | 2);
        }

        if ((((InputEvent) mouseEvent).getModifiersEx() & 0x800) != 0) {
            by = (byte) (by | 4);
        }

        if ((((InputEvent) mouseEvent).getModifiersEx() & 0x400) != 0) {
            by = (byte) (by | 1);
        }

        return by;
    }

    public void sendMouse(MouseEvent mouseEvent) {
        Point point;
        Point point2 = new Point(0, 0);
        point = getAbsMouseCoordinates(mouseEvent);
        char c = (char) point.x;
        char c2 = (char) point.y;

        if ((((InputEvent) mouseEvent).getModifiersEx() & 0x80) > 0) {
            mousePrevPosn.x = c;
            mousePrevPosn.y = c2;
        } else if (c <= screen_x && c2 <= screen_y) {
            point2.x = c - mousePrevPosn.x;
            point2.y = mousePrevPosn.y - c2;

            mousePrevPosn.x = c;
            mousePrevPosn.y = c2;

            int n = point2.x;
            int n2 = point2.y;

            if (n < -127) {
                n = -127;
            } else if (n > 127) {
                n = 127;
            }

            if (n2 < -127) {
                n2 = -127;
            } else if (n2 > 127) {
                n2 = 127;
            }

            UI_dirty = true;

            if (screen_x > 0 && screen_y > 0) {
                c = (char) (3000 * c / screen_x);
                c2 = (char) (3000 * c2 / screen_y);
            } else {
                c = (char) (3000 * c);
                c2 = (char) (3000 * c2);
            }

            byte[] byArray = new byte[]{2, 0, (byte) (c & 0xFF), (byte) (c >> 8), (byte) (c2 & 0xFF), (byte) (c2 >> 8), (byte) (n & 0xFF), (byte) (n2 & 0xFF), getMouseButtonState(mouseEvent), 0};
            transmitb(byArray, byArray.length);
        }
    }

    private Point getAbsMouseCoordinates(MouseEvent mouseEvent) {
        Point point = new Point();
        point.y = mouseEvent.getY();
        point.x = mouseEvent.getX();

        return point;
    }

    private void sendAck() {
        byte[] byArray = new byte[]{12, 0};
        String string = new String(byArray);

        transmit(string);
    }

    public void requestScreenFocus(MouseEvent mouseEvent) {
        requestFocus();
    }

    public void installKeyboardHook() {
        remconsObj.remconsInstallKeyboardHook();
    }

    public void unInstallKeyboardHook() {
        remconsObj.remconsUnInstallKeyboardHook();
    }

    static {
        block = new int[256];
        cmd_last = 0;
        cmd_p_buff = new int[256];
        cmd_p_count = 0;
        count_bytes = 0L;
        debug_msgs = false;
        dvc_code = 0;
        dvc_decoder_state = 0;
        dvc_ib_acc = 0;
        dvc_ib_bcnt = 0;
        dvc_next_state = 0;
        dvc_pixcode = 38;
        dvc_process_inhibit = false;
        dvc_zero_count = 0;
        framerate = 30;
        printchan = 0;
        printstring = "";
        timeout_count = 0L;
        video_detected = true;
    }
}
 