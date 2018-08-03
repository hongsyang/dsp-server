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
     *
     * @param userDevice
     * @param flag
     * @param message
     * @return
     */
    @Override
    public Boolean filterDeviceParameter(Device userDevice, Boolean flag, Map message) {
        jedis = JedisManager.getInstance().getResource();
        log.debug("jedis 是否为空：{}", jedis);
        if (flag) {
            return isShuZiLianMengData(userDevice, message);
        } else {
            if (fieldCompliance(userDevice, message)) {//设备字段是否合规
                if (ipCompliance(userDevice, message)) {//设备IP是否合规
                    return isShuZiLianMengData(userDevice, message);//是否在数盟库
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    /**
     * 条件3：是否在数盟库
     *
     * @param userDevice
     * @param message
     * @return
     */
    private Boolean isShuZiLianMengData(Device userDevice, Map message) {
        if (StringUtils.isBlank(userDevice.getMac())) {//判断设备 MAC 的 MD5 值
            log.debug("设备 MAC 的 MD5 值为空,userDevice参数入参：{}", userDevice);
            message.put(SystemCodeEnum.CODE_FAIL.getCode(), SystemCodeEnum.CODE_FAIL.getMessage() + "，设备 MAC 的 MD5 值为空");
            return false;
        } else {
            String mac = jedis.get(userDevice.getMac());
            if (mac != null) {
                message.put(SystemCodeEnum.CODE_SUCCESS.getCode(), SystemCodeEnum.CODE_SUCCESS.getMessage() + "，此设备MAC的MD5值在数盟数据库中");
                return true;
            } else {
                log.debug(" 此设备不在数盟有效设备库中,userDevice参数入参：{}", userDevice);
                message.put(SystemCodeEnum.CODE_FAIL.getCode(), SystemCodeEnum.CODE_FAIL.getMessage() + "，此设备MAC的MD5值不在数盟有效设备库中");
                return false;
            }
        }
    }

    /**
     * 条件2 ：设备IP是否合规
     *
     * @param userDevice
     * @param message
     * @return
     */
    private Boolean ipCompliance(Device userDevice, Map message) {
        if (StringUtils.isBlank(userDevice.getIp())) {//判断设备ip地址
            log.debug("设备ip为空,userDevice参数入参：{}", userDevice);
            message.put(SystemCodeEnum.COMPLIANCE_IP.getCode(), SystemCodeEnum.COMPLIANCE_IP.getMessage() + "，设备IP为空");
            return false;
        } else {
            String ip = jedis.get(userDevice.getIp());
            if (ip != null) {
                log.debug(" 此设备IP在黑名单中,userDevice参数入参：{}", userDevice);
                message.put(SystemCodeEnum.COMPLIANCE_IP.getCode(), SystemCodeEnum.COMPLIANCE_IP.getMessage() + "，此设备IP在黑名单中");
                return false;

            } else {
                message.put(SystemCodeEnum.COMPLIANCE_IP.getCode(), SystemCodeEnum.COMPLIANCE_IP.getMessage() + "，此设备MAC的MD5值不在数盟有效设备库中");
                return true;
            }

        }
    }

    /**
     * 条件1 ：设备字段是否合规
     *
     * @param userDevice
     * @param message
     * @return
     */
    private Boolean fieldCompliance(Device userDevice, Map message) {
        if (StringUtils.isNotBlank(userDevice.getImei())) {
            String imei = userDevice.getImei();
            String substring = imei.substring(0, 7);
            String complianceImei = jedis.get(substring);
            if (complianceImei != null) {//合规库有IMEI的值判断厂商和品牌合规
                if (complianceImei.contains(userDevice.getMake()) && complianceImei.contains(userDevice.getModel())) {
                    log.debug("设备的IMEI合规");
                    message.put(SystemCodeEnum.COMPLIANCE_IMEI.getCode(), SystemCodeEnum.COMPLIANCE_IMEI.getMessage() + "，设备的IMEI合规");
                    String mac2 = userDevice.getMac().substring(1, 2);//判断Mac是否合规
                    if (mac2.equals("2") || mac2.equals("6") || mac2.equals("a") || mac2.equals("e")) {
                        String complianceMac = jedis.get(userDevice.getMac());//根据mac取Redis中去设备信息
                        if (complianceMac == null) {//合规库有IMEI的值判断厂商和品牌合规
                            log.debug("设备的MAC合规");
                            message.put(SystemCodeEnum.COMPLIANCE_MAC.getCode(), SystemCodeEnum.COMPLIANCE_MAC.getMessage() + "，设备的MAC合规");
                            return true;
                        } else if (complianceMac.contains(userDevice.getMake()) && complianceMac.contains(userDevice.getModel())) {
                            log.debug("设备的MAC与ios库厂商和型号匹配，设备不合规");
                            message.put(SystemCodeEnum.COMPLIANCE_MAC.getCode(), SystemCodeEnum.COMPLIANCE_MAC.getMessage() + "，设备的MAC与ios库厂商和型号匹配，设备不合规");
                            return false;
                        } else {
                            log.debug("设备的MAC与ios库厂商和型号不匹配，设备合规");
                            message.put(SystemCodeEnum.CODE_SUCCESS.getCode(), SystemCodeEnum.CODE_SUCCESS.getMessage() + "，设备的MAC与ios库厂商和型号不匹配，设备合规");
                            return true;
                        }
                    }
                }
            }
        } else {
            return false;
        }
        return false;
    }
}
