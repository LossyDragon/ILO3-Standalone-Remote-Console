package com.hp.ilo2.remcons;

public class Aes {
    public static final int Bits128 = 0;
    public static final int Bits192 = 1;
    public static final int Bits256 = 2;
    private int Nb;
    private int Nk;
    private int Nr;
    private final byte[] key;
    private byte[][] Sbox;
    private byte[][] iSbox;
    private byte[][] w;
    private byte[][] Rcon;
    private byte[][] State;
    byte[] ofb_iv = new byte[16];
    int ofb_num;

    public Aes(int n, byte[] byArray) {
        this.SetNbNkNr(n);
        this.key = new byte[this.Nk * 4];
        if (byArray.length < this.key.length) {
            System.out.println("Alert: KeyBytes size is less than specified KeySize");
        }
        System.arraycopy(byArray, 0, this.key, 0, this.key.length);
        this.BuildSbox();
        this.BuildInvSbox();
        this.BuildRcon();
        this.KeyExpansion();
    }

    public void Cipher(byte[] byArray, byte[] byArray2) {
        if (byArray.length < 16) {
            System.out.println("Alert- InputSize:" + byArray.length + " is less than standard size:16");
        }
        if (byArray2.length < 16) {
            System.out.println("Alert- OutputSize:" + byArray2.length + " is less than standard size:16");
        }
        this.State = new byte[4][this.Nb];
        int n = 0;
        while (n < 4 * this.Nb) {
            this.State[n % 4][n / 4] = byArray[n];
            ++n;
        }
        this.AddRoundKey(0);
        int n2 = 1;
        while (n2 <= this.Nr - 1) {
            this.SubBytes();
            this.ShiftRows();
            this.MixColumns();
            this.AddRoundKey(n2);
            ++n2;
        }
        this.SubBytes();
        this.ShiftRows();
        this.AddRoundKey(this.Nr);
        int n3 = 0;
        while (n3 < 4 * this.Nb) {
            byArray2[n3] = this.State[n3 % 4][n3 / 4];
            ++n3;
        }
    }

    public void InvCipher(byte[] byArray, byte[] byArray2) {
        this.State = new byte[4][this.Nb];
        int n = 0;
        while (n < 4 * this.Nb) {
            this.State[n % 4][n / 4] = byArray[n];
            ++n;
        }
        this.AddRoundKey(this.Nr);
        int n2 = this.Nr - 1;
        while (n2 >= 1) {
            this.InvShiftRows();
            this.InvSubBytes();
            this.AddRoundKey(n2);
            this.InvMixColumns();
            --n2;
        }
        this.InvShiftRows();
        this.InvSubBytes();
        this.AddRoundKey(0);
        int n3 = 0;
        while (n3 < 4 * this.Nb) {
            byArray2[n3] = this.State[n3 % 4][n3 / 4];
            ++n3;
        }
    }

    private void SetNbNkNr(int n) {
        this.Nb = 4;
        if (n == 0) {
            this.Nk = 4;
            this.Nr = 10;
        } else if (n == 1) {
            this.Nk = 6;
            this.Nr = 12;
        } else if (n == 2) {
            this.Nk = 8;
            this.Nr = 14;
        } else {
            System.out.println("Alert: Invalid keysize Specified for SetNbNkNr");
            System.out.println("Pls use constants from Aes.KeySize");
        }
    }

    private void BuildSbox() {
        byte[][] byArrayArray = new byte[][]{{99, 124, 119, 123, -14, 107, 111, -59, 48, 1, 103, 43, -2, -41, -85, 118}, {-54, -126, -55, 125, -6, 89, 71, -16, -83, -44, -94, -81, -100, -92, 114, -64}, {-73, -3, -109, 38, 54, 63, -9, -52, 52, -91, -27, -15, 113, -40, 49, 21}, {4, -57, 35, -61, 24, -106, 5, -102, 7, 18, -128, -30, -21, 39, -78, 117}, {9, -125, 44, 26, 27, 110, 90, -96, 82, 59, -42, -77, 41, -29, 47, -124}, {83, -47, 0, -19, 32, -4, -79, 91, 106, -53, -66, 57, 74, 76, 88, -49}, {-48, -17, -86, -5, 67, 77, 51, -123, 69, -7, 2, 127, 80, 60, -97, -88}, {81, -93, 64, -113, -110, -99, 56, -11, -68, -74, -38, 33, 16, -1, -13, -46}, {-51, 12, 19, -20, 95, -105, 68, 23, -60, -89, 126, 61, 100, 93, 25, 115}, {96, -127, 79, -36, 34, 42, -112, -120, 70, -18, -72, 20, -34, 94, 11, -37}, {-32, 50, 58, 10, 73, 6, 36, 92, -62, -45, -84, 98, -111, -107, -28, 121}, {-25, -56, 55, 109, -115, -43, 78, -87, 108, 86, -12, -22, 101, 122, -82, 8}, {-70, 120, 37, 46, 28, -90, -76, -58, -24, -35, 116, 31, 75, -67, -117, -118}, {112, 62, -75, 102, 72, 3, -10, 14, 97, 53, 87, -71, -122, -63, 29, -98}, {-31, -8, -104, 17, 105, -39, -114, -108, -101, 30, -121, -23, -50, 85, 40, -33}, {-116, -95, -119, 13, -65, -26, 66, 104, 65, -103, 45, 15, -80, 84, -69, 22}};
        this.Sbox = byArrayArray;
    }

