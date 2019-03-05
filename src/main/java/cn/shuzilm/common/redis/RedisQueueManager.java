package cn.shuzilm.common.redis;

import cn.shuzilm.common.jedis.ObjectUtils;
import cn.shuzilm.common.jedis.Priority;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.LinkedList;
import java.util.List;

/**
 * jedis队列
 * User: weichun.zhan
 * Date: 18-6-30
 * Time: 下午12:23
 */
public class RedisQueueManager {

    private static final Logger logger = LoggerFactory.getLogger(RedisQueueManager.class);

    private static RedisManager manager = RedisManager.getInstance();
    public static void init(){
        manager = RedisManager.getInstance();
    }
    /**
     * 放入指定队列
     *  放入队列中的对象必须要override equals 和 hashcode 方法 , 实现Serializable接口,方便查看对象值，建议override toString方法
     * @param queueName
     * @param value
     * @param priority  优先级 默认是NORM_PRIORITY
     * @return true，成功; false,失败
     */
    public static boolean putElementToQueue(String queueName, Object value, Priority priority,RedisManager manager,String configFileName) {
        if (StringUtils.isEmpty(queueName)) {
            logger.error("Queue Name should not be null");
            return false;
        }
        Jedis jedis = null;
        try {
            jedis = manager.getResource(configFileName);
            long reValue = 0;
            if (null == priority) {
                priority = Priority.NORM_PRIORITY;
            }
            if (priority.equals(Priority.MAX_PRIORITY)) {
                reValue = jedis.rpush(queueName.getBytes("utf-8"), ObjectUtils.convertToByteArray(value));
            } else if (priority.equals(Priority.NORM_PRIORITY)) {
                reValue = jedis.lpush(queueName.getBytes("utf-8"), ObjectUtils.convertToByteArray(value));
            } else {
                logger.error("The priority incorrect.");
                return false;
            }

            return reValue > 0;
        } catch (Exception e) {
            logger.error("Put element {} failure to queue {}, {}", new Object[]{value, queueName, e});
            return false;
        } finally {
            manager.returnResource(jedis);
        }
    }


    /**
     * 放入指定队列
     *  放入队列中的对象必须要override equals 和 hashcode 方法 , 实现Serializable接口,方便查看对象值，建议override toString方法
     * @param queueName
     * @param value
     * @param priority  优先级 默认是NORM_PRIORITY
     * @return true，成功; false,失败
     */
    public static boolean putElementToQueue(String queueName, Object value, Priority priority) {
        if (StringUtils.isEmpty(queueName)) {
            logger.error("Queue Name should not be null");
            return false;
        }
        Jedis jedis = null;
        try {
            jedis = manager.getResource();
            long reValue = 0;
            if (null == priority) {
                priority = Priority.NORM_PRIORITY;
            }
            if (priority.equals(Priority.MAX_PRIORITY)) {
                reValue = jedis.rpush(queueName.getBytes("utf-8"), ObjectUtils.convertToByteArray(value));
            } else if (priority.equals(Priority.NORM_PRIORITY)) {
                reValue = jedis.lpush(queueName.getBytes("utf-8"), ObjectUtils.convertToByteArray(value));
            } else {
                logger.error("The priority incorrect.");
                return false;
            }

            return reValue > 0;
        } catch (Exception e) {
            logger.error("Put element {} failure to queue {}, {}", new Object[]{value, queueName, e});
            return false;
        } finally {
            manager.returnResource(jedis);
        }
    }
    public static String getStringFromQueue(String queueName,RedisManager manager,String configFileName) {
        Jedis jedis = null;
        try {
            jedis = manager.getResource(configFileName);
            Object obj = jedis.rpop(queueName);
            if(obj !=null)
                return obj.toString();
            else
                return null;
        }catch(Exception ex){
            logger.error("Put element failure" + queueName);
            return null;
        }finally{
            manager.returnResource(jedis);
        }
    }

