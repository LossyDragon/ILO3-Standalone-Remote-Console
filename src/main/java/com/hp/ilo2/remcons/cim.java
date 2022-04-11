package com.hp.ilo2.remcons;

import com.hp.ilo2.virtdevs.VErrorDialog;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.MemoryImageSource;
import java.io.IOException;
import java.lang.reflect.Method;

public class cim
        extends telnet
        implements MouseSyncListener {
    private static final int CMD_MOUSE_MOVE = 208;
    private static final int CMD_BUTTON_PRESS = 209;
    private static final int CMD_BUTTON_RELEASE = 210;
    private static final int CMD_BUTTON_CLICK = 211;
    private static final int CMD_BYTE = 212;
    private static final int CMD_SET_MODE = 213;
    private static final char MOUSE_USBABS = '';
    private static final char MOUSE_USBREL = '';
    static final int CMD_ENCRYPT = 192;
    public static final int MOUSE_BUTTON_LEFT = 4;
    public static final int MOUSE_BUTTON_CENTER = 2;
    public static final int MOUSE_BUTTON_RIGHT = 1;
    private char prev_char = (char)32;
    private boolean disable_kbd = false;
    private boolean altlock = false;
    private static final int block_width = 16;
    private static final int block_height = 16;
    public int[] color_remap_table = new int[32768];
    private int scale_x = 1;
    private int scale_y = 1;
    private int screen_x = 1;
    private int screen_y = 1;
    private int mouse_protocol = 0;
    protected MouseSync mouse_sync = new MouseSync(this);
    public boolean UI_dirty = false;
    private boolean sending_encrypt_command = false;
    public byte[] encrypt_key = new byte[16];
    private RC4 RC4encrypter;
    private Aes Aes128encrypter;
    private Aes Aes256encrypter;
    private int key_index = 0;
    private int bitsPerColor = 5;
    public Point mousePrevPosn = new Point(0, 0);
    private byte mouseBtnState = 0;
    private static final int RESET = 0;
    private static final int START = 1;
    private static final int PIXELS = 2;
    private static final int PIXLRU1 = 3;
    private static final int PIXLRU0 = 4;
    private static final int PIXCODE1 = 5;
    private static final int PIXCODE2 = 6;
    private static final int PIXCODE3 = 7;
    private static final int PIXGREY = 8;
    private static final int PIXRGBR = 9;
    private static final int PIXRPT = 10;
    private static final int PIXRPT1 = 11;
    private static final int PIXRPTSTD1 = 12;
    private static final int PIXRPTSTD2 = 13;
    private static final int PIXRPTNSTD = 14;
    private static final int CMD = 15;
    private static final int CMD0 = 16;
    private static final int MOVEXY0 = 17;
    private static final int EXTCMD = 18;
    private static final int CMDX = 19;
    private static final int MOVESHORTX = 20;
    private static final int MOVELONGX = 21;
    private static final int BLKRPT = 22;
    private static final int EXTCMD1 = 23;
    private static final int FIRMWARE = 24;
    private static final int EXTCMD2 = 25;
    private static final int MODE0 = 26;
    private static final int TIMEOUT = 27;
    private static final int BLKRPT1 = 28;
    private static final int BLKRPTSTD = 29;
    private static final int BLKRPTNSTD = 30;
    private static final int PIXFAN = 31;
    private static final int PIXCODE4 = 32;
    private static final int PIXDUP = 33;
    private static final int BLKDUP = 34;
    private static final int PIXCODE = 35;
    private static final int PIXSPEC = 36;
    private static final int EXIT = 37;
    private static final int LATCHED = 38;
    private static final int MOVEXY1 = 39;
    private static final int MODE1 = 40;
    private static final int PIXRGBG = 41;
    private static final int PIXRGBB = 42;
    private static final int HUNT = 43;
    private static final int PRINT0 = 44;
    private static final int PRINT1 = 45;
    private static final int CORP = 46;
    private static final int MODE2 = 47;
    private static final int SIZE_OF_ALL = 48;
    private static int[] bits_to_read = new int[]{0, 1, 1, 1, 1, 1, 2, 3, 5, 5, 1, 1, 3, 3, 8, 1, 1, 7, 1, 1, 3, 7, 1, 1, 8, 1, 7, 0, 1, 3, 7, 1, 4, 0, 0, 0, 1, 0, 1, 7, 7, 5, 5, 1, 8, 8, 1, 4};
    private static int[] next_0 = new int[]{1, 2, 31, 2, 2, 10, 10, 10, 10, 41, 2, 33, 2, 2, 2, 16, 19, 39, 22, 20, 1, 1, 34, 25, 46, 26, 40, 1, 29, 1, 1, 36, 10, 2, 1, 35, 8, 37, 38, 1, 47, 42, 10, 43, 45, 45, 1, 1};
    private static int[] next_1 = new int[]{1, 15, 3, 11, 11, 10, 10, 10, 10, 41, 11, 12, 2, 2, 2, 17, 18, 39, 23, 21, 1, 1, 28, 24, 46, 27, 40, 1, 30, 1, 1, 35, 10, 2, 1, 35, 9, 37, 38, 1, 47, 42, 10, 0, 45, 45, 24, 1};
    private static int dvc_cc_active = 0;
    private static int[] dvc_cc_color = new int[17];
    private static int[] dvc_cc_usage = new int[17];
    private static int[] dvc_cc_block = new int[17];
    private static int[] dvc_lru_lengths = new int[]{0, 0, 0, 1, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4};
    private static int[] dvc_getmask = new int[]{0, 1, 3, 7, 15, 31, 63, 127, 255};
    private static int[] dvc_reversal = new int[256];
    private static int[] dvc_left = new int[256];
    private static int[] dvc_right = new int[256];
    private static int dvc_pixel_count;
    private static int dvc_size_x;
    private static int dvc_size_y;
    private static int dvc_y_clipped;
    private static int dvc_lastx;
    private static int dvc_lasty;
    private static int dvc_newx;
    private static int dvc_newy;
    private static int dvc_color;
    private static int dvc_last_color;
    private static int dvc_ib_acc;
    private static int dvc_ib_bcnt;
    private static int dvc_zero_count;
    private static int dvc_decoder_state;
    private static int dvc_next_state;
    private static int dvc_pixcode;
    private static int dvc_code;
    private static int[] block;
    private static int dvc_red;
    private static int dvc_green;
    private static int dvc_blue;
    private static int fatal_count;
    private static int printchan;
    private static String printstring;
    private static long count_bytes;
    private static int[] cmd_p_buff;
    private static int cmd_p_count;
    private static int cmd_last;
    private static int framerate;
    private static boolean debug_msgs;
    private static char last_bits;
    private static char last_bits2;
    private static char last_bits3;
    private static char last_bits4;
    private static char last_bits5;
    private static char last_bits6;
    private static char last_bits7;
    private static int last_len;
    private static int last_len1;
    private static int last_len2;
    private static int last_len3;
    private static int last_len4;
    private static int last_len5;
    private static int last_len6;
    private static int last_len7;
    private static int last_len8;
    private static int last_len9;
    private static int last_len10;
    private static int last_len11;
    private static int last_len12;
    private static int last_len13;
    private static int last_len14;
    private static int last_len15;
    private static int last_len16;
    private static int last_len17;
    private static int last_len18;
    private static int last_len19;
    private static int last_len20;
    private static int last_len21;
    private static char dvc_new_bits;
    private static int debug_lastx;
    private static int debug_lasty;
    private static int debug_show_block;
    private static long timeout_count;
    private static long dvc_counter_block;
    private static long dvc_counter_bits;
    private static boolean show_bitsblk_count;
    private static long show_slices;
    private static boolean dvc_process_inhibit;
    private static boolean video_detected;
    private boolean ignore_next_key = false;
    private int blockHeight = 16;
    private int blockWidth = 16;
    private boolean unsupportedVideoModeWarned = false;
    private static final int B = -16777216;
    private static final int W = -8355712;
    private static final byte[] cursor_none;
    private static final int[] cursor_outline;
    protected Cursor current_cursor;
    static Class class$java$awt$Toolkit;
    static Class class$java$awt$Image;
    static Class class$java$awt$Point;
    static Class class$java$lang$String;

    public String getLocalString(int n) {
        String string = "";
        try {
            string = this.remconsObj.ParentApp.locinfoObj.getLocString(n);
        }
        catch (Exception exception) {
            System.out.println("cim:getLocalString" + exception.getMessage());
        }
        return string;
    }

    public cim(remcons remcons2) {
        super(remcons2);
        cim.dvc_reversal[255] = 0;
        this.current_cursor = Cursor.getDefaultCursor();
        this.screen.addMouseListener(this.mouse_sync);
        this.screen.addMouseMotionListener(this.mouse_sync);
        this.screen.addMouseWheelListener(this.mouse_sync);
        this.mouse_sync.setListener(this);
    }

    public void setup_encryption(byte[] byArray, int n) {
        System.arraycopy(byArray, 0, this.encrypt_key, 0, 16);
        this.RC4encrypter = new RC4(byArray);
        this.Aes128encrypter = new Aes(0, byArray);
        this.Aes256encrypter = new Aes(0, byArray);
        this.key_index = n;
    }

    public void reinit_vars() {
        super.reinit_vars();
        dvc_code = 0;
        dvc_ib_acc = 0;
        dvc_ib_bcnt = 0;
        dvc_counter_bits = 0L;
        this.prev_char = (char)32;
        this.disable_kbd = false;
        this.altlock = false;
        cim.dvc_reversal[255] = 0;
        this.scale_x = 1;
        this.scale_y = 1;
        this.mouse_sync.restart();
        dvc_process_inhibit = false;
    }

    public void enable_debug() {
        debug_msgs = true;
        super.enable_debug();
        this.mouse_sync.enableDebug();
    }

    public void disable_debug() {
        debug_msgs = false;
        super.disable_debug();
        this.mouse_sync.disableDebug();
    }

    public void sync_start() {
        this.mouse_sync.sync();
    }

    public void serverMove(int n, int n2, int n3, int n4) {
        if (n < -128) {
            n = -128;
        } else if (n > 127) {
            n = 127;
        }
        if (n2 < -128) {
            n2 = -128;
        } else if (n2 > 127) {
            n2 = 127;
        }
        this.UI_dirty = true;
        if (this.screen_x > 0 && this.screen_y > 0) {
            n3 = 3000 * n3 / this.screen_x;
            n4 = 3000 * n4 / this.screen_y;
        } else {
            n3 = 3000 * n3 / 1;
            n4 = 3000 * n4 / 1;
        }
        byte[] byArray = new byte[]{2, 0, (byte)(n3 & 0xFF), (byte)(n3 >> 8), (byte)(n4 & 0xFF), (byte)(n4 >> 8), 0, 0, this.mouseBtnState, 0};
        String string = new String(byArray);
        this.transmit(string);
    }

    public void mouse_mode_change(boolean bl) {
        int n = bl ? 1 : 2;
    }

    public synchronized void mouseEntered(MouseEvent mouseEvent) {
        this.UI_dirty = true;
        super.mouseEntered(mouseEvent);
    }

    public void serverPress(int n) {
        this.UI_dirty = true;
        this.send_mouse_press(n);
    }

    public void serverRelease(int n) {
        this.UI_dirty = true;
        this.send_mouse_release(n);
    }

    public void serverClick(int n, int n2) {
        this.UI_dirty = true;
        this.send_mouse_click(n, n2);
        this.mouseBtnState = this.mouseButtonState(n);
    }

    public synchronized void mouseExited(MouseEvent mouseEvent) {
        super.mouseExited(mouseEvent);
        this.setCursor(Cursor.getDefaultCursor());
    }

    public void disable_keyboard() {
        this.disable_kbd = true;
    }

    public void enable_keyboard() {
        this.disable_kbd = false;
    }

    public void disable_altlock() {
        this.altlock = false;
    }

    public void enable_altlock() {
        this.altlock = true;
    }

    public synchronized void connect(String string, String string2, int n, int n2, int n3, remcons remcons2) {
        char[] cArray = new char[]{'ÿ', 'À'};
        super.connect(string, string2, n, n2, n3, remcons2);
    }

    public synchronized void transmit(String string) {
        block10: {
            if (this.out == null || string == null) {
                return;
            }
            if (string.length() == 0) break block10;
            byte[] byArray = new byte[string.length()];
            int n = 0;
            while (n < string.length()) {
                byArray[n] = (byte)string.charAt(n);
                if (this.dvc_encryption) {
                    switch (this.cipher) {
                        case 1: {
                            char c = (char)(this.RC4encrypter.randomValue() & 0xFF);
                            byArray[n] = (byte)(byArray[n] ^ c);
                            break;
                        }
                        case 2: {
                            char c = (char)(this.Aes128encrypter.randomValue() & 0xFF);
                            byArray[n] = (byte)(byArray[n] ^ c);
                            break;
                        }
                        case 3: {
                            char c = (char)(this.Aes256encrypter.randomValue() & 0xFF);
                            byArray[n] = (byte)(byArray[n] ^ c);
                            break;
                        }
                        default: {
                            char c = '\u0000';
                            System.out.println("Unknown encryption");
                        }
                    }
                    int n2 = n;
                    byArray[n2] = (byte)(byArray[n2] & 0xFF);
                }
                ++n;
            }
            try {
                this.out.write(byArray, 0, byArray.length);
            }
            catch (IOException iOException) {
                System.out.println("telnet.transmit() IOException: " + iOException);
            }
        }
    }

    public synchronized void transmitb(byte[] byArray, int n) {
        byte[] byArray2 = new byte[n];
        System.arraycopy(byArray, 0, byArray2, 0, n);
        int n2 = 0;
        while (n2 < n) {
            if (this.dvc_encryption) {
                switch (this.cipher) {
                    case 1: {
                        char c = (char)(this.RC4encrypter.randomValue() & 0xFF);
                        byArray2[n2] = (byte)(byArray2[n2] ^ c);
                        break;
                    }
                    case 2: {
                        char c = (char)(this.Aes128encrypter.randomValue() & 0xFF);
                        byArray2[n2] = (byte)(byArray2[n2] ^ c);
                        break;
                    }
                    case 3: {
                        char c = (char)(this.Aes256encrypter.randomValue() & 0xFF);
                        byArray2[n2] = (byte)(byArray2[n2] ^ c);
                        break;
                    }
                    default: {
                        char c = '\u0000';
                        System.out.println("Unknown encryption");
                    }
                }
                int n3 = n2;
                byArray2[n3] = (byte)(byArray2[n3] & 0xFF);
            }
            ++n2;
        }
        try {
            if (null != this.out) {
                this.out.write(byArray2, 0, n);
            }
        }
        catch (IOException iOException) {
            System.out.println("telnet.transmitb() IOException: " + iOException);
        }
    }

    public String translate_key(KeyEvent keyEvent) {
        String string = "";
        char c = keyEvent.getKeyChar();
        int n = 0;
        boolean bl = true;
        if (this.disable_kbd) {
            return "";
        }
        if (this.ignore_next_key) {
            this.ignore_next_key = false;
            return "";
        }
        this.UI_dirty = true;
        if (keyEvent.isShiftDown()) {
            n = 1;
        } else if (keyEvent.isControlDown()) {
            n = 2;
        } else if (this.altlock || keyEvent.isAltDown()) {
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
        if (this.disable_kbd) {
            return "";
        }
        this.UI_dirty = true;
        if (keyEvent.isShiftDown()) {
            n = 1;
        } else if (keyEvent.isControlDown()) {
            n = 2;
        } else if (this.altlock || keyEvent.isAltDown()) {
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
                if (keyEvent.isControlDown() && (this.altlock || keyEvent.isAltDown())) {
                    this.send_ctrl_alt_del();
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

    protected String translate_special_key_release(KeyEvent keyEvent) {
        String string = "";
        int n = 0;
        if (keyEvent.isShiftDown()) {
            n = 1;
        }
        if (this.altlock || keyEvent.isAltDown()) {
            n += 2;
        }
        if (keyEvent.isControlDown()) {
            n += 4;
        }
        switch (keyEvent.getKeyCode()) {
            case 243:
            case 244:
            case 263: {
                n += 128;
                break;
            }
            case 29: {
                n += 136;
                break;
            }
            case 28:
            case 256:
            case 257: {
                n += 144;
                break;
            }
            case 241:
            case 242:
            case 245: {
                n += 152;
            }
        }
        string = n > 127 ? "" + (char)n : "";
        return string;
    }

    public void send_ctrl_alt_del() {
        byte[] byArray = new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byArray[2] = 5;
        byArray[4] = 76;
        String string = new String(byArray);
        this.transmit(string);
        try {
            Thread.currentThread();
            Thread.sleep(250L);
        }
        catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }
        byArray[4] = 0;
        String string2 = new String(byArray);
        this.transmit(string2);
        try {
            Thread.currentThread();
            Thread.sleep(250L);
        }
        catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }
        byArray[2] = 0;
        String string3 = new String(byArray);
        this.transmit(string3);
        this.requestFocus();
    }

    public void send_num_lock() {
        System.out.println("sending num lock");
        byte[] byArray = new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byArray[4] = 83;
        String string = new String(byArray);
        this.transmit(string);
        try {
            Thread.currentThread();
            Thread.sleep(250L);
        }
        catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }
        byArray[4] = 0;
        String string2 = new String(byArray);
        this.transmit(string2);
    }

    public void send_caps_lock() {
        System.out.println("sending caps lock");
        byte[] byArray = new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byArray[4] = 57;
        String string = new String(byArray);
        this.transmit(string);
        try {
            Thread.currentThread();
            Thread.sleep(250L);
        }
        catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }
        byArray[4] = 0;
        String string2 = new String(byArray);
        this.transmit(string2);
    }

    public void send_ctrl_alt_back() {
        byte[] byArray = new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byArray[2] = 5;
        byArray[4] = 42;
        String string = new String(byArray);
        this.transmit(string);
        try {
            Thread.currentThread();
            Thread.sleep(250L);
        }
        catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }
        byArray[4] = 0;
        String string2 = new String(byArray);
        this.transmit(string2);
        try {
            Thread.currentThread();
            Thread.sleep(250L);
        }
        catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }
        byArray[2] = 0;
        String string3 = new String(byArray);
        this.transmit(string3);
        this.requestFocus();
    }

    public void send_ctrl_alt_fn(int n) {
        byte[] byArray = new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int n2 = 0;
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
        this.transmit(string);
        try {
            Thread.currentThread();
            Thread.sleep(250L);
        }
        catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }
        byArray[4] = 0;
        String string2 = new String(byArray);
        this.transmit(string2);
        try {
            Thread.currentThread();
            Thread.sleep(250L);
        }
        catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }
        byArray[2] = 0;
        String string3 = new String(byArray);
        this.transmit(string3);
        this.requestFocus();
    }

    public void send_alt_fn(int n) {
        byte[] byArray = new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int n2 = 0;
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
        this.transmit(string);
        try {
            Thread.currentThread();
            Thread.sleep(250L);
        }
        catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }
        byArray[4] = 0;
        String string2 = new String(byArray);
        this.transmit(string2);
        try {
            Thread.currentThread();
            Thread.sleep(250L);
        }
        catch (InterruptedException interruptedException) {
            System.out.println("Thread interrupted..");
        }
        byArray[2] = 0;
        String string3 = new String(byArray);
        this.transmit(string3);
        this.requestFocus();
    }

    public void sendMomPress() {
        this.post_complete = false;
        byte[] byArray = new byte[]{0, 0, 0, 0};
        String string = new String(byArray);
        this.transmit(string);
    }

    public void sendPressHold() {
        this.post_complete = false;
        byte[] byArray = new byte[]{0, 0, 1, 0};
        String string = new String(byArray);
        this.transmit(string);
    }

    public void sendPowerCycle() {
        this.post_complete = false;
        byte[] byArray = new byte[]{0, 0, 2, 0};
        String string = new String(byArray);
        this.transmit(string);
    }

    public void sendSystemReset() {
        this.post_complete = false;
        byte[] byArray = new byte[]{0, 0, 3, 0};
        String string = new String(byArray);
        this.transmit(string);
    }

    public void send_mouse_press(int n) {
    }

    public void send_mouse_release(int n) {
    }

    public void send_mouse_click(int n, int n2) {
    }

    public void send_mouse_byte(int n) {
    }

    public void refresh_screen() {
        byte[] byArray = new byte[]{5, 0};
        String string = new String(byArray);
        this.transmit(string);
        this.requestFocus();
    }

    public void send_keep_alive_msg() {
    }

    public static String byteToHex(byte by) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(cim.toHexChar(by >>> 4 & 0xF));
        stringBuffer.append(cim.toHexChar(by & 0xF));
        return stringBuffer.toString();
    }

    public static String intToHex(int n) {
        byte by = (byte)n;
        return cim.byteToHex(by);
    }

    public static String intToHex4(int n) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(cim.byteToHex((byte)(n / 256)));
        stringBuffer.append(cim.byteToHex((byte)(n & 0xFF)));
        return stringBuffer.toString();
    }

    public static String charToHex(char c) {
        byte by = (byte)c;
        return cim.byteToHex(by);
    }

    public static char toHexChar(int n) {
        if (0 <= n && n <= 9) {
            return (char)(48 + n);
        }
        return (char)(65 + (n - 10));
    }

    protected synchronized void set_framerate(int n) {
        framerate = n;
        this.screen.set_framerate(n);
        this.set_status(3, "" + framerate);
    }

    protected void show_error(String string) {
        System.out.println("dvc:" + string + ": state " + dvc_decoder_state + " code " + dvc_code);
        System.out.println("dvc:error at byte count " + count_bytes);
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
                int n7 = n5;
                dvc_cc_usage[n7] = dvc_cc_usage[n7] + 1;
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
                        int n6 = n3;
                        dvc_cc_usage[n6] = dvc_cc_usage[n6] + 1;
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
        boolean bl = true;
        if (!video_detected) {
            bl = false;
        }
        if (dvc_pixel_count != 0 && dvc_y_clipped > 0 && dvc_lasty == dvc_size_y) {
            int n3 = this.color_remap_table[0];
            n2 = dvc_y_clipped;
            while (n2 < 256) {
                cim.block[n2] = n3;
                ++n2;
            }
        }
        dvc_pixel_count = 0;
        dvc_next_state = 1;
        int n4 = dvc_lastx * this.blockWidth;
        n2 = dvc_lasty * this.blockHeight;
        while (n != 0) {
            if (bl) {
                this.screen.paste_array(block, n4, n2, 16, this.blockHeight);
            }
            n4 += 16;
            if (++dvc_lastx >= dvc_size_x) break;
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

    final int add_bits(char c) {
        char c2 = c;
        dvc_ib_acc |= c2 << dvc_ib_bcnt;
        dvc_ib_bcnt += 8;
        if ((dvc_zero_count += dvc_right[c]) > 30) {
            if (!debug_msgs || dvc_decoder_state != 38 || fatal_count >= 40 || fatal_count > 0) {
                // empty if block
            }
            dvc_next_state = 43;
            dvc_decoder_state = 43;
            return 4;
        }
        if (c != '\u0000') {
            dvc_zero_count = dvc_left[c];
        }
        return 0;
    }

    final int get_bits(int n) {
        if (n == 1) {
            dvc_code = dvc_ib_acc & 1;
            dvc_ib_acc >>= 1;
            --dvc_ib_bcnt;
            return 0;
        }
        if (n == 0) {
            return 0;
        }
        int n2 = dvc_ib_acc & dvc_getmask[n];
        dvc_ib_bcnt -= n;
        dvc_ib_acc >>= n;
        n2 = dvc_reversal[n2];
        dvc_code = n2 >>= 8 - n;
        return 0;
    }

    int process_bits(char c) {
        boolean bl = true;
        int n = 0;
        this.add_bits(c);
        dvc_new_bits = c;
        ++count_bytes;
        int n2 = 0;
        while (n == 0) {
            n2 = bits_to_read[dvc_decoder_state];
            if (n2 > dvc_ib_bcnt) {
                n = 0;
                break;
            }
            int n3 = this.get_bits(n2);
            dvc_counter_bits += (long)n2;
            dvc_next_state = dvc_code == 0 ? next_0[dvc_decoder_state] : next_1[dvc_decoder_state];
            block0 : switch (dvc_decoder_state) {
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
                    dvc_color = this.cache_find(dvc_code);
                    if (dvc_color == -1) {
                        dvc_next_state = 38;
                        break;
                    }
                    dvc_last_color = this.color_remap_table[dvc_color];
                    if (dvc_pixel_count >= this.blockHeight * this.blockWidth) {
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
                        if (dvc_pixel_count >= this.blockHeight * this.blockWidth) {
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
                    if (!debug_msgs || dvc_decoder_state != 14 || dvc_code < 16) {
                        // empty if block
                    }
                    int n4 = 0;
                    while (n4 < dvc_code) {
                        if (dvc_pixel_count >= this.blockHeight * this.blockWidth) {
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
                    if (dvc_pixel_count >= this.blockHeight * this.blockWidth) {
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
                case 36: {
                    break;
                }
                case 35: {
                    dvc_next_state = dvc_pixcode;
                    break;
                }
                case 9: {
                    dvc_red = dvc_code << this.bitsPerColor * 2;
                    break;
                }
                case 41: {
                    dvc_green = dvc_code << this.bitsPerColor;
                    break;
                }
                case 8: {
                    dvc_red = dvc_code << this.bitsPerColor * 2;
                    dvc_green = dvc_code << this.bitsPerColor;
                }
                case 42: {
                    dvc_blue = dvc_code;
                    dvc_color = dvc_red | dvc_green | dvc_blue;
                    n3 = this.cache_lru(dvc_color);
                    if (n3 != 0) {
                        if (!debug_msgs || count_bytes > 6L) {
                            // empty if block
                        }
                        dvc_next_state = 38;
                        break;
                    }
                    dvc_last_color = this.color_remap_table[dvc_color];
                    if (dvc_pixel_count >= this.blockHeight * this.blockWidth) {
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
                    if (dvc_decoder_state != 17 || dvc_newx <= dvc_size_x) break;
                    if (debug_msgs) {
                        // empty if block
                    }
                    dvc_newx = 0;
                    break;
                }
                case 39: {
                    dvc_newy = dvc_code;
                    if (this.blockHeight == 16) {
                        dvc_newy &= 0x7F;
                    }
                    dvc_lastx = dvc_newx;
                    dvc_lasty = dvc_newy;
                    if (dvc_lasty <= dvc_size_y || debug_msgs) {
                        // empty if block
                    }
                    this.screen.repaint_it(1);
                    break;
                }
                case 20: {
                    dvc_code = dvc_lastx + dvc_code + 1;
                    if (dvc_code <= dvc_size_x || debug_msgs) {
                        // empty if block
                    }
                }
                case 21: {
                    dvc_lastx = dvc_code;
                    if (this.blockHeight == 16) {
                        dvc_lastx &= 0x7F;
                    }
                    if (dvc_lastx <= dvc_size_x || !debug_msgs) break;
                    break;
                }
                case 27: {
                    if (timeout_count == count_bytes - 1L) {
                        dvc_next_state = 38;
                    }
                    if ((dvc_ib_bcnt & 7) != 0) {
                        this.get_bits(dvc_ib_bcnt & 7);
                    }
                    timeout_count = count_bytes;
                    this.screen.repaint_it(1);
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
                                this.set_framerate(cmd_p_buff[0]);
                                break;
                            }
                            this.set_framerate(0);
                            break;
                        }
                        case 4: {
                            this.remconsObj.setPwrStatusPower(1);
                            break;
                        }
                        case 5: {
                            this.remconsObj.setPwrStatusPower(0);
                            this.screen.clearScreen();
                            dvc_newx = 50;
                            dvc_code = 38;
                            break;
                        }
                        case 6: {
                            this.screen.clearScreen();
                            if (!video_detected) {
                                this.screen.clearScreen();
                            }
                            this.set_status(2, this.getLocalString(12290));
                            this.set_status(1, " ");
                            this.set_status(3, " ");
                            this.set_status(4, " ");
                            this.post_complete = false;
                            break;
                        }
                        case 7: {
                            this.ts_type = cmd_p_buff[0];
                            break;
                        }
                        case 8: {
                            break;
                        }
                        case 9: {
                            System.out.println("received keychg and cleared bits\n");
                            if ((dvc_ib_bcnt & 7) == 0) break;
                            this.get_bits(dvc_ib_bcnt & 7);
                            break;
                        }
                        case 10: {
                            this.seize();
                            break;
                        }
                        case 11: {
                            System.out.println("Setting bpc to  " + cmd_p_buff[0]);
                            this.setBitsPerColor(cmd_p_buff[0]);
                            break;
                        }
                        case 12: {
                            System.out.println("Setting encryption to  " + cmd_p_buff[0]);
                            this.setVideoDecryption(cmd_p_buff[0]);
                            break;
                        }
                        case 13: {
                            System.out.println("Header received ");
                            this.setBitsPerColor(cmd_p_buff[0]);
                            this.setVideoDecryption(cmd_p_buff[1]);
                            this.remconsObj.SetLicensed(cmd_p_buff[2]);
                            this.remconsObj.SetFlags(cmd_p_buff[3]);
                            break;
                        }
                        case 16: {
                            this.sendAck();
                            break;
                        }
                        case 128: {
                            this.screen.invalidate();
                            this.screen.repaint();
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
                        printstring = printstring + (char)dvc_code;
                        break;
                    }
                    switch (printchan) {
                        case 1:
                        case 2: {
                            this.set_status(2 + printchan, printstring);
                            break;
                        }
                        case 3: {
                            System.out.println(printstring);
                            break;
                        }
                        case 4: {
                            this.screen.show_text(printstring);
                        }
                    }
                    dvc_next_state = 1;
                    break;
                }
                case 15:
                case 16:
                case 18:
                case 19:
                case 23:
                case 25: {
                    break;
                }
                case 0: {
                    this.cache_reset();
                    dvc_pixel_count = 0;
                    dvc_lastx = 0;
                    dvc_lasty = 0;
                    dvc_red = 0;
                    dvc_green = 0;
                    dvc_blue = 0;
                    fatal_count = 0;
                    timeout_count = -1L;
                    cmd_p_count = 0;
                    break;
                }
                case 38: {
                    if (fatal_count == 0) {
                        debug_lastx = dvc_lastx;
                        debug_lasty = dvc_lasty;
                        debug_show_block = 1;
                    }
                    if (fatal_count == 40) {
                        this.refresh_screen();
                    }
                    if (fatal_count == 11680) {
                        this.refresh_screen();
                    }
                    if (++fatal_count == 120000) {
                        this.refresh_screen();
                    }
                    if (fatal_count != 12000000) break;
                    this.refresh_screen();
                    fatal_count = 41;
                    break;
                }
                case 34: {
                    this.next_block(1);
                    break;
                }
                case 29: {
                    dvc_code += 2;
                }
                case 30: {
                    this.next_block(dvc_code);
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
                    this.cache_reset();
                    this.scale_x = 1;
                    this.scale_y = 1;
                    this.screen_x = dvc_size_x * this.blockWidth;
                    this.screen_y = dvc_size_y * 16 + dvc_code;
                    video_detected = this.screen_x != 0 && this.screen_y != 0;
                    dvc_y_clipped = dvc_code > 0 ? 256 - 16 * dvc_code : 0;
                    if (!video_detected) {
                        this.screen.clearScreen();
                        this.set_status(2, this.getLocalString(12290));
                        this.set_status(1, " ");
                        this.set_status(3, " ");
                        this.set_status(4, " ");
                        System.out.println("No video. image_source = " + this.screen.image_source);
                        this.post_complete = false;
                        break;
                    }
                    this.screen.set_abs_dimensions(this.screen_x, this.screen_y);
                    this.SetHalfHeight();
                    this.mouse_sync.serverScreen(this.screen_x, this.screen_y);
                    this.set_status(2, this.getLocalString(12291) + this.screen_x + "x" + this.screen_y);
                    this.set_status(1, " ");
                    break;
                }
                case 43: {
                    if (dvc_next_state == dvc_decoder_state) break;
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
            if (dvc_next_state == 2 && dvc_pixel_count == this.blockHeight * this.blockWidth) {
                this.next_block(1);
                this.cache_prune();
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

    boolean process_dvc(char c) {
        boolean bl;
        int n;
        if (dvc_reversal[255] == 0) {
            System.out.println("dvc initializing");
            this.init_reversal();
            this.cache_reset();
            dvc_decoder_state = 0;
            dvc_next_state = 0;
            dvc_zero_count = 0;
            dvc_ib_acc = 0;
            dvc_ib_bcnt = 0;
            this.buildPixelTable(this.bitsPerColor);
            this.SetHalfHeight();
        }
        if ((n = !dvc_process_inhibit ? this.process_bits(c) : 0) == 0) {
            bl = true;
        } else {
            System.out.println("Exit from DVC mode status =" + n);
            System.out.println("Current block at " + dvc_lastx + " " + dvc_lasty);
            System.out.println("Byte count " + count_bytes);
            bl = true;
            dvc_decoder_state = 38;
            dvc_next_state = 38;
            fatal_count = 0;
            this.refresh_screen();
        }
        return bl;
    }

    public void set_sig_colors(int[] nArray) {
    }

    public void change_key() {
        this.RC4encrypter.update_key();
        super.change_key();
    }

    public void set_mouse_protocol(int n) {
        this.mouse_protocol = n;
    }

    Cursor customCursor(Image image, Point point, String string) {
        Cursor cursor = null;
        try {
            Class clazz = class$java$awt$Toolkit == null ? (class$java$awt$Toolkit = cim.class$("java.awt.Toolkit")) : class$java$awt$Toolkit;
            Method method = clazz.getMethod("createCustomCursor", class$java$awt$Image == null ? (class$java$awt$Image = cim.class$("java.awt.Image")) : class$java$awt$Image, class$java$awt$Point == null ? (class$java$awt$Point = cim.class$("java.awt.Point")) : class$java$awt$Point, class$java$lang$String == null ? (class$java$lang$String = cim.class$("java.lang.String")) : class$java$lang$String);
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            if (method != null) {
                cursor = (Cursor)method.invoke(toolkit, image, point, string);
            }
        }
        catch (Exception exception) {
            System.out.println("This JVM cannot create custom cursors");
        }
        return cursor;
    }

    Cursor createCursor(int n) {
        Image image;
        String string = System.getProperty("java.version", "0");
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        switch (n) {
            case 0: {
                return Cursor.getDefaultCursor();
            }
            case 1: {
                return Cursor.getPredefinedCursor(1);
            }
            case 2: {
                image = toolkit.createImage(cursor_none);
                break;
            }
            case 3: {
                int[] nArray = new int[1024];
                nArray[33] = -8355712;
                nArray[32] = -8355712;
                nArray[1] = -8355712;
                nArray[0] = -8355712;
                MemoryImageSource memoryImageSource = new MemoryImageSource(32, 32, nArray, 0, 32);
                image = this.createImage(memoryImageSource);
                break;
            }
            case 4: {
                int[] nArray = new int[1024];
                int n2 = 0;
                while (n2 < 21) {
                    int n3 = 0;
                    while (n3 < 12) {
                        nArray[n3 + n2 * 32] = cursor_outline[n3 + n2 * 12];
                        ++n3;
                    }
                    ++n2;
                }
                MemoryImageSource memoryImageSource = new MemoryImageSource(32, 32, nArray, 0, 32);
                image = this.createImage(memoryImageSource);
                break;
            }
            default: {
                System.out.println("createCursor: unknown cursor " + n);
                return Cursor.getDefaultCursor();
            }
        }
        Cursor cursor = null;
        if (string.compareTo("1.2") < 0) {
            System.out.println("This JVM cannot create custom cursors");
        } else {
            cursor = this.customCursor(image, new Point(), "rcCursor");
        }
        return cursor != null ? cursor : Cursor.getDefaultCursor();
    }

    public void set_cursor(int n) {
        this.current_cursor = this.createCursor(n);
        this.setCursor(this.current_cursor);
    }

    private void SetHalfHeight() {
        if (this.screen_x > 1616) {
            if (this.remconsObj.halfHeightCapable) {
                if (8 != this.blockHeight) {
                    System.out.println("Setting halfheight mode on supported system");
                    this.blockHeight = 8;
                    cim.bits_to_read[21] = 8;
                    cim.bits_to_read[17] = 8;
                    cim.bits_to_read[39] = 8;
                    cim.bits_to_read[30] = 8;
                }
            } else if (!this.unsupportedVideoModeWarned) {
                new VErrorDialog(this.remconsObj.ParentApp.dispFrame, this.getLocalString(8225), this.getLocalString(8226), false);
                this.unsupportedVideoModeWarned = true;
            }
        } else if (16 != this.blockHeight) {
            System.out.println("Setting non-halfheight mode");
            this.blockHeight = 16;
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
                    this.color_remap_table[n3] = (n3 & 0x1F) << 3;
                    int n4 = n3;
                    this.color_remap_table[n4] = this.color_remap_table[n4] | (n3 & 0x3E0) << 6;
                    int n5 = n3;
                    this.color_remap_table[n5] = this.color_remap_table[n5] | (n3 & 0x7C00) << 9;
                    ++n3;
                }
                break;
            }
            case 4: {
                int n6 = 0;
                while (n6 < n2) {
                    this.color_remap_table[n6] = (n6 & 0xF) << 4;
                    int n7 = n6;
                    this.color_remap_table[n7] = this.color_remap_table[n7] | (n6 & 0xF0) << 8;
                    int n8 = n6;
                    this.color_remap_table[n8] = this.color_remap_table[n8] | (n6 & 0xF00) << 12;
                    ++n6;
                }
                break;
            }
            case 3: {
                int n9 = 0;
                while (n9 < n2) {
                    this.color_remap_table[n9] = (n9 & 0xF) << 5;
                    int n10 = n9;
                    this.color_remap_table[n10] = this.color_remap_table[n10] | (n9 & 0xF0) << 11;
                    int n11 = n9;
                    this.color_remap_table[n11] = this.color_remap_table[n11] | (n9 & 0xF00) << 15;
                    ++n9;
                }
                break;
            }
            case 2: {
                int n12 = 0;
                while (n12 < n2) {
                    this.color_remap_table[n12] = (n12 & 0xF) << 6;
                    int n13 = n12;
                    this.color_remap_table[n13] = this.color_remap_table[n13] | (n12 & 0xF0) << 15;
                    int n14 = n12;
                    this.color_remap_table[n14] = this.color_remap_table[n14] | (n12 & 0xF00) << 18;
                    ++n12;
                }
                break;
            }
        }
    }

    void setBitsPerColor(int n) {
        cim.bits_to_read[8] = this.bitsPerColor = 5 - (n & 3);
        cim.bits_to_read[9] = this.bitsPerColor;
        cim.bits_to_read[41] = this.bitsPerColor;
        cim.bits_to_read[42] = this.bitsPerColor;
        this.buildPixelTable(this.bitsPerColor);
    }

    void setVideoDecryption(int n) {
        switch (n) {
            case 0: {
                this.dvc_encryption = false;
                this.cipher = 0;
                this.remconsObj.setPwrStatusEncLabel(this.getLocalString(12292));
                this.remconsObj.setPwrStatusEnc(0);
                System.out.println("Setting encryption -> None");
                break;
            }
            case 1: {
                this.dvc_encryption = true;
                this.remconsObj.setPwrStatusEncLabel(this.getLocalString(12293));
                this.remconsObj.setPwrStatusEnc(1);
                this.dvc_mode = true;
                this.cipher = 1;
                System.out.println("Setting encryption -> RC4 - 128 bit");
                break;
            }
            case 2: {
                this.dvc_encryption = true;
                this.remconsObj.setPwrStatusEncLabel(this.getLocalString(12294));
                this.remconsObj.setPwrStatusEnc(1);
                this.dvc_mode = true;
                this.cipher = 2;
                System.out.println("Setting encryption -> AES - 128 bit");
                break;
            }
            case 3: {
                this.dvc_encryption = true;
                this.remconsObj.setPwrStatusEncLabel(this.getLocalString(12295));
                this.remconsObj.setPwrStatusEnc(1);
                this.dvc_mode = true;
                this.cipher = 3;
                System.out.println("Setting encryption -> AES - 256 bit");
                break;
            }
            default: {
                this.dvc_encryption = false;
                this.remconsObj.setPwrStatusEncLabel(this.getLocalString(12292));
                this.remconsObj.setPwrStatusEnc(0);
                System.out.println("Unsupported encryption");
            }
        }
    }

    public byte mouseButtonState(int n) {
        byte by = 0;
        switch (n) {
            case 4: {
                by = (byte)(by | 1);
                break;
            }
            case 2: {
                by = (byte)(by | 4);
                break;
            }
            case 1: {
                by = (byte)(by | 2);
            }
        }
        return by;
    }

    public byte getMouseButtonState(MouseEvent mouseEvent) {
        byte by = 0;
        if ((((InputEvent)mouseEvent).getModifiersEx() & 0x1000) != 0) {
            by = (byte)(by | 2);
        }
        if ((((InputEvent)mouseEvent).getModifiersEx() & 0x800) != 0) {
            by = (byte)(by | 4);
        }
        if ((((InputEvent)mouseEvent).getModifiersEx() & 0x400) != 0) {
            by = (byte)(by | 1);
        }
        return by;
    }

    public void sendMouse(MouseEvent mouseEvent) {
        Point point = new Point(0, 0);
        Point point2 = new Point(0, 0);
        point = this.getAbsMouseCoordinates(mouseEvent);
        char c = (char)point.x;
        char c2 = (char)point.y;
        if ((((InputEvent)mouseEvent).getModifiersEx() & 0x80) > 0) {
            this.mousePrevPosn.x = c;
            this.mousePrevPosn.y = c2;
        } else if (c <= this.screen_x && c2 <= this.screen_y) {
            point2.x = c - this.mousePrevPosn.x;
            point2.y = this.mousePrevPosn.y - c2;
            this.mousePrevPosn.x = c;
            this.mousePrevPosn.y = c2;
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
            this.UI_dirty = true;
            if (this.screen_x > 0 && this.screen_y > 0) {
                c = (char)(3000 * c / this.screen_x);
                c2 = (char)(3000 * c2 / this.screen_y);
            } else {
                c = (char)(3000 * c / 1);
                c2 = (char)(3000 * c2 / 1);
            }
            byte[] byArray = new byte[]{2, 0, (byte)(c & 0xFF), (byte)(c >> 8), (byte)(c2 & 0xFF), (byte)(c2 >> 8), n < 0 ? (byte)(n & 0xFF) : (byte)(n & 0xFF), n2 < 0 ? (byte)(n2 & 0xFF) : (byte)(n2 & 0xFF), this.getMouseButtonState(mouseEvent), 0};
            this.transmitb(byArray, byArray.length);
        }
    }

    private Point getAbsMouseCoordinates(MouseEvent mouseEvent) {
        Point point = new Point();
        point.y = mouseEvent.getY();
        point.x = mouseEvent.getX();
        return point;
    }

    public void sendMouseScroll(MouseWheelEvent mouseWheelEvent) {
    }

    private void sendAck() {
        byte[] byArray = new byte[]{12, 0};
        String string = new String(byArray);
        this.transmit(string);
    }

    public void requestScreenFocus(MouseEvent mouseEvent) {
        this.requestFocus();
    }

    public void installKeyboardHook() {
        this.remconsObj.remconsInstallKeyboardHook();
    }

    public void unInstallKeyboardHook() {
        this.remconsObj.remconsUnInstallKeyboardHook();
    }

    static Class class$(String string) {
        try {
            return Class.forName(string);
        }
        catch (ClassNotFoundException classNotFoundException) {
            throw new NoClassDefFoundError(classNotFoundException.getMessage());
        }
    }

    static {
        dvc_ib_acc = 0;
        dvc_ib_bcnt = 0;
        dvc_zero_count = 0;
        dvc_decoder_state = 0;
        dvc_next_state = 0;
        dvc_pixcode = 38;
        dvc_code = 0;
        block = new int[256];
        printchan = 0;
        printstring = "";
        count_bytes = 0L;
        cmd_p_buff = new int[256];
        cmd_p_count = 0;
        cmd_last = 0;
        framerate = 30;
        debug_msgs = false;
        last_bits = '\u0000';
        last_bits2 = '\u0000';
        last_bits3 = '\u0000';
        last_bits4 = '\u0000';
        last_bits5 = '\u0000';
        last_bits6 = '\u0000';
        last_bits7 = '\u0000';
        last_len = 0;
        last_len1 = 0;
        last_len2 = 0;
        last_len3 = 0;
        last_len4 = 0;
        last_len5 = 0;
        last_len6 = 0;
        last_len7 = 0;
        last_len8 = 0;
        last_len9 = 0;
        last_len10 = 0;
        last_len11 = 0;
        last_len12 = 0;
        last_len13 = 0;
        last_len14 = 0;
        last_len15 = 0;
        last_len16 = 0;
        last_len17 = 0;
        last_len18 = 0;
        last_len19 = 0;
        last_len20 = 0;
        last_len21 = 0;
        dvc_new_bits = '\u0000';
        debug_lastx = 0;
        debug_lasty = 0;
        debug_show_block = 0;
        timeout_count = 0L;
        dvc_counter_block = 0L;
        dvc_counter_bits = 0L;
        show_bitsblk_count = false;
        show_slices = 0L;
        dvc_process_inhibit = false;
        video_detected = true;
        cursor_none = new byte[]{0};
        cursor_outline = new int[]{-8355712, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8355712, -8355712, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8355712, 0, -8355712, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8355712, 0, 0, -8355712, 0, 0, 0, 0, 0, 0, 0, 0, -8355712, 0, 0, 0, -8355712, 0, 0, 0, 0, 0, 0, 0, -8355712, 0, 0, 0, 0, -8355712, 0, 0, 0, 0, 0, 0, -8355712, 0, 0, 0, 0, 0, -8355712, 0, 0, 0, 0, 0, -8355712, 0, 0, 0, 0, 0, 0, -8355712, 0, 0, 0, 0, -8355712, 0, 0, 0, 0, 0, 0, 0, -8355712, 0, 0, 0, -8355712, 0, 0, 0, 0, 0, 0, 0, 0, -8355712, 0, 0, -8355712, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8355712, 0, -8355712, 0, 0, 0, 0, 0, 0, -8355712, -8355712, -8355712, -8355712, -8355712, -8355712, 0, 0, 0, 0, 0, 0, -8355712, 0, 0, 0, 0, -8355712, 0, 0, 0, -8355712, 0, 0, -8355712, 0, 0, 0, 0, -8355712, 0, 0, -8355712, -8355712, -8355712, 0, 0, -8355712, 0, 0, 0, -8355712, 0, -8355712, 0, 0, -8355712, 0, 0, -8355712, 0, 0, 0, -8355712, -8355712, 0, 0, 0, 0, -8355712, 0, 0, -8355712, 0, 0, -8355712, 0, 0, 0, 0, 0, -8355712, 0, 0, -8355712, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8355712, 0, 0, -8355712, 0, 0, 0, 0, 0, 0, 0, 0, -8355712, 0, 0, -8355712, 0, 0, 0, 0, 0, 0, 0, 0, 0, -8355712, -8355712, 0, 0};
    }
}
 