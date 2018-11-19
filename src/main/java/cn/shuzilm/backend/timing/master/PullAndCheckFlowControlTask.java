package cn.shuzilm.backend.timing.master;

import cn.shuzilm.backend.master.AdFlowControl;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by thunders on 2018/7/23.
 */
public class PullAndCheckFlowControlTask implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        AdFlowControl.getInstance().pullAndCheckFlowControl();

    }
}
