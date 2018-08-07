package cn.shuzilm.backend.master;

import ch.qos.logback.core.joran.conditional.ElseAction;
import cn.shuzilm.bean.control.*;
import cn.shuzilm.bean.dmp.AudienceBean;
import com.yao.util.db.bean.ResultList;
import com.yao.util.db.bean.ResultMap;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by thunders on 2018/7/11.
 */
public class TaskServicve extends Service {
    /**
     * 查找 10 分钟前的 人群包条件
     *
     * @return
     * @throws java.sql.SQLException
     */
    public AudienceBean queryAudienceByUpTime(String adUid) throws SQLException {
        AudienceBean bean = new AudienceBean();
        Object[] arr = new Object[1];
        arr[0] = adUid;
        String sql = "select c.* from ad join map_ad_audience b on ad.uid = b.ad_uid" +
                " join audience c on b.ad_uid = c.uid" +
                " where b.ad_uid = ?";
        ResultList list =  select.select(sql,arr);
        if(list.size() >0 ){
            ResultMap rm = (ResultMap)list.get(0);
            bean.setAdUid(adUid);
            bean.setName(rm.getString("name"));
            bean.setType(rm.getString("type"));
            bean.setAdviserId(rm.getString("advertiser_uid"));

            //特定人群
            bean.setDemographicCitys(rm.getString("demographic_city"));
            bean.setDemographicTagId(rm.getString("demographic_tag"));
            //兴趣偏好标签
            bean.setAppPreferenceIds(rm.getString("ap_preference_ids"));
            bean.setBrandIds(rm.getString("brand_ids"));
            bean.setCarrierId(rm.getInteger("carrier_id"));
            //选定城市或者经纬度 工作地、居住地、活动地
            bean.setMobilityType(rm.getInteger("location_type"));
            bean.setCitys(rm.getString("location_city"));
            bean.setGeos(rm.getString("location_map"));
            bean.setIncomeLevel(rm.getInteger("income_level"));
            bean.setNetworkId(rm.getInteger("network_id"));
            bean.setPhonePriceLevel(rm.getInteger("phone_price_level"));
            bean.setPlatformId(rm.getInteger("platform_id"));
            //特定公司
            bean.setCompanyIds(rm.getString("company_ids"));
            bean.setCompanyNames(rm.getString("company_names"));
            return bean;
        }else{
            return null;
        }


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
        arr[1] = now / 1000;
        arr[2] = now / 1000;
        String sql = "select * from ad where updated_at >= ? and s <= ? and e >= ?";
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
            adver.setName(rm.getString("name"));
            adver.setUid(rm.getString("uid"));
            return adver;
        } catch (SQLException e) {
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
            creativeBean.setDescShort(cMap.getString("text_short"));
            creativeBean.setDomain(cMap.getString("brand_domain"));
            creativeBean.setFileName(cMap.getString("filename"));
            creativeBean.setHeight(cMap.getInteger("h"));
            creativeBean.setTitle(cMap.getString("title"));
            creativeBean.setTitleLong(cMap.getString("title_long"));
            creativeBean.setTitleShort(cMap.getString("title_short"));
            creativeBean.setType(cMap.getString("type"));
            creativeBean.setWidth(cMap.getInteger("w"));

            return creativeBean;
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
     * 广告分组查询
     * @param updateTimeStamp
     * @return
     */
    public ArrayList<GroupAdBean> queryAdGroupAll(long updateTimeStamp){
        long now = System.currentTimeMillis();
        Object[] arr = new Object[1];
        arr[0] = (int)(updateTimeStamp / 1000);

//        String sql = "select a.*, b.uid ad_uid group a join ad b on a.uid = b.group_uid where b.s <= ? and b.e >= ? ";
        String sql = "select * from ad_group where updated_at >= ?";
        try{
            ArrayList<GroupAdBean> list = new ArrayList<>();
            ResultList rl = select.select(sql,arr);
            for(ResultMap rm : rl){
                GroupAdBean g = new GroupAdBean();
                g.setGroupId(rm.getString("uid"));
                g.setAdviserId(rm.getString("advertiser_uid"));
                g.setGroupName(rm.getString("name"));
                g.setQuotaMoney(rm.getBigDecimal("quota_amount"));
                list.add(g);
            }
            return list;
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    public HashMap<String,ReportBean> statAdCostTotal(){
        long startStamp = 0;
        long nowStamp = System.currentTimeMillis();
        return statAdCost(startStamp,nowStamp,false);
    }

    /**
     * 加载当前小时 0 分 到现在的账户消耗数据
     * @return
     */
    public HashMap<String,ReportBean> statAdCostHour(){
        int minute = Calendar.getInstance().get(Calendar.MINUTE);
        Calendar start = Calendar.getInstance();
        start.set(Calendar.MINUTE,0);
        //从这个小时的 0 分作为开始时间
        long startStamp = start.getTimeInMillis();
        //以当前时间作为结束时间
        long nowStamp = System.currentTimeMillis();
        return statAdCost(startStamp,nowStamp,true);
    }

    public HashMap<String,ReportBean> statAdCostDaily(){
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR,0);
        start.set(Calendar.MINUTE,0);
        long startStamp = start.getTimeInMillis();
        long nowStamp = System.currentTimeMillis();
        return statAdCost(startStamp,nowStamp,false);
    }

    /**
     * 统计费用实际消耗情况
     * @return
     * @param startTime
     * @param endTime
     */
    public HashMap<String,ReportBean> statAdCost(long startTime, long endTime,boolean isHour){
        // type：
        // 0 : 小时存量费用的统计，对于一个小时前，当天的广告耗费的汇总
        // 1 : 天存量费用的统计，
        Object [] arr = new Object[2];
        //转换成秒的时间戳
        arr[0] = startTime / 1000;
        arr[1] = endTime / 1000;
        String sql = "";
        if(isHour)
            sql = "select ad_uid,sum(amount) expense , sum(cost) cost from reports_hour where created_at >= ? and created_at <= ?  group by ad_uid";
        else
            sql = "select ad_uid,sum(amount) expense , sum(cost) cost from reports where created_at >= ? and created_at <= ?  group by ad_uid";
        try {
            ResultList rl = select.select(sql,arr);
            HashMap<String,ReportBean> map = new HashMap<>();
            for(ResultMap rm : rl){
                ReportBean report = new ReportBean();
                String adUid = rm.getString("ad_uid");
                BigDecimal expense = rm.getBigDecimal("expense");
                BigDecimal cost = rm.getBigDecimal("cost");
                report.setAdUid(adUid);
                report.setExpense(expense);
                report.setCost(cost);
                map.put(adUid,report);
            }
            return map;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


}
