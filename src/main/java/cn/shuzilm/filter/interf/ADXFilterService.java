package cn.shuzilm.filter.interf;
import cn.shuzilm.bean.adview.request.Device;

import java.util.Map; /**
* @Description:    ADXFilterService  ADX服务商过滤器
* @Author:         houkp
* @CreateDate:     2018/8/2 17:01
* @UpdateUser:     houkp
* @UpdateDate:     2018/8/2 17:01
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
public interface ADXFilterService {
    /**
     * 根据ADX 服务商生产过滤规则
     * @param userDevice
     * @param flag
     * @param message
     * @return
     */
   public Boolean filterDeviceParameter(Device userDevice, Boolean flag, Map message);
}
