package com.corpize.sdk.mobads.common;


public final class Constants {

    //TODO:每次打新包,请在build中修改版本号,默认显示
    public static       Boolean IS_SHOW_LOG         = false;                 //给logUtil使用,暴露给三方
    public static       Boolean IS_SHOW_HTTP_LOG    = false;                 //给http使用,http的日志不暴露出去
    public static       Boolean IS_DEBUG            = false;                //是否是denug模式,穿山甲的日志
    public static final Boolean IS_TEST             = false;                //是否是测试环境的URL
    public static final boolean IS_INIT_SIMPLE_DATA = false;                //是否使用sdk demo 自带的广告位信息

    private static final String SDK_VERSION = "2.0.1";                         //sdK的版本
    private static final String mBaseUrl1   = "http://adx-test.corpize.com/";  //测试基础的url
    private static final String mBaseUrl2   = "http://adx.corpize.com/";       //生产基础的url(获取广告信息)
    private static final String mBaseUrl3   = "http://a.corpize.com/";         //生产基础的url(获取广告配置)

    public static final String OAID = "qcadoaid";

    //测试数据
    //百青藤
    public static class BaiduSimpleData {
        //appid
        public static final String APP_ID             = "e866cfb0";
        //开屏广告
        public static final String SPLASH_AD_ID       = "2058622";
        //banner
        public static final String BANNER_AD_ID       = "2015351";
        //插屏
        public static final String INSERT_AD_ID       = "2403633";
        //单图信息流
        //多图信息流
        public static final String SINGLE_INFO_AD_ID  = "2058628";
        //激励视频
        public static final String REWARD_VIDEO_AD_ID = "5925490";
    }

    //广点通
    public static class GDTSimpleData {
        //app id
        public static final String APP_ID             = "1108817031";
        //开屏广告
        public static final String SPLASH_AD_ID       = "8863364436303842593";
        //banner
        public static final String BANNER_AD_ID       = "1000664018562886";
        //插屏
        public static final String INSERT_AD_ID       = "4080298282218338";
        //单图信息流
        //多图信息流
        public static final String SINGLE_INFO_AD_ID  = "7030020348049331";
        //激励视频
        public static final String REWARD_VIDEO_AD_ID = "2090845242931421";
    }

    //穿山甲
    public static class CSJSimpleData {
        //appid
        public static final String APP_ID             = "5001121";
        //开屏广告
        public static final String SPLASH_AD_ID       = "801121648";
        //banner
        public static final String BANNER_AD_ID       = "901121148";
        //插屏
        public static final String INSERT_AD_ID       = "901121725";
        //单图信息流
        //多图信息流
        public static final String SINGLE_INFO_AD_ID  = "901121253";
        //激励视频
        public static final String REWARD_VIDEO_AD_ID = "945051461";
    }

    //快手
    public static class KSSimpleData {
        //appid
        public static final String KS_APP_ID            = "639900001";
        //开屏广告
        public static final Long   KS_SPLASH_POSID      = 6399000001L;
        //单图信息流
        public static final Long   KS_INFO_SINGLE_POSID = 6399000002L;
        //banner
        public static final Long   KS_POSID_FEED_TYPE_1 = 4000000001L; // Feed测试+文字悬浮在图片
        //激励视频
        public static final Long   KS_REWARD_VIDEO      = 6399000003L;
    }


    //获取广告配置
    public static String getBaseUrl () {
        if (IS_TEST) {
            return mBaseUrl1;
        } else {
            return mBaseUrl3;
        }
    }

    //获取广告信息
    public static String getBaseAdUrl () {
        if (IS_TEST) {
            return mBaseUrl1;
        } else {
            return mBaseUrl2;
        }
    }

    public static String getSdkVer () {
        return SDK_VERSION;
    }

    public static void setAllLog (boolean showHttp, boolean showLog, boolean showSdk) {
        IS_SHOW_HTTP_LOG = showHttp;
        IS_SHOW_LOG = showLog;
        IS_DEBUG = showSdk;
    }
}
