package cn.shuzilm.util;

import cn.shuzilm.util.aes.AES;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;

import java.io.*;
import java.util.Map;

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
                        System.out.println(readLine);
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
