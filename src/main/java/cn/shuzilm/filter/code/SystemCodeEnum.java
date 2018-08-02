package cn.shuzilm.filter.code;

/**
* @Description:    SystemCodeEnum 过滤参数结果枚举
* @Author:         houkp
* @CreateDate:     2018/8/2 15:27
* @UpdateUser:     houkp
* @UpdateDate:     2018/8/2 15:27
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
public enum SystemCodeEnum {

    SYSTEM_ERROR("-99","系统错误"),
    CODE_SUCCESS("000000","成功"),
    CODE_FAIL("000001","错误的请求数据");


    private String code;
    private String message;

    SystemCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
