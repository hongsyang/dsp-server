package cn.shuzilm.rtb;

import cn.shuzilm.backend.rtb.RtbFlowControl;
import cn.shuzilm.backend.timing.rtb.*;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Created by thunders on 2018/7/23.
 */
public class RtbCronDispatchTest {
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

    }
}
