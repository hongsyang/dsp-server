package cn.shuzilm.util;

import cn.shuzilm.util.aes.AES;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.io.*;
import java.util.Iterator;
import java.util.Map;


/**
 * @Description: 文件转换为Excel
 * @Author: houkp
 * @CreateDate: 2018/11/7 11:55
 * @UpdateUser: houkp
 * @UpdateDate: 2018/11/7 11:55
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
public class FileConvertExcel {


    /**
     * 文件夹路径下的所有文件转换为Excel
     *
     * @param file 文件夹
     * @return
     */
    public static String fileConvertExcel(File file) throws Exception {


        //创建excel工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //创建工作表sheet
        HSSFSheet sheet = workbook.createSheet();
        //创建第一行


        int j = 0;
        File[] files = file.listFiles();
        for (File txtFile : files) {
            int i = 0;
            File lingji = new File("f://"+ j++  +"lingji.xls" );
            FileOutputStream stream = new FileOutputStream(lingji);
            BufferedReader bufferedRead = new BufferedReader(new FileReader(txtFile));
            while (bufferedRead.read() != -1) {
                String readLine = bufferedRead.readLine();
                if (readLine.contains("lingjiexp?")) {
                    Map<String, String> stringStringMap = UrlParserUtil.urlRequest(readLine);
                    HSSFRow row = sheet.createRow(i++);
                    HSSFCell cell1 = row.createCell(0);
                    cell1.setCellValue(readLine);
                    cell1 = row.createCell(1);
                    String price = stringStringMap.get("price");
                    cell1.setCellValue(AES.decrypt(price, "af36ec6c77c042b5a5e49e6414fb436f"));
                }
            }
            //创建excel文件
            workbook.write(stream);
            stream.close();
            System.out.println(txtFile.getName());
        }


        return "";
    }

    /**
     * 文件夹路径下的所有文件转换为Excel
     *
     * @param fileStr 文件夹
     * @return
     */
    public static String fileConvertExcel(String fileStr) {
        File file = new File(fileStr);
        try {
            return fileConvertExcel(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileStr;
    }


    public static void main(String[] args) {
        String file = "C:\\Users\\houkp\\Desktop\\对账\\lingji";
        fileConvertExcel(file);
    }
}
