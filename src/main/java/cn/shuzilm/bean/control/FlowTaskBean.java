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
public class FlowTaskBean implements ICommand {
    private String aid;
    /**
     * 命令
     */
    private int command;

    public static final long serialVersionUID = 1123223132124L;


    public static final int COMMAND_START = 0;
    public static final int COMMAND_UPDATE = 1;
    public static final int COMMAND_PAUSE = 2;
    public static final int COMMAND_STOP = 3;
    public static final int COMMAND_RECOVERY = 4;

}
