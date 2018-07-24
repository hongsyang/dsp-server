package cn.shuzilm.analytics.realtime;

import cn.shuzilm.bean.internalflow.DUFlowBean;

import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Callable;



/**
 * Created by yangqi on 2018/7/16.
 * 该类是用来把redis 中取到的数据放入到 phoenix 数据库中
 */
public class Data2Phoenix implements Runnable {

    public Data2Phoenix() {
    }

    @Override
    public void run() {
        BatchInsert();
    }

    private Connection connection;
    private List<DUFlowBean> duFlowBeanList;
    private String tableName;

    private int BATCH_SIZE = 1000;

    public Data2Phoenix(Connection collection, List<DUFlowBean> duFlowBeanList, String tableName, int batch_size) {
        this.connection = collection;
        this.duFlowBeanList = duFlowBeanList;
        this.tableName = tableName;
        this.BATCH_SIZE = batch_size;
    }

    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public Date date2TimeStamp(String dateString) {
        try {
            df.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            return df.parse(df.format(df.parse(dateString).getTime() + 8 * 3600 * 1000));
        } catch (ParseException e) {
            return null;
        }
    }

    public Long BatchInsert() {


        List<String> list = new ArrayList<>();
        Long recordNum = 0L;
        //动态sql语句
        String sql = "upsert into produce_rtq.device (did_2,original_id,did_2_change,did_1,did_1_from,center_number,final_type,device_type,abnormal_tag,mac,imei,imsi,iccid,serial,bluetooth,sys_boot_id,gsm_operator,ro_manufacturer,root,fields_type,andriod_id,manufacturer,model,ro_model,cpu_abi,brand,ro_brand,rom_fingerprint,create_date) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement pstate = null;

        try {
            //设置事务为非自动提交
            connection.setAutoCommit(false);
            pstate = connection.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //预编译sql record.getString("mac__addr") != null && (record.getString("mac__addr").equals("null") || record.getString("mac__addr").length() == 0) ? null : record.getString("mac__addr")
        try {
            DUFlowBean record;
            //该索引 全量加载就ok，不需要增量。
            for (int i = 0; i <duFlowBeanList.size() ; i++) {
                object2DocumentOfAdinfo(duFlowBeanList.get(i), pstate);

            }
            if (list.size() % BATCH_SIZE == 0) {
                try {
                    pstate.executeBatch();
                    connection.commit();
                    pstate.clearBatch();
                } catch (SQLException e) {
                    for(String tmpUniqueId:list){
                        System.out.println("ExceptionID:"+tmpUniqueId);
                    }
                    e.printStackTrace();
                    try {
                        pstate.clearBatch();
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }
                list.clear();
                if (recordNum >= 1 && recordNum % 10000 == 0) {
                    System.out.println(Thread.currentThread().getName().toString() + "recordNum:" + recordNum + ":" + new Date());
                }
            }

            System.out.println(Thread.currentThread().getName().toString() + "end recordNum:" + recordNum + ":" + new Date());
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
        return recordNum;
    }
/*    create table ad_info_test (info_id varchar(36) not null primary key,ad_info.hour DATE, ad_info.create_time BIGINT(20),ad_info.insert_time BIGINT(20), ad_info.did varchar(50), ad_info.device_id varchar(50),
    ad_info.ad_uid varchar(36), ad_info.audienceuid varchar(36), ad_info.creative_uid varchar(36), ad_info.province varchar(20), ad_info.city varchar(20)
    ad_info.actualPrice decimal(10,5),ad_info.bidddingPrice decimal(10,5),ad_info.settingPrice decimal(10,5)
 ) salt_buckets=10;*/
     public void object2DocumentOfAdinfo(DUFlowBean dUFlowBean, PreparedStatement pstate) {
        try {
             String infoId = dUFlowBean.getInfoId();//上报信息的唯一ID
             String did = dUFlowBean.getDid();
             byte hour = -1;
             String deviceId = null;//   唯一识别用户
             Timestamp createTime = null;// timestamp  该条信息的创建时间
            Timestamp insertTime = null;// timestamp  该条信息的插入时间
             String adUid = null;// varchar(36)  广告ID
             String audienceuid = null;//  varchar(36)  人群ID
             String advertiserUid = null;//  varchar(36)  广告主ID
             String agencyUid = null;//varchar(36)  代理商ID
             String creativeUid = null;//varchar(36)  创意ID
             String province = null;//varchar(20)  省
             String city = null;//varchar(20)  市
             BigDecimal actualPrice = new BigDecimal(0.0);
            BigDecimal bidddingPrice = new BigDecimal(0.0);
            BigDecimal settingPrice = new BigDecimal(0.0);
            pstate.setString(1,infoId);
            pstate.setByte(2, hour);
            pstate.setTimestamp(3, createTime);
            pstate.setTimestamp(4, insertTime);
            pstate.setString(5, did);
            pstate.setString(6, deviceId);
            pstate.setString(7, adUid);
            pstate.setString(8, audienceuid);
            pstate.setString(9, creativeUid);
            pstate.setString(10, province);
            pstate.setString(11, city);
            pstate.setBigDecimal(12, actualPrice);
            pstate.setBigDecimal(13, bidddingPrice);
            pstate.setBigDecimal(14, settingPrice);


            pstate.addBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }










    /**
     * @param src
     * @param b   ture 需要转为小写，false 不需要转为小写
     * @return
     */
    public String cleanChar(String src, boolean b) {
        if (src == null || "null".equals(src.toLowerCase()) || src.length() == 0)
            return null;
        String s = src.replaceAll("\\\\t", "").replaceAll("\\\\n", "").replaceAll("\\\\r", "");
        StringBuffer sb = new StringBuffer(s.length());
        char[] charArray = s.toCharArray();
        for (char c : charArray) {
            if (getCharType(c) >= 1 && getCharType(c) <= 3)
                sb.append(c);
        }
        charArray = null;
        if (b) {
            return sb.toString().toLowerCase();
        }

        return sb.toString();
    }


    public String cleanChar(String src) {
        if (src == null || "null".equals(src.toLowerCase()) || src.length() == 0)
            return null;
        StringBuffer sb = new StringBuffer(src.length());
        char[] charArray = src.toCharArray();
        for (char c : charArray) {
            if (getCharType(c) <= 6)
                sb.append(c);
        }
        charArray = null;
        //该处 128 是和作云设置的字段长度来的
        if (sb.toString().length() > 128) {
            return sb.toString().substring(0, 128);
        }
        return sb.toString();
    }

    /**
     * @param ch
     * @return 0 汉字 1 字母 2 数字  3 分隔符 4 空格,空格 5 全角字符 6 标点符号 7 其它
     */
    public int getCharType(char ch) {
        // 最多的是汉字
        if (ch >= 0x4E00 && ch <= 0x9FA5)
            return 0;
        if ((ch >= 0x0041 && ch <= 0x005A) || (ch >= 0x0061 && ch <= 0x007A))
            return 1;
        if (ch >= 0x0030 && ch <= 0x0039)
            return 2;
        // 最前面的其它的都是标点符号了
//        if ((ch >= 0x0021 && ch <= 0x00BB) || (ch >= 0x2010 && ch <= 0x2642)
//                || (ch >= 0x3001 && ch <= 0x301E))
//            return 3;
        //if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n' || ch == '　')
        if (ch == ' ' || ch == '\t' || ch == '　')
            return 4;

//        // 全角字符区域
//        if ((ch >= 0xFF21 && ch <= 0xFF3A) || (ch >= 0xFF41 && ch <= 0xFF5A))
//            return 5;
//        if (ch >= 0xFF10 && ch <= 0xFF19)
//            return 5;
        if (ch >= 0xFE30 && ch <= 0xFF63)
            return 6;
        return 7;

    }


    public static void main(String[] args) {
        String str = " P80 3Gå\u0085«æ ¸(A7LL) ";
        String s = str.replaceAll("\\r", "");
        System.out.println(s);
        System.out.println(str.substring(0, 10));
        Data2Phoenix d2m = new Data2Phoenix();
        d2m.cleanChar(str);

    }
}


