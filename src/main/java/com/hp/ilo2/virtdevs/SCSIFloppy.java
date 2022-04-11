package com.hp.ilo2.virtdevs;

import com.hp.ilo2.intgapp.locinfo;
import com.hp.ilo2.remcons.telnet;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Date;


public class SCSIFloppy extends SCSI {
    long media_sz;
    virtdevs v;
    VErrorDialog dlg;
    int fdd_state = 0;
    Date date = new Date();
    byte[] rcs_resp = {0, 0, 0, 16, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 11, 64, 0, 0, 2, 0};

    @Override // com.hp.ilo2.virtdevs.SCSI
    public void setWriteProt(boolean z) {
        this.writeprot = z;
        if (this.fdd_state == 2) {
            this.fdd_state = 0;
        }
    }

    public SCSIFloppy(Socket socket, InputStream inputStream, BufferedOutputStream bufferedOutputStream, String str, int i, virtdevs virtdevsVar) throws IOException {
        super(socket, inputStream, bufferedOutputStream, str, i);
        D.print(1, new StringBuffer().append("open returns ").append(this.media.open(str, i)).toString());
        this.v = virtdevsVar;
    }

    @Override // com.hp.ilo2.virtdevs.SCSI
    public boolean process() throws IOException {
        this.date.setTime(System.currentTimeMillis());
        D.println(1, new StringBuffer().append("Date = ").append(this.date).toString());
        D.println(1, new StringBuffer().append("Device: ").append(this.selectedDevice).append(" (").append(this.targetIsDevice).append(")").toString());
        read_command(this.req, 12);
        D.print(1, "SCSI Request: ");
        D.hexdump(1, this.req, 12);
        this.media_sz = this.media.size();
        this.v.ParentApp.remconsObj.setvmAct(1);
        if (this.media_sz < 0 || (this.media.dio != null && this.media.dio.filehandle == -1)) {
            D.println(1, "Disk change detected\n");
            this.media.close();
            this.media.open(this.selectedDevice, this.targetIsDevice);
            this.media_sz = this.media.size();
            this.fdd_state = 0;
        }
        D.println(1, new StringBuffer().append("retval=").append(this.media_sz).append(" type=").append(this.media.type()).append(" physdrive=").append(this.media.dio != null ? this.media.dio.PhysicalDevice : -1).toString());
        if (this.media_sz == -6) {
            new VErrorDialog(this.v.ParentApp.dispFrame, new StringBuffer().append(this.selectedDevice).append(" ").append(this.v.ParentApp.remconsObj.getLocalString(locinfo.DIALOGSTR_2060)).append("\n\n").append(this.v.ParentApp.remconsObj.getLocalString(locinfo.DIALOGSTR_202f)).toString());
            return false;
        }
        if (this.media_sz <= 0) {
            this.reply.setmedia(0);
            this.fdd_state = 0;
        } else {
            this.reply.setmedia(36);
            this.fdd_state++;
            if (this.fdd_state > 2) {
                this.fdd_state = 2;
            }
        }
        if (!this.writeprot && this.media.wp()) {
            this.writeprot = true;
        }
        switch (this.req[0] & 255) {
            case 0:
                client_test_unit_ready();
                return true;
            case 4:
                client_format_unit(this.req);
                return true;
            case SCSI.SCSI_START_STOP_UNIT :
                client_start_stop_unit(this.req);
                return true;
            case SCSI.SCSI_SEND_DIAGNOSTIC :
                client_send_diagnostic();
                return true;
            case SCSI.SCSI_PA_MEDIA_REMOVAL :
                client_pa_media_removal(this.req);
                return true;
            case SCSI.SCSI_READ_CAPACITIES :
                client_read_capacities();
                return true;
            case SCSI.SCSI_READ_CAPACITY :
                client_read_capacity();
                return true;
            case SCSI.SCSI_READ_10 :
            case SCSI.SCSI_READ_12 :
                client_read(this.req);
                return true;
            case SCSI.SCSI_WRITE_10 :
            case SCSI.SCSI_WRITE_VERIFY :
            case SCSI.SCSI_WRITE_12 :
                client_write(this.req);
                return true;
            default:
                D.println(0, new StringBuffer().append("Unknown request:cmd = ").append(Integer.toHexString(this.req[0])).toString());
                return true;
        }
    }

