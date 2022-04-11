package com.hp.ilo2.intgapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class locinfo {
    public static int UID;
    private intgapp ParentApp;
    private String lstrVersion;
    public String rcErrMessage;
    public static final int MENUSTR_1001 = 4097;
    public static final int MENUSTR_1002 = 4098;
    public static final int MENUSTR_1003 = 4099;
    public static final int MENUSTR_1004 = 4100;
    public static final int MENUSTR_1005 = 4101;
    public static final int MENUSTR_1006 = 4102;
    public static final int MENUSTR_1007 = 4103;
    public static final int MENUSTR_1008 = 4104;
    public static final int MENUSTR_1009 = 4105;
    public static final int MENUSTR_100A = 4106;
    public static final int MENUSTR_100B = 4107;
    public static final int MENUSTR_100C = 4108;
    public static final int MENUSTR_100D = 4109;
    public static final int MENUSTR_100E = 4110;
    public static final int MENUSTR_100F = 4111;
    public static final int MENUSTR_1010 = 4112;
    public static final int MENUSTR_1011 = 4113;
    public static final int MENUSTR_1012 = 4114;
    public static final int MENUSTR_1013 = 4115;
    public static final int MENUSTR_1014 = 4116;
    public static final int MENUSTR_1015 = 4117;
    public static final int MENUSTR_1016 = 4118;
    public static final int MENUSTR_1017 = 4119;
    public static final int MENUSTR_1018 = 4120;
    public static final int MENUSTR_1019 = 4121;
    public static final int MENUSTR_101A = 4122;
    public static final int MENUSTR_101B = 4123;
    public static final int MENUSTR_101C = 4124;
    public static final int MENUSTR_101D = 4125;
    public static final int MENUSTR_101E = 4126;
    public static final int MENUSTR_101F = 4127;
    public static final int MENUSTR_1020 = 4128;
    public static final int MENUSTR_1021 = 4129;
    public static final int MENUSTR_1022 = 4130;
    public static final int MENUSTR_1023 = 4131;
    public static final int MENUSTR_1024 = 4132;
    public static final int MENUSTR_1025 = 4133;
    public static final int MENUSTR_1026 = 4134;
    public static final int MENUSTR_1027 = 4135;
    public static final int MENUSTR_1028 = 4136;
    public static final int MENUSTR_1029 = 4137;
    public static final int DIALOGSTR_2001 = 8193;
    public static final int DIALOGSTR_2002 = 8194;
    public static final int DIALOGSTR_2003 = 8195;
    public static final int DIALOGSTR_2004 = 8196;
    public static final int DIALOGSTR_2005 = 8197;
    public static final int DIALOGSTR_2006 = 8198;
    public static final int DIALOGSTR_2007 = 8199;
    public static final int DIALOGSTR_2008 = 8200;
    public static final int DIALOGSTR_2009 = 8201;
    public static final int DIALOGSTR_200a = 8202;
    public static final int DIALOGSTR_200b = 8203;
    public static final int DIALOGSTR_200c = 8204;
    public static final int DIALOGSTR_200d = 8205;
    public static final int DIALOGSTR_200e = 8206;
    public static final int DIALOGSTR_200f = 8207;
    public static final int DIALOGSTR_2010 = 8208;
    public static final int DIALOGSTR_2011 = 8209;
    public static final int DIALOGSTR_2012 = 8210;
    public static final int DIALOGSTR_2013 = 8211;
    public static final int DIALOGSTR_2014 = 8212;
    public static final int DIALOGSTR_2015 = 8213;
    public static final int DIALOGSTR_2016 = 8214;
    public static final int DIALOGSTR_2017 = 8215;
    public static final int DIALOGSTR_2021 = 8225;
    public static final int DIALOGSTR_2022 = 8226;
    public static final int DIALOGSTR_2023 = 8227;
    public static final int DIALOGSTR_2024 = 8228;
    public static final int DIALOGSTR_2025 = 8229;
    public static final int DIALOGSTR_2026 = 8230;
    public static final int DIALOGSTR_2027 = 8231;
    public static final int DIALOGSTR_2028 = 8232;
    public static final int DIALOGSTR_2029 = 8233;
    public static final int DIALOGSTR_202a = 8234;
    public static final int DIALOGSTR_202b = 8235;
    public static final int DIALOGSTR_202c = 8236;
    public static final int DIALOGSTR_202d = 8237;
    public static final int DIALOGSTR_202e = 8238;
    public static final int DIALOGSTR_202f = 8239;
    public static final int DIALOGSTR_2030 = 8240;
    public static final int DIALOGSTR_2031 = 8241;
    public static final int DIALOGSTR_2032 = 8242;
    public static final int DIALOGSTR_2033 = 8243;
    public static final int DIALOGSTR_2034 = 8244;
    public static final int DIALOGSTR_2035 = 8245;
    public static final int DIALOGSTR_2036 = 8246;
    public static final int DIALOGSTR_2037 = 8247;
    public static final int DIALOGSTR_2038 = 8248;
    public static final int DIALOGSTR_2039 = 8249;
    public static final int DIALOGSTR_203a = 8250;
    public static final int DIALOGSTR_203b = 8251;
    public static final int DIALOGSTR_203c = 8252;
    public static final int DIALOGSTR_203d = 8253;
    public static final int DIALOGSTR_203e = 8254;
    public static final int DIALOGSTR_203f = 8255;
    public static final int DIALOGSTR_2040 = 8256;
    public static final int DIALOGSTR_2041 = 8257;
    public static final int DIALOGSTR_2042 = 8258;
    public static final int DIALOGSTR_2043 = 8259;
    public static final int DIALOGSTR_2044 = 8260;
    public static final int DIALOGSTR_2045 = 8261;
    public static final int DIALOGSTR_2046 = 8262;
    public static final int DIALOGSTR_2047 = 8263;
    public static final int DIALOGSTR_2048 = 8264;
    public static final int DIALOGSTR_2049 = 8265;
    public static final int DIALOGSTR_205a = 8282;
    public static final int DIALOGSTR_205b = 8283;
    public static final int DIALOGSTR_205c = 8284;
    public static final int DIALOGSTR_205d = 8285;
    public static final int DIALOGSTR_205e = 8286;
    public static final int DIALOGSTR_205f = 8287;
    public static final int DIALOGSTR_2060 = 8288;
    public static final int DIALOGSTR_2061 = 8289;
    public static final int DIALOGSTR_2062 = 8290;
    public static final int DIALOGSTR_2063 = 8291;
    public static final int DIALOGSTR_2064 = 8292;
    public static final int STATUSSTR_3001 = 12289;
    public static final int STATUSSTR_3002 = 12290;
    public static final int STATUSSTR_3003 = 12291;
    public static final int STATUSSTR_3004 = 12292;
    public static final int STATUSSTR_3005 = 12293;
    public static final int STATUSSTR_3006 = 12294;
    public static final int STATUSSTR_3007 = 12295;
    public static final int STATUSSTR_3008 = 12296;
    public static final int STATUSSTR_3009 = 12297;
    public static final int STATUSSTR_300a = 12298;
    public static final int STATUSSTR_300b = 12299;
    public static final int STATUSSTR_300c = 12300;
    public static final int STATUSSTR_300d = 12301;
    public static final int STATUSSTR_300e = 12302;
    public static final int STATUSSTR_300f = 12303;
    public static final int STATUSSTR_3010 = 12304;
    public static final int STATUSSTR_3011 = 12305;
    public static final int STATUSSTR_3012 = 12306;
    public static final int STATUSSTR_3013 = 12307;
    public static final int STATUSSTR_3014 = 12308;
    public static final int STATUSSTR_3015 = 12309;
    public static final int STATUSSTR_3016 = 12310;
    public static final int STATUSSTR_3100 = 12544;
    public static final int STATUSSTR_3101 = 12545;
    public static final int STATUSSTR_3102 = 12546;
    public static final int STATUSSTR_3103 = 12547;
    public static final int STATUSSTR_3104 = 12548;
    public static final int STATUSSTR_3105 = 12549;
    public static final int STATUSSTR_3106 = 12550;
    public static final int STATUSSTR_3107 = 12551;
    public static final int STATUSSTR_3108 = 12552;
    public static final int STATUSSTR_3109 = 12553;
    public static final int STATUSSTR_310a = 12554;
    public static final int STATUSSTR_310b = 12555;
    public static final int STATUSSTR_310c = 12556;
    public static final int STATUSSTR_310d = 12557;
    public static final int STATUSSTR_310e = 12558;
    public static final int STATUSSTR_310f = 12559;
    public static final int STATUSSTR_3110 = 12560;
    public static final int STATUSSTR_3111 = 12561;
    public static final int STATUSSTR_3112 = 12562;
    public static final int STATUSSTR_3113 = 12563;
    public static final int STATUSSTR_3114 = 12564;
    public static final int STATUSSTR_3115 = 12565;
    public static final int STATUSSTR_3116 = 12566;
    public static final int STATUSSTR_3117 = 12567;
    public static final int STATUSSTR_3118 = 12568;
    public static final int STATUSSTR_3119 = 12569;
    public static final int STATUSSTR_3120 = 12576;
    public static final int STATUSSTR_3121 = 12577;
    public static final int STATUSSTR_3122 = 12578;
    public static final int STATUSSTR_3123 = 12579;
    public static final int STATUSSTR_3124 = 12580;
    public static final int STATUSSTR_3125 = 12581;
    public static final int STATUSSTR_3126 = 12582;
    public static final int TOOLSTR_4001 = 16385;
    public static final int TOOLSTR_4002 = 16386;
    public static final int TOOLSTR_4003 = 16387;
    public static final int TOOLSTR_4004 = 16388;
    private DocumentBuilderFactory dbf = null;
    private DocumentBuilder db = null;
    private Document document = null;
    private File file = null;
    private String localLocStrFile = "";

    public boolean retrieveLocStrings(boolean bl) {
        boolean bl2;
        block17: {
            FileOutputStream fileOutputStream;
            int n;
            Object object;
            Object object2;
            File file;
            HttpURLConnection httpURLConnection = null;
            String string = null;
            String string2 = null;
            Object var5_5 = null;
            String string3 = null;
            URL uRL = null;
            int n2 = 0;
            String string4 = System.getProperty("java.io.tmpdir");
            String string5 = System.getProperty("os.name").toLowerCase();
            String string6 = System.getProperty("file.separator");
            bl2 = false;
            String string7 = "com/hp/ilo2/intgapp/";
            String string8 = "jirc_strings";
            String string9 = ".xml";
            String string10 = this.ParentApp.getParameter("RCINFOLANG");
            String string11 = null;
            if (UID == 0) {
                UID = this.hashCode();
            }
            String string12 = Integer.toHexString(UID);
            if (null != string10 && !string10.equalsIgnoreCase("")) {
                System.out.println("langStr received:" + string10);
                string11 = "lang/" + string10 + "/jirc_strings.xml";
                System.out.println("lolcalized xml file shoudl be:" + string11);
            } else {
                bl = false;
            }
            if (string4 == null) {
                String string13 = string4 = string5.startsWith("windows") ? "C:\\TEMP" : "/tmp";
            }
            if (!(file = new File(string4)).exists()) {
                file.mkdir();
            }
            if (!string4.endsWith(string6)) {
                string4 = string4 + string6;
            }
            this.localLocStrFile = string4 = string4 + string8 + string12 + string9;
            File file2 = new File(string4);
            if (file2.exists()) {
                System.out.println(this.localLocStrFile + " already exists.");
                bl2 = true;
                return bl2;
            }
            byte[] byArray = new byte[4096];
            System.out.println("Creating" + this.localLocStrFile + "...");
            if (null != string11 && bl) {
                try {
                    try {
                        System.out.println("try localize file from webserver..");
                        string = this.ParentApp.getCodeBase().getHost();
                        n2 = this.ParentApp.getCodeBase().getPort();
                        string2 = n2 >= 0 ? ":" + Integer.toString(n2) : "";
                        string3 = "http://" + string + string2 + "/" + string11;
                        System.out.println("trying to retreive webser localize file:" + string3);
                        uRL = new URL(string3);
                        httpURLConnection = null;
                        httpURLConnection = (HttpURLConnection)uRL.openConnection();
                        httpURLConnection.setRequestMethod("GET");
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setUseCaches(false);
                        httpURLConnection.connect();
                        object2 = httpURLConnection.getInputStream();
                        object = new FileOutputStream(this.localLocStrFile);
                        while ((n = ((InputStream)object2).read(byArray, 0, 4096)) != -1) {
                            ((FileOutputStream)object).write(byArray, 0, n);
                        }
                        System.out.println("Write xml to" + this.localLocStrFile + "complete");
                        ((InputStream)object2).close();
                        ((FileOutputStream)object).close();
                        bl2 = true;
                        System.out.println("Message after comp of webserver retrieval");
                    }
                    catch (Exception exception) {
                        object = System.getProperty("line.separator");
                        this.rcErrMessage = exception.getMessage() + "." + (String)object + (String)object + "Your browser session may have timed out.";
                        exception.printStackTrace();
                        fileOutputStream = null;
                        httpURLConnection.disconnect();
                        httpURLConnection = null;
                    }
                    fileOutputStream = null;
                    httpURLConnection.disconnect();
                    httpURLConnection = null;
                }
                catch (Throwable throwable) {
                    Object var26_27 = null;
                    httpURLConnection.disconnect();
                    httpURLConnection = null;
                    throw throwable;
                }
            }
            if (bl2 && bl) break block17;
            System.out.println("try localize file from applet..");
            object2 = this.getClass().getClassLoader();
            object = string7 + string8 + string9;
            try {
                InputStream inputStream = ((ClassLoader)object2).getResourceAsStream((String)object);
                fileOutputStream = new FileOutputStream(this.localLocStrFile);
                while ((n = inputStream.read(byArray, 0, 4096)) != -1) {
                    fileOutputStream.write(byArray, 0, n);
                }
                inputStream.close();
                fileOutputStream.close();
                bl2 = true;
                System.out.println("Message after default xml initialization");
            }
            catch (IOException iOException) {
                System.out.println("xmlExtract: " + iOException);
                this.rcErrMessage = iOException.getMessage();
                iOException.printStackTrace();
            }
        }
        return bl2;
    }

    public locinfo(intgapp intgappVar) {
        this.lstrVersion = "0001";
        this.rcErrMessage = "";
        this.ParentApp = intgappVar;
        this.lstrVersion = "0001";
        this.rcErrMessage = "";
    }

    public boolean initLocStringsDefault() {
        boolean z = false;
        int i = 0;
        try {
            System.out.println(new StringBuffer().append("Message from beginning of initLocStringsDefault").append(this.localLocStrFile).toString());
            z = retrieveLocStrings(false);
            if (false == z) {
                i = 2;
            } else {
                this.file = new File(this.localLocStrFile);
                if (null == this.file) {
                    i = 3;
                } else {
                    this.dbf = DocumentBuilderFactory.newInstance();
                    if (null == this.dbf) {
                        i = 4;
                    } else {
                        this.db = this.dbf.newDocumentBuilder();
                        if (null == this.db) {
                            i = 5;
                        } else {
                            this.document = this.db.parse(this.file);
                            if (null == this.document) {
                                i = 6;
                            } else {
                                this.document.getDocumentElement().normalize();
                                z = true;
                                System.out.println("Message after completion of initLocStringsDefault");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            String property = System.getProperty("line.separator");
            this.rcErrMessage = new StringBuffer().append(e.getMessage()).append(".").append(property).append(property).append("Could not Parse the localization strings.").toString();
            e.printStackTrace();
        }
        if (false == z) {
            System.out.println(new StringBuffer().append("initLocStringsDefault:Error Parsing Xml file:%d").append(i).toString());
        }
        return z;
    }

    public boolean initLocStrings() {
        boolean z = false;
        int i = 0;
        try {
            System.out.println(new StringBuffer().append("Message from beginning of initLocStrings").append(this.localLocStrFile).toString());
            if (null != this.document) {
                i = 1;
            } else {
                z = retrieveLocStrings(true);
                if (false == z) {
                    i = 2;
                } else {
                    this.file = new File(this.localLocStrFile);
                    if (null == this.file) {
                        i = 3;
                    } else {
                        this.dbf = DocumentBuilderFactory.newInstance();
                        if (null == this.dbf) {
                            i = 4;
                        } else {
                            this.db = this.dbf.newDocumentBuilder();
                            if (null == this.db) {
                                i = 5;
                            } else {
                                this.document = this.db.parse(this.file);
                                if (null == this.document) {
                                    i = 6;
                                } else {
                                    this.document.getDocumentElement().normalize();
                                    z = true;
                                    System.out.println("Message after completion of initLocStrings");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            String property = System.getProperty("line.separator");
            this.rcErrMessage = new StringBuffer().append(e.getMessage()).append(".").append(property).append(property).append("Could not Parse the localization strings.").toString();
            e.printStackTrace();
        }
        if (false == z) {
            z = initLocStringsDefault();
        }
        if (false == z) {
            System.out.println(new StringBuffer().append("Error Parsing Xml file:%d").append(i).toString());
        }
        return z;
    }

    public String getLocString(int i) {
        boolean z = false;
        String stringBuffer = new StringBuffer().append("ID_").append(Integer.toHexString(i)).toString();
        String str = "";
        int i2 = 0;
        try {
            if (null == this.document) {
                i2 = 1;
            } else {
                Element elementById = this.document.getElementById(stringBuffer);
                if (null == elementById) {
                    i2 = 2;
                } else {
                    NodeList childNodes = elementById.getChildNodes();
                    if (null == childNodes) {
                        i2 = 3;
                    } else {
                        str = childNodes.item(0).getNodeValue();
                        if (null == str) {
                            i2 = 4;
                        } else {
                            z = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (false == z) {
            str = "LS_NF";
            System.out.println(new StringBuffer().append("LSFNound:").append(stringBuffer).append("rval:").append(i2).toString());
        }
        return str;
    }

    public void dumpLocStrings() {
        try {
            NodeList elementsByTagName = this.document.getElementsByTagName("javaIRC");
            for (int i = 0; i < elementsByTagName.getLength(); i++) {
                Node item = elementsByTagName.item(i);
                if (item.getNodeType() == 1) {
                    Element element = (Element) item;
                    NodeList elementsByTagName2 = element.getElementsByTagName("menu");
                    for (int i2 = 0; i2 < elementsByTagName2.getLength(); i2++) {
                        ((Element) elementsByTagName2.item(i2)).getChildNodes();
                    }
                    NodeList elementsByTagName3 = element.getElementsByTagName("dialog");
                    for (int i3 = 0; i3 < elementsByTagName3.getLength(); i3++) {
                        ((Element) elementsByTagName3.item(i3)).getChildNodes();
                    }
                    NodeList elementsByTagName4 = element.getElementsByTagName("status");
                    for (int i4 = 0; i4 < elementsByTagName4.getLength(); i4++) {
                        ((Element) elementsByTagName4.item(i4)).getChildNodes();
                    }
                    NodeList elementsByTagName5 = element.getElementsByTagName("tooltip");
                    for (int i5 = 0; i5 < elementsByTagName5.getLength(); i5++) {
                        ((Element) elementsByTagName5.item(i5)).getChildNodes();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
