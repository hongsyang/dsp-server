package cn.shuzilm.rtb;

import java.util.HashMap;
import java.util.List;

public class JsonTest {

    /**
     * id : y053-test-t24-1535017029-0-854
     * device : {"ip":"49.5.2.83","ua":"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36","dpidmd5":"2fb25aac4555d7ffc89ca998cce00ecc","os":"iOS","osv":"8","devicetype":0,"ext":{"idfa":"97C304E-4C8E-4872-8666-03FE67DC15D","mac":"3D8A278F33E4F97181DF1EAEFE500D05","macmd5":"DC7D41E352D13D60765414D53F40BC25","ts":1374225975,"realip":"49.5.2.83","isipdx":false}}
     * user : {"id":"pPeBf0AWqh30"}
     * imp : [{"id":"7c8e1a1def4a45c3887adabbb71c74be","tagid":"669","bidfloor":1,"banner":{"w":320,"h":50,"mimes":["image/jpeg","image/png","application/x-shockwave-flash","video/x-flv","application/x-shockwave-flash","text/html","image/gif"],"pos":0},"ext":{"showtype":14,"has_winnotice":1,"has_clickthrough":0,"action_type":1},"secure":0}]
     * ext : {"media_source":1}
     */

    private String id;
    private DeviceBean device;
    private UserBean user;
    private ExtBeanX ext;
    private List<ImpBean> imp;

