package cn.shuzilm.util;

import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.interf.rtb.parser.RequestService;
import cn.shuzilm.interf.rtb.parser.RequestServiceFactory;
import org.reflections.Reflections;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;


public class Test {
    public static void main(String[] args)  {

        JedisManager jedisManager = JedisManager.getInstance();
        Jedis jedis = jedisManager.getResource();
        System.out.println(jedis.get("houkp"));
//        //连接本地的 Redis 服务
//        Jedis jedis = new Jedis("101.200.56.200");
//        jedis.append("houkp","ceshi");
//        String houkp = jedis.get("houkp");
//        System.out.println(houkp);
//        System.out.println("连接成功");
//        //查看服务是否运行
//        System.out.println("服务正在运行: "+jedis.ping());
//        try {
//
//            Class.forName("org.apache.phoenix.jdbc.PhoenixDriver");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        Statement stmt = null;
//        ResultSet rs = null;
//
//        Connection con = DriverManager.getConnection("jdbc:phoenix:hadoop0,hadoop1,hadoop2:2181");
//        stmt = con.createStatement();
//        String sql = "select * from test";
//        rs = stmt.executeQuery(sql);
//        while (rs.next()) {
//            System.out.print("id:"+rs.getString("id"));
//            System.out.println(",name:"+rs.getString("name"));
//        }
//        stmt.close();
//        con.close();

//        DUFlowBean duFlowBean =new DUFlowBean();
//        duFlowBean.setRequestId("11111111111111111111111");
//        DUFlowBean duFlowBean1 =new DUFlowBean();
//        duFlowBean1.setBidid("22222222222222222");
//        System.out.println(duFlowBean1);
//        BeanUtil.copyPropertyByNotNull(duFlowBean, duFlowBean1);
//        System.out.println("这是第一个："+duFlowBean);
//        System.out.println("这是第二个"+duFlowBean1);


//        Reflections reflections = new Reflections("cn.shuzilm.interf.rtb.parser");
//
//        Set<Class<? extends RequestService>> monitorClasses = reflections.getSubTypesOf(RequestService.class);
//        player.toString().contains("Lingji")
//        monitorClasses.forEach((player) -> RequestServiceFactory.getRequestService(player.getName()) );
//        if (player.toString().toLowerCase().contains("lingji"))
//        String data ="11";
//        AtomicReference<String> className1 =null;
//        monitorClasses.forEach(player ->{
//            System.out.println(player.toString().toLowerCase().contains("lingji"));
//            if (player.toString().toLowerCase().contains("lingji")){
//                className1.set(player.getName());
//            }
//        });
//        String className =null;
//        for (Class<? extends RequestService> player : monitorClasses) {
//            System.out.println(player.toString().toLowerCase().contains("lingji"));
//            if (player.toString().toLowerCase().contains("lingji")) {
//                className=player.getName() ;
//            }
//        }
//        String s = RequestServiceFactory.getRequestService(className).parseRequest(className);
    }
}
