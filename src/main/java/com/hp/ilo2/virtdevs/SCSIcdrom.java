package com.hp.ilo2.virtdevs;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;


public class SCSIcdrom extends SCSI {
    public static final int SCSI_IOCTL_DATA_OUT = 0;
    public static final int SCSI_IOCTL_DATA_IN = 1;
    public static final int SCSI_IOCTL_DATA_UNSPECIFIED = 2;
    public static final int CONST = 0;
    static final int WRITE = 0;
    public static final int BLKS = 8388608;
    static final int B32 = 262144;
    static final int B24 = 196608;
    static final int B16 = 131072;
    static final int B08 = 65536;
    VErrorDialog dlg;
    virtdevs v;
    static final int NONE = 33554432;
    static final int READ = 16777216;
    static final int[] commands = {30, NONE, 37, 16777224, 29, NONE, 0, NONE, 40, 25296903, SCSI.SCSI_READ_12, 25427974, 27, NONE, SCSI.SCSI_READ_CD, 25362438, SCSI.SCSI_READ_CD_MSF, READ, 68, 16777224, 66, 16908295, 67, 16908295, 78, NONE, SCSI.SCSI_MECHANISM_STATUS, 16908296, 90, 16908295, 74, 16908295};
    byte[] sense = new byte[3];
    boolean do_split_reads = false;
    int retrycount = Integer.valueOf(virtdevs.prop.getProperty("com.hp.ilo2.virtdevs.retrycount", "10"));

    void media_err(byte[] bArr, byte[] bArr2) {
        this.dlg = new VErrorDialog("The CDROM drive reports a media error:\nCommand: " + D.hex(bArr[0], 2) + " " + D.hex(bArr[1], 2) + " " + D.hex(bArr[2], 2) + " " + D.hex(bArr[3], 2) + " " + D.hex(bArr[4], 2) + " " + D.hex(bArr[5], 2) + " " + D.hex(bArr[6], 2) + " " + D.hex(bArr[7], 2) + " " + D.hex(bArr[8], 2) + " " + D.hex(bArr[9], 2) + " " + D.hex(bArr[10], 2) + " " + D.hex(bArr[11], 2) + "\n" + "Sense Code: " + D.hex(bArr2[0], 2) + "/" + D.hex(bArr2[1], 2) + "/" + D.hex(bArr2[2], 2) + "\n\n", false);
    }

    public SCSIcdrom(Socket socket, InputStream inputStream, BufferedOutputStream bufferedOutputStream, String str, int i, virtdevs virtdevsVar) throws IOException {
        super(socket, inputStream, bufferedOutputStream, str, i);
        D.println(1, "Media opening " + str + "(" + (i | 2) + ")");
        D.println(1, "Media open returns " + this.media.open(str, i));
        this.v = virtdevsVar;
    }

    @Override // com.hp.ilo2.virtdevs.SCSI
    public void close() throws IOException {
        this.req[0] = 30;
        byte[] bArr = this.req;
        byte[] bArr2 = this.req;
        byte[] bArr3 = this.req;
        byte[] bArr4 = this.req;
        byte[] bArr5 = this.req;
        byte[] bArr6 = this.req;
        byte[] bArr7 = this.req;
        byte[] bArr8 = this.req;
        byte[] bArr9 = this.req;
        byte[] bArr10 = this.req;
        this.req[11] = 0;
        bArr10[10] = 0;
        bArr9[9] = 0;
        bArr8[8] = 0;
        bArr7[7] = 0;
        bArr6[7] = 0;
        bArr5[5] = 0;
        bArr4[4] = 0;
        bArr3[3] = 0;
        bArr2[2] = 0;
        bArr[1] = 0;
        this.media.scsi(this.req, 2, 0, this.buffer, null);
        super.close();
    }

    int scsi_length(int i, byte[] bArr) {
        int i2 = 0;
        int i3 = i + 1;
        switch (commands[i3] & 8323072) {
            case 0:
                i2 = commands[i3] & 65535;
                break;
            case B08 :
                i2 = bArr[commands[i3] & 65535] & 255;
                break;
            case B16 :
                i2 = SCSI.mk_int16(bArr, commands[i3] & 65535);
                break;
            case B24 :
                i2 = SCSI.mk_int24(bArr, commands[i3] & 65535);
                break;
            case B32 :
                i2 = SCSI.mk_int32(bArr, commands[i3] & 65535);
                break;
            default:
                D.println(0, "Unknown Size!");
                break;
        }
        if ((commands[i3] & BLKS) == 8388608) {
            i2 *= 2048;
        }
        return i2;
    }

    void start_stop_unit() {
        byte[] bArr = new byte[3];
        D.println(3, "Start/Stop unit = " + this.media.scsi(new byte[]{27, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0}, 2, 0, this.buffer, bArr) + " " + bArr[0] + "/" + bArr[1] + "/" + bArr[2]);
    }

