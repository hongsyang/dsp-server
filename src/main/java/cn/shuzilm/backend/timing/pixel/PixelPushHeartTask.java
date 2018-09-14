package cn.shuzilm.backend.timing.pixel;

import cn.shuzilm.backend.master.AdFlowControl;
import cn.shuzilm.backend.pixel.PixelFlowControl;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by thunders on 2018/7/23.
 */
public class PixelPushHeartTask implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        PixelFlowControl.getInstance().pushPixelHeart();

    }
}
