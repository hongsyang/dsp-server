package cn.shuzilm.backend.rtb;

import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdPropertyBean;
import cn.shuzilm.bean.control.AdvertiserBean;
import cn.shuzilm.bean.control.CreativeBean;
import cn.shuzilm.bean.dmp.AudienceBean;
import cn.shuzilm.bean.dmp.TagBean;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.Constants;
import cn.shuzilm.util.AsyncRedisClient;
import cn.shuzilm.util.JsonTools;
import cn.shuzilm.util.MathTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;

/**
 * Created by thunders on 2018/7/17.
 */
public class RuleMatching {

	private static final Logger LOG = LoggerFactory.getLogger(RuleMatching.class);

	private AsyncRedisClient redis;

	private RtbFlowControl rtbIns;

	private Constants constant;
	/**
	 * 随机数控制开启标签顺序，雨露均沾
	 */
	private Random tagRandom;
	/**
	 * 随机数控制广告进度等顺序，雨露均沾
	 */
	private Random adRandom;

	public RuleMatching(String[] nodes) {
		MDC.put("sift", "rtb");
		redis = new AsyncRedisClient(nodes);
		rtbIns = RtbFlowControl.getInstance();	
		tagRandom = new Random();
		adRandom = new Random();

		// 加载标签溢价比和权重
		constant = Constants.getInstance();

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
	 */
	public boolean filter(int width, int height, int adWidth, int adHeight, boolean isResolutionRatio,
			int widthDeviation, int heightDeviation) {
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
	 * @param tagBean
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
	public DUFlowBean match(String deviceId, String adType, int width, int height, boolean isResolutionRatio,
			int widthDeviation, int heightDeviation) {
		DUFlowBean targetDuFlowBean = null;
		if(deviceId == null || deviceId.trim().equals("")){
			LOG.warn("deviceId["+deviceId+"]为空!");
			return null;
		}
		// 取出标签
		 String tagJson = redis.getAsync(deviceId);
		 TagBean tagBean = (TagBean) JsonTools.fromJson(tagJson);
		
		if(tagBean == null){
			LOG.warn("TAGBEAN["+tagBean+"]为空!");
			return null;
		}

		// 开始匹配
		int divisor = MathTools.division(width, height);
		String creativeKey = adType + "_" + width / divisor + "/" + height / divisor;
		List<String> auidList = rtbIns.getCreativeRatioMap().get(creativeKey);
		
		if(auidList == null){
			LOG.warn("根据["+creativeKey+"]未找到广告!");
			return null;
		}

		// String creativeKey = adType + "_" + width + "_" + height;
		// List<String> auidList = rtbIns.getCreativeMap().get(creativeKey);

		List<AdBean> machedAdList = new ArrayList<AdBean>();// 匹配到的广告资源列表

		// 开始遍历符合广告素材尺寸的广告
		for (String adUid : auidList) {
			long startTime = System.currentTimeMillis();
			boolean isAvaliable = rtbIns.checkAvalable(adUid);
			// 是否投当前的广告
			if (!isAvaliable) {
				LOG.debug("ID[" + adUid + "]广告不参与投放!");
				continue;
			}
			AdBean ad = rtbIns.getAdMap().get(adUid);
			CreativeBean creative = ad.getCreativeList().get(0);
			if (!filter(width, height, creative.getWidth(), creative.getHeight(), isResolutionRatio, widthDeviation,
					heightDeviation)) {
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
					if (rtbIns.getAreaMap().get(key).contains(ad.getAdUid()) && (commonMatch(tagBean, audience))) {
						//LOG.debug("ID[" + ad.getAdUid() + "]通过匹配，参与排序");//记录日志太花费时间,忽略
						machedAdList.add(ad);
					}
					
				} else {// 按照经纬度匹配
					boolean isInBound = this.checkInBoudByType(audience.getMobilityType(), tagBean);
					if (isInBound && (commonMatch(tagBean, audience))) {
						//LOG.debug("ID[" + ad.getAdUid() + "]通过匹配，参与排序");//记录日志太花费时间,忽略
						machedAdList.add(ad);
					}
				}
			} else if (audience.getType().equals("demographic")) { // 特定人群
				String tagIdStr = tagBean.getTagIdList();
				String tagIds[] = tagIdStr.split(",");
				if (audience.getDemographicTagIdList().containsAll(Arrays.asList(tagIds))) {
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
							&& (commonMatch(tagBean, audience))) {
						//LOG.debug("ID[" + ad.getAdUid() + "]通过匹配，参与排序");//记录日志太花费时间,忽略
						machedAdList.add(ad);
					}
				}

			} else if (audience.getType().equals("company")) { // 具体公司
				String companyIdStr = tagBean.getCompanyIdList();
				String companyIds[] = companyIdStr.split(",");
				if (audience.getCompanyIdList().containsAll(Arrays.asList(companyIds))) {// 涉及到库中存储的数据样式和标签中的样式
					//LOG.debug("ID[" + ad.getAdUid() + "]通过匹配，参与排序");//记录日志太花费时间,忽略
					machedAdList.add(ad);
				}
			}
		}

		// 排序
		if(machedAdList.size()>0)
		targetDuFlowBean = order(deviceId,machedAdList,tagBean);

		return targetDuFlowBean;
	}

	/**
	 * 对匹配的广告按照规则进行排序
	 */
	public DUFlowBean order(String deviceId,List<AdBean> machedAdList,TagBean tagBean) {

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
			targetDuFlowBean = packageDUFlowData(deviceId,ad,tagBean);
		} else {
			gradeOrderByPremiumStrategy(machedAdList);
			gradeOrderOtherParaStrategy(machedAdList);
		
			AdBean ad = gradeByRandom(machedAdList);
			// 封装返回接口引擎数据
			LOG.debug("ID[" + ad.getAdUid() + "]通过排序获得竞价资格!");
			targetDuFlowBean = packageDUFlowData(deviceId,ad,tagBean);
		}