    /**
     * 从指定队列获取值
     *
     * @param queueName
     * @return null or value
     */
    public static Object getElementFromQueue(String queueName,RedisManager manager,String configFileName) {
        if (StringUtils.isEmpty(queueName)) {
            logger.error("Queue Name should not be null");
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = manager.getResource(configFileName);
            return ObjectUtils.converteToObject(jedis.rpop(queueName.getBytes("utf-8")));
        } catch (Exception e) {
            logger.error("Get element failure from queue {}, {}", queueName, e);
            return null;
        } finally {
            manager.returnResource(jedis);
        }
    }
    public static boolean putStringToQueue(String queueName, String value,RedisManager manager,String configFileName) {
        Jedis jedis = null;
        try {
            jedis = manager.getResource(configFileName);
            jedis.lpush(queueName,value);
            return true;
        }catch(Exception ex){
            logger.error("Put element {} failure to queue {}, {}", new Object[]{value, queueName, ex});
            return false;
        }finally{
            manager.returnResource(jedis);
        }
    }

    /**
     * 从指定队列获取值
     *
     * @param queueName
     * @return null or value
     */
    public static Object getElementFromQueue(String queueName) {
        RedisQueueManager.init();
        if (StringUtils.isEmpty(queueName)) {
            logger.error("Queue Name should not be null");
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = manager.getResource();
            return ObjectUtils.converteToObject(jedis.rpop(queueName.getBytes("utf-8")));
        } catch (Exception e) {
            logger.error("Get element failure from queue {}, {}", queueName, e);
            return null;
        } finally {
            manager.returnResource(jedis);
        }
    }

    /**
     * 获取当前队列中元素个数
     *
     * @param queueName
     * @return 元素个数
     */
    public static Long getLength(String queueName) {
        if (StringUtils.isEmpty(queueName)) {
            logger.error("Queue Name should not be null");
            return null;
        }
        Jedis jedis = null;
        try {
            jedis = manager.getResource();
            return jedis.llen(queueName);
        } catch (Exception e) {
            logger.error("Get length failure from queue {}, {}", queueName, e);
            return null;
        } finally {
            manager.returnResource(jedis);
        }
    }

