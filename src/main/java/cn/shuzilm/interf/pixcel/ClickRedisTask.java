package cn.shuzilm.interf.pixcel;

import cn.shuzilm.common.redis.RedisQueueManager;
import cn.shuzilm.interf.pixcel.parser.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @Description:    点击解析
* @Author:         houkp
* @CreateDate:     2019/1/15 19:08
* @UpdateUser:     houkp
* @UpdateDate:     2019/1/15 19:08
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
public class ClickRedisTask implements Runnable {


    private static final Logger log = LoggerFactory.getLogger(YouYiExpParameterParserImpl.class);

    @Override
    public void run() {
        while (true) {
            try {
                Object adviewclick = RedisQueueManager.getElementFromQueue("adviewclick");
                Object youyiclick = RedisQueueManager.getElementFromQueue("youyiclick");
                Object lingjiclick = RedisQueueManager.getElementFromQueue("lingjiclick");
                Object tencentclick = RedisQueueManager.getElementFromQueue("tencentclick");
                if (adviewclick != null) {
                    log.debug("线程号" + Thread.currentThread().getName());
                    AdViewClickParameterParserImpl.parseUrlStr(adviewclick.toString());
                } else if (youyiclick != null) {
                    log.debug("线程号" + Thread.currentThread().getName());
                    YouYiClickParameterParserImpl.parseUrlStr(youyiclick.toString());
                } else if (lingjiclick != null) {
                    log.debug("线程号" + Thread.currentThread().getName());
                    LingJiClickParameterParserImpl.parseUrlStr(lingjiclick.toString());
                } else if (tencentclick != null) {
                    log.debug("线程号" + Thread.currentThread().getName());
                    TencentClickParameterParserImpl.parseUrlStr(tencentclick.toString());
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

