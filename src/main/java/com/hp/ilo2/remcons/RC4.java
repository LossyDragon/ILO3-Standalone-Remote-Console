package com.hp.ilo2.remcons;

import java.util.Arrays;


public class RC4 {

    byte[] key = new byte[16];
    byte[] keyBox = new byte[256];
    byte[] keyData = new byte[16];
    byte[] pre = new byte[16];
    byte[] sBox = new byte[256];
    int i;
    int j;

    public RC4(byte[] bArr) {
        System.arraycopy(bArr, 0, keyData, 0, keyData.length);

        Init();
    }

    public void Init() {
        i = 0;
        j = 0;

        Arrays.fill(key, (byte) 0);
        System.arraycopy(keyData, 0, pre, 0, pre.length);
        Arrays.fill(sBox, (byte) 0);
        Arrays.fill(keyBox, (byte) 0);

        update_key();
    }

    public void update_key() {
        System.arraycopy(pre, 0, key, 0, key.length);

        for (int i = 0; i < 256; i++) {
            sBox[i] = (byte) (i & telnet.TELNET_IAC);
            keyBox[i] = key[i % 16];
        }

        int i2 = 0;

        for (int i3 = 0; i3 < 256; i3++) {
            i2 = ((i2 & telnet.TELNET_IAC) + (sBox[i3] & 255) + (keyBox[i3] & 255)) & telnet.TELNET_IAC;
            byte b = sBox[i3];
            sBox[i3] = sBox[i2];
            sBox[i2] = b;
        }

        i = 0;
        j = 0;
    }

    public int randomValue() {
        i = ((i & telnet.TELNET_IAC) + 1) & telnet.TELNET_IAC;
        j = ((j & telnet.TELNET_IAC) + (sBox[i] & 255)) & telnet.TELNET_IAC;

        byte b = sBox[i];

        sBox[i] = sBox[j];
        sBox[j] = b;

        return sBox[((sBox[i] & 255) + (sBox[j] & 255)) & telnet.TELNET_IAC];
    }
}
