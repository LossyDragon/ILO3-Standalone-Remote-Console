package com.hp.ilo2.remcons;

public class Aes {
    
    byte[] ofb_iv = new byte[16];
    int ofb_num;
    private byte[][] Rcon;
    private byte[][] Sbox;
    private byte[][] State;
    private byte[][] w;
    private final byte[] key;
    private int Nb;
    private int Nk;
    private int Nr;

    public Aes(int n, byte[] byArray) {
        SetNbNkNr(n);
        key = new byte[Nk * 4];

        if (byArray.length < key.length) {
            System.out.println("Alert: KeyBytes size is less than specified KeySize");
        }

        System.arraycopy(byArray, 0, key, 0, key.length);

        BuildSbox();
        BuildRcon();
        KeyExpansion();
    }

    public void Cipher(byte[] byArray, byte[] byArray2) {
        if (byArray.length < 16) {
            System.out.println("Alert- InputSize:" + byArray.length + " is less than standard size:16");
        }

        if (byArray2.length < 16) {
            System.out.println("Alert- OutputSize:" + byArray2.length + " is less than standard size:16");
        }

        State = new byte[4][Nb];

        int n = 0;
        while (n < 4 * Nb) {
            State[n % 4][n / 4] = byArray[n];
            ++n;
        }

        AddRoundKey(0);

        int n2 = 1;
        while (n2 <= Nr - 1) {
            SubBytes();
            ShiftRows();
            MixColumns();
            AddRoundKey(n2);
            ++n2;
        }

        SubBytes();
        ShiftRows();
        AddRoundKey(Nr);

        int n3 = 0;
        while (n3 < 4 * Nb) {
            byArray2[n3] = State[n3 % 4][n3 / 4];
            ++n3;
        }
    }

    private void SetNbNkNr(int n) {
        Nb = 4;

        if (n == 0) {
            Nk = 4;
            Nr = 10;
        } else if (n == 1) {
            Nk = 6;
            Nr = 12;
        } else if (n == 2) {
            Nk = 8;
            Nr = 14;
        } else {
            System.out.println("Alert: Invalid keysize Specified for SetNbNkNr");
            System.out.println("Pls use constants from Aes.KeySize");
        }
    }

    private void BuildSbox() {
        Sbox = new byte[][]{{99, 124, 119, 123, -14, 107, 111, -59, 48, 1, 103, 43, -2, -41, -85, 118}, {-54, -126, -55, 125, -6, 89, 71, -16, -83, -44, -94, -81, -100, -92, 114, -64}, {-73, -3, -109, 38, 54, 63, -9, -52, 52, -91, -27, -15, 113, -40, 49, 21}, {4, -57, 35, -61, 24, -106, 5, -102, 7, 18, -128, -30, -21, 39, -78, 117}, {9, -125, 44, 26, 27, 110, 90, -96, 82, 59, -42, -77, 41, -29, 47, -124}, {83, -47, 0, -19, 32, -4, -79, 91, 106, -53, -66, 57, 74, 76, 88, -49}, {-48, -17, -86, -5, 67, 77, 51, -123, 69, -7, 2, 127, 80, 60, -97, -88}, {81, -93, 64, -113, -110, -99, 56, -11, -68, -74, -38, 33, 16, -1, -13, -46}, {-51, 12, 19, -20, 95, -105, 68, 23, -60, -89, 126, 61, 100, 93, 25, 115}, {96, -127, 79, -36, 34, 42, -112, -120, 70, -18, -72, 20, -34, 94, 11, -37}, {-32, 50, 58, 10, 73, 6, 36, 92, -62, -45, -84, 98, -111, -107, -28, 121}, {-25, -56, 55, 109, -115, -43, 78, -87, 108, 86, -12, -22, 101, 122, -82, 8}, {-70, 120, 37, 46, 28, -90, -76, -58, -24, -35, 116, 31, 75, -67, -117, -118}, {112, 62, -75, 102, 72, 3, -10, 14, 97, 53, 87, -71, -122, -63, 29, -98}, {-31, -8, -104, 17, 105, -39, -114, -108, -101, 30, -121, -23, -50, 85, 40, -33}, {-116, -95, -119, 13, -65, -26, 66, 104, 65, -103, 45, 15, -80, 84, -69, 22}};
    }

    private void BuildRcon() {
        Rcon = new byte[][]{{0, 0, 0, 0}, {1, 0, 0, 0}, {2, 0, 0, 0}, {4, 0, 0, 0}, {8, 0, 0, 0}, {16, 0, 0, 0}, {32, 0, 0, 0}, {64, 0, 0, 0}, {-128, 0, 0, 0}, {27, 0, 0, 0}, {54, 0, 0, 0}};
    }

    private void AddRoundKey(int n) {
        int n2 = 0;
        while (n2 < 4) {
            int n3 = 0;
            while (n3 < 4) {
                State[n2][n3] = (byte) (State[n2][n3] & 0xFF ^ w[n * 4 + n3][n2] & 0xFF);
                ++n3;
            }

            ++n2;
        }
    }

    private void SubBytes() {
        int n = 0;
        while (n < 4) {
            int n2 = 0;
            while (n2 < 4) {
                State[n][n2] = Sbox[(byte) (State[n][n2] >> 4 & 0xF)][State[n][n2] & 0xF];
                ++n2;
            }

            ++n;
        }
    }

