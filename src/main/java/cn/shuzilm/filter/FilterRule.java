package cn.shuzilm.filter;

import cn.shuzilm.bean.adview.request.BidRequestBean;
import cn.shuzilm.bean.adview.request.Device;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.filter.code.SystemCodeEnum;
import cn.shuzilm.filter.interf.ADXFilterService;
import cn.shuzilm.filter.interf.ADXFilterServiceFactory;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.Set;

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
     * 请求过滤规则  BidRequestBean
     *
     * @param bidRequestBean
     * @return
     */
    public static Boolean filterRuleBidRequest(BidRequestBean bidRequestBean, Map message) {
        return filterRuleBidRequest(bidRequestBean, true, message);
    }

    /**
     * 请求过滤规则  bidRequestBeanStr 字符串
     *
     * @param bidRequestBeanStr
     * @return
     */
    public static Boolean filterRuleBidRequest(String bidRequestBeanStr, Map message) {
        BidRequestBean bidRequestBean = JSON.parseObject(bidRequestBeanStr, BidRequestBean.class);
        return filterRuleBidRequest(bidRequestBean, true, message);
    }

    /**
     * 是否只判断数盟有效设备
     *
     * @param bidRequestBean
     * @param flag
     * @return
     */
    public static Boolean filterRuleBidRequest(BidRequestBean bidRequestBean, Boolean flag, Map message) {
        return filterRuleBidRequest(bidRequestBean, flag, message, " ");
    }

    /**
     * 根据ADX服务商做过滤规则，快友用IMEI的sha1，灵集用MAC的MD5值
     *
     * @param bidRequestBean
     * @param flag
     * @param adxName        ADX服务商
     * @return
     */
    public static Boolean filterRuleBidRequest(BidRequestBean bidRequestBean, Boolean flag, Map message, String adxName) {
        //初步的过滤规则
        if (bidRequestBean.getDevice() == null) {
            log.debug("设备信息为空,BidRequest参数入参：{}", bidRequestBean);
            message.put(SystemCodeEnum.CODE_FAIL.getCode(), SystemCodeEnum.CODE_FAIL.getMessage() + "，设备信息为空");
            return false;
        } else {
            Device userDevice = bidRequestBean.getDevice();
            if (StringUtils.isBlank(adxName)) {
                log.debug("无对应ADX服务商过滤器，请检查代码。厂商名称为：{}", adxName);
                message.put(SystemCodeEnum.CODE_FAIL.getCode(), SystemCodeEnum.CODE_FAIL.getMessage() + "，无对应ADX服务商过滤器");
                return false;
            } else {
                Reflections reflections = new Reflections("cn.shuzilm.filter.interf");
                Set<Class<? extends ADXFilterService>> monitorClasses = reflections.getSubTypesOf(ADXFilterService.class);
                for (Class<? extends ADXFilterService> monitorClass : monitorClasses) {
                    if (monitorClass.getSimpleName().toLowerCase().contains(adxName)) {
                        ADXFilterService adxFilterService = ADXFilterServiceFactory.getADXFilterService(monitorClass.getName());
                        log.debug("adxFilterService的名称:{}",adxFilterService.getClass().getSimpleName());
                        return adxFilterService.filterDeviceParameter(userDevice, flag, message);
                    }
                }
            }
        }
        return false;
    }
}
