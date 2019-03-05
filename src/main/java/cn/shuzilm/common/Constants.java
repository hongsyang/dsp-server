package cn.shuzilm.common;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Constants {
    private static Properties p = new Properties();

    private Constants() {
        try {
            //测试环境
//            String FileName = "C:\\Users\\houkp\\Desktop\\test\\rtb.properties";
            //正式环境
            String FileName = "/home/srvadmin/dsp/rtb.properties";
            //读取属性文件a.properties
            InputStream fis = new BufferedInputStream(new FileInputStream(FileName));
//            InputStream fis = Constants.class.getClassLoader().getResourceAsStream("rtb.properties");
            try {
                p.load(fis);
            } catch (IOException e) {
                e.printStackTrace();

                throw new NullPointerException("Failed to load config file: config.properties, error: " + e
                        .getMessage());
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException localIOException2) {
                        localIOException2.printStackTrace();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static Constants con;

    public static Constants getInstance() {
        if (con == null) {
            con = new Constants();
            return con;
        } else {
            return con;
        }
    }

    public String getConf(String key) {
        return p.getProperty(key);
    }


}