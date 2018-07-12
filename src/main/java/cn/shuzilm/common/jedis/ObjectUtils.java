package cn.shuzilm.common.jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * 对象工具类
 * User: weichun.zhan
 * Date: 18-6-30
 * Time: 下午4:37
 */
public class ObjectUtils {

    private static final Logger logger = LoggerFactory.getLogger(ObjectUtils.class);

    public static byte[] convertToByteArray(Object obj) {
        if(obj == null){
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream(3000);
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            byte buf[] = byteArrayOutputStream.toByteArray();

            return buf;
        } catch (Exception e) {
            logger.error("{}", e);

        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    logger.error("{}", e);
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    logger.error("{}", e);
                }

            }
        }
        return null;
    }

    public static Object converteToObject(byte[] buf) {
        if(buf == null){
            return null;
        }
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream objectInputStream = null;
        Object obj = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(buf);
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            obj = objectInputStream.readObject();
        } catch (Exception e) {
            logger.error("{}", e);
        } finally {
            if(objectInputStream != null){
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    logger.error("{}", e);
                }
            }
            if(byteArrayInputStream != null){
                try {
                    byteArrayInputStream.close();
                } catch (IOException e) {
                    logger.error("{}", e);
                }
            }
        }
        return obj;
    }
}
