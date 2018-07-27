package cn.shuzilm.util;

import cn.shuzilm.bean.adview.request.BidRequestBean;
import cn.shuzilm.bean.adview.request.Device;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.common.jedis.JedisQueueManager;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * @Description: FilterRule 过滤规则
 * @Author: houkp
 * @CreateDate: 2018/7/26 17:20
 * @UpdateUser: houkp
 * @UpdateDate: 2018/7/26 17:20
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class FilterRule {

    private static final Logger log = LoggerFactory.getLogger(FilterRule.class);

    /**
     * 请求过滤规则
     *
     * @param bidRequestBean
     * @return
     */
    public static Boolean filterRuleBidRequest(BidRequestBean bidRequestBean) {
        return filterRuleBidRequest(bidRequestBean, true);
    }

    /**
     * 请求过滤规则  bidRequestBeanStr 字符串
     *
     * @param bidRequestBeanStr
     * @return
     */
    public static Boolean filterRuleBidRequest(String bidRequestBeanStr) {
        BidRequestBean bidRequestBean = JSON.parseObject(bidRequestBeanStr, BidRequestBean.class);
        return filterRuleBidRequest(bidRequestBean, true);
    }

    /**
     * 是否只判断数盟有效设备
     *
     * @param bidRequestBean
     * @param flag
     * @return
     */
    public static Boolean filterRuleBidRequest(BidRequestBean bidRequestBean, Boolean flag) {
        boolean tag = false;
        //初步的过滤规则
        if (bidRequestBean.getDevice() == null) {
            log.debug("没有设备信息,BidRequest参数入参：{}", bidRequestBean);
            return false;
        } else {
            Jedis jedis= JedisManager.getInstance().getResource();
            Device userDevice = bidRequestBean.getDevice();
            if (flag) {
                if (StringUtils.isBlank(userDevice.getDidsha1())) {//判断设备IMEI 的 SHA1 值
                    log.debug("设备IMEI 的 SHA1 值为空,BidRequest参数入参：{}", bidRequestBean);
                    return false;
                } else {
                    String didsha1 = jedis.get(userDevice.getDidsha1());
                    if (didsha1 != null) {
                        tag = true;
                    } else {
                        log.debug(" 此设备不在数盟有效设备库中,BidRequest参数入参：{}", bidRequestBean);
                        return false;
                    }
                }
            } else {
                if (StringUtils.isBlank(userDevice.getIp())) {//判断设备ip地址
                    log.debug("设备ip为空,BidRequest参数入参：{}", bidRequestBean);
                    return false;
                } else {
                    String ip = jedis.get(userDevice.getIp());
                    if (ip != null) {
                        log.debug(" 此设备IP在黑名单中,BidRequest参数入参：{}", bidRequestBean);
                        return false;
                    } else {
                        tag = true;
                    }
                }
            }
            return tag;
        }
    }
}
