import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

public class PropertiesTest {
    private static Properties prop = new Properties();

    public static void main(String[] args) throws Exception {
        String FileName = "C:\\Users\\houkp\\Desktop\\test\\rtb.properties";
        //读取属性文件a.properties
        InputStream in = new BufferedInputStream(new FileInputStream(FileName));
        ///加载属性列表
        prop.load(in);
        Iterator<String> it=prop.stringPropertyNames().iterator();
        while (it.hasNext()) {
            String key = it.next();
            System.out.println(key + ":" + prop.getProperty(key));
        }
        in.close();

    }
}
