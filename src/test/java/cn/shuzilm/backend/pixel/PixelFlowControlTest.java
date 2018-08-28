package cn.shuzilm.backend.pixel;

import cn.shuzilm.bean.control.AdPixelBean;

/**
 * Created by DENGJIAN on 2018/8/24.
 */
public class PixelFlowControlTest {

    public static void main(String[] args) {

        // case 1 没有点击， 未投放完，超出透支额度
        for(int i = 0; i < 20; i++ ){
            AdPixelBean bean = new AdPixelBean();
            bean.setAdUid("7635434a-7ff2-45f6-9806-09b6d4908e2d");
            bean.setType(0);
            bean.setWinNoticeNums(1);
            bean.setCost(5.0D);
            PixelFlowControl.getInstance().sendStatus(bean);
        }
        System.out.println("+++++++++++++++++++++++++++++++++++++++");


        //  case 2: 没有点击， 投放完毕
        for(int i = 0; i < 15; i++ ){
            AdPixelBean bean = new AdPixelBean();
            bean.setAdUid("a3caf623-bd65-4eec-8ac1-46a4a6c44689");
            bean.setType(0);
            bean.setWinNoticeNums(500);
            bean.setCost(4.0D);
            PixelFlowControl.getInstance().sendStatus(bean);
        }
        System.out.println("+++++++++++++++++++++++++++++++++++++++");

        // case 3 : 出现点击
        // 5次曝光
        for(int i = 0; i < 5; i++){
            AdPixelBean bean = new AdPixelBean();
            bean.setAdUid("f2cb2fda-caf3-4c24-8bdd-940ac2c74485");
            bean.setType(0);
            bean.setWinNoticeNums(1);
            bean.setCost(5.0D);
            PixelFlowControl.getInstance().sendStatus(bean);
        }

        // 4次点击
        for(int i = 0; i < 4; i++){
            AdPixelBean bean = new AdPixelBean();
            bean.setAdUid("f2cb2fda-caf3-4c24-8bdd-940ac2c74485");
            bean.setType(1);
            bean.setClickNums(1);
            PixelFlowControl.getInstance().sendStatus(bean);
        }

        // 10次曝光
        for(int i = 0; i < 10; i++ ){
            AdPixelBean bean = new AdPixelBean();
            bean.setAdUid("f2cb2fda-caf3-4c24-8bdd-940ac2c74485");
            bean.setType(0);
            bean.setWinNoticeNums(1);
            bean.setCost(5.0D);
            PixelFlowControl.getInstance().sendStatus(bean);
        }
    }
}
