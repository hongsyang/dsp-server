package cn.shuzilm.interf.pixcel;

import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.interf.pixcel.parser.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description: 曝光解析不结费
 * @Author: houkp
 * @CreateDate: 2019/1/15 19:09
 * @UpdateUser: houkp
 * @UpdateDate: 2019/1/15 19:09
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class LingJiRedisTask implements Runnable {


    private static final Logger log = LoggerFactory.getLogger(LingJiRedisTask.class);

    @Override
    public void run() {
        while (true) {
            try {
                Object lingjiexp = JedisQueueManager.getElementFromQueue("lingjiexp");
                Object lingjiimp = JedisQueueManager.getElementFromQueue("lingjiimp");
                if (lingjiexp != null) {
                    log.debug("线程号" + Thread.currentThread().getName());
                    LingJiExpParameterParserImpl.parseUrlStr(lingjiexp.toString());
                } else if (lingjiimp != null) {
                    log.debug("线程号" + Thread.currentThread().getName());
                    LingJiImpParameterParserImpl.parseUrlStr(lingjiimp.toString());
                } else {
//                    log.debug("等待中" + Thread.currentThread().getName());
                    Thread.currentThread().join(1000);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}

