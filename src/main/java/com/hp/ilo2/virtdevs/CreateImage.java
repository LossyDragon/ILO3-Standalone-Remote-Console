package com.hp.ilo2.virtdevs;

import com.hp.ilo2.intgapp.locinfo;
import com.hp.ilo2.remcons.telnet;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class CreateImage extends JDialog implements ActionListener, WindowListener, ItemListener, Runnable {
    JTextField ImgFile;
    JButton browse;
    JButton create;
    JButton cancel;
    VProgressBar progress;
    boolean diskimage;
    boolean iscdrom;
    JFrame frame;
    String[] dev;
    int[] devt;
    boolean defaultRemovable;
    private JLabel statLabel;
    JPanel p;
    virtdevs virtdevsObj;
    boolean canceled = false;
    int retrycount = 10;
    int drvCboxChecked = 0;
    int targetIsDevice = 1;
    int targetIsCdrom = 0;
    JButton dimg = new JButton(getLocalString(locinfo.STATUSSTR_3107));
    CheckboxGroup drvGroup = new CheckboxGroup();
    Checkbox drvSel = new Checkbox(getLocalString(locinfo.STATUSSTR_3103), this.drvGroup, true);
    Checkbox drvPath = new Checkbox(getLocalString(locinfo.STATUSSTR_3104), this.drvGroup, false);
    JTextField DriveFile = new JTextField();
    JButton dbrowse = new JButton(getLocalString(locinfo.STATUSSTR_3109));
    Choice fdDrive = new Choice();

    public String getLocalString(int i) {
        String str = "";
        try {
            str = this.virtdevsObj.ParentApp.locinfoObj.getLocString(i);
        } catch (Exception e) {
            System.out.println(new StringBuffer().append("CreateImage:getLocalString").append(e.getMessage()).toString());
        }
        return str;
    }

    public CreateImage(virtdevs virtdevsVar) {
        super(virtdevsVar.parent, virtdevsVar.getLocalString(locinfo.STATUSSTR_3100));
        this.diskimage = true;
        this.iscdrom = false;
        this.defaultRemovable = false;
        boolean z = true;
        boolean z2 = false;
        this.virtdevsObj = virtdevsVar;
        this.frame = virtdevsVar.parent;
        this.virtdevsObj.ParentApp.vdMenuItemCrImage.setEnabled(false);
        setSize(400, 330);
        setResizable(false);
        setModal(false);
        addWindowListener(this);
        setLayout(new GridLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = 2;
        setLayout(new GridBagLayout());
        this.dimg.addActionListener(this);
        JPanel jPanel = new JPanel();
        jPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(1), getLocalString(locinfo.STATUSSTR_3121)));
        jPanel.add(this.dimg);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(jPanel, gridBagConstraints);
        this.drvSel.addItemListener(this);
        this.drvPath.addItemListener(this);
        this.DriveFile.addActionListener(this);
        this.dbrowse.setEnabled(false);
        this.dbrowse.addActionListener(this);
        MediaAccess mediaAccess = new MediaAccess();
        this.dev = mediaAccess.devices();
        this.devt = new int[this.dev.length];
        for (int i = 0; i < this.dev.length; i++) {
            this.devt[i] = mediaAccess.devtype(this.dev[i]);
            if (this.devt[i] == 2) {
                this.fdDrive.add(this.dev[i]);
                z = false;
                this.defaultRemovable = true;
            }
            if (this.devt[i] == 5) {
                this.fdDrive.add(this.dev[i]);
                if (i == 0) {
                    this.iscdrom = true;
                } else if (!this.defaultRemovable) {
                    this.iscdrom = true;
                    z2 = true;
                }
                z = false;
            }
        }
        if (z) {
            this.fdDrive.add(getLocalString(locinfo.STATUSSTR_3106));
        }
        this.fdDrive.addItemListener(this);
        JPanel jPanel2 = new JPanel(new GridBagLayout());
        jPanel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(1), getLocalString(locinfo.STATUSSTR_3122)));
        GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 0;
        jPanel2.add(this.drvSel, gridBagConstraints2);
        gridBagConstraints2.gridx = 1;
        gridBagConstraints2.gridy = 0;
        jPanel2.add(this.fdDrive, gridBagConstraints2);
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 1;
        jPanel2.add(this.drvPath, gridBagConstraints2);
        gridBagConstraints2.gridx = 2;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.weighty = 1.0d;
        gridBagConstraints2.anchor = 19;
        jPanel2.add(this.dbrowse, gridBagConstraints2);
        gridBagConstraints2.ipadx = 187;
        gridBagConstraints2.gridx = 1;
        gridBagConstraints2.gridy = 1;
        jPanel2.add(this.DriveFile, gridBagConstraints2);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        add(jPanel2, gridBagConstraints);
        this.ImgFile = new JTextField();
        this.ImgFile.setSize((int) telnet.TELNET_SB, 30);
        this.ImgFile.addActionListener(this);
        this.browse = new JButton(getLocalString(locinfo.STATUSSTR_3109));
        this.browse.addActionListener(this);
        JPanel jPanel3 = new JPanel(new GridBagLayout());
        jPanel3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(1), getLocalString(locinfo.STATUSSTR_3123)));
        GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.gridx = 1;
        gridBagConstraints3.gridy = 0;
        jPanel3.add(this.browse, gridBagConstraints3);
        gridBagConstraints3.ipadx = 270;
        gridBagConstraints3.gridx = 0;
        gridBagConstraints3.gridy = 0;
        jPanel3.add(this.ImgFile, gridBagConstraints3);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        add(jPanel3, gridBagConstraints);
        this.progress = new VProgressBar(350, 25, Color.lightGray, Color.blue, Color.white);
        JPanel jPanel4 = new JPanel(new GridBagLayout());
        jPanel4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(1), getLocalString(locinfo.STATUSSTR_3124)));
        GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
        this.statLabel = new JLabel(" ");
        this.statLabel.setFont(new Font("Arial", 1, 12));
        gridBagConstraints4.gridx = 0;
        gridBagConstraints4.gridy = 0;
        jPanel4.add(this.statLabel, gridBagConstraints4);
        gridBagConstraints4.gridx = 0;
        gridBagConstraints4.gridy = 1;
        jPanel4.add(this.progress, gridBagConstraints4);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        add(jPanel4, gridBagConstraints);
        this.create = new JButton(getLocalString(locinfo.STATUSSTR_310a));
        this.create.setEnabled(false);
        this.create.addActionListener(this);
        this.cancel = new JButton(getLocalString(locinfo.STATUSSTR_310b));
        this.cancel.addActionListener(this);
        this.p = new JPanel();
        this.p.setLayout(new FlowLayout(2));
        this.p.add(this.create);
        this.p.add(this.cancel);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        add(this.p, gridBagConstraints);
        if (z2) {
            this.dimg.setLabel(getLocalString(locinfo.STATUSSTR_3107));
            this.diskimage = true;
            this.dimg.setEnabled(false);
        } else {
            this.dimg.setEnabled(true);
        }
        this.dimg.repaint();
        setLocationRelativeTo((Component) null);
        setVisible(true);
    }

    void add(Component component, GridBagConstraints gridBagConstraints, int i, int i2, int i3, int i4) {
        gridBagConstraints.gridx = i;
        gridBagConstraints.gridy = i2;
        gridBagConstraints.gridwidth = i3;
        gridBagConstraints.gridheight = i4;
        add(component, gridBagConstraints);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        Object source = actionEvent.getSource();
        if (source == this.browse) {
            this.statLabel.setText(" ");
            this.progress.updateBar(0.0f);
            String string = new VFileDialog(getLocalString(locinfo.STATUSSTR_310c), null).getString();
            if (string != null) {
                this.ImgFile.setText(string);
                if ((0 != this.drvCboxChecked || this.fdDrive.getSelectedItem().equals(getLocalString(locinfo.STATUSSTR_3106))) && (1 != this.drvCboxChecked || this.DriveFile.getText().equals(""))) {
                    this.create.setEnabled(false);
                } else {
                    this.create.setEnabled(true);
                }
            }
        }
        if (source == this.dbrowse) {
            this.statLabel.setText(" ");
            this.progress.updateBar(0.0f);
            String string2 = new VFileDialog(getLocalString(locinfo.STATUSSTR_310d), null).getString();
            if (string2 != null) {
                this.DriveFile.setText(string2);
                if (!this.ImgFile.getText().equals("")) {
                    this.create.setEnabled(true);
                } else {
                    this.create.setEnabled(false);
                }
            }
        }
        if (source == this.create) {
            this.create.setEnabled(false);
            this.browse.setEnabled(false);
            if (0 == this.drvCboxChecked) {
                this.fdDrive.setEnabled(false);
            } else {
                this.DriveFile.setEnabled(false);
                this.dbrowse.setEnabled(false);
            }
            this.ImgFile.setEnabled(false);
            this.dimg.setEnabled(false);
            if (this.diskimage) {
                this.statLabel.setText(getLocalString(locinfo.STATUSSTR_310e));
            } else {
                this.statLabel.setText(getLocalString(locinfo.STATUSSTR_310f));
            }
            new Thread(this).start();
        }
        if (source == this.dimg) {
            this.statLabel.setText(" ");
            this.progress.updateBar(0.0f);
            this.diskimage = !this.diskimage;
            if (this.diskimage) {
                this.dimg.setLabel(getLocalString(locinfo.STATUSSTR_3107));
            } else {
                this.dimg.setLabel(getLocalString(locinfo.STATUSSTR_3108));
            }
            this.dimg.repaint();
        }
        if (source == this.cancel) {
            this.statLabel.setText(" ");
            this.progress.updateBar(0.0f);
            this.virtdevsObj.ParentApp.vdMenuItemCrImage.setEnabled(true);
            this.canceled = true;
            dispose();
        }
        if (source == this.ImgFile) {
            this.statLabel.setText(" ");
            this.progress.updateBar(0.0f);
            if (this.ImgFile.getText().equals("") || ((0 != this.drvCboxChecked || this.fdDrive.getSelectedItem().equals(getLocalString(locinfo.STATUSSTR_3106))) && (1 != this.drvCboxChecked || this.DriveFile.getText().equals("")))) {
                this.create.setEnabled(false);
            } else {
                this.create.setEnabled(true);
            }
        } else if (source == this.DriveFile) {
            this.statLabel.setText(" ");
            this.progress.updateBar(0.0f);
            if (this.ImgFile.getText().equals("") || this.DriveFile.getText().equals("")) {
                this.create.setEnabled(false);
            } else {
                this.create.setEnabled(true);
            }
        }
    }

    public void itemStateChanged(ItemEvent itemEvent) {
        if (itemEvent.getSource() == this.fdDrive) {
            this.statLabel.setText(" ");
            this.progress.updateBar(0.0f);
            String selectedItem = this.fdDrive.getSelectedItem();
            int i = 0;
            while (i < this.dev.length && !selectedItem.equals(this.dev[i])) {
                i++;
            }
            if (i < this.dev.length) {
                this.iscdrom = this.devt[i] == 5;
            } else {
                this.iscdrom = false;
                this.create.setEnabled(false);
            }
            if (this.iscdrom) {
                this.dimg.setLabel(getLocalString(locinfo.STATUSSTR_3107));
                this.diskimage = true;
                this.dimg.setEnabled(false);
            } else {
                this.dimg.setEnabled(true);
            }
            this.dimg.repaint();
            if (this.ImgFile.getText().equals("") || this.fdDrive.getSelectedItem().equals(getLocalString(locinfo.STATUSSTR_3106))) {
                this.create.setEnabled(false);
            } else {
                this.create.setEnabled(true);
            }
        }
        if (itemEvent.getSource() == this.drvSel) {
            this.DriveFile.setEditable(false);
            this.dbrowse.setEnabled(false);
            this.fdDrive.setEnabled(true);
            this.drvCboxChecked = 0;
            this.statLabel.setText(" ");
            this.progress.updateBar(0.0f);
            String selectedItem2 = this.fdDrive.getSelectedItem();
            int i2 = 0;
            while (i2 < this.dev.length && !selectedItem2.equals(this.dev[i2])) {
                i2++;
            }
            if (i2 < this.dev.length) {
                this.iscdrom = this.devt[i2] == 5;
            } else {
                this.iscdrom = false;
            }
            if (this.iscdrom) {
                this.dimg.setLabel(getLocalString(locinfo.STATUSSTR_3107));
                this.diskimage = true;
                this.dimg.setEnabled(false);
            } else {
                this.dimg.setEnabled(true);
            }
            this.dimg.repaint();
            if (this.fdDrive.getSelectedItem().equals(getLocalString(locinfo.STATUSSTR_3106)) || this.ImgFile.getText().equals("")) {
                this.create.setEnabled(false);
            } else {
                this.create.setEnabled(true);
            }
        } else if (itemEvent.getSource() == this.drvPath) {
            this.DriveFile.setEditable(true);
            this.dbrowse.setEnabled(true);
            this.fdDrive.setEnabled(false);
            this.drvCboxChecked = 1;
            this.dimg.setLabel(getLocalString(locinfo.STATUSSTR_3107));
            this.diskimage = true;
            this.dimg.setEnabled(false);
            this.dimg.repaint();
            if (this.DriveFile.getText().equals("") || this.ImgFile.getText().equals("")) {
                this.create.setEnabled(false);
            } else {
                this.create.setEnabled(true);
            }
        }
    }

    public int cdrom_testunitready(MediaAccess mediaAccess) {
        byte[] bArr = new byte[8];
        int scsi = mediaAccess.scsi(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, 1, 8, bArr, new byte[3]);
        if (scsi >= 0) {
            scsi = SCSI.mk_int32(bArr, 0) * SCSI.mk_int32(bArr, 4);
        }
        return scsi;
    }

    public int cdrom_startstopunit(MediaAccess mediaAccess) {
        byte[] bArr = new byte[8];
        int scsi = mediaAccess.scsi(new byte[]{27, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0}, 1, 8, bArr, new byte[3]);
        if (scsi >= 0) {
            scsi = SCSI.mk_int32(bArr, 0) * SCSI.mk_int32(bArr, 4);
        }
        return scsi;
    }

    public long cdrom_size(MediaAccess mediaAccess) {
        byte[] byArray = new byte[]{37, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] byArray2 = new byte[8];
        byte[] byArray3 = new byte[3];
        long l = mediaAccess.scsi(byArray, 1, 8, byArray2, byArray3);
        if (l >= 0L) {
            l = (long)SCSI.mk_int32(byArray2, 0) * (long)SCSI.mk_int32(byArray2, 4);
        }
        return l;
    }

    public void cdrom_read(MediaAccess mediaAccess, long j, int i, byte[] bArr) throws IOException {
        byte[] bArr2 = new byte[3];
        int i2 = (int) (j / 2048);
        if (mediaAccess.scsi(new byte[]{40, 0, (byte) ((i2 >> 24) & telnet.TELNET_IAC), (byte) ((i2 >> 16) & telnet.TELNET_IAC), (byte) ((i2 >> 8) & telnet.TELNET_IAC), (byte) ((i2 >> 0) & telnet.TELNET_IAC), 0, (byte) (((i / 2048) >> 8) & telnet.TELNET_IAC), (byte) (((i / 2048) >> 0) & telnet.TELNET_IAC), 0, 0, 0}, 1, i, bArr, bArr2) == -1) {
            throw new IOException("Error reading CD-ROM.");
        } else if (bArr2[0] != 0) {
            throw new IOException(new StringBuffer().append("Error reading CD-ROM.  Sense data (").append(D.hex(bArr2[0], 1)).append("/").append(D.hex(bArr2[1], 2)).append("/").append(D.hex(bArr2[2], 2)).append(")").toString());
        }
    }

    public void cdrom_read_retry(MediaAccess mediaAccess, long j, int i, byte[] bArr) throws IOException {
        byte[] bArr2 = new byte[3];
        byte[] bArr3 = new byte[12];
        int i2 = 0;
        int i3 = (int) (j / 2048);
        byte[] bArr4 = {40, 0, (byte) ((i3 >> 24) & telnet.TELNET_IAC), (byte) ((i3 >> 16) & telnet.TELNET_IAC), (byte) ((i3 >> 8) & telnet.TELNET_IAC), (byte) ((i3 >> 0) & telnet.TELNET_IAC), 0, (byte) (((i / 2048) >> 8) & telnet.TELNET_IAC), (byte) (((i / 2048) >> 0) & telnet.TELNET_IAC), 0, 0, 0};
        do {
            System.currentTimeMillis();
            int scsi = mediaAccess.scsi(bArr4, 1, i, bArr, bArr2);
            System.currentTimeMillis();
            if (scsi < 0) {
                cdrom_testunitready(mediaAccess);
                cdrom_startstopunit(mediaAccess);
                scsi = -1;
            }
            if (bArr2[1] == 41) {
                scsi = -1;
            }
            if (bArr2[0] == 3 || bArr2[0] == 4) {
                if (bArr2[1] == 2 && bArr2[2] == 0) {
                    bArr3[0] = 43;
                    bArr3[1] = 0;
                    bArr3[2] = bArr4[2];
                    bArr3[3] = bArr4[3];
                    bArr3[4] = bArr4[4];
                    bArr3[5] = bArr4[5];
                    bArr3[6] = 0;
                    bArr3[7] = 0;
                    bArr3[8] = 0;
                    bArr3[9] = 0;
                    bArr3[10] = 0;
                    bArr3[11] = 0;
                    mediaAccess.scsi(bArr3, 1, i, bArr, bArr2);
                    cdrom_testunitready(mediaAccess);
                } else if (bArr2[1] == 17) {
                    cdrom_testunitready(mediaAccess);
                    cdrom_startstopunit(mediaAccess);
                } else {
                    cdrom_testunitready(mediaAccess);
                }
                scsi = -1;
            }
            if (scsi >= 0) {
                break;
            }
            i2++;
        } while (i2 < this.retrycount);
        if (i2 >= this.retrycount) {
            D.println(0, "RETRIES FAILED ! ");
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        int n = 0;
        long l = 0L;
        String string = this.ImgFile.getText();
        boolean bl = false;
        if (string.equals("")) {
            this.browse.setEnabled(true);
            if (0 == this.drvCboxChecked) {
                this.fdDrive.setEnabled(true);
            } else {
                this.DriveFile.setEnabled(true);
                this.dbrowse.setEnabled(true);
            }
            this.ImgFile.setEnabled(true);
            this.DriveFile.setEnabled(true);
            this.dimg.setEnabled(true);
            return;
        }
        MediaAccess mediaAccess = new MediaAccess();
        MediaAccess mediaAccess2 = new MediaAccess();
        System.out.println("Message from CreateImage");
        try {
            int n2;
            if (0 == this.drvCboxChecked && this.iscdrom) {
                n2 = mediaAccess.open(this.fdDrive.getSelectedItem(), 1);
                if (n2 < 0) {
                    bl = true;
                    new VErrorDialog(this.getLocalString(8247) + " (" + mediaAccess.dio.sysError(-n2) + ")", false);
                    throw new IOException("Couldn't open cdrom " + n2);
                }
                this.cdrom_testunitready(mediaAccess);
                l = this.cdrom_size(mediaAccess);
                n = 65536;
            } else {
                if (0 == this.drvCboxChecked) {
                    n2 = mediaAccess.open(this.fdDrive.getSelectedItem(), 1);
                    this.targetIsDevice = 1;
                    this.targetIsCdrom = 0;
                    System.out.println("CrtDev " + this.fdDrive.getSelectedItem() + " " + n2 + " " + this.targetIsDevice);
                } else {
                    int n3 = mediaAccess.devtype(this.DriveFile.getText());
                    if (n3 == 5) {
                        this.targetIsDevice = 1;
                        this.targetIsCdrom = 1;
                    } else if (n3 == 2) {
                        this.targetIsDevice = 1;
                        this.targetIsCdrom = 0;
                    } else {
                        this.targetIsDevice = 0;
                        this.targetIsCdrom = 0;
                    }
                    n2 = mediaAccess.open(this.DriveFile.getText(), this.targetIsDevice);
                    System.out.println("CrtFile " + this.DriveFile.getText() + " " + n2 + " " + this.targetIsDevice);
                }
                if (1 == this.targetIsDevice) {
                    if (1 == this.targetIsCdrom) {
                        this.cdrom_testunitready(mediaAccess);
                        l = this.cdrom_size(mediaAccess);
                        n = 65536;
                    } else {
                        l = mediaAccess.size();
                        n = mediaAccess.dio.BytesPerSec * mediaAccess.dio.SecPerTrack;
                    }
                    System.out.println("CrtDev actual Dev size" + l + " " + n);
                } else {
                    l = mediaAccess.size();
                    n = (int)(l / 512L);
                    System.out.println("CrtFile static Dev size" + l + " " + n);
                }
            }
        }
        catch (IOException iOException) {
            System.out.println("Exception opening media access");
        }
        if (!this.diskimage && mediaAccess.wp()) {
            new VErrorDialog(this.frame, this.getLocalString(8248) + " " + this.fdDrive.getSelectedItem() + this.getLocalString(8249));
            bl = true;
            this.create.setEnabled(true);
            this.browse.setEnabled(true);
            if (0 == this.drvCboxChecked) {
                this.fdDrive.setEnabled(true);
            } else {
                this.DriveFile.setEnabled(true);
                this.dbrowse.setEnabled(true);
            }
            this.ImgFile.setEnabled(true);
            this.DriveFile.setEnabled(true);
            this.dimg.setEnabled(true);
            try {
                mediaAccess.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            return;
        }
        this.setCursor(Cursor.getPredefinedCursor(3));
        long l2 = l;
        if (n == 0 || l2 == 0L) {
            String string2 = this.getLocalString(8250) + " " + this.getLocalString(8241);
            new VErrorDialog(this.frame, string2);
            bl = true;
            n = 0;
            l2 = 0L;
        } else {
            try {
                mediaAccess2.open(string, this.diskimage ? 2 : 0);
            }
            catch (IOException iOException) {
                new VErrorDialog(this.frame, this.getLocalString(8251) + string + ".");
            }
        }
        long l3 = 0L;
        byte[] byArray = new byte[n];
        boolean bl2 = false;
        try {
            while (l2 > 0L && !this.canceled) {
                int n4;
                int n5 = n4 = (long)n < l2 ? n : (int)l2;
                if (this.diskimage) {
                    if (this.iscdrom) {
                        this.cdrom_read_retry(mediaAccess, l3, n4, byArray);
                    } else {
                        mediaAccess.read(l3, n4, byArray);
                    }
                    mediaAccess2.write(l3, n4, byArray);
                } else {
                    mediaAccess2.read(l3, n4, byArray);
                    mediaAccess.write(l3, n4, byArray);
                }
                l3 += (long)n4;
                l2 -= (long)n4;
                if (!this.diskimage && (double)((float)l3 / (float)l) >= 0.95) {
                    this.progress.updateBar(0.95f);
                    continue;
                }
                this.progress.updateBar((float)l3 / (float)l);
            }
        }
        catch (IOException iOException) {
            bl = true;
            new VErrorDialog(this.frame, this.getLocalString(8252) + (this.diskimage ? this.getLocalString(8253) : this.getLocalString(8254)) + this.getLocalString(8255) + " (" + iOException + ")");
        }
        this.setCursor(Cursor.getPredefinedCursor(0));
        if (!bl) {
            try {
                mediaAccess.close();
                mediaAccess2.close();
            }
            catch (IOException iOException) {
                D.println(0, "Closing: " + iOException);
            }
            this.progress.updateBar((float)l3 / (float)l);
            if (this.diskimage) {
                this.statLabel.setText(this.getLocalString(12560));
            } else {
                this.statLabel.setText(this.getLocalString(12561));
            }
            this.p.remove(this.create);
            this.cancel.setLabel(this.getLocalString(12566));
        } else {
            this.statLabel.setText(" ");
        }
        this.create.setEnabled(true);
        this.browse.setEnabled(true);
        if (0 == this.drvCboxChecked) {
            this.fdDrive.setEnabled(true);
        } else {
            this.DriveFile.setEnabled(true);
            this.dbrowse.setEnabled(true);
        }
        this.ImgFile.setEnabled(true);
        this.DriveFile.setEnabled(true);
        if (this.iscdrom) {
            this.dimg.setEnabled(false);
        } else {
            this.dimg.setEnabled(true);
        }
    }

    public void windowClosing(WindowEvent windowEvent) {
        this.virtdevsObj.ParentApp.vdMenuItemCrImage.setEnabled(true);
        this.canceled = true;
        dispose();
    }

    public void windowActivated(WindowEvent windowEvent) {
    }

    public void windowClosed(WindowEvent windowEvent) {
    }

    public void windowDeactivated(WindowEvent windowEvent) {
    }

    public void windowDeiconified(WindowEvent windowEvent) {
    }

    public void windowIconified(WindowEvent windowEvent) {
    }

    public void windowOpened(WindowEvent windowEvent) {
    }
}
