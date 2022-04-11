package com.hp.ilo2.virtdevs;

import com.hp.ilo2.remcons.telnet;
import java.io.IOException;
import java.io.OutputStream;


public class ReplyHeader {
    public static final int magic = 195936478;
    public static final int WP = 1;
    public static final int KEEPALIVE = 2;
    public static final int DISCONNECT = 4;
    int flags;
    byte sense_key;
    byte asc;
    byte ascq;
    byte media;
    int length;
    byte[] data = new byte[16];

    
    public void set(int i, int i2, int i3, int i4) {
        this.sense_key = (byte) i;
        this.asc = (byte) i2;
        this.ascq = (byte) i3;
        this.length = i4;
    }

    
    public void setmedia(int i) {
        this.media = (byte) i;
    }

    
    public void setflags(boolean z) {
        if (z) {
            this.flags |= 1;
        } else {
            this.flags &= -2;
        }
    }

    
    public void keepalive(boolean z) {
        if (z) {
            this.flags |= 2;
        } else {
            this.flags &= -3;
        }
    }

    
    public void disconnect(boolean z) {
        if (z) {
            this.flags |= 4;
        } else {
            this.flags &= -5;
        }
    }

    
    public void send(OutputStream outputStream) throws IOException {
        this.data[0] = -34;
        this.data[1] = -64;
        this.data[2] = -83;
        this.data[3] = 11;
        this.data[4] = (byte) (this.flags & telnet.TELNET_IAC);
        this.data[5] = (byte) ((this.flags >> 8) & telnet.TELNET_IAC);
        this.data[6] = (byte) ((this.flags >> 16) & telnet.TELNET_IAC);
        this.data[7] = (byte) ((this.flags >> 24) & telnet.TELNET_IAC);
        this.data[8] = this.media;
        this.data[9] = this.sense_key;
        this.data[10] = this.asc;
        this.data[11] = this.ascq;
        this.data[12] = (byte) (this.length & telnet.TELNET_IAC);
        this.data[13] = (byte) ((this.length >> 8) & telnet.TELNET_IAC);
        this.data[14] = (byte) ((this.length >> 16) & telnet.TELNET_IAC);
        this.data[15] = (byte) ((this.length >> 24) & telnet.TELNET_IAC);
        outputStream.write(this.data, 0, 16);
    }

    
    public void sendsynch(OutputStream outputStream, byte[] bArr) throws IOException {
        this.data[0] = -34;
        this.data[1] = -64;
        this.data[2] = -83;
        this.data[3] = 11;
        this.data[4] = (byte) (this.flags & telnet.TELNET_IAC);
        this.data[5] = (byte) ((this.flags >> 8) & telnet.TELNET_IAC);
        this.data[6] = (byte) ((this.flags >> 16) & telnet.TELNET_IAC);
        this.data[7] = (byte) ((this.flags >> 24) & telnet.TELNET_IAC);
        this.data[8] = bArr[4];
        this.data[9] = bArr[5];
        this.data[10] = bArr[6];
        this.data[11] = bArr[7];
        this.data[12] = bArr[8];
        this.data[13] = bArr[9];
        this.data[14] = bArr[10];
        this.data[15] = bArr[11];
        outputStream.write(this.data, 0, 16);
    }
}
