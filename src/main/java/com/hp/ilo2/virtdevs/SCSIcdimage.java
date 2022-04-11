package com.hp.ilo2.virtdevs;

import com.hp.ilo2.remcons.telnet;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;


public class SCSIcdimage extends SCSI {
    int fdd_state = 0;
    int event_state = 0;
    long media_sz;
    virtdevs v;

    @Override // com.hp.ilo2.virtdevs.SCSI
    public void setWriteProt(boolean z) {
        this.writeprot = z;
    }

    public SCSIcdimage(Socket socket, InputStream inputStream, BufferedOutputStream bufferedOutputStream, String str, int i, virtdevs virtdevsVar) throws IOException {
        super(socket, inputStream, bufferedOutputStream, str, i);
        D.println(1, new StringBuffer().append("Media open returns ").append(this.media.open(str, 0)).append(" / ").append(this.media.size()).append(" bytes").toString());
        this.v = virtdevsVar;
    }

    @Override // com.hp.ilo2.virtdevs.SCSI
    public boolean process() throws IOException {
        boolean z = true;
        D.println(1, new StringBuffer().append("Device: ").append(this.selectedDevice).append(" (").append(this.targetIsDevice).append(")").toString());
        read_command(this.req, 12);
        D.print(1, "SCSI Request: ");
        D.hexdump(1, this.req, 12);
        this.v.ParentApp.remconsObj.setvmAct(1);
        this.media_sz = this.media.size();
        if (this.media_sz == 0) {
            this.reply.setmedia(0);
            this.fdd_state = 0;
            this.event_state = 4;
        } else {
            this.reply.setmedia(1);
            this.fdd_state++;
            if (this.fdd_state > 2) {
                this.fdd_state = 2;
            }
            if (this.event_state == 4) {
                this.event_state = 0;
            }
            this.event_state++;
            if (this.event_state > 2) {
                this.event_state = 2;
            }
        }
        switch (this.req[0] & 255) {
            case 0:
                client_test_unit_ready();
                break;
            case SCSI.SCSI_START_STOP_UNIT :
                z = client_start_stop_unit(this.req);
                break;
            case SCSI.SCSI_SEND_DIAGNOSTIC :
                client_send_diagnostic();
                break;
            case SCSI.SCSI_PA_MEDIA_REMOVAL :
                client_pa_media_removal(this.req);
                break;
            case SCSI.SCSI_READ_CAPACITY :
                client_read_capacity();
                break;
            case SCSI.SCSI_READ_10 :
            case SCSI.SCSI_READ_12 :
                client_read(this.req);
                break;
            case SCSI.SCSI_READ_TOC :
                client_read_toc(this.req);
                break;
            case SCSI.SCSI_GET_EVENT_STATUS :
                client_get_event_status(this.req);
                break;
            case SCSI.SCSI_MODE_SENSE :
                client_mode_sense(this.req);
                break;
            default:
                D.println(0, new StringBuffer().append("Unknown request:cmd = ").append(Integer.toHexString(this.req[0])).toString());
                this.reply.set(5, 36, 0, 0);
                this.reply.send(this.out);
                this.out.flush();
                break;
        }
        return z;
    }

    void client_send_diagnostic() throws IOException {
    }

    void client_read(byte[] bArr) throws IOException {
        long mk_int32 = SCSI.mk_int32(bArr, 2) * 2048;
        int mk_int322 = (bArr[0] == 168 ? SCSI.mk_int32(bArr, 6) : SCSI.mk_int16(bArr, 7)) * 2048;
        D.println(3, new StringBuffer().append("CDImage :Client read ").append(mk_int32).append(", len=").append(mk_int322).toString());
        if (this.fdd_state == 0) {
            D.println(3, "media not present");
            this.reply.set(2, 58, 0, 0);
            mk_int322 = 0;
        } else if (this.fdd_state == 1) {
            D.println(3, "media changed");
            this.reply.set(6, 40, 0, 0);
            mk_int322 = 0;
            this.fdd_state = 2;
        } else if (mk_int32 < 0 || mk_int32 >= this.media_sz) {
            this.reply.set(5, 33, 0, 0);
            mk_int322 = 0;
        } else {
            this.media.read(mk_int32, mk_int322, this.buffer);
            this.reply.set(0, 0, 0, mk_int322);
        }
        this.reply.send(this.out);
        if (mk_int322 != 0) {
            this.out.write(this.buffer, 0, mk_int322);
        }
        this.out.flush();
    }

