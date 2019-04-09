package cn.shuzilm.interf.pixcel;

import cn.shuzilm.common.redis.RedisQueueManager;
import cn.shuzilm.interf.pixcel.parser.BaiduImpParameterParserImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description:    腾讯解析器
 * @Author:         houkp
 * @CreateDate:     2019/1/15 19:09
 * @UpdateUser:     houkp
 * @UpdateDate:     2019/1/15 19:09
 * @UpdateRemark:   修改内容
 * @Version:        1.0
 */
public class BaiduRedisTask implements Runnable {


    private static final Logger log = LoggerFactory.getLogger(BaiduRedisTask.class);

    @Override
    public void run() {
        while (true) {
            try {
                Object baiduimp = RedisQueueManager.getElementFromQueue("baiduimp");
                if (baiduimp != null) {
                    log.debug("线程号" + Thread.currentThread().getName());
                    BaiduImpParameterParserImpl.parseUrlStr(baiduimp.toString());
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

