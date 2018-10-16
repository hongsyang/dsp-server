package cn.shuzilm.common;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PixelConstants
{
    private static Properties p = null;
    private PixelConstants()
    {
        try
        {
            p = new java.util.Properties();
            InputStream fis = PixelConstants.class.getClassLoader().getResourceAsStream("pixel.properties");
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

    private static PixelConstants con;

    public static PixelConstants getInstance(){
        if(con == null){
            con =  new PixelConstants();
            return con;
        }else{
            return con;
        }
    }

    public String getConf(String key){
        return p.getProperty(key);
    }


}