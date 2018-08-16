package cn.shuzilm.backend.rtb;

import cn.shuzilm.backend.master.AdFlowControl;
import cn.shuzilm.backend.master.MsgControlCenter;
import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdPropertyBean;
import cn.shuzilm.bean.control.AdvertiserBean;
import cn.shuzilm.bean.control.CreativeBean;
import cn.shuzilm.bean.control.Material;
import cn.shuzilm.bean.control.TaskBean;
import cn.shuzilm.bean.dmp.AreaBean;
import cn.shuzilm.bean.dmp.AudienceBean;
import cn.shuzilm.bean.dmp.GpsBean;
import cn.shuzilm.bean.dmp.GpsGridBean;
import cn.shuzilm.common.Constants;
import cn.shuzilm.util.MathTools;
import cn.shuzilm.util.TimeUtil;
import cn.shuzilm.util.geo.GeoHash;
import cn.shuzilm.util.geo.GridMark;
import cn.shuzilm.util.geo.GridMark2;
import com.jcraft.jsch.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by thunders on 2018/7/17.
 */
public class RtbFlowControl {
	private static final org.slf4j.Logger myLog = LoggerFactory.getLogger(AdFlowControl.class);
	private static RtbFlowControl rtb = null;

	private SimpleDateFormat dateFm = new SimpleDateFormat("EEEE_hh");

	public static RtbFlowControl getInstance() {
		if (rtb == null) {
			rtb = new RtbFlowControl();
		}
		return rtb;
	}

	public static void main(String[] args) {
		// 从主控节点读取一些数据
		AdFlowControl.getInstance().loadAdInterval(true);
		// 测试 RTB 引擎的
		RtbFlowControl.getInstance().trigger();

	}

	public ConcurrentHashMap<String, AdBean> getAdMap() {
		return mapAd;
	}

	public ConcurrentHashMap<String, List<String>> getMaterialMap() {
		return mapAdMaterial;
	}

	public ConcurrentHashMap<String, List<String>> getMaterialRatioMap() {
		return mapAdMaterialRatio;
	}

	public ConcurrentHashMap<String, Set<String>> getAreaMap() {
		return areaMap;
	}

	public ConcurrentHashMap<String, Set<String>> getDemographicMap() {
		return demographicMap;
	}

	private String nodeName;
	/**
	 * 广告资源管理 key: aduid : taskBean
	 */
	private static ConcurrentHashMap<String, AdBean> mapAd = null;

	/**
	 * 广告任务管理
	 */
	private static ConcurrentHashMap<String, TaskBean> mapTask = null;
	/**
	 * 广告资源的倒置 key: 广告类型 + 广告宽 + 广告高 value: list<aduid>
	 */
	private static ConcurrentHashMap<String, List<String>> mapAdMaterial = null;

	/**
	 * 广告资源的倒置 key: 广告类型 + (广告宽/广告高) value: list<aduid>
	 */
	private static ConcurrentHashMap<String, List<String>> mapAdMaterialRatio = null;

	/**
	 * 省级、地级、县级 map key: 北京市北京市海淀区 河北省廊坊地区广平县 value：aduid
	 */
	private static ConcurrentHashMap<String, Set<String>> areaMap = null;

	private static ConcurrentHashMap<String, Set<String>> demographicMap = null;

	// 判断标签坐标是否在 广告主的选取范围内
	private HashMap<Integer, GridMark2> gridMap = null;

	private RtbFlowControl() {
		MDC.put("sift", "rtb");
		nodeName = Constants.getInstance().getConf("HOST");
		this.nodeName = nodeName;
		mapAd = new ConcurrentHashMap<>();
		mapTask = new ConcurrentHashMap<>();
		areaMap = new ConcurrentHashMap<>();
		demographicMap = new ConcurrentHashMap<>();
		mapAdMaterial = new ConcurrentHashMap<>();
		mapAdMaterialRatio = new ConcurrentHashMap<>();
		// 判断标签坐标是否在 广告主的选取范围内
		gridMap = new HashMap<>();

	}

	public void trigger() {
		// 5 s
		pullAndUpdateTask();

		// 10分钟拉取一次最新的广告内容
		pullTenMinutes(null);

		// 1 hour
		refreshAdStatus();
	}

