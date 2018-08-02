package cn.shuzilm.filter.interf;

import cn.shuzilm.bean.adview.request.Device;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.filter.FilterRule;
import cn.shuzilm.filter.code.SystemCodeEnum;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * @Description: LingJiFilterServiceImpl  灵集服务商过滤规则
 * @Author: houkp
 * @CreateDate: 2018/8/2 17:21
 * @UpdateUser: houkp
 * @UpdateDate: 2018/8/2 17:21
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class LingJiFilterServiceImpl implements ADXFilterService {

    private static final Logger log = LoggerFactory.getLogger(LingJiFilterServiceImpl.class);

    private Jedis jedis = null;

    /**
     * 根据ADX服务商做过滤规则，快友用IMEI的sha1，灵集用MAC的MD5值
     * @param userDevice
     * @param flag
     * @param message
     * @return
     */
    @Override
    public Boolean filterDeviceParameter(Device userDevice, Boolean flag, Map message) {
        jedis = JedisManager.getInstance().getResource();
        if (flag) {
            if (StringUtils.isBlank(userDevice.getDidsha1())) {//判断设备 MAC 的 MD5  值
                log.debug("设备 MAC 的 MD5 值为空,userDevice参数入参：{}", userDevice);
                message.put(SystemCodeEnum.CODE_FAIL.getCode(), SystemCodeEnum.CODE_FAIL.getMessage() + "，设备 MAC 的 MD5 值为空");
                return false;
            } else {
                String didsha1 = jedis.get(userDevice.getDidsha1());
                if (didsha1 != null) {
                    return true;
                } else {
                    log.debug(" 此设备不在数盟有效设备库中,userDevice参数入参：{}", userDevice);
                    return false;
                }
            }
        } else {
            if (StringUtils.isNotBlank(userDevice.getImei())) {
                String imei = userDevice.getImei();
                String substring = imei.substring(0, 7);
                String complianceImei = jedis.get(substring);
                if (complianceImei != null) {//合规库有IMEI的值判断厂商和品牌合规

                }
            }
            if (StringUtils.isBlank(userDevice.getIp())) {//判断设备ip地址
                log.debug("设备ip为空,userDevice参数入参：{}", userDevice);
                return false;
            } else {
                String ip = jedis.get(userDevice.getIp());
                if (ip != null) {
                    log.debug(" 此设备IP在黑名单中,userDevice参数入参：{}", userDevice);
                    return false;
                } else {
                    return true;
                }
            }
        }
    }
}
