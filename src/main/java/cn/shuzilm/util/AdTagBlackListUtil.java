package cn.shuzilm.util;

import cn.shuzilm.bean.control.AdLocationBean;
import cn.shuzilm.bean.control.AdLocationItemBean;
import cn.shuzilm.bean.control.MediaBean;
import cn.shuzilm.util.db.Select;
import com.yao.util.db.bean.ResultList;
import com.yao.util.db.bean.ResultMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by DENGJIAN on 2019/3/14.
 */
public class AdTagBlackListUtil {

    private static final Logger LOG = LoggerFactory.getLogger(AdTagBlackListUtil.class);
    private static Select select = new Select();

    private static Set<String> adLocationSet = new HashSet();

    public static void updateAdTagBlackList(){
        LOG.error("开始加载广告位黑名单");
        String sql = "SELECT * FROM adx_media_placement where active = 1 or deleted = 1";
        ArrayList<AdLocationBean> adLocationList = new ArrayList<AdLocationBean>();
        ResultList rl = new ResultList();
        Set<String> tempSet = new HashSet();
        try{
            rl = select.select(sql);
            LOG.error("广告位黑名单数量：{}", rl.size());
            for(ResultMap rm:rl){
               try{
                   Long id = rm.getInteger("id").longValue();
                   Long mediaId = rm.getInteger("media_id").longValue();
                   MediaBean media = queryMediaById(mediaId);
                   List<String> mediaAppPackageNameList = new ArrayList<String>();
                   if(media != null){
                       mediaAppPackageNameList = media.getPackageNameList();
                   }
                   String placeId = rm.getString("place_id");
                   AdLocationItemBean item = queryAdLocationItemLit(id);
                   Integer width = null;
                   Integer height = null;
                   if(item != null){
                       width = item.getWidth();
                       height = item.getHeight();
                   }
                   long adxId = rm.getInteger("adx_id").longValue();

                   for(String packageName:mediaAppPackageNameList){

                       String adLocationStr1 = adxId+"_"+packageName+"_"+adxId+"_"+placeId;
                       String adLocationStr2 = adxId+"_"+packageName+"_"+width+"_"+height;
                       tempSet.add(adLocationStr1);
                       tempSet.add(adLocationStr2);
                   }
                   adLocationSet = tempSet;
               }catch (Exception e) {
                   LOG.error("转换广告位黑名单失败 ",e);
               }
            }
            LOG.error("最终广告位黑名单数量：{}", adLocationSet.size());
        } catch (Exception e){
            LOG.error("获取广告位黑名单失败 ",e);
        }
    }

    public static void main(String[] args) {
        updateAdTagBlackList();
        System.out.println("");
    }


    public static boolean inAdTagBlackList(String adxId, String appPackageName, List<String> adTagIdList, int width,int height) {
        LOG.debug("adxId:{}, appPackageName: {}  width: {} height: {}", adxId, appPackageName, width, height);
        // adxId 和 appPackageName 为空，直接放过
        if(adxId == null || "".equals(adxId.trim())
            || appPackageName == null || "".equals(appPackageName.trim())) {
            return false;
        }
        if(adTagIdList.size() > 0) {
            for(String adTagId : adTagIdList) {
                LOG.debug(" adTagId : {}", adTagId);
                String adLocationStr = "";
                if(StringUtils.isNotEmpty(adTagId)) {
                    adLocationStr = adxId+"_"+appPackageName+"_"+adTagId;
                }else if(width > 0 && height > 0) {
                    adLocationStr = adxId+"_"+appPackageName+"_"+width+"_"+height;
                }else {
                    // 不符合规范，直接放过
                    return false;
                }
                if(!adLocationSet.contains(adLocationStr)){
                    return false;
                }
            }
            return true;
        }else {
            return false;
        }
    }


    /**
     * 根据媒体ID查询媒体
     * @return
     */
    public static MediaBean queryMediaById(Long id){
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

    public static ArrayList<String> queryPackageNameByMediaId(Long mediaId){
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

    public static AdLocationItemBean queryAdLocationItemLit(Long id){
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


}