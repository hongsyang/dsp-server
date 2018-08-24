package cn.shuzilm.backend.timing.master;

import cn.shuzilm.backend.master.AdFlowControl;
import cn.shuzilm.backend.master.RealTask;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Created by thunders on 2018/7/23.
 */
public class CronDispatch {
    private static AdFlowControl control = new AdFlowControl();

    public static void dispatch(Class<? extends Job> myClass , String cronTime){
        try {
            //得到默认的调度器
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            //定义当前调度器的具体作业对象
            JobDetail jobDetail = JobBuilder.
                    newJob(myClass).
                    withIdentity("cronTriggerDetail", "cronTriggerDetailGrounp").
                    build();
            //定义当前具体作业对象的参数
            JobDataMap jobDataMap = jobDetail.getJobDataMap();
            jobDataMap.put("name", "cronTriggerMap");
            jobDataMap.put("group", "cronTriggerGrounp");

            //作业的触发器
            CronTrigger cronTrigger = TriggerBuilder.//和之前的 SimpleTrigger 类似，现在的 CronTrigger 也是一个接口，通过 Tribuilder 的 build()方法来实例化
                    newTrigger().
                    withIdentity("cronTrigger", "cronTrigger").
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
//        CronDispatch.startTimer(0);
//        System.out.println();
        //    // 5 s 触发
        AdFlowControl.getInstance().pullAndUpdateTask();
        //  每小时触发
        AdFlowControl.getInstance().resetHourMonitor();
        //  每天触发
        AdFlowControl.getInstance().resetDayMonitor();
        //  10 min 触发
        AdFlowControl.getInstance().loadAdInterval(true);



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
               dispatch(HourTask.class,"0 * * * * ?");
               break;
           case 3:
               dispatch(DailyTask.class,"0 0 * * * ?");
               break;
           default:
               break;
       }
    }











}
