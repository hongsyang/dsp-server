package cn.shuzilm.bean.control;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * 任务
 * 
 * @author wanght by 20180710
 */
@Data
public class TaskBean implements ICommand {
    private String adUid;
    /**
     * 命令
     */
    private int command;
    /**
     * 作用域，是指当前小时，还是全部
     * 0 当前小时 1 全部
     */
    private int scope;
    /**
     * 操作说明，一般指原因
     */
    private String commandMemo;
    /**
     * 曝光每小时限额
     */
    private long exposureLimitPerHour;
    /**
     * 曝光每天限额
     */
    private long exposureLimitPerDay;

    /**
     * 点击次数
     */
    private long clickNums;

    /**
     * 曝光次数
     */
    private long exposureNums;
    /**
     * 金钱
     */
    private float money;

    public static final long serialVersionUID = 1123223132123L;


    public static final int COMMAND_START = 0;
    public static final int COMMAND_UPDATE = 1;
    public static final int COMMAND_PAUSE = 2;
    public static final int COMMAND_STOP = 3;
    public static final int COMMAND_RECOVERY = 4;

    public static final int SCOPE_HOUR = 0;
    public static final int SCOPE_ALL = 1;



	/** 等待初始化 */
	private static final int TASK_STATE_READY = 0;
    private static final int TASK_STATE_RUNNING = 1;
    private static final int TASK_STATE_FINISHED = 2;
    private static final int TASK_STATE_PAUSED = 3;
    private static final int TASK_STATE_STOPED = 4;

	public static final Map<Integer, String> stateMap = new LinkedHashMap<Integer, String>();

	static {
		stateMap.put(TASK_STATE_READY, "空闲");
		stateMap.put(TASK_STATE_RUNNING, "投放中");
        stateMap.put(TASK_STATE_PAUSED,"暂停中");
        stateMap.put(TASK_STATE_STOPED,"终止投放");

		stateMap.put(TASK_STATE_FINISHED, "投放完成");

	}

	public TaskBean(String adUid) {
		this.adUid = adUid;
		init();
	}

	/**
	 * 初始化<br />
	 * 调用时机为新任务初始化或一个任务周期执行完毕后进行重新初始化
	 */
	public void init() {

	}


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Task");
        sb.append(", adUid=").append(adUid);
        sb.append('}');
        return sb.toString();
    }
}
