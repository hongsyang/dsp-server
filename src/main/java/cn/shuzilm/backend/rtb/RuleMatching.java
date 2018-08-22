
package cn.shuzilm.backend.rtb;

import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdPropertyBean;
import cn.shuzilm.bean.control.AdvertiserBean;
import cn.shuzilm.bean.control.CreativeBean;
import cn.shuzilm.bean.control.Material;
import cn.shuzilm.bean.dmp.AudienceBean;
import cn.shuzilm.bean.dmp.TagBean;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.Constants;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.util.AsyncRedisClient;
import cn.shuzilm.util.JsonTools;
import cn.shuzilm.util.MathTools;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;

import com.alibaba.fastjson.JSON;

/**
 * Created by thunders on 2018/7/17.
 */
public class RuleMatching {

	private static final Logger LOG = LoggerFactory.getLogger(RuleMatching.class);

	private static RuleMatching rule = null;

	private AsyncRedisClient redis;

	private Jedis jedis;

	private RtbFlowControl rtbIns;

	private RtbConstants constant;
	/**
	 * 随机数控制开启标签顺序，雨露均沾
	 */
	private Random tagRandom;
	/**
	 * 随机数控制广告进度等顺序，雨露均沾
	 */
	private Random adRandom;

	public static RuleMatching getInstance(String[] nodes) {
		if (rule == null) {
			rule = new RuleMatching(nodes);
		}
		return rule;
	}

	public RuleMatching(String[] nodes) {
		MDC.put("sift", "rtb");
		redis = new AsyncRedisClient(nodes);
		jedis = JedisManager.getInstance().getResource();

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
		//au1.setGeos(
		//		"{\"北京师范大学附近，约5KM\":[116.374293,39.968458,5000],\"北京工人体育场附近，约3KM\":[116.455356,39.935271,3000],\"北海公园附近，约50M\":[116.395565,39.933501,50]}");
		au1.setMobilityType(0);
		au1.setDemographicTagId("[111120,222220,333320,444420]");
		au1.setDemographicCitys("[[6,62,737],[4,45,0],[23,271,2504]]");
		au1.setIncomeLevel(2);
		au1.setAppPreferenceIds("eat food");
		au1.setPlatformId(1);
		au1.setBrandIds("nike");
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

		Material material = new Material();
		material.setUid("1");
		material.setType("feed");
		material.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material.setWidth(340);
		material.setHeight(70);
		Material material01 = new Material();
		material01.setUid("01");
		material01.setType("banner");
		material01.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material01.setWidth(320);
		material01.setHeight(50);
		Material material02 = new Material();
		material02.setUid("02");
		material02.setType("fullscreen");
		material02.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material02.setWidth(320);
		material02.setHeight(50);
		Material material03 = new Material();
		material03.setUid("03");
		material03.setType("interstitial");
		material03.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material03.setWidth(320);
		material03.setHeight(50);

		List<Material> materialList = new ArrayList<Material>();
		materialList.add(material);
		materialList.add(material01);
		materialList.add(material02);
		materialList.add(material03);
		creative.setMaterialList(materialList);
		List<CreativeBean> creativeList = new ArrayList<CreativeBean>();
		creativeList.add(creative);

		ad1.setCreativeList(creativeList);

		ad1.setPrice(10);
		int[][]timeSchedulingArr = {{0,11},{0,12},{0,13},{0,14}};
		ad1.setTimeSchedulingArr(timeSchedulingArr);
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
		//au2.setGeos(
		//		"{\"北京师范大学附近，约5KM\":[116.374293,39.968458,5000],\"北京工人体育场附近，约3KM\":[116.455356,39.935271,3000],\"北海公园附近，约50M\":[116.395565,39.933501,50]}");
		au2.setMobilityType(0);
		au2.setDemographicTagId("[111120,222220,333320,444420]");
		au2.setDemographicCitys("[[6,62,737],[4,45,0],[23,271,2504]]");
		au2.setIncomeLevel(2);
		au2.setAppPreferenceIds("eat food");
		au2.setPlatformId(1);
		au2.setBrandIds("nike");
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
		material2.setType("feed");
		material2.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material2.setWidth(820);
		material2.setHeight(630);
		Material material21 = new Material();
		material21.setUid("21");
		material21.setType("feed");
		material21.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material21.setWidth(390);
		material21.setHeight(100);
		Material material22 = new Material();
		material22.setUid("22");
		material22.setType("feed");
		material22.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material22.setWidth(360);
		material22.setHeight(70);
		Material material23 = new Material();
		material23.setUid("23");
		material23.setType("feed");
		material23.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
		material23.setWidth(320);
		material23.setHeight(50);

		List<Material> materialList2 = new ArrayList<Material>();
		materialList2.add(material2);
		materialList2.add(material21);
		materialList2.add(material22);
		materialList2.add(material23);
		creative2.setMaterialList(materialList2);
		List<CreativeBean> creativeList2 = new ArrayList<CreativeBean>();
		creativeList2.add(creative2);

		ad2.setCreativeList(creativeList2);

		ad2.setPrice(100);
		int[][]timeSchedulingArr2 = {{0,11},{0,12},{0,13},{0,14}};
		ad2.setTimeSchedulingArr(timeSchedulingArr2);
		creative2.setLink(curl);
		creative2.setTracking("https://www.shuzilm.cn/");
		creative2.setLanding("https://www.shuzilm.cn/");

		ArrayList<AdBean> adList = new ArrayList<AdBean>();
		adList.add(ad1);
		adList.add(ad2);

		for (int i = 0; i < 10000; i++) {
			AdBean ad = new AdBean();
			BeanUtils.copyProperties(ad2, ad);
			ad.setAdUid("aaa" + i);
			List<CreativeBean> list = ad.getCreativeList();
			List<CreativeBean> list1 = new ArrayList<CreativeBean>();
			CreativeBean c = list.get(0);
			CreativeBean c1 = new CreativeBean();
			BeanUtils.copyProperties(c, c1);
			List<Material> materialList1 = c.getMaterialList();
			List<Material> materialList3 = new ArrayList<Material>();
			int k = 0;
			for(int q=0;q<materialList1.size();q++){
				Material m = new Material();
				m.setUid(Math.random()+"");
				m.setType("feed");
				m.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");

				m.setWidth(320+k);
				m.setHeight(50+k);
				k =k+10;
				materialList3.add(m);
			}
			c1.setMaterialList(materialList3);
			list1.add(c1);
			ad.setCreativeList(list1);
//			for(Material m:materialList1){
//				Material m1 = new Material();
//				BeanUtils.copyProperties(m1, m);
//				m.setUid(Math.random()+"");
//				m.setType("feed");
//				m.setFileName("http://dp.test.zhiheworld.com/m/qsbk_320x50.gif");
//
//				material22.setWidth(800+k);
//				material22.setHeight(600+k);
//				k =k+10;
//			}
			adList.add(ad);
		}

		System.out.println(adList.size());

		rtbIns = RtbFlowControl.getInstance();

		rtbIns.pullTenMinutes(adList);
		tagRandom = new Random();
		adRandom = new Random();

		// 加载标签溢价比和权重
		constant = RtbConstants.getInstance();
	}

