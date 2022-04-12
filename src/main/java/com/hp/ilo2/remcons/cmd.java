package com.hp.ilo2.remcons;

import com.hp.ilo2.intgapp.locinfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;


public class cmd implements Runnable {

    protected DataInputStream in;
    protected DataOutputStream out;
    protected Socket s;
    protected Thread receiver;
    protected int connected = 0;
    remcons cmdHandler;

    public synchronized void transmit(String str) {
        System.out.println("in cmd::transmit");

        if (out != null && str.length() != 0) {
            byte[] bArr = new byte[str.length()];

            for (int i = 0; i < str.length(); i++) {
                bArr[i] = (byte) str.charAt(i);
            }

            try {
                out.write(bArr, 0, bArr.length);
            } catch (IOException e) {
                System.out.println("telnet.transmit() IOException: " + e);
            }
        }
    }

    public synchronized void transmitb(byte[] bArr, int i) {
        try {
            out.write(bArr, 0, i);
        } catch (IOException e) {
            System.out.println("cmd.transmitb() IOException: " + e);
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

    @Override
    public void run() {
        byte[] byArray = new byte[12];
        byte[] byArray2 = new byte[1];
        byte[] byArray3 = new byte[4];
        byte[] byArray4 = new byte[128];
        short s;

        try {
            while (true) {
                int n2 = 0;
                int n3;

                while (n2 < 12) {
                    n3 = in.read(byArray2, 0, 1);
                    if (n3 != 1) continue;
                    byArray[n2++] = byArray2[0];
                }

                byte by = byArray[0];
                byte by2 = byArray[4];

                s = byArray[10];
                switch (by) {
                    case 2: {
                        System.out.println("Received Post complete notification\n");
                        cmdHandler.session.post_complete = true;
                        cmdHandler.session.set_status(4, "");
                        break;
                    }
                    case 3: {
                        if (by2 != 1) {
                            System.out.println("Invalid size for cmd: " + by + " size:" + by2);
                        }
                        in.read(byArray2, 0, 1);
                        cmdHandler.setPwrStatusPower(byArray2[0]);
                        break;
                    }
                    case 4: {
                        if (by2 != 1) {
                            System.out.println("Invalid size for cmd: " + by + " size:" + by2);
                        }
                        in.read(byArray2, 0, 1);
                        cmdHandler.setPwrStatusHealth(byArray2[0]);
                        break;
                    }
                    case 5: {
                        if (cmdHandler.session.post_complete) break;
                        StringBuilder charSequence = new StringBuilder(16);
                        n3 = in.read(byArray4, 0, 2);
                        String string4 = Integer.toHexString(0xFF & byArray4[1]).toUpperCase();
                        String string2 = Integer.toHexString(0xFF & byArray4[0]).toUpperCase();
                        String string3 = charSequence.append(locinfo.STATUSSTR_3126).append(string4).append(string2).toString();
                        cmdHandler.session.set_status(4, string3);
                        break;
                    }
                    case 6: {
                        String string4;
                        System.out.println("Seized command notification\n");
                        n3 = in.read(byArray4, 0, 128);
                        String charSequence = "UNKNOWN";
                        String string3 = "UNKNOWN";
                        System.out.println("Data rcvd for acquire " + Arrays.toString(byArray4) + "rd count " + n3);

                        if (n3 > 0) {
                            string4 = new String(byArray4);
                            System.out.println("Pakcet " + string4);
                            charSequence = string4.substring(0, 63).trim();
                            string3 = string4.substring(64, 127).trim();
                            if (charSequence.length() <= 0) {
                                charSequence = "UNKNOWN";
                            }
                            if (string3.length() <= 0) {
                                string3 = "UNKNOWN";
                            }
                        } else {
                            System.out.println("Invalid acquire info");
                        }

                        if (cmdHandler.seize_dialog(charSequence, string3, s) == 0) {
                            sendBool(true);
                            cmdHandler.seize_confirmed();
                            break;
                        }

                        sendBool(false);
                        break;
                    }
                    case 7: {
                        in.read(byArray3, 0, 4);
                        cmdHandler.ack(byArray3[0], byArray3[1], byArray3[2], byArray3[3]);
                        break;
                    }
                    case 8: {
                        System.out.println("Playback not supported now.\n");
                        break;
                    }
                    case 9: {
                        String string;
                        System.out.println("Share command notification\n");
                        n3 = in.read(byArray4, 0, 128);
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

                        sendBool(false);
                        cmdHandler.shared(string2, string4);
                        break;
                    }
                    case 10: {
                        System.out.println("Firmware upgrade in progress notification\n");
                        cmdHandler.firmwareUpgrade();
                        break;
                    }
                    case 11: {
                        System.out.println("Un authorized action performed\n");
                        String string;
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
                        cmdHandler.unAuthorized(string);
                        break;
                    }
                    case 13: {
                        in.read(byArray2, 0, 1);
                        System.out.println("VM notification from firmware\n");
                        break;
                    }
                    case 14: {
                        System.out.println("Unlicensed notification from firmware\n");
                        cmdHandler.UnlicensedShutdown();
                        break;
                    }
                    case 15: {
                        System.out.println("Reset notification from firmware\n");
                        cmdHandler.resetShutdown();
                        break;
                    }
                }

                by = 0;
            }
        }
        catch (Exception exception) {
            System.out.println("CMD exception: " + exception);
        }
    }

    public void connectCmd(remcons remconsVar, String str, int i) {
        try {
            cmdHandler = remconsVar;
            byte[] bArr2 = new byte[2];
            s = new Socket(str, i);

            try {
                s.setSoLinger(true, 0);
            } catch (SocketException e) {
                System.out.println("connectCmd linger SocketException: " + e);
            }

            in = new DataInputStream(s.getInputStream());
            out = new DataOutputStream(s.getOutputStream());

            if (in.readByte() == 80) {
                bArr2[0] = 2;
                bArr2[1] = 32;
                byte[] bytes = remconsVar.ParentApp.getParameter("RCINFO1").getBytes();

                if (remconsVar.ParentApp.optional_features.contains("ENCRYPT_KEY")) {
                    for (int i2 = 0; i2 < bytes.length; i2++) {
                        bytes[i2] = (byte) (bytes[i2] ^ ((byte) remconsVar.ParentApp.enc_key.charAt(i2 % remconsVar.ParentApp.enc_key.length())));
                    }

                    if (remconsVar.ParentApp.optional_features.contains("ENCRYPT_VMKEY")) {
                        bArr2[1] = (byte) (bArr2[1] | 64);
                    } else {
                        bArr2[1] = (byte) (bArr2[1] | 128);
                    }
                }

                byte[] bArr3 = new byte[bArr2.length + bytes.length];
                System.arraycopy(bArr2, 0, bArr3, 0, bArr2.length);
                System.arraycopy(bytes, 0, bArr3, bArr2.length, bytes.length);
                transmit(new String(bArr3));

                byte readByte = in.readByte();
                if (readByte == 82) {
                    receiver = new Thread(this);
                    receiver.setName("cmd_rcvr");
                    receiver.start();
                } else {
                    System.out.println("login failed. read data" + readByte);
                }
            } else {
                System.out.println("Socket connection failure... ");
            }

        } catch (SocketException e2) {
            System.out.println("telnet.connect() SocketException: " + e2);

            s = null;
            in = null;
            out = null;
            receiver = null;
            connected = 0;

        } catch (UnknownHostException e3) {
            System.out.println("telnet.connect() UnknownHostException: " + e3);

            s = null;
            in = null;
            out = null;
            receiver = null;
            connected = 0;

        } catch (IOException e4) {
            System.out.println("telnet.connect() IOException: " + e4);

            s = null;
            in = null;
            out = null;
            receiver = null;
            connected = 0;

        }
    }

    public void disconnectCmd() {
        if (receiver != null && receiver.isAlive()) {
            receiver.stop();
        }

        receiver = null;

        if (s != null) {
            try {
                System.out.println("Closing socket");
                s.close();
            } catch (IOException e) {
                System.out.println("telnet.disconnect() IOException: " + e);
            }
        }

        if (cmdHandler != null) {
            cmdHandler.setPwrStatusHealth(3);
            cmdHandler.setPwrStatusPower(0);
            cmdHandler = null;
        }

        s = null;
        in = null;
        out = null;
    }
}
