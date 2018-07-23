package cn.shuzilm.interf.rtb.parser;


public class RequestServiceFactory {

    public static RequestService getRequestService(String className) {
        RequestService requestService = null;

        try {
            requestService= (RequestService) Class.forName(className).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return requestService;
    }
}
