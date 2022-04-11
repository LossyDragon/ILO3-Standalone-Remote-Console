package com.hp.ilo2.virtdevs;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.Socket;


public abstract class SCSI {
    public static final int SCSI_FORMAT_UNIT = 4;
    public static final int SCSI_INQUIRY = 18;
    public static final int SCSI_MODE_SELECT_6 = 21;
    public static final int SCSI_MODE_SELECT = 85;
    public static final int SCSI_MODE_SENSE_6 = 26;
    public static final int SCSI_MODE_SENSE = 90;
    public static final int SCSI_PA_MEDIA_REMOVAL = 30;
    public static final int SCSI_READ_10 = 40;
    public static final int SCSI_READ_12 = 168;
    public static final int SCSI_READ_CAPACITY = 37;
    public static final int SCSI_READ_CAPACITIES = 35;
    public static final int SCSI_REQUEST_SENSE = 3;
    public static final int SCSI_REZERO_UNIT = 1;
    public static final int SCSI_SEEK = 43;
    public static final int SCSI_SEND_DIAGNOSTIC = 29;
    public static final int SCSI_START_STOP_UNIT = 27;
    public static final int SCSI_TEST_UNIT_READY = 0;
    public static final int SCSI_VERIFY = 47;
    public static final int SCSI_WRITE_10 = 42;
    public static final int SCSI_WRITE_12 = 170;
    public static final int SCSI_WRITE_VERIFY = 46;
    public static final int SCSI_READ_CD = 190;
    public static final int SCSI_READ_CD_MSF = 185;
    public static final int SCSI_READ_HEADER = 68;
    public static final int SCSI_READ_SUBCHANNEL = 66;
    public static final int SCSI_READ_TOC = 67;
    public static final int SCSI_STOP_PLAY_SCAN = 78;
    public static final int SCSI_MECHANISM_STATUS = 189;
    public static final int SCSI_GET_EVENT_STATUS = 74;
    String selectedDevice;
    protected InputStream in;
    protected BufferedOutputStream out;
    protected Socket sock;
    int targetIsDevice;
    MediaAccess media = new MediaAccess();
    ReplyHeader reply = new ReplyHeader();
    boolean writeprot = false;
    boolean please_exit = false;
    byte[] buffer = new byte[131072];
    byte[] req = new byte[12];

    public abstract boolean process() throws IOException;

    public void setWriteProt(boolean z) {
        this.writeprot = z;
    }

    public boolean getWriteProt() {
        D.println(3, "media.wp = " + this.media.wp());
        return this.media.wp();
    }

    public SCSI(Socket socket, InputStream inputStream, BufferedOutputStream bufferedOutputStream, String str, int i) {
        this.targetIsDevice = 0;
        this.sock = socket;
        this.in = inputStream;
        this.out = bufferedOutputStream;
        this.selectedDevice = str;
        this.targetIsDevice = i;
    }

    public void close() throws IOException {
        this.media.close();
    }

    public static int mk_int32(byte[] bArr, int i) {
        return ((bArr[i] & 255) << 24) | ((bArr[i + 1] & 255) << 16) | ((bArr[i + 2] & 255) << 8) | (bArr[i + 3] & 255);
    }

    public static int mk_int24(byte[] bArr, int i) {
        return ((bArr[i] & 255) << 16) | ((bArr[i + 1] & 255) << 8) | (bArr[i + 2] & 255);
    }

    public static int mk_int16(byte[] bArr, int i) {
        return ((bArr[i] & 255) << 8) | (bArr[i + 1] & 255);
    }

    
    public int read_complete(byte[] bArr, int i) throws IOException {
        int read = 0;
        int i2 = 0;
        while (i > 0) {
            try {
                this.sock.setSoTimeout(1000);
                read = this.in.read(bArr, i2, i);
            } catch (InterruptedIOException e) {
            }
            if (read < 0) {
                break;
            }
            i -= read;
            i2 += read;
        }
        return i2;
    }

    
    public int read_command(byte[] bArr, int i) throws IOException {
        int i2 = 0;
        while (true) {
            try {
                this.sock.setSoTimeout(1000);
                i2 = this.in.read(bArr, 0, i);
            } catch (InterruptedIOException e) {
                this.reply.keepalive(true);
                D.println(3, "Sending keepalive");
                this.reply.send(this.out);
                this.out.flush();
                this.reply.keepalive(false);
                if (this.please_exit) {
                    break;
                }
            }
            if ((bArr[0] & 255) != 254) {
                break;
            }
            this.reply.sendsynch(this.out, bArr);
            this.out.flush();
        }
        if (this.please_exit) {
            throw new IOException("Asked to exit");
        } else if (i2 >= 0) {
            return i2;
        } else {
            throw new IOException("Socket Closed");
        }
    }

    public void send_disconnect() {
        try {
            this.reply.disconnect(true);
            this.reply.send(this.out);
            this.out.flush();
            this.reply.disconnect(false);
        } catch (Exception e) {
            D.println(1, "Exception in send_disconnect" + e);
            e.printStackTrace();
        }
    }

    public void change_disk() {
        this.please_exit = true;
    }
}
