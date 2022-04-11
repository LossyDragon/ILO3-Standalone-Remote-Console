package com.hp.ilo2.virtdevs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;


public class MediaAccess {
    public static final int Unknown = 0;
    public static final int F5_1Pt2_512 = 1;
    public static final int F3_1Pt44_512 = 2;
    public static final int F3_2Pt88_512 = 3;
    public static final int F3_20Pt88_512 = 4;
    public static final int F3_720_512 = 5;
    public static final int F5_360_512 = 6;
    public static final int F5_320_512 = 7;
    public static final int F5_320_1024 = 8;
    public static final int F5_180_512 = 9;
    public static final int F5_160_512 = 10;
    public static final int RemovableMedia = 11;
    public static final int FixedMedia = 12;
    public static final int F3_120M_512 = 13;
    public static final int ImageFile = 100;
    public static final int NoRootDir = 1;
    public static final int Removable = 2;
    public static final int Fixed = 3;
    public static final int Remote = 4;
    public static final int CDROM = 5;
    public static final int Ramdisk = 6;
    public static String dllext = "";
    static int dio_setup = -1;
    DirectIO dio;
    File file;
    RandomAccessFile raf;
    boolean dev = false;
    boolean readonly = false;
    int zero_offset = 0;

    public int open(String str, int i) throws IOException {
        this.dev = (i & 1) == 1;
        boolean z = (i & 2) == 2;
        this.zero_offset = 0;
        if (!this.dev) {
            this.readonly = false;
            this.file = new File(str);
            if (!this.file.exists() && !z) {
                throw new IOException(new StringBuffer().append("File ").append(str).append(" does not exist").toString());
            } else if (this.file.isDirectory()) {
                throw new IOException(new StringBuffer().append("File ").append(str).append(" is a directory").toString());
            } else {
                try {
                    this.raf = new RandomAccessFile(str, "rw");
                } catch (IOException e) {
                    if (!z) {
                        this.raf = new RandomAccessFile(str, "r");
                        this.readonly = true;
                    } else {
                        throw e;
                    }
                }
                byte[] bArr = new byte[512];
                read(0L, 512, bArr);
                if (bArr[0] == 67 && bArr[1] == 80 && bArr[2] == 81 && bArr[3] == 82 && bArr[4] == 70 && bArr[5] == 66 && bArr[6] == 76 && bArr[7] == 79) {
                    this.zero_offset = bArr[14] | (bArr[15] << 8);
                }
                return 0;
            }
        } else if (dio_setup != 0) {
            throw new IOException(new StringBuffer().append("DirectIO not possible (").append(dio_setup).append(")").toString());
        } else {
            if (this.dio == null) {
                this.dio = new DirectIO();
            }
            return this.dio.open(str);
        }
    }

    public int close() throws IOException {
        if (this.dev) {
            return this.dio.close();
        }
        this.raf.close();
        return 0;
    }

    public void read(long j, int i, byte[] bArr) throws IOException {
        long j2 = j + this.zero_offset;
        if (this.dev) {
            int read = this.dio.read(j2, i, bArr);
            if (read != 0) {
                throw new IOException(new StringBuffer().append("DirectIO read error (").append(this.dio.sysError(-read)).append(")").toString());
            }
            return;
        }
        this.raf.seek(j2);
        this.raf.read(bArr, 0, i);
    }

    public void write(long j, int i, byte[] bArr) throws IOException {
        long j2 = j + this.zero_offset;
        if (this.dev) {
            int write = this.dio.write(j2, i, bArr);
            if (write != 0) {
                throw new IOException(new StringBuffer().append("DirectIO write error (").append(this.dio.sysError(-write)).append(")").toString());
            }
            return;
        }
        this.raf.seek(j2);
        this.raf.write(bArr, 0, i);
    }

    public long size() throws IOException {
        long j;
        if (this.dev) {
            j = this.dio.size();
        } else {
            j = this.raf.length() - this.zero_offset;
        }
        return j;
    }

    public int format(int i, int i2, int i3, int i4, int i5) throws IOException {
        if (!this.dev) {
            return 0;
        }
        this.dio.media_type = i;
        this.dio.StartCylinder = i2;
        this.dio.EndCylinder = i3;
        this.dio.StartHead = i4;
        this.dio.EndHead = i5;
        return this.dio.format();
    }

    public String[] devices() {
        if (dio_setup != 0) {
            return null;
        }
        if (this.dio == null) {
            this.dio = new DirectIO();
        }
        return this.dio.devices();
    }

    public int devtype(String str) {
        if (dio_setup != 0) {
            return 0;
        }
        if (this.dio == null) {
            this.dio = new DirectIO();
        }
        return this.dio.devtype(str);
    }

    public int scsi(byte[] bArr, int i, int i2, byte[] bArr2, byte[] bArr3) {
        return scsi(bArr, i, i2, bArr2, bArr3, 0);
    }

