package cn.shuzilm.backend.timing.rtb;

import cn.shuzilm.backend.rtb.RtbFlowControl;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by thunders on 2018/7/23.
 */
@DisallowConcurrentExecution
public  class DynamicPriceTask implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        RtbFlowControl.getInstance().updateDynamicPriceMap();
    }
}