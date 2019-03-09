package cn.shuzilm.util;


import java.io.*;

public class ClickLogUtil {
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
                    if (readLine.contains("TencentClick点击的url值:/")) {
                        String exp_error = readLine.substring(readLine.indexOf("TencentClick点击的url值:/"));
                        System.out.println(exp_error);
                        String substring = exp_error.substring(exp_error.indexOf("bidid="));

                        HttpRequestUtil.sendGet("http://pixel.shuzijz.cn/tencentclick",substring);
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
