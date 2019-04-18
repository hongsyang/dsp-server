package cn.shuzilm.util;

import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.util.db.Select;
import com.yao.util.db.bean.ResultList;
import com.yao.util.db.bean.ResultMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;

/**
 * @Description: ip黑名单
 * @Author: houkp
 * @CreateDate: 2018/12/12 11:48
 * @UpdateUser: houkp
 * @UpdateDate: 2018/12/12 11:48
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class IpBlacklistUtil {

    private static IpBlacklistUtil ipBlacklist = null;

    private static final Logger LOG = LoggerFactory.getLogger(IpBlacklistUtil.class);

    private static Select select = new Select();

    private static final String FILTER_CONFIG = "filter.properties";

    private static AppConfigs configs = AppConfigs.getInstance(FILTER_CONFIG);

    private static HashMap<String, Integer> ipBlacklistMap ;

    public static IpBlacklistUtil getInstance() {
        if (ipBlacklist == null) {
            ipBlacklist = new IpBlacklistUtil();
        }
        return ipBlacklist;
    }

    public IpBlacklistUtil() {
        String fileTest = "C:\\Users\\houkp\\Desktop\\duizhang\\ip_chinese_black_list.txt";
        File file = new File(configs.getString("FILE_PATH"));
        ipBlacklistMap = getIpBlacklist(file);

    }

    /**
     * 加载ip黑名单
     *
     * @param file
     * @return
     */
    private static HashMap<String, Integer> getIpBlacklist(File file) {
        System.out.println("ip_chinese_black_list:" + file.isFile());
        HashMap<String, Integer> ipMap = new HashMap<String, Integer>();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
//            int i = 1;
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
//                System.out.println(i++ + "行：" + line);
                ipMap.put(line, 1);
            }

            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ipMap;
    }

    /**
     * 判断是否在ip黑名单
     *
     * @param ip
     * @return
     */
    public boolean isIpBlacklist(String ip) {
        Integer integer = ipBlacklistMap.get(ip);
        if (integer != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 更新ip黑名单
     */
    public static void updateAdTagBlackList() {
        String now = LocalDate.now().toString();
        ResultList rl = new ResultList();
        LOG.error("开始更新ip黑名单");

        String sql = "SELECT a.`ip_blacklist` FROM blacklist_ip a " +
                "WHERE a.`flag` = 1 AND  a.`start_time`<= '" + now
                + "'AND a.`end_time`  >=  '" + now + "'";
        try {
            rl = select.select(sql);
            for (ResultMap resultMap : rl) {
                ipBlacklistMap.put(resultMap.getString("ip_blacklist"),1);
//                System.out.println(resultMap.getString("ip_blacklist"));
            }
            LOG.debug("ipBlacklistMap的数量:{}",ipBlacklistMap.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        updateAdTagBlackList();

    }
}
