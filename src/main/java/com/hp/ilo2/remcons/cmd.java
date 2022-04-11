package com.hp.ilo2.remcons;

import com.hp.ilo2.intgapp.locinfo;
import com.hp.ilo2.virtdevs.MediaAccess;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;


public class cmd implements Runnable {
    protected Thread receiver;
    protected Socket s;
    protected DataInputStream in;
    protected DataOutputStream out;
    public static final int TELNET_PORT = 23;
    remcons cmdHandler;
    protected String login = "";
    protected String host = "";
    protected int port = 23;
    protected int connected = 0;

    public synchronized void transmit(String str) {
        System.out.println("in cmd::transmit");
        if (this.out != null && str.length() != 0) {
            byte[] bArr = new byte[str.length()];
            for (int i = 0; i < str.length(); i++) {
                bArr[i] = (byte) str.charAt(i);
            }
            try {
                this.out.write(bArr, 0, bArr.length);
            } catch (IOException e) {
                System.out.println(new StringBuffer().append("telnet.transmit() IOException: ").append(e).toString());
            }
        }
    }

    public synchronized void transmitb(byte[] bArr, int i) {
        try {
            this.out.write(bArr, 0, i);
        } catch (IOException e) {
            System.out.println(new StringBuffer().append("cmd.transmitb() IOException: ").append(e).toString());
        }
    }

    public void sendBool(boolean z) {
        byte[] bArr = new byte[4];
        if (z) {
            bArr[0] = 4;
        } else {
            bArr[0] = 3;
        }
        bArr[1] = 0;
        bArr[2] = 0;
        bArr[3] = 0;
        transmitb(bArr, bArr.length);
    }

    @Override // java.lang.Runnable
    public void run() {
        byte[] byArray = new byte[12];
        byte[] byArray2 = new byte[1];
        byte[] byArray3 = new byte[4];
        byte[] byArray4 = new byte[128];
        int n = 0;
        short s = 0;
        short s2 = 0;
        try {
            while (true) {
                int n2 = 0;
                int n3 = 0;
                while (n2 < 12) {
                    n3 = this.in.read(byArray2, 0, 1);
                    if (n3 != 1) continue;
                    byArray[n2++] = byArray2[0];
                }
                byte by = byArray[0];
                byte by2 = byArray[4];
                s2 = byArray[8];
                s = byArray[10];
                switch (by) {
                    case 2: {
                        System.out.println("Received Post complete notification\n");
                        this.cmdHandler.session.post_complete = true;
                        this.cmdHandler.session.set_status(4, "");
                        break;
                    }
                    case 3: {
                        if (by2 != 1) {
                            System.out.println("Invalid size for cmd: " + by + " size:" + by2);
                        }
                        this.in.read(byArray2, 0, 1);
                        this.cmdHandler.setPwrStatusPower(byArray2[0]);
                        break;
                    }
                    case 4: {
                        if (by2 != 1) {
                            System.out.println("Invalid size for cmd: " + by + " size:" + by2);
                        }
                        this.in.read(byArray2, 0, 1);
                        this.cmdHandler.setPwrStatusHealth(byArray2[0]);
                        break;
                    }
                    case 5: {
                        if (this.cmdHandler.session.post_complete) break;
                        CharSequence charSequence = new StringBuffer(16);
                        n3 = this.in.read(byArray4, 0, 2);
                        String string4 = Integer.toHexString(0xFF & byArray4[1]).toUpperCase();
                        String string2 = Integer.toHexString(0xFF & byArray4[0]).toUpperCase();
                        String string3 = ((StringBuffer)charSequence).append(this.cmdHandler.getLocalString(12582)).append(string4).append(string2).toString();
                        this.cmdHandler.session.set_status(4, string3);
                        break;
                    }
                    case 6: {
                        String string4;
                        System.out.println("Seized command notification\n");
                        n3 = this.in.read(byArray4, 0, 128);
                        CharSequence charSequence = "UNKNOWN";
                        String string3 = "UNKNOWN";
                        System.out.println("Data rcvd for acquire " + byArray4 + "rd count " + n3);
                        if (n3 > 0) {
                            string4 = new String(byArray4);
                            System.out.println("Pakcet " + string4);
                            charSequence = string4.substring(0, 63).trim();
                            string3 = string4.substring(64, 127).trim();
                            if (((String)charSequence).length() <= 0) {
                                charSequence = "UNKNOWN";
                            }
                            if (string3.length() <= 0) {
                                string3 = "UNKNOWN";
                            }
                        } else {
                            System.out.println("Invalid acquire info");
                        }
                        if ((n = this.cmdHandler.seize_dialog((String)charSequence, string3, s)) == 0) {
                            this.sendBool(true);
                            this.cmdHandler.seize_confirmed();
                            break;
                        }
                        this.sendBool(false);
                        break;
                    }
                    case 7: {
                        this.in.read(byArray3, 0, 4);
                        this.cmdHandler.ack(byArray3[0], byArray3[1], byArray3[2], byArray3[3]);
                        break;
                    }
                    case 8: {
                        System.out.println("Playback not supported now.\n");
                        break;
                    }
                    case 9: {
                        String string;
                        System.out.println("Share command notification\n");
                        n3 = this.in.read(byArray4, 0, 128);
                        String string4 = "UNKNOWN";
                        String string2 = "UNKNOWN";
                        if (n3 > 0) {
                            string = new String(byArray4);
                            System.out.println("Pakcet " + string);
                            string4 = string.substring(0, 63).trim();
                            string2 = string.substring(64, 127).trim();
                            if (string4.length() <= 0) {
                                string4 = "UNKNOWN";
                            }
                            if (string2.length() <= 0) {
                                string2 = "UNKNOWN";
                            }
                        } else {
                            System.out.println("Invalid acquire info");
                        }
                        this.sendBool(false);
                        this.cmdHandler.shared(string2, string4);
                        break;
                    }
                    case 10: {
                        System.out.println("Firmware upgrade in progress notification\n");
                        this.cmdHandler.firmwareUpgrade();
                        break;
                    }
                    case 11: {
                        System.out.println("Un authorized action performed\n");
                        String string = "";
                        switch (s) {
                            case 2: {
                                string = " for remote console";
                                break;
                            }
                            case 3: {
                                string = " for virtual media";
                                break;
                            }
                            case 4: {
                                string = " for virtual power switch operations";
                                break;
                            }
                            default: {
                                string = "{0x" + s + "}";
                            }
                        }
                        this.cmdHandler.unAuthorized(string);
                        break;
                    }
                    case 13: {
                        this.in.read(byArray2, 0, 1);
                        System.out.println("VM notification from firmware\n");
                        break;
                    }
                    case 14: {
                        System.out.println("Unlicensed notification from firmware\n");
                        this.cmdHandler.UnlicensedShutdown();
                        break;
                    }
                    case 15: {
                        System.out.println("Reset notification from firmware\n");
                        this.cmdHandler.resetShutdown();
                        break;
                    }
                }
                by = 0;
            }
        }
        catch (Exception exception) {
            System.out.println("CMD exception: " + exception.toString());
            return;
        }
    }

