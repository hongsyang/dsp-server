package cn.shuzilm.util.geo;

import cn.shuzilm.backend.master.TaskServicve;
import cn.shuzilm.backend.rtb.RtbConstants;
import cn.shuzilm.bean.control.*;
import cn.shuzilm.bean.dmp.AreaBean;
import cn.shuzilm.bean.dmp.AudienceBean;
import cn.shuzilm.bean.dmp.GpsBean;
import cn.shuzilm.common.Constants;
import cn.shuzilm.common.jedis.Priority;
import cn.shuzilm.util.AsyncRedisClient;
import cn.shuzilm.util.InvokePython;
import cn.shuzilm.util.MathTools;
import cn.shuzilm.util.TimeSchedulingUtil;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;

import com.yao.util.db.bean.ResultList;
import com.yao.util.db.bean.ResultMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;

/**
 * 广告流量控制
 * Created by thunders on 2018/7/11.
 */
public class PutGEOToRedis {

	
	private static final String dir = Constants.getInstance().getConf("PYTHON_GEO_TRANSFER_DIR");

	// private static final String dir = "d:\\";

	private static final String pythonEnvDir = "python";

    private static final Logger myLog = LoggerFactory.getLogger(PutGEOToRedis.class);

    public static ArrayList<WorkNodeBean> nodeList = null;
    /**
     * 主控定期从数据库中读取的间隔 单位：分钟
     */
    private static final int INTERVAL = 10 * 60 * 1000;
    private static TaskServicve taskService = new TaskServicve();

//    /**
//     * 广告主对应的广告 MAP
//     */
//    private static HashMap<String,ArrayList<String>> adviserMap = null;

    public HashMap<String, AdBean> getMapAd(){
        return mapAd;
    }

    public HashMap<String, AdFlowStatus> getMapMonitorHour(){
        return mapMonitorHour;
    }

    public HashMap<String, ReportBean> getReportMapHour(){
        return reportMapHour;
    }

    /**
     * 广告资源管理
     */
    private static HashMap<String, AdBean> mapAd = null;
    
    private static List<AdBean> listAd = null;

    private static HashMap<String, ReportBean> reportMapHour = null;


    /**
     * 广告每小时的指标监控
     */
    private static HashMap<String, AdFlowStatus> mapMonitorHour = null;

    public PutGEOToRedis() {
        MDC.put("sift", "control");

        mapAd = new HashMap<>();
        listAd = new ArrayList<AdBean>();
        mapMonitorHour = new HashMap<>();
        reportMapHour = new HashMap<>();

    }

