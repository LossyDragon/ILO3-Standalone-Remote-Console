package com.hp.ilo2.remcons;


import java.util.Arrays;

@SuppressWarnings("unused") // It is being used, not sure why it says it's not.
public final class VMD5 implements Cloneable {

    private byte[] buffer;
    private byte[] digestBits;
    private int[] state;
    private int[] transformBuffer;
    private long count;
    private static final int S13 = 17;
    private static final int S14 = 22;
    private static final int S23 = 14;
    private static final int S24 = 20;
    private static final int S33 = 16;
    private static final int S43 = 15;

    public VMD5() {
        init();
    }

    private VMD5(VMD5 vmd5) {
        this();

        state = new int[vmd5.state.length];
        System.arraycopy(vmd5.state, 0, state, 0, vmd5.state.length);

        transformBuffer = new int[vmd5.transformBuffer.length];
        System.arraycopy(vmd5.transformBuffer, 0, transformBuffer, 0, vmd5.transformBuffer.length);

        buffer = new byte[vmd5.buffer.length];
        System.arraycopy(vmd5.buffer, 0, buffer, 0, vmd5.buffer.length);

        digestBits = new byte[vmd5.digestBits.length];
        System.arraycopy(vmd5.digestBits, 0, digestBits, 0, vmd5.digestBits.length);

        count = vmd5.count;
    }

    private int F(int i, int i2, int i3) {
        return (i & i2) | ((~i) & i3);
    }

    private int G(int i, int i2, int i3) {
        return (i & i3) | (i2 & (~i3));
    }

    private int H(int i, int i2, int i3) {
        return (i ^ i2) ^ i3;
    }

    private int I(int i, int i2, int i3) {
        return i2 ^ (i | (~i3));
    }

    private int rotateLeft(int i, int i2) {
        return (i << i2) | (i >>> (32 - i2));
    }

    private int FF(int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        return rotateLeft(i + F(i2, i3, i4) + i5 + i7, i6) + i2;
    }

    private int GG(int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        return rotateLeft(i + G(i2, i3, i4) + i5 + i7, i6) + i2;
    }

    private int HH(int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        return rotateLeft(i + H(i2, i3, i4) + i5 + i7, i6) + i2;
    }

    private int II(int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        return rotateLeft(i + I(i2, i3, i4) + i5 + i7, i6) + i2;
    }

