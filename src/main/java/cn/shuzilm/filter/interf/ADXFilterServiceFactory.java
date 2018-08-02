package cn.shuzilm.filter.interf;


public class ADXFilterServiceFactory {

    public static ADXFilterService getADXFilterService(String className) {
        ADXFilterService adxFilterService = null;

        try {
            adxFilterService= (ADXFilterService) Class.forName(className).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return adxFilterService;
    }
}
