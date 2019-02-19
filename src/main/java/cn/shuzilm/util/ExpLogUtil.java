package cn.shuzilm.util;


import cn.shuzilm.bean.internalflow.DUFlowBean;

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
//                    System.out.println(readLine);
                    if (readLine.contains("TencentExp曝光的url值:/")) {
                        String exp_error = readLine.substring(readLine.indexOf("TencentExp曝光的url值:/"));
                        System.out.println(exp_error);

//                        System.out.println(duFlowBean.toString().getBytes());
//                        JedisQueueManager.putElementToQueue("EXP", exp_error, Priority.MAX_PRIORITY);
//                        JSON.parse(duFlowBean);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
