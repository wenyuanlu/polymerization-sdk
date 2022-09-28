package com.corpize.sdk.mobads.video;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * author: yh
 * date: 2019-09-12 09:23
 * description: 包识别管理,查看当前手机是否有对应包名的app
 */
public class PackageUtils {

    /**
     * description:国外热门App
     **/
    public static final String INSTAGRAM  = "com.instagram.android";
    public static final String FACE_BOOK  = "com.facebook.katana";
    public static final String MESSENGER  = "com.facebook.orca";
    public static final String WHATS_APP  = "com.whatsapp";
    public static final String GMAIL      = "com.google.android.gm";
    public static final String GOOGLE_MAP = "com.google.android.apps.maps";
    public static final String ALLO       = "com.google.android.apps.fireball";

    /**
     * description:国内热门App
     **/
    public static final String MEITUAN_WAIMAI    = "com.sankuai.meituan.takeoutnew";    //美团外卖
    public static final String E_LE_ME           = "me.ele";                            //饿了么
    public static final String MO_BAI            = "com.mobike.mobikeapp";              //摩拜单车
    public static final String OFO               = "so.ofo.labofo";                     //OFO
    public static final String JIN_RI_TOU_TIAO   = "com.ss.android.article.news";       //今日头条
    public static final String SINA_WEI_BO       = "com.sina.weibo";                    //新浪微博
    public static final String WANG_YI_XIN_WEN   = "com.netease.newsreader.activity";   //网易新闻
    public static final String ZHI_HU            = "com.zhihu.android";                 //知乎
    public static final String KUAI_SHOU         = "com.smile.gifmaker";                //快手
    public static final String HU_YA_ZHI_BO      = "com.duowan.kiwi";                   //虎牙直播
    public static final String YING_KE_ZHI_BO    = "com.meelive.ingkee";                //映客直播
    public static final String MIAO_PAI          = "com.yixia.videoeditor";             //秒拍
    public static final String MEI_TU_XIU_XIU    = "com.mt.mtxx.mtxx";                  //美图秀秀
    public static final String MEI_YAN_XIANG_JI  = "com.meitu.meiyancamera";            //美颜相机
    public static final String XIE_CHENG         = "ctrip.android.view";                //携程
    public static final String MO_MO             = "com.immomo.momo";                   //陌陌
    public static final String YOU_KU            = "com.youku.phone";                   //优酷
    public static final String AI_QI_YI          = "com.qiyi.video";                    //爱奇艺
    public static final String DI_DI             = "com.sdu.didi.psnger";               //滴滴出行
    public static final String ZHI_FU_BAO        = "com.eg.android.AlipayGphone";       //支付宝
    public static final String TAO_BAO           = "com.taobao.taobao";                 //淘宝
    public static final String JING_DONG         = "com.jingdong.app.mall";             //京东
    public static final String DA_ZONG_DIAN_PING = "com.dianping.v1";                   //大众点评
    public static final String JIAN_SHU          = "com.jianshu.haruki";                //搜狗输入法
    public static final String BAI_DU_DI_TU      = "com.baidu.BaiduMap";                //百度地图
    public static final String GAO_DE_DI_TU      = "com.autonavi.minimap";              //高德地图
    public static final String WEI_XIN           = "com.tencent.mm";                    //微信
    public static final String QQ                = "com.tencent.mobileqq";              //QQ
    public static final String WANG_YI_YUN_MUSIC = "com.netease.cloudmusic";            //网易云音乐
    public static final String DING_DING         = "com.alibaba.android.rimet";         //钉钉

    /**
     * 对应包名的App是否安装
     *
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isAppInstalled (Context context, String packageName) {
        PackageManager pm        = context.getPackageManager();
        boolean        installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

}
