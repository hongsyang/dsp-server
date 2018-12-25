package cn.shuzilm.util;


import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by thunders on 2018/10/18.
 */
public class AlarmUtil {
    public static final String PATH = "/srv/app/alarm/";
//    public static final String PATH = "d:/";
    public static final String ALERM_USER = "yangqi";

    public static boolean sendAlarm(String hostIp,String title,String content) {
        String dateTime = new Date().toString();
        String command = "python3 "+PATH+"alarm.py \""+ALERM_USER+"\" \"1\" \"\\n时间："+ dateTime +"\\n主机："+hostIp+"\\n业务："+title+"\\n内容："+content+"\"";
//        String command =  "E:\\python3.5.2\\python  "+PATH+"alarm.py \"yangqi\" \"1\" \"\\n时间：\\n主机：127.0.0.1\\n业务：222222222222\\n内容：测试\"";
        HashMap<String,String> map = new HashMap<>();
        String workDir = PATH;
        StringBuffer sb = new StringBuffer();
        TProcess t = new TProcess(command,map,workDir,sb);
        Map<String, Object> result = null;
        try {
            result = t.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(result.get("exitCode"));
        System.out.println(result.get("result"));
//        System.out.println(sb.toString());

        String exitCode = result.get("exitCode").toString();
        if(exitCode.equals("0")){
            return true;
        }else{
            return false;
        }
    }

    public static void main(String[] args) {
        AlarmUtil.sendAlarm("127.0.0.1","44444","44444");
    }
}
