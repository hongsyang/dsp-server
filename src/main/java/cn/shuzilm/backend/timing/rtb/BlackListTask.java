package cn.shuzilm.backend.timing.rtb;

import cn.shuzilm.backend.rtb.RtbFlowControl;
import cn.shuzilm.common.AppConfigs;
import cn.shuzilm.util.AdTagBlackListUtil;
import cn.shuzilm.util.AppBlackListUtil;
import cn.shuzilm.util.DeviceBlackListUtil;
import cn.shuzilm.util.IpBlacklistUtil;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by dengjian on 2019/02/13.
 */
@DisallowConcurrentExecution
public  class BlackListTask implements Job {

    private static final String FILTER_CONFIG = "filter.properties";
    private static AppConfigs configs = AppConfigs.getInstance(FILTER_CONFIG);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 开启媒体黑名单
        if (Boolean.valueOf(configs.getString("BUNDLE_BLACK_LIST"))) {
            AppBlackListUtil.updateAppBlackList();
        }else {
            AppBlackListUtil.stopTask();
        }
        // 开启设备黑名单
        if (Boolean.valueOf(configs.getString("DEVICE_ID_BLACK_LIST"))) {
            DeviceBlackListUtil.updateDeviceBlackList();
        }else {
            DeviceBlackListUtil.stopTask();
        }
        // 开启广告位黑名单
        if (Boolean.valueOf(configs.getString("AD_TAG_BLACK_LIST"))) {
            AdTagBlackListUtil.updateAdTagBlackList();
        }else {
            AdTagBlackListUtil.stopTask();
        }
        // 定时更新广告位黑名单
        if (Boolean.valueOf(configs.getString("IP_BLACK_LIST"))) {
            IpBlacklistUtil.updateAdTagBlackList();
        }
    }

    /*public static void main(String[] args) {
        AppBlackListUtil.updateAppBlackList();
        DeviceBlackListUtil.updateDeviceBlackList();
    }*/
}