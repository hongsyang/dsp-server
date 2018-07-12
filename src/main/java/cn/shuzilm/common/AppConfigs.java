package cn.shuzilm.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: weichun.zhan
 * Date: 18-6-7
 * Time: 下午12:23
 */
public class AppConfigs {
    private final static Logger log = LoggerFactory.getLogger(AppConfigs.class);
    private static HashMap<String,AppConfigs> instance = new HashMap<String, AppConfigs>();
    private Map<String, String> configs;
    public static String DEFAULT = "configs.properties";
    private String configFile ;

    private static final Object lock = new Object();

    private AppConfigs(String name) {

        configFile = name;
        init(name);
    }
    public static AppConfigs getInstance() {
        if (!instance.containsKey(DEFAULT)) {
            synchronized (lock) {
                AppConfigs conf = new AppConfigs(DEFAULT);
                instance.put(DEFAULT,conf);
            }
        }
        return instance.get(DEFAULT);
    }
    public AppConfigs getByName(){
        return instance.get(DEFAULT);
    }

    public AppConfigs getByName(String name){
        return instance.get(name);
    }
    public static AppConfigs getInstance(String name) {
        if (!instance.containsKey(name)) {
            synchronized (lock) {
                AppConfigs conf = new AppConfigs(name);
                instance.put(name,conf);
            }
        }
        return instance.get(name);
    }

    public synchronized void reload() {
        Map<String, String> configsTemp = new ConcurrentHashMap<String, String>();
        log.debug("reload AppConfigs");
        Properties props = new Properties();
        try {
            URL url = this.getClass().getClassLoader().getResource(configFile);
            InputStream in = url.openStream();
            props.load(in);
            Enumeration en = props.propertyNames();
            while (en.hasMoreElements()) {
                String key = (String) en.nextElement();
                String value = props.getProperty(key);
                configsTemp.put(key, value);
            }
        } catch (Exception e) {
            log.error("reload AppConfigs error:" + e.getMessage());
            return;
        }
        configs = configsTemp;
    }

    public void init(String name) {
        log.debug("init AppConfigs");
        Properties props = new Properties();
        try {
            URL url = this.getClass().getClassLoader().getResource(name);
            InputStream in = url.openStream();
            props.load(in);
            Enumeration en = props.propertyNames();
            configs = new ConcurrentHashMap<String, String>();
            while (en.hasMoreElements()) {
                String key = (String) en.nextElement();
                String value = props.getProperty(key);
                configs.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("init AppConfigs error:" + e.getMessage());
        }
    }

    public Map<String, String> getConfigs() {
        return configs;
    }

    public String getString(String key) {
        return configs.get(key);
    }

    public Integer getInt(String key){
        return  Integer.valueOf(configs.get(key));
    }

    public String get(String key,String defaultStr) {
        String result = configs.get(key);
        if(result == null){
            result =  defaultStr;
        }
        return result;
    }
}
