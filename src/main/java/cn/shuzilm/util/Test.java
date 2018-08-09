package cn.shuzilm.util;


import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.util.base64.AdViewDecodeUtil;
import cn.shuzilm.util.base64.Base64;
import cn.shuzilm.util.base64.Decrypter;
import org.springframework.util.Base64Utils;
import redis.clients.jedis.Jedis;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.util.Arrays;
import java.util.Date;


public class Test {
    public static void main(String[] args) {
//
        String ekey= "pkoI14zSBMgD8hK4yd4nQpgBa7Aiqqgg";
        String ikey= "PxHFG8iUh8cBAnuoU8eNOaovDIaXVMHy";
        String price="-DeoHWUBAABecRQOcCgIOHv03XBETdgjMHHbSA";

        Long aLong = AdViewDecodeUtil.priceDecode(price, ekey, ikey);
        System.out.println(aLong);
        System.out.println(Double.valueOf(aLong));

//        Map msg = new HashMap();
//        msg.put("code",1001);
//        msg.put("message","参数异常");
//        String jsonString = JSON.toJSONString(msg);
//        Test test =new Test();
//        System.out.println(jsonString);

//        String test=" [[6,62,737],[4,45,0],[23,271,2504]]";
//        AudienceBean audienceBean = new AudienceBean();
//        audienceBean.setCitys(test);
//        List<AreaBean> areaBeans = audienceBean.getCityList();
//        for (AreaBean areaBean : areaBeans) {
//            System.out.println(areaBean);
//        }

//        String geos = "{\"北京师范大学附近，约5KM\":[116.374293,39.968458,5000],\"北京工人体育场附近，约3KM\":[116.455356,39.935271,3000],\"北海公园附近，约50M\":[116.395565,39.933501,50]}";
//        audienceBean.setGeos(geos);
//        ArrayList<GpsBean> geoList = audienceBean.getGeoList();
//        for (GpsBean gpsBean : geoList) {
//            System.out.println(gpsBean);
//        }

//        String scheduleTime = "{\"1\":[3,4,19],\"2\":[3,4,9],\"3\":[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23],\"4\":[4,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23],\"5\":[5,4,9],\"6\":[6,4,9],\"7\":[7,4,9]}";
//        int[][] timeTxtToMatrix = TimeSchedulingUtil.timeTxtToMatrix(scheduleTime);
//        System.out.println(timeTxtToMatrix[6][7]);
//        JSONObject parse = JSONObject.parseObject(scheduleTime);
//        Iterator<Map.Entry<String, Object>> iterator = parse.entrySet().iterator();
//        List<Map.Entry> list = new ArrayList<Map.Entry>();
//        while (iterator.hasNext()) {
//            Map.Entry<String, Object> entry = iterator.next();
//            list.add(entry);
//        }
//        String re = "[";
//        String ra = "]";
//        int[][] timeSchedulingArr = new int[7][24];
//        for (int i = 0; i < list.size(); i++) {
//            String[] split = list.get(i).getValue().toString().replace(re, "").trim().replace(ra, "").split(",");
//            for (int i1 = 0; i1 < split.length; i1++) {
//                timeSchedulingArr[i][Integer.parseInt(split[i1])]=1;
//            }
//        }
//        JSONArray objects = JSONArray.parseArray(parse1.getString("1"));

//        for (Object object : objects) {
//
//            System.out.println(object);
//        }
//        Jedis jedis = JedisManager.getInstance().getResource();
//        String set = jedis.set("6f1de61b613c9b095ea1385eb18bf5a07de0413c", "1");
//        System.out.println(set);

//        JedisQueueManager.getElementFromQueue("LingJiExp");
//        String elementJson  jedis.get("LingJiExp");
//        DUFlowBean element = JSON.parseObject(elementJson, DUFlowBean.class);

//        DUFlowBean element =new DUFlowBean();
//        element.setRequestId("1111");
//        boolean b = JedisQueueManager.putElementToQueue("LingJiExp", element, Priority.MAX_PRIORITY);
//        DUFlowBean lingJiExp = (DUFlowBean) JedisQueueManager.getElementFromQueue("LingJiExp");
//        System.out.println(lingJiExp);
//        Object lingJiExp = JedisQueueManager.getElementFromQueue("LingJiExp");
//        DUFlowBean lingJiExp = (DUFlowBean) JedisQueueManager.getElementFromQueue("LingJiExp");
//        System.out.println(lingJiExp);
//        jedis.expire("houkp",10);
//        String houkp = jedis.get("houkp");
//        System.out.println(element.toString());
//        JedisQueueManager.init();
//        Set<String> stringSet = new HashSet<>();
//        for (int i = 0; i <100 ; i++) {
//            stringSet.add("houkp"+i);
//        }
//        boolean b = JedisQueueManager.putElementToQueue("houkp", stringSet, Priority.MAX_PRIORITY);
//        for (int i = 0; i < 100; i++) {
//            duFlowBean.setRequestId("houkp"+i);
//            JedisQueueManager.putElementToQueue("houkp",duFlowBean,Priority.MAX_PRIORITY);
//        }
//        for (int i = 0; i <99 ; i++) {
//            DUFlowBean elementFromQueue = (DUFlowBean) JedisQueueManager.getElementFromQueue("houkp");
//            System.out.println(elementFromQueue);
//        }

//        String houkp ="houkp12";
//        for (String s : elementFromQueue) {
//            System.out.println(s);
//        }
//        boolean houkp1 = elementFromQueue.contains(houkp);
//        System.out.println(houkp1);
//        if (b){
//            Object houkp1 = JedisQueueManager.getElementFromQueue("houkp");
//
//        }else {
//            System.out.println("未成功");
//        }
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









    /**
     * 转化为安全Base64内容
     * @param input
     * @return
     */
    public static String WebSafeBase64Encode(byte[] input) {
        String value = "";
        try {
            value = new String(Base64.encodeBase64(input), "utf-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value.replace('+', '-').replace('/', '_').replace("=", "");
    }
}