    /**
     * 每隔 10 分钟
     * 从数据库中加载所有的广告,广告主、广告素材和广告配额
     */
    public void loadAdInterval() {
        long timeNow = System.currentTimeMillis();
        long timeBefore = timeNow - INTERVAL;
        //取出所有的广告，并取出变动的部分，如果是配额和金额发生变化，则需要重新分配任务
        try {
            //加载广告信息
            ResultList adList = taskService.queryAdByUpTime(0);
            for (ResultMap map : adList) {
                AdBean ad = new AdBean();
                ad.setAdUid(map.getString("uid"));
                //广告组
                String groupId = map.getString("group_uid");
                ad.setGroupId(groupId);
                String adUid = ad.getAdUid();


                String adverUid = map.getString("advertiser_uid");

                //根据 广告主ID 获得 广告主
                AdvertiserBean adver = taskService.queryAdverByUid(adverUid);
                ad.setAdvertiser(adver);
                ad.setName(map.getString("name"));
                //每天限制
                ad.setCpmDailyLimit(map.getInteger("cpm_daily"));
                //每小时限制
                ad.setCpmHourLimit(map.getInteger("cpm_hourly"));


                //获得人群
                List<AudienceBean> audience = taskService.queryAudienceByUpTime(adUid);
                ad.setAudienceList(audience);

                String creativeUid = map.getString("creative_uid");
                //根据 广告创意ID 获得广告创意
                CreativeBean creativeBean = taskService.queryCreativeUidByAid(creativeUid);
                //根据创意 ID 查询 物料
                List<Material> materialList = taskService.queryMaterialByCreativeId(creativeUid);
                creativeBean.setMaterialList(materialList);

                ArrayList<CreativeBean> creaList = new ArrayList<>();
                creaList.add(creativeBean);
                ad.setCreativeList(creaList);
                ad.setEndTime(new Date(map.getInteger("e")));
                ad.setFrqDaily(map.getInteger("frq_daily"));
                ad.setFrqHour(map.getInteger("frq_hourly"));
                ad.setPrice(map.getBigDecimal("price").floatValue());
                ad.setMode(map.getString("mode"));
                // 设置广告的可拖欠的额度
                ad.setMoneyArrears(map.getInteger("money_arrears"));
                //出价模式
                ad.setMode(map.getString("mode"));

                ad.setPriority(map.getInteger("priority"));
                //限额
                // 如果当前广告设定限额为 0 ，则以该账户的每日限额为准，
                BigDecimal quotaAmount = map.getBigDecimal("quota_amount");
                if(quotaAmount.doubleValue() <= 0 ){
                    ad.setQuotaAmount(reportMapHour.get(adUid).getMoneyQuota());
                }else{
                    ad.setQuotaAmount(quotaAmount);
                }
                ad.setSpeedMode(map.getInteger("speed"));
                ad.setStartTime(new Date(map.getInteger("s")));
                String timeScheTxt = map.getString("time");
                int[][] timeScheduling = TimeSchedulingUtil.timeTxtToMatrix(timeScheTxt);
                ad.setTimeSchedulingArr(timeScheduling);
                ad.setTimestamp(map.getInteger("created_at"));
                //如果是价格和配额发生了变化，直接通知
                //如果素材发生了变化，直接通知
                mapAd.put(adUid, ad);
                listAd.add(ad);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
    	
		String nodeStr = RtbConstants.getInstance().getRtbStrVar(RtbConstants.REDIS_CLUSTER_URI);
		String nodes [] = nodeStr.split(";");
    	AsyncRedisClient redis = AsyncRedisClient.getInstance(nodes);
		PutGEOToRedis putGeoToRedis = new PutGEOToRedis();
		//putGeoToRedis.loadAdInterval();
		ArrayList<GpsBean> gpsAllList = new ArrayList<>();
		
		
		AdBean ad1 = new AdBean();
		AudienceBean au1 = new AudienceBean();
		String curl = "http://101.200.56.200:8880/" + "lingjiclick?" + "id=" + "1213" + "&price=" + 6 + "&pmp="
				+ "2222";
		au1.setAdUid("12345678");
		au1.setAdviserId("123456");
		au1.setName("大学生");
		au1.setRemark("remark");
		au1.setType("location");
		au1.setCitys("[[6,62,737],[4,45,0],[23,271,2504]]");
		 au1.setGeos(
			 "[{\"北京市通州区\":[\"116.640865\",\"39.852104\",\"1000\"]},{\"北京市大兴区公园北环路辅路-旧宫,清和园\":[\"116.461492\",\"39.794028\",\"5316\"]}]");
		au1.setMobilityType(0);
		//[{"北京市通州区":["116.640865","39.852104","1000"]},{"北京市大兴区公园北环路辅路-旧宫,清和园":["116.461492","39.794028","5316"]}]
		au1.setDemographicTagId("[111120,222220,333320,444420]");
		au1.setDemographicCitys("[[6,62,737],[4,45,0],[23,271,2504]]");
		au1.setIncomeLevel(2);
		au1.setAppPreferenceIds("eat food");
		au1.setPlatformId(1);
		au1.setBrandIds("335");
		au1.setPhonePriceLevel(3);
		au1.setNetworkId(2);
		au1.setCarrierId(4);
		au1.setCompanyIds("{\"北京AAA有限公司\":275231,\"北京BBB有限公司\":375331,\"北京CCC有限公司\":475431}");
		au1.setCompanyNames("北京AAA有限公司,北京BBB有限公司,北京CCC有限公司");

		List<AudienceBean> au1List = new ArrayList<AudienceBean>();
		au1List.add(au1);
		ad1.setAudienceList(au1List);

		AdPropertyBean propertyBean = new AdPropertyBean();
		propertyBean.setImpProcess(3);
		propertyBean.setCreativeQuality(2);
		propertyBean.setMoneyLeft(4);
		propertyBean.setAdvertiserScore(5);
		propertyBean.setCtrScore(2);

		ad1.setPropertyBean(propertyBean);

		AdvertiserBean advertiser = new AdvertiserBean();
		advertiser.setUid("7777777");
		advertiser.setName("广告主A");
		advertiser.setGrade(1);

		ad1.setAdvertiser(advertiser);

		ad1.setAdUid("23455555");
		ad1.setName("广告名称A");

		CreativeBean creative = new CreativeBean();
		creative.setUid("567890");
		creative.setName("广告素材A");
		creative.setApproved(1);

		Material material = new Material();
		material.setUid("1");
		material.setType("banner");
		material.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material.setWidth(340);
		material.setHeight(70);
		material.setApproved_adx("1");
		material.setExt("jpg");
		Material material01 = new Material();
		material01.setUid("01");
		material01.setType("banner");
		material01.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material01.setWidth(320);
		material01.setHeight(50);
		material01.setApproved_adx("1");
		material01.setExt("jpg");
		Material material02 = new Material();
		material02.setUid("02");
		material02.setType("banner");
		material02.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material02.setWidth(350);
		material02.setHeight(80);
		material02.setApproved_adx("1");
		material02.setExt("jpg");
		Material material03 = new Material();
		material03.setUid("03");
		material03.setType("banner");
		material03.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material03.setWidth(360);
		material03.setHeight(90);
		material03.setExt("jpg");
		material03.setApproved_adx("1");

		List<Material> materialList = new ArrayList<Material>();
		materialList.add(material);
		materialList.add(material01);
		materialList.add(material02);
		materialList.add(material03);
		creative.setType("banner");
		creative.setMaterialList(materialList);
		creative.setApproved_adx("1");
		List<CreativeBean> creativeList = new ArrayList<CreativeBean>();
		creativeList.add(creative);

		ad1.setCreativeList(creativeList);

		ad1.setPrice(100);
		
		creative.setLink(curl);
		creative.setTracking("https://www.shuzilm.cn/");
		creative.setLanding("https://www.shuzilm.cn/");

		AdBean ad2 = new AdBean();
		AudienceBean au2 = new AudienceBean();
		au2.setAdUid("12345678");
		au2.setAdviserId("123456");
		au2.setName("大学生");
		au2.setRemark("remark");
		au2.setType("location");
		au2.setCitys("[[6,62,737],[4,45,0],[23,271,2504]]");
		 au2.setGeos(
		 "[{\"北京市通州区\":[\"116.640865\",\"22.852104\",\"1000\"]},{\"北京市大兴区公园北环路辅路-旧宫,清和园\":[\"116.461492\",\"22.794028\",\"5316\"]}]");
		au2.setMobilityType(0);
		au2.setDemographicTagId("[111120,222220,333320,444420]");
		au2.setDemographicCitys("[[6,62,737],[4,45,0],[23,271,2504]]");
		au2.setIncomeLevel(2);
		au2.setAppPreferenceIds("eat food");
		au2.setPlatformId(1);
		au2.setBrandIds("335");
		au2.setPhonePriceLevel(3);
		au2.setNetworkId(2);
		au2.setCarrierId(4);
		au2.setCompanyIds("{\"北京AAA有限公司\":275231,\"北京BBB有限公司\":375331,\"北京CCC有限公司\":475431}");
		au2.setCompanyNames("北京AAA有限公司,北京BBB有限公司,北京CCC有限公司");

		List<AudienceBean> au2List = new ArrayList<AudienceBean>();
		au2List.add(au2);
		ad2.setAudienceList(au2List);

		AdPropertyBean propertyBean2 = new AdPropertyBean();
		propertyBean2.setImpProcess(4);
		propertyBean2.setCreativeQuality(3);
		propertyBean2.setMoneyLeft(4);
		propertyBean2.setAdvertiserScore(5);
		propertyBean2.setCtrScore(5);

		ad2.setPropertyBean(propertyBean2);

		AdvertiserBean advertiser2 = new AdvertiserBean();
		advertiser2.setUid("7777777");
		advertiser2.setName("广告主A");
		advertiser2.setGrade(1);

		ad2.setAdvertiser(advertiser2);

		ad2.setAdUid("23455556");
		ad2.setName("广告名称A");

		CreativeBean creative2 = new CreativeBean();
		creative2.setUid("567890");
		creative2.setName("广告素材A");
		Material material2 = new Material();
		material2.setUid("2");
		material2.setType("banner");
		material2.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material2.setWidth(820);
		material2.setHeight(630);
		material2.setApproved_adx("1");
		material2.setExt("jpg");
		Material material21 = new Material();
		material21.setUid("21");
		material21.setType("banner");
		material21.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material21.setWidth(390);
		material21.setHeight(100);
		material21.setExt("jpg");
		material21.setApproved_adx("1");
		Material material22 = new Material();
		material22.setUid("22");
		material22.setType("banner");
		material22.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material22.setWidth(360);
		material22.setHeight(70);
		material22.setApproved_adx("1");
		material22.setExt("jpg");
		Material material23 = new Material();
		material23.setUid("23");
		material23.setType("banner");
		material23.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material23.setWidth(320);
		material23.setHeight(50);
		material23.setApproved_adx("1");
		material23.setExt("jpg");

		List<Material> materialList2 = new ArrayList<Material>();
		materialList2.add(material2);
		materialList2.add(material21);
		materialList2.add(material22);
		materialList2.add(material23);
		creative2.setMaterialList(materialList2);
		creative2.setType("banner");
		creative2.setApproved_adx("1");
		creative2.setApproved(1);
		List<CreativeBean> creativeList2 = new ArrayList<CreativeBean>();
		creativeList2.add(creative2);

		ad2.setCreativeList(creativeList2);

		ad2.setPrice(100);
		
		creative2.setLink(curl);
		creative2.setTracking("https://www.shuzilm.cn/");
		creative2.setLanding("https://www.shuzilm.cn/");

		listAd.add(ad1);
		listAd.add(ad2);

		for (int i = 0; i < 10000; i++) {
			AdBean ad = new AdBean();
			BeanUtils.copyProperties(ad2, ad);
			AudienceBean auTemp = ad2.getAudienceList().get(0);
			AudienceBean au = new AudienceBean();
			BeanUtils.copyProperties(auTemp,au);
			List<AudienceBean> audienceList = new ArrayList<AudienceBean>();
			au.setGeos(
					 "[{\"北京市通州区\":[\"116.640"+i+"\",\"22.852"+i+"\",\"1000\"]},{\"北京市大兴区公园北环路辅路-旧宫,清和园\":[\"116.461"+i+"\",\"22.794"+i+"\",\"5316\"]}]");
			audienceList.add(au);
			ad.setAudienceList(audienceList);
			ad.setAdUid("aaa" + i);
			List<CreativeBean> list = ad.getCreativeList();
			List<CreativeBean> list1 = new ArrayList<CreativeBean>();
			CreativeBean c = list.get(0);
			CreativeBean c1 = new CreativeBean();
			BeanUtils.copyProperties(c, c1);
			List<Material> materialList1 = c.getMaterialList();
			List<Material> materialList3 = new ArrayList<Material>();
			int k = 0;
			for (int q = 0; q < materialList1.size(); q++) {
				Material m = new Material();
				m.setUid(Math.random() + "");
				m.setType("banner");
				m.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
				m.setApproved_adx("1");
				m.setWidth(320 + k);
				m.setHeight(50 + k);
				m.setExt("jpg");
				k = k + 10;
				materialList3.add(m);
			}
			c1.setMaterialList(materialList3);
			list1.add(c1);
			ad.setCreativeList(list1);
			// for(Material m:materialList1){
			// Material m1 = new Material();
			// BeanUtils.copyProperties(m1, m);
			// m.setUid(Math.random()+"");
			// m.setType("banner");
			// m.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
			//
			// material22.setWidth(800+k);
			// material22.setHeight(600+k);
			// k =k+10;
			// }
			listAd.add(ad);
		}
		
		
		for(AdBean adBean:listAd){
            List<AudienceBean> audienceList = adBean.getAudienceList();
            if (audienceList.size() == 0) {
                myLog.error(adBean.getAdUid() + "\t" + adBean.getName() + " 没有设置人群包..");
            }
            ArrayList<GpsBean> gpsList = null;
            for (AudienceBean audience : audienceList) {
                if (audience != null) {
                    //加载人群中的GEO位置信息
                    gpsList = audience.getGeoList();
                    if (gpsList != null) {
                    	gpsAllList.addAll(gpsList);
                    }
                    }
            }
        
		}
		String currWorkPath = dir + "geo_transfer.py";
		
		Map<String,String> redisMap = new HashMap<String,String>();
		for(GpsBean gps:gpsAllList){
          String[] arg = new String[]{
          pythonEnvDir,
          currWorkPath,
          String.valueOf(gps.getLng()),
          String.valueOf(gps.getLat()),
          String.valueOf(gps.getRadius()),
  };
          
          String result = InvokePython.invoke(arg,dir);
          redisMap.put(gps.getLng()+"_"+gps.getLat()+"_"+gps.getRadius(), result);
          
          //redis.setAsync(gps.getLng()+"_"+gps.getLat()+"_"+gps.getRadius(), result);
		}
		
		redis.setHMAsync("geo", redisMap);
		System.out.println(redis.getHMAsync("geo").get("116.6403_22.8523_1000"));
		
		myLog.info("redis geo缓存更新成功!");
	}

}