    public boolean connectCmd(remcons remconsVar, String str, int i) {
        try {
            this.cmdHandler = remconsVar;
            byte[] bArr = new byte[32];
            byte[] bArr2 = new byte[2];
            this.s = new Socket(str, i);
            try {
                this.s.setSoLinger(true, 0);
            } catch (SocketException e) {
                System.out.println(new StringBuffer().append("connectCmd linger SocketException: ").append(e).toString());
            }
            this.in = new DataInputStream(this.s.getInputStream());
            this.out = new DataOutputStream(this.s.getOutputStream());
            if (this.in.readByte() == 80) {
                bArr2[0] = 2;
                bArr2[1] = 32;
                byte[] bytes = remconsVar.ParentApp.getParameter("RCINFO1").getBytes();
                if (remconsVar.ParentApp.optional_features.indexOf("ENCRYPT_KEY") != -1) {
                    for (int i2 = 0; i2 < bytes.length; i2++) {
                        bytes[i2] = (byte) (bytes[i2] ^ ((byte) remconsVar.ParentApp.enc_key.charAt(i2 % remconsVar.ParentApp.enc_key.length())));
                    }
                    if (remconsVar.ParentApp.optional_features.indexOf("ENCRYPT_VMKEY") != -1) {
                        bArr2[1] = (byte) (bArr2[1] | 64);
                    } else {
                        bArr2[1] = (byte) (bArr2[1] | 128);
                    }
                }
                byte[] bArr3 = new byte[bArr2.length + bytes.length];
                System.arraycopy(bArr2, 0, bArr3, 0, bArr2.length);
                System.arraycopy(bytes, 0, bArr3, bArr2.length, bytes.length);
                transmit(new String(bArr3));
                byte readByte = this.in.readByte();
                if (readByte == 82) {
                    this.receiver = new Thread(this);
                    this.receiver.setName("cmd_rcvr");
                    this.receiver.start();
                } else {
                    System.out.println(new StringBuffer().append("login failed. read data").append((int) readByte).toString());
                }
            } else {
                System.out.println("Socket connection failure... ");
            }
            return true;
        } catch (SocketException e2) {
            System.out.println(new StringBuffer().append("telnet.connect() SocketException: ").append(e2).toString());
            this.s = null;
            this.in = null;
            this.out = null;
            this.receiver = null;
            this.connected = 0;
            return true;
        } catch (UnknownHostException e3) {
            System.out.println(new StringBuffer().append("telnet.connect() UnknownHostException: ").append(e3).toString());
            this.s = null;
            this.in = null;
            this.out = null;
            this.receiver = null;
            this.connected = 0;
            return true;
        } catch (IOException e4) {
            System.out.println(new StringBuffer().append("telnet.connect() IOException: ").append(e4).toString());
            this.s = null;
            this.in = null;
            this.out = null;
            this.receiver = null;
            this.connected = 0;
            return true;
        }
    }

    public void disconnectCmd() {
        if (this.receiver != null && this.receiver.isAlive()) {
            this.receiver.stop();
        }
        this.receiver = null;
        if (this.s != null) {
            try {
                System.out.println("Closing socket");
                this.s.close();
            } catch (IOException e) {
                System.out.println(new StringBuffer().append("telnet.disconnect() IOException: ").append(e).toString());
            }
        }
        if (this.cmdHandler != null) {
            this.cmdHandler.setPwrStatusHealth(3);
            this.cmdHandler.setPwrStatusPower(0);
            this.cmdHandler = null;
        }
        this.s = null;
        this.in = null;
        this.out = null;
    }
}
