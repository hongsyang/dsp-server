package cn.shuzilm.interf.pixcel.parser;
/**
* @Description:    ParameterParserFactory 参数解析工厂用于动态创建解析对象
* @Author:         houkp
* @CreateDate:     2018/7/19 18:42
* @UpdateUser:     houkp
* @UpdateDate:     2018/7/19 18:42
* @UpdateRemark:   修改内容
* @Version:        1.0
*/
public class ParameterParserFactory {

    public static ParameterParser parameterParser = null;

    public static ParameterParser getParameterParser(String className) {


        try {
            parameterParser= (ParameterParser) Class.forName(className).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return parameterParser;
    }
}