    private void BuildInvSbox() {
        byte[][] byArrayArray = new byte[][]{{82, 9, 106, -43, 48, 54, -91, 56, -65, 64, -93, -98, -127, -13, -41, -5}, {124, -29, 57, -126, -101, 47, -1, -121, 52, -114, 67, 68, -60, -34, -23, -53}, {84, 123, -108, 50, -90, -62, 35, 61, -18, 76, -107, 11, 66, -6, -61, 78}, {8, 46, -95, 102, 40, -39, 36, -78, 118, 91, -94, 73, 109, -117, -47, 37}, {114, -8, -10, 100, -122, 104, -104, 22, -44, -92, 92, -52, 93, 101, -74, -110}, {108, 112, 72, 80, -3, -19, -71, -38, 94, 21, 70, 87, -89, -115, -99, -124}, {-112, -40, -85, 0, -116, -68, -45, 10, -9, -28, 88, 5, -72, -77, 69, 6}, {-48, 44, 30, -113, -54, 63, 15, 2, -63, -81, -67, 3, 1, 19, -118, 107}, {58, -111, 17, 65, 79, 103, -36, -22, -105, -14, -49, -50, -16, -76, -26, 115}, {-106, -84, 116, 34, -25, -83, 53, -123, -30, -7, 55, -24, 28, 117, -33, 110}, {71, -15, 26, 113, 29, 41, -59, -119, 111, -73, 98, 14, -86, 24, -66, 27}, {-4, 86, 62, 75, -58, -46, 121, 32, -102, -37, -64, -2, 120, -51, 90, -12}, {31, -35, -88, 51, -120, 7, -57, 49, -79, 18, 16, 89, 39, -128, -20, 95}, {96, 81, 127, -87, 25, -75, 74, 13, 45, -27, 122, -97, -109, -55, -100, -17}, {-96, -32, 59, 77, -82, 42, -11, -80, -56, -21, -69, 60, -125, 83, -103, 97}, {23, 43, 4, 126, -70, 119, -42, 38, -31, 105, 20, 99, 85, 33, 12, 125}};
        this.iSbox = byArrayArray;
    }

    private void BuildRcon() {
        byte[][] byArrayArray = new byte[][]{{0, 0, 0, 0}, {1, 0, 0, 0}, {2, 0, 0, 0}, {4, 0, 0, 0}, {8, 0, 0, 0}, {16, 0, 0, 0}, {32, 0, 0, 0}, {64, 0, 0, 0}, {-128, 0, 0, 0}, {27, 0, 0, 0}, {54, 0, 0, 0}};
        this.Rcon = byArrayArray;
    }

    private void AddRoundKey(int n) {
        int n2 = 0;
        while (n2 < 4) {
            int n3 = 0;
            while (n3 < 4) {
                this.State[n2][n3] = (byte) (this.State[n2][n3] & 0xFF ^ this.w[n * 4 + n3][n2] & 0xFF);
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
                this.State[n][n2] = this.Sbox[(byte) (this.State[n][n2] >> 4 & 0xF)][this.State[n][n2] & 0xF];
                ++n2;
            }
            ++n;
        }
    }

    private void InvSubBytes() {
        int n = 0;
        while (n < 4) {
            int n2 = 0;
            while (n2 < 4) {
                this.State[n][n2] = this.iSbox[(byte) (this.State[n][n2] >> 4 & 0xF)][this.State[n][n2] & 0xF];
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
                byArray[n2][n] = this.State[n2][n];
                ++n;
            }
            ++n2;
        }
        n = 1;
        while (n < 4) {
            int n3 = 0;
            while (n3 < 4) {
                this.State[n][n3] = byArray[n][(n3 + n) % this.Nb];
                ++n3;
            }
            ++n;
        }
    }

