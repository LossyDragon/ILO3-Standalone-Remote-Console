package com.hp.ilo2.remcons;

import java.util.Arrays;


public class KeyboardHook {

    private final int[] keyMap = new int[256];
    private final int[] linkey_to_hid_dll_en_US = {0, 41, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 45, 46, 42, 43, 20, 26, 8, 21, 23, 28, 24, 12, 18, 19, 47, 48, 40, 224, 4, 22, 7, 9, 10, 11, 13, 14, 15, 51, 52, 53, 225, 49, 29, 27, 6, 25, 5, 17, 16, 54, 55, 56, 229, 85, 226, 44, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 83, 71, 95, 96, 97, 86, 92, 93, 94, 87, 89, 90, 91, 98, 99, 0, 0, 100, 68, 69, 135, 0, 0, 0, 0, 0, 0, 88, 228, 84, 70, 230, 100, 74, 82, 75, 80, 79, 77, 81, 78, 73, 76, 0, 104, 105, 0, 0, 0, 0, 0, 0, 0, 0, 0, 137, 227, 231, 101, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private final int[] winkey_to_hid_dll_en_US = {0, 0, 0, 72, 0, 0, 0, 0, 42, 43, 0, 0, 0, 40, 0, 0, 0, 0, 0, 72, 57, 0, 0, 0, 0, 0, 0, 41, 0, 0, 0, 0, 44, 75, 78, 77, 74, 80, 82, 79, 81, 0, 0, 0, 70, 73, 76, 0, 39, 30, 31, 32, 33, 34, 35, 36, 37, 38, 0, 0, 0, 0, 0, 0, 0, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 227, 231, 101, 0, 0, 98, 89, 90, 91, 92, 93, 94, 95, 96, 97, 85, 87, 0, 86, 99, 84, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 71, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 225, 229, 224, 228, 226, 230, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 51, 46, 54, 45, 55, 56, 53, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 47, 49, 48, 52, 0, 0, 0, 100, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, telnet.TELNET_IAC};
    private final int[] winkey_to_hid_dll_ja_JP = {0, 0, 0, 72, 0, 0, 0, 0, 42, 43, 0, 0, 0, 40, 0, 0, 0, 0, 0, 72, 57, 0, 0, 0, 0, 53, 0, 41, 138, 139, 0, 0, 44, 75, 78, 77, 74, 80, 82, 79, 81, 0, 0, 0, 70, 73, 76, 0, 39, 30, 31, 32, 33, 34, 35, 36, 37, 38, 0, 0, 0, 0, 0, 0, 0, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 227, 231, 101, 0, 0, 98, 89, 90, 91, 92, 93, 94, 95, 96, 97, 85, 87, 0, 86, 99, 84, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 83, 71, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 225, 229, 224, 228, 226, 230, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 52, 51, 54, 45, 55, 56, 47, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 137, 50, 46, 0, 0, 0, 135, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 57, 0, 136, 53, 53, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, telnet.TELNET_IAC};
    private int keyboardLayoutId = 0;
    public boolean kcmdValid = false;
    public byte[] kcmd = new byte[10];

    /* ILO3RemCon addition */
    public KeyboardHook(String str) {
        try {
            String hookFilename = str + ".dll";

            String fileSeparator = System.getProperty("file.separator");
            String tempDir = System.getProperty("java.io.tmpdir");
            String osName = System.getProperty("os.name").toLowerCase();

            if (tempDir == null) {
                tempDir = osName.startsWith("windows") ? "C:\\TEMP" : "/tmp";
            }

            if (!tempDir.endsWith(fileSeparator)) {
                tempDir = tempDir + fileSeparator;
            }

            String hookFile = tempDir + hookFilename;

            System.out.println(" Loading " + hookFile + "...");
            System.load(hookFile);
            System.out.println(" Loaded..!");
        } catch (Exception e) {
            System.err.println("Error loading library HpqKbHook.dll - " + e);
        }
    }

    public native int InstallKeyboardHook();

    public native int UnInstallKeyboardHook();

    public native int GetKeyData();

    public native int setLocalKbdLayout(int i);

    public void clearKeymap() {
        for (int i = 0; i < 256; i++) {
            this.keyMap[i] = 0;
        }
    }

    public void setKeyboardLayoutId(int i) {
        this.keyboardLayoutId = i;
    }

    public void HandleSpecialKey(int i, int i2) {
        if (1041 == this.keyboardLayoutId && 1 == this.keyMap[25] && 0 == this.keyMap[164]) {
            this.keyMap[25] = 0;
        }
    }

    public byte[] HandleHookKey(int i, int i2, boolean z, boolean z2) {
        int i3;
        int i4 = this.keyMap[i];
        Arrays.fill(this.kcmd, (byte) 0);
        this.kcmd[0] = 1;
        this.kcmdValid = false;
        if (z2) {
            System.out.println("HandleHookKey ctl-Alt-Del clearkeymap");
            clearKeymap();
            this.kcmdValid = true;
        } else {
            if (1041 == this.keyboardLayoutId && (243 == i || 244 == i)) {
                i = 243;
                i4 = this.keyMap[243];
                if (z) {
                    this.keyMap[243] = 0;
                } else {
                    this.keyMap[243] = 1;
                }
            } else if (z) {
                this.keyMap[i] = 1;
            } else {
                this.keyMap[i] = 0;
            }
            if (i4 != this.keyMap[i]) {
                this.kcmdValid = true;
                HandleSpecialKey(i, z ? 1 : 0);
                int i5 = 0;
                for (int i6 = 0; i6 < 256; i6++) {
                    if (this.keyMap[i6] != 0) {
                        if (-16711935 == this.keyboardLayoutId) {
                            i3 = this.linkey_to_hid_dll_en_US[i6];
                        } else if (1041 == this.keyboardLayoutId) {
                            i3 = this.winkey_to_hid_dll_ja_JP[i6];
                        } else {
                            i3 = this.winkey_to_hid_dll_en_US[i6];
                        }
                        if (!(i3 == 0 || i3 == 255)) {
                            if ((i3 & 224) == 224) {
                                byte[] bArr = this.kcmd;
                                bArr[2] = (byte) (bArr[2] | ((byte) (1 << (i3 ^ 224))));
                            } else {
                                this.kcmd[4 + i5] = (byte) i3;
                                i5++;
                                if (i5 == 6) {
                                    i5 = 5;
                                }
                            }
                        }
                    }
                }
            }
        }
        this.kcmd[0] = 1;
        return this.kcmd;
    }
}
