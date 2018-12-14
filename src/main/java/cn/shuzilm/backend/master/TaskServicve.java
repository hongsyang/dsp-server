package cn.shuzilm.backend.master;

import cn.shuzilm.bean.control.*;
import cn.shuzilm.bean.dmp.AudienceBean;
import com.yao.util.db.bean.ResultList;
import com.yao.util.db.bean.ResultMap;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by thunders on 2018/7/11.
 */
public class TaskServicve extends Service {
	
	private SimpleDateFormat dateFm = new SimpleDateFormat("yyyyMM");
	
	private SimpleDateFormat specDateFM = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
	private SimpleDateFormat yydDateFM = new SimpleDateFormat("yyyy-MM-dd");
	
	private SimpleDateFormat hourDateFM = new SimpleDateFormat("HH");
		
	private static final int INTERVAL = 5 * 60 * 1000;
    /**
     * 查找 10 分钟前的 人群包条件
     *
     * @return
     * @throws java.sql.SQLException
     */
    public List<AudienceBean> queryAudienceByUpTime(String adUid) throws SQLException {
        ArrayList<AudienceBean> aList = new ArrayList<>();

        Object[] arr = new Object[1];
        arr[0] = adUid;
        String sql = "SELECT c.* FROM ad JOIN map_ad_audience b ON ad.uid = b.ad_uid JOIN audience c ON b.audience_uid = c.uid WHERE b.ad_uid = ?";
        ResultList list =  select.select(sql,arr);
        for(ResultMap rm : list) {
            AudienceBean bean = new AudienceBean();
            bean.setAdUid(adUid);
            bean.setUid(rm.getString("uid"));
            bean.setName(rm.getString("name"));
            bean.setType(rm.getString("type"));
            bean.setAdviserId(rm.getString("advertiser_uid"));

            //特定人群
            bean.setDemographicCitys(rm.getString("demographic_city"));
            bean.setDemographicTagId(rm.getString("demographic_tag"));
            //兴趣偏好标签
            bean.setAppPreferenceIds(rm.getString("app_preference_ids"));
            bean.setBrandIds(rm.getString("brand_ids"));
            bean.setCarrierId(rm.getString("carrier_id"));
            //选定城市或者经纬度 工作地、居住地、活动地
            bean.setMobilityType(rm.getString("location_type"));
            bean.setCitys(rm.getString("location_city"));
            bean.setGeos(rm.get("location_map") != null ? rm.getString("location_map") : "");
            bean.setIncomeLevel(rm.getString("income_level"));
            bean.setNetworkId(rm.getString("network_id"));
            bean.setPhonePriceLevel(rm.getString("phone_price_level"));
            bean.setPlatformId(rm.getString("platform_id"));
            bean.setLocationMode(rm.getString("location_mode"));
            //特定公司
            bean.setCompanyIds(rm.getString("company_ids"));
            //智能设备
            bean.setIps(rm.getString("ips"));
            //定制人群包ID
            bean.setDmpId(rm.getString("dmp_tag"));
            aList.add(bean);

        }
        return aList;
    }
    