    boolean within_75(byte[] bArr) {
        byte[] bArr2 = new byte[8];
        byte[] bArr3 = {37, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        boolean z = bArr[0] == 168;
        int mk_int32 = SCSI.mk_int32(bArr, 2);
        int mk_int322 = z ? SCSI.mk_int32(bArr, 6) : SCSI.mk_int16(bArr, 7);
        this.media.scsi(bArr3, 1, 8, bArr2, null);
        int mk_int323 = SCSI.mk_int32(bArr2, 0);
        return mk_int32 > mk_int323 - 75 || mk_int32 + mk_int322 > mk_int323 - 75;
    }

    int split_read() {
        boolean z = this.req[0] == 168;
        int mk_int32 = SCSI.mk_int32(this.req, 2);
        int mk_int322 = z ? SCSI.mk_int32(this.req, 6) : SCSI.mk_int16(this.req, 7);
        int i = Math.min(mk_int322, 32);
        this.req[2] = (byte) (mk_int32 >> 24);
        this.req[3] = (byte) (mk_int32 >> 16);
        this.req[4] = (byte) (mk_int32 >> 8);
        this.req[5] = (byte) mk_int32;
        if (z) {
            this.req[6] = (byte) (i >> 24);
            this.req[7] = (byte) (i >> 16);
            this.req[8] = (byte) (i >> 8);
            this.req[9] = (byte) i;
        } else {
            this.req[7] = (byte) (i >> 8);
            this.req[8] = (byte) i;
        }
        int scsi = this.media.scsi(this.req, 1, i * 2048, this.buffer, this.sense);
        if (scsi < 0) {
            return scsi;
        }
        int i2 = mk_int322 - i;
        if (i2 <= 0) {
            return scsi;
        }
        int i3 = mk_int32 + i;
        this.req[2] = (byte) (i3 >> 24);
        this.req[3] = (byte) (i3 >> 16);
        this.req[4] = (byte) (i3 >> 8);
        this.req[5] = (byte) i3;
        if (z) {
            this.req[6] = (byte) (i2 >> 24);
            this.req[7] = (byte) (i2 >> 16);
            this.req[8] = (byte) (i2 >> 8);
            this.req[9] = (byte) i2;
        } else {
            this.req[7] = (byte) (i2 >> 8);
            this.req[8] = (byte) i2;
        }
        int scsi2 = this.media.scsi(this.req, 1, i2 * 2048, this.buffer, this.sense, B08);
        if (scsi2 < 0) {
            return scsi2;
        }
        return scsi + scsi2;
    }

    @Override // com.hp.ilo2.virtdevs.SCSI
    public boolean process() throws IOException {
        int i;
        int i2;
        int open;
        read_command(this.req, 12);
        D.println(1, "SCSI Request:");
        D.hexdump(1, this.req, 12);
        this.v.ParentApp.remconsObj.setvmAct(1);
        if (this.media.dio.filehandle != -1 || (open = this.media.open(this.selectedDevice, this.targetIsDevice)) >= 0) {
            int i3 = 0;
            while (i3 < commands.length && this.req[0] != ((byte) commands[i3])) {
                i3 += 2;
            }
            if (i3 != commands.length) {
                int scsi_length = scsi_length(i3, this.req);
                int i4 = commands[i3 + 1] >> 24;
                int i5 = this.req[0] & 255;
                if (i4 == 0) {
                    read_complete(this.buffer, scsi_length);
                }
                D.println(1, "SCSI dir=" + i4 + " len=" + scsi_length);
                int i6 = 0;
                do {
                    long currentTimeMillis = System.currentTimeMillis();
                    if ((i5 == 40 || i5 == 168) && this.do_split_reads) {
                        i2 = split_read();
                    } else {
                        i2 = this.media.scsi(this.req, i4, scsi_length, this.buffer, this.sense);
                    }
                    D.println(1, "ret=" + i2 + " sense=" + D.hex(this.sense[0], 2) + " " + D.hex(this.sense[1], 2) + " " + D.hex(this.sense[2], 2) + " Time=" + (System.currentTimeMillis() - currentTimeMillis));
                    if (i5 == 90) {
                        D.println(1, "media type: " + D.hex(this.buffer[3], 2));
                        this.reply.setmedia(this.buffer[3]);
                    }
                    if (i5 == 67) {
                        D.hexdump(3, this.buffer, scsi_length);
                    }
                    if (i5 == 27) {
                        i2 = 0;
                    }
                    if (i5 == 40 || i5 == 168) {
                        if (this.sense[1] == 41) {
                            i2 = -1;
                        } else if (i2 < 0 && within_75(this.req)) {
                            this.sense[0] = 5;
                            this.sense[1] = 33;
                            this.sense[2] = 0;
                            i2 = 0;
                        } else if (i2 < 0) {
                            this.do_split_reads = true;
                        }
                    }
                    if (this.sense[0] == 3 || this.sense[0] == 4) {
                        media_err(this.req, this.sense);
                        i2 = -1;
                    }
                    if (i2 >= 0) {
                        break;
                    }
                    i6++;
                } while (i6 < this.retrycount);
                i = i2;
                if (i < 0 || i > B16) {
                    D.println(0, "AIEE! len out of bounds: " + i + ", cmd: " + D.hex(i5, 2) + "\n");
                    i = 0;
                    this.reply.set(5, 32, 0, 0);
                } else {
                    this.reply.set(this.sense[0], this.sense[1], this.sense[2], i);
                }
            } else {
                D.println(0, "AIEE! Unhandled command" + D.hex(this.req[0], 2) + "\n");
                this.reply.set(5, 32, 0, 0);
                i = 0;
            }
            this.reply.send(this.out);
            if (i != 0) {
                this.out.write(this.buffer, 0, i);
            }
            this.out.flush();
            return true;
        }
        new VErrorDialog("Could not open CDROM (" + this.media.dio.sysError(-open) + ")", false);
        throw new IOException("Couldn't open cdrom " + open);
    }
}
