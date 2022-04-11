package com.hp.ilo2.intgapp;

import util.Http;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class jsonparser {
    private intgapp ParentApp;

    public jsonparser(intgapp intgappVar) {
        this.ParentApp = intgappVar;
    }

    public String postJSONRequest(String str, String str2) {
        String str3;
        BufferedReader bufferedReader;
        HttpURLConnection httpURLConnection = null;
        String str4 = null;
        try {
            try {
                System.out.println(new StringBuffer().append("Making JSON POST Request: ").append(str).toString());
                System.out.println(new StringBuffer().append("json data: ").append(str2).toString());
                String host = this.ParentApp.getCodeBase().getHost();
                int port = this.ParentApp.getCodeBase().getPort();
                System.out.println(new StringBuffer().append("chk getPort: ").append(port).toString());
                if (port >= 0) {
                    str3 = new StringBuffer().append(":").append(Integer.toString(port)).toString();
                } else {
                    str3 = "";
                }
                String stringBuffer = new StringBuffer().append("https://").append(host).append(str3).append("/json/").append(str).toString();
                String parameter = this.ParentApp.getParameter("RCINFO1");
                httpURLConnection = (HttpURLConnection) new URL(stringBuffer).openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setRequestProperty("Cookie", new StringBuffer().append("sessionKey=").append(parameter).toString());
                httpURLConnection.connect();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream());
                outputStreamWriter.write(str2, 0, str2.getBytes().length);
                outputStreamWriter.flush();
                outputStreamWriter.close();
                int responseCode = httpURLConnection.getResponseCode();
                System.out.println(new StringBuffer().append("connect.response code =  ").append(responseCode).toString());
                if (responseCode == 200) {
                    bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    str4 = "Success";
                } else {
                    bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
                }
                StringBuffer stringBuffer2 = new StringBuffer();
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    stringBuffer2.append(new StringBuffer().append(readLine).append('\n').toString());
                }
                System.out.println(new StringBuffer().append("Response Message = ").append(stringBuffer2.toString()).toString());
                if (str4 != "Success") {
                    if (stringBuffer2.toString().indexOf("SCSI_ERR_NO_LICENSE") != -1) {
                        str4 = "SCSI_ERR_NO_LICENSE";
                    } else {
                        str4 = stringBuffer2.toString();
                    }
                }
            } catch (Exception e) {
                String property = System.getProperty("line.separator");
                this.ParentApp.rcErrMessage = new StringBuffer().append(e.getMessage()).append(".").append(property).append(property).append("Your browser session may have timed out.").toString();
                e.printStackTrace();
                httpURLConnection.disconnect();
            }
            return str4;
        } finally {
            httpURLConnection.disconnect();
        }
    }

    public String getJSONRequest(String str) {
        String str2;
        String str3;
        HttpsURLConnection httpsURLConnection = null; // ILO3RemCon modify
        try {
            try {
                String host = this.ParentApp.getCodeBase().getHost();
                int port = this.ParentApp.getCodeBase().getPort();
                System.out.println(new StringBuffer().append("chk getPort: ").append(port).toString());
                if (port >= 0) {
                    str3 = new StringBuffer().append(":").append(Integer.toString(port)).toString();
                } else {
                    str3 = "";
                }
                String stringBuffer = new StringBuffer().append("https://").append(host).append(str3).append("/json/").append(str).toString();
                String parameter = this.ParentApp.getParameter("RCINFO1");
                httpsURLConnection = (HttpsURLConnection) new URL(stringBuffer).openConnection();
                httpsURLConnection.setRequestMethod("GET");
                httpsURLConnection.setDoOutput(true);
                httpsURLConnection.setUseCaches(false);
                httpsURLConnection.setSSLSocketFactory(Http.sslContext.getSocketFactory()); // ILO3RemCon addon
                httpsURLConnection.setHostnameVerifier((hostname, session) -> true); // ILO3RemCon addon
                httpsURLConnection.setRequestProperty("Cookie", new StringBuffer().append("sessionKey=").append(parameter).toString());
                httpsURLConnection.connect();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
                StringBuffer stringBuffer2 = new StringBuffer();
                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    stringBuffer2.append(new StringBuffer().append(readLine).append('\n').toString());
                }
                str2 = stringBuffer2.toString();
                httpsURLConnection.disconnect();
            } catch (Exception e) {
                String property = System.getProperty("line.separator");
                this.ParentApp.rcErrMessage = new StringBuffer().append(e.getMessage()).append(".").append(property).append(property).append("Your browser session may have timed out.").toString();
                e.printStackTrace();
                str2 = null;
                httpsURLConnection.disconnect();
            }
            return str2;
        } catch (Throwable th) {
            httpsURLConnection.disconnect();
            throw th;
        }
    }

    public String getJSONObject(String str, String str2) {
        String trim = str.trim();
        String substring = trim.substring(1, trim.indexOf("}") + 1);
        return substring.substring(substring.indexOf("{"));
    }

    public int getJSONNumber(String str, String str2) {
        String trim = str.trim();
        for (String str3 : trim.substring(1, trim.length() - 1).split(",")) {
            String[] split = str3.split(":");
            String trim2 = split[0].trim();
            if (trim2.substring(1, trim2.length() - 1).compareToIgnoreCase(str2) == 0) {
                return Integer.parseInt(split[1].trim());
            }
        }
        return 0;
    }

    public String getJSONArray(String str, String str2, int i) {
        String trim = str.trim();
        String substring = trim.substring(trim.indexOf("[") + 1);
        String substring2 = substring.substring(0, substring.indexOf("]") + 1);
        return new StringBuffer().append("{").append(substring2.substring(1, substring2.length() - 1).split("\\},\\{")[i]).append("}").toString();
    }
}
