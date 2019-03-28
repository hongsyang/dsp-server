package cn.shuzilm.util;

import cn.shuzilm.interf.rtb.parser.AdViewRequestServiceImpl;
import cn.shuzilm.util.db.Select;
import com.yao.util.db.bean.ResultList;
import com.yao.util.db.bean.ResultMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by DENGJIAN on 2019/2/12.
 */
public class AppBlackListUtil {

    private static Set<String> appBlackList = new HashSet();

    private static String tableName = "blacklist_app";
    private static String countSql = "select count(*) count from " + tableName + " where date=<DATE>";
    private static String selectSql = "select package_name from " + tableName + " where date=<DATE>";
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private static final Logger log = LoggerFactory.getLogger(AppBlackListUtil.class);

    // 更新媒体黑名单
    public static void updateAppBlackList() {
        MDC.put("sift", "dsp-server");
        try {
            log.info("开始更新媒体黑名单");
            Select select = new Select();
            String date = sdf.format(new Date());
            ResultMap countRs = select.selectSingle(countSql.replace("<DATE>", date));
            Long count = 0L;
            if(countRs != null) {
                count = countRs.getLong("count");
            }
            if(count > 1000000) {
                log.error("媒体黑名单数量（数量：{}）超限, 不执行操作", count);
                return;
            }
            // 如果当天没有取到数据，就取前一天的数据
            if(count == 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                date = sdf.format(calendar.getTime());
                log.error("当天媒体黑名单为空, 取前一天的黑名单数据。 date: {}", date);
            }

            // 将媒体黑名单保存到内存
            ResultList rs = select.select(selectSql.replace("<DATE>", date));
            if(rs != null && rs.size() > 0) {
                Set<String> tempSet = new HashSet();
                String packageName = "";
                for(ResultMap app : rs) {
                    try{
                        packageName = app.getString("package_name");
                        tempSet.add(packageName);
                    }catch (Exception e) {
                        log.error("保存媒体黑名单失败", e);
                    }
                }
                appBlackList = tempSet;
            }
            log.info("更新媒体黑名单结束：黑名单数量：{}", appBlackList.size());
        } catch (Exception e) {
            log.error("获取媒体黑名单失败", e);
        }
    }

    public static boolean inAppBlackList(String appName) {
        if(appBlackList != null && appBlackList.contains(appName)) {
            return true;
        }else {
            return false;
        }
    }

    public static void stopTask(){
        appBlackList = new HashSet();;
    }

}
