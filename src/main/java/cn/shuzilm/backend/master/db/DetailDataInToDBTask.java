package cn.shuzilm.backend.master.db;

import java.sql.SQLException;

import cn.shuzilm.backend.master.TaskServicve;
import cn.shuzilm.backend.queue.DataTransQueue;
import cn.shuzilm.bean.control.AdLogBean;
import cn.shuzilm.bean.control.AdNoticeDetailBean;

public class DetailDataInToDBTask implements Runnable{
	
	private static TaskServicve taskService = new TaskServicve();

	@Override
	public void run() {
		DataTransQueue queue = DataTransQueue.getInstance();
		while(queue != null){
			AdNoticeDetailBean adNoticeDetail = queue.pollFromDetailQueue();
			if(adNoticeDetail == null){
				try {
					Thread.sleep(500);
					continue;
				} catch (InterruptedException e) {
					e.printStackTrace();
					continue;
				}
			}
			try{
				//将广告明细插入库中，为满足实时，暂为一条一条插入
				taskService.insertDataToNoticeDetailPerHour(adNoticeDetail);
			}catch(SQLException e){
				e.printStackTrace();
				continue;
			}
		}
		
	}
	
	
}
