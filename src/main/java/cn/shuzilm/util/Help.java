package cn.shuzilm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

public class Help {

    private static final String ALERT_USER = "houkp";

    private static final byte ALERT_LEVEL = 1;

    public static void main(String[] args) {
        InetAddress ia = null;
        try {
            ia = ia.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        String huanhang = "\r\n";
        String data = "test";
        System.out.println(ia.getHostAddress());
        System.out.println(ia.getHostName());
        System.out.println(LocalDateTime.now().toString().replace("T", " "));
        String warningData = "告警等级：低  " + huanhang + "告警主机：" + ia.getHostName() + " \n" + huanhang + " 主机 IP：" + ia.getHostAddress() + huanhang + "告警时间：" + LocalDateTime.now().toString().replace("T", " ") + huanhang + "告警业务： " + data;
        System.out.println(warningData);
        Help.sendAlert("侯克佩测试");


    }

    public static void sendAlert(String content) {
        String id = ALERT_USER;
        byte level = ALERT_LEVEL;
        String data = null;
        String huanhang = "\r\n";
        String warningData = null;
        InetAddress ia = null;
        try {
            ia = ia.getLocalHost();

            data = new String(content.getBytes(), "utf-8");
            warningData = "告警等级：低  " + " \n" + huanhang + "告警主机：" + ia.getHostName() + " \n" + huanhang + "主机 IP：" + ia.getHostAddress() + " \n" + huanhang + "告警时间：" + LocalDateTime.now().toString().replace("T", " ") + " \n" + huanhang + "告警业务： " + data;
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        String s = id + level + warningData + "YYLmfY6IRdjZMQ1";
        String md5 = md5(s);

        String url = "http://osa.shuzilm.cn/alarm";
        try {
            url += "?id=" + id + "&" + "level=" + level + "&data=" + URLEncoder.encode(warningData, "utf-8") + "&sign=" + md5;
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        URL realUrl = null;
        BufferedReader in = null;
        String result = "";
        try {
            realUrl = new URL(url);
            URLConnection connection = realUrl.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setRequestProperty("Charset", "UTF-8");
            connection.connect();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * J计算md5值
     *
     * @param data
     * @return
     */
    public static String md5(String data) {

        MessageDigest md;
        String md5 = "";
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(data.getBytes());
            md5 = bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5;
    }

    /**
     * @param md5Array
     * @return
     */
    private static String bytesToHex(byte[] md5Array) {
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < md5Array.length; i++) {
            int temp = 0xff & md5Array[i];
            String hexString = Integer.toHexString(temp);
            if (hexString.length() == 1) {
                strBuilder.append("0").append(hexString);
            } else {
                strBuilder.append(hexString);
            }
        }
        return strBuilder.toString();
    }
}