	/**
	 * 检查设备的标签所带的居住地、工作地、活动地坐标
	 * 
	 * @param lng
	 *            0 不限 1 居住地 2 工作地 3 活动地
	 * @param lat
	 *            0 不限 1 居住地 2 工作地 3 活动地
	 * @return
	 */
	public HashSet<String> checkInBound(double[] lng, double[] lat) {
		HashSet<String> allUidSet = new HashSet<>();
		for (int i = 0; i < lng.length; i++) {
			if (lng[i] != 0.0d) {
				ArrayList<String> uidList = gridMap.get(i).findGrid(lng[i], lat[i]);
				allUidSet.addAll(uidList);
			}
		}

		return allUidSet;
	}

	/**
	 * 每隔 10 分钟更新一次广告素材或者人群包
	 */
	public void pullTenMinutes(ArrayList<AdBean> adBeanList) {
		// 从 10 分钟的队列中获得广告素材和人群包
		 //ArrayList<AdBean> adBeanList = MsgControlCenter.recvAdBean(nodeName);
		ArrayList<GpsBean> gpsAll = new ArrayList<>();
		ArrayList<GpsBean> gpsResidenceList = new ArrayList<>();
		ArrayList<GpsBean> gpsWorkList = new ArrayList<>();
		ArrayList<GpsBean> gpsActiveList = new ArrayList<>();
		if (adBeanList != null) {
			for (AdBean adBean : adBeanList) {
				// 广告ID
				String uid = adBean.getAdUid();

				mapAd.put(uid, adBean);
				List<AudienceBean> audienceList = adBean.getAudienceList();
				if (audienceList.size() == 0) {
					myLog.error(adBean.getAdUid() + "\t" + adBean.getName() + " 没有设置人群包..");
				}
				for (AudienceBean audience : audienceList) {
					if (audience != null) {
						// 加载人群中的GEO位置信息
						switch (audience.getMobilityType()) {
						case 0:// 不限
							// 将 经纬度坐标装载到 MAP 中，便于快速查找
							ArrayList<GpsBean> gpsList = audience.getGeoList();
							if (gpsList != null) {
								for (GpsBean gps : gpsList) {
									gps.setPayload(uid);
								}
								gpsAll.addAll(gpsList);
							}
							break;
						case 1:// 居住地
								// 将 经纬度坐标装载到 MAP 中，便于快速查找
							gpsList = audience.getGeoList();
							if (gpsList != null) {
								for (GpsBean gps : gpsList) {
									gps.setPayload(uid);
								}
								gpsResidenceList.addAll(gpsList);
							}
							break;
						case 2:// 工作地
								// 将 经纬度坐标装载到 MAP 中，便于快速查找
							gpsList = audience.getGeoList();
							if (gpsList != null) {
								for (GpsBean gps : gpsList) {
									gps.setPayload(uid);
								}
								gpsWorkList.addAll(gpsList);
							}
							break;
						case 3:// 活动地
								// 将 经纬度坐标装载到 MAP 中，便于快速查找
							gpsList = audience.getGeoList();
							if (gpsList != null) {
								for (GpsBean gps : gpsList) {
									gps.setPayload(uid);
								}
								gpsActiveList.addAll(gpsList);
							}
							break;
						default:
							break;
						}

						// 将 省、地级、县级装载到 MAP 中，便于快速查找
						List<AreaBean> areaList = audience.getCityList();
						String key = null;
						for (AreaBean area : areaList) {
							if (area.getProvinceId() == 0) {
								// 当省选项为 0 的时候，则认为是匹配全国
								key = "china";
							} else if (area.getCityId() == 0) {
								// 当市级选项为 0 的时候，则认为是匹配全省
								key = area.getProvinceId() + "";
							} else if (area.getCountyId() == 0) {
								// 当县级选项为 0 的时候，则认为是匹配全市
								key = area.getProvinceId() + "_" + area.getCityId();
							} else {
								key = area.getProvinceId() + "_" + area.getCityId() + "_" + area.getCountyId();
							}

							if (!areaMap.containsKey(key)) {
								Set<String> set = new HashSet<String>();
								set.add(adBean.getAdUid());
								areaMap.put(key, set);
							} else {
								Set<String> set = areaMap.get(key);
								set.add(adBean.getAdUid());
							}

							if (!demographicMap.containsKey(key)) {
								Set<String> set = new HashSet<String>();
								set.add(adBean.getAdUid());
								demographicMap.put(key, set);
							} else {
								Set<String> set = demographicMap.get(key);
								set.add(adBean.getAdUid());
							}
						}
					}
				}
				// 广告内容的更新 ，按照素材的类型和尺寸
				CreativeBean creative = adBean.getCreativeList().get(0);
				List<Material> materialList = creative.getMaterialList();
				for (Material material : materialList) {
					int width = material.getWidth();
					int height = material.getHeight();
					int divisor = MathTools.division(width, height);
					String materialKey = material.getType() + "_" + width + "_" + +height;
					String materialRatioKey = material.getType() + "_" + width / divisor + "/" + height / divisor;

					if (!mapAdMaterial.containsKey(materialKey)) {
						List<String> uidList = new ArrayList<String>();
						uidList.add(uid);
						mapAdMaterial.put(materialKey, uidList);
					} else {
						List<String> uidList = mapAdMaterial.get(materialKey);
						if (!uidList.contains(uid)) {
							uidList.add(uid);
						}
					}

					if (!mapAdMaterialRatio.containsKey(materialRatioKey)) {
						List<String> uidList = new ArrayList<String>();
						uidList.add(uid);
						mapAdMaterialRatio.put(materialRatioKey, uidList);
					} else {
						List<String> uidList = mapAdMaterialRatio.get(materialRatioKey);
						if (!uidList.contains(uid)) {
							uidList.add(uid);
						}
					}
				}
			}

			gridMap.clear();
			// 将 GPS 坐标加载到 栅格快速比对处理类中
			// gridMap.put(0,new GridMark2(gpsAll));
			// gridMap.put(1,new GridMark2(gpsResidenceList));
			// gridMap.put(2,new GridMark2(gpsWorkList));
			// gridMap.put(3,new GridMark2(gpsActiveList));

			myLog.info("广告共计加载条目数 : " + adBeanList.size());
			myLog.info("广告中的经纬度坐标共计条目数：" + gpsAll.size());

		}

	}

