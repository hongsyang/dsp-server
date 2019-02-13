package cn.shuzilm.backend.timing.rtb;

import cn.shuzilm.backend.rtb.RtbFlowControl;
import cn.shuzilm.util.AppBlackListUtil;
import cn.shuzilm.util.DeviceBlackListUtil;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by dengjian on 2019/02/13.
 */
@DisallowConcurrentExecution
public  class BlackListTask implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        AppBlackListUtil.updateAppBlackList();
        DeviceBlackListUtil.updateDeviceBlackList();
    }

    /*public static void main(String[] args) {
        AppBlackListUtil.updateAppBlackList();
        DeviceBlackListUtil.updateDeviceBlackList();
    }*/
}