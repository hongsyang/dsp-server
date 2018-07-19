package cn.shuzilm.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Constants
{
    private static Properties p = null;
    private Constants()
    {
        try
        {
            p = new Properties();
            InputStream fis = Constants.class.getClassLoader().getResourceAsStream("configs.properties");
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
                    }
                    catch (IOException localIOException2)
                    {
                        localIOException2.printStackTrace();
                    }
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private static Constants con;

    public static Constants getInstance(){
        if(con == null){
            con =  new Constants();
            return con;
        }else{
            return con;
        }
    }

    public String getConf(String key){
        return p.getProperty(key);
    }


}