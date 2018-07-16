package cn.shuzilm.backend.master;

import cn.shuzilm.bean.*;
import cn.shuzilm.bean.NodeStatusBean;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.common.jedis.Priority;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wanghaiting
 * Date: 18-7-11
 * Time: 上午10:26
 */
public class MsgControlCenter {

    public static final String UP = "_up";
    public static final String DOWN = "_down";
    public static final String PIXEL_STATUS = "_pixel";
    public static final String BID_STATUS = "_bid";
    public static final String MASTER_QUEUE_NAME = "task_queue";

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




    public static List<AdPixelBean> listRecvNodeStatus(){
        List<AdPixelBean> list = new ArrayList<AdPixelBean>();
        for(WorkNodeBean node : AdFlowControl.nodeList){
            AdPixelBean bean = recvPixelStatus(node.getName());
            list.add(bean);
        }
        return list;
    }

    /**
     * 主控接收从转码机发来的 机器状态 报告
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
     * 转码机定时将状态报告发送至 主控机
     * @param nodeId
     * @param bean
     * @return
     */
    public static boolean sendPixelStatus(String nodeId,AdPixelBean bean){
        return JedisQueueManager.putElementToQueue(nodeId + PIXEL_STATUS,bean,Priority.NORM_PRIORITY) ;

    }


    /**
     * 主控接收从转码机发来的 机器状态 报告
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
     * 转码机定时将状态报告发送至 主控机
     * @param nodeId
     * @param bean
     * @return
     */
    public static boolean sendBidStatus(String nodeId,NodeStatusBean bean){
        return JedisQueueManager.putElementToQueue(nodeId + BID_STATUS,bean,Priority.NORM_PRIORITY) ;

    }


}
