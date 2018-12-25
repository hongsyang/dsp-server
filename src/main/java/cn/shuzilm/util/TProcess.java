package cn.shuzilm.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class TProcess {
    private final String workingDir;
    private int processId;
    private Process process;
    private final Map<String, String> env;
    private List<String> cmd;
    public final StringBuffer logs;

    public static List<String> arrayToCollection(String[] values) {
        List list = new ArrayList(values.length);
        Collections.addAll(list, values);
        return list;
    }

    public static List<String> stringToCollection(String value, String separator) {
        String[] strings = StringUtils.splitPreserveAllTokens(value, separator);
        return arrayToCollection(strings);
    }

    public TProcess(String command, Map<String, String> env, String workingDir, StringBuffer logs) {
        this.cmd = stringToCollection(command, " ");
        this.processId = -1;
        this.workingDir = workingDir;
        this.env = env;
        this.logs = logs;
    }

    public Map<String, Object> run()
            throws IOException {
        Map result = new HashMap();
        ProcessBuilder builder = new ProcessBuilder(this.cmd);
        builder.directory(new File(this.workingDir));

        this.process = builder.start();
        this.processId = processId(this.process);
        if (this.processId == 0) {
            System.out.println("Cannot get process's id.");
            this.logs.append("processIs=0,不能获取进程\n");
        } else {
            System.out.println("Process ID : " + this.processId);
            this.logs.append("进程ID：" + this.processId + "\n");
        }

        OutputRedirector errorRedirector = new OutputRedirector(this.process.getErrorStream());
        OutputRedirector inRedirector = new OutputRedirector(this.process.getInputStream());
        errorRedirector.start();
        inRedirector.start();
        int exitCode = -1;
        try {
            exitCode = this.process.waitFor();
            result.put("exitCode", Integer.valueOf(exitCode));
            result.put("executeResult", "success");
            result.put("result", this.logs.toString());

            return result;
        } catch (InterruptedException e) {
            System.out.println("Interrupted process" + getStackMsg(e));
            this.logs.append("进程被中断,异常信息" + getStackMsg(e));
            result.put("exitCode", "-1");
            result.put("executeResult", "failed");
            result.put("result", this.logs.toString());

            return result;
        }
    }

    private int processId(Process process) {
        int processId = 0;
        try {
            Field f = process.getClass().getDeclaredField("pid");
            f.setAccessible(true);

            processId = f.getInt(process);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return processId;
    }

    private static String getStackMsg(Exception e) {
        StringBuffer sb = new StringBuffer();
        StackTraceElement[] stackArray = e.getStackTrace();
        for (int i = 0; i < stackArray.length; i++) {
            StackTraceElement element = stackArray[i];
            sb.append(element.toString() + "\n");
        }
        return sb.toString();
    }

    private static String getStackMsg(Throwable e) {
        StringBuffer sb = new StringBuffer();
        StackTraceElement[] stackArray = e.getStackTrace();
        for (int i = 0; i < stackArray.length; i++) {
            StackTraceElement element = stackArray[i];
            sb.append(element.toString() + "\n");
        }
        return sb.toString();
    }

    class OutputRedirector extends Thread {
        private InputStream is;

        public OutputRedirector(InputStream is) {
            this.is = is;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(this.is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null)
                    TProcess.this.logs.append(line + "\n");
            } catch (IOException ioE) {
                ioE.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        String command = "E:\\python3.5.2\\python d:/alarm.py \"yangqi\" \"1\" \"\\n时间：\\n主机：127.0.0.1\\n业务：222222222222\\n内容：测试\"";
        HashMap<String,String> map = new HashMap<>();
        String workDir = "d:/";
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
        System.out.println(sb.toString());
    }
}

/*
 * Qualified Name:     com.comecheer.schex.engine.utils.TProcess
 *
 */