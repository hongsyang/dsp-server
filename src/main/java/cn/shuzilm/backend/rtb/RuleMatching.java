package cn.shuzilm.backend.rtb;

import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdPropertyBean;
import cn.shuzilm.bean.control.TaskBean;
import cn.shuzilm.bean.dmp.AreaBean;
import cn.shuzilm.bean.dmp.GpsBean;
import cn.shuzilm.bean.dmp.AudienceBean;
import cn.shuzilm.bean.dmp.TagBean;
import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.Constants;
import cn.shuzilm.util.AsyncRedisClient;
import cn.shuzilm.util.JsonTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		redis = new AsyncRedisClient(nodes);
		rtbIns = RtbFlowControl.getInstance();
		tagRandom = new Random();
		adRandom = new Random();

		// 加载标签溢价比和权重
		constant = Constants.getInstance();

	}

	/**
	 * 创意类型、尺寸匹配
	 */
	public void filter(String type, int width, int height) {

	}

	/**
	 * 将设备ID 的标签从加速层取出，并做规则判断
	 * 
	 * @param deviceId
	 */
	public DUFlowBean match(String deviceId, String adType, int width, int height) {

		DUFlowBean targetDuFlowBean = null;
		// 取出标签
		String tagJson = redis.getAsync(deviceId);
		TagBean tagBean = (TagBean) JsonTools.fromJson(tagJson);

		// 开始匹配
		String creativeKey = adType + "_" + width + "_" + height;
		List<String> auidList = rtbIns.getCreativeMap().get(creativeKey);

		List<AdBean> machedAdList = new ArrayList<AdBean>();// 匹配到的广告资源列表

		// 开始遍历符合广告素材尺寸的广告
		for (String adUid : auidList) {

			boolean isAvaliable = rtbIns.checkAvalable(adUid);
			// 是否投当前的广告
			if (!isAvaliable){
				LOG.debug("ID["+adUid+"]广告不参与投放!");
				continue;
			}

			AdBean ad = rtbIns.getAdMap().get(adUid);

			AudienceBean audience = ad.getAudience();

			if (audience.getType().equals("location")) {// 地理位置
				if (audience.getGeos().equals("")) {
					// 省市县的匹配
					String key = null;
					if (tagBean.getCountyId() == 0) {
						key = tagBean.getProvinceId() + "_" + tagBean.getCityId();
					} else {
						key = tagBean.getProvinceId() + "_" + tagBean.getCityId() + "_" + tagBean.getCountyId();
					}
					if (rtbIns.getAreaMap().containsKey(key) && (commonMatch(tagBean, audience))) {
						LOG.debug("ID["+ad.getAdUid()+"]通过匹配，参与排序");
						machedAdList.add(ad);
					}

				} else {// 按照经纬度匹配
					float[] geoArray = this.matchMobilityType(audience.getMobilityType(), tagBean);
					if (geoArray.length > 1) {
						ArrayList<String> uidList = rtbIns.checkInBound(geoArray[0], geoArray[1]);
						if (uidList != null && uidList.size() > 0 && (commonMatch(tagBean, audience))) {
							LOG.debug("ID["+ad.getAdUid()+"]通过匹配，参与排序");
							machedAdList.add(ad);
						}
					}
				}
			} else if (audience.getType().equals("demographic")) { // 特定人群
				if (audience.getDemographicTagIdList().contains(tagBean.getTagIdList())) {// tagBean标签中的特定人群为具体的某一种
					String key = null;
					if (tagBean.getCountyId() == 0) {
						key = tagBean.getProvinceId() + "_" + tagBean.getCityId();
					} else {
						key = tagBean.getProvinceId() + "_" + tagBean.getCityId() + "_" + tagBean.getCountyId();
					}
					if (rtbIns.getAreaMap().containsKey(key) && (commonMatch(tagBean, audience))) {
						LOG.debug("ID["+ad.getAdUid()+"]通过匹配，参与排序");
						machedAdList.add(ad);
					}
				}

			} else if (audience.getType().equals("company")) { // 具体公司
				if (audience.getCompanyIds().contains(tagBean.getTagIdList()) && commonMatch(tagBean, audience)) {// 涉及到库中存储的数据样式和标签中的样式
					LOG.debug("ID["+ad.getAdUid()+"]通过匹配，参与排序");
					machedAdList.add(ad);
				}
			}

		}
		
		// 排序
		targetDuFlowBean = order(machedAdList);

		return targetDuFlowBean;
	}

	/**
	 * 对匹配的广告按照规则进行排序
	 */
	public DUFlowBean order(List<AdBean> machedAdList) {

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
			LOG.debug("ID["+ad.getAdUid()+"]通过排序获得竞价资格!");
			targetDuFlowBean = packageDUFlowData(ad);
		} else {
			gradeOrderByPremiumStrategy(machedAdList);
			gradeOrderOtherParaStrategy(machedAdList);

			AdBean ad = machedAdList.get(0);// 排序完分数最高的
			// 封装返回接口引擎数据
			LOG.debug("ID["+ad.getAdUid()+"]通过排序获得竞价资格!");
			targetDuFlowBean = packageDUFlowData(ad);
		}

		return targetDuFlowBean;
	}

	/**
	 * 
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
		if (!tagBean.getBrand().equals(audience.getBrandIds())) {
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
	public float[] matchMobilityType(int type, TagBean tagBean) {
		switch (type) {
		case 0:
			return tagBean.getResidence();// 不限制时使用居住地
		case 1:
			return tagBean.getResidence();
		case 2:
			return tagBean.getWork();
		case 3:
			return tagBean.getActivity();
		}
		return null;
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
				AudienceBean audience1 = o1.getAudience();
				AudienceBean audience2 = o2.getAudience();
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
	
	public DUFlowBean packageDUFlowData(AdBean ad){
		DUFlowBean targetDuFlowBean = new DUFlowBean();
		
		return targetDuFlowBean;
	}
}
