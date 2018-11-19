package cn.shuzilm.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Description: TimeSchedulingUtil 广告排期
 * @Author: houkp
 * @CreateDate: 2018/8/6 15:45
 * @UpdateUser: houkp
 * @UpdateDate: 2018/8/6 15:45
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class TimeSchedulingUtil {

    /**
     * 广告排期转换
     *
     * @param timeScheTxt
     * @return
     */
    public static int[][] timeTxtToMatrix(String timeScheTxt) {
        if (StringUtils.isNotBlank(timeScheTxt)) {
            int[][] timeSchedulingArr = new int[7][24];
            JSONObject parse = JSONObject.parseObject(timeScheTxt);
            Iterator<Map.Entry<String, Object>> iterator = parse.entrySet().iterator();
            List<Map.Entry> list = new ArrayList<Map.Entry>();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                list.add(entry);
            }
            String re = "[";
            String ra = "]";
            for (int i = 0; i < list.size(); i++) {
                String[] split = list.get(i).getValue().toString().replace(re, "").trim().replace(ra, "").split(",");
                for (int i1 = 0; i1 < split.length; i1++) {
                    timeSchedulingArr[i][Integer.parseInt(split[i1])] = 1;
                }
            }
            return timeSchedulingArr;

        }else {
            return null;
        }
    }
    
    public static void main(String[] args) {
    	//String timeScheTxt = "{\"1\":[11,12,13,14,15,16,17,18,19,20,21,22,23],\"2\":[11,12,13,14,15,16,17,18,19,20,21,22,23],\"3\":[11,12,13,14,15,16,17,18,19,20,21,22,23],\"4\":[11,12,13,14,15,16,17,18,19,20,21,22,23],\"5\":[11,12,13,14,15,16,17,18,19,20,21,22,23],\"6\":[11,12,13,14,15,16,17,18,19,20,21,22,23],\"7\":[11,12,13,14,15,16,17,18,19,20,21,22,23]}";
    	String timeScheTxt = "{}";
		int[][] timeSchedulingArr = timeTxtToMatrix(timeScheTxt);
		for(int i=0;i<timeSchedulingArr.length;i++){
			for(int j=0;j<timeSchedulingArr[i].length;j++){
				System.out.print(timeSchedulingArr[i][j]);
			}
			System.out.println();
		}
		SimpleDateFormat dateFm = new SimpleDateFormat("EEEE_HH");
		Date date = new Date();
		String time = dateFm.format(date);
		String splitTime[] = time.split("_");
		int weekNum = TimeUtil.weekDayToNum(splitTime[0]);
		int dayNum = Integer.parseInt(splitTime[1]);
		if (dayNum == 24)
			dayNum = 0;
		
		System.out.println(weekNum+"\t"+dayNum);
		if(timeSchedulingArr != null){
            for (int i = 0; i < timeSchedulingArr.length; i++) {
                if (weekNum != i)
                    continue;
                for (int j = 0; j < timeSchedulingArr[i].length; j++) {
                    if (dayNum == j) {
                        if (timeSchedulingArr[i][j] == 1) {
                            System.out.println("yes");
                        } else {
                            System.out.println("no");
                        }
                    }
                }
            }
            }
	}

}
