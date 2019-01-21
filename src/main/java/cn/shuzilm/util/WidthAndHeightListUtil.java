package cn.shuzilm.util;

import cn.shuzilm.common.AppConfigs;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    private static WidthAndHeightListUtil widthAndHeightListUtil = null;

    private static final String FILTER_CONFIG = "filter.properties";

    private static AppConfigs configs = AppConfigs.getInstance(FILTER_CONFIG);

    private static HashMap<Integer, String> widthAndHeightListUtilMap;

    public static WidthAndHeightListUtil getInstance() {
        if (widthAndHeightListUtil == null) {
            widthAndHeightListUtil = new WidthAndHeightListUtil();
        }
        return widthAndHeightListUtil;
    }

    public WidthAndHeightListUtil() {
        String fileTest = "C:\\Users\\houkp\\Desktop\\tencent\\tencent.txt";
        File fileTxt = new File(fileTest);
        File file = new File(configs.getString("SIZE_FILE_PATH"));
        widthAndHeightListUtilMap = getwidthAndHeightListUtil(file);

    }

    /**
     * 加载广点通长宽列表
     *
     * @param file
     * @return
     */
    private static HashMap<Integer, String> getwidthAndHeightListUtil(File file) {
        System.out.println("widthAndHeightList:" + file.isFile());
        HashMap<Integer, String> widthAndHeightMap = new HashMap<Integer, String>();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            int i = 1;
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(i++ + "行：" + line);
                String[] split = line.split("-");
                widthAndHeightMap.put(Integer.valueOf(split[0]), split[1]);
            }

            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return widthAndHeightMap;
    }


    /**
     * 获取宽列表
     *
     * @param creativeSpecList
     * @return
     */
    public  List getWidthList(List<Integer> creativeSpecList) {

        List widthList = new ArrayList();
        for (Integer s : creativeSpecList) {
            String widthAndHeight = widthAndHeightListUtilMap.get(s);
            if (widthAndHeight != null) {
                String[] split = widthAndHeight.split("x");
                widthList.add(split[0]);
            }
        }
        return widthList;
    }

    /**
     * 获取高列表
     *
     * @param creativeSpecList
     * @return
     */
    public  List getHeightList(List<Integer> creativeSpecList) {

        List heightList = new ArrayList();
        for (Integer s : creativeSpecList) {
            String heights = widthAndHeightListUtilMap.get(s);
            System.out.println(heights);
            if (heightList != null) {
                String[] split = heights.split("x");
                heightList.add(split[1]);
            }
        }
        return heightList;
    }

    public static void main(String[] args) {
        WidthAndHeightListUtil instance = WidthAndHeightListUtil.getInstance();
//        String fileTest = "C:\\Users\\houkp\\Desktop\\tencent\\tencent.txt";
//        File fileTxt = new File(fileTest);
//        System.out.println(WidthAndHeightListUtil.getwidthAndHeightListUtil(fileTxt));
        List  creativeSpecList = new ArrayList();
        creativeSpecList.add(2);
        creativeSpecList.add(10);
        creativeSpecList.add(28);
        creativeSpecList.add(2);
        creativeSpecList.add(2);
        List heightList = instance.getHeightList(creativeSpecList);
        System.out.println(heightList);
        List widthList = instance.getWidthList(creativeSpecList);
        System.out.println(widthList);
//        System.out.println(instance.iswidthAndHeightListUtil("39.167.203"));

    }
}
