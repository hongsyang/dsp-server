package cn.shuzilm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

public class Help {
	
	private static final String ALERT_USER = "zhangqian";
	
	private static final byte ALERT_LEVEL = 1;
	
	/**
	 * 生成订单流水号
	 * @return
	 */
	public static String generateBillsNum() {
		int rand = (int)((Math.random()*9+1)*1000000);
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat ("yyyyMMddHHmmssSSS");
		return sdf.format(d) + rand;
	}
	/**
	 * 交换键值对
	 * @param map
	 * @param rmap
	 * @return
	 */
	public static <X, Y> Map<Y, X> reverseMap(Map<X,Y> map ,Map<Y,X> rmap){
		for (Entry<X,Y> entry : map.entrySet()) { 
			rmap.put(entry.getValue(),  entry.getKey());
		}
		return rmap;
	}
	/**
	 * 时间戳 转字符时间
	 * @param unixTimeStamp
	 * @param format
	 * @return
	 */
	public static String getDate(long unixTimeStamp , String format){
	       Date date = new Date();
	       date.setTime(unixTimeStamp);
	       SimpleDateFormat ft = new SimpleDateFormat (format);
	       return ft.format(date);
	}
	/**
	 * 获取传入时间 的 当天 00:00:00 unixtimestamp
	 * @return
	 */
	public static long getStartTimeStamp(long startTime) {
		long unixtime = 0;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(startTime);
	    calendar.set(Calendar.HOUR_OF_DAY, 00);
	    calendar.set(Calendar.MINUTE, 00);
	    calendar.set(Calendar.SECOND, 00);
	    calendar.set(Calendar.MILLISECOND, 000);
	    unixtime = calendar.getTimeInMillis();
		return unixtime;
	}
	
	public static void sendAlert(String content) {
		String id =  ALERT_USER;
		byte level = ALERT_LEVEL;
		String data = null;
		try {
			data = new String(content.getBytes(),"utf-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		String s = id + level + data +"YYLmfY6IRdjZMQ1";
        String md5 = md5(s);

		String url = "http://osa.shuzilm.cn/alarm";
		try {
			url += "?id="+id+"&"+"level="+level+"&data="+URLEncoder.encode(data,"utf-8")+"&sign="+md5;
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
	        connection.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
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
	 * 
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