    /**
     * 设置队列中某个元素到队列的头部
     * 注：放入队列中的对象必须要override equals 和 hashcode 方法 , 实现Serializable接口,方便查看对象值，建议override toString方法
     * @param queueName
     * @param value
     * @return
     */
    public static boolean setElementToHead(String queueName, Object value) {
        long beginTime = System.currentTimeMillis();
        if (StringUtils.isEmpty(queueName)) {
            logger.error("Queue Name should not be null");
            return false;
        }
        Jedis jedis = null;
        int index = 0;

        try {
            byte[] tmp = null;
            byte[] quequeByteArr = queueName.getBytes("utf-8");
            jedis = manager.getResource();
            long length = getLength(queueName);
            while (true) {
                tmp = jedis.lindex(quequeByteArr, index);
                index++;
                if (value.equals(ObjectUtils.converteToObject(tmp))) {
                    jedis.lrem(quequeByteArr, 0, tmp);
                    logger.info("Queue Name = [{}], Element= [{}], set element to head  successful, took times: [{}]" , new Object[]{queueName , value , (System.currentTimeMillis() - beginTime)});
                    return jedis.rpush(quequeByteArr, ObjectUtils.convertToByteArray(value)) > 0; //必须是把value重新加入队列,因为修改了优先级.
                }
                if( index > length ){
                    logger.info("Queue Name = [{}], Element= [{}], set element to head  failure, took times: [{}]" , new Object[]{queueName , value , (System.currentTimeMillis() - beginTime)});
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("SetElementToHead failure from queue {}, {}", queueName, e);
            return false;
        } finally {
            manager.returnResource(jedis);
        }
    }

    /**
     * 返回队列中的所有数据
     *
     * @param queueName
     * @return
     */
    public static List<Object> getAllElement(String queueName) {
        List<Object> list = new LinkedList<Object>();
        if (StringUtils.isEmpty(queueName)) {
            logger.error("Queue Name should not be null");
            return list;
        }
        Jedis jedis = null;
        int index = 0;

        try {
            jedis = manager.getResource();
            Object tmp = null;
            while ((tmp = ObjectUtils.converteToObject(jedis.lindex(queueName.getBytes("utf-8"), index))) != null) {
                list.add(tmp);
                index++;
            }
        } catch (Exception e) {
            logger.error("getAllElement failure from queue {}, {}", queueName, e);
            return list;
        } finally {
            manager.returnResource(jedis);
        }
        return list;
    }

    /**
     * 删除队列的所有元素
     * @param queueName
     * @return
     */
    public static boolean removeAll(String queueName) {
        if (StringUtils.isEmpty(queueName)) {
            logger.error("Queue Name should not be null");
            return false;
        }
        Jedis jedis = null;
        try {
            jedis = manager.getResource();
            Long r = jedis.del(queueName.getBytes("utf-8"));
            logger.info("{} elements be deleted." , r);
            return r > 0;
        } catch (Exception e) {
            logger.error("Get length failure from queue {}, {}", queueName, e);
            return false;
        } finally {
            manager.returnResource(jedis);
        }
    }

    public static boolean removeElement(String queueName,Object value){
        long beginTime = System.currentTimeMillis();
        if (StringUtils.isEmpty(queueName)) {
            logger.error("Queue Name should not be null");
            return false;
        }
        Jedis jedis = null;
        int index = 0;
        try {
            jedis = manager.getResource();
            byte[] tmp = null;
            byte[] quequeByteArr = queueName.getBytes("utf-8");
            jedis = manager.getResource();
            long length = getLength(queueName);
            while (true) {
                tmp = jedis.lindex(quequeByteArr, index);
                index++;
                if (value.equals(ObjectUtils.converteToObject(tmp))) {
                    return jedis.lrem(quequeByteArr, 0, tmp) > 0;

                }
                if( index > length ){
                    logger.info("Queue Name = [{}], remove element failure, took times: [{}]" ,
                            new Object[]{queueName ,(System.currentTimeMillis() - beginTime)});
                    return false;
                }
            }
        } catch (Exception e) {
            logger.error("Get length failure from queue {}, {}", queueName, e);
            return false;
        } finally {
            manager.returnResource(jedis);
        }
    }
    
    public static void main(String[] args) {
    	
//    	Object o1 = getElementFromQueue("pixel-001_ad");
//    	Object o2 = getElementFromQueue("rtb-002ad");
//    	Object o3 = getElementFromQueue("rtb-003ad");
//    	System.out.println(o1);
//    	System.out.println(o2);
//    	System.out.println(o3);
//    	try{
//    		for(int i=0;i<100;i++){
//    		try{
//    		TaskBean taskBean = MsgControlCenter.recvTask("rtb-001");
//    		System.out.println(taskBean.getAdUid());
//    		}catch(Exception ex){
//    			System.out.println(ex);
//    			continue;
//    		}
//    		}
//    	}catch(Exception e){
//    		System.out.println(e);
//    	}
//    	List list = getAllElement("rtb-001_down");
//    	
//    	System.out.println(list.size());
//    	long start = System.currentTimeMillis();
//    	putElementToQueue("rtb-010","123",null);
//    	putElementToQueue("rtb-010","456",null);
//    	temp:while(true){
 //   	List<TaskBean> taskList = (List<TaskBean>) getElementFromQueue("rtb-001_tdown");
 //   	System.out.println(taskList.size());
//    	for(AdBean ad:adBeanList){
//    		System.out.println(ad.getAdUid());
//    		if(ad.getAdUid().equals("438276e7-738d-4b39-9d86-d58d5dd84b77")){
//    			int a[][] = ad.getTimeSchedulingArr();
//    			if(a == null){
//    				System.out.println("空");
//    			}else{
//    				for(int i=0;i<a.length;i++){
//    					for(int j=0;j<a[i].length;j++){
//    						System.out.print(a[i][j]);
//    					}
//    					System.out.println();
//    				}
//    			}
//    			break temp;
//    		}
//    	}
//    	}

//    	System.out.println(taskList.size());
    	//NodeStatusBean node = (NodeStatusBean)getElementFromQueue("rtb-001_node_status");
    	//System.out.println(node);
    	//System.out.println(node.getNodeName());
    	//System.out.println(node.getLastUpdateTime());
//    	removeAll("rtb-100_ad");
//    	removeAll("rtb-100_flow_tdown");
//    	for(int i=2;i<5;i++){
//    	removeAll("pixel-00"+i+"_pixel");
//    	//removeAll("rtb-00"+"_pixel");
//    	removeAll("pixel-00"+i+"_ad");
//    	//removeAll("rtb-00"+"_ad");
//    	removeAll("pixel-00"+i+"_tdown");
//    	//removeAll("rtb-00"+"_tdown");
//    	}
	}
}