    void transform(byte[] bArr, int i) {
        int[] iArr = transformBuffer;
        int i2 = state[0];
        int i3 = state[1];
        int i4 = state[2];
        int i5 = state[3];

        for (int i6 = 0; i6 < S33; i6++) {
            iArr[i6] = bArr[(i6 * 4) + i] & telnet.TELNET_IAC;
            for (int i7 = 1; i7 < 4; i7++) {
                iArr[i6] = iArr[i6] + ((bArr[((i6 * 4) + i7) + i] & telnet.TELNET_IAC) << (i7 * 8));
            }
        }

        int FF = FF(i2, i3, i4, i5, iArr[0], 7, -680876936);
        int FF2 = FF(i5, FF, i3, i4, iArr[1], 12, -389564586);
        int FF3 = FF(i4, FF2, FF, i3, iArr[2], S13, 606105819);
        int FF4 = FF(i3, FF3, FF2, FF, iArr[3], S14, -1044525330);
        int FF5 = FF(FF, FF4, FF3, FF2, iArr[4], 7, -176418897);
        int FF6 = FF(FF2, FF5, FF4, FF3, iArr[5], 12, 1200080426);
        int FF7 = FF(FF3, FF6, FF5, FF4, iArr[6], S13, -1473231341);
        int FF8 = FF(FF4, FF7, FF6, FF5, iArr[7], S14, -45705983);
        int FF9 = FF(FF5, FF8, FF7, FF6, iArr[8], 7, 1770035416);
        int FF10 = FF(FF6, FF9, FF8, FF7, iArr[9], 12, -1958414417);
        int FF11 = FF(FF7, FF10, FF9, FF8, iArr[10], S13, -42063);
        int FF12 = FF(FF8, FF11, FF10, FF9, iArr[11], S14, -1990404162);
        int FF13 = FF(FF9, FF12, FF11, FF10, iArr[12], 7, 1804603682);
        int FF14 = FF(FF10, FF13, FF12, FF11, iArr[13], 12, -40341101);
        int FF15 = FF(FF11, FF14, FF13, FF12, iArr[S23], S13, -1502002290);
        int FF16 = FF(FF12, FF15, FF14, FF13, iArr[S43], S14, 1236535329);
        int GG = GG(FF13, FF16, FF15, FF14, iArr[1], 5, -165796510);
        int GG2 = GG(FF14, GG, FF16, FF15, iArr[6], 9, -1069501632);
        int GG3 = GG(FF15, GG2, GG, FF16, iArr[11], S23, 643717713);
        int GG4 = GG(FF16, GG3, GG2, GG, iArr[0], S24, -373897302);
        int GG5 = GG(GG, GG4, GG3, GG2, iArr[5], 5, -701558691);
        int GG6 = GG(GG2, GG5, GG4, GG3, iArr[10], 9, 38016083);
        int GG7 = GG(GG3, GG6, GG5, GG4, iArr[S43], S23, -660478335);
        int GG8 = GG(GG4, GG7, GG6, GG5, iArr[4], S24, -405537848);
        int GG9 = GG(GG5, GG8, GG7, GG6, iArr[9], 5, 568446438);
        int GG10 = GG(GG6, GG9, GG8, GG7, iArr[S23], 9, -1019803690);
        int GG11 = GG(GG7, GG10, GG9, GG8, iArr[3], S23, -187363961);
        int GG12 = GG(GG8, GG11, GG10, GG9, iArr[8], S24, 1163531501);
        int GG13 = GG(GG9, GG12, GG11, GG10, iArr[13], 5, -1444681467);
        int GG14 = GG(GG10, GG13, GG12, GG11, iArr[2], 9, -51403784);
        int GG15 = GG(GG11, GG14, GG13, GG12, iArr[7], S23, 1735328473);
        int GG16 = GG(GG12, GG15, GG14, GG13, iArr[12], S24, -1926607734);
        int HH = HH(GG13, GG16, GG15, GG14, iArr[5], 4, -378558);
        int HH2 = HH(GG14, HH, GG16, GG15, iArr[8], 11, -2022574463);
        int HH3 = HH(GG15, HH2, HH, GG16, iArr[11], S33, 1839030562);
        int HH4 = HH(GG16, HH3, HH2, HH, iArr[S23], 23, -35309556);
        int HH5 = HH(HH, HH4, HH3, HH2, iArr[1], 4, -1530992060);
        int HH6 = HH(HH2, HH5, HH4, HH3, iArr[4], 11, 1272893353);
        int HH7 = HH(HH3, HH6, HH5, HH4, iArr[7], S33, -155497632);
        int HH8 = HH(HH4, HH7, HH6, HH5, iArr[10], 23, -1094730640);
        int HH9 = HH(HH5, HH8, HH7, HH6, iArr[13], 4, 681279174);
        int HH10 = HH(HH6, HH9, HH8, HH7, iArr[0], 11, -358537222);
        int HH11 = HH(HH7, HH10, HH9, HH8, iArr[3], S33, -722521979);
        int HH12 = HH(HH8, HH11, HH10, HH9, iArr[6], 23, 76029189);
        int HH13 = HH(HH9, HH12, HH11, HH10, iArr[9], 4, -640364487);
        int HH14 = HH(HH10, HH13, HH12, HH11, iArr[12], 11, -421815835);
        int HH15 = HH(HH11, HH14, HH13, HH12, iArr[S43], S33, 530742520);
        int HH16 = HH(HH12, HH15, HH14, HH13, iArr[2], 23, -995338651);
        int II = II(HH13, HH16, HH15, HH14, iArr[0], 6, -198630844);
        int II2 = II(HH14, II, HH16, HH15, iArr[7], 10, 1126891415);
        int II3 = II(HH15, II2, II, HH16, iArr[S23], S43, -1416354905);
        int II4 = II(HH16, II3, II2, II, iArr[5], 21, -57434055);
        int II5 = II(II, II4, II3, II2, iArr[12], 6, 1700485571);
        int II6 = II(II2, II5, II4, II3, iArr[3], 10, -1894986606);
        int II7 = II(II3, II6, II5, II4, iArr[10], S43, -1051523);
        int II8 = II(II4, II7, II6, II5, iArr[1], 21, -2054922799);
        int II9 = II(II5, II8, II7, II6, iArr[8], 6, 1873313359);
        int II10 = II(II6, II9, II8, II7, iArr[S43], 10, -30611744);
        int II11 = II(II7, II10, II9, II8, iArr[6], S43, -1560198380);
        int II12 = II(II8, II11, II10, II9, iArr[13], 21, 1309151649);
        int II13 = II(II9, II12, II11, II10, iArr[4], 6, -145523070);
        int II14 = II(II10, II13, II12, II11, iArr[11], 10, -1120210379);
        int II15 = II(II11, II14, II13, II12, iArr[2], S43, 718787259);
        int II16 = II(II12, II15, II14, II13, iArr[9], 21, -343485551);

        int[] iArr2 = state;
        iArr2[0] = iArr2[0] + II13;

        int[] iArr3 = state;
        iArr3[1] = iArr3[1] + II16;

        int[] iArr4 = state;
        iArr4[2] = iArr4[2] + II15;

        int[] iArr5 = state;
        iArr5[3] = iArr5[3] + II14;
    }

    public void init() {
        state = new int[4];
        transformBuffer = new int[S33];
        buffer = new byte[64];
        digestBits = new byte[S33];
        count = 0L;
        state[0] = 1732584193;
        state[1] = -271733879;
        state[2] = -1732584194;
        state[3] = 271733878;
        Arrays.fill(digestBits, (byte) 0);
    }

    public synchronized void engineUpdate(byte b) {
        int i = (int) ((count >>> 3) & 63);
        count += 8;
        buffer[i] = b;
        if (i >= 63) {
            transform(buffer, 0);
        }
    }

    public synchronized void engineUpdate(byte[] bArr, int i, int i2) {
        int i3 = i;
        while (i2 > 0) {
            int i4 = (int) ((count >>> 3) & 63);

            if (i4 != 0 || i2 <= 64) {
                count += 8;
                buffer[i4] = bArr[i3];

                if (i4 >= 63) {
                    transform(buffer, 0);
                }

                i3++;
                i2--;
            } else {
                count += 512;
                transform(bArr, i3);
                i2 -= 64;
                i3 += 64;
            }
        }
    }


    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Object clone() {
        return new VMD5(this);
    }

    public void update(byte b) {
        engineUpdate(b);
    }

    public void update(byte[] bArr, int i, int i2) {
        engineUpdate(bArr, i, i2);
    }

    public void update(byte[] bArr) {
        engineUpdate(bArr, 0, bArr.length);
    }
}
