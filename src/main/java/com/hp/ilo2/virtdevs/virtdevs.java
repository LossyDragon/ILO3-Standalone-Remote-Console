package com.hp.ilo2.virtdevs;

import com.hp.ilo2.intgapp.intgapp;
import com.hp.ilo2.intgapp.locinfo;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.URL;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class virtdevs extends JPanel implements Runnable {

    JFrame parent;
    String base;
    String configuration;
    String dev_auto;
    String dev_cdrom;
    String dev_floppy;
    String host;
    String hostAddress;
    boolean force_config = false;
    boolean thread_init = false;
    byte[] key = new byte[32];
    byte[] pre = new byte[16];
    int fdport = 17988;
    protected boolean running = false;
    protected boolean stopFlag = false;
    public Connection cdConnection;
    public Connection fdConnection;
    public Thread cdThread;
    public Thread fdThread;
    public boolean cdConnected = false;
    public boolean fdConnected = false;
    public int dev_cd_device = 0;
    public int dev_fd_device = 0;
    public int unq_feature = 0;
    public intgapp ParentApp;
    public static Properties prop;
    public static boolean cd_support = true;
    public static final int UNQF_HIDEFLP = 1;
    public static int UID;
    static Class class$java$io$FileDescriptor;
    static Class class$java$net$Socket;
    static Class class$java$net$SocketImpl;
    static final int ImageDone = 39;

    public virtdevs(intgapp intgappVar) {
        this.ParentApp = intgappVar;
    }

    public Image get(String str) {
        return this.ParentApp.getImage(getClass().getClassLoader().getResource("com/hp/ilo2/virtdevs/" + str));
    }

    public void init() {
        if (UID == 0) {
            UID = hashCode();
        }
        URL documentBase = this.ParentApp.getDocumentBase();
        this.host = this.ParentApp.getParameter("hostAddress");
        if (this.host == null) {
            this.host = documentBase.getHost();
        }
        this.base = documentBase.getProtocol() + "://" + documentBase.getHost();
        if (documentBase.getPort() != -1) {
            this.base = this.base + ":" + documentBase.getPort();
        }
        this.base = this.base + "/";
        String parameter = this.ParentApp.getParameter("INFO0");
        if (parameter != null) {
            for (int i = 0; i < 16; i++) {
                try {
                    this.pre[i] = (byte) Integer.parseInt(parameter.substring(2 * i, (2 * i) + 2), 16);
                } catch (NumberFormatException e) {
                    D.println(0, "Couldn't parse INFO0: " + e);
                }
            }
        }
        try {
            if (null != this.ParentApp.vm_port) {
                this.fdport = Integer.parseInt(this.ParentApp.vm_port);
            }
        } catch (NumberFormatException e2) {
            D.println(0, "Couldn't parse INFO1: " + e2);
        }
        this.configuration = this.ParentApp.getParameter("INFO2");
        if (this.configuration == null) {
            this.configuration = "auto";
        }
        this.dev_floppy = this.ParentApp.getParameter("floppy");
        this.dev_cdrom = this.ParentApp.getParameter("cdrom");
        this.dev_auto = this.ParentApp.getParameter("device");
        String parameter2 = this.ParentApp.getParameter("config");
        if (parameter2 != null) {
            this.configuration = parameter2;
            this.force_config = true;
        }
        String parameter3 = this.ParentApp.getParameter("UNIQUE_FEATURES");
        if (parameter3 != null) {
            try {
                this.unq_feature = Integer.parseInt(parameter3);
            } catch (NumberFormatException e3) {
                D.println(0, "Couldn't parse UNIQUE_FEATURES: " + e3);
            }
        }
        this.key = this.ParentApp.getParameter("RCINFO1").getBytes();
        if (this.ParentApp.optional_features.contains("ENCRYPT_VMKEY")) {
            for (int i2 = 0; i2 < this.key.length; i2++) {
                byte[] bArr = this.key;
                bArr[i2] = (byte) (bArr[i2] ^ ((byte) this.ParentApp.enc_key.charAt(i2 % this.ParentApp.enc_key.length())));
            }
        }
        this.parent = this.ParentApp.dispFrame;
    }

    public void start() {
        new Thread(this).start();
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            System.out.println("Exception: " + e);
        }
        this.hostAddress = this.host;
        if (ui_init(this.base)) {
            if (this.force_config) {
                updateconfig();
            }
            show();
            if (this.dev_floppy != null) {
                do_floppy(this.dev_floppy);
            }
            if (this.dev_cdrom != null) {
                do_cdrom(this.dev_cdrom);
            }
        }
    }

    public void stop() {
        D.println(3, "Stop " + this);
        if (this.fdConnection != null) {
            try {
                this.fdConnection.close();
                this.fdThread = null;
            } catch (IOException e) {
                D.println(3, e.toString());
            }
        }
        if (this.cdConnection != null) {
            try {
                this.cdConnection.close();
                this.cdThread = null;
            } catch (IOException e2) {
                D.println(3, e2.toString());
            }
        }
    }

    public void destroy() {
        new Thread(this).start();
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            System.out.println("Exception: " + e);
        }
    }

    @Override
    public synchronized void run() {
        if (!this.thread_init) {
            prop = new Properties();
            try {
                prop.load(new FileInputStream(System.getProperty("user.home") + System.getProperty("file.separator") + ".java" + System.getProperty("file.separator") + "hp.properties"));
            } catch (Exception e) {
                System.out.println("Exception: " + e);
            }
            cd_support = Boolean.parseBoolean(prop.getProperty("com.hp.ilo2.virtdevs.cdimage", "true"));
            new MediaAccess().setup_DirectIO();
            this.thread_init = true;
            this.ParentApp.updateVdMenu();
            return;
        }
        MediaAccess.cleanup(this);
        this.thread_init = false;
    }

    public boolean ui_init(String string) {
        MouseAdapter mouseAdapter = new MouseAdapter(){

            public void mouseClicked(MouseEvent mouseEvent) {
                if ((mouseEvent.getModifiers() & 2) != 0) {
                    System.out.println("Debug set to " + ++D.debug);
                }
                if ((mouseEvent.getModifiers() & 8) != 0) {
                    System.out.println("Debug set to " + --D.debug);
                }
            }
        };
        this.addMouseListener(mouseAdapter);
        return true;
    }

    public void add(Component component, GridBagConstraints gridBagConstraints, int i, int i2, int i3, int i4) {
        gridBagConstraints.gridx = i;
        gridBagConstraints.gridy = i2;
        gridBagConstraints.gridwidth = i3;
        gridBagConstraints.gridheight = i4;
        add(component, gridBagConstraints);
    }

    public void createImage() {
        new CreateImage(this);
    }

    public boolean do_floppy(String str) {
        String str2;
        if (!this.fdConnected) {
            try {
                this.fdConnection = new Connection(this.hostAddress, this.fdport, 1, str, 0, this.pre, this.key, this);
                System.out.println("Starting fd non-Read-Only");
                this.fdConnection.setWriteProt(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try {
                    int connect = this.fdConnection.connect();
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    switch (connect) {
                        case 0:
                            this.fdThread = new Thread(this.fdConnection, "fdConnection");
                            this.fdThread.start();
                            this.fdConnected = true;
                            return true;
                        case 33:
                            this.ParentApp.lockFdMenu(true, "");
                            new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, locinfo.DIALOGSTR_2006);
                            return false;
                        case 34:
                            if (rekey("/html/java_irc.html")) {
                                str2 = locinfo.DIALOGSTR_2007;
                            } else {
                                str2 = locinfo.DIALOGSTR_2008;
                            }
                            new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, str2);
                            return false;
                        case SCSI.SCSI_READ_CAPACITIES :
                            this.ParentApp.lockFdMenu(true, "");
                            new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, locinfo.DIALOGSTR_2009);
                            return false;
                        case SCSI.SCSI_READ_CAPACITY :
                            this.ParentApp.lockFdMenu(true, "");
                            new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, locinfo.DIALOGSTR_200a);
                            return false;
                        case 38:
                            this.ParentApp.lockFdMenu(true, "");
                            new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, locinfo.DIALOGSTR_200b);
                            return false;
                        default:
                            this.ParentApp.lockFdMenu(true, "");
                            new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, locinfo.DIALOGSTR_200c + "(" + Integer.toHexString(connect) + ")." + locinfo.DIALOGSTR_200d);
                            return false;
                    }
                } catch (Exception e) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    D.println(0, "Couldn't connect!\n");
                    new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, locinfo.DIALOGSTR_2005 + "(" + e + ")");
                    return false;
                }
            } catch (Exception e2) {
                new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, e2.getMessage());
                return false;
            }
        } else {
            try {
                this.fdConnection.close();
                return true;
            } catch (Exception e3) {
                D.println(0, "Exception during close: " + e3);
                return true;
            }
        }
    }

    public boolean do_cdrom(String str) {
        String str2;
        if (!this.cdConnected) {
            try {
                this.cdConnection = new Connection(this.hostAddress, this.fdport, 2, str, 0, this.pre, this.key, this);
                this.cdConnection.setWriteProt(true);
                try {
                    int connect = this.cdConnection.connect();
                    switch (connect) {
                        case 0:
                            this.cdThread = new Thread(this.cdConnection, "cdConnection");
                            this.cdThread.start();
                            this.cdConnected = true;
                            return true;
                        case 33:
                            this.ParentApp.lockCdMenu(true, "");
                            new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, locinfo.DIALOGSTR_2006);
                            return false;
                        case 34:
                            if (rekey("/html/java_irc.html")) {
                                str2 = locinfo.DIALOGSTR_2007;
                            } else {
                                str2 = locinfo.DIALOGSTR_2008;
                            }
                            new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, str2);
                            return false;
                        case SCSI.SCSI_READ_CAPACITIES :
                            this.ParentApp.lockCdMenu(true, "");
                            new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, locinfo.DIALOGSTR_2009);
                            return false;
                        case SCSI.SCSI_READ_CAPACITY :
                            this.ParentApp.lockCdMenu(true, "");
                            new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, locinfo.DIALOGSTR_200f);
                            return false;
                        case 38:
                            this.ParentApp.lockCdMenu(true, "");
                            new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, locinfo.DIALOGSTR_200b);
                            return false;
                        default:
                            this.ParentApp.lockCdMenu(true, "");
                            new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, locinfo.DIALOGSTR_200c + " (" + Integer.toHexString(connect) + ")." + locinfo.DIALOGSTR_200d);
                            return false;
                    }
                } catch (Exception e) {
                    D.println(0, "Couldn't connect!\n");
                    new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, locinfo.DIALOGSTR_200e + " (" + e + ")");
                    return false;
                }
            } catch (Exception e2) {
                new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, e2.getMessage());
                return false;
            }
        } else {
            try {
                this.cdConnection.close();
                return true;
            } catch (Exception e3) {
                D.println(0, "Exception during close: " + e3);
                return true;
            }
        }
    }

    public void paint(Graphics graphics) {
        virtdevs.super.paintComponent(graphics);
    }

    public void update(Graphics graphics) {
        paint(graphics);
    }

    void updateconfig() {
        try {
            URL url = new URL(this.base + "modusb.cgi?usb=" + this.configuration);
            url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    bufferedReader.close();
                    return;
                }
                D.println(3, "updcfg: " + readLine);
            }
        } catch (Exception e) {
            new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, locinfo.DIALOGSTR_2010 + "(" + e + ")");
            e.printStackTrace();
        }
    }

    public boolean rekey(String str) {
        String str2 = null;
        try {
            D.println(3, "Downloading new key: " + this.base + str);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(this.base + str).openStream()));
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
                D.println(0, "rekey: " + readLine);
                if (readLine.startsWith("info0=\"")) {
                    str2 = readLine.substring(7, ImageDone);
                    break;
                }
            }
            bufferedReader.close();
            if (str2 == null) {
                new VErrorDialog(this.parent, (locinfo.DIALOGSTR_2014), (locinfo.DIALOGSTR_2011));
                return false;
            }
            for (int i = 0; i < 16; i++) {
                try {
                    this.pre[i] = (byte) Integer.parseInt(str2.substring(2 * i, (2 * i) + 2), 16);
                } catch (NumberFormatException e) {
                    D.println(0, "Couldn't parse new key: " + e);
                    new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, locinfo.DIALOGSTR_2012);
                    return false;
                }
            }
            return true;
        } catch (Exception e2) {
            D.println(0, "rekey: " + e2);
            new VErrorDialog(this.parent,locinfo.DIALOGSTR_2014, (locinfo.DIALOGSTR_2011));
            return false;
        }
    }

    public void change_disk(Connection connection, String str) {
        try {
            connection.change_disk(str);
        } catch (IOException e) {
            new VErrorDialog(this.parent, locinfo.DIALOGSTR_2014, locinfo.DIALOGSTR_2013 + " (" + e + ")");
        }
    }

    public void fdDisconnect() {
        this.fdThread = null;
        repaint();
        this.fdConnected = false;
        this.ParentApp.lockFdMenu(true, "");
        this.ParentApp.remconsObj.setvmAct(0);
    }

    public void cdDisconnect() {
        this.cdThread = null;
        repaint();
        this.cdConnected = false;
        this.ParentApp.lockCdMenu(true, "");
        this.ParentApp.remconsObj.setvmAct(0);
    }

    public static int getSockFd(Socket socket) {
        Class cls;
        Class cls2;
        Class cls3;
        int i = -1;
        Field field = null;
        Field field2 = null;
        try {
            if (class$java$net$Socket == null) {
                cls = class$("java.net.Socket");
                class$java$net$Socket = cls;
            } else {
                cls = class$java$net$Socket;
            }
            Field[] declaredFields = cls.getDeclaredFields();
            int i2 = 0;
            while (true) {
                if (i2 >= declaredFields.length) {
                    break;
                } else if (declaredFields[i2].getName().equals("impl")) {
                    field = declaredFields[i2];
                    field.setAccessible(true);
                    break;
                } else {
                    i2++;
                }
            }
            SocketImpl socketImpl = (SocketImpl) field.get(socket);
            if (class$java$net$SocketImpl == null) {
                cls2 = class$("java.net.SocketImpl");
                class$java$net$SocketImpl = cls2;
            } else {
                cls2 = class$java$net$SocketImpl;
            }
            Field[] declaredFields2 = cls2.getDeclaredFields();
            int i3 = 0;
            while (true) {
                if (i3 >= declaredFields2.length) {
                    break;
                } else if (declaredFields2[i3].getName().equals("fd")) {
                    field2 = declaredFields2[i3];
                    field2.setAccessible(true);
                    break;
                } else {
                    i3++;
                }
            }
            FileDescriptor fileDescriptor = (FileDescriptor) field2.get(socketImpl);
            if (class$java$io$FileDescriptor == null) {
                cls3 = class$("java.io.FileDescriptor");
                class$java$io$FileDescriptor = cls3;
            } else {
                cls3 = class$java$io$FileDescriptor;
            }
            Field[] declaredFields3 = cls3.getDeclaredFields();
            int i4 = 0;
            while (true) {
                if (i4 >= declaredFields3.length) {
                    break;
                } else if (declaredFields3[i4].getName().equals("fd")) {
                    field2 = declaredFields3[i4];
                    field2.setAccessible(true);
                    break;
                } else {
                    i4++;
                }
            }
            i = field2.getInt(fileDescriptor);
        } catch (Exception e) {
            System.out.println("Ex: " + e);
        }
        return i;
    }

    @SuppressWarnings("rawtypes")
    static Class class$(String str) {
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
    }
}