    void client_read_capacities() throws IOException {
        if (this.fdd_state != 1) {
            this.reply.set(0, 0, 0, this.rcs_resp.length);
        } else {
            this.reply.set(6, 40, 0, this.rcs_resp.length);
            this.fdd_state = 2;
        }
        if (this.media.type() == 0) {
            byte[] bArr = this.rcs_resp;
            byte[] bArr2 = this.rcs_resp;
            byte[] bArr3 = this.rcs_resp;
            byte[] bArr4 = this.rcs_resp;
            byte[] bArr5 = this.rcs_resp;
            this.rcs_resp[11] = 0;
            bArr5[10] = 0;
            bArr4[7] = 0;
            bArr3[6] = 0;
            bArr2[5] = 0;
            bArr[4] = 0;
        } else if (this.media.type() == 100) {
            long size = this.media.size() / 512;
            this.rcs_resp[4] = (byte) ((size >> 24) & 255);
            this.rcs_resp[5] = (byte) ((size >> 16) & 255);
            this.rcs_resp[6] = (byte) ((size >> 8) & 255);
            this.rcs_resp[7] = (byte) ((size >> 0) & 255);
            this.rcs_resp[10] = 2;
            this.rcs_resp[11] = 0;
        } else {
            long size2 = this.media.size() / this.media.dio.BytesPerSec;
            this.rcs_resp[4] = (byte) ((size2 >> 24) & 255);
            this.rcs_resp[5] = (byte) ((size2 >> 16) & 255);
            this.rcs_resp[6] = (byte) ((size2 >> 8) & 255);
            this.rcs_resp[7] = (byte) ((size2 >> 0) & 255);
            this.rcs_resp[10] = (byte) ((this.media.dio.BytesPerSec >> 8) & telnet.TELNET_IAC);
            this.rcs_resp[11] = (byte) (this.media.dio.BytesPerSec & telnet.TELNET_IAC);
        }
        this.reply.setflags(this.writeprot);
        this.reply.send(this.out);
        this.out.write(this.rcs_resp, 0, this.rcs_resp.length);
        this.out.flush();
    }

    void client_send_diagnostic() throws IOException {
        this.fdd_state = 1;
    }

    void client_read(byte[] bArr) throws IOException {
        long mk_int32 = SCSI.mk_int32(bArr, 2) * 512;
        int mk_int322 = (bArr[0] == 168 ? SCSI.mk_int32(bArr, 6) : SCSI.mk_int16(bArr, 7)) * 512;
        D.println(3, new StringBuffer().append("FDIO.client_read:Client read ").append(mk_int32).append(", len=").append(mk_int322).toString());
        if (mk_int32 < 0 || mk_int32 >= this.media_sz) {
            this.reply.set(5, 33, 0, 0);
            mk_int322 = 0;
        } else {
            try {
                this.media.read(mk_int32, mk_int322, this.buffer);
                this.reply.set(0, 0, 0, mk_int322);
            } catch (IOException e) {
                D.println(0, new StringBuffer().append("Exception during read: ").append(e).toString());
                this.reply.set(3, 16, 0, 0);
                mk_int322 = 0;
            }
        }
        this.reply.setflags(this.writeprot);
        this.reply.send(this.out);
        if (mk_int322 != 0) {
            this.out.write(this.buffer, 0, mk_int322);
        }
        this.out.flush();
    }

