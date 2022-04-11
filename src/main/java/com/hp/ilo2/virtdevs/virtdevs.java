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
    public static final int UNQF_HIDEFLP = 1;
    public static int UID;
    static final int ImageDone = 39;
    String host;
    String base;
    String configuration;
    String dev_floppy;
    String dev_cdrom;
    String dev_auto;
    JFrame parent;
    public static boolean cd_support = true;
    public static Properties prop;
    public intgapp ParentApp;
    String hostAddress;
    public Connection fdConnection;
    public Connection cdConnection;
    public Thread fdThread;
    public Thread cdThread;
    static Class class$java$net$Socket;
    static Class class$java$net$SocketImpl;
    static Class class$java$io$FileDescriptor;
    public int dev_cd_device = 0;
    public int dev_fd_device = 0;
    public int unq_feature = 0;
    boolean force_config = false;
    boolean thread_init = false;
    byte[] pre = new byte[16];
    byte[] key = new byte[32];
    int fdport = 17988;
    public boolean cdConnected = false;
    public boolean fdConnected = false;
    protected boolean stopFlag = false;
    protected boolean running = false;

    public String getLocalString(int i) {
        String str = "";
        try {
            str = this.ParentApp.locinfoObj.getLocString(i);
        } catch (Exception e) {
            System.out.println(new StringBuffer().append("virdevs:getLocalString").append(e.getMessage()).toString());
        }
        return str;
    }

    public virtdevs(intgapp intgappVar) {
        this.ParentApp = intgappVar;
    }

    public Image get(String str) {
        return this.ParentApp.getImage(getClass().getClassLoader().getResource(new StringBuffer().append("com/hp/ilo2/virtdevs/").append(str).toString()));
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
        this.base = new StringBuffer().append(documentBase.getProtocol()).append("://").append(documentBase.getHost()).toString();
        if (documentBase.getPort() != -1) {
            this.base = new StringBuffer().append(this.base).append(":").append(documentBase.getPort()).toString();
        }
        this.base = new StringBuffer().append(this.base).append("/").toString();
        String parameter = this.ParentApp.getParameter("INFO0");
        if (parameter != null) {
            for (int i = 0; i < 16; i++) {
                try {
                    this.pre[i] = (byte) Integer.parseInt(parameter.substring(2 * i, (2 * i) + 2), 16);
                } catch (NumberFormatException e) {
                    D.println(0, new StringBuffer().append("Couldn't parse INFO0: ").append(e).toString());
                }
            }
        }
        try {
            if (null != this.ParentApp.vm_port) {
                this.fdport = Integer.parseInt(this.ParentApp.vm_port);
            }
        } catch (NumberFormatException e2) {
            D.println(0, new StringBuffer().append("Couldn't parse INFO1: ").append(e2).toString());
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
                D.println(0, new StringBuffer().append("Couldn't parse UNIQUE_FEATURES: ").append(e3).toString());
            }
        }
        this.key = this.ParentApp.getParameter("RCINFO1").getBytes();
        if (this.ParentApp.optional_features.indexOf("ENCRYPT_VMKEY") != -1) {
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
            System.out.println(new StringBuffer().append("Exception: ").append(e).toString());
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
        D.println(3, new StringBuffer().append("Stop ").append(this).toString());
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
            System.out.println(new StringBuffer().append("Exception: ").append(e).toString());
        }
    }

    @Override // java.lang.Runnable
    public synchronized void run() {
        if (!this.thread_init) {
            prop = new Properties();
            try {
                prop.load(new FileInputStream(new StringBuffer().append(System.getProperty("user.home")).append(System.getProperty("file.separator")).append(".java").append(System.getProperty("file.separator")).append("hp.properties").toString()));
            } catch (Exception e) {
                System.out.println(new StringBuffer().append("Exception: ").append(e).toString());
            }
            cd_support = Boolean.valueOf(prop.getProperty("com.hp.ilo2.virtdevs.cdimage", "true")).booleanValue();
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
                setCursor(Cursor.getPredefinedCursor(3));
                try {
                    int connect = this.fdConnection.connect();
                    setCursor(Cursor.getPredefinedCursor(0));
                    switch (connect) {
                        case 0:
                            this.fdThread = new Thread(this.fdConnection, "fdConnection");
                            this.fdThread.start();
                            this.fdConnected = true;
                            return true;
                        case 33:
                            this.ParentApp.lockFdMenu(true, "");
                            new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), getLocalString(locinfo.DIALOGSTR_2006));
                            return false;
                        case 34:
                            if (rekey("/html/java_irc.html")) {
                                str2 = getLocalString(locinfo.DIALOGSTR_2007);
                            } else {
                                str2 = getLocalString(locinfo.DIALOGSTR_2008);
                            }
                            new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), str2);
                            return false;
                        case SCSI.SCSI_READ_CAPACITIES :
                            this.ParentApp.lockFdMenu(true, "");
                            new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), getLocalString(locinfo.DIALOGSTR_2009));
                            return false;
                        case SCSI.SCSI_READ_CAPACITY :
                            this.ParentApp.lockFdMenu(true, "");
                            new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), getLocalString(locinfo.DIALOGSTR_200a));
                            return false;
                        case 38:
                            this.ParentApp.lockFdMenu(true, "");
                            new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), getLocalString(locinfo.DIALOGSTR_200b));
                            return false;
                        default:
                            this.ParentApp.lockFdMenu(true, "");
                            new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), new StringBuffer().append(getLocalString(locinfo.DIALOGSTR_200c)).append("(").append(Integer.toHexString(connect)).append(").").append(getLocalString(locinfo.DIALOGSTR_200d)).toString());
                            return false;
                    }
                } catch (Exception e) {
                    setCursor(Cursor.getPredefinedCursor(0));
                    D.println(0, "Couldn't connect!\n");
                    new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), new StringBuffer().append(getLocalString(locinfo.DIALOGSTR_2005)).append("(").append(e).append(")").toString());
                    return false;
                }
            } catch (Exception e2) {
                new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), e2.getMessage());
                return false;
            }
        } else {
            try {
                this.fdConnection.close();
                return true;
            } catch (Exception e3) {
                D.println(0, new StringBuffer().append("Exception during close: ").append(e3).toString());
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
                            new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), getLocalString(locinfo.DIALOGSTR_2006));
                            return false;
                        case 34:
                            if (rekey("/html/java_irc.html")) {
                                str2 = getLocalString(locinfo.DIALOGSTR_2007);
                            } else {
                                str2 = getLocalString(locinfo.DIALOGSTR_2008);
                            }
                            new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), str2);
                            return false;
                        case SCSI.SCSI_READ_CAPACITIES :
                            this.ParentApp.lockCdMenu(true, "");
                            new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), getLocalString(locinfo.DIALOGSTR_2009));
                            return false;
                        case SCSI.SCSI_READ_CAPACITY :
                            this.ParentApp.lockCdMenu(true, "");
                            new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), getLocalString(locinfo.DIALOGSTR_200f));
                            return false;
                        case 38:
                            this.ParentApp.lockCdMenu(true, "");
                            new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), getLocalString(locinfo.DIALOGSTR_200b));
                            return false;
                        default:
                            this.ParentApp.lockCdMenu(true, "");
                            new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), new StringBuffer().append(getLocalString(locinfo.DIALOGSTR_200c)).append(" (").append(Integer.toHexString(connect)).append(").").append(getLocalString(locinfo.DIALOGSTR_200d)).toString());
                            return false;
                    }
                } catch (Exception e) {
                    D.println(0, "Couldn't connect!\n");
                    new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), new StringBuffer().append(getLocalString(locinfo.DIALOGSTR_200e)).append(" (").append(e).append(")").toString());
                    return false;
                }
            } catch (Exception e2) {
                new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), e2.getMessage());
                return false;
            }
        } else {
            try {
                this.cdConnection.close();
                return true;
            } catch (Exception e3) {
                D.println(0, new StringBuffer().append("Exception during close: ").append(e3).toString());
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
            URL url = new URL(new StringBuffer().append(this.base).append("modusb.cgi?usb=").append(this.configuration).toString());
            url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    bufferedReader.close();
                    return;
                }
                D.println(3, new StringBuffer().append("updcfg: ").append(readLine).toString());
            }
        } catch (Exception e) {
            new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), new StringBuffer().append(getLocalString(locinfo.DIALOGSTR_2010)).append("(").append(e).append(")").toString());
            e.printStackTrace();
        }
    }

    public boolean rekey(String str) {
        String str2 = null;
        try {
            D.println(3, new StringBuffer().append("Downloading new key: ").append(this.base).append(str).toString());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(new StringBuffer().append(this.base).append(str).toString()).openStream()));
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
                D.println(0, new StringBuffer().append("rekey: ").append(readLine).toString());
                if (readLine.startsWith("info0=\"")) {
                    str2 = readLine.substring(7, ImageDone);
                    break;
                }
            }
            bufferedReader.close();
            if (str2 == null) {
                new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), getLocalString(locinfo.DIALOGSTR_2011));
                return false;
            }
            for (int i = 0; i < 16; i++) {
                try {
                    this.pre[i] = (byte) Integer.parseInt(str2.substring(2 * i, (2 * i) + 2), 16);
                } catch (NumberFormatException e) {
                    D.println(0, new StringBuffer().append("Couldn't parse new key: ").append(e).toString());
                    new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), getLocalString(locinfo.DIALOGSTR_2012));
                    return false;
                }
            }
            return true;
        } catch (Exception e2) {
            D.println(0, new StringBuffer().append("rekey: ").append(e2).toString());
            new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), getLocalString(locinfo.DIALOGSTR_2011));
            return false;
        }
    }

    public void change_disk(Connection connection, String str) {
        try {
            connection.change_disk(str);
        } catch (IOException e) {
            new VErrorDialog(this.parent, getLocalString(locinfo.DIALOGSTR_2014), new StringBuffer().append(getLocalString(locinfo.DIALOGSTR_2013)).append(" (").append(e).append(")").toString());
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
            System.out.println(new StringBuffer().append("Ex: ").append(e).toString());
        }
        return i;
    }

    static Class class$(String str) {
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException e) {
            throw new NoClassDefFoundError(e.getMessage());
        }
    }
}
