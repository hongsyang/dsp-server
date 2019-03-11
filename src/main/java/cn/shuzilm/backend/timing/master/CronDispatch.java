package cn.shuzilm.backend.timing.master;

import cn.shuzilm.backend.master.AdFlowControl;
import cn.shuzilm.backend.master.CPCHandler;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.MDC;

/**
 * Created by thunders on 2018/7/23.
 */
public class CronDispatch {
    private static AdFlowControl control = new AdFlowControl();

    public  CronDispatch(){

    }

    public static void dispatch(Class<? extends Job> myClass , String cronTime){
        try {

            //得到默认的调度器
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            //定义当前调度器的具体作业对象
            JobDetail jobDetail = JobBuilder.
                    newJob(myClass).
//                    withIdentity("cronTriggerDetail", "cronTriggerDetailGrounp").
                    build();
            //定义当前具体作业对象的参数
//            JobDataMap jobDataMap = jobDetail.getJobDataMap();
//            jobDataMap.put("name", "cronTriggerMap");
//            jobDataMap.put("group", "cronTriggerGrounp");

            //作业的触发器
            CronTrigger cronTrigger = TriggerBuilder.//和之前的 SimpleTrigger 类似，现在的 CronTrigger 也是一个接口，通过 Tribuilder 的 build()方法来实例化
                    newTrigger().
//                    withIdentity("cronTrigger", "cronTrigger").
                    withSchedule(CronScheduleBuilder.cronSchedule(cronTime)). //在任务调度器中，使用任务调度器的 CronScheduleBuilder 来生成一个具体的 CronTrigger 对象
                    build();
            //注册作业和触发器
            scheduler.scheduleJob(jobDetail, cronTrigger);

            //开始调度任务
            scheduler.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
    	//程序初始化 加载程序
    	
    	AdFlowControl.getInstance().loadAllAdInterval(true);
        
        AdFlowControl.getInstance().loadAdInterval(true);
        
//        AdFlowControl.getInstance().checkAdCpmLimit(true);
               
        // 初始化CPC结算方式的广告的 阈值和moniter
        CPCHandler.getInstance().updateIndicator(true);
//        AdFlowControl.getInstance().pullAndUpdateTask();
        
        AdFlowControl.getInstance().pullAndUpdateTask(true);
        
        AdFlowControl.getInstance().updateFlow();
        
        AdFlowControl.getInstance().updateAdxAndAppFlowControl(true);
        
        AdFlowControl.getInstance().updateAndPushMediaList();
    	
    	AdFlowControl.getInstance().updateAndPushAdLocationList();
 
          // 5 s 触发
        CronDispatch.startTimer(0);
//        //  每小时触发
        CronDispatch.startTimer(2);
//        AdFlowControl.getInstance().resetHourMonitor();
//        //  每天触发
        CronDispatch.startTimer(3);
//        AdFlowControl.getInstance().resetDayMonitor();
//        //  10 min 触发
        CronDispatch.startTimer(1);
        
        CronDispatch.startTimer(4);
//        AdFlowControl.getInstance().loadAdInterval(true);
        CronDispatch.startTimer(5);
        
        CronDispatch.startTimer(6);
        
        CronDispatch.startTimer(7);
        
        CronDispatch.startTimer(8);
        
        CronDispatch.startTimer(9);
        
        CronDispatch.startTimer(10);
        
        CronDispatch.startTimer(11);
        
 //       CronDispatch.startTimer(12);
        
        CronDispatch.startTimer(13);
        
        CronDispatch.startTimer(14);
    }

    /**
     * 按照指定的步调间隔周期调用
     *
     * 0 实时 5 秒
     * 1 10分钟
     * 2 1小时
     * 3 一天
     * @param type
     */
    public static void  startTimer(int type){
       switch(type){
           case 0 :
               dispatch(RealTask.class,"0/5 * * * * ?");
               break;
           case 1:
               dispatch(TenMinuteTask.class,"0 0/10 * * * ?");
               break;
           case 2:
               dispatch(HourTask.class,"0 0 * * * ?");
               break;
           case 3:
               dispatch(DailyTask.class,"0 0 0 * * ?");
               break;
           case 4:
        	   dispatch(UpdateNodeStatusTask.class,"0 0/5 * * * ?");
        	   break;
           case 5:
        	   dispatch(UpdateCloseAdTask.class,"0 0/10 * * * ?");
        	   break;
           case 6:
        	   dispatch(UpdateAdMapTask.class,"0 0/5 * * * ?");
        	   break;
           case 7:
        	   dispatch(UpdateFlowTask.class,"0 0 0 * * ?");
        	   break;
           case 8:
        	   dispatch(UpdateAdxAndAppFlowControlTask.class,"0 0/10 * * * ?");
        	   break;
           case 9:
        	   dispatch(PullAndCheckFlowControlTask.class,"0/30 * * * * ?");
        	   break;
           case 10:
               dispatch(UpdateMonitorTask.class,"0 0/10 * * * ?");
               break;
           case 11:
               dispatch(PushFlowTaskPerTenMinute.class,"0 0/10 * * * ?");
               break;        
           case 12:
        	   dispatch(CheckAdLimitTask.class,"0/10 * * * * ?");
        	   break;
           case 13:
        	   dispatch(UpdateAndPushMediaTask.class,"0 0/10 * * * ?");
        	   break;  
           case 14:
        	   dispatch(UpdateAndPushAdLocationTask.class,"0 0/10 * * * ?");
        	   break;
           default:
               break;
       }
    }

}
