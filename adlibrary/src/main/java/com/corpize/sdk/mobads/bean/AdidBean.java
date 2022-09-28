package com.corpize.sdk.mobads.bean;

/**
 * author ：yh
 * date : 2020-02-11 15:57
 * description : 广告位的实体类
 */
public class AdidBean {
    /**
     * width : 640
     * version : 20170518
     * storeurl : https://itunes.apple.com/cn/app/
     * sdk : {"tengxun":{"adid":"adid","weight":32,"appid":"appid"},"chuangshanjia":{"adid":"adid_202","weight":68,"appid":"appid_202"}}
     * pos : 1
     * adtype : 202
     * bundle : test.com
     * appname : 测试
     * height : 960
     * p : false
     */
    private int     width;
    private String  layout;//返回的样式1,左图右文 2,左文右图 3,上图下文 4,上文下图 5,文字在图片上，即文字浮层
    private String  version;
    private String  storeurl;
    private SdkBean sdk;
    private String  pos;
    private String  adtype;
    private String  bundle;
    private String  appname;
    private int     height;
    private int     weight;
    private boolean p;

    public int getWidth () {
        return width;
    }

    public void setWidth (int width) {
        this.width = width;
    }

    public String getLayout () {
        return layout;
    }

    public void setLayout (String layout) {
        this.layout = layout;
    }

    public String getVersion () {
        return version;
    }

    public void setVersion (String version) {
        this.version = version;
    }

    public String getStoreurl () {
        return storeurl;
    }

    public void setStoreurl (String storeurl) {
        this.storeurl = storeurl;
    }

    public SdkBean getSdk () {
        return sdk;
    }

    public void setSdk (SdkBean sdk) {
        this.sdk = sdk;
    }

    public String getPos () {
        return pos;
    }

    public void setPos (String pos) {
        this.pos = pos;
    }

    public String getAdtype () {
        return adtype;
    }

    public void setAdtype (String adtype) {
        this.adtype = adtype;
    }

    public String getBundle () {
        return bundle;
    }

    public void setBundle (String bundle) {
        this.bundle = bundle;
    }

    public String getAppname () {
        return appname;
    }

    public void setAppname (String appname) {
        this.appname = appname;
    }

    public int getHeight () {
        return height;
    }

    public void setHeight (int height) {
        this.height = height;
    }

    public int getWeight () {
        return weight;
    }

    public void setWeight (int weight) {
        this.weight = weight;
    }

    public boolean isP () {
        return p;
    }

    public void setP (boolean p) {
        this.p = p;
    }

    public static class SdkBean {
        /**
         * tengxun : {"adid":"adid","weight":32,"appid":"appid"}
         * chuangshanjia : {"adid":"adid_202","weight":68,"appid":"appid_202"}
         */

        private TengxunBean       qq;
        private ChuangshanjiaBean oceanengine;
        private BaiQingTeng       baidu;
        private KuaiShouBean      kuaishou;

        public KuaiShouBean getKuaishou () {
            return kuaishou;
        }

        public void setKuaishou (KuaiShouBean kuaishou) {
            this.kuaishou = kuaishou;
        }

        public TengxunBean getTengxun () {
            return qq;
        }

        public void setTengxun (TengxunBean tengxun) {
            this.qq = tengxun;
        }

        public ChuangshanjiaBean getChuangshanjia () {
            return oceanengine;
        }

        public void setChuangshanjia (ChuangshanjiaBean chuangshanjia) {
            this.oceanengine = chuangshanjia;
        }

        public BaiQingTeng getBai () {
            return baidu;
        }

        public void setBai (BaiQingTeng baidu) {
            this.baidu = baidu;
        }

        public static class BaseAdIdBean {
            /**
             * adid : adid_202
             * weight : 68
             * appid : appid_202
             */
            private String adid;
            private int    weight;
            private String appid;

            public String getAdid () {
                return adid;
            }

            public void setAdid (String adid) {
                this.adid = adid;
            }

            public int getWeight () {
                return weight;
            }

            public void setWeight (int weight) {
                this.weight = weight;
            }

            public String getAppid () {
                return appid;
            }

            public void setAppid (String appid) {
                this.appid = appid;
            }
        }

        public static class TengxunBean extends BaseAdIdBean {

        }

        public static class ChuangshanjiaBean extends BaseAdIdBean {

        }

        public static class BaiQingTeng extends BaseAdIdBean {

        }

        public static class KuaiShouBean extends BaseAdIdBean {

        }
    }
}
