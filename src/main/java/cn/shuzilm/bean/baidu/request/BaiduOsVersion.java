package cn.shuzilm.bean.baidu.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description: // 移动操作系统版本号
 * // 例如 Android 2.1, major, minor 分别是 2,1
 * // 例如 Iphone 4.2.1， major, minor, micro 分别是 4,2,
 * @Author: houkp
 * @CreateDate: 2019/3/13 17:51
 * @UpdateUser: houkp
 * @UpdateDate: 2019/3/13 17:51
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Data
public class BaiduOsVersion implements Serializable {

    private Integer os_version_major;// 1;
    private Integer os_version_minor;// 2;
    private Integer os_version_micro;// 3;
}
