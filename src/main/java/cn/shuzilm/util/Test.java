package cn.shuzilm.util;

import cn.shuzilm.interf.rtb.parser.RequestService;
import cn.shuzilm.interf.rtb.parser.RequestServiceFactory;
import org.reflections.Reflections;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;


public class Test {

    public static void main(String[] args) {
//        DUFlowBean duFlowBean =new DUFlowBean();
//        duFlowBean.setRequestId("11111111111111111111111");
//        DUFlowBean duFlowBean1 =new DUFlowBean();
//        duFlowBean1.setBidid("22222222222222222");
//        System.out.println(duFlowBean1);
//        BeanUtil.copyPropertyByNotNull(duFlowBean, duFlowBean1);
//        System.out.println("这是第一个："+duFlowBean);
//        System.out.println("这是第二个"+duFlowBean1);


        Reflections reflections = new Reflections("cn.shuzilm.interf.rtb.parser");

        Set<Class<? extends RequestService>> monitorClasses = reflections.getSubTypesOf(RequestService.class);
//        player.toString().contains("Lingji")
//        monitorClasses.forEach((player) -> RequestServiceFactory.getRequestService(player.getName()) );
//        if (player.toString().toLowerCase().contains("lingji"))
        String data ="11";
        AtomicReference<String> className1 =null;
        monitorClasses.forEach(player ->{
            System.out.println(player.toString().toLowerCase().contains("lingji"));
            if (player.toString().toLowerCase().contains("lingji")){
                className1.set(player.getName());
            }
        });
        String className =null;
        for (Class<? extends RequestService> player : monitorClasses) {
            System.out.println(player.toString().toLowerCase().contains("lingji"));
            if (player.toString().toLowerCase().contains("lingji")) {
                className=player.getName() ;
            }
        }
        String s = RequestServiceFactory.getRequestService(className).parseRequest(className);
    }
}
