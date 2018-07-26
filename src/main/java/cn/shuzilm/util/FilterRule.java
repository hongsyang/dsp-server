package cn.shuzilm.util;

import cn.shuzilm.bean.adview.request.BidRequestBean;
import cn.shuzilm.bean.adview.request.Device;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.interf.rtb.parser.AdViewParser;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @Description:    FilterRule 过滤规则
* @Author:         houkp
* @CreateDate:     2018/7/26 17:20
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/26 17:20
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
public class FilterRule {

    private static final Logger log = LoggerFactory.getLogger(FilterRule.class);

    /**
     * 初步过滤规则
     *
     * @param bidRequestBean
     * @return
     */
    public static Boolean filterRuleBidRequest(BidRequestBean bidRequestBean) {
        boolean flag = true;
        //初步的过滤规则
        if (bidRequestBean.getDevice() != null) {
            Device userDevice = bidRequestBean.getDevice();
            if (StringUtils.isBlank(userDevice.getIp())) {
                flag = false;
                log.debug("设备ip为空,BidRequest参数入参：{}", bidRequestBean); ;
            } else {
                Object queue = JedisQueueManager.getElementFromQueue(userDevice.getIp());
                if (queue!=null) {
                    flag = false;
                    log.debug(" 此设备IP在黑名单中,BidRequest参数入参：{}", bidRequestBean);;
                } else {
                    flag = true;
                }
            }
        } else {
            flag = false;
            log.debug("没有设备信息,BidRequest参数入参：{}", bidRequestBean);;
        }
        return flag;
    }
}
