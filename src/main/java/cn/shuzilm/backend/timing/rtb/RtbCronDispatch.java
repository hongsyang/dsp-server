package cn.shuzilm.backend.timing.rtb;

import cn.shuzilm.backend.master.AdFlowControl;
import cn.shuzilm.backend.rtb.RtbFlowControl;
import cn.shuzilm.backend.timing.pixel.PixelTenMinuteTask;
import cn.shuzilm.util.AsyncRedisClient;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Created by thunders on 2018/7/23.
 */
public class RtbCronDispatch {
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

    /**
     * PIXEL 服务器启动类
     * @param args
     */
    public static void main2(String[] args) {
        RtbCronDispatch.startTimer(1);

    }

    public static void main(String[] args) {

    	//第一次启动加载全部缓存
    	RtbFlowControl rtbIns = RtbFlowControl.getInstance();
    			rtbIns.trigger();
    	
        RtbCronDispatch.startTimer(0);
        
        RtbCronDispatch.startTimer(1);
        
        RtbCronDispatch.startTimer(2);
        
        RtbCronDispatch.startTimer(4);
        
        RtbCronDispatch.startTimer(5);

    }
    
    public static void startRtbDispatch() {

    	//第一次启动加载全部缓存
    	RtbFlowControl.getInstance().trigger();
    	
        RtbCronDispatch.startTimer(0);
        
        RtbCronDispatch.startTimer(1);
        
        RtbCronDispatch.startTimer(2);
        
        RtbCronDispatch.startTimer(4);
        
        RtbCronDispatch.startTimer(5);
        
        RtbCronDispatch.startTimer(6);
        
        RtbCronDispatch.startTimer(7);

        RtbCronDispatch.startTimer(8);

        RtbCronDispatch.startTimer(9);

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
               dispatch(RtbRealTask.class,"0/5 * * * * ?");
               break;
           case 1:
               dispatch(RtbTenMinuteTask.class,"0/30 * * * * ?");
               break;
           case 2:
               dispatch(RtbHourTask.class,"0 0 * * * ?");
               break;
           case 3:
//               dispatch(DailyTask.class,"0 0 * * * ?");
               break;
           case 4:
        	   dispatch(RtbPushAdBidNumsTask.class,"0/5 * * * * ?");
        	   break;
           case 5:
        	   dispatch(RtbPushHeartTask.class,"0 0/10 * * * ?");
        	   break;
           case 6:
        	   dispatch(RtbPushAdxAndAppFlowTask.class,"0 0/1 * * * ?");
        	   break;
           case 7:
        	   dispatch(RtbPullFlowTask.class,"0/5 * * * * ?");
        	   break;
           case 8:
               dispatch(FetchDeviceLimitDataTask.class,"0 * * * * ?");
               break;
           case 9:
               dispatch(BlackListTask.class,"0 0 * * * ?");
               break;
           default:
               break;
       }
    }











}
