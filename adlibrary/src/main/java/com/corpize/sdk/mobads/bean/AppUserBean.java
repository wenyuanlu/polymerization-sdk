package com.corpize.sdk.mobads.bean;

/**
 * author: yh
 * date: 2019-08-09 09:39
 * description: 个人信息的保存
 */
public class AppUserBean {

    private static volatile AppUserBean singleton = null;

    private AppUserBean () {
    }

    public static AppUserBean getInstance () {
        if (singleton == null) {
            synchronized (AppUserBean.class) {
                if (singleton == null) {
                    singleton = new AppUserBean();
                }
            }
        }
        return singleton;
    }

    private boolean isShowAd;      //是否返回UserId,返回则展示广告
    private boolean isWakeUp;      //根据权重唤醒接口返回看是否开启唤醒服务

    //请求返回的数据
    /*private String         adid;            //广告位ID
    private String         adtype;          //广告位类型
    private int            version;         //协议版本号
    private String         storeurl;        //APP应用市场下载链接
    private int            pos;             //广告位在屏幕的第N屏
    private int            width;           //广告宽度
    private int            height;          //广告高度*/
    private String mid;             //媒体ID
    private String adTitle;
    private String adContent;

    //本地的参数(异步获取,初始化的时候获取)
    private String ua;              //设备的ua信息
    private double lat;             //设备当前所在的纬度
    private double lon;             //设备当前所在的经度
    private String province;        //省
    private String city;            //市
    private String street;          //区+街道

    //APP传递的数据
    private String titleColor;        //toobar的标题颜色
    private String backgroundColor;   //toobar的背景颜色

    public boolean isShowAd () {
        return isShowAd;
    }

    public void setShowAd (boolean showAd) {
        isShowAd = showAd;
    }

    public boolean isWakeUp () {
        return isWakeUp;
    }

    public void setWakeUp (boolean wakeUp) {
        isWakeUp = wakeUp;
    }

    public String getMid () {
        return mid;
    }

    public void setMid (String mid) {
        this.mid = mid;
    }


    public String getAdTitle () {
        return adTitle;
    }

    public void setAdTitle (String adTitle) {
        this.adTitle = adTitle;
    }

    public String getAdContent () {
        return adContent;
    }

    public void setAdContent (String adContent) {
        this.adContent = adContent;
    }

    public double getLat () {
        return lat;
    }

    public void setLat (double lat) {
        this.lat = lat;
    }

    public double getLon () {
        return lon;
    }

    public void setLon (double lon) {
        this.lon = lon;
    }

    public String getProvince () {
        return province;
    }

    public void setProvince (String province) {
        this.province = province;
    }

    public String getCity () {
        return city;
    }

    public void setCity (String city) {
        this.city = city;
    }

    public String getStreet () {
        return street;
    }

    public void setStreet (String street) {
        this.street = street;
    }

    public String getUa () {
        return ua;
    }

    public void setUa (String ua) {
        this.ua = ua;
    }

    public String getTitleColor () {
        return titleColor;
    }

    public void setTitleColor (String titleColor) {
        this.titleColor = titleColor;
    }

    public String getBackgroundColor () {
        return backgroundColor;
    }

    public void setBackgroundColor (String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

}
