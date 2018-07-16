package cn.shuzilm.backend.master;

import cn.shuzilm.bean.control.AdvertiserBean;
import cn.shuzilm.bean.control.CreativeBean;
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

        String sql = "select * from ad where updated_at >=  " + startTime;
        return select.select(sql);
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


}
