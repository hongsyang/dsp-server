package cn.shuzilm.util;

import java.net.*;
import java.util.Enumeration;

public class NetUtil {
    public static InetAddress getInetAddress() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println("unknown host!");
        }
        return null;

    }

    public static String getHostIp() {
        InetAddress netAddress = getInetAddress();
        if (null == netAddress) {
            return null;
        }
        String ip = netAddress.getHostAddress(); // get the ip address
        return ip;
    }

    public static String getHostName() {
        InetAddress netAddress = getInetAddress();
        if (null == netAddress) {
            return null;
        }
        String name = netAddress.getHostName(); // get the host address
        return name;
    }

    public static String getPPP0IP() {
        Enumeration allNetInterfaces = null;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        InetAddress ip = null;
        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
            if ("PPP0".equalsIgnoreCase(netInterface.getName())) {
                Enumeration addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = (InetAddress) addresses.nextElement();
                    if (ip != null && ip instanceof Inet4Address) {
                        return ip.getHostAddress();
                    }
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(NetUtil.getHostIp());
//        System.out.println(NetUtil.getHostName());
    }
}
