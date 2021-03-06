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
        String sql = "SELECT c.* FROM ad JOIN map_ad_audience b ON ad.uid = b.ad_uid JOIN audience c ON b.audience_uid = c.uid WHERE b.ad_uid = ? and c.deleted = 0";
        ResultList list =  select.select(sql,arr);
        for(ResultMap rm : list) {
            AudienceBean bean = new AudienceBean();
            bean.setAdUid(adUid);
            bean.setUid(rm.getString("uid"));
            bean.setName(rm.getString("name"));
            bean.setType(rm.getString("type"));
            bean.setAdviserId(rm.getString("advertiser_uid"));

            //特定人群
            bean.setDemographicCitys(rm.getString("demographic_city"),rm.getString("type"));
            bean.setDemographicTagId(rm.getString("demographic_tag"));
            //兴趣偏好标签
            bean.setAppPreferenceIds(rm.getString("app_preference_ids"));
            bean.setBrandIds(rm.getString("brand_ids"));
            bean.setCarrierId(rm.getString("carrier_id"));
            //选定城市或者经纬度 工作地、居住地、活动地
            bean.setMobilityType(rm.getString("location_type"));
            bean.setCitys(rm.getString("location_city"),rm.getString("type"),rm.getString("location_mode"));
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
        String sql = "SELECT m.ad_uid,a.* FROM audience a JOIN map_ad_audience m ON m.audience_uid = uid where updated_at >= ? and a.deleted = 0";
        ResultList list =  select.select(sql,arr);
        for(ResultMap rm : list) {
        	AudienceBean bean = new AudienceBean();
            bean.setAdUid(rm.getString("ad_uid"));
            bean.setUid(rm.getString("uid"));
            bean.setName(rm.getString("name"));
            bean.setType(rm.getString("type"));
            bean.setAdviserId(rm.getString("advertiser_uid"));

            //特定人群
            bean.setDemographicCitys(rm.getString("demographic_city"),rm.getString("type"));
            bean.setDemographicTagId(rm.getString("demographic_tag"));
            //兴趣偏好标签
            bean.setAppPreferenceIds(rm.getString("app_preference_ids"));
            bean.setBrandIds(rm.getString("brand_ids"));
            bean.setCarrierId(rm.getString("carrier_id"));
            //选定城市或者经纬度 工作地、居住地、活动地
            bean.setMobilityType(rm.getString("location_type"));
            bean.setCitys(rm.getString("location_city"),rm.getString("type"),rm.getString("location_mode"));
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
     * 根据 AD UID 查询 创意(已失效)
     * @param creativeUid
     * @return
     */
    public CreativeBean queryCreativeUidByAid(String creativeUid){
        String sql = "select * from creative where uid = '"+ creativeUid +"' and deleted = 0";
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
     * 查找5分钟前的创意(已失效)
     * @return
     */
    public List<CreativeBean> queryCreativeByUpTime() {
    	List<CreativeBean> creativeList = new ArrayList<CreativeBean>();
    	long timeNow = System.currentTimeMillis();
        long timeBefore = timeNow - INTERVAL;
        Object[] arr = new Object[1];
        //arr[0] = timeBefore / 1000;
        arr[0] = specDateFM.format(new Date(timeBefore));
        String sql = "select a.uid ad_uid,c.* from creative c JOIN ad a ON c.uid=a.creative_uid where c.refresh_ts >= ? and c.deleted = 0";
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
     * 查找5分钟前的创意组
     * @return
     */
    public List<CreativeGroupBean> queryCreativeGroupByUpTime() {
    	List<CreativeGroupBean> creativeGroupList = new ArrayList<CreativeGroupBean>();
    	long timeNow = System.currentTimeMillis();
        long timeBefore = timeNow - INTERVAL;
        Object[] arr = new Object[1];
        arr[0] = timeBefore / 1000;
        arr[0] = specDateFM.format(new Date(timeBefore));
        String sql = "SELECT m.ad_uid,a.* FROM creative_group a JOIN map_ad_creative m ON m.creative_group_uid = a.uid where refresh_ts >= ? and a.deleted = 0";
        try {           
            ResultList list =  select.select(sql,arr);
            for(ResultMap cMap : list) {
            	CreativeGroupBean creativeGroup = new CreativeGroupBean();
            	creativeGroup.setAdUid(cMap.getString("ad_uid"));
            	creativeGroup.setUid(cMap.getString("uid"));
            	creativeGroup.setName(cMap.getString("name"));
            	creativeGroup.setLink_type(Integer.parseInt(cMap.getString("link_type")));
            	creativeGroup.setLink(cMap.getString("link_uri"));
            	creativeGroup.setTracking(cMap.getString("tracking_uri"));
            	creativeGroup.setClickTrackingUrl(cMap.getString("click_tracking_uri"));
            	creativeGroup.setTradeId(cMap.getInteger("trade_id"));
            	creativeGroupList.add(creativeGroup);
            }
            
            return creativeGroupList;
        } catch (SQLException e) {
            e.printStackTrace();
            return creativeGroupList;
        }
    }
    
    /**
     * 根据广告单元ID加载创意组
     * @param adUid
     * @return
     */
    public List<CreativeGroupBean> queryCreativeGroupByAdUid(String adUid){
    	List<CreativeGroupBean> aList = new ArrayList<>();

        Object[] arr = new Object[1];
        arr[0] = adUid;
        String sql = "SELECT c.* FROM ad JOIN map_ad_creative b ON ad.uid = b.ad_uid JOIN creative_group c ON b.creative_group_uid = c.uid WHERE b.ad_uid = ? and c.deleted = 0";
        try {           
            ResultList list =  select.select(sql,arr);
            for(ResultMap cMap : list) {
            	CreativeGroupBean creativeGroup = new CreativeGroupBean();
            	creativeGroup.setUid(cMap.getString("uid"));
            	creativeGroup.setName(cMap.getString("name"));
            	creativeGroup.setLink_type(Integer.parseInt(cMap.getString("link_type")));
            	creativeGroup.setLink(cMap.getString("link_uri"));
            	creativeGroup.setTracking(cMap.getString("tracking_uri"));
            	creativeGroup.setClickTrackingUrl(cMap.getString("click_tracking_uri"));
            	creativeGroup.setTradeId(cMap.getLong("trade_id").intValue());
            	aList.add(creativeGroup);
            }
            return aList;
        } catch (SQLException e){
        	e.printStackTrace();
        	return aList;
        }
    }
    
    /**
     * 根据 创意组ID 查询 创意
     * @param creativeUid
     * @return
     */
    public List<CreativeBean> queryCreativeByGroupId(String creativeGroupUid){
    	List<CreativeBean> creativeList = new ArrayList<CreativeBean>();
    	Object[] arr = new Object[1];
        arr[0] = creativeGroupUid;
        String sql = "select * from creative where creative_group_uid = ? and deleted = 0";
        try {            
            ResultList list = select.select(sql,arr);
            for(ResultMap cMap:list){
            	CreativeBean creativeBean = new CreativeBean();
	           // creativeBean.setName(cMap.getString("name"));
	            creativeBean.setUid(cMap.getString("uid"));
	            creativeBean.setBrand(cMap.getString("brand"));
	            creativeBean.setDesc(cMap.getString("desc"));
	            creativeBean.setTitle(cMap.getString("title"));
	            creativeBean.setType(cMap.getString("type"));
	            creativeList.add(creativeBean);
            }
            return creativeList;
        } catch (SQLException e) {
            e.printStackTrace();
            return creativeList;
        }
    }
    
    
    
    /**
     * 根据创意 ID 查询 物料
     * @return
     */
    public ArrayList<Material> queryMaterialByCreativeId(String creativeUid,String creativeType){
        String sql = "select * from material where creative_uid = '"+ creativeUid +"' and deleted = 0 and op_status = 1";
        ResultList rl = null;
        try {
            rl = select.select(sql);
            ArrayList<Material> list = new ArrayList<>();
            for(ResultMap rm : rl){
                Material material = new Material();
                material.setUid(rm.getString("uid"));
                material.setNid(rm.getString("nid"));
                material.setCreativeUid(creativeUid);
                //material.setType(rm.getString("type"));
                //material.setFileName(rm.getString("filename"));
                //material.setExt(rm.getString("ext"));
                //material.setSize(rm.getInteger("size"));
                //material.setWidth(rm.getInteger("w"));
                //material.setHeight(rm.getInteger("h"));
                material.setApproved_adx(rm.getString("approved_adx"));
                //material.setDuration(rm.getInteger("duration"));
                material.setAuditId(rm.getString("audit_id"));
                List<Image> imageList = new ArrayList<Image>();
                String file1 = rm.getString("file1");
                imageList.add(queryImageByfileId(file1));
                if("feed3".equals(creativeType)){
                	String file2 = rm.getString("file2");
                	String file3 = rm.getString("file3");               	
                	imageList.add(queryImageByfileId(file2));
                	imageList.add(queryImageByfileId(file3));
                }
                material.setImageList(imageList);
                list.add(material);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 根据文件ID查询某一个图片
     * @param fileId
     * @return
     */
    public Image queryImageByfileId(String fileId){
    	Object[] arr = new Object[1];
    	arr[0] = fileId;
    	String sql = "SELECT * FROM img where uid = ? and status = 1";
    	 try {           
    		 Image image = new Image();
             ResultMap cMap =  select.selectSingle(sql,arr);
             if(cMap == null){
            	 return null;
             }
             image.setUid(cMap.getString("uid"));
             image.setFileName(cMap.getString("filename"));
             image.setExt(cMap.getString("ext"));
             image.setSize(cMap.getInteger("size"));
             image.setWidth(cMap.getInteger("w"));
             image.setHeight(cMap.getInteger("h"));
             image.setDuration(cMap.getInteger("duration"));
             return image;
         } catch (SQLException e) {
             e.printStackTrace();
             return null;
         }
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
     * 读取所有广告主余额
     * @return
     */
    public ResultList queryAdviserAccount(boolean isInit){
    	 String sql = null;
    	 ResultList rl = null;
    	if(isInit){
    		sql = "select * from balance";    		
            try {
                rl = select.select(sql);
                return rl;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
    	}else{
    		long timeNow = System.currentTimeMillis();
            long timeBefore = timeNow - INTERVAL;
            Object[] arr = new Object[1];
            arr[0] = timeBefore / 1000;
    		sql = "select * from balance where updated_at >= ?";
            try {
                rl = select.select(sql,arr);
                return rl;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
    	}

        
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
    
    /**
     * 查询所有媒体
     * @return
     */
    public ArrayList<MediaBean> queryMediaAll(){
    	//String sql = "SELECT * FROM media where op_status = 1 and media_status = 1";
    	String sql = "SELECT * FROM media";
    	ResultList rl = new ResultList(); 
    	ArrayList<MediaBean> mediaList = new ArrayList<MediaBean>();
    	try {
			rl = select.select(sql);
			for(ResultMap rm : rl){
				MediaBean media = new MediaBean();
				Long mediaId = rm.getLong("id");
				media.setId(mediaId);
				media.setAdxId(rm.getInteger("adx_id"));
				List<String> packageNameList = queryPackageNameByMediaId(mediaId);
				media.setPackageNameList(packageNameList);
				media.setAppName(rm.getString("name"));
				media.setMediaType(rm.getInteger("media_type"));
				media.setSetOther(rm.getInteger("set_other"));
				media.setIsMaster(rm.getInteger("is_master"));
				media.setMasterMediaId(rm.getInteger("master_media_id").longValue());
				media.setOpStatus(rm.getInteger("op_status"));
				media.setMediaStatus(rm.getInteger("media_status"));
				mediaList.add(media);
			}
			
			return mediaList;
		} catch (SQLException e) {
			e.printStackTrace();
			return mediaList;
		}
    }
    
    public ArrayList<String> queryPackageNameByMediaId(Long mediaId){
    	Object arr[] = new Object[1];
    	arr[0] = mediaId;
    	String sql = "SELECT package_name FROM map_media_package where media_id = ? and deleted = 0";
    	ResultList rl = new ResultList(); 
    	ArrayList<String> packageNameList = new ArrayList<String>();
    	try {
			rl = select.select(sql,arr);
			for(ResultMap rm : rl){
				packageNameList.add(rm.getString("package_name"));
			}
			
			return packageNameList;
		} catch (SQLException e) {
			e.printStackTrace();
			return packageNameList;
		}
    }
    
    /**
     * 根据媒体ID查询媒体
     * @return
     */
    public MediaBean queryMediaById(Long id){
    	Object arr[] = new Object[1];
    	arr[0] = id;
    	String sql = "SELECT * FROM media where op_status = 1 and media_status = 1 and id = ?";
    	MediaBean media = new MediaBean();
    	try {
			ResultMap rm = select.selectSingle(sql,arr);
			if(rm == null){
				return null;
			}
			Long mediaId = rm.getLong("id");
			media.setId(mediaId);
			media.setAdxId(rm.getInteger("adx_id"));
			List<String> packageNameList = queryPackageNameByMediaId(mediaId);
			media.setPackageNameList(packageNameList);
			media.setAppName(rm.getString("name"));
			media.setMediaType(rm.getInteger("media_type"));
			media.setSetOther(rm.getInteger("set_other"));
			media.setIsMaster(rm.getInteger("is_master"));
			media.setMasterMediaId(rm.getInteger("master_media_id").longValue());
			media.setOpStatus(rm.getInteger("op_status"));
			media.setMediaStatus(rm.getInteger("media_status"));			
			return media;
		} catch (SQLException e) {
			e.printStackTrace();
			return media;
		}
    }
    
    /**
     * 根据广告单元ID获取媒体包名
     * @param adUid
     * @return
     */
    public ArrayList<Long> queryMediaIdByAdUid(String adUid){
    	Object arr[] = new Object[1];
    	arr[0] = adUid;
    	String sql = "SELECT media_uid from map_ad_media where ad_uid = ? and deleted = 0";
    	ArrayList<Long> mediaIdList = new ArrayList<Long>();
    	ResultList rl = new ResultList(); 
    	try {
			rl = select.select(sql,arr);
			for(ResultMap rm : rl){
				Long mediaId = rm.getInteger("media_uid").longValue();
				ArrayList<Long> mediaIdTempList = queryMediaIdByMasterMediaId(mediaId);
				if(mediaIdTempList != null && !mediaIdTempList.isEmpty()){
					mediaIdList.addAll(mediaIdTempList);
				}
				mediaIdList.add(mediaId);
			}
			
			return mediaIdList;
		} catch (SQLException e) {
			e.printStackTrace();
			return mediaIdList;
		}
    }
    
    public ArrayList<Long> queryMediaIdByMasterMediaId(Long masterMediaId){
    	Object arr[] = new Object[1];
    	arr[0] = masterMediaId;
    	String sql = "SELECT id from media where master_media_id <> 0 and master_media_id = ?";
    	ArrayList<Long> mediaIdList = new ArrayList<Long>();
    	ResultList rl = new ResultList(); 
    	try {
			rl = select.select(sql,arr);
			for(ResultMap rm : rl){
				Long mediaId = rm.getLong("id");				
				mediaIdList.add(mediaId);
			}
			
			return mediaIdList;
		} catch (SQLException e) {
			e.printStackTrace();
			return mediaIdList;
		}
    }
    
    /**
     * 每隔10分钟获取一次开启的广告位列表
     * @return
     */
    public ArrayList<AdLocationBean> queryAdLocationList(){
    	String sql = "SELECT * FROM adx_media_placement where active = 0 and deleted = 0";
    	ArrayList<AdLocationBean> adLocationList = new ArrayList<AdLocationBean>();
    	ResultList rl = new ResultList();
    	try{
    		rl = select.select(sql);
    		for(ResultMap rm:rl){
    			AdLocationBean adLocation = new AdLocationBean();
    			Long id = rm.getInteger("id").longValue();  		
    			String type = rm.getString("type");
    			String fields = rm.getString("fields");
    			Long mediaId = rm.getInteger("media_id").longValue();
    			adLocation.setId(id);
    			adLocation.setAdxId(rm.getInteger("adx_id").longValue());
    			adLocation.setMediaId(mediaId);
    			adLocation.setAdLocationId(rm.getString("place_id"));
    			adLocation.setType(type);
    			adLocation.setFields(fields);
    			AdLocationItemBean item = queryAdLocationItemLit(id);
    			adLocation.setAdLocationItem(item);
    			MediaBean media = queryMediaById(mediaId);
    			adLocation.setMedia(media);
    			adLocationList.add(adLocation);
    		}
    		return adLocationList;
    	} catch (Exception e){
    		e.printStackTrace();
    		return adLocationList;
    	}
    }
 
    public AdLocationItemBean queryAdLocationItemLit(Long id){
    	Object arr[] = new Object[1];
    	arr[0] = id;
    	String sql = "SELECT * FROM adx_media_placement_item where placement_id = ? order by item_limit_key";
    	AdLocationItemBean item = new AdLocationItemBean();
    	ResultList rl = new ResultList();
    	try{
    		rl = select.select(sql,arr);
    		for(ResultMap rm:rl){
    			String itemLimitKey = rm.getString("item_limit_key");
    			if(itemLimitKey.contains("_width")){
    				String itemLimitValue = rm.getString("item_limit_value");   				    				
    				item.setWidth(Integer.parseInt(itemLimitValue));
    			}
    			if(itemLimitKey.contains("_height")){
    				String itemLimitValue = rm.getString("item_limit_value");  
    				item.setHeight(Integer.parseInt(itemLimitValue));
    			}
    		}
    		return item;
    	} catch (Exception e){
    		e.printStackTrace();
    		return item;
    	}
    }
    
    
    public static void main(String[] args) {
    	TaskServicve taskService = new TaskServicve();
    	
    	try {
    		List<AudienceBean> audience = taskService.queryAudienceByUpTime("4c370950-f7e0-4532-b978-191a62505aa8");
    		System.out.println(audience);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
}
