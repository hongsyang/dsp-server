package cn.shuzilm.util;

import cn.shuzilm.bean.internalflow.DUFlowBean;
import com.yao.util.bean.BeanUtil;



public class Test {

    public static void main(String[] args) {
        DUFlowBean duFlowBean =new DUFlowBean();
        duFlowBean.setRequestId("11111111111111111111111");
        DUFlowBean duFlowBean1 =new DUFlowBean();
        duFlowBean1.setBidid("22222222222222222");
        System.out.println(duFlowBean1);
        BeanUtil.copyPropertyByNotNull(duFlowBean, duFlowBean1);
        System.out.println("这是第一个："+duFlowBean);
        System.out.println("这是第二个"+duFlowBean1);

    }
}