    public int scsi(byte[] bArr, int i, int i2, byte[] bArr2, byte[] bArr3, int i3) {
        int i4;
        if (this.dev) {
            i4 = this.dio.scsi(bArr, i, i2, bArr2, bArr3, i3);
        } else {
            i4 = -1;
        }
        return i4;
    }

    public boolean wp() {
        boolean z;
        if (this.dev) {
            z = this.dio.wp == 1;
        } else {
            z = this.readonly;
        }
        return z;
    }

    public int type() {
        if (this.dev && this.dio != null) {
            return this.dio.media_type;
        }
        if (this.raf != null) {
            return 100;
        }
        return 0;
    }

    public int dllExtract(String str, String str2) {
        ClassLoader classLoader = getClass().getClassLoader();
        byte[] bArr = new byte[4096];
        D.println(1, new StringBuffer().append("dllExtract trying ").append(str).toString());
        if (classLoader.getResource(str) == null) {
            return -1;
        }
        D.println(1, new StringBuffer().append("Extracting ").append(classLoader.getResource(str).toExternalForm()).append(" to ").append(str2).toString());
        try {
            InputStream resourceAsStream = classLoader.getResourceAsStream(str);
            FileOutputStream fileOutputStream = new FileOutputStream(str2);
            while (true) {
                int read = resourceAsStream.read(bArr, 0, 4096);
                if (read == -1) {
                    resourceAsStream.close();
                    fileOutputStream.close();
                    return 0;
                }
                fileOutputStream.write(bArr, 0, read);
            }
        } catch (IOException e) {
            D.println(0, new StringBuffer().append("dllExtract: ").append(e).toString());
            return -2;
        }
    }

    public int setup_DirectIO() {
        String property = System.getProperty("file.separator");
        String property2 = System.getProperty("java.io.tmpdir");
        String lowerCase = System.getProperty("os.name").toLowerCase();
        String property3 = System.getProperty("java.vm.name");
        String str = "unknown";
        if (property2 == null) {
            property2 = lowerCase.startsWith("windows") ? "C:\\TEMP" : "/tmp";
        }
        if (lowerCase.startsWith("windows")) {
            if (property3.indexOf("64") != -1) {
                System.out.println("virt: Detected win 64bit jvm");
                str = "x86-win64";
            } else {
                System.out.println("virt: Detected win 32bit jvm");
                str = "x86-win32";
            }
            dllext = ".dll";
        } else if (lowerCase.startsWith("linux")) {
            if (property3.indexOf("64") != -1) {
                System.out.println("virt: Detected 64bit linux jvm");
                str = "x86-linux-64";
            } else {
                System.out.println("virt: Detected 32bit linux jvm");
                str = "x86-linux-32";
            }
        }
        File file = new File(property2);
        if (!file.exists()) {
            file.mkdir();
        }
        if (!property2.endsWith(property)) {
            property2 = new StringBuffer().append(property2).append(property).toString();
        }
        String stringBuffer = new StringBuffer().append(property2).append("cpqma-").append(Integer.toHexString(virtdevs.UID)).append(dllext).toString();
        System.out.println(new StringBuffer().append("Checking for ").append(stringBuffer).toString());
        if (new File(stringBuffer).exists()) {
            System.out.println("DLL present");
            dio_setup = 0;
            return 0;
        }
        System.out.println("DLL not present");
        int dllExtract = dllExtract(new StringBuffer().append("com/hp/ilo2/virtdevs/cpqma-").append(str).toString(), stringBuffer);
        dio_setup = dllExtract;
        return dllExtract;
    }

    public static void cleanup(virtdevs virtdevsVar) {
        String property = System.getProperty("file.separator");
        String property2 = System.getProperty("java.io.tmpdir");
        String lowerCase = System.getProperty("os.name").toLowerCase();
        if (property2 == null) {
            property2 = lowerCase.startsWith("windows") ? "C:\\TEMP" : "/tmp";
        }
        String[] list = new File(property2).list();
        if (!property2.endsWith(property)) {
            property2 = new StringBuffer().append(property2).append(property).toString();
        }
        for (int i = 0; i < list.length; i++) {
            if (list[i].startsWith("cpqma-") && list[i].endsWith(dllext)) {
                new File(new StringBuffer().append(property2).append(list[i]).toString()).delete();
            }
        }
        for (int i2 = 0; i2 < list.length; i2++) {
            if (list[i2].startsWith("HpqKbHook-") && list[i2].endsWith(dllext)) {
                new File(new StringBuffer().append(property2).append(list[i2]).toString()).delete();
            }
        }
        for (int i3 = 0; i3 < list.length; i3++) {
            if (list[i3].startsWith("jirc_strings") && list[i3].endsWith("xml")) {
                new File(new StringBuffer().append(property2).append(list[i3]).toString()).delete();
            }
        }
    }
}
