package cn.shuzilm.backend.master;

import cn.shuzilm.bean.ICommand;
import cn.shuzilm.bean.NodeStatusBean;
import cn.shuzilm.common.Constants;
import cn.shuzilm.common.jedis.JedisQueueManager;
import cn.shuzilm.common.jedis.Priority;
import com.yao.config.Node;
import com.yao.config.tool.SimpleConfigManager;

import java.util.ArrayList;
import java.util.Date;
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
    public static final String NODE_STATUS = "_node_status";
    public static final String MASTER_QUEUE_NAME = "task_queue";

    public static List<String> getNodeList(){
        List<Node> list = SimpleConfigManager.getConfig().getNodes("nodes");
        List<String> nodeList = new ArrayList<String>();
        for(Node n:list){
            nodeList.add(n.getName());
        }
        return nodeList;
    }

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
     * @param nodeId
     * @param command
     * @param priority
     * @return
     */
    public static boolean sendCommand(String nodeId,ICommand command,Priority priority){
        return JedisQueueManager.putElementToQueue(nodeId + DOWN,command,priority);
    }

    /**
     * 转码机接收主控发送的指令
     * @param nodeId
     * @return
     */
    public static ICommand recvCommand(String nodeId){
        Object obj = JedisQueueManager.getElementFromQueue(nodeId+DOWN);
        if(obj == null)
            return null;
        else
            return (ICommand)obj;
    }




    public static List<NodeStatusBean> listRecvNodeStatus(){
        List<NodeStatusBean> list = new ArrayList<NodeStatusBean>();
        for(String nodeIp:getNodeList()){
            NodeStatusBean bean = recvNodeStatus(nodeIp);
            list.add(bean);
        }
        return list;
    }

    /**
     * 主控接收从转码机发来的 机器状态 报告
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
     * 转码机定时将状态报告发送至 主控机
     * @param nodeId
     * @param bean
     * @return
     */
    public static boolean sendNodeStatus(String nodeId,NodeStatusBean bean){
        return JedisQueueManager.putElementToQueue(nodeId+NODE_STATUS,bean,Priority.NORM_PRIORITY) ;

    }
    public static boolean sendNodeStatus(NodeStatusBean bean){
        return JedisQueueManager.putElementToQueue(bean.getHost()+NODE_STATUS,bean,Priority.NORM_PRIORITY) ;

    }

}
