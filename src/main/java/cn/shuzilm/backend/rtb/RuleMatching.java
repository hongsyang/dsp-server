package cn.shuzilm.backend.rtb;

import cn.shuzilm.backend.timing.rtb.RtbCronDispatch;
import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdPropertyBean;
import cn.shuzilm.bean.control.AdvertiserBean;
import cn.shuzilm.bean.control.CreativeBean;
import cn.shuzilm.bean.control.FlowTaskBean;
import cn.shuzilm.bean.control.Material;
import cn.shuzilm.bean.dmp.AudienceBean;
import cn.shuzilm.bean.dmp.GpsBean;
import cn.shuzilm.bean.dmp.TagBean;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.Constants;
import cn.shuzilm.common.jedis.JedisManager;
import cn.shuzilm.util.AsyncRedisClient;
import cn.shuzilm.util.GPSDistance;
import cn.shuzilm.util.JsonTools;
import cn.shuzilm.util.MathTools;
import cn.shuzilm.util.TimeUtil;
import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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

	private SimpleDateFormat dateFm = new SimpleDateFormat("EEEE_HH");

	private static int gradeRatio = 30;

	public static RuleMatching getInstance() {
		if (rule == null) {
			rule = new RuleMatching();
		}
		return rule;
	}

	public RuleMatching() {
		MDC.put("sift", "rtb");
		// 加载标签溢价比和权重
		constant = RtbConstants.getInstance();
		String nodeStr = constant.getRtbStrVar(RtbConstants.REDIS_CLUSTER_URI);
		String gradeRatioStr = constant.getRtbStrVar(RtbConstants.GRADE_RATIO);
		gradeRatio = Integer.parseInt(gradeRatioStr);
		String nodes[] = nodeStr.split(";");
		redis = AsyncRedisClient.getInstance(nodes);
		// jedis = JedisManager.getInstance().getResource();
		long start = System.currentTimeMillis();
		RtbCronDispatch.startRtbDispatch();
		LOG.info("初始化缓存完成,加载时间:" + (System.currentTimeMillis() - start) + " ms");
		rtbIns = RtbFlowControl.getInstance();

		tagRandom = new Random();
		adRandom = new Random();

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
	 *            ADX名称
	 * @param material
	 *            物料
	 * @param extStr
	 *            广告位支持的文件扩展名列表
	 */
	public boolean filter(int width, int height, int adWidth, int adHeight, boolean isResolutionRatio,
			int widthDeviation, int heightDeviation, String adxName, Material material, String extStr,
			Set<String> materialSet,List<Integer> widthList,List<Integer> heightList) throws Exception {
		// 筛选审核通过的物料
		if (material.getApproved_adx() == null || material.getApproved_adx().trim().equals("")
				|| !material.getApprovedAdxSet().contains(adxName)) {
			return false;
		}
		if (!extStr.equals("") && !extStr.contains(material.getExt())) {
			return false;
		}
		if (!materialSet.contains(material.getUid())) {
			return false;
		}
		//多尺寸
		if(!widthList.isEmpty()){
			for(int i=0;i<widthList.size();i++){
				int gWidth = widthList.get(i);
				int gHeight = heightList.get(i);
				if (isResolutionRatio) {
					if (adWidth >= gWidth && adHeight >= gHeight) {
						return true;
					}
				} else {
					if ((gWidth + widthDeviation >= adWidth && gWidth - widthDeviation <= adWidth)
							&& (gHeight + heightDeviation >= adHeight && gHeight - heightDeviation <= adHeight)) {
						return true;
					}
				}
			}
		}else{
			if (isResolutionRatio) {
				if (adWidth >= width && adHeight >= height) {
					return true;
				}
			} else {
				if ((width + widthDeviation >= adWidth && width - widthDeviation <= adWidth)
						&& (height + heightDeviation >= adHeight && height - heightDeviation <= adHeight)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 将设备ID 的标签从加速层取出，并做规则判断
	 * 
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
	 * @param adxName
	 *            ADX名称
	 * @param extStr
	 *            扩展名
	 * @param ip
	 *            ip地址
	 * @param appPackageName
	 *            应用包名称
	 */
	public DUFlowBean match(String deviceId, String adType, int width, int height, boolean isResolutionRatio,
			int widthDeviation, int heightDeviation, String adxName, String extStr, String ip, 
			String appPackageName,List<Integer> widthList,List<Integer> heightList)
			throws Exception {
		MDC.put("sift", "rtb");

		List<AdBean> machedAdList = new ArrayList<AdBean>();// 匹配到的广告资源列表
		Map<String, Material> metrialMap = new HashMap<String, Material>();
		Map<String, AudienceBean> audienceMap = new HashMap<String, AudienceBean>();
		Map<String, Boolean> rtbIpMap = new HashMap<String, Boolean>();
		Map<String, Boolean> demographicMap = new HashMap<String, Boolean>();
		
		// 匹配
		DUFlowBean targetDuFlowBean = null;
		TagBean tagBean = null;
		if (deviceId == null || deviceId.trim().equals("")) {
			LOG.warn("deviceId[" + deviceId + "]为空!");
			// return null;
		} else {

			deviceId = deviceId.toLowerCase();
			// 取出标签
			String tagJson = redis.getAsync(deviceId);
			// String tagJson = jedis.get(deviceId);
			tagBean = JSON.parseObject(tagJson, TagBean.class);
			// TagBean tagBean = (TagBean) JsonTools.fromJson(tagJson);
		}
		if (tagBean == null) {
			LOG.warn("根据DEVICEID[" + deviceId + "]查找TAGBEAN[" + tagBean + "]为空!");
		}
		// 开始匹配
		// String materialRatioKey = adType + "_" + widthHeightRatio;
		List<String> auidList = null;
		Set<String> materialSet = null;
		String widthHeightRatio =null;
		//多尺寸
		if(!widthList.isEmpty()){
			//多尺寸不按尺寸筛选广告
			auidList = new ArrayList(rtbIns.getAdMap().keySet());
			materialSet = new HashSet<String>();
			for(int i=0;i<widthList.size();i++){
				int gWidth = widthList.get(i);
				int gHeight = heightList.get(i);
				int divisor = MathTools.division(gWidth, gHeight);
				widthHeightRatio = gWidth / divisor + "/" + gHeight / divisor;
				if(rtbIns.getMaterialByRatioMap().get(widthHeightRatio) != null){
					materialSet.addAll(rtbIns.getMaterialByRatioMap().get(widthHeightRatio));
				}
			}
		}else{
			
			int divisor = MathTools.division(width, height);
			widthHeightRatio = width / divisor + "/" + height / divisor;
			//materialRatioKey = widthHeightRatio;
			auidList = rtbIns.getMaterialRatioMap().get(widthHeightRatio);
			materialSet = rtbIns.getMaterialByRatioMap().get(widthHeightRatio);
		}
		if (auidList == null || auidList.isEmpty()) {
			LOG.warn("根据[" + widthHeightRatio + "]未找到广告!");
			return null;
		}
		String adxNameTemp = adxName;

		if (adxNameTemp != null) {			
			if (adxNameTemp.contains("_")) {
				String adxNameSplit[] = adxNameTemp.split("_");
				adxNameTemp = adxNameSplit[0];

			}
			FlowTaskBean adxFlowTaskBean = rtbIns.getMapFlowTask().get(adxNameTemp);
			if (adxFlowTaskBean != null) {
				if (adxFlowTaskBean.getCommand() != FlowTaskBean.COMMAND_START) {
					 LOG.info("adxName["+adxNameTemp+"]关闭!");
					return null;
				}
			}
		}
		if (appPackageName != null) {
			FlowTaskBean appFlowTaskBean = rtbIns.getMapFlowTask().get(appPackageName);
			if (appFlowTaskBean != null) {
				if (appFlowTaskBean.getCommand() != FlowTaskBean.COMMAND_START) {
					 LOG.info("appPackageName["+appPackageName+"]关闭!");
					return null;
				}
			}
		}

		List<String> tagIdList = null;
		List<String> companyIdList = null;
		List<String> appPreferenceIdList = null;
		List<String> carrierIdList = null;
		List<String> brandList = null;
		List<String> ipList = null;
		Set<String> audienceTagIdSet = null;
		String provinceIdKey = null;
		String cityIdKey = null;
		String countryIdKey = null;
		String chinaKey = "china";
		String demoProvinceIdKey = null;
		String demoCityIdKey = null;
		String demoCountryIdKey = null;

		if (tagBean != null) {

			String ipStr = tagBean.getIp();
			ipList = new ArrayList<String>();
			if (ipStr != null) {
				String ips[] = ipStr.split(",");
				ipList = Arrays.asList(ips);
			}

			String tagIdStr = tagBean.getTagIdList();
			tagIdList = new ArrayList<String>();
			if (tagIdStr != null) {
				String tagIds[] = tagIdStr.split(",");
				tagIdList = Arrays.asList(tagIds);
			}

			String companyIdStr = tagBean.getCompanyIdList();
			companyIdList = new ArrayList<String>();
			if (companyIdStr != null) {
				String companyIds[] = companyIdStr.split(",");
				companyIdList = Arrays.asList(companyIds);
			}

			String appPreferenceIdStr = tagBean.getAppPreferenceIds();
			appPreferenceIdList = new ArrayList<String>();
			if (appPreferenceIdStr != null) {
				String appPreferenceIds[] = appPreferenceIdStr.split(",");
				appPreferenceIdList = Arrays.asList(appPreferenceIds);
			}

			String carrierIdStr = tagBean.getCarrierId();
			carrierIdList = new ArrayList<String>();
			if (carrierIdStr != null) {
				String carrierIds[] = carrierIdStr.split(",");
				carrierIdList = Arrays.asList(carrierIds);
			}

			String brandStr = tagBean.getBrand();
			brandList = new ArrayList<String>();
			if (brandStr != null) {
				String brands[] = brandStr.split(",");
				brandList = Arrays.asList(brands);
			}

			String audienceTagIdStr = tagBean.getAudienceTagIdList();
			if (audienceTagIdStr != null) {
				String audienceTagIds[] = audienceTagIdStr.split(",");
				List<String> audienceTagIdList = Arrays.asList(audienceTagIds);
				audienceTagIdSet = new HashSet<String>(audienceTagIdList);
			}

			provinceIdKey = String.valueOf(tagBean.getProvinceId());
			cityIdKey = provinceIdKey.concat("_").concat(String.valueOf(tagBean.getCityId()));
			countryIdKey = cityIdKey.concat("_").concat(String.valueOf(tagBean.getCountyId()));

			demoProvinceIdKey = String.valueOf(tagBean.getDemographicProvinceId());
			demoCityIdKey = demoProvinceIdKey.concat("_").concat(String.valueOf(tagBean.getDemographicCityId()));
			demoCountryIdKey = demoCityIdKey.concat("_").concat(String.valueOf(tagBean.getDemographicCountyId()));
			
		}

		if (extStr.contains("jpg")) {
			extStr = extStr.concat(",jpeg");
		} else if (extStr.contains("jpeg")) {
			extStr = extStr.concat(",jpg");
		}

		// 开始遍历符合广告素材尺寸的广告
		// long startOrder = System.currentTimeMillis();

		for (String adUid : auidList) {
			boolean isAvaliable = rtbIns.checkAvalable(adUid,deviceId,adxName);

			// 是否投当前的广告
			if (!isAvaliable) {
				LOG.debug("ID[" + adUid + "]广告不参与投放!");
				continue;
			}
			AdBean ad = rtbIns.getAdMap().get(adUid);
			CreativeBean creative = ad.getCreativeList().get(0);

			if (creative.getApproved() != 1) {
				LOG.debug("广告ID[" + adUid + "]创意未在ADX[" + adxName + "]通过,不参与投放!");
				continue;
			}
			
			LOG.debug("广告ID[" + adUid + "]开始物料过滤!");

			List<Material> materialList = creative.getMaterialList();
			boolean filterFlag = false;
			for (Material material : materialList) {
				if (filter(width, height, material.getWidth(), material.getHeight(), isResolutionRatio, widthDeviation,
						heightDeviation, adxName, material, extStr, materialSet,widthList,heightList)) {
					metrialMap.put(ad.getAdUid(), material);
					filterFlag = true;
					break;
				}
			}
			if (!filterFlag) {
				LOG.debug("广告ID[" + adUid + "]下未匹配到满足要求的物料,不参与投放!");
				continue;
			}else{
				LOG.debug("广告ID[" + adUid + "]匹配到满足要求的物料,参与投放!");
			}

			List<AudienceBean> audienceList = ad.getAudienceList();
			for (AudienceBean audience : audienceList) {
				if (tagBean != null && audience.getType().equals("location")) {// 地理位置
					if (audience.getLocationMode().equals("city")) {
						// 省市县的匹配
						if ((rtbIns.getAreaMap().get(chinaKey) != null
								&& rtbIns.getAreaMap().get(chinaKey).contains(ad.getAdUid()))
								|| (rtbIns.getAreaMap().get(demoProvinceIdKey) != null
										&& rtbIns.getAreaMap().get(demoProvinceIdKey).contains(ad.getAdUid()))
								|| (rtbIns.getAreaMap().get(demoCityIdKey) != null
										&& rtbIns.getAreaMap().get(demoCityIdKey).contains(ad.getAdUid()))
								|| (rtbIns.getAreaMap().get(demoCountryIdKey) != null
										&& rtbIns.getAreaMap().get(demoCountryIdKey).contains(ad.getAdUid()))) {
							if ((commonMatch(tagBean, audience, appPreferenceIdList, brandList, carrierIdList))) {
								machedAdList.add(ad);
								audienceMap.put(ad.getAdUid(), audience);
								demographicMap.put(ad.getAdUid(), true);
								break;
							}
						} else if ((rtbIns.getAreaMap().get(provinceIdKey) != null
								&& rtbIns.getAreaMap().get(provinceIdKey).contains(ad.getAdUid()))
								|| (rtbIns.getAreaMap().get(cityIdKey) != null
										&& rtbIns.getAreaMap().get(cityIdKey).contains(ad.getAdUid()))
								|| (rtbIns.getAreaMap().get(countryIdKey) != null
										&& rtbIns.getAreaMap().get(countryIdKey).contains(ad.getAdUid()))) {
							if ((commonMatch(tagBean, audience, appPreferenceIdList, brandList, carrierIdList))) {
								machedAdList.add(ad);
								audienceMap.put(ad.getAdUid(), audience);
								demographicMap.put(ad.getAdUid(), false);
								break;
							}
						}

					} else {// 按照经纬度匹配
						if (commonMatch(tagBean, audience, appPreferenceIdList, brandList, carrierIdList)
								&& checkInBound(tagBean, audience)) {
							// LOG.debug("ID[" + ad.getAdUid() +
							// "]通过匹配，参与排序");//记录日志太花费时间,忽略
							// machedAdList.add(ad);
							// geoAdList.add(ad);
							machedAdList.add(ad);
							audienceMap.put(ad.getAdUid(), audience);
							break;
						}
					}
				} else if (tagBean != null && audience.getType().equals("demographic")) { // 特定人群
					if ((audience.getDemographicTagId() != null && audience.getDemographicTagId().equals("[\"0\"]"))
							|| (audience.getDemographicTagIdSet() != null
									&& checkRetain(tagIdList, audience.getDemographicTagIdSet()))) {
						if (((rtbIns.getDemographicMap().get(chinaKey) != null
								&& rtbIns.getDemographicMap().get(chinaKey).contains(ad.getAdUid()))
								|| (rtbIns.getDemographicMap().get(demoProvinceIdKey) != null
										&& rtbIns.getDemographicMap().get(demoProvinceIdKey).contains(ad.getAdUid()))
								|| (rtbIns.getDemographicMap().get(demoCityIdKey) != null
										&& rtbIns.getDemographicMap().get(demoCityIdKey).contains(ad.getAdUid()))
								|| (rtbIns.getDemographicMap().get(demoCountryIdKey) != null
										&& rtbIns.getDemographicMap().get(demoCountryIdKey).contains(ad.getAdUid())))
								&& (commonMatch(tagBean, audience, appPreferenceIdList, brandList, carrierIdList))) {
							// LOG.debug("ID[" + ad.getAdUid() +
							// "]通过匹配，参与排序");//记录日志太花费时间,忽略
							machedAdList.add(ad);
							audienceMap.put(ad.getAdUid(), audience);
							demographicMap.put(ad.getAdUid(), true);
							break;
						}
					}
				} else if (tagBean != null && audience.getType().equals("company")) { // 具体公司
					if (audience.getCompanyIdSet() != null && checkRetain(companyIdList, audience.getCompanyIdSet())) {// 涉及到库中存储的数据样式和标签中的样式
						// LOG.debug("ID[" + ad.getAdUid() +
						// "]通过匹配，参与排序");//记录日志太花费时间,忽略
						machedAdList.add(ad);
						audienceMap.put(ad.getAdUid(), audience);
						break;
					}
				} else if (audience.getType().equals("ip")) {// 智能设备
					Set<String> ipSet = audience.getIpSet();
					if ((ipSet != null && ipList != null && checkRetain(ipList, ipSet))) {// 通过标签库匹配
						machedAdList.add(ad);
						audienceMap.put(ad.getAdUid(), audience);
						rtbIpMap.put(ad.getAdUid(), false);
						break;
					} else if (ipSet != null && ip != null && ipSet.contains(ip)) {// 通过请求IP匹配
						machedAdList.add(ad);
						audienceMap.put(ad.getAdUid(), audience);
						rtbIpMap.put(ad.getAdUid(), true);
						break;
					}
				} else if (tagBean != null && audience.getType().equals("dmp")) {// 定制人群包
					String audienceId = audience.getUid();
					if (audienceTagIdSet != null && !audienceTagIdSet.isEmpty()
							&& audienceTagIdSet.contains(audienceId)) {
						machedAdList.add(ad);
						audienceMap.put(ad.getAdUid(), audience);
						break;
					}
				}
			}
			
			materialList = null;
			audienceList = null;
		}

		// 按经纬度匹配
		// if (geoAdList.size() > 0) {
		// float[] residenceArray = tagBean.getResidence();
		// float[] workArray = tagBean.getWork();
		// float[] activityArray = tagBean.getActivity();
		// double[] lng = { residenceArray[0], workArray[0], activityArray[0] };
		// double[] lat = { residenceArray[1], workArray[1], activityArray[1] };
		// Set<String> boundSet = rtbIns.checkInBound(lng, lat);
		// for (AdBean ad : geoAdList) {
		// if (boundSet.contains(ad.getAdUid())) {
		// machedAdList.add(ad);
		// }
		// }
		// }
		// 排序

		if (!machedAdList.isEmpty()) {
			targetDuFlowBean = order(metrialMap, deviceId, machedAdList, tagBean, widthHeightRatio, audienceMap,
					adxName, ip, rtbIpMap, demographicMap,appPackageName);
			// 上传请求数
			if (rtbIns.getBidMap().get(targetDuFlowBean.getAdUid()) != null) {
				rtbIns.getBidMap().put(targetDuFlowBean.getAdUid(),
						rtbIns.getBidMap().get(targetDuFlowBean.getAdUid()) + 1);
			} else {
				rtbIns.getBidMap().put(targetDuFlowBean.getAdUid(), 1L);
			}
			// 上传adx流量数
			if (rtbIns.getAdxFlowMap().get(adxNameTemp) != null) {				
				rtbIns.getAdxFlowMap().put(adxNameTemp, rtbIns.getAdxFlowMap().get(adxNameTemp) + 1);
			} else {
				rtbIns.getAdxFlowMap().put(adxNameTemp, 1L);
			}
			// 上传app流量数
			if (appPackageName != null) {
				if (rtbIns.getAppFlowMap().get(appPackageName) != null) {
					rtbIns.getAppFlowMap().put(appPackageName, rtbIns.getAppFlowMap().get(appPackageName) + 1);
				} else {
					rtbIns.getAppFlowMap().put(appPackageName, 1L);
				}
			}
		}

		
		machedAdList = null;
		metrialMap = null;
		audienceMap = null;
		rtbIpMap = null;
		demographicMap = null;
		auidList = null;
		materialSet = null;
		tagIdList = null;
		companyIdList = null;
		appPreferenceIdList = null;
		carrierIdList = null;
		brandList = null;
		ipList = null;
		audienceTagIdSet = null;
		
		return targetDuFlowBean;
	}

	/**
	 * 对匹配的广告按照规则进行排序
	 */
	public DUFlowBean order(Map<String, Material> metrialMap, String deviceId, List<AdBean> machedAdList,
			TagBean tagBean, String widthHeightRatio, Map<String, AudienceBean> audienceMap, String adxName,
			String ipAddr, Map<String, Boolean> rtbIpMap, Map<String, Boolean> demographicMap,String appPackageName) throws Exception {
		MDC.put("sift", "rtb");
		DUFlowBean targetDuFlowBean = null;
		List<AdBean> gradeList = new ArrayList<AdBean>();
		List<AdBean> ungradeList = new ArrayList<AdBean>();
		for (AdBean ad : machedAdList) {
			int grade = ad.getAdvertiser().getGrade();
			// 优先级为1或者2的,100%执行分级策略
			if (grade >= 1 && grade <= 2) {
				gradeList.add(ad);
			} else {
				// 优先级为3-5的,30%执行分级策略,70%的概率跳出分级,直接参与投放
				int num = adRandom.nextInt(100);
				if (num > gradeRatio) {
					ungradeList.add(ad);
				} else {
					gradeList.add(ad);
				}
			}
		}
		boolean gradeFlag = false;
		int gradeNum = adRandom.nextInt(100);
		if (gradeNum > gradeRatio) {
			gradeFlag = true;
		} else {
			gradeFlag = false;
		}
		if (ungradeList.size() == 1 || (ungradeList.size() > 1 && gradeFlag)) {
			AdBean ad = ungradeList.get(0);// 暂时获取第一个
			// 封装返回接口引擎数据
			LOG.debug("广告ID[" + ad.getAdUid() + "]广告主ID[" + ad.getAdvertiser().getUid() + "]通过排序获得竞价资格!");
			Material material = metrialMap.get(ad.getAdUid());
			targetDuFlowBean = packageDUFlowData(material, deviceId, ad, tagBean, widthHeightRatio, audienceMap,
					adxName, ipAddr, rtbIpMap, demographicMap,appPackageName);
		} else {
			// long startOrder = System.currentTimeMillis();
			AdBean ad = null;
			if (gradeList.size() == 1) {
				ad = gradeList.get(0);
			} else {
				gradeOrderByPremiumStrategy(gradeList, audienceMap);
				// gradeOrderOtherParaStrategy(gradeList); //暂时移除广告因子打分排序
				ad = gradeByRandom(gradeList);
			}
			LOG.debug("广告ID[" + ad.getAdUid() + "]广告主ID[" + ad.getAdvertiser().getUid() + "]通过排序获得竞价资格!");
			// LOG.debug("排序花费时间:" + (System.currentTimeMillis() - startOrder));
			// 封装返回接口引擎数据
			Material material = metrialMap.get(ad.getAdUid());
			targetDuFlowBean = packageDUFlowData(material, deviceId, ad, tagBean, widthHeightRatio, audienceMap,
					adxName, ipAddr, rtbIpMap, demographicMap,appPackageName);
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
	public boolean commonMatch(TagBean tagBean, AudienceBean audience, List<String> appPreferenceIdList,
			List<String> brandList, List<String> carrierIdList) throws Exception {
		// 匹配收入
		if (audience.getIncomeLevel() != null && !audience.getIncomeLevelSet().contains(tagBean.getIncomeId())) {
			return false;
		}
		// 匹配兴趣
		if (audience.getAppPreferenceIds() != null
				&& !checkRetain(appPreferenceIdList, audience.getAppPreferenceIdSet())) {
			return false;
		}
		// 匹配平台
		if (audience.getPlatformId() != null && !audience.getPlatformIdSet().contains(tagBean.getPlatformId())) {
			return false;
		}

		// 匹配品牌
		if (audience.getBrandIds() != null && !checkRetain(brandList, audience.getBrandIdSet())) {// 可以多选，不限是空值
			return false;
		}

		// 匹配设备价格
		// if (audience.getPhonePriceLevel() != null
		// &&
		// !audience.getPhonePriceLevelSet().contains(tagBean.getPhonePrice()))
		// {
		// return false;
		// }

		// 匹配网络类型
		// if (audience.getNetworkId() != null &&
		// !audience.getNetworkIdSet().contains(tagBean.getNetworkId())) {
		// return false;
		// }

		// 匹配运营商
		if (audience.getCarrierId() != null && !checkRetain(carrierIdList, audience.getCarrierIdSet())) {
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
	public void gradeOrderByPremiumStrategy(List<AdBean> machedAdList, Map<String, AudienceBean> audienceMap)
			throws Exception {
		Collections.sort(machedAdList, new Comparator<AdBean>() {

			@Override
			public int compare(AdBean o1, AdBean o2) {
				AudienceBean audience1 = audienceMap.get(o1.getAdUid());
				AudienceBean audience2 = audienceMap.get(o2.getAdUid());
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
	public void gradeOrderOtherParaStrategy(List<AdBean> machedAdList) throws Exception {
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

	public AdBean gradeByRandom(List<AdBean> machedAdList) throws Exception {
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

	public DUFlowBean packageDUFlowData(Material material, String deviceId, AdBean ad, TagBean tagBean,
			String widthHeightRatio, Map<String, AudienceBean> audienceMap, String adxName, String ipAddr,
			Map<String, Boolean> rtbIpMap, Map<String, Boolean> demographicMap,String appPackageName) throws Exception {
		DUFlowBean targetDuFlowBean = new DUFlowBean();
		CreativeBean creative = ad.getCreativeList().get(0);
		AudienceBean audience = audienceMap.get(ad.getAdUid());
		AdvertiserBean advertiser = ad.getAdvertiser();
		// targetDuFlowBean.setBidid("123");// 广告竞价ID
		if (material.getFileName().contains("http")) {
			targetDuFlowBean.setAdm(material.getFileName());// 广告素材
		} else {
			String url = constant.getRtbStrVar(RtbConstants.MATERIAL_URL).concat(material.getUid()).concat(".")
					.concat(material.getExt());
			targetDuFlowBean.setAdm(url);// 广告素材
		}
		targetDuFlowBean.setAdw(material.getWidth());
		targetDuFlowBean.setAdh(material.getHeight());
		// targetDuFlowBean.setCrid(creative.getUid());

		targetDuFlowBean.setCrid(material.getAuditIdMap() != null ? material.getAuditIdMap().get(adxName) : null);
		targetDuFlowBean.setAdmt(material.getType());
		targetDuFlowBean.setAdct(creative.getLink_type());// 点击广告行为
		targetDuFlowBean.setAdUid(ad.getAdUid());
		targetDuFlowBean.setDid(deviceId);
		targetDuFlowBean.setDeviceId(deviceId);
		targetDuFlowBean.setAdxId(advertiser.getUid());

		targetDuFlowBean.setAudienceuid(audience.getUid());
		targetDuFlowBean.setAdvertiserUid(advertiser.getUid());
		targetDuFlowBean.setAgencyUid(advertiser.getAgencyUid());
		targetDuFlowBean.setCreativeUid(creative.getUid());
		String provinceId = null, cityId = null, countyId = null;
		if (tagBean != null) {
			if (rtbIpMap.containsKey(ad.getAdUid())) {// 通过IP匹配的结果
				if (!rtbIpMap.get(ad.getAdUid())) {// 标签设备IP匹配的结果
					provinceId = tagBean.getProvinceId() + "";
					cityId = tagBean.getCityId() + "";
					countyId = tagBean.getCountyId() + "";
				}
			} else {
				if (demographicMap.containsKey(ad.getAdUid()) && demographicMap.get(ad.getAdUid())) {
					provinceId = tagBean.getDemographicProvinceId() + "";
					cityId = tagBean.getDemographicCityId() + "";
					countyId = tagBean.getDemographicCountyId() + "";
				} else {
					provinceId = tagBean.getProvinceId() + "";
					cityId = tagBean.getCityId() + "";
					countyId = tagBean.getCountyId() + "";
				}
			}
		}
		targetDuFlowBean.setProvince(provinceId);// 省
		targetDuFlowBean.setCity(cityId);// 市
		targetDuFlowBean.setCountry(countyId);// 县
		// targetDuFlowBean.setActualPrice(1.0);//成本价
		String type = audience.getType().toUpperCase();
		double premiumRatio = constant.getRtbVar(type);
		// targetDuFlowBean.setActualPricePremium(premiumRatio*((double)ad.getPrice()));//溢价		
		//if(appPackageName != null && appPackageName.contains("com.moji")){
		if(appPackageName != null && (appPackageName.equals("com.moji.mjweather") || appPackageName.equals("com.moji.MojiWeather"))){
			targetDuFlowBean.setBiddingPrice((double) ad.getPrice()*0.6);
		}else{
			targetDuFlowBean.setBiddingPrice((double) ad.getPrice());
		}
		targetDuFlowBean.setPremiumFactor(premiumRatio);
		targetDuFlowBean.setLandingUrl(creative.getLink());
		targetDuFlowBean.setLinkUrl(creative.getClickTrackingUrl());
		targetDuFlowBean.setTracking(creative.getTracking());
		// targetDuFlowBean.setDspid("123");// DSP对该次出价分配的ID
		targetDuFlowBean.setWidthHeightRatio(widthHeightRatio);
		targetDuFlowBean.setPlatform(tagBean != null ? (getPlatformById(tagBean.getPlatformId())) : null);
		// targetDuFlowBean.setDemographicTagId(tagBean.getTagIdList());
		// 信息流相关
		targetDuFlowBean.setTitle(creative.getTitle());
		targetDuFlowBean.setTitleShort(creative.getTitleShort());
		targetDuFlowBean.setTitleLong(creative.getTitleLong());
		targetDuFlowBean.setDesc(creative.getDesc());
		targetDuFlowBean.setDescShort(creative.getDescShort());
		targetDuFlowBean.setDescLong(creative.getDescLong());
		targetDuFlowBean.setMode(ad.getMode());
		targetDuFlowBean.setDuration(material.getDuration());
		targetDuFlowBean.setMaterialId(material.getUid());
		targetDuFlowBean.setIpAddr(ipAddr);
		return targetDuFlowBean;
	}

	public String getPlatformById(int platformId) {
		String platform = "android";
		if (platformId == 1) {
			platform = "android";
		} else if (platformId == 0) {
			platform = "ios";
		}
		return platform;
	}

	public boolean checkRetain(List<String> list, Set<String> set) {
		for (String str : list) {
			if (set.contains(str)) {
				return true;
			}
		}
		return false;
	}

	public boolean checkInBound(TagBean tagBean, AudienceBean audience) throws Exception {
		boolean isInBoundReturn = false;
		boolean residenceFlag = true, workFlag = true, activityFlag = true;
		if (tagBean.getResidence() == null) {
			residenceFlag = false;
		}
		if (tagBean.getWork() == null) {
			workFlag = false;
		}
		if (tagBean.getActivity() == null) {
			activityFlag = false;
		}
		if (!residenceFlag && !workFlag && !activityFlag) {
			return false;
		}
		double[] residenceArray = tagBean.getResidence();
		double[] workArray = tagBean.getWork();
		double[] activityArray = tagBean.getActivity();
		List<GpsBean> geoList = audience.getGeoList();
		for (GpsBean gps : geoList) {
			if (audience.getMobilityType() == null) {// 不限流动性
				if (residenceFlag) {
					boolean isInBoundResidence = GPSDistance.isInArea(residenceArray[0], residenceArray[1],
							gps.getLng(), gps.getLat(), gps.getRadius());
					if (isInBoundResidence) {
						isInBoundReturn = true;
						break;
					}
				}
				if (workFlag) {
					boolean isInBoundWork = GPSDistance.isInArea(workArray[0], workArray[1], gps.getLng(), gps.getLat(),
							gps.getRadius());
					if (isInBoundWork) {
						isInBoundReturn = true;
						break;
					}
				}
				if (activityFlag) {
					boolean isInBoundActivity = GPSDistance.isInArea(activityArray[0], activityArray[1], gps.getLng(),
							gps.getLat(), gps.getRadius());
					if (isInBoundActivity) {
						isInBoundReturn = true;
						break;
					}
				}
			} else {
				if (audience.getMobilityTypeSet().contains(1) && residenceFlag) {// 居住地
					boolean isInBound = GPSDistance.isInArea(residenceArray[0], residenceArray[1], gps.getLng(),
							gps.getLat(), gps.getRadius());
					if (isInBound) {
						isInBoundReturn = true;
						break;
					}
				}
				if (audience.getMobilityTypeSet().contains(2) && workFlag) {// 工作地
					boolean isInBound = GPSDistance.isInArea(workArray[0], workArray[1], gps.getLng(), gps.getLat(),
							gps.getRadius());
					if (isInBound) {
						isInBoundReturn = true;
						break;
					}
				}
				if (audience.getMobilityTypeSet().contains(3) && activityFlag) {// 活动地
					boolean isInBound = GPSDistance.isInArea(activityArray[0], activityArray[1], gps.getLng(),
							gps.getLat(), gps.getRadius());
					if (isInBound) {
						isInBoundReturn = true;
						break;
					}
				}
			}
		}
		return isInBoundReturn;
	}

	public static void main(String[] args) {
//		try {
//			RuleMatching rule = RuleMatching.getInstance();
//			while(true){
//			DUFlowBean duflowBean = rule.match("a24d0y33j853d4d9da28t69d4bf83e77", "banner", 670, 100, true, 5, 5, "1,2", "jpg,gif", "127.0.0.1",
//					"cn.asm.clweather");
//			System.out.println(duflowBean);
//			Thread.sleep(60 * 1000);
//			}
//			DUFlowBean duflowBean = new DUFlowBean();
//			AdBean adBean = new AdBean();
//			adBean.setPrice(50.2f);
//			duflowBean.setBiddingPrice((double) adBean.getPrice()*0.6);
//			System.out.println(duflowBean.getBiddingPrice());
//		} catch (Exception e) {
//			e.getMessage();
//		}
	}

}