    private void InvShiftRows() {
        int n;
        byte[][] byArray = new byte[4][4];
        int n2 = 0;
        while (n2 < 4) {
            n = 0;
            while (n < 4) {
                byArray[n2][n] = this.State[n2][n];
                ++n;
            }
            ++n2;
        }
        n = 1;
        while (n < 4) {
            int n3 = 0;
            while (n3 < 4) {
                this.State[n][(n3 + n) % this.Nb] = byArray[n][n3];
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
                byArray[n2][n] = this.State[n2][n];
                ++n;
            }
            ++n2;
        }
        n = 0;
        while (n < 4) {
            this.State[0][n] = (byte) (Aes.gfmultby02(byArray[0][n]) & 0xFF ^ Aes.gfmultby03(byArray[1][n]) & 0xFF ^ Aes.gfmultby01(byArray[2][n]) & 0xFF ^ Aes.gfmultby01(byArray[3][n]) & 0xFF);
            this.State[1][n] = (byte) (Aes.gfmultby01(byArray[0][n]) & 0xFF ^ Aes.gfmultby02(byArray[1][n]) & 0xFF ^ Aes.gfmultby03(byArray[2][n]) & 0xFF ^ Aes.gfmultby01(byArray[3][n]) & 0xFF);
            this.State[2][n] = (byte) (Aes.gfmultby01(byArray[0][n]) & 0xFF ^ Aes.gfmultby01(byArray[1][n]) & 0xFF ^ Aes.gfmultby02(byArray[2][n]) & 0xFF ^ Aes.gfmultby03(byArray[3][n]) & 0xFF);
            this.State[3][n] = (byte) (Aes.gfmultby03(byArray[0][n]) & 0xFF ^ Aes.gfmultby01(byArray[1][n]) & 0xFF ^ Aes.gfmultby01(byArray[2][n]) & 0xFF ^ Aes.gfmultby02(byArray[3][n]) & 0xFF);
            ++n;
        }
    }

