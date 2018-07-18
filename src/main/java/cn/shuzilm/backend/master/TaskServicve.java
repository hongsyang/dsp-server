package cn.shuzilm.backend.master;

import cn.shuzilm.bean.control.AdvertiserBean;
import cn.shuzilm.bean.control.CreativeBean;
import cn.shuzilm.bean.control.GroupAdBean;
import cn.shuzilm.bean.control.WorkNodeBean;
import com.yao.util.db.bean.ResultList;
import com.yao.util.db.bean.ResultMap;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by thunders on 2018/7/11.
 */
public class TaskServicve extends Service {
    /**
     * 查找 10 分钟前的数据
     *
     * @return
     * @throws java.sql.SQLException
     */
    public ResultList queryAdByUpTime(long startTime) throws SQLException {
        long now = System.currentTimeMillis();
        Object[] arr = new Object[3];
        arr[0] = startTime;
        arr[1] = now;
        arr[2] = now;
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

    public ArrayList<GroupAdBean> queryAdGroupAll(long updateTimeStamp){
        long now = System.currentTimeMillis();
        Object[] arr = new Object[3];
        arr[0] = 0;
        arr[1] = now;
        arr[2] = now;

//        String sql = "select a.*, b.uid ad_uid group a join ad b on a.uid = b.group_uid where b.s <= ? and b.e >= ? ";
        String sql = "select * from group where updated_at >= " + updateTimeStamp ;
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


}