    private void ShiftRows() {
        int n;
        byte[][] byArray = new byte[4][4];

        int n2 = 0;
        while (n2 < 4) {
            n = 0;
            while (n < 4) {
                byArray[n2][n] = State[n2][n];
                ++n;
            }

            ++n2;
        }

        n = 1;
        while (n < 4) {
            int n3 = 0;
            while (n3 < 4) {
                State[n][n3] = byArray[n][(n3 + n) % Nb];
                ++n3;
            }

            ++n;
        }
    }

    private void MixColumns() {
        int n;
        byte[][] byArray = new byte[4][4];

        int n2 = 0;
        while (n2 < 4) {
            n = 0;
            while (n < 4) {
                byArray[n2][n] = State[n2][n];
                ++n;
            }

            ++n2;
        }

        n = 0;
        while (n < 4) {
            State[0][n] = (byte) (Aes.gfmultby02(byArray[0][n]) & 0xFF ^ Aes.gfmultby03(byArray[1][n]) & 0xFF ^ Aes.gfmultby01(byArray[2][n]) & 0xFF ^ Aes.gfmultby01(byArray[3][n]) & 0xFF);
            State[1][n] = (byte) (Aes.gfmultby01(byArray[0][n]) & 0xFF ^ Aes.gfmultby02(byArray[1][n]) & 0xFF ^ Aes.gfmultby03(byArray[2][n]) & 0xFF ^ Aes.gfmultby01(byArray[3][n]) & 0xFF);
            State[2][n] = (byte) (Aes.gfmultby01(byArray[0][n]) & 0xFF ^ Aes.gfmultby01(byArray[1][n]) & 0xFF ^ Aes.gfmultby02(byArray[2][n]) & 0xFF ^ Aes.gfmultby03(byArray[3][n]) & 0xFF);
            State[3][n] = (byte) (Aes.gfmultby03(byArray[0][n]) & 0xFF ^ Aes.gfmultby01(byArray[1][n]) & 0xFF ^ Aes.gfmultby01(byArray[2][n]) & 0xFF ^ Aes.gfmultby02(byArray[3][n]) & 0xFF);
            ++n;
        }
    }

    private static byte gfmultby01(byte by) {
        return by;
    }

    private static byte gfmultby02(byte by) {
        if ((by & 0xFF) < 128) {
            return (byte) (by << 1 & 0xFF);
        }

        return (byte) (by << 1 & 0xFF ^ 0x1B);
    }

    private static byte gfmultby03(byte by) {
        return (byte) (Aes.gfmultby02(by) & 0xFF ^ by & 0xFF);
    }

    private void KeyExpansion() {
        w = new byte[Nb * (Nr + 1)][4];
        int n = 0;
        while (n < Nk) {
            w[n][0] = key[4 * n];
            w[n][1] = key[4 * n + 1];
            w[n][2] = key[4 * n + 2];
            w[n][3] = key[4 * n + 3];
            ++n;
        }

        byte[] byArray = new byte[4];
        int n2 = Nk;

        while (n2 < Nb * (Nr + 1)) {
            byArray[0] = w[n2 - 1][0];
            byArray[1] = w[n2 - 1][1];
            byArray[2] = w[n2 - 1][2];
            byArray[3] = w[n2 - 1][3];

            if (n2 % Nk == 0) {
                byArray = SubWord(RotWord(byArray));
                byArray[0] = (byte) (byArray[0] & 0xFF ^ Rcon[n2 / Nk][0] & 0xFF);
                byArray[1] = (byte) (byArray[1] & 0xFF ^ Rcon[n2 / Nk][1] & 0xFF);
                byArray[2] = (byte) (byArray[2] & 0xFF ^ Rcon[n2 / Nk][2] & 0xFF);
                byArray[3] = (byte) (byArray[3] & 0xFF ^ Rcon[n2 / Nk][3] & 0xFF);
            } else if (Nk > 6 && n2 % Nk == 4) {
                byArray = SubWord(byArray);
            }

            w[n2][0] = (byte) (w[n2 - Nk][0] & 0xFF ^ byArray[0] & 0xFF);
            w[n2][1] = (byte) (w[n2 - Nk][1] & 0xFF ^ byArray[1] & 0xFF);
            w[n2][2] = (byte) (w[n2 - Nk][2] & 0xFF ^ byArray[2] & 0xFF);
            w[n2][3] = (byte) (w[n2 - Nk][3] & 0xFF ^ byArray[3] & 0xFF);
            ++n2;
        }

        InitOfbIv();
    }

    private void InitOfbIv() {
        ofb_num = 0;
        int n = 0;

        while (n < ofb_iv.length) {
            ofb_iv[n] = 0;
            ++n;
        }
    }

    private byte[] SubWord(byte[] byArray) {
        return new byte[]{Sbox[(byte) (byArray[0] >> 4 & 0xF)][byArray[0] & 0xF], Sbox[(byte) (byArray[1] >> 4 & 0xF)][byArray[1] & 0xF], Sbox[(byte) (byArray[2] >> 4 & 0xF)][byArray[2] & 0xF], Sbox[(byte) (byArray[3] >> 4 & 0xF)][byArray[3] & 0xF]};
    }

    private byte[] RotWord(byte[] byArray) {
        return new byte[]{byArray[1], byArray[2], byArray[3], byArray[0]};
    }

    public byte randomValue() {
        if (ofb_num == 0) {
            Cipher(ofb_iv, ofb_iv);
        }

        byte by = ofb_iv[ofb_num];
        ofb_num = ofb_num + 1 & 0xF;

        return by;
    }
}
