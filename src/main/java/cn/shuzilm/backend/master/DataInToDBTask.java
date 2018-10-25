package cn.shuzilm.backend.master;

import java.sql.SQLException;

import cn.shuzilm.backend.queue.DataTransQueue;
import cn.shuzilm.bean.control.AdLogBean;

public class DataInToDBTask implements Runnable{
	
	private static TaskServicve taskService = new TaskServicve();

	@Override
	public void run() {
		DataTransQueue queue = DataTransQueue.getInstance();
		while(queue != null){
			AdLogBean adLog = queue.poll();
			if(adLog == null){
				try {
					Thread.sleep(500);
					continue;
				} catch (InterruptedException e) {
					e.printStackTrace();
					continue;
				}
			}
			try{
				//将广告日志插入库中，为满足实时，暂为一条一条插入
				taskService.insertDataToLog(adLog);
			}catch(SQLException e){
				e.printStackTrace();
				continue;
			}
		}
		
	}
	
	
}
