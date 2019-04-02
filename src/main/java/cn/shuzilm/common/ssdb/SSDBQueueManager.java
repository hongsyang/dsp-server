package cn.shuzilm.common.ssdb;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import cn.shuzilm.backend.master.MsgControlCenter;
import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdPixelBean;
import cn.shuzilm.bean.control.AdPropertyBean;
import cn.shuzilm.bean.control.AdvertiserBean;
import cn.shuzilm.bean.control.CreativeBean;
import cn.shuzilm.bean.control.Material;
import cn.shuzilm.bean.control.NodeStatusBean;
import cn.shuzilm.bean.control.TaskBean;
import cn.shuzilm.bean.dmp.AudienceBean;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * jedis队列
 * User: weichun.zhan
 * Date: 18-6-30
 * Time: 下午12:23
 */
public class SSDBQueueManager {

    private static final Logger logger = LoggerFactory.getLogger(SSDBQueueManager.class);

    private static SSDBManager manager = SSDBManager.getInstance();
    public static void init(){
        manager = SSDBManager.getInstance();
    }
    
	/**
	 * 向redis中存入数据
	 * @param key
	 * @param value
	 */
	public static void putDate(String key, String value) {
	        
	        Jedis jedis = null;
	        try {
	            jedis = manager.getResource();
	             jedis.set(key, value);
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            manager.returnResource(jedis);
	        }
	    }
	    
    /**
     * 从redis中获取数据
     * @param key
     * @return
     */
    public static String getDate(String key) {
        
        Jedis jedis = null;
        try {
            jedis = manager.getResource();
             return jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            manager.returnResource(jedis);
        }
		return null;
    }
    
}
