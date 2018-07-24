package cn.shuzilm.analytics.realtime;

import cn.shuzilm.bean.internalflow.DUFlowBean;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


/**
 * Created by yangqi on 2018/7/16.
 * 该类是用来把redis 中取到的数据放入到 phoenix 数据库中
 */
public class Data2PhoenixRunnable implements Runnable {

    public Data2PhoenixRunnable() {
    }

    @Override
    public void run() {
        BatchInsert();
    }

    private Connection connection;
    private List<DUFlowBean> duFlowBeanList;
    private String tableName;
    public Data2PhoenixRunnable(Connection collection, List<DUFlowBean>  duFlowBeanList, String tableName) {
        this.connection = collection;
        this.duFlowBeanList = duFlowBeanList;
        this.tableName = tableName;

    }

    public int BatchInsert() {

        //预编译sql record.getString("mac__addr") != null && (record.getString("mac__addr").equals("null") || record.getString("mac__addr").length() == 0) ? null : record.getString("mac__addr")
        try {
            //将该集合中的数据取出，放出 sql 集合中
            ArrayList<String> duFlowBeanSqlList= new ArrayList<>();
            for (int i = 0; i <duFlowBeanList.size() ; i++) {
                String upsert_sql=object2DocumentOfAdinfo(duFlowBeanList.get(i));
                duFlowBeanSqlList.add(upsert_sql);
            }
            PhoenixClient.executeUpdateSqlBatch(connection,duFlowBeanSqlList);


            System.out.println(Thread.currentThread().getName().toString() + "end recordNum:" +duFlowBeanList.size() + ":" + new Date());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("no load data:");
        } finally {
            try {

                    connection.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return duFlowBeanList.size();
    }
/*    create table ad_info_test (info_id varchar(36) not null primary key,ad_info.hour DATE, ad_info.create_time BIGINT(20),ad_info.insert_time BIGINT(20), ad_info.did varchar(50), ad_info.device_id varchar(50),
    ad_info.ad_uid varchar(36), ad_info.audienceuid varchar(36), ad_info.creative_uid varchar(36), ad_info.province varchar(20), ad_info.city varchar(20)
    ad_info.actualPrice decimal(10,5),ad_info.bidddingPrice decimal(10,5),ad_info.settingPrice decimal(10,5)
 ) salt_buckets=10;*/
     public String object2DocumentOfAdinfo(DUFlowBean dUFlowBean) {
             String infoId = dUFlowBean.getInfoId();//上报信息的唯一ID
             String did = dUFlowBean.getDid();
             byte hour = -1;
             String deviceId = dUFlowBean.getDeviceId();//   唯一识别用户
             long createTime = dUFlowBean.getCreateTime();// timestamp  该条信息的创建时间
            long actualCreateTime = dUFlowBean.getCreateTime();// timestamp  该条信息的插入时间
             String adUid = dUFlowBean.getAdUid();// varchar(36)  广告ID
             String audienceuid = dUFlowBean.getAudienceuid();//  varchar(36)  人群ID
             String advertiserUid = dUFlowBean.getAdvertiserUid();//  varchar(36)  广告主ID
             String agencyUid = dUFlowBean.getAgencyUid();//varchar(36)  代理商ID
             String creativeUid = dUFlowBean.getCreativeUid();//varchar(36)  创意ID
             String province = dUFlowBean.getProvince();//varchar(20)  省
             String city = dUFlowBean.getCity();//varchar(20)  市
             String actualPrice = dUFlowBean.getActualPrice();//成本价
             String biddingPrice = dUFlowBean.getBiddingPrice();
             String actualPricePremium = dUFlowBean.getActualPricePremium();
         //  infoId varchar(36) not null ,hour char(10) , createTime BIGINT(20),actualCreateTime BIGINT(20), did varchar(50), deviceId varchar(50), adUid varchar(50), audienceuid varchar(50),agencyUid varchar(50),advertiserUid varchar(50), creativeUid varchar(50), province varchar(50), city varchar(50),actualPricePremium varchar(50),biddingPrice varchar(50),actualPrice varchar(50) CONSTRAINT pk PRIMARY KEY (infoId)

             String sql = "upsert into DU_TEST."+tableName+" (infoId,hour,createTime,actualCreateTime,did,deviceId,adUid,audienceuid,agencyUid,advertiserUid,creativeUid,province,city,actualPricePremium,biddingPrice,actualPrice) values ("+infoId+","+hour+","+createTime+","+actualCreateTime+","+did+","+deviceId+","+adUid+","+audienceuid+","+agencyUid+","+advertiserUid+","+creativeUid+","+province+","+city+","+actualPricePremium+","+actualPricePremium+","+biddingPrice+","+actualPrice+")";
             return sql;

    }












    public static void main(String[] args) {
        Data2PhoenixRunnable d2m = new Data2PhoenixRunnable();
    }
}