    void client_pa_media_removal(byte[] bArr) throws IOException {
        if ((bArr[4] & 1) != 0) {
            D.println(3, "Media removal prevented");
        } else {
            D.println(3, "Media removal allowed");
        }
        this.reply.set(0, 0, 0, 0);
        this.reply.send(this.out);
        this.out.flush();
    }

    boolean client_start_stop_unit(byte[] bArr) throws IOException {
        this.reply.set(0, 0, 0, 0);
        this.reply.send(this.out);
        this.out.flush();
        if ((bArr[4] & 3) != 2) {
            return true;
        }
        this.fdd_state = 0;
        this.event_state = 4;
        D.println(3, "Media eject");
        return false;
    }

    void client_test_unit_ready() throws IOException {
        if (this.fdd_state == 0) {
            D.println(3, "media not present");
            this.reply.set(2, 58, 0, 0);
        } else if (this.fdd_state == 1) {
            D.println(3, "media changed");
            this.reply.set(6, 40, 0, 0);
            this.fdd_state = 2;
        } else {
            D.println(3, "device ready");
            this.reply.set(0, 0, 0, 0);
        }
        this.reply.send(this.out);
        this.out.flush();
    }

    void client_read_capacity() throws IOException {
        byte[] bArr = {0, 0, 0, 0, 0, 0, 0, 0};
        this.reply.set(0, 0, 0, bArr.length);
        if (this.fdd_state == 0) {
            this.reply.set(2, 58, 0, 0);
        } else if (this.fdd_state == 1) {
            this.reply.set(6, 40, 0, 0);
        } else {
            int size = (int) ((this.media.size() / 2048) - 1);
            bArr[0] = (byte) ((size >> 24) & telnet.TELNET_IAC);
            bArr[1] = (byte) ((size >> 16) & telnet.TELNET_IAC);
            bArr[2] = (byte) ((size >> 8) & telnet.TELNET_IAC);
            bArr[3] = (byte) ((size >> 0) & telnet.TELNET_IAC);
            bArr[6] = 8;
        }
        this.reply.send(this.out);
        if (this.fdd_state == 2) {
            this.out.write(bArr, 0, bArr.length);
        }
        this.out.flush();
        D.print(3, "client_read_capacity: ");
        D.hexdump(3, bArr, 8);
    }

