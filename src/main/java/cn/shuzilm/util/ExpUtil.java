package cn.shuzilm.util;

import cn.shuzilm.bean.control.AdPixelBean;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.jedis.JedisQueueManager;
/**
* @Description:    异常曝光数据
* @Author:         houkp
* @CreateDate:     2018/12/3 15:38
* @UpdateUser:     houkp
* @UpdateDate:     2018/12/3 15:38
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
public class ExpUtil {

    public static void main(String[] args) {
        AdPixelBean bean = new AdPixelBean();
        DUFlowBean exp_error = (DUFlowBean) JedisQueueManager.getElementFromQueue("EXP_ERROR");
        String adxId = exp_error.getAdxId();
        if (adxId.equals(2)){

        }
        System.out.println(exp_error);
    }
}
