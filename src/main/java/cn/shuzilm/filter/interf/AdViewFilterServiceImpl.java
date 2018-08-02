package cn.shuzilm.filter.interf;

import cn.shuzilm.bean.adview.request.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
* @Description:    AdViewFilterServiceImpl  快友服务商过滤规则
* @Author:         houkp
* @CreateDate:     2018/8/2 17:21
* @UpdateUser:     houkp
* @UpdateDate:     2018/8/2 17:21
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
public class AdViewFilterServiceImpl implements ADXFilterService {

    private static final Logger log = LoggerFactory.getLogger(AdViewFilterServiceImpl.class);
    @Override
    public Boolean filterDeviceParameter(Device userDevice, Boolean flag, Map message) {
        return null;
    }
}
