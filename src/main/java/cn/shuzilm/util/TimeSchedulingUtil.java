package cn.shuzilm.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
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

}
