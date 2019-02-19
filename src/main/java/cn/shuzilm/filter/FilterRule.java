package cn.shuzilm.filter;

import cn.shuzilm.bean.adview.request.App;
import cn.shuzilm.bean.adview.request.BidRequestBean;
import cn.shuzilm.bean.adview.request.Device;
import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.filter.code.SystemCodeEnum;
import cn.shuzilm.filter.interf.ADXFilterService;
import cn.shuzilm.filter.interf.ADXFilterServiceFactory;
import cn.shuzilm.util.AppBlackListUtil;
import cn.shuzilm.util.DeviceBlackListUtil;
import cn.shuzilm.util.IpBlacklistUtil;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
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

    private static final String FILTER_CONFIG = "filter.properties";

    private static AppConfigs configs = AppConfigs.getInstance(FILTER_CONFIG);

    private static final Logger log = LoggerFactory.getLogger(FilterRule.class);

    private static IpBlacklistUtil ipBlacklist = IpBlacklistUtil.getInstance();
    /**
     * 请求过滤规则  BidRequestBean
     *
     * @param bidRequestBean
     * @return
     */
    public static Map<String,String> filterRuleBidRequest(BidRequestBean bidRequestBean, String adxId) {
        Map<String,String> msg= new HashMap();
        Device userDevice = bidRequestBean.getDevice();//设备信息
        Impression userImpression = bidRequestBean.getImp().get(0);//曝光信息
        App app = bidRequestBean.getApp();//应用信息
        String deviceId=null;//设备号
        //设备的设备号：用于匹配数盟库中的数据
        if (userDevice != null) {
            if (userDevice.getOs() != null) {
                if ("ios".equals(userDevice.getOs().toLowerCase())) {
                    deviceId = userDevice.getExt().getIdfa();
                } else if ("android".equalsIgnoreCase(userDevice.getOs().toLowerCase())) {
                    //竞价请求进来之前对imei和mac做过滤
                    if (userDevice.getDidmd5() != null) {
                        if (userDevice.getDidmd5().length() == 32) {
                        }
                    } else if (userDevice.getMacmd5() != null) {
                        if (userDevice.getExt().getMacmd5().length() == 32) {
                            userDevice.setDidmd5("mac-" + userDevice.getExt().getMacmd5());
                        }
                    } else {
                        log.debug("imeiMD5和macMD5不符合规则，imeiMD5:{}，macMD5:{}", userDevice.getDidmd5(), userDevice.getExt().getMacmd5());
                        msg.put("deviceIdBlackList","0");
                    }
                    deviceId = userDevice.getDidmd5();
                } else if ("wp".equals(userDevice.getOs().toLowerCase())) {
//                    deviceId = userDevice.getExt().getMac();
                    deviceId = userDevice.getDidmd5();
                }
            }
        }

        //ip 黑名单规则  在黑名单内直接返回
        if (Boolean.valueOf(configs.getString("IP_BLACK_LIST"))) {
            if (ipBlacklist.isIpBlacklist(userDevice.getIp())) {
                log.debug("IP黑名单:{}", userDevice.getIp());
                msg.put("ipBlackList","0");
            }
        }
        // 过滤媒体黑名单
        if (Boolean.valueOf(configs.getString("BUNDLE_BLACK_LIST"))) {
            if (app != null) {
                String bundle = app.getBundle();
                if (AppBlackListUtil.inAppBlackList(bundle)) {
                    log.debug("媒体黑名单:{}", bundle);
                    msg.put("bundleBlackList","0");
                }
            }
        }


        // 过滤设备黑名单
        if (Boolean.valueOf(configs.getString("DEVICE_ID_BLACK_LIST"))) {
            if (DeviceBlackListUtil.inDeviceBlackList(deviceId)) {
                log.debug("设备黑名单:{}", deviceId);
                msg.put("deviceIdBlackList","0");
            }
        }



        return msg;
    }


}
