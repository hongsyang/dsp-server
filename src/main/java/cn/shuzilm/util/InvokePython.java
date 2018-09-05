package cn.shuzilm.util;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class InvokePython {

	private static final Logger LOG = LoggerFactory.getLogger(InvokePython.class);
    private static String readFromStream(InputStream stream){
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));

            StringBuilder sb = new StringBuilder();
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String invoke(String[] args,String dirPath) {
        try {
            Process pr = Runtime.getRuntime().exec(args, new String[]{""}, new File(dirPath));
            pr.waitFor();
            String error = readFromStream(pr.getErrorStream());
            String info = readFromStream(pr.getInputStream());
            if(error != null && !error.trim().equals(""))
            	LOG.error("异常: " + error);
            return  info;
        }catch(Exception ex){
            ex.printStackTrace();
            return "error";
        }

    }

//    public static void main(String[] args) throws IOException {
//        System.setProperty("python.home", "E:\\python_2.7_64");
//        String python = "E:\\工作源码\\数盟网络\\es-api\\util\\geo_transfer.py";
//        PythonInterpreter interp = new PythonInterpreter();
//        interp.execfile(python);
//        interp.cleanup();
//        interp.close();
//    }


    public static void main(String[] args) {

        String pythonFile = "d:\\geo_transfer.py";
        String dir  = "d:\\";
        String lngStr = "116.640865,116.461492";
        String latStr = "22.852104,39.794028";
        String radiusStr = "1000,5316";
        String[] args2 = new String[]{
            "python.exe",
//                "--version"
            pythonFile,
            lngStr,
            latStr,
            radiusStr,
        };
        long startOrder = System.currentTimeMillis();
        for(int i=0;i<1;i++){
        String result = InvokePython.invoke(args2,dir);
        
        System.out.println(result);
        }
        System.out.println(System.currentTimeMillis()-startOrder);
        //System.out.println(result);
    }
}