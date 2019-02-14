package cn.shuzilm.interf.pixcel;

import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.interf.pixcel.parser.AdViewExpParameterParserImpl;
import cn.shuzilm.interf.pixcel.parser.TESTAdViewNurlParameterParserImpl;
import cn.shuzilm.interf.pixcel.parser.YouYiExpParameterParserImpl;
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


    private static final Logger log = LoggerFactory.getLogger(YouYiExpParameterParserImpl.class);

    @Override
    public void run() {
        while (true) {
            try {
                Object adviewnurl = JedisQueueManager.getElementFromQueue("adviewnurl");
                Object adviewexp = JedisQueueManager.getElementFromQueue("adviewexp");
                if (adviewnurl != null) {
                    log.debug("线程号" + Thread.currentThread().getName());
                    TESTAdViewNurlParameterParserImpl.parseUrlStr(adviewnurl.toString());
                } else if (adviewexp != null) {
                    log.debug("线程号" + Thread.currentThread().getName());
                    AdViewExpParameterParserImpl.parseUrlStr(adviewexp.toString());
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

