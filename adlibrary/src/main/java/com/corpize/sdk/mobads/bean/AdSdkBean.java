package com.corpize.sdk.mobads.bean;

/**
 * author: yh
 * date: 2020-02-17 22:01
 * description: TODO:
 */
public class AdSdkBean {

    /**
     * oceanengine : {"appid":"5049549"}
     * qq : {"appid":"1108817031"}
     * baidu : {"appid":"e4f0333c"}
     * ks : {"appid":"639900001"}
     */
    private OceanengineBean oceanengine;
    private QqBean          qq;
    private BaiduBean       baidu;
    private KSBean          ks;

    public KSBean getKs () {
        return ks;
    }

    public void setKs (KSBean ks) {
        this.ks = ks;
    }

    public OceanengineBean getOceanengine () {
        return oceanengine;
    }

    public void setOceanengine (OceanengineBean oceanengine) {
        this.oceanengine = oceanengine;
    }

    public QqBean getQq () {
        return qq;
    }

    public void setQq (QqBean qq) {
        this.qq = qq;
    }

    public BaiduBean getBaidu () {
        return baidu;
    }

    public void setBaidu (BaiduBean baidu) {
        this.baidu = baidu;
    }

    public static class CommonAdBean {
        /**
         * appid : 5049549
         */

        private String appid;

        public String getAppid () {
            return appid;
        }

        public void setAppid (String appid) {
            this.appid = appid;
        }
    }

    public static class OceanengineBean extends CommonAdBean {

    }

    public static class QqBean extends CommonAdBean {

    }

    public static class BaiduBean extends CommonAdBean {

    }

    public static class KSBean extends CommonAdBean {

    }
}
