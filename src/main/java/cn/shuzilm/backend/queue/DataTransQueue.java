package cn.shuzilm.backend.queue;


import java.util.concurrent.LinkedBlockingQueue;

import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdLogBean;


public class DataTransQueue {
	
	private static DataTransQueue ins;
	
	private LinkedBlockingQueue<AdLogBean> adLogBeanToDBQueue = new LinkedBlockingQueue<AdLogBean>();
	
	public static DataTransQueue getInstance(){
		if(ins == null){
			ins = new DataTransQueue();
		}
		return ins;
	}
	
	/**
	 * 将广告日志bean放入队列
	 */
	public void put(AdLogBean adLog){
		try{
			this.adLogBeanToDBQueue.put(adLog);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 从队列中获取广告日志
	 */
	public AdLogBean poll(){
		try{
			return this.adLogBeanToDBQueue.poll();
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean isEmpty(){
		return this.adLogBeanToDBQueue.isEmpty();
	}
}