    /**
     * 查找 5 分钟前的 人群包条件
     *
     * @return
     * @throws java.sql.SQLException
     */
    public List<AudienceBean> queryAudienceByUpTime() throws SQLException {
    	ArrayList<AudienceBean> aList = new ArrayList<>();
        long timeNow = System.currentTimeMillis();
        long timeBefore = timeNow - INTERVAL;
        Object[] arr = new Object[1];
        arr[0] = timeBefore / 1000;
        String sql = "SELECT m.ad_uid,a.* FROM audience a JOIN map_ad_audience m ON m.audience_uid = uid where updated_at >= ?";
        ResultList list =  select.select(sql,arr);
        for(ResultMap rm : list) {
        	AudienceBean bean = new AudienceBean();
            bean.setAdUid(rm.getString("ad_uid"));
            bean.setUid(rm.getString("uid"));
            bean.setName(rm.getString("name"));
            bean.setType(rm.getString("type"));
            bean.setAdviserId(rm.getString("advertiser_uid"));

            //特定人群
            bean.setDemographicCitys(rm.getString("demographic_city"));
            bean.setDemographicTagId(rm.getString("demographic_tag"));
            //兴趣偏好标签
            bean.setAppPreferenceIds(rm.getString("app_preference_ids"));
            bean.setBrandIds(rm.getString("brand_ids"));
            bean.setCarrierId(rm.getString("carrier_id"));
            //选定城市或者经纬度 工作地、居住地、活动地
            bean.setMobilityType(rm.getString("location_type"));
            bean.setCitys(rm.getString("location_city"));
            bean.setGeos(rm.get("location_map") != null ? rm.getString("location_map") : "");
            bean.setIncomeLevel(rm.getString("income_level"));
            bean.setNetworkId(rm.getString("network_id"));
            bean.setPhonePriceLevel(rm.getString("phone_price_level"));
            bean.setPlatformId(rm.getString("platform_id"));
            bean.setLocationMode(rm.getString("location_mode"));
            //特定公司
            bean.setCompanyIds(rm.getString("company_ids"));
            //智能设备
            bean.setIps(rm.getString("ips"));
            //定制人群包ID
            bean.setDmpId(rm.getString("dmp_tag"));
            aList.add(bean);

        }
        return aList;
    }

    /**
     * 查找 10 分钟前的数据
     *
     * @return
     * @throws java.sql.SQLException
     */
    public ResultList queryAdByUpTime(long startTime) throws SQLException {
        long now = System.currentTimeMillis() ;
        Object[] arr = new Object[3];
        arr[0] = startTime / 1000;
        //arr[0] = specDateFM.format(new Date(startTime));
        arr[1] = now / 1000;
        arr[2] = now / 1000;
        String sql = "select * from ad where updated_at >= ? and s <= ? and e >= ? and status = 1 and group_status = 1";
        return select.select(sql,arr);
    }
    
    /**
     * 查找 全部的广告
     *
     * @return
     * @throws java.sql.SQLException
     */
    public ResultList queryAllAd() throws SQLException {
        String sql = "select * from ad";
        return select.select(sql);
    }
    
    /**
     * 查找 全部的广告(包括超时的)
     *
     * @return
     * @throws java.sql.SQLException
     */
    public ResultList queryAllAdTotal(long startTime) throws SQLException { 
    	Object[] arr = new Object[1];
        arr[0] = startTime / 1000;
        String sql = "select * from ad where updated_at >= ?";
        return select.select(sql,arr);
    }

