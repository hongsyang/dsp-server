package cn.shuzilm.backend.rtb;

import cn.shuzilm.backend.master.AdFlowControl;
import cn.shuzilm.backend.timing.rtb.RtbCronDispatch;
import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdPropertyBean;
import cn.shuzilm.bean.control.AdvertiserBean;
import cn.shuzilm.bean.control.CreativeBean;
import cn.shuzilm.bean.control.CreativeGroupBean;
import cn.shuzilm.bean.control.FlowTaskBean;
import cn.shuzilm.bean.control.Image;
import cn.shuzilm.bean.control.Material;
import cn.shuzilm.bean.control.MediaBean;
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
import com.google.common.util.concurrent.AtomicDouble;
import org.apache.commons.lang.StringUtils;
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
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

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
						  int widthDeviation, int heightDeviation, String adxName, Material material,
						  Image image,String extStr,Set<String> materialSet,List<String> adxNameList,
						  boolean isDimension,List<String> adxNamePushList) throws Exception {
		// 筛选审核通过的物料
		if(material.getApproved_adx() == null || material.getApproved_adx().trim().equals("")){
			return false;
		}

		if (!extStr.equals("") && !extStr.contains(image.getExt())) {
			return false;
		}
		if (isDimension && !materialSet.contains(material.getUid())) {
			return false;
		}

		if(isDimension){
			if (!material.getApprovedAdxSet().contains(adxName)) {
				return false;
			}
		}else{
			//多尺寸or无尺寸			
			if(adxNameList != null){
				boolean dimensionFlag = false;
				for(String adxNameTemp:adxNameList){
					//是否在推审通过列表中
					if (material.getApprovedAdxSet().contains(adxNameTemp)) {
						//是否满足尺寸
						if(width != 0 && width != -1 && height != 0 && height != -1){
							if (isResolutionRatio) {
								if (adWidth >= width && adHeight >= height) {
									dimensionFlag = true;
									adxNamePushList.add(adxNameTemp);
									break;
								}
							} else {
								if ((width + widthDeviation >= adWidth && width - widthDeviation <= adWidth)
										&& (height + heightDeviation >= adHeight && height - heightDeviation <= adHeight)) {
									dimensionFlag = true;
									adxNamePushList.add(adxNameTemp);
									break;
								}
							}
						}
						
					}
				}
				if(!dimensionFlag){
					return false;
				}else{
					return true;
				}
			}
		}

		if(isDimension){
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
							String appPackageName,List<String> adxNameList,boolean isDimension,String requestId,String tagId)
			throws Exception {
		MDC.put("sift", "rtb");
		String reason = null;
		List<AdBean> machedAdList = new ArrayList<AdBean>();// 匹配到的广告资源列表
		Map<String, Material> metrialMap = new HashMap<String, Material>();
		Map<String,CreativeGroupBean> creativeGroupMap = new HashMap<String,CreativeGroupBean>();
		Map<String,CreativeBean> creativeMap = new HashMap<String,CreativeBean>();
		Map<String, AudienceBean> audienceMap = new HashMap<String, AudienceBean>();
		Map<String, Boolean> rtbIpMap = new HashMap<String, Boolean>();
		Map<String, Boolean> demographicMap = new HashMap<String, Boolean>();
		List<String> adxNamePushList = new ArrayList<String>();
		
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
		String widthHeightRatio ="";
		
		String adLocationId = "";
		if(adxNameList != null){
			adLocationId = adxNameList.toString();
		}

		//多尺寸
		if(!isDimension){
			//多尺寸不按尺寸筛选广告
			auidList = new ArrayList<String>(rtbIns.getAdMap().keySet());
			materialSet = new HashSet<String>();
			
//			ListIterator<String> adLocationIt = adxNameList.listIterator();
//			boolean adLocationFlag = false;
//			if(appPackageName != null && !appPackageName.trim().equals("")){
//				while(adLocationIt.hasNext()){
//					String adx = adLocationIt.next();
//					String adLocationStr = adxName+"_"+adxName+"_"+appPackageName+"_"+adx;
//					if(!rtbIns.getAdLocationSet().contains(adLocationStr)){
//						adLocationIt.remove();
//						LOG.info("["+adLocationStr+"]广告位未开启投放");
//					}else{
//						adLocationFlag = true;
//					}
//				}
//				if(!adLocationFlag){
//					LOG.info("["+adLocationId+"]广告位都未开启投放,该请求停止投放");
//					reason = requestId+"\t"+widthHeightRatio+"\t"+0+"\t"+width+"_"+height+"\t"+adLocationId+"\t"+""+"\t"+
//							""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+deviceId;
//					//另起目录记录原因
//					MDC.put("phoenix", "rtb-bid-notice");
//					LOG.info(reason);
//					MDC.remove("phoenix");
//					MDC.put("sift", "rtb");
//					return null; 
//				}
//			}
		}else{
				
				int divisor = MathTools.division(width, height);
				widthHeightRatio = width / divisor + "/" + height / divisor;
				//materialRatioKey = widthHeightRatio;
				auidList = rtbIns.getMaterialRatioMap().get(widthHeightRatio);
				materialSet = rtbIns.getMaterialByRatioMap().get(widthHeightRatio);
				
//			if(appPackageName != null && !appPackageName.trim().equals("")){
//				String adLocationStr = adxName+"_"+adxName+"_"+appPackageName+"_"+width+"_"+height;
//				if(!rtbIns.getAdLocationSet().contains(adLocationStr)){
//					LOG.info("["+adLocationStr+"]尺寸都未开启投放,该请求停止投放");
//					reason = requestId+"\t"+widthHeightRatio+"\t"+0+"\t"+width+"_"+height+"\t"+adLocationId+"\t"+""+"\t"+
//							""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+deviceId;
//					//另起目录记录原因
//					MDC.put("phoenix", "rtb-bid-notice");
//					LOG.info(reason);
//					MDC.remove("phoenix");
//					MDC.put("sift", "rtb");
//					return null; 
//				}
//			}
		}


		if (auidList == null || auidList.isEmpty()) {
			LOG.warn("根据[" + widthHeightRatio + "]未找到广告!");
			//reason = requestId+"\t"+deviceId+"\t"+adxName+"\t"+appPackageName+"\t"+width+"\t"+height+
			//"\t"+ip+"\t尺寸筛选未找到广告\t"+widthHeightRatio;

			reason = requestId+"\t"+widthHeightRatio+"\t"+0+"\t"+width+"_"+height+"\t"+adLocationId+"\t"+""+"\t"+
					""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+""+"\t"+deviceId;
			//另起目录记录原因
			MDC.put("phoenix", "rtb-bid-notice");
			LOG.info(reason);
			MDC.remove("phoenix");
			MDC.put("sift", "rtb");
			return null;
		}
		//String adxNameTemp = adxName;
		String adxAndMedia = "";
		boolean packageFlag = false;
		List<Long> mediaTempList = new ArrayList<Long>();
		if (adxName != null) {
//			if (adxNameTemp.contains("_")) {
//				String adxNameSplit[] = adxNameTemp.split("_");
//				adxNameTemp = adxNameSplit[0];

//			}
			
			if(appPackageName != null && !appPackageName.trim().equals("")){
				adxAndMedia = adxName + "_" + appPackageName;
				packageFlag = true;
			}else{
				if(!isDimension){
				for(String adLocationIdTemp:adxNameList){
					if(rtbIns.getAdLocationMap().containsKey(adLocationIdTemp)){						
						MediaBean media = rtbIns.getAdLocationMap().get(adLocationIdTemp).getMedia();
						if(media != null){
							mediaTempList.add(media.getId());
							packageFlag = false;
						}
						
					}
				}
				}
			}
			
			FlowTaskBean adxFlowTaskBean = rtbIns.getMapFlowTask().get(adxName);
			if (adxFlowTaskBean != null) {
				if (adxFlowTaskBean.getCommand() != FlowTaskBean.COMMAND_START) {
					LOG.info("adxName["+adxName+"]关闭!");
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

		if(extStr != null){
			if (extStr.contains("jpg")) {
				extStr = extStr.concat(",jpeg");
			} else if (extStr.contains("jpeg")) {
				extStr = extStr.concat(",jpg");
			}
		}else{
			extStr = "jpg,jpeg,png,mp4,gif";
		}

		// 开始遍历符合广告素材尺寸的广告
		
		String adAvalableReason = null;
		String materialReason = null;
		String audienceReason = null;

		for (String adUid : auidList) {
			boolean isAvaliable = rtbIns.checkAvalable(adUid,deviceId,adxName,appPackageName,adxAndMedia,packageFlag,mediaTempList);

			AdBean ad = rtbIns.getAdMap().get(adUid);
			String advertierId = "";
			if(ad != null && ad.getAdvertiser() != null){
				advertierId = ad.getAdvertiser().getUid();
			}
			// 是否投当前的广告
			if (!isAvaliable) {
				//LOG.debug("ID[" + adUid + "]广告不参与投放!");
				//reason = adUid+"\t"+deviceId+"\t"+adxName+"\t"+appPackageName+"\t"+width+"\t"+height+
				//"\t"+ip+"\t广告投放策略触发广告停投\t";
				adAvalableReason = requestId+"\t"+widthHeightRatio+"\t"+1+"\t"+width+"_"+height+"\t"+adLocationId+"\t"+0+"\t"+
						""+"\t"+""+"\t"+advertierId+"\t"+adUid+"\t"+""+"\t"+""+"\t"+""+"\t"+deviceId;
				continue;
			}

//			CreativeBean creative = ad.getCreativeList().get(0);

//			if (creative.getApproved() != 1) {
//				LOG.debug("广告ID[" + adUid + "]创意未在ADX[" + adxName + "]通过,不参与投放!");
//				reason = adUid+"\t"+deviceId+"\t"+adxName+"\t"+appPackageName+"\t"+width+"\t"+height+
//						"\t"+ip+"\t创意未在ADX["+adxName+"]审核通过\t";
//				continue;
//			}

			boolean filterFlag = false;
			List<CreativeGroupBean> creativeGroupList = ad.getCreativeGroupList();
			temp:for(CreativeGroupBean creativeGroup:creativeGroupList){
				List<CreativeBean> creativeList = creativeGroup.getCreativeList();
				for(CreativeBean creative:creativeList){
					List<Material> materialList = creative.getMaterialList();	
					for (Material material : materialList) {
						List<Image> imageList = material.getImageList();
						if(isDimension && imageList.size() >= 3){
							continue;
						}
						for(Image image:imageList){
							if (filter(width, height, image.getWidth(), image.getHeight(), isResolutionRatio, widthDeviation,
									heightDeviation, adxName, material,image, extStr, materialSet,adxNameList,
									isDimension,adxNamePushList)) {
								metrialMap.put(ad.getAdUid(), material);
								creativeGroupMap.put(ad.getAdUid(), creativeGroup);
								creativeMap.put(ad.getAdUid(), creative);
								filterFlag = true;
								break temp;
							}
						}
					}
				}
			}
			if (!filterFlag) {
				//LOG.debug("广告ID[" + adUid + "]下未匹配到满足要求的物料,不参与投放!");
				//reason = adUid+"\t"+deviceId+"\t"+adxName+"\t"+appPackageName+"\t"+width+"\t"+height+
				//"\t"+ip+"\t未匹配到满足要求的物料\t";
				materialReason = requestId+"\t"+widthHeightRatio+"\t"+1+"\t"+width+"_"+height+"\t"+adLocationId+"\t"+1+"\t"+
						0+"\t"+""+"\t"+advertierId+"\t"+adUid+"\t"+""+"\t"+""+"\t"+""+"\t"+deviceId;
				continue;
			}

			List<AudienceBean> audienceList = ad.getAudienceList();
			boolean audienceFlag = false;
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
								audienceFlag = true;
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
								audienceFlag = true;
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
							audienceFlag = true;
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
							audienceFlag = true;
							break;
						}
					}
				} else if (tagBean != null && audience.getType().equals("company")) { // 具体公司
					if (audience.getCompanyIdSet() != null && checkRetain(companyIdList, audience.getCompanyIdSet())) {// 涉及到库中存储的数据样式和标签中的样式
						// LOG.debug("ID[" + ad.getAdUid() +
						// "]通过匹配，参与排序");//记录日志太花费时间,忽略
						machedAdList.add(ad);
						audienceMap.put(ad.getAdUid(), audience);
						audienceFlag = true;
						break;
					}
				} else if (audience.getType().equals("ip")) {// 智能设备
					Set<String> ipSet = audience.getIpSet();
					if ((ipSet != null && ipList != null && checkRetain(ipList, ipSet))) {// 通过标签库匹配
						machedAdList.add(ad);
						audienceMap.put(ad.getAdUid(), audience);
						rtbIpMap.put(ad.getAdUid(), false);
						audienceFlag = true;
						break;
					} else if (ipSet != null && ip != null && ipSet.contains(ip)) {// 通过请求IP匹配
						machedAdList.add(ad);
						audienceMap.put(ad.getAdUid(), audience);
						rtbIpMap.put(ad.getAdUid(), true);
						audienceFlag = true;
						break;
					}
				} else if (tagBean != null && audience.getType().equals("dmp")) {// 定制人群包
					String audienceId = audience.getUid();
					if (audienceTagIdSet != null && !audienceTagIdSet.isEmpty()
							&& audienceTagIdSet.contains(audienceId)) {
						machedAdList.add(ad);
						audienceMap.put(ad.getAdUid(), audience);
						audienceFlag = true;
						break;
					}
				}
			}
			
			audienceList = null;
			
			if(!audienceFlag){
				//reason = adUid+"\t"+deviceId+"\t"+adxName+"\t"+appPackageName+"\t"+width+"\t"+height+
				//"\t"+ip+"\t人群包匹配未成功\t";
				audienceReason = requestId+"\t"+widthHeightRatio+"\t"+1+"\t"+width+"_"+height+"\t"+adLocationId+"\t"+1+"\t"+
						1+"\t"+0+"\t"+advertierId+"\t"+adUid+"\t"+""+"\t"+""+"\t"+""+"\t"+deviceId;
				
			}
		}
		
		// 排序

		if (!machedAdList.isEmpty()) {
			targetDuFlowBean = order(metrialMap, deviceId, machedAdList, tagBean, widthHeightRatio, audienceMap,
					adxName, ip, rtbIpMap, demographicMap,appPackageName,requestId,width,height,
					adxNamePushList,isDimension,adLocationId,creativeGroupMap,creativeMap);
			// 上传请求数
			if (rtbIns.getBidMap().get(targetDuFlowBean.getAdUid()) != null) {
				rtbIns.getBidMap().put(targetDuFlowBean.getAdUid(),
						rtbIns.getBidMap().get(targetDuFlowBean.getAdUid()) + 1);
			} else {
				rtbIns.getBidMap().put(targetDuFlowBean.getAdUid(), 1L);
			}

			// 动态出价累计
			updateDynamicPriceMap(1L, "", "", 0, 0, 0f, "");
			// 上传adx流量数
			if (rtbIns.getAdxFlowMap().get(adxName) != null) {
				rtbIns.getAdxFlowMap().put(adxName, rtbIns.getAdxFlowMap().get(adxName) + 1);
			} else {
				rtbIns.getAdxFlowMap().put(adxName, 1L);
			}
			// 上传app流量数
			if (appPackageName != null) {
				if (rtbIns.getAppFlowMap().get(appPackageName) != null) {
					rtbIns.getAppFlowMap().put(appPackageName, rtbIns.getAppFlowMap().get(appPackageName) + 1);
				} else {
					rtbIns.getAppFlowMap().put(appPackageName, 1L);
				}
			}
		}else{
			MDC.put("phoenix", "rtb-bid-notice");
			if(audienceReason != null){
				LOG.info(audienceReason);
			}else if(materialReason != null){
				LOG.info(materialReason);
			}else{
				LOG.info(adAvalableReason);
			}			
			MDC.remove("phoenix");
			MDC.put("sift", "rtb");
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
	 * 更新动态出价缓存map
	 * @param amount
	 * @param packageName
	 * @param adTagId
	 * @param width
	 * @param height
	 * @param price
	 * @param requestId
	 */

	public void updateDynamicPriceMap(long amount, String packageName,
									  String adTagId, int width, int height, float price ,
									  String requestId) {
		String key = rtbIns.getMapKey(packageName, adTagId, width, height);
		if(StringUtils.isEmpty(key)) {
			return;
		}
		Object[] value = RtbFlowControl.getDynamicMap().get(key);

		if(value != null) {
			((AtomicLong)value[0]).addAndGet(amount);
			((AtomicDouble)value[1]).addAndGet(price);
		}else {
			Object[] array = new Object[3];
			array[0] = new AtomicLong(amount);
			array[1] = new AtomicDouble(price);
			array[2] = requestId;
			// 解决线程并发问题
			Object [] previous = RtbFlowControl.getDynamicMap().putIfAbsent(key, array);
			if(previous != null) {
				((AtomicLong)value[0]).addAndGet(amount);
				((AtomicDouble)value[1]).addAndGet(price);
			}
		}
	}

	public static void main(String[] args) {
		RuleMatching ruleMatching = new RuleMatching();
		for(int i=0;i<10;i++) {
				ruleMatching.updateDynamicPriceMap(1l,"com.dengjian.andrid","1"
					,0,0,5.0f,"requestid1");
		}

		for(int i=0;i<10;i++) {
			ruleMatching.updateDynamicPriceMap(1l,"com.dengjian.andrid",""
					,300,600,6.0f,"requestid2");
		}
	}

	/**
	 * 对匹配的广告按照规则进行排序
	 */
	public DUFlowBean order(Map<String, Material> metrialMap, String deviceId, List<AdBean> machedAdList,
		TagBean tagBean, String widthHeightRatio, Map<String, AudienceBean> audienceMap, String adxName,
		String ipAddr, Map<String, Boolean> rtbIpMap, Map<String, Boolean> demographicMap,
		String appPackageName,String requestId,int width,int height,List<String> adxNamePushList,
		boolean isDimension,String adLocationId,Map<String,CreativeGroupBean> creativeGroupMap,
		Map<String,CreativeBean> creativeMap) throws Exception {
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

		if (ungradeList.size() == 1 || (ungradeList.size() > 1 && gradeFlag) || (ungradeList.size() > 1 && gradeList.isEmpty())) {
			AdBean ad = ungradeList.get(0);// 暂时获取第一个
			// 封装返回接口引擎数据
			LOG.debug("广告ID[" + ad.getAdUid() + "]广告主ID[" + ad.getAdvertiser().getUid() + "]通过排序获得竞价资格!");
			Material material = metrialMap.get(ad.getAdUid());
			targetDuFlowBean = packageDUFlowData(material, deviceId, ad, tagBean, widthHeightRatio, audienceMap,
					adxName, ipAddr, rtbIpMap, demographicMap,appPackageName,requestId,width,height,
					adxNamePushList,isDimension,adLocationId,creativeGroupMap,creativeMap);
		} else {
			
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
					adxName, ipAddr, rtbIpMap, demographicMap,appPackageName,requestId,width,height,
					adxNamePushList,isDimension,adLocationId,creativeGroupMap,creativeMap);
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
										Map<String, Boolean> rtbIpMap, Map<String, Boolean> demographicMap,String appPackageName,
										String requestId,int width,int height,List<String> adxNamePushList,boolean isDimension,
										String adLocationId,Map<String,CreativeGroupBean> creativeGroupMap,
										Map<String,CreativeBean> creativeMap) throws Exception {
		MDC.put("sift", "rtb");
		DUFlowBean targetDuFlowBean = new DUFlowBean();
//		CreativeBean creative = ad.getCreativeList().get(0);
		CreativeGroupBean creativeGroup = creativeGroupMap.get(ad.getAdUid());
		CreativeBean creative = creativeMap.get(ad.getAdUid());
		AudienceBean audience = audienceMap.get(ad.getAdUid());
		AdvertiserBean advertiser = ad.getAdvertiser();
		List<Image> imageList = material.getImageList();
		// targetDuFlowBean.setBidid("123");// 广告竞价ID
		Map<Integer,String> admMap = new HashMap<Integer,String>();
		for(int i=0;i<imageList.size();i++){
			Image image = imageList.get(i);
			if (image.getFileName().contains("http")) {
				//targetDuFlowBean.setAdm(image.getFileName());// 广告素材
				admMap.put(i, image.getFileName());
			} else {
				String url = constant.getRtbStrVar(RtbConstants.MATERIAL_URL).concat(image.getUid()).concat(".")
					.concat(image.getExt());
				//targetDuFlowBean.setAdm(url);// 广告素材
				admMap.put(i, url);
			}
		}
		targetDuFlowBean.setAdw(imageList.get(0).getWidth());
		targetDuFlowBean.setAdh(imageList.get(0).getHeight());
		// targetDuFlowBean.setCrid(creative.getUid());
		targetDuFlowBean.setAdmMap(admMap);
		if(isDimension){
			//有尺寸
			targetDuFlowBean.setCrid(material.getAuditIdMap() != null ? material.getAuditIdMap().get(adxName) : null);
		}else{
			//多尺寸or无尺寸
			targetDuFlowBean.setCrid(adxNamePushList.isEmpty()?null:material.getAuditIdMap().get(adxNamePushList.get(0)));
		}
//		targetDuFlowBean.setAdmt(material.getType());
		targetDuFlowBean.setAdct(creativeGroup.getLink_type());// 点击广告行为
		targetDuFlowBean.setAdUid(ad.getAdUid());
		targetDuFlowBean.setDid(deviceId);
		targetDuFlowBean.setDeviceId(deviceId);
		targetDuFlowBean.setAdxId(advertiser.getUid());

		targetDuFlowBean.setAudienceuid(audience.getUid());
		targetDuFlowBean.setAdvertiserUid(advertiser.getUid());
		targetDuFlowBean.setAgencyUid(advertiser.getAgencyUid());
		targetDuFlowBean.setCreativeUid(creativeGroup.getUid());
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
		double price = ad.getPrice();
		float tempPrice = rtbIns.getDynamicPrice(appPackageName, isDimension?null:adxNamePushList.get(0), width, height);
		if(tempPrice != 0){
			price = tempPrice;
		}
//		if(appPackageName != null && (appPackageName.equals("com.moji.mjweather") || appPackageName.equals("com.moji.MojiWeather"))){
//			targetDuFlowBean.setBiddingPrice(price*0.6);
//		}else{
		targetDuFlowBean.setBiddingPrice(price);
//		}
		targetDuFlowBean.setPremiumFactor(premiumRatio);
		targetDuFlowBean.setLandingUrl(creativeGroup.getLink());
		targetDuFlowBean.setLinkUrl(creativeGroup.getClickTrackingUrl());
		targetDuFlowBean.setTracking(creativeGroup.getTracking());
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

		String succReason = requestId+"\t"+widthHeightRatio+"\t"+1+"\t"+width+"_"+height+"\t"+adLocationId+"\t"+1+"\t"+
				1+"\t"+1+"\t"+advertiser.getUid()+"\t"+ad.getAdUid()+"\t"+audience.getUid()+"\t"+creative.getUid()+"\t"+material.getUid()+"\t"+deviceId;
		MDC.put("phoenix", "rtb-bid-notice");
		LOG.info(succReason);
		MDC.remove("phoenix");
		MDC.put("sift", "rtb");

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

	/*public static void main(String[] args) {
		try {
			RuleMatching rule = RuleMatching.getInstance();
//			while(true){
			DUFlowBean duflowBean = rule.match("a24d0y33j853d4d9da28t69d4bf83e77", "banner", 640, 100, false, 5, 5, "1", "jpg,gif", "127.0.0.1",
					"com.iflytek.inputmethod",new ArrayList(),true,"123","aaa");
//			System.out.println(duflowBean);
//			Thread.sleep(60 * 1000);
//			}
//			DUFlowBean duflowBean = new DUFlowBean();
//			AdBean adBean = new AdBean();
//			adBean.setPrice(50.2f);
//			duflowBean.setBiddingPrice((double) adBean.getPrice()*0.6);
//			System.out.println(duflowBean.getBiddingPrice());
		} catch (Exception e) {
			e.getMessage();
		}
	}*/

}
