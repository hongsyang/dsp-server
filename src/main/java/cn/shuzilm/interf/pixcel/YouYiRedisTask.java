package cn.shuzilm.interf.pixcel;

import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.interf.pixcel.parser.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
* @Description:    悠易解析器
* @Author:         houkp
* @CreateDate:     2019/1/15 19:09
* @UpdateUser:     houkp
* @UpdateDate:     2019/1/15 19:09
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
public class YouYiRedisTask implements Runnable {


    private static final Logger log = LoggerFactory.getLogger(YouYiRedisTask.class);

    @Override
    public void run() {
        while (true) {
            try {
                Object youyiexp = JedisQueueManager.getElementFromQueue("youyiexp");
                Object youyiimp = JedisQueueManager.getElementFromQueue("youyiimp");
                if (youyiimp != null) {
                    log.debug("线程号" + Thread.currentThread().getName());
                    YouYiImpParameterParserImpl.parseUrlStr(youyiimp.toString());
                } else if (youyiexp != null) {
                    log.debug("线程号" + Thread.currentThread().getName());
                    YouYiExpParameterParserImpl.parseUrlStr(youyiexp.toString());
                }  else {
//                    log.debug("等待中" + Thread.currentThread().getName());
                    Thread.currentThread().join(1000);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}

