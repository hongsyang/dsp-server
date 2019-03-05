package cn.shuzilm.interf.pixcel;

import cn.shuzilm.common.redis.RedisQueueManager;
import cn.shuzilm.interf.pixcel.parser.TencentExpParameterParserImpl;
import cn.shuzilm.interf.pixcel.parser.TencentImpParameterParserImpl;
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
public class TencentRedisTask implements Runnable {


    private static final Logger log = LoggerFactory.getLogger(TencentRedisTask.class);

    @Override
    public void run() {
        while (true) {
            try {
                Object tencentexp = RedisQueueManager.getElementFromQueue("tencentexp");
                Object tencentimp = RedisQueueManager.getElementFromQueue("tencentimp");
                if (tencentimp != null) {
                    log.debug("线程号" + Thread.currentThread().getName());
                    TencentImpParameterParserImpl.parseUrlStr(tencentimp.toString());
                } else if (tencentexp != null) {
                    log.debug("线程号" + Thread.currentThread().getName());
                    TencentExpParameterParserImpl.parseUrlStr(tencentexp.toString());
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