    public String getId() {
        HashMap hashMap =new HashMap<>();
        hashMap.put(0,1);
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DeviceBean getDevice() {
        return device;
    }

    public void setDevice(DeviceBean device) {
        this.device = device;
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public ExtBeanX getExt() {
        return ext;
    }

    public void setExt(ExtBeanX ext) {
        this.ext = ext;
    }

    public List<ImpBean> getImp() {
        return imp;
    }

    public void setImp(List<ImpBean> imp) {
        this.imp = imp;
    }

    public static class DeviceBean {
        /**
         * ip : 49.5.2.83
         * ua : Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36
         * dpidmd5 : 2fb25aac4555d7ffc89ca998cce00ecc
         * os : iOS
         * osv : 8
         * devicetype : 0
         * ext : {"idfa":"97C304E-4C8E-4872-8666-03FE67DC15D","mac":"3D8A278F33E4F97181DF1EAEFE500D05","macmd5":"DC7D41E352D13D60765414D53F40BC25","ts":1374225975,"realip":"49.5.2.83","isipdx":false}
         */

        private String ip;
        private String ua;
        private String dpidmd5;
        private String os;
        private String osv;
        private int devicetype;
        private ExtBean ext;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getUa() {
            return ua;
        }

        public void setUa(String ua) {
            this.ua = ua;
        }

        public String getDpidmd5() {
            return dpidmd5;
        }

        public void setDpidmd5(String dpidmd5) {
            this.dpidmd5 = dpidmd5;
        }

        public String getOs() {
            return os;
        }

        public void setOs(String os) {
            this.os = os;
        }

        public String getOsv() {
            return osv;
        }

        public void setOsv(String osv) {
            this.osv = osv;
        }

        public int getDevicetype() {
            return devicetype;
        }

        public void setDevicetype(int devicetype) {
            this.devicetype = devicetype;
        }

        public ExtBean getExt() {
            return ext;
        }

        public void setExt(ExtBean ext) {
            this.ext = ext;
        }

        public static class ExtBean {
            /**
             * idfa : 97C304E-4C8E-4872-8666-03FE67DC15D
             * mac : 3D8A278F33E4F97181DF1EAEFE500D05
             * macmd5 : DC7D41E352D13D60765414D53F40BC25
             * ts : 1374225975
             * realip : 49.5.2.83
             * isipdx : false
             */

            private String idfa;
            private String mac;
            private String macmd5;
            private int ts;
            private String realip;
            private boolean isipdx;

            public String getIdfa() {
                return idfa;
            }

            public void setIdfa(String idfa) {
                this.idfa = idfa;
            }

            public String getMac() {
                return mac;
            }

            public void setMac(String mac) {
                this.mac = mac;
            }

            public String getMacmd5() {
                return macmd5;
            }

            public void setMacmd5(String macmd5) {
                this.macmd5 = macmd5;
            }

            public int getTs() {
                return ts;
            }

            public void setTs(int ts) {
                this.ts = ts;
            }

            public String getRealip() {
                return realip;
            }

            public void setRealip(String realip) {
                this.realip = realip;
            }

            public boolean isIsipdx() {
                return isipdx;
            }

            public void setIsipdx(boolean isipdx) {
                this.isipdx = isipdx;
            }
        }
    }

    public static class UserBean {
        /**
         * id : pPeBf0AWqh30
         */

        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static class ExtBeanX {
        /**
         * media_source : 1
         */

        private int media_source;

        public int getMedia_source() {
            return media_source;
        }

        public void setMedia_source(int media_source) {
            this.media_source = media_source;
        }
    }

    public static class ImpBean {
        /**
         * id : 7c8e1a1def4a45c3887adabbb71c74be
         * tagid : 669
         * bidfloor : 1
         * banner : {"w":320,"h":50,"mimes":["image/jpeg","image/png","application/x-shockwave-flash","video/x-flv","application/x-shockwave-flash","text/html","image/gif"],"pos":0}
         * ext : {"showtype":14,"has_winnotice":1,"has_clickthrough":0,"action_type":1}
         * secure : 0
         */

        private String id;
        private String tagid;
        private int bidfloor;
        private BannerBean banner;
        private ExtBeanXX ext;
        private int secure;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTagid() {
            return tagid;
        }

        public void setTagid(String tagid) {
            this.tagid = tagid;
        }

        public int getBidfloor() {
            return bidfloor;
        }

        public void setBidfloor(int bidfloor) {
            this.bidfloor = bidfloor;
        }

        public BannerBean getBanner() {
            return banner;
        }

        public void setBanner(BannerBean banner) {
            this.banner = banner;
        }

        public ExtBeanXX getExt() {
            return ext;
        }

        public void setExt(ExtBeanXX ext) {
            this.ext = ext;
        }

        public int getSecure() {
            return secure;
        }

        public void setSecure(int secure) {
            this.secure = secure;
        }

        public static class BannerBean {
            /**
             * w : 320
             * h : 50
             * mimes : ["image/jpeg","image/png","application/x-shockwave-flash","video/x-flv","application/x-shockwave-flash","text/html","image/gif"]
             * pos : 0
             */

            private int w;
            private int h;
            private int pos;
            private List<String> mimes;

            public int getW() {
                return w;
            }

            public void setW(int w) {
                this.w = w;
            }

            public int getH() {
                return h;
            }

            public void setH(int h) {
                this.h = h;
            }

            public int getPos() {
                return pos;
            }

            public void setPos(int pos) {
                this.pos = pos;
            }

            public List<String> getMimes() {
                return mimes;
            }

            public void setMimes(List<String> mimes) {
                this.mimes = mimes;
            }
        }

        public static class ExtBeanXX {
            /**
             * showtype : 14
             * has_winnotice : 1
             * has_clickthrough : 0
             * action_type : 1
             */

            private int showtype;
            private int has_winnotice;
            private int has_clickthrough;
            private int action_type;

            public int getShowtype() {
                return showtype;
            }

            public void setShowtype(int showtype) {
                this.showtype = showtype;
            }

            public int getHas_winnotice() {
                return has_winnotice;
            }

            public void setHas_winnotice(int has_winnotice) {
                this.has_winnotice = has_winnotice;
            }

            public int getHas_clickthrough() {
                return has_clickthrough;
            }

            public void setHas_clickthrough(int has_clickthrough) {
                this.has_clickthrough = has_clickthrough;
            }

            public int getAction_type() {
                return action_type;
            }

            public void setAction_type(int action_type) {
                this.action_type = action_type;
            }
        }
    }
}