		return targetDuFlowBean;
	}

	/**
	 * 需要确认标签中的值和人群中的值
	 * @param tagBean
	 * @param audience
	 * @return
	 */
	public boolean commonMatch(TagBean tagBean, AudienceBean audience) {
		// 匹配收入
		if (tagBean.getIncomeId() != audience.getIncomeLevel()) {
			return false;
		}
		// 匹配兴趣
		if (!tagBean.getAppPreferenceIds().equals(audience.getAppPreferenceIds())) {
			return false;
		}
		// 匹配平台
		if (tagBean.getPlatformId() != audience.getPlatformId()) {
			return false;
		}

		// 匹配品牌
		if (!tagBean.getBrand().equals(audience.getBrandIds())) {//可以多选，不限是空值
			return false;
		}

		// 匹配设备价格
		if (tagBean.getPhonePrice() != audience.getPhonePriceLevel()) {
			return false;
		}

		// 扩展

		return true;
	}

	/**
	 * 根据流动性返回坐标
	 * 
	 * @param type
	 * @param tagBean
	 * @return
	 */
	public boolean checkInBoudByType(int type, TagBean tagBean) {
		float[] residenceArray = tagBean.getResidence();
		float[] workArray = tagBean.getWork();
		float[] activityArray = tagBean.getActivity();
		switch (type) {
		case 0:
			if (rtbIns.checkInBound(residenceArray[0], residenceArray[1]).size() > 0
					|| rtbIns.checkInBound(workArray[0], workArray[1]).size() > 0
					|| rtbIns.checkInBound(activityArray[0], activityArray[1]).size() > 0) {
				return true;
			}
		case 1:
			return rtbIns.checkInBound(residenceArray[0], residenceArray[1]).size() > 0;
		case 2:
			return rtbIns.checkInBound(workArray[0], workArray[1]).size() > 0;
		case 3:
			return rtbIns.checkInBound(activityArray[0], activityArray[1]).size() > 0;
		}
		return false;
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
				String premiumRatio1 = constant.getConf(type1);
				String premiumRatio2 = constant.getConf(type2);
				float realPrice1 = (Float.parseFloat(premiumRatio1)) * o1.getPrice();
				float realPrice2 = (Float.parseFloat(premiumRatio2)) * o2.getPrice();
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
				float score1 = property1.getImpProcess() * Float.parseFloat(constant.getConf("IMP_PROCESS"))
						+ property1.getCreativeQuality() * Float.parseFloat(constant.getConf("CREATIVE_QUALITY"))
						+ property1.getMoneyLeft() * Float.parseFloat(constant.getConf("MONEY_LEFT"))
						+ property1.getAdvertiserScore() * Float.parseFloat(constant.getConf("ADVERTISER_SCORE"))
						+ property1.getCtrScore() * Float.parseFloat(constant.getConf("CTR_SCORE"));

				float score2 = property2.getImpProcess() * Float.parseFloat(constant.getConf("IMP_PROCESS"))
						+ property2.getCreativeQuality() * Float.parseFloat(constant.getConf("CREATIVE_QUALITY"))
						+ property2.getMoneyLeft() * Float.parseFloat(constant.getConf("MONEY_LEFT"))
						+ property2.getAdvertiserScore() * Float.parseFloat(constant.getConf("ADVERTISER_SCORE"))
						+ property2.getCtrScore() * Float.parseFloat(constant.getConf("CTR_SCORE"));

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

	public DUFlowBean packageDUFlowData(String deviceId,AdBean ad,TagBean tagBean) {
		DUFlowBean targetDuFlowBean = new DUFlowBean();
		CreativeBean creative = ad.getCreativeList().get(0);
		AudienceBean audience = ad.getAudienceList().get(0);
		AdvertiserBean advertiser = ad.getAdvertiser();
		targetDuFlowBean.setBidid("广告竞价ID");//广告竞价ID
		targetDuFlowBean.setAdm(creative.getFileName());//广告素材
		targetDuFlowBean.setAdw(creative.getWidth());
		targetDuFlowBean.setAdh(creative.getHeight());
		targetDuFlowBean.setCrid(creative.getUid());
		targetDuFlowBean.setAdmt(creative.getType());
		targetDuFlowBean.setAdct(1);//点击广告行为
		targetDuFlowBean.setAdUid(ad.getAdUid());
		targetDuFlowBean.setDid(deviceId);
		//targetDuFlowBean.setAudienceuid("人群ID");
		targetDuFlowBean.setAdvertiserUid(advertiser.getUid());
		//targetDuFlowBean.setAgencyUid("代理商ID");
		targetDuFlowBean.setCreativeUid(creative.getUid());
		//targetDuFlowBean.setProvince("省");//省
		//targetDuFlowBean.setCity("市");//市
		//targetDuFlowBean.setActualPrice(1.0);//成本价
		String type = audience.getType().toUpperCase();
		double premiumRatio = Double.parseDouble(constant.getConf(type));
		//targetDuFlowBean.setActualPricePremium(premiumRatio*((double)ad.getPrice()));//溢价
		targetDuFlowBean.setBiddingPrice((double)ad.getPrice());
		targetDuFlowBean.setPremiumFactor(premiumRatio);
		targetDuFlowBean.setLandingUrl(creative.getLanding());
		targetDuFlowBean.setLinkUrl(creative.getLink());
		targetDuFlowBean.setTracking(creative.getTracking());
		return targetDuFlowBean;
	}
	
}