    void client_read_toc(byte[] bArr) throws IOException {
        boolean z = (bArr[1] & 2) != 0;
        int i = (bArr[9] & 192) >> 6;
        int size = (int) (this.media.size() / 2048);
        double d = (size / 75.0d) + 2.0d;
        int i2 = ((int) d) / 60;
        int i3 = ((int) d) % 60;
        int i4 = (int) ((d - ((int) d)) * 75.0d);
        int mk_int16 = SCSI.mk_int16(bArr, 7);
        for (int i5 = 0; i5 < mk_int16; i5++) {
            this.buffer[i5] = 0;
        }
        if (i == 0) {
            this.buffer[0] = 0;
            this.buffer[1] = 18;
            this.buffer[2] = 1;
            this.buffer[3] = 1;
            this.buffer[4] = 0;
            this.buffer[5] = 20;
            this.buffer[6] = 1;
            this.buffer[7] = 0;
            this.buffer[8] = 0;
            this.buffer[9] = 0;
            this.buffer[10] = z ? (byte) 2 : (byte) 0;
            this.buffer[11] = 0;
            this.buffer[12] = 0;
            this.buffer[13] = 20;
            this.buffer[14] = -86;
            this.buffer[15] = 0;
            this.buffer[16] = 0;
            this.buffer[17] = z ? (byte) i2 : (byte) ((size >> 16) & telnet.TELNET_IAC);
            this.buffer[18] = z ? (byte) i3 : (byte) ((size >> 8) & telnet.TELNET_IAC);
            this.buffer[19] = z ? (byte) i4 : (byte) (size & telnet.TELNET_IAC);
        }
        if (i == 1) {
            this.buffer[0] = 0;
            this.buffer[1] = 10;
            this.buffer[2] = 1;
            this.buffer[3] = 1;
            this.buffer[4] = 0;
            this.buffer[5] = 20;
            this.buffer[6] = 1;
            this.buffer[7] = 0;
            this.buffer[8] = 0;
            this.buffer[9] = 0;
            this.buffer[10] = z ? (byte) 2 : (byte) 0;
            this.buffer[11] = 0;
        }
        int i6 = 412;
        if (mk_int16 < 412) {
            i6 = mk_int16;
        }
        D.hexdump(3, this.buffer, i6);
        this.reply.set(0, 0, 0, i6);
        this.reply.send(this.out);
        this.out.write(this.buffer, 0, i6);
        this.out.flush();
    }

    void client_mode_sense(byte[] bArr) throws IOException {
        this.buffer[0] = 0;
        this.buffer[1] = 8;
        this.buffer[2] = 1;
        this.buffer[3] = 0;
        this.buffer[4] = 0;
        this.buffer[5] = 0;
        this.buffer[6] = 0;
        this.buffer[7] = 0;
        this.reply.set(0, 0, 0, 8);
        D.hexdump(3, this.buffer, 8);
        this.reply.setmedia(this.buffer[2]);
        this.reply.send(this.out);
        this.out.write(this.buffer, 0, 8);
        this.out.flush();
    }

    void client_get_event_status(byte[] bArr) throws IOException {
        byte b = bArr[4];
        int mk_int16 = SCSI.mk_int16(bArr, 7);
        for (int i = 0; i < mk_int16; i++) {
            this.buffer[i] = 0;
        }
        if ((bArr[1] & 1) == 0) {
            this.reply.set(5, 36, 0, 0);
            this.reply.send(this.out);
            this.out.flush();
        }
        if ((b & 16) != 0) {
            this.buffer[0] = 0;
            this.buffer[1] = 6;
            this.buffer[2] = 4;
            this.buffer[3] = 16;
            if (this.event_state == 0) {
                this.buffer[4] = 0;
                this.buffer[5] = 0;
            } else if (this.event_state == 1) {
                this.buffer[4] = 4;
                this.buffer[5] = 2;
                if (mk_int16 > 4) {
                    this.event_state = 2;
                }
            } else if (this.event_state == 4) {
                this.buffer[4] = 3;
                this.buffer[5] = 0;
                if (mk_int16 > 4) {
                    this.event_state = 0;
                }
            } else {
                this.buffer[4] = 0;
                this.buffer[5] = 2;
            }
            D.hexdump(3, this.buffer, 8);
            this.reply.set(0, 0, 0, mk_int16 < 8 ? mk_int16 : 8);
            this.reply.send(this.out);
            this.out.write(this.buffer, 0, mk_int16 < 8 ? mk_int16 : 8);
            this.out.flush();
            return;
        }
        this.buffer[0] = 0;
        this.buffer[1] = 2;
        this.buffer[2] = Byte.MIN_VALUE;
        this.buffer[3] = 16;
        D.hexdump(3, this.buffer, 4);
        this.reply.set(0, 0, 0, mk_int16 < 4 ? mk_int16 : 4);
        this.reply.send(this.out);
        this.out.write(this.buffer, 0, mk_int16 < 4 ? mk_int16 : 4);
        this.out.flush();
    }
}
