package cn.shuzilm.util;


import cn.shuzilm.bean.internalflow.DUFlowBean;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.common.jedis.Priority;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.*;

public class ExpLogUtil {
    public static void main(String[] args) {
        String fileStr = "C:\\Users\\houkp\\Desktop\\exception";
        File file = new File(fileStr);
        File[] files = file.listFiles();
        int i=0;
        for (File expFile : files) {
            System.out.println(expFile);
            try {
                BufferedReader bufferedRead = new BufferedReader(new FileReader(expFile));
                while (bufferedRead.read() != -1) {
                    String readLine = bufferedRead.readLine();
                    if (readLine.contains("elementDUFlowBean")) {
                        String exp_error = readLine.substring(readLine.indexOf("DUFlowBean"));
                        System.out.println(exp_error);
                        Object obj = null;
                        byte[] bytes = exp_error.getBytes();
                        ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
                        ObjectInputStream oi = new ObjectInputStream(bi);
                        obj = oi.readObject();
                        bi.close();
                        oi.close();
                        System.out.println(obj);
                        DUFlowBean duFlowBean=new DUFlowBean();
                        duFlowBean.setRequestId("1");
                        byte[] bytes1 = duFlowBean.toString().getBytes();
//                        System.out.println(duFlowBean.toString().getBytes());
//                        JedisQueueManager.putElementToQueue("EXP", exp_error, Priority.MAX_PRIORITY);
//                        JSON.parse(duFlowBean);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