	/**
	 * @param width
	 *            广告位宽度
	 * @param height
	 *            广告位高度
	 * @param adWidth
	 *            素材宽度
	 * @param adHeight
	 *            素材高度
	 * @param isResolutionRatio
	 *            是否要求分辨率
	 * @param widthDeviation
	 *            宽度误差
	 * @param heightDeviation
	 *            高度误差 创意类型、尺寸匹配
	 * @param adxName
	 *         	  ADX名称
	 * @param material
	 * 			  物料
	 * @param extSet
	 * 			 广告位支持的文件扩展名列表
	 */
	public boolean filter(int width, int height, int adWidth, int adHeight, boolean isResolutionRatio,
						  int widthDeviation, int heightDeviation,String adxName,Material material,Set<String> extSet) {
		//筛选审核通过的物料
//		if(material.getApproved() !=1 || !material.getApproved_adx().contains(adxName)){
//			return false;
//		}
//		if(!extSet.contains(material.getApproved_adx())){
//			return false;
//		}
		if (isResolutionRatio) {
			if (adWidth >= width && adHeight >= height) {
				return true;
			}
		} else {
			if ((width + widthDeviation >= adWidth || width - widthDeviation <= adWidth)
					&& (height + heightDeviation >= adHeight || height - heightDeviation <= adHeight)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 将设备ID 的标签从加速层取出，并做规则判断
	 *
	 *            标签
	 * @param adType
	 *            广告类型
	 * @param width
	 *            素材宽
	 * @param height
	 *            素材高
	 * @param isResolutionRatio
	 *            是否要求分辨率
	 * @param widthDeviation
	 *            宽度误差
	 * @param heightDeviation
	 *            高度误差
	 */
	public DUFlowBean match(String deviceId, String adType, int width, int height,
							boolean isResolutionRatio, int widthDeviation, int heightDeviation,String adxName,Set<String> extSet) {
		DUFlowBean targetDuFlowBean = null;
		if (deviceId == null || deviceId.trim().equals("")) {
			LOG.warn("deviceId[" + deviceId + "]为空!");
			return null;
		}
		// 取出标签
		//String tagJson = redis.getAsync(deviceId);
		String tagJson = jedis.get(deviceId);
		TagBean tagBean = JSON.parseObject(tagJson,TagBean.class);
		//TagBean tagBean = (TagBean) JsonTools.fromJson(tagJson);

		if (tagBean == null) {
			LOG.warn("TAGBEAN[" + tagBean + "]为空!");
			return null;
		}

		// 开始匹配
		int divisor = MathTools.division(width, height);
		String widthHeightRatio = width / divisor + "/" + height / divisor;
		String materialRatioKey = adType + "_" + widthHeightRatio;
		List<String> auidList = rtbIns.getMaterialRatioMap().get(materialRatioKey);
		if (auidList == null) {
			LOG.warn("根据[" + materialRatioKey + "]未找到广告!");
			return null;
		}

		List<AdBean> machedAdList = new ArrayList<AdBean>();// 匹配到的广告资源列表
		List<AdBean> geoAdList = new ArrayList<AdBean>();// 符合经纬度的广告资源列表
		Map<String,Material> metrialMap = new HashMap<String,Material>();
		String tagIdStr = tagBean.getTagIdList();
		String tagIds[] = tagIdStr.split(";");
		List<String> tagIdList = Arrays.asList(tagIds);

		String companyIdStr = tagBean.getCompanyIdList();
		String companyIds[] = companyIdStr.split(";");
		List<String> companyIdList = Arrays.asList(companyIds);

		String appPreferenceIdStr = tagBean.getAppPreferenceIds();
		String appPreferenceIds[] = appPreferenceIdStr.split(";");
		List<String> appPreferenceIdList = Arrays.asList(appPreferenceIds);

		String brandStr = tagBean.getBrand();
		String brands[] = brandStr.split(";");
		List<String> brandList = Arrays.asList(brands);

		// 开始遍历符合广告素材尺寸的广告
		long startOrder = System.currentTimeMillis();
		for (String adUid : auidList) {
			boolean isAvaliable = true;
			// 是否投当前的广告
			if (!isAvaliable) {
				//LOG.debug("ID[" + adUid + "]广告不参与投放!");
				continue;
			}
			AdBean ad = rtbIns.getAdMap().get(adUid);
			CreativeBean creative = ad.getCreativeList().get(0);

//			if(creative.getApproved() != 1 || !creative.getApproved_adx().contains(adxName)){
//				//LOG.debug("广告ID[" + adUid + "]创意未在ADX["+adxName+"]通过,不参与投放!");
//				continue;
//			}

			List<Material> materialList = creative.getMaterialList();
			boolean filterFlag = false;
			for(Material material:materialList){
				if (filter(width, height, material.getWidth(), material.getHeight(), isResolutionRatio, widthDeviation,
						heightDeviation,adxName,material,extSet)) {
					metrialMap.put(ad.getAdUid(), material);
					filterFlag = true;
					break;
				}
			}
			if(!filterFlag){
				continue;
			}

			AudienceBean audience = ad.getAudienceList().get(0);

			if (audience.getType().equals("location")) {// 地理位置
				if (audience.getGeos() == null || audience.getGeos().trim().equals("")) {
					// 省市县的匹配
					String key = null;
					if (tagBean.getProvinceId() == 0) {
						key = "china";
					} else if (tagBean.getCityId() == 0) {
						key = tagBean.getProvinceId() + "";
					} else if (tagBean.getCountyId() == 0) {
						key = tagBean.getProvinceId() + "_" + tagBean.getCityId();
					} else {
						key = tagBean.getProvinceId() + "_" + tagBean.getCityId() + "_" + tagBean.getCountyId();
					}
					if (rtbIns.getAreaMap().get(key).contains(ad.getAdUid()) && (commonMatch(tagBean, audience,appPreferenceIdList,brandList))) {
						// LOG.debug("ID[" + ad.getAdUid() +
						// "]通过匹配，参与排序");//记录日志太花费时间,忽略
						machedAdList.add(ad);
					}

				} else {// 按照经纬度匹配
					// boolean isInBound =
					// this.checkInBoudByType(audience.getMobilityType(),
					// tagBean);
					if (commonMatch(tagBean, audience,appPreferenceIdList,brandList)) {
						// LOG.debug("ID[" + ad.getAdUid() +
						// "]通过匹配，参与排序");//记录日志太花费时间,忽略
						// machedAdList.add(ad);
						geoAdList.add(ad);
					}
				}
			} else if (audience.getType().equals("demographic")) { // 特定人群
				if (audience.getDemographicTagIdSet().containsAll(tagIdList)) {
					String key = null;
					if (tagBean.getProvinceId() == 0) {
						key = "china";
					} else if (tagBean.getCityId() == 0) {
						key = tagBean.getProvinceId() + "";
					} else if (tagBean.getCountyId() == 0) {
						key = tagBean.getProvinceId() + "_" + tagBean.getCityId();
					} else {
						key = tagBean.getProvinceId() + "_" + tagBean.getCityId() + "_" + tagBean.getCountyId();
					}
					if (rtbIns.getDemographicMap().get(key).contains(ad.getAdUid())
							&& (commonMatch(tagBean, audience,appPreferenceIdList,brandList))) {
						// LOG.debug("ID[" + ad.getAdUid() +
						// "]通过匹配，参与排序");//记录日志太花费时间,忽略
						machedAdList.add(ad);
					}
				}

			} else if (audience.getType().equals("company")) { // 具体公司
				if (audience.getCompanyIdSet().containsAll(companyIdList)) {// 涉及到库中存储的数据样式和标签中的样式
					// LOG.debug("ID[" + ad.getAdUid() +
					// "]通过匹配，参与排序");//记录日志太花费时间,忽略
					machedAdList.add(ad);
				}
			}
		}

		// 按经纬度匹配
		if (geoAdList.size() > 0) {
			float[] residenceArray = tagBean.getResidence();
			float[] workArray = tagBean.getWork();
			float[] activityArray = tagBean.getActivity();
			double[] lng = { residenceArray[0], workArray[0], activityArray[0] };
			double[] lat = { residenceArray[1], workArray[1], activityArray[1] };
			Set<String> boundSet = rtbIns.checkInBound(lng, lat);
			for (AdBean ad : geoAdList) {
				if (boundSet.contains(ad.getAdUid())) {
					machedAdList.add(ad);
				}
			}
		}
		LOG.info("匹配花费时间:" + (System.currentTimeMillis() - startOrder));
		// 排序
		if (machedAdList.size() > 0)
			targetDuFlowBean = order(metrialMap,deviceId, machedAdList, tagBean,widthHeightRatio);

		return targetDuFlowBean;
	}

	/**
	 * 对匹配的广告按照规则进行排序
	 */
	public DUFlowBean order(Map<String,Material> metrialMap,String deviceId, List<AdBean> machedAdList, TagBean tagBean,String widthHeightRatio) {

		DUFlowBean targetDuFlowBean = null;
		List<AdBean> gradeList = new ArrayList<AdBean>();
		List<AdBean> ungradeList = new ArrayList<AdBean>();
		for (AdBean ad : machedAdList) {
			int grade = ad.getAdvertiser().getGrade();
			if (grade >= 1 && grade <= 2) {// 100%执行分级策略
				gradeList.add(ad);
			} else {// 70%执行分级策略
				int num = adRandom.nextInt(100);
				if (num <= 30) {
					ungradeList.add(ad);
				} else {
					gradeList.add(ad);
				}
			}
		}
		if (ungradeList.size() > 0) {
			AdBean ad = ungradeList.get(0);// 暂时获取第一个
			// 封装返回接口引擎数据
			LOG.debug("ID[" + ad.getAdUid() + "]通过排序获得竞价资格!");
			Material material = metrialMap.get(ad.getAdUid());
			targetDuFlowBean = packageDUFlowData(material,deviceId, ad, tagBean,widthHeightRatio);
		} else {
			System.out.println("machedAdlist=" + machedAdList.size());
			long startOrder = System.currentTimeMillis();
			AdBean ad = null;
			if (machedAdList.size() == 1) {
				ad = machedAdList.get(0);
			} else {
				gradeOrderByPremiumStrategy(machedAdList);
				gradeOrderOtherParaStrategy(machedAdList);
				ad = gradeByRandom(machedAdList);
			}
			LOG.debug("ID[" + ad.getAdUid() + "]通过排序获得竞价资格!");
			LOG.info("排序花费时间:" + (System.currentTimeMillis() - startOrder));
			// 封装返回接口引擎数据
			Material material = metrialMap.get(ad.getAdUid());
			targetDuFlowBean = packageDUFlowData(material, deviceId, ad, tagBean, widthHeightRatio);
		}

		return targetDuFlowBean;
	}

	/**
	 * 需要确认标签中的值和人群中的值
	 *
	 * @param tagBean
	 * @param audience
	 * @return
	 */
	public boolean commonMatch(TagBean tagBean, AudienceBean audience,List<String> appPreferenceIdList,List<String> brandList) {
		// 匹配收入
		if (audience.getIncomeLevel() != 0 && tagBean.getIncomeId() != audience.getIncomeLevel()) {
			return false;
		}
		// 匹配兴趣
		if (audience.getAppPreferenceIds() != null && !audience.getAppPreferenceIdSet().containsAll(appPreferenceIdList)) {
			return false;
		}
		// 匹配平台
		if (audience.getPlatformId() != 0 && tagBean.getPlatformId() != audience.getPlatformId()) {
			return false;
		}

		// 匹配品牌
		if (audience.getBrandIds() != null && !audience.getBrandIdSet().containsAll(brandList)) {// 可以多选，不限是空值
			return false;
		}

		// 匹配设备价格
		if (audience.getPhonePriceLevel() !=0 && tagBean.getPhonePrice() != audience.getPhonePriceLevel()) {
			return false;
		}

		//匹配网络类型
		if(audience.getNetworkId() != 0 && tagBean.getNetworkId() != audience.getNetworkId()){
			return false;
		}

		//匹配运营商
		if(audience.getCarrierId() != 0 && tagBean.getCarrierId() != audience.getCarrierId()){
			return false;
		}
		// 扩展
		return true;
	}


	/**
	 * 根据出价*溢价比排序
	 *
	 * @param machedAdList
	 */
	public void gradeOrderByPremiumStrategy(List<AdBean> machedAdList) {
		Collections.sort(machedAdList, new Comparator<AdBean>() {

			@Override
			public int compare(AdBean o1, AdBean o2) {
				AudienceBean audience1 = o1.getAudienceList().get(0);
				AudienceBean audience2 = o2.getAudienceList().get(0);
				String type1 = audience1.getType().toUpperCase();
				String type2 = audience2.getType().toUpperCase();
				// 得到溢价比
				double premiumRatio1 = constant.getRtbVar(type1);
				double premiumRatio2 = constant.getRtbVar(type2);
				double realPrice1 = (premiumRatio1) * o1.getPrice();
				double realPrice2 = (premiumRatio2) * o2.getPrice();
				if (realPrice1 > realPrice2) {
					return -1;
				}
				if (realPrice1 == realPrice2) {
					return 0;
				}
				return 1;
			}

		});
	}

	/**
	 * 根据广告投放进度、素材质量、剩余资金池金额、广告主打分、广告组点击率排序
	 *
	 * @param machedAdList
	 */
	public void gradeOrderOtherParaStrategy(List<AdBean> machedAdList) {
		Collections.sort(machedAdList, new Comparator<AdBean>() {

			@Override
			public int compare(AdBean o1, AdBean o2) {
				AdPropertyBean property1 = o1.getPropertyBean();
				AdPropertyBean property2 = o2.getPropertyBean();
				double score1 = property1.getImpProcess() * constant.getRtbVar(RtbConstants.IMP_PROCESS)
						+ property1.getCreativeQuality() * constant.getRtbVar(RtbConstants.CREATIVE_QUALITY)
						+ property1.getMoneyLeft() * constant.getRtbVar(RtbConstants.MONEY_LEFT)
						+ property1.getAdvertiserScore() * constant.getRtbVar(RtbConstants.ADVERTISER_SCORE)
						+ property1.getCtrScore() * constant.getRtbVar(RtbConstants.CTR_SCORE);

				double score2 = property2.getImpProcess() * constant.getRtbVar(RtbConstants.IMP_PROCESS)
						+ property2.getCreativeQuality() * constant.getRtbVar(RtbConstants.CREATIVE_QUALITY)
						+ property2.getMoneyLeft() * constant.getRtbVar(RtbConstants.MONEY_LEFT)
						+ property2.getAdvertiserScore() * constant.getRtbVar(RtbConstants.ADVERTISER_SCORE)
						+ property2.getCtrScore() * constant.getRtbVar(RtbConstants.CTR_SCORE);

				if (score1 > score2) {
					return -1;
				}
				if (score1 == score2) {
					return 0;
				}
				return 1;
			}

		});
	}

	public AdBean gradeByRandom(List<AdBean> machedAdList) {
		AdBean ad = null;
		int num = tagRandom.nextInt(100);
		if (num < 70) {
			ad = machedAdList.get(0);// 70%的概率直接获取第一个
		} else {
			int ra = 1;
			while (true) {
				ra = tagRandom.nextInt(machedAdList.size());
				if (ra != 0)
					break;
			}

			ad = machedAdList.get(ra);// 获取从第二个到最后一个随机某个元素
		}

		return ad;
	}

	public DUFlowBean packageDUFlowData(Material material,String deviceId, AdBean ad, TagBean tagBean,String widthHeightRatio) {
		DUFlowBean targetDuFlowBean = new DUFlowBean();
		CreativeBean creative = ad.getCreativeList().get(0);
		AudienceBean audience = ad.getAudienceList().get(0);
		AdvertiserBean advertiser = ad.getAdvertiser();
		//targetDuFlowBean.setBidid("123");// 广告竞价ID
		targetDuFlowBean.setAdm(material.getFileName());// 广告素材
		targetDuFlowBean.setAdw(material.getWidth());
		targetDuFlowBean.setAdh(material.getHeight());
		targetDuFlowBean.setCrid(creative.getUid());
		targetDuFlowBean.setAdmt(material.getType());
		targetDuFlowBean.setAdct(creative.getLink_type());// 点击广告行为
		targetDuFlowBean.setAdUid(ad.getAdUid());
		targetDuFlowBean.setDid(deviceId);
		targetDuFlowBean.setDeviceId(deviceId);
		targetDuFlowBean.setAdxId(advertiser.getUid());
		//targetDuFlowBean.setSeat("222");// SeatBid 的标识,由 DSP 生成
		// targetDuFlowBean.setAudienceuid("人群ID");
		targetDuFlowBean.setAdvertiserUid(advertiser.getUid());
		// targetDuFlowBean.setAgencyUid("代理商ID");
		targetDuFlowBean.setCreativeUid(creative.getUid());
		// targetDuFlowBean.setProvince("省");//省
		// targetDuFlowBean.setCity("市");//市
		// targetDuFlowBean.setActualPrice(1.0);//成本价
		String type = audience.getType().toUpperCase();
		double premiumRatio = constant.getRtbVar(type);
		// targetDuFlowBean.setActualPricePremium(premiumRatio*((double)ad.getPrice()));//溢价
		targetDuFlowBean.setBiddingPrice((double) ad.getPrice());
		targetDuFlowBean.setPremiumFactor(premiumRatio);
		targetDuFlowBean.setLandingUrl(creative.getLanding());
		targetDuFlowBean.setLinkUrl(creative.getLink());
		targetDuFlowBean.setTracking(creative.getTracking());
		//targetDuFlowBean.setDspid("123");// DSP对该次出价分配的ID
		targetDuFlowBean.setWidthHeightRatio(widthHeightRatio);
		targetDuFlowBean.setPlatform(getPlatformById(tagBean.getPlatformId()));
		targetDuFlowBean.setDemographicTagId(tagBean.getTagIdList());
		//信息流相关
		targetDuFlowBean.setTitle(creative.getTitle());
		targetDuFlowBean.setTitleShort(creative.getTitleShort());
		targetDuFlowBean.setTitleLong(creative.getTitleLong());
		targetDuFlowBean.setDesc(creative.getDesc());
		targetDuFlowBean.setDescShort(creative.getDescShort());
		targetDuFlowBean.setDescLong(creative.getDescLong());
		return targetDuFlowBean;
	}

	public String getPlatformById(int platformId){
		String platform = "android";
		if(platformId == 1){
			platform = "android";
		}else if(platformId == 0){
			platform = "ios";
		}
		return platform;
	}

}


