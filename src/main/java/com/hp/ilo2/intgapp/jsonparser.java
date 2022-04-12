package com.hp.ilo2.intgapp;

import util.Http;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class jsonparser {

    private final intgapp ParentApp;

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
                System.out.println("Making JSON POST Request: " + str);
                System.out.println("json data: " + str2);
                String host = this.ParentApp.getCodeBase().getHost();

                int port = this.ParentApp.getCodeBase().getPort();
                System.out.println("chk getPort: " + port);

                if (port >= 0) {
                    str3 = ":" + port;
                } else {
                    str3 = "";
                }

                String stringBuffer = "https://" + host + str3 + "/json/" + str;
                String parameter = this.ParentApp.getParameter("RCINFO1");

                httpURLConnection = (HttpURLConnection) new URL(stringBuffer).openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setRequestProperty("Cookie", "sessionKey=" + parameter);
                httpURLConnection.connect();

                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream());
                outputStreamWriter.write(str2, 0, str2.getBytes().length);
                outputStreamWriter.flush();
                outputStreamWriter.close();

                int responseCode = httpURLConnection.getResponseCode();
                System.out.println("connect.response code =  " + responseCode);

                if (responseCode == 200) {
                    bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    str4 = "Success";
                } else {
                    bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
                }

                StringBuilder stringBuffer2 = new StringBuilder();

                while (true) {
                    String readLine = bufferedReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    stringBuffer2.append(readLine).append('\n');
                }

                System.out.println("Response Message = " + stringBuffer2);

                if (!Objects.equals(str4, "Success")) {
                    if (stringBuffer2.toString().contains("SCSI_ERR_NO_LICENSE")) {
                        str4 = "SCSI_ERR_NO_LICENSE";
                    } else {
                        str4 = stringBuffer2.toString();
                    }
                }
            } catch (Exception e) {
                String property = System.getProperty("line.separator");
                this.ParentApp.rcErrMessage = e.getMessage() + "." + property + property + "Your browser session may have timed out.";

                httpURLConnection.disconnect();

                e.printStackTrace();
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

                System.out.println("chk getPort: " + port);

                if (port >= 0) {
                    str3 = ":" + port;
                } else {
                    str3 = "";
                }

                String stringBuffer = "https://" + host + str3 + "/json/" + str;
                String parameter = this.ParentApp.getParameter("RCINFO1");

                httpsURLConnection = (HttpsURLConnection) new URL(stringBuffer).openConnection();
                httpsURLConnection.setRequestMethod("GET");
                httpsURLConnection.setDoOutput(true);
                httpsURLConnection.setUseCaches(false);
                httpsURLConnection.setSSLSocketFactory(Http.sslContext.getSocketFactory()); // ILO3RemCon addon
                httpsURLConnection.setHostnameVerifier((hostname, session) -> true); // ILO3RemCon addon
                httpsURLConnection.setRequestProperty("Cookie", "sessionKey=" + parameter);
                httpsURLConnection.connect();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
                StringBuilder stringBuffer2 = new StringBuilder();

                while (true) {
                    String readLine = bufferedReader.readLine();

                    if (readLine == null) {
                        break;
                    }

                    stringBuffer2.append(readLine).append('\n');
                }

                str2 = stringBuffer2.toString();
                httpsURLConnection.disconnect();
            } catch (Exception e) {
                String property = System.getProperty("line.separator");
                this.ParentApp.rcErrMessage = e.getMessage() + "." + property + property + "Your browser session may have timed out.";
                str2 = null;
                httpsURLConnection.disconnect();

                e.printStackTrace();
            }

            return str2;
        } catch (Throwable th) {
            httpsURLConnection.disconnect();

            throw th;
        }
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

        return "{" + substring2.substring(1, substring2.length() - 1).split("},\\{")[i] + "}";
    }
}