    private void InvMixColumns() {
        int n;
        byte[][] byArray = new byte[4][4];
        int n2 = 0;
        while (n2 < 4) {
            n = 0;
            while (n < 4) {
                byArray[n2][n] = this.State[n2][n];
                ++n;
            }
            ++n2;
        }
        n = 0;
        while (n < 4) {
            this.State[0][n] = (byte) (Aes.gfmultby0e(byArray[0][n]) & 0xFF ^ Aes.gfmultby0b(byArray[1][n]) & 0xFF ^ Aes.gfmultby0d(byArray[2][n]) & 0xFF ^ Aes.gfmultby09(byArray[3][n]) & 0xFF);
            this.State[1][n] = (byte) (Aes.gfmultby09(byArray[0][n]) & 0xFF ^ Aes.gfmultby0e(byArray[1][n]) & 0xFF ^ Aes.gfmultby0b(byArray[2][n]) & 0xFF ^ Aes.gfmultby0d(byArray[3][n]) & 0xFF);
            this.State[2][n] = (byte) (Aes.gfmultby0d(byArray[0][n]) & 0xFF ^ Aes.gfmultby09(byArray[1][n]) & 0xFF ^ Aes.gfmultby0e(byArray[2][n]) & 0xFF ^ Aes.gfmultby0b(byArray[3][n]) & 0xFF);
            this.State[3][n] = (byte) (Aes.gfmultby0b(byArray[0][n]) & 0xFF ^ Aes.gfmultby0d(byArray[1][n]) & 0xFF ^ Aes.gfmultby09(byArray[2][n]) & 0xFF ^ Aes.gfmultby0e(byArray[3][n]) & 0xFF);
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

    private static byte gfmultby09(byte by) {
        return (byte) (Aes.gfmultby02(Aes.gfmultby02(Aes.gfmultby02(by))) & 0xFF ^ by & 0xFF);
    }

    private static byte gfmultby0b(byte by) {
        return (byte) (Aes.gfmultby02(Aes.gfmultby02(Aes.gfmultby02(by))) & 0xFF ^ Aes.gfmultby02(by) & 0xFF ^ by & 0xFF);
    }

    private static byte gfmultby0d(byte by) {
        return (byte) (Aes.gfmultby02(Aes.gfmultby02(Aes.gfmultby02(by))) & 0xFF ^ Aes.gfmultby02(Aes.gfmultby02(by)) & 0xFF ^ by & 0xFF);
    }

    private static byte gfmultby0e(byte by) {
        return (byte) (Aes.gfmultby02(Aes.gfmultby02(Aes.gfmultby02(by))) & 0xFF ^ Aes.gfmultby02(Aes.gfmultby02(by)) & 0xFF ^ Aes.gfmultby02(by) & 0xFF);
    }

    private void KeyExpansion() {
        this.w = new byte[this.Nb * (this.Nr + 1)][4];
        int n = 0;
        while (n < this.Nk) {
            this.w[n][0] = this.key[4 * n];
            this.w[n][1] = this.key[4 * n + 1];
            this.w[n][2] = this.key[4 * n + 2];
            this.w[n][3] = this.key[4 * n + 3];
            ++n;
        }
        byte[] byArray = new byte[4];
        int n2 = this.Nk;
        while (n2 < this.Nb * (this.Nr + 1)) {
            byArray[0] = this.w[n2 - 1][0];
            byArray[1] = this.w[n2 - 1][1];
            byArray[2] = this.w[n2 - 1][2];
            byArray[3] = this.w[n2 - 1][3];
            if (n2 % this.Nk == 0) {
                byArray = this.SubWord(this.RotWord(byArray));
                byArray[0] = (byte) (byArray[0] & 0xFF ^ this.Rcon[n2 / this.Nk][0] & 0xFF);
                byArray[1] = (byte) (byArray[1] & 0xFF ^ this.Rcon[n2 / this.Nk][1] & 0xFF);
                byArray[2] = (byte) (byArray[2] & 0xFF ^ this.Rcon[n2 / this.Nk][2] & 0xFF);
                byArray[3] = (byte) (byArray[3] & 0xFF ^ this.Rcon[n2 / this.Nk][3] & 0xFF);
            } else if (this.Nk > 6 && n2 % this.Nk == 4) {
                byArray = this.SubWord(byArray);
            }
            this.w[n2][0] = (byte) (this.w[n2 - this.Nk][0] & 0xFF ^ byArray[0] & 0xFF);
            this.w[n2][1] = (byte) (this.w[n2 - this.Nk][1] & 0xFF ^ byArray[1] & 0xFF);
            this.w[n2][2] = (byte) (this.w[n2 - this.Nk][2] & 0xFF ^ byArray[2] & 0xFF);
            this.w[n2][3] = (byte) (this.w[n2 - this.Nk][3] & 0xFF ^ byArray[3] & 0xFF);
            ++n2;
        }
        this.InitOfbIv();
    }

    private void InitOfbIv() {
        this.ofb_num = 0;
        int n = 0;
        while (n < this.ofb_iv.length) {
            this.ofb_iv[n] = 0;
            ++n;
        }
    }

    private byte[] SubWord(byte[] byArray) {
        byte[] byArray2 = new byte[]{this.Sbox[(byte) (byArray[0] >> 4 & 0xF)][byArray[0] & 0xF], this.Sbox[(byte) (byArray[1] >> 4 & 0xF)][byArray[1] & 0xF], this.Sbox[(byte) (byArray[2] >> 4 & 0xF)][byArray[2] & 0xF], this.Sbox[(byte) (byArray[3] >> 4 & 0xF)][byArray[3] & 0xF]};
        return byArray2;
    }

    private byte[] RotWord(byte[] byArray) {
        byte[] byArray2 = new byte[]{byArray[1], byArray[2], byArray[3], byArray[0]};
        return byArray2;
    }

    public void Dump() {
        System.out.println("Nb = " + this.Nb + " Nk = " + this.Nk + " Nr = " + this.Nr);
        System.out.println("\nThe key is \n" + this.DumpKey());
        System.out.println("\nThe Sbox is \n" + this.DumpTwoByTwo(this.Sbox));
        System.out.println("\nThe w array is \n" + this.DumpTwoByTwo(this.w));
        System.out.println("\nThe State array is \n" + this.DumpTwoByTwo(this.State));
    }

    public String DumpKey() {
        StringBuilder string = new StringBuilder();
        String string2 = "";
        int n = 0;
        while (n < this.key.length) {
            string2 = Integer.toHexString(this.key[n] & 0xFF);
            if (string2.length() == 1) {
                string.append("0");
            }
            string.append(string2).append(" ");
            ++n;
        }
        return string.toString();
    }

    public String DumpTwoByTwo(byte[][] byArray) {
        StringBuilder string = new StringBuilder();
        String string2 = "";
        int n = 0;
        while (n < byArray.length) {
            string.append("[").append(n).append("]").append(" ");
            int n2 = 0;
            while (n2 < byArray[n].length) {
                string2 = Integer.toHexString(byArray[n][n2] & 0xFF);
                if (string2.length() == 1) {
                    string.append("0");
                }
                string.append(string2).append(" ");
                ++n2;
            }
            string.append("\n");
            ++n;
        }
        return string.toString();
    }

    public byte randomValue() {
        if (this.ofb_num == 0) {
            this.Cipher(this.ofb_iv, this.ofb_iv);
        }
        byte by = this.ofb_iv[this.ofb_num];
        this.ofb_num = this.ofb_num + 1 & 0xF;
        return by;
    }
}
