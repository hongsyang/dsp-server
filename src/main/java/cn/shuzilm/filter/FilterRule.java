package cn.shuzilm.filter;

import cn.shuzilm.bean.adview.request.App;
import cn.shuzilm.bean.adview.request.BidRequestBean;
import cn.shuzilm.bean.adview.request.Device;
import cn.shuzilm.bean.adview.request.Impression;
import cn.shuzilm.bean.tencent.request.TencentBidRequest;
import cn.shuzilm.bean.youyi.request.YouYiAdzone;
import cn.shuzilm.bean.youyi.request.YouYiBidRequest;
import cn.shuzilm.bean.youyi.request.YouYiMobile;
import cn.shuzilm.bean.youyi.request.YouYiUser;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.filter.code.SystemCodeEnum;
import cn.shuzilm.filter.interf.ADXFilterService;
import cn.shuzilm.filter.interf.ADXFilterServiceFactory;
import cn.shuzilm.util.AdTagBlackListUtil;
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
     * @param userIp,bundle,deviceId
     * @return
     */
    public static Map<String, String> filterRuleBidRequest(String deviceId, String bundle,String userIp,
                                                           String adxId, String adTagId, int width, int heigth) {
        Map<String, String> msg = new HashMap();

        //ip 黑名单规则  在黑名单内直接返回
        if (Boolean.valueOf(configs.getString("IP_BLACK_LIST"))) {
            if (ipBlacklist.isIpBlacklist(userIp)) {
                log.debug("IP黑名单:{}",userIp);
                msg.put("ipBlackList", "0");
            }
        }
        // 过滤媒体黑名单
        if (Boolean.valueOf(configs.getString("BUNDLE_BLACK_LIST"))) {
                if (AppBlackListUtil.inAppBlackList(bundle)) {
                    log.debug("媒体黑名单:{}", bundle);
                    msg.put("bundleBlackList", "0");
                }

        }

        // 过滤设备黑名单
        if (Boolean.valueOf(configs.getString("DEVICE_ID_BLACK_LIST"))) {
            if (DeviceBlackListUtil.inDeviceBlackList(deviceId)) {
                log.debug("设备黑名单:{}", deviceId);
                msg.put("deviceIdBlackList", "0");
            }
        }


        // 过滤广告位黑名单
        if (Boolean.valueOf(configs.getString("AD_TAG_BLACK_LIST"))) {
            if (AdTagBlackListUtil.inAdTagBlackList(adxId,bundle,adTagId,width,heigth)) {
                log.debug("广告位黑名单: adxId:{} bundle: {} adTagId:{} width:{} heigth:{} ", adxId,bundle,adTagId,width,heigth);
                msg.put("AdTagBlackList", "0");
            }
        }


        return msg;
    }


}
