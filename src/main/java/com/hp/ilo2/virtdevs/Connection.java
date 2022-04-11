package com.hp.ilo2.virtdevs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.Timer;


public class Connection implements Runnable, ActionListener {
    public static final int FLOPPY = 1;
    public static final int CDROM = 2;
    public static final int USBKEY = 3;
    Socket s;
    InputStream in;
    BufferedOutputStream out;
    String host;
    int port;
    int device;
    String target;
    int targetIsDevice;
    SCSI scsi;
    boolean writeprot = false;
    virtdevs v;
    byte[] pre;
    byte[] key;
    boolean changing_disks;
    VMD5 digest;

    public Connection(String str, int i, int i2, String str2, int i3, byte[] bArr, byte[] bArr2, virtdevs virtdevsVar) throws IOException {
        this.host = str;
        this.port = i;
        this.device = i2;
        this.target = str2;
        this.pre = bArr;
        this.key = bArr2;
        this.v = virtdevsVar;
        MediaAccess mediaAccess = new MediaAccess();
        int devtype = mediaAccess.devtype(str2);
        if (devtype == 2 || devtype == 5) {
            this.targetIsDevice = 1;
            D.println(0, "Got CD or removable connection\n");
        } else {
            this.targetIsDevice = 0;
            D.println(0, "Got NO CD or removable connection\n");
        }
        mediaAccess.open(str2, this.targetIsDevice);
        long size = mediaAccess.size();
        mediaAccess.close();
        if (this.device == 1 && size > 2949120) {
            this.device = 3;
        }
        this.digest = new VMD5();
    }

    public int connect() throws UnknownHostException, IOException {
        byte[] bArr = {16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        this.s = new Socket(this.host, this.port);
        this.s.setTcpNoDelay(true);
        this.in = this.s.getInputStream();
        this.out = new BufferedOutputStream(this.s.getOutputStream());
        this.digest.reset();
        this.digest.update(this.pre);
        this.digest.update(this.key);
        System.arraycopy(this.key, 0, bArr, 2, this.key.length);
        bArr[1] = (byte) this.device;
        if (this.targetIsDevice == 0) {
            bArr[1] = (byte) (bArr[1] | Byte.MIN_VALUE);
        }
        this.out.write(bArr);
        this.out.flush();
        this.in.read(bArr, 0, 4);
        D.println(3, new StringBuffer().append("Hello response0: ").append(D.hex(bArr[0], 2)).toString());
        D.println(3, new StringBuffer().append("Hello response1: ").append(D.hex(bArr[1], 2)).toString());
        if (bArr[0] == 32 && bArr[1] == 0) {
            D.println(1, new StringBuffer().append("Connected.  Protocol version = ").append(bArr[3] & 255).append(".").append(bArr[2] & 255).toString());
            return 0;
        }
        D.println(0, "Unexpected Hello Response!");
        this.s.close();
        this.s = null;
        this.in = null;
        this.out = null;
        return bArr[0];
    }

    public void close() throws IOException {
        if (this.scsi != null) {
            try {
                this.scsi.send_disconnect();
                Timer timer = new Timer(2000, this);
                timer.setRepeats(false);
                timer.start();
                this.scsi.change_disk();
                timer.stop();
            } catch (Exception e) {
                this.scsi.change_disk();
            }
        } else {
            internal_close();
        }
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {
            internal_close();
        } catch (Exception e) {
        }
    }

    public void internal_close() throws IOException {
        if (this.s != null) {
            this.s.close();
        }
        this.s = null;
        this.in = null;
        this.out = null;
    }

    public void setWriteProt(boolean z) {
        this.writeprot = z;
        if (this.scsi != null) {
            this.scsi.setWriteProt(this.writeprot);
        }
    }

    public void change_disk(String str) throws IOException {
        int i;
        MediaAccess mediaAccess = new MediaAccess();
        int devtype = mediaAccess.devtype(str);
        if (devtype == 2 || devtype == 5) {
            i = 1;
        } else {
            i = 0;
        }
        if (i == 0) {
            mediaAccess.open(str, 0);
            mediaAccess.close();
        }
        this.target = str;
        this.targetIsDevice = i;
        this.changing_disks = true;
        this.scsi.change_disk();
    }

    @Override
    public void run() {
        System.out.println("Message before invoking  connection run method");
        do {
            block14: {
                this.changing_disks = false;
                try {
                    if (this.device == 1 || this.device == 3) {
                        this.scsi = new SCSIFloppy(this.s, this.in, this.out, this.target, this.targetIsDevice, this.v);
                        break block14;
                    }
                    if (this.device == 2) {
                        this.scsi = this.targetIsDevice == 1 ? new SCSIcdrom(this.s, this.in, this.out, this.target, 1, this.v) : new SCSIcdimage(this.s, this.in, this.out, this.target, 0, this.v);
                        break block14;
                    }
                    D.println(0, "Unsupported virtual device " + this.device);
                    return;
                }
                catch (Exception exception) {
                    D.println(0, "Exception while opening " + this.target + "(" + exception + ")");
                }
            }
            this.scsi.setWriteProt(this.writeprot);
            try {
                while (this.scsi.process()) {
                }
                System.out.println("Connection can not be stablished");
            }
            catch (IOException iOException) {
                D.println(1, "Exception in Connection::run() " + iOException);
                iOException.printStackTrace();
            }
            D.println(3, "Closing scsi and socket");
            try {
                this.scsi.close();
                if (!this.changing_disks) {
                    this.internal_close();
                }
            }
            catch (IOException iOException) {
                D.println(0, "Exception closing connection " + iOException);
            }
            this.scsi = null;
        } while (this.changing_disks);
        if (this.device == 1 || this.device == 3) {
            System.out.println("Message before invoking fdDisconnect");
            this.v.fdDisconnect();
        } else if (this.device == 2) {
            System.out.println("Message before invoking cdDisconnect");
            this.v.cdDisconnect();
        }
    }
}
