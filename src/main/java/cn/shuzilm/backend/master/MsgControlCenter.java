package cn.shuzilm.backend.master;

import cn.shuzilm.bean.*;
import cn.shuzilm.bean.control.*;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.common.jedis.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: wanghaiting
 * Date: 18-7-11
 * Time: 上午10:26
 */
public class MsgControlCenter {

    public static final String UP = "_up";
    public static final String DOWN = "_tdown";
    public static final String AD_BEAN = "_ad";
    public static final String PIXEL_STATUS = "_pixel";
    public static final String BID_STATUS = "_bid";
    public static final String MASTER_QUEUE_NAME = "_task_queue";
    public static final String NODE_STATUS = "_node_status";
    public static final String ADX_FLOW = "_adx_flow";
    public static final String APP_FLOW = "_app_flow";
    public static final String FLOW_DOWN = "_flow_tdown";

    public static ICommand getCommandFromMasterQueue(){
        Object obj = JedisQueueManager.getElementFromQueue(MASTER_QUEUE_NAME);
        if(obj == null)
            return null;
        else
            return (ICommand)obj;
    }

    public static boolean sendCommandToMasterQueue(ICommand command,Priority p){
        return JedisQueueManager.putElementToQueue(MASTER_QUEUE_NAME,command,p);
    }

    /**
     * 主控发送至节点的转码命令
     * @param nodeName
     * @param command
     * @param priority
     * @return
     */
    public static boolean sendCommand(String nodeName,ICommand command,Priority priority){
        return JedisQueueManager.putElementToQueue(nodeName + DOWN,command,priority);
    }

    public static boolean sendAdBean(String nodeName, ArrayList<AdBean> beanList, Priority priority){
        return JedisQueueManager.putElementToQueue(nodeName + AD_BEAN,beanList,priority);
    }

    public static ArrayList<AdBean> recvAdBean(String nodeName){
        Object obj = JedisQueueManager.getElementFromQueue(nodeName + AD_BEAN);
        if(obj == null)
            return null;
        else
            return (ArrayList<AdBean>)obj;
    }



    public static boolean sendTask(String nodeName, ArrayList<TaskBean> taskList, Priority priority){
        return JedisQueueManager.putElementToQueue(nodeName + DOWN,taskList,priority);
    }
    
    public static boolean sendTask(String nodeName, TaskBean bean, Priority priority){
        return JedisQueueManager.putElementToQueue(nodeName + DOWN,bean,priority);
    }

    public static ArrayList<TaskBean> recvTask(String nodeName){
        Object obj = JedisQueueManager.getElementFromQueue(nodeName + DOWN);
        if(obj == null)
            return null;
        else
            return (ArrayList<TaskBean>)obj;
    }
    
    public static boolean sendAdxFlow(String nodeName,ConcurrentHashMap<String,Long> adxFlowMap){
    	return JedisQueueManager.putElementToQueue(nodeName + ADX_FLOW, adxFlowMap, Priority.NORM_PRIORITY);
    }
    
    public static ConcurrentHashMap<String,Long> recvAdxFlow(String nodeName){
    	Object obj = JedisQueueManager.getElementFromQueue(nodeName + ADX_FLOW);
    	if(obj == null)
    		return null;
    	else
    		return (ConcurrentHashMap<String, Long>) obj;
    }
    
    public static boolean sendAppFlow(String nodeName,ConcurrentHashMap<String,Long> appFlowMap){
    	return JedisQueueManager.putElementToQueue(nodeName + APP_FLOW, appFlowMap, Priority.NORM_PRIORITY);
    }
    
    public static ConcurrentHashMap<String,Long> recvAppFlow(String nodeName){
    	Object obj = JedisQueueManager.getElementFromQueue(nodeName + APP_FLOW);
    	if(obj == null)
    		return null;
    	else
    		return (ConcurrentHashMap<String, Long>) obj;
    }
    
    public static boolean sendFlowTask(String nodeName, ArrayList<FlowTaskBean> beanList, Priority priority){
        return JedisQueueManager.putElementToQueue(nodeName + FLOW_DOWN,beanList,priority);
    }

    public static ArrayList<FlowTaskBean> recvFlowTask(String nodeName){
        Object obj = JedisQueueManager.getElementFromQueue(nodeName + FLOW_DOWN);
        if(obj == null)
            return null;
        else
            return (ArrayList<FlowTaskBean>)obj;
    }

    /**
     * 转码机接收主控发送的指令
     * @param nodeName
     * @return
     */
    public static ICommand recvCommand(String nodeName){
        Object obj = JedisQueueManager.getElementFromQueue(nodeName+DOWN);
        if(obj == null)
            return null;
        else
            return (ICommand)obj;
    }



    /**
     *  主控节点： 接收 pixel 状态报告
     * @param nodeId
     * @return
     */
    public static AdPixelBean recvPixelStatus(String nodeId){
        Object obj =  JedisQueueManager.getElementFromQueue(nodeId + PIXEL_STATUS);
        if(obj!=null)
            return (AdPixelBean)obj;
        else
            return null;
    }

    /**
     * PIXEL 节点  发送 pixel 状态报告
     * @param nodeId
     * @param bean
     * @return
     */
    public static boolean sendPixelStatus(String nodeId,AdPixelBean bean){
        return JedisQueueManager.putElementToQueue(nodeId + PIXEL_STATUS,bean,Priority.NORM_PRIORITY) ;

    }


    /**
     * 主控机 ： 接收 bid 报告
     * @param nodeId
     * @return
     */
    public static NodeStatusBean recvBidStatus(String nodeId){
        Object obj =  JedisQueueManager.getElementFromQueue(nodeId + BID_STATUS);
        if(obj!=null)
            return (NodeStatusBean)obj;
        else
            return null;
    }

    /**
     * RTB 节点：发送 bid 报告
     * @param nodeId
     * @param bean
     * @return
     */
    public static boolean sendBidStatus(String nodeId,NodeStatusBean bean){
        return JedisQueueManager.putElementToQueue(nodeId + BID_STATUS,bean,Priority.NORM_PRIORITY) ;

    }
    
    public static void removeAll(String nodeId){
    	JedisQueueManager.removeAll(nodeId.concat(DOWN));
    	JedisQueueManager.removeAll(nodeId.concat(AD_BEAN));
    	
    }
    
    /**
     * 主控机 ： 接收节点状态
     * @param nodeId
     * @return
     */
    public static NodeStatusBean recvNodeStatus(String nodeId){
        Object obj =  JedisQueueManager.getElementFromQueue(nodeId + NODE_STATUS);
        if(obj!=null)
            return (NodeStatusBean)obj;
        else
            return null;
    }

    /**
     * 发送节点状态
     * @param nodeId
     * @param bean
     * @return
     */
    public static boolean sendNodeStatus(String nodeId,NodeStatusBean bean){
        return JedisQueueManager.putElementToQueue(nodeId + NODE_STATUS,bean,Priority.NORM_PRIORITY) ;

    }


}
