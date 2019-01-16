package cn.shuzilm.util;

import cn.shuzilm.common.AppConfigs;

import java.io.*;
import java.util.HashMap;

/**
 * @Description: 长宽列表
 * @Author: houkp
 * @CreateDate: 2018/12/12 11:48
 * @UpdateUser: houkp
 * @UpdateDate: 2018/12/12 11:48
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class WidthAndHeightListUtil {

    private static WidthAndHeightListUtil ipBlacklist = null;

    private static final String FILTER_CONFIG = "filter.properties";

    private static AppConfigs configs = AppConfigs.getInstance(FILTER_CONFIG);

    private static HashMap<String, Integer> ipBlacklistMap;

    public static WidthAndHeightListUtil getInstance() {
        if (ipBlacklist == null) {
            ipBlacklist = new WidthAndHeightListUtil();
        }
        return ipBlacklist;
    }

    public WidthAndHeightListUtil() {
        String fileTest ="C:\\Users\\houkp\\Desktop\\duizhang\\ip_chinese_black_list.txt";
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

    public static void main(String[] args) {
        WidthAndHeightListUtil instance = WidthAndHeightListUtil.getInstance();
        System.out.println(instance.isIpBlacklist("39.167.203"));

    }
}