	/**
	 * 每隔 5 秒钟从消息中心获得当前节点的当前任务，并与当前两个 MAP monitor 进行更新 不包括素材
	 *
	 */
	public void pullAndUpdateTask() {
		TaskBean task = MsgControlCenter.recvTask(nodeName);
		if (task == null) {
			return;
		}
		if (task != null) {
			// 把最新的任务更新到 MAP task 中
			mapTask.put(task.getAdUid(), task);
		}

	}

	/**
	 * 每个小时重置一次 重置每个小时的投放状态，如果为暂停状态，且作用域为小时，则下一个小时可以继续开始
	 */
	public void refreshAdStatus() {
		for (String auid : mapTask.keySet()) {
			TaskBean bean = mapTask.get(auid);
			AdBean ad = mapAd.get(auid);

			ad.getPropertyBean();

			int scope = bean.getScope();
			int commandCode = bean.getCommand();
			if (scope == TaskBean.SCOPE_HOUR && commandCode == TaskBean.COMMAND_PAUSE) {
				bean.setCommand(TaskBean.COMMAND_START);
			}
		}
	}

	/**
	 * 监测广告是否可用
	 * 
	 * @param auid
	 * @return
	 */
	public boolean checkAvalable(String auid) {
		TaskBean bean = mapTask.get(auid);
		if (bean != null) {
			int commandCode = bean.getCommand();
			// int scope = bean.getScope();
			// public static final int TASK_STATE_READY = 0;
			// public static final int TASK_STATE_RUNNING = 1;
			// public static final int TASK_STATE_FINISHED = 2;
			// public static final int TASK_STATE_PAUSED = 3;
			// public static final int TASK_STATE_STOPED = 4;
			switch (commandCode) {
			case TaskBean.COMMAND_PAUSE:
				return false;
			case TaskBean.COMMAND_STOP:
				return false;
			default:
				break;
			}
		}

		// 匹配广告投放时间窗
		// AdBean adBean = mapAd.get(auid);
		// if (adBean != null) {
		// int[][] timeSchedulingArr = adBean.getTimeSchedulingArr();
		// Date date = new Date();
		// String time = dateFm.format(date);
		// String splitTime[] = time.split("_");
		// int weekNum = TimeUtil.weekDayToNum(splitTime[0]);
		// int dayNum = Integer.parseInt(splitTime[1]);
		// if (dayNum == 24)
		// dayNum = 0;
		// for (int i = 0; i < timeSchedulingArr.length; i++) {
		// if (weekNum != i)
		// continue;
		// for (int j = 0; j < timeSchedulingArr[i].length; j++) {
		// if (dayNum == j) {
		// if (timeSchedulingArr[i][j] == 1) {
		// return true;
		// } else {
		// return false;
		// }
		// }
		// }
		// }
		// }
		return true;
	}
}
