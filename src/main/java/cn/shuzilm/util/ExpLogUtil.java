package cn.shuzilm.util;


import cn.shuzilm.interf.rtb.RtbHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExpLogUtil {

    private static final Logger log = LoggerFactory.getLogger(ExpLogUtil.class);

    public static void main(String[] args) throws Exception {
        String fileStr = "C:\\Users\\houkp\\Desktop\\exception";
        String fileStr1 = "C:\\Users\\houkp\\Desktop\\exception1";
        File file = new File(fileStr);
        File file1 = new File(fileStr1);

        File[] files = file.listFiles();
        File[] files1 = file1.listFiles();
        List<String> list= new ArrayList();
        for (File file2 : files1) {
            BufferedReader bufferedRead = new BufferedReader(new FileReader(file2));
            String str = "";
            int j =1;
//readLine判断一行是以\r\n来判断的。如果最后一段字符没有\r\n，那么采用这种方式将无法读出剩下的字符串
            while ((str = bufferedRead.readLine()) != null) {
                j++;
                if (j>180132)
                log.debug("list:{}", str);
                list.add(str);
            }

        }
//        for (File expFile : files) {
//            System.out.println(expFile);
//            try {
//                BufferedReader bufferedRead = new BufferedReader(new FileReader(expFile));
//                while (bufferedRead.read() != -1) {
//                    String readLine = bufferedRead.readLine();
//                    for (int i = 0; i <list.size() ; i++) {
////                        System.out.println(list.get(i));
////                        System.out.println(readLine);
//                        if (readLine.contains(list.get(i))) {
//                            log.debug("readLine:{}",readLine);
//                        }else {
//
//                        }
//
////                        String exp_error = readLine.substring(readLine.indexOf("TencentExp曝光的url值:/"));
////                        String substring = exp_error.substring(exp_error.indexOf("bidid="));
//
////                        HttpRequestUtil.sendGet("http://pixel.shuzijz.cn/tencentexp",substring);
//                    }
//                }
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
