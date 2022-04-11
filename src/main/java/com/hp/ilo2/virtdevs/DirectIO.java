package com.hp.ilo2.virtdevs;


public class DirectIO {
    public int media_type;
    public int StartCylinder;
    public int EndCylinder;
    public int StartHead;
    public int EndHead;
    public int Cylinders;
    public int TracksPerCyl;
    public int SecPerTrack;
    public int BytesPerSec;
    public int media_size;
    public int filehandle = -1;
    public int aux_handle = -1;
    public long bufferaddr;
    public int wp;
    public int misc0;
    public int PhysicalDevice;
    public static int keydrive;

    public native int open(String str);

    public native int close();

    public native int read(long j, int i, byte[] bArr);

    public native int write(long j, int i, byte[] bArr);

    public native long size();

    public native int format();

    public native String[] devices();

    public native int devtype(String str);

    public native int scsi(byte[] bArr, int i, int i2, byte[] bArr2, byte[] bArr3, int i3);

    public native String sysError(int i);

    protected void finalize() {
        if (this.filehandle != -1) {
            close();
        }
    }

    static {
        keydrive = 1;
        String stringBuffer = "cpqma-" + Integer.toHexString(virtdevs.UID) + MediaAccess.dllext;
        String property = System.getProperty("file.separator");
        String property2 = System.getProperty("java.io.tmpdir");
        String lowerCase = System.getProperty("os.name").toLowerCase();
        if (property2 == null) {
            property2 = lowerCase.startsWith("windows") ? "C:\\TEMP" : "/tmp";
        }
        if (!property2.endsWith(property)) {
            property2 = property2 + property;
        }
        String stringBuffer2 = property2 + stringBuffer;
        String property3 = virtdevs.prop.getProperty("com.hp.ilo2.virtdevs.dll");
        keydrive = Boolean.valueOf(virtdevs.prop.getProperty("com.hp.ilo2.virtdevs.keydrive", "true")) ? 1 : 0;
        if (property3 != null) {
            stringBuffer2 = property3;
        }
        System.out.println("Loading " + stringBuffer2);
        System.load(stringBuffer2);
    }
}
