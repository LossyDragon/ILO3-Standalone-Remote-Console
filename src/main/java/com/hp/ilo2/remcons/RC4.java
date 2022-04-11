package com.hp.ilo2.remcons;

import java.util.Arrays;


public class RC4 {
    byte[] keyData = new byte[16];
    byte[] key = new byte[16];
    byte[] pre = new byte[16];
    byte[] sBox = new byte[256];
    byte[] keyBox = new byte[256];
    int i;
    int j;

    public RC4(byte[] bArr) {
        System.arraycopy(bArr, 0, this.keyData, 0, this.keyData.length);
        Init();
    }

    public void Init() {
        this.i = 0;
        this.j = 0;
        Arrays.fill(this.key, (byte) 0);
        System.arraycopy(this.keyData, 0, this.pre, 0, this.pre.length);
        Arrays.fill(this.sBox, (byte) 0);
        Arrays.fill(this.keyBox, (byte) 0);
        update_key();
    }

    public void update_key() {
        System.arraycopy(this.pre, 0, this.key, 0, this.key.length);
        for (int i = 0; i < 256; i++) {
            this.sBox[i] = (byte) (i & telnet.TELNET_IAC);
            this.keyBox[i] = this.key[i % 16];
        }
        int i2 = 0;
        for (int i3 = 0; i3 < 256; i3++) {
            i2 = ((i2 & telnet.TELNET_IAC) + (this.sBox[i3] & 255) + (this.keyBox[i3] & 255)) & telnet.TELNET_IAC;
            byte b = this.sBox[i3];
            this.sBox[i3] = this.sBox[i2];
            this.sBox[i2] = b;
        }
        this.i = 0;
        this.j = 0;
    }

    public int randomValue() {
        this.i = ((this.i & telnet.TELNET_IAC) + 1) & telnet.TELNET_IAC;
        this.j = ((this.j & telnet.TELNET_IAC) + (this.sBox[this.i] & 255)) & telnet.TELNET_IAC;
        byte b = this.sBox[this.i];
        this.sBox[this.i] = this.sBox[this.j];
        this.sBox[this.j] = b;
        return this.sBox[((this.sBox[this.i] & 255) + (this.sBox[this.j] & 255)) & telnet.TELNET_IAC];
    }
}
