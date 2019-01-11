package cn.shuzilm.util;

import cn.shuzilm.backend.pixel.PixelFlowControl;
import cn.shuzilm.backend.timing.pixel.PixelCronDispatch;
import cn.shuzilm.bean.control.AdPixelBean;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.common.jedis.Priority;

import java.util.UUID;

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
        try {
            System.out.println("开始");
            PixelCronDispatch.startPixelDispatch();
            PixelFlowControl pixelFlowControl = PixelFlowControl.getInstance();
            System.out.println("redis取数据");
            while (true) {

                DUFlowBean exp_error = (DUFlowBean) JedisQueueManager.getElementFromQueue("EXP_ERROR");
                Long size = JedisQueueManager.getLength("EXP_ERROR");
                System.out.println("EXP_ERROR的个数：" + size);
                System.out.println(exp_error);
                String adxId = exp_error.getAdxId();
                if (exp_error.getRequestId() != null) {
                    //计算价格
                    AdPixelBean bean = new AdPixelBean();
                    bean.setAdUid(exp_error.getAdUid());
                    bean.setPremiumFactor(exp_error.getPremiumFactor());
                    bean.setWinNoticeNums(1);
                    bean.setCost(exp_error.getActualPrice());
                    AdPixelBean adPixelBean = null;//价格返回结果
                    adPixelBean = pixelFlowControl.sendStatus(bean);
                    //pixel服务器发送到Phoenix
                    exp_error.setInfoId(exp_error.getRequestId() + UUID.randomUUID());
                    exp_error.setRequestId(exp_error.getRequestId());
                    exp_error.setActualPrice(exp_error.getActualPrice());//成本价
                    exp_error.setActualPricePremium(adPixelBean.getFinalCost());//最终价格
                    exp_error.setOurProfit(adPixelBean.getDspProfit());//dsp利润
                    exp_error.setAgencyProfit(adPixelBean.getRebateProfit());//代理商利润

                    boolean lingJiClick = JedisQueueManager.putElementToQueue("EXP", exp_error, Priority.MAX_PRIORITY);
                    if (lingJiClick) {
                        System.out.println("到Phoenix是否成功:" + lingJiClick + "   发送elemen :{}" + exp_error);
                    } else {
                        System.out.println("发送elemen :{}" + exp_error + "到Phoenix是否成功：{}" + lingJiClick);
                        throw new RuntimeException();
                    }


                } else {
                    System.out.println(exp_error);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

}