    void client_write(byte[] bArr) throws IOException {
        boolean z = bArr[0] == 170;
        long mk_int32 = SCSI.mk_int32(bArr, 2) * 512;
        int mk_int322 = (z ? SCSI.mk_int32(bArr, 6) : SCSI.mk_int16(bArr, 7)) * 512;
        D.println(3, new StringBuffer().append("FDIO.client_write:lba = ").append(mk_int32).append(", length = ").append(mk_int322).toString());
        read_complete(this.buffer, mk_int322);
        if (this.writeprot) {
            this.reply.set(7, 39, 0, 0);
        } else if (mk_int32 < 0 || mk_int32 >= this.media_sz) {
            this.reply.set(5, 33, 0, 0);
        } else {
            try {
                this.media.write(mk_int32, mk_int322, this.buffer);
                this.reply.set(0, 0, 0, 0);
            } catch (IOException e) {
                D.println(0, new StringBuffer().append("Exception during write: ").append(e).toString());
                this.reply.set(3, 16, 0, 0);
            }
        }
        this.reply.setflags(this.writeprot);
        this.reply.send(this.out);
        this.out.flush();
    }

    void client_pa_media_removal(byte[] bArr) throws IOException {
        if ((bArr[4] & 1) != 0) {
            this.reply.set(5, 36, 0, 0);
        } else {
            this.reply.set(0, 0, 0, 0);
        }
        this.reply.setflags(this.writeprot);
        this.reply.send(this.out);
        this.out.flush();
    }

    void client_start_stop_unit(byte[] bArr) throws IOException {
        if ((bArr[4] & 2) != 0) {
            this.reply.set(5, 36, 0, 0);
        } else {
            this.reply.set(0, 0, 0, 0);
        }
        this.reply.setflags(this.writeprot);
        this.reply.send(this.out);
        this.out.flush();
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
        this.reply.setflags(this.writeprot);
        this.reply.send(this.out);
        this.out.flush();
    }

    void client_format_unit(byte[] bArr) throws IOException {
        int i;
        byte[] bArr2 = new byte[100];
        int mk_int16 = SCSI.mk_int16(bArr, 7);
        read_complete(bArr2, mk_int16);
        D.print(3, "Format params: ");
        D.hexdump(3, bArr2, mk_int16);
        int i2 = bArr2[1] & 1;
        if (SCSI.mk_int32(bArr2, 4) == 2880 && SCSI.mk_int24(bArr2, 9) == 512) {
            i = 2;
        } else if (SCSI.mk_int32(bArr2, 4) == 1440 && SCSI.mk_int24(bArr2, 9) == 512) {
            i = 5;
        } else {
            i = 0;
        }
        if (this.writeprot) {
            this.reply.set(7, 39, 0, 0);
        } else if (i != 0) {
            int i3 = bArr[2] & 255;
            this.media.format(i, i3, i3, i2, i2);
            D.println(3, "format");
            this.reply.set(0, 0, 0, 0);
        } else {
            this.reply.set(5, 38, 0, 0);
        }
        this.reply.setflags(this.writeprot);
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
        } else if (this.media.type() != 0) {
            if (this.media.type() == 100) {
                long size = (this.media.size() / 512) - 1;
                bArr[0] = (byte) ((size >> 24) & 255);
                bArr[1] = (byte) ((size >> 16) & 255);
                bArr[2] = (byte) ((size >> 8) & 255);
                bArr[3] = (byte) ((size >> 0) & 255);
                bArr[6] = 2;
            } else {
                long size2 = (this.media.size() / this.media.dio.BytesPerSec) - 1;
                bArr[0] = (byte) ((size2 >> 24) & 255);
                bArr[1] = (byte) ((size2 >> 16) & 255);
                bArr[2] = (byte) ((size2 >> 8) & 255);
                bArr[3] = (byte) ((size2 >> 0) & 255);
                bArr[6] = (byte) ((this.media.dio.BytesPerSec >> 8) & telnet.TELNET_IAC);
                bArr[7] = (byte) (this.media.dio.BytesPerSec & telnet.TELNET_IAC);
            }
        }
        this.reply.setflags(this.writeprot);
        this.reply.send(this.out);
        if (this.fdd_state == 2) {
            this.out.write(bArr, 0, bArr.length);
        }
        this.out.flush();
        D.print(3, "FDIO.client_read_capacity: ");
        D.hexdump(3, bArr, 8);
    }
}
