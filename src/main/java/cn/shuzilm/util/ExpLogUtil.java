package cn.shuzilm.util;

import java.io.File;

public class ExpLogUtil {
    public static void main(String[] args) {
        String fileStr = "C:\\Users\\houkp\\Desktop\\exception";
        File file = new File(fileStr);
        File[] files = file.listFiles();
        for (File expFile : files) {
            System.out.println(expFile);
            
        }
    }
}
