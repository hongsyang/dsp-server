package cn.shuzilm.interf.pixcel;

import cn.shuzilm.common.redis.RedisQueueManager;
import cn.shuzilm.interf.pixcel.parser.AdViewExpParameterParserImpl;
import cn.shuzilm.interf.pixcel.parser.AdViewNurlParameterParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @Description:   快友解析器
* @Author:         houkp
* @CreateDate:     2019/1/15 19:09
* @UpdateUser:     houkp
* @UpdateDate:     2019/1/15 19:09
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
public class AdviewRedisTask implements Runnable {


    private static final Logger log = LoggerFactory.getLogger(AdviewRedisTask.class);

    @Override
    public void run() {
        while (true) {
            try {
                Object adviewnurl = RedisQueueManager.getElementFromQueue("adviewnurl");
                Object adviewexp = RedisQueueManager.getElementFromQueue("adviewexp");
                if (adviewnurl != null) {
                    log.debug("线程号" + Thread.currentThread().getName());
                    AdViewNurlParameterParserImpl.parseUrlStr(adviewnurl.toString());
                } else if (adviewexp != null) {
                    log.debug("线程号" + Thread.currentThread().getName());
                    AdViewExpParameterParserImpl.parseUrlStr(adviewexp.toString());
                } else {
                    Thread.currentThread().join(1000);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}