    /**
     * 根据 AD UID 查询广告主
     * @param adverUid
     * @return
     */
    public AdvertiserBean queryAdverByUid(String adverUid){
        String sql = "select * from advertiser where uid = '"+ adverUid+"'";
        try {
            AdvertiserBean adver = new AdvertiserBean();
            ResultMap rm =  select.selectSingle(sql);
            if(rm == null)
                return null;
            adver.setAgencyUid(rm.getString("agency_uid"));
            AgencyBean agencyBean = queryAgencyByAdviserUid(adver.getAgencyUid());
            adver.setAgencyBean(agencyBean);
            adver.setName(rm.getString("name"));
            adver.setUid(rm.getString("uid"));
            adver.setGrade(rm.getInteger("priority"));
            return adver;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 查询5分钟内的广告主
     * @return
     */
    public List<AdvertiserBean> queryAdverByUpTime() throws SQLException{
    	List<AdvertiserBean> advertiserList = new ArrayList<AdvertiserBean>();
    	 long timeNow = System.currentTimeMillis();
         long timeBefore = timeNow - INTERVAL;
         Object[] arr = new Object[1];
         arr[0] = timeBefore / 1000;
        String sql = "select * from advertiser where updated_at >= ?";            
            ResultList list =  select.select(sql,arr);
            for(ResultMap rm:list){
            	AdvertiserBean adver = new AdvertiserBean();
            	adver.setAgencyUid(rm.getString("agency_uid"));
            	AgencyBean agencyBean = queryAgencyByAdviserUid(adver.getAgencyUid());
            	adver.setAgencyBean(agencyBean);
            	adver.setName(rm.getString("name"));
            	adver.setUid(rm.getString("uid"));
            	adver.setGrade(rm.getInteger("priority"));
            	advertiserList.add(adver);
            }
            return advertiserList;
    }

    public AgencyBean queryAgencyByAdviserUid(String agencyUid){

        String sql = "select * from agency where uid = '" + agencyUid +"'";
        try{
            AgencyBean bean = new AgencyBean();
            ResultMap rm =  select.selectSingle(sql);
            if(rm == null)
                return null;
            bean.setUid(rm.getString("uid"));
            bean.setAbbr(rm.getString("name"));
            bean.setCompany(rm.getString("company"));
            bean.setName(rm.getString("name"));
            bean.setRebate(rm.getFloat("rebate"));
            bean.setRemark(rm.getString("remark"));
            return bean;

        }catch(SQLException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据 AD UID 查询 创意
     * @param creativeUid
     * @return
     */
    public CreativeBean queryCreativeUidByAid(String creativeUid){
        String sql = "select * from creative where uid = '"+ creativeUid +"'";
        try {
            CreativeBean creativeBean = new CreativeBean();
            ResultMap cMap = select.selectSingle(sql);
            if(cMap == null)
                return null;
            creativeBean.setName(cMap.getString("name"));
            creativeBean.setUid(creativeUid);
            creativeBean.setBrand(cMap.getString("brand"));
            creativeBean.setDesc(cMap.getString("text"));
            creativeBean.setDescLong(cMap.getString("text_long"));
//            creativeBean.setDescShort(cMap.getString("text_short"));
            creativeBean.setDomain(cMap.getString("brand_domain"));
            creativeBean.setTitle(cMap.getString("title"));
            creativeBean.setTitleLong(cMap.getString("title_long"));
//            creativeBean.setTitleShort(cMap.getString("title_short"));
            creativeBean.setLink_type(Integer.parseInt(cMap.getString("link_type")));
            creativeBean.setLink(cMap.getString("link_uri"));
            creativeBean.setLanding(cMap.getString("landing_uri"));
            creativeBean.setTracking(cMap.getString("tracking_uri"));
            creativeBean.setClickTrackingUrl(cMap.getString("click_tracking_uri"));
            creativeBean.setApproved(cMap.getInteger("approved"));
            //creativeBean.setApproved_adx(cMap.getString("approved_adx"));
            creativeBean.setType(cMap.getString("type"));
            return creativeBean;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 查找5分钟前的创意
     * @return
     */
    public List<CreativeBean> queryCreativeByUpTime() {
    	List<CreativeBean> creativeList = new ArrayList<CreativeBean>();
    	long timeNow = System.currentTimeMillis();
        long timeBefore = timeNow - INTERVAL;
        Object[] arr = new Object[1];
        //arr[0] = timeBefore / 1000;
        arr[0] = specDateFM.format(new Date(timeBefore));
        String sql = "select a.uid ad_uid,c.* from creative c JOIN ad a ON c.uid=a.creative_uid where c.refresh_ts >= ?";
        try {           
            ResultList list =  select.select(sql,arr);
            for(ResultMap cMap : list) {
            	CreativeBean creativeBean = new CreativeBean();
            	creativeBean.setRelatedAdUid(cMap.getString("ad_uid"));
            	creativeBean.setName(cMap.getString("name"));
                creativeBean.setUid(cMap.getString("uid"));
                creativeBean.setBrand(cMap.getString("brand"));
                creativeBean.setDesc(cMap.getString("text"));
                creativeBean.setDescLong(cMap.getString("text_long"));
//                creativeBean.setDescShort(cMap.getString("text_short"));
                creativeBean.setDomain(cMap.getString("brand_domain"));
                creativeBean.setTitle(cMap.getString("title"));
                creativeBean.setTitleLong(cMap.getString("title_long"));
//                creativeBean.setTitleShort(cMap.getString("title_short"));
                creativeBean.setLink_type(Integer.parseInt(cMap.getString("link_type")));
                creativeBean.setLink(cMap.getString("link_uri"));
                creativeBean.setLanding(cMap.getString("landing_uri"));
                creativeBean.setTracking(cMap.getString("tracking_uri"));
                creativeBean.setClickTrackingUrl(cMap.getString("click_tracking_uri"));
                creativeBean.setApproved(cMap.getInteger("approved"));
                //creativeBean.setApproved_adx(cMap.getString("approved_adx"));
                creativeBean.setType(cMap.getString("type"));
                creativeList.add(creativeBean);
            }
            
            return creativeList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 根据创意 ID 查询 物料
     * @return
     */
    public ArrayList<Material> queryMaterialByCreativeId(String creativeUid){
        String sql = "select * from material where creative_uid = '"+ creativeUid +"'";
        ResultList rl = null;
        try {
            rl = select.select(sql);
            ArrayList<Material> list = new ArrayList<>();
            for(ResultMap rm : rl){
                Material material = new Material();
                material.setUid(rm.getString("uid"));
                material.setNid(rm.getString("nid"));
                material.setCreativeUid(creativeUid);
                material.setType(rm.getString("type"));
                material.setFileName(rm.getString("filename"));
                material.setExt(rm.getString("ext"));
                material.setSize(rm.getInteger("size"));
                material.setWidth(rm.getInteger("w"));
                material.setHeight(rm.getInteger("h"));
                material.setApproved_adx(rm.getString("approved_adx"));
                material.setDuration(rm.getInteger("duration"));
                material.setAuditId(rm.getString("audit_id"));
                list.add(material);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获得所有的 RTB 主机节点
     * @return
     */
    public ArrayList<WorkNodeBean> getWorkNodeAll(){
        String sql = "select * from work_node where status = 1";
        ResultList rl = null;
        try {
            rl = select.select(sql);
            ArrayList<WorkNodeBean> list = new ArrayList<>();
            for(ResultMap rm : rl){
                WorkNodeBean node = new WorkNodeBean();
                node.setId(rm.getInteger("id"));
                node.setIp(rm.getString("ip"));
                node.setMemo(rm.getString("memo"));
                node.setName(rm.getString("name"));
                node.setStatus(rm.getInteger("status"));
                list.add(node);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 广告主余额查询
     * @param adviserId
     * @return
     */
    public ResultMap queryAdviserAccountById(String adviserId){
        String sql = "select * from balance where advertiser_uid = '"+adviserId+"'";

        try {
            ResultMap cMap =  select.selectSingle(sql);
            return cMap;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 广告分组查询
     * @param updateTimeStamp
     * @return
     */
    public ArrayList<GroupAdBean> queryAdGroupAll(long updateTimeStamp){
        long now = System.currentTimeMillis();
        Object[] arr = new Object[1];
        arr[0] = (int)(updateTimeStamp / 1000);

//        String sql = "select a.*, b.uid ad_uid group a join ad b on a.uid = b.group_uid where b.s <= ? and b.e >= ? ";
        String sql = "select * from ad_group where updated_at >= ? and status = 1";
        try{
            ArrayList<GroupAdBean> list = new ArrayList<>();
            ResultList rl = select.select(sql,arr);
            for(ResultMap rm : rl){
                GroupAdBean g = new GroupAdBean();
                g.setGroupId(rm.getString("uid"));
                g.setAdviserId(rm.getString("advertiser_uid"));
                g.setGroupName(rm.getString("name"));
                g.setQuota(rm.getBoolean("quota")==false?0:1);
                g.setQuotaMoney(rm.getBigDecimal("quota_amount"));
                g.setQuota_total(rm.getBoolean("quota_total")==false?0:1);
                g.setQuotaTotalMoney(rm.getBigDecimal("quota_total_amount"));
                list.add(g);
            }
            return list;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }
    
    /**
     * 根据广告组查询广告
     * @param updateTimeStamp
     * @return
     */
    public ArrayList<String> queryAdByGroupId(String groupId){
       Object[] arr = new Object[1];
       arr[0] = groupId;
        String sql = "select uid from ad where group_uid = ? and status = 1";
        try{
            ArrayList<String> list = new ArrayList<>();
            ResultList rl = select.select(sql,arr);
            for(ResultMap rm : rl){                
                list.add(rm.getString("uid"));
            }
            return list;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public ConcurrentHashMap<String,ReportBean> statAdCostTotal(){
//        long startStamp = 0;
//        long nowStamp = System.currentTimeMillis();
        return statAdCost(0);
    }

    /**
     * 加载当前小时 0 分 到现在的账户消耗数据
     * @return
     */
    public ConcurrentHashMap<String,ReportBean> statAdCostHour(){
//        int minute = Calendar.getInstance().get(Calendar.MINUTE);
//        Calendar start = Calendar.getInstance();
//        start.set(Calendar.MINUTE,0);
//        //从这个小时的 0 分作为开始时间
//        long startStamp = start.getTimeInMillis();
//        //以当前时间作为结束时间
//        long nowStamp = System.currentTimeMillis();
        return statAdCost(1);
    }

    public ConcurrentHashMap<String,ReportBean> statAdCostDaily(){
//        Calendar start = Calendar.getInstance();
//        start.set(Calendar.HOUR,0);
//        start.set(Calendar.MINUTE,0);
//        long startStamp = start.getTimeInMillis();
//        long nowStamp = System.currentTimeMillis();
        return statAdCost(2);
    }

    /**
     * 统计费用实际消耗情况
     * @return
     * @param reportType:0/总共,1/小时,2/天
     */
    public ConcurrentHashMap<String,ReportBean> statAdCost(int reportType){
        // type：
        // 0 : 小时存量费用的统计，对于一个小时前，当天的广告耗费的汇总
        // 1 : 天存量费用的统计，
        
        String sql = "";
        Object [] arr = null;
        if(reportType == 1){
        	arr = new Object[2];
            //转换成秒的时间戳
            //arr[0] = startTime / 1000;
            //arr[1] = endTime / 1000;
            Date date = new Date();
    		String time = "_"+dateFm.format(date);
    		arr[0] = yydDateFM.format(date);
    		arr[1] = hourDateFM.format(date);
            sql = "select ad_uid,sum(amount) expense , sum(cost) cost , sum(imp) imp , sum(click) click from reports_hour"+time+" where date = ? and hour = ?  group by ad_uid";
        } else if(reportType == 2){
        	arr = new Object[1];
        	Date date = new Date();
        	arr[0] = yydDateFM.format(date);
            sql = "select ad_uid,sum(amount) expense , sum(cost) cost, sum(imp) imp , sum(click) click from reports where date = ?  group by ad_uid";
        }else{
        	sql = "select ad_uid,sum(amount) expense , sum(cost) cost, sum(imp) imp, sum(click) click from reports  group by ad_uid";
        }
        try {
        	ResultList rl = new ResultList();
        	if(arr == null){
        		rl = select.select(sql);
        	}else{
        		rl = select.select(sql,arr);
        	}
            ConcurrentHashMap<String,ReportBean> map = new ConcurrentHashMap<>();
            for(ResultMap rm : rl){
                ReportBean report = new ReportBean();
                String adUid = rm.getString("ad_uid");
                if(adUid == null){
                	continue;
                }
                BigDecimal expense = rm.getBigDecimal("expense");
                BigDecimal cost = rm.getBigDecimal("cost");
                BigDecimal imp = rm.getBigDecimal("imp");
                BigDecimal click = rm.getBigDecimal("click");
                report.setAdUid(adUid);
                report.setExpense(expense);
                report.setCost(cost);
                report.setImpNums(imp.intValue());
                report.setClickNums(click.intValue());
                map.put(adUid,report);
            }
            return map;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 广告日志入库
     * @param adLog
     * @throws SQLException
     */
    public void insertDataToLog(AdLogBean adLog) throws SQLException{  	
    	String sql = "insert into ad_log (ad_uid,ad_name,advertiser_uid,advertiser_name,created_at,reason,status) "
    			+ "values ('"+adLog.getAdUid()+"','"+adLog.getAdName()+"','"+adLog.getAdvertiserUid()+"',"
    					+ "'"+adLog.getAdvertiserName()+"','"+specDateFM.format(adLog.getCreatedAt())+"','"+adLog.getReason()+"',"+adLog.getStatus()+")";	
    	update.doUpdate(sql);    	
    }
    
    /**
     * 广告明细入库(每小时)
     * @param adNoticeDetail
     * @throws SQLException
     */
    public void insertDataToNoticeDetailPerHour(AdNoticeDetailBean adNoticeDetail) throws SQLException{
    	String sql = "insert into ad_notice_detail_hour (ad_uid,ad_name,advertiser_uid,advertiser_name,win_nums,"
    			+ "click_nums,bid_nums,win_ratio,click_ratio,date,hour) values ('"+adNoticeDetail.getAdUid()+"','"+
    			adNoticeDetail.getAdName()+"','"+adNoticeDetail.getAdvertiserUid()+"','"+adNoticeDetail.getAdvertiserName()+
    			"',"+adNoticeDetail.getWinNums()+","+adNoticeDetail.getClickNums()+","+adNoticeDetail.getBidNums()+","+
    			adNoticeDetail.getWinRatio()+","+adNoticeDetail.getClickRatio()+",'"+yydDateFM.format(new Date())+"','"
    					+hourDateFM.format(new Date())+"')";
    	update.doUpdate(sql);
    }
    
    /**
     * 获取前一天广告明细
     */
    public ConcurrentHashMap<Integer,Long> getNoticeDetailByHourPerDay(){
    	Object [] arr = new Object[1];
    	Calendar c = Calendar.getInstance(); 
		Date date=new Date(); 
		c.setTime(date); 
		int day=c.get(Calendar.DATE); 
		c.set(Calendar.DATE,day-1);
    	arr[0] = yydDateFM.format(c.getTime());
    	String sql = "select sum(bid_nums) bid_nums,hour from ad_notice_detail_hour where date = ? group by hour";  
    	ConcurrentHashMap<Integer,Long> map = new ConcurrentHashMap<>();
    	try{    	
    		ResultList rl = new ResultList();   	
    		rl = select.select(sql,arr);
    		for(ResultMap rm : rl){
    			BigDecimal bidNums = rm.getBigDecimal("bid_nums");
    			map.put(rm.getInteger("hour"), bidNums.longValue());
    		}
    		return map;
    	}catch(SQLException e){
    		e.printStackTrace();
            return map;
    	}
    }
    
    /**
     * 获取adx流量控制
     * @return
     */
    public List<FlowControlBean> getAdxFlowControl(long updateTime){
    	Object arr[] = new Object[1];
    	arr[0] = specDateFM.format(new Date(updateTime));
    	String sql = "select adx_id,adx_name,low_flows,flow_control_ratio,status,put_ad_status from adx_flow_control where updated_at >= ?";
    	List<FlowControlBean> flowControlList = new ArrayList<FlowControlBean>();
    	ResultList rl = new ResultList(); 
    	try {
			rl = select.select(sql,arr);
			for(ResultMap rm : rl){
				FlowControlBean bean = new FlowControlBean();
				bean.setAid(rm.getString("adx_id"));
				bean.setName(rm.getString("adx_name"));
				bean.setLowFlows(rm.getInteger("low_flows"));
				bean.setFlowControlRatio(rm.getInteger("flow_control_ratio"));
				bean.setStatus(rm.getInteger("status"));
				bean.setPutAdStatus(rm.getInteger("put_ad_status"));
				flowControlList.add(bean);
			}
			
			return flowControlList;
		} catch (SQLException e) {
			e.printStackTrace();
			return flowControlList;
		}
    }
    
    /**
     * 获取app流量控制
     * @return
     */
    public List<FlowControlBean> getAppFlowControl(long updateTime){
    	Object arr[] = new Object[1];
    	arr[0] = specDateFM.format(new Date(updateTime));
    	String sql = "select app_package_id,app_package_name,low_flows,flow_control_ratio,status,put_ad_status from app_package_control where updated_at >= ?";
    	List<FlowControlBean> flowControlList = new ArrayList<FlowControlBean>();
    	ResultList rl = new ResultList(); 
    	try {
			rl = select.select(sql,arr);
			for(ResultMap rm : rl){
				FlowControlBean bean = new FlowControlBean();
				bean.setAid(rm.getString("app_package_id"));
				bean.setName(rm.getString("app_package_name"));
				bean.setLowFlows(rm.getInteger("low_flows"));
				bean.setFlowControlRatio(rm.getInteger("flow_control_ratio"));
				bean.setStatus(rm.getInteger("status"));
				bean.setPutAdStatus(rm.getInteger("put_ad_status"));
				flowControlList.add(bean);
			}
			
			return flowControlList;
		} catch (SQLException e) {
			e.printStackTrace();
			return flowControlList;
		}
    }
    
    
    public static void main(String[] args) {
    	TaskServicve task = new TaskServicve();
    	AdNoticeDetailBean adNoticeDetail = new AdNoticeDetailBean();
    	adNoticeDetail.setAdUid("123");
    	adNoticeDetail.setAdName("test");
    	adNoticeDetail.setAdvertiserUid("123");
    	adNoticeDetail.setAdvertiserName("TEST");
    	adNoticeDetail.setWinNums(10000);
    	adNoticeDetail.setClickNums(12);
    	adNoticeDetail.setBidNums(100000);
    	adNoticeDetail.setWinRatio(adNoticeDetail.getWinNums()*1.0f/adNoticeDetail.getBidNums());
    	adNoticeDetail.setClickRatio(adNoticeDetail.getClickNums()*1.0f/adNoticeDetail.getWinNums());
    	try {
			//task.insertDataToNoticeDetailPerHour(adNoticeDetail);
    		//Map map = task.getNoticeDetailByHourPerDay();
    		long timeNow = System.currentTimeMillis();
           long timeBefore = timeNow - 10 * 60 *1000;
//    		List<FlowControlBean> list = task.getAdxFlowControl(timeBefore);
//    		System.out.println(list);
//    		ResultMap balanceMap = task.queryAdviserAccountById("1e3ebfc0-db29-49fc-b39f-82543606e887");
//    		Integer updatedAt = balanceMap.getInteger("updated_at");
//    		long timeNow = System.currentTimeMillis();
//            long timeBefore = timeNow - 10 * 60 * 1000;
//            timeBefore  = timeBefore / 1000;
//            if(updatedAt != null && updatedAt != 0 && updatedAt >= timeBefore){
//            	System.out.println("ok");
//            }
//    		SimpleDateFormat specDateFM = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//    		String s = specDateFM.format(new Date(timeBefore));
//    		System.out.println(s);
           //ResultList adList = task.queryAdByUpTime(timeBefore);
           //System.out.println(adList.size());
           ConcurrentHashMap<String,AdBean> mapAd = new ConcurrentHashMap<String,AdBean>();
           
           ResultList adList = task.queryAdByUpTime(0);
           for(ResultMap map:adList){
        	   AdBean ad = new AdBean();
               ad.setAdUid(map.getString("uid"));
               //广告组
               String groupId = map.getString("group_uid");
               ad.setGroupId(groupId);
               mapAd.put(ad.getAdUid(), ad);
           }
           ConcurrentHashMap<String, ReportBean> reportMapTotal = task.statAdCostTotal();
           Map<String, Double> map = new HashMap<>();
           Iterator iter = reportMapTotal.entrySet().iterator();
           while (iter.hasNext()) {
        	   	Map.Entry entry = (Map.Entry) iter.next();
        	   	String key = (String) entry.getKey();
        	   	ReportBean value = (ReportBean) entry.getValue();
        	   	AdBean ad = mapAd.get(key);
        	   	if(ad != null){
        	   		String groupId = ad.getGroupId();
        	   		if(map.containsKey(groupId)){
        	   			if(groupId.equals("58a38bd5-a8c0-4866-b988-926512c90a2b")){
        	   				System.out.println(value.getExpense().doubleValue()*1000);
        	   			}
        	   			map.put(groupId, map.get(groupId)+value.getExpense().doubleValue()*1000);
        	   		}else{
        	   			map.put(groupId, value.getExpense().doubleValue()*1000);
        	   		}
        	   	}
           }
           System.out.println(map);
           
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
}
