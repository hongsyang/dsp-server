package cn.shuzilm.backend.queue;


import java.util.concurrent.LinkedBlockingQueue;

import cn.shuzilm.bean.control.AdBean;
import cn.shuzilm.bean.control.AdLogBean;
import cn.shuzilm.bean.control.AdNoticeDetailBean;


public class DataTransQueue {
	
	private static DataTransQueue ins;
	
	private LinkedBlockingQueue<AdLogBean> adLogBeanToDBQueue = new LinkedBlockingQueue<AdLogBean>();
	
	private LinkedBlockingQueue<AdNoticeDetailBean> adNoticeDetailBeanToDBQueue = new LinkedBlockingQueue<AdNoticeDetailBean>(); 
	
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
	
	/**
	 * 将广告明细bean放入队列
	 */
	public void put(AdNoticeDetailBean adNoticeDetail){
		try{
			this.adNoticeDetailBeanToDBQueue.put(adNoticeDetail);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 从队列中获取广告明细
	 */
	public AdNoticeDetailBean pollFromDetailQueue(){
		try{
			return this.adNoticeDetailBeanToDBQueue.poll();
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean isDetailQueueEmpty(){
		return this.adNoticeDetailBeanToDBQueue.isEmpty();
	}
}
