package cn.shuzilm.filter.interf;

import cn.shuzilm.bean.adview.request.Device;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.filter.code.SystemCodeEnum;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * @Description: AdViewFilterServiceImpl  快友服务商过滤规则
 * @Author: houkp
 * @CreateDate: 2018/8/2 17:21
 * @UpdateUser: houkp
 * @UpdateDate: 2018/8/2 17:21
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class AdViewFilterServiceImpl extends AbstractFilter implements ADXFilterService {

    private static final Logger log = LoggerFactory.getLogger(AdViewFilterServiceImpl.class);

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
    public Boolean isShuZiLianMengData(Device userDevice, Map message) {
        if (StringUtils.isBlank(userDevice.getDidsha1())) {//判断设备 IMEI 的 sha1 值
            log.debug("设备 IMEI 的 sha1 值为空,userDevice参数入参：{}", userDevice);
            message.put(SystemCodeEnum.CODE_FAIL.getCode(), SystemCodeEnum.CODE_FAIL.getMessage() + "，设备 IMEI 的 sha1 值为空");
            return false;
        } else {
            String mac = jedis.get(userDevice.getDidsha1());
            if (mac != null) {
                message.put(SystemCodeEnum.CODE_SUCCESS.getCode(), SystemCodeEnum.CODE_SUCCESS.getMessage() + "，此设备IMEI的sha1值在数盟数据库中");
                return true;
            } else {
                log.debug(" 此设备不在数盟有效设备库中,userDevice参数入参：{}", userDevice);
                message.put(SystemCodeEnum.CODE_FAIL.getCode(), SystemCodeEnum.CODE_FAIL.getMessage() + "，此设备IMEI的sha1值不在数盟有效设备库中");
                return false;
            }
        }
    }


}
