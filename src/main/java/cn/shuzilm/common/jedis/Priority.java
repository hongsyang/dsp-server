package cn.shuzilm.common.jedis;

/**
 * 队列优先级
 * User: weichun.zhan
 * Date: 18-6-30
 * Time: 下午1:16
 */
public enum Priority {

    //高优先级
    MAX_PRIORITY(10),
    //正常
    NORM_PRIORITY(5);


    private Integer value;

    public Integer getValue(){
        return this.value;
    }

    Priority(int value){
        this.value = value;
    }
}
