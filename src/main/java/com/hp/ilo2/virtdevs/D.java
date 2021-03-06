package com.hp.ilo2.virtdevs;

import java.io.FileOutputStream;
import java.io.PrintStream;


public class D {
    public static final int NONE = -1;
    public static final int FATAL = 0;
    public static final int INFORM = 1;
    public static final int WARNING = 2;
    public static final int VERBOSE = 3;
    public static int debug;
    public static PrintStream out;

    static {
        debug = 0;
        String property = virtdevs.prop.getProperty("com.hp.ilo2.virtdevs.debugfile");
        try {
            if (property == null) {
                out = System.out;
            } else {
                out = new PrintStream(new FileOutputStream(property));
            }
        } catch (Exception e) {
            out = System.out;
            out.println("Exception trying to open debug trace\n" + e);
        }
        String property2 = virtdevs.prop.getProperty("com.hp.ilo2.virtdevs.debug");
        if (property2 != null) {
            debug = Integer.valueOf(property2);
        }
    }

    public static void println(int i, String str) {
        if (debug >= i) {
            out.println(str);
        }
    }

    public static void print(int i, String str) {
        if (debug >= i) {
            out.println(str);
        }
    }

    public static String hex(byte b, int i) {
        return hex(b & 255, i);
    }

    public static String hex(short s, int i) {
        return hex(s & 65535, i);
    }

    public static String hex(int i, int i2) {
        StringBuilder hexString = new StringBuilder(Integer.toHexString(i));
        while (hexString.length() < i2) {
            hexString.insert(0, "0");
        }
        return hexString.toString();
    }

    public static String hex(long j, int i) {
        StringBuilder hexString = new StringBuilder(Long.toHexString(j));
        while (hexString.length() < i) {
            hexString.insert(0, "0");
        }
        return hexString.toString();
    }

    public static void hexdump(int i, byte[] bArr, int i2) {
        if (debug >= i) {
            if (i2 == 0) {
                i2 = bArr.length;
            }
            for (int i3 = 0; i3 < i2; i3++) {
                if (i3 % 16 == 0) {
                    out.print("\n");
                }
                out.print(hex(bArr[i3], 2) + " ");
            }
            out.print("\n");
        }
    }
}
