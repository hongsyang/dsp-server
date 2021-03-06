package cn.shuzilm;


import bidserver.BidserverSsp;
import cn.shuzilm.bean.adview.request.BidRequestBean;
import cn.shuzilm.bean.youyi.request.YouYiBidRequest;
import cn.shuzilm.util.MessageLiteToStringUtil;
import cn.shuzilm.util.base64.Base64;
import com.alibaba.fastjson.JSON;
import com.google.protobuf.InvalidProtocolBufferException;
import com.googlecode.protobuf.format.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Test {

    private static final Logger log = LoggerFactory.getLogger(Test.class);

    public String toJson() throws InvalidProtocolBufferException {
//        String json = "{\"session_id\":\"o6n3WwoADDomPAAA\",\"bucket_id\":\"1\",\"host_nodes\":\"bidr3a6.sora.cm2\",\"exchange\":{\"bid_id\":\"0b88ec18000010805bf7a9a300007791\",\"adx_id\":5},\"user\":{\"user_exid\":\"800f7f3ed7f1bde111f7b8da0283c55d\",\"user_yyid\":\"800f7f3ed7f1bde111f7b8da0283c55d\",\"user_ip\":\"49.92.86.244\",\"user_agent\":\"Mozilla/5.0 (Linux; Android 8.1.0; vivo Y85A Build/OPM1.171019.011; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/43.0.2357.65 Mobile Safari/537.36\",\"user_gender\":\"GENDER_TYPE_UNKNOWN\",\"user_crowd_tags\":\"yob:0,1:0,2:0,3:0,4:0,5:0\",\"user_area\":1156320000,\"user_yyid_type\":3},\"adzone\":[{\"pid\":\"273\",\"adz_id\":\"2734b512556d9e88c80ec0cb022e54bd\",\"adz_type\":\"ADZONE_TYPE_INAPP_NATIVE\",\"adz_width\":640,\"adz_height\":360,\"adz_ad_count\":1,\"adz_position\":0,\"reserve_price\":0,\"native\":[{\"native_id\":1}]}],\"mobile\":{\"is_app\":true,\"device_os\":\"android\",\"device_os_version\":\"8.1.0\",\"device_id\":\"800f7f3ed7f1bde111f7b8da0283c55d\",\"device_type\":\"phone\",\"imei\":\"869701033753966\",\"mac\":\"4CC00AC21C63\",\"android_id\":\"2730171220e15186\",\"app_name\":\"优酷客户端\",\"md5_imei\":\"800f7f3ed7f1bde111f7b8da0283c55d\",\"md5_android_id\":\"0d2456cffdd9f0466dd2229b61f871da\",\"md5_mac\":\"e02d62079c5351894cf99aa7c6a0ef7f\"}}";
        String json ="";
                BidserverSsp.BidRequest.Builder builder1 = BidserverSsp.BidRequest.newBuilder();
        try {
            JsonFormat.merge(json,builder1);
            System.out.println(builder1.getSessionId());

        } catch (JsonFormat.ParseException e) {
            e.printStackTrace();
        }
        BidserverSsp.BidRequest.Builder builder = BidserverSsp.BidRequest.newBuilder();
        builder.setSessionId("1111111111111111");
        builder.setBucketId("2222");
        builder.setExchange(BidserverSsp.BidRequest.Exchange.newBuilder().setAdxId(111).build());
        builder.setUser(BidserverSsp.BidRequest.User.newBuilder().build());
        BidserverSsp.BidRequest.Adzone adzone = BidserverSsp.BidRequest.Adzone.newBuilder().setPid("1111111111111111111").setAdzAdCount(1).build();
        builder.setMobile(BidserverSsp.BidRequest.Mobile.newBuilder().build());
        builder.addAdzone(adzone);
        BidserverSsp.BidRequest bidRequest = builder.build();
        String printToString = JsonFormat.printToString(bidRequest);
        System.out.println("printToString"+printToString);
        byte[] byteArray = builder1.build().toByteArray();
        BidserverSsp.BidRequest request = BidserverSsp.BidRequest.parseFrom(byteArray);
        System.out.println(request.getSessionId());
        String print = JsonFormat.printToString(request);
        //输出json串
        System.out.println(print);
        //快友json
        String adview ="{\"app\":{\"ext\":{},\"ver\":\"1.1.3\",\"storeurl\":\"https:\\/\\/play.google.com\\/store\\/apps\\/details?id=com.commsource.beautyplus\",\"cat\":[0],\"name\":\"Beautyplus\",\"paid\":0,\"id\":\"4967162ec2fae7b1c162587d99b5c635\",\"bundle\":\"com.commsource.beautyplus\"},\"at\":1,\"tmax\":300,\"regs\":{\"ext\":{\"gdpr\":0},\"coppa\":1},\"id\":\"20181122-200000_bidreq_179-1272-XXAM-2020\",\"imp\":[{\"tagid\":\"POSID8r10nbz39851\",\"banner\":{\"pos\":1,\"w\":300,\"h\":250},\"bidfloor\":15000,\"bidfloorcur\":\"RMB\",\"id\":\"20181122-200000_reqimp_179-948539-qfbd-1899\",\"instl\":1}],\"device\":{\"ext\":{\"uuid\":\"aa704205-20e9-45e0-b2fc-da233765530a\"},\"orientation\":0,\"os\":\"Android\",\"sw\":720,\"didmd5\":\"a8793c01e0217fa9b0146de6f6af18ad\",\"ifa\":\"aa704205-20e9-45e0-b2fc-da233765530a\",\"ip\":\"217.64.110.214\",\"js\":1,\"language\":\"fr\",\"ua\":\"Mozilla\\/5.0 (Linux; Android 8.0.0; SM-G935F Build\\/R16NW; wv) AppleWebKit\\/537.36 (KHTML, like Gecko) Version\\/4.0 Chrome\\/70.0.3538.80 Mobile Safari\\/537.36\",\"devicetype\":2,\"geo\":{\"country\":\"MLI\"},\"carrier\":\"\",\"osv\":\"OSVER\",\"sh\":1280,\"didsha1\":\"d61ba77bd070cc5c63ca2d2d863c8aaab6338c8e\",\"model\":\"\",\"connectiontype\":3,\"make\":\"\"},\"user\":{\"ext\":{\"consent\":\"0\"},\"gender\":\"Null\"}}"  ;
        BidRequestBean adviewRequestBean = JSON.parseObject(adview, BidRequestBean.class);
        System.out.println(adviewRequestBean);
        YouYiBidRequest bidRequestBean = JSON.parseObject(json, YouYiBidRequest.class);
        System.out.println(bidRequestBean);
//        try {
//            BidserverSsp.BidRequest parseFrom = BidserverSsp.BidRequest.parseFrom(result);
//            System.out.println(parseFrom);
//        } catch (InvalidProtocolBufferException e) {
//            e.printStackTrace();
//        }
//        String s = new String(result);
//        System.out.println("s"+s);
//        BidserverSsp.BidRequest request = null;
//        try {
//            request = BidserverSsp.BidRequest.parseFrom(bytes);
//        } catch (InvalidProtocolBufferException e) {
//            e.printStackTrace();
//        }
//        System.out.println(request.getSessionId());
//        ByteString byteString = bidRequest.toByteString();
//        for (Byte aByte : byteString) {
//            System.out.println(aByte.getClass());
//        }
//        System.out.println(json);
//        System.out.println(bidRequest.toByteString().toString());

        return null;
    }

    public static void main(String[] args) throws InvalidProtocolBufferException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        String format = LocalDateTime.now().format(formatter);//时间戳
        System.out.println(format);
//        BidserverSsp.BidRequest.Builder builder = BidserverSsp.BidRequest.newBuilder();
//        builder.setSessionId("1");
//        System.out.println(builder.toString());
//        String printToString = JsonFormat.printToString(builder.build());
//        System.out.println(printToString);
//        JsonFormat.merge(json,builder);
//
//        System.out.println("输出结果:"+(builder.getSessionId()));
//        AdPixelBean bean = new AdPixelBean();
//        bean.setWinNoticeNums(1);
//        //pixel服务器发送到主控模块
//        log.debug("pixel服务器发送到主控模块的AdViewNurlBean：{}", bean);

//        String ekey= "hqvBbhco3nPm5kr0TXgQxaO4Er25qd7n";
//        String ikey= "eKUKpIu1cFDETMSo3CY8RJYxfRpNQSu2";
//
//        File file =new File("C:\\Users\\houkp\\Desktop\\对账\\对账\\9.27号曝光");
//        System.out.println(file.isFile());
//        BufferedReader bufferedReader =new BufferedReader( new FileReader(file));
//        String price=null;
//        String temp=null;
//
//        while ((temp=bufferedReader.readLine())!=null){
//            String[] split = temp.split(",");
//            if (split.length>1){
//            for (String s : split) {
//                if (s.substring(0,6).trim().equals("price")){
//                    String[] strings = s.split("=");
//                    price =strings[1].trim();
//                    System.out.println(price);
//                    Long aLong = AdViewDecodeUtil.priceDecode(price, ekey, ikey);
//                    System.out.println(aLong);
//                }
//            }
//            }
//        }
//        bufferedReader.close();


//        String result = AES.decrypt("1JhqBHCPuAdwdO2OsPom9b758Vn-cov2e6jsjGdWo9o","af36ec6c77c042b5a5e49e6414fb436f" );
//        System.out.println(result);
//        Long priceLong = AdViewDecodeUtil.priceDecode("4Kc2GWYBAAAGZE1CYxZrD3KYNzyTz56onfCs6A", "hqvBbhco3nPm5kr0TXgQxaO4Er25qd7n", "eKUKpIu1cFDETMSo3CY8RJYxfRpNQSu2");
//        System.out.println(Double.valueOf(priceLong)/10000);
//        String adm="{\"nativead\":{\"imptrackers\":[\"http://test.xxx.com?id=${AUCTION_ID}&bidid=${AUCTION_BID_ID}&impid=${AUCTION_IMP_ID}&price=${AUCTION_PRICE}\",\"http://a.com/a\",\"http://b.com/b\"],\"link\":{\"url\":\"deeplink://deeplink/url/into/app\",\"clicktrackers\":[\"http://a.com/a\",\"http://b.com/b\"]},\"event\":[{\"vm\":[\"http://test1\",\"http://test2\"],\"v\":0}],\"assets\":[{\"id\":1,\"title\":{\"text\":\"InstallBOA\"}},{\"id\":2,\"data\":{\"value\":5}},{\"id\":3,\"img\":{\"url\":[\"http://cdn.mobad.com/ad.png\",\"http://img2.com\"],\"w\":1200,\"h\":627}},{\"id\":4,\"video\":{\"url\":\"http://video.com\",\"cover_img_url\":\"http://img.com\",\"w\":640,\"h\":480,\"duration\":15}},{\"id\":5,\"data\":{\"value\":\"Click\"}}]}}";
//        String s = UrlEncoded.encodeString(adm);
////        System.out.println(s);
//        String nodes[] = {"172.17.129.116,7001", "172.17.129.116,7002", "172.17.129.116,7003", "172.17.129.116,7004", "172.17.129.116,7005", "172.17.129.116,7006"};
//        String s = Arrays.toString(nodes);
//        System.out.println(s);
//        List<RedisURI> nodeList = new ArrayList<>();
//        for(String node : nodes){
//            String[] nodeArr = node.split(",");
//            RedisURI nodeUri = RedisURI.create(nodeArr[0], Integer.parseInt(nodeArr[1]));
//            nodeList.add(nodeUri);
//        }
//
//        RedisClusterClient clusterClient = RedisClusterClient.create(nodeList);
//        ClientResources clusterClientResources = clusterClient.getResources();
//        Partitions partitions = clusterClient.getPartitions();
//        List<RedisClusterNode> partitionsList = partitions.getPartitions();
//        for (RedisClusterNode partition : partitions) {
//            System.out.println(partition);
//        }
//        System.out.println(clusterClientResources);

//      commands.hset("3D8A278F33E4F97181DF1EAEFE500D05", "temp", ss);
//        double f = 111231.4545;
//        NumberFormat nf = NumberFormat.getNumberInstance();
//        digits 显示的数字位数 为格式化对象设定小数点后的显示的最多位,显示的最后位是舍入的
//        nf.setMaximumFractionDigits(2);
//        String format = nf.format(f);
//        System.out.println(nf.format(f));
//        String ekey= "pkoI14zSBMgD8hK4yd4nQpgBa7Aiqqgg";
//        String ikey= "PxHFG8iUh8cBAnuoU8eNOaovDIaXVMHy";
//        String price="-DeoHWUBAABecRQOcCgIOHv03XBETdgjMHHbSA";
//
//        Long aLong = AdViewDecodeUtil.priceDecode(price, ekey, ikey);
//        System.out.println(aLong);
//        System.out.println(Double.valueOf(aLong)/10000);
//        TagBean tagBean = new TagBean();
//        tagBean.setTagId(123);
//        float[] work = { 11.11f, 22.22f };
//        float[] residence = { 33.11f, 44.22f };
//        float[] activity = { 55.11f, 66.22f };
//        tagBean.setWork(work);
//        tagBean.setResidence(residence);
//        tagBean.setActivity(activity);
//
//        tagBean.setProvinceId(6);
//        tagBean.setCityId(62);
//        tagBean.setCountyId(737);
//
//        tagBean.setIncomeId(2);
//        tagBean.setAppPreferenceIds("eat food");
//        tagBean.setPlatformId(1);
//        tagBean.setBrand("nike");
//        tagBean.setPhonePrice(3);
//        tagBean.setNetworkId(2);
//        tagBean.setCarrierId(4);
//        tagBean.setAppPreferenceId("app");
//        tagBean.setTagIdList("222220,333320");
//        tagBean.setCompanyIdList("123,321,222");
//
//        Jedis jedis = JedisManager.getInstance().getResource();
//
//
//        String set = jedis.set("3D8A278F33E4F97181DF1EAEFE500D05", JsonTools.toJsonString(tagBean));
//
//        System.out.println(set);
//        String s = jedis.get("3D8A278F33E4F97181DF1EAEFE500D05");
//        System.out.println(s);
//        DUFlowBean duFlowBean1 =new DUFlowBean();
//        duFlowBean1.setRequestId("1");
//        DUFlowBean duFlowBean2 =new DUFlowBean();
//        duFlowBean2.setRequestId("2");
//        duFlowBean2.setBidid("2");
//        BeanUtil.copyPropertyByNotNull(duFlowBean1,duFlowBean2);
//        System.out.println(duFlowBean1);
//        System.out.println(duFlowBean2);
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
     *
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
