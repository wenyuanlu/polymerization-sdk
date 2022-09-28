package com.corpize.sdk.mobads;

import android.app.Application;
import android.text.TextUtils;

import com.baidu.mobads.sdk.api.BDAdConfig;
import com.corpize.sdk.mobads.bean.AdSdkBean;
import com.corpize.sdk.mobads.common.Constants;
import com.corpize.sdk.mobads.utils.AESUtil;
import com.corpize.sdk.mobads.utils.GsonUtil;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.SdkConfig;
import com.qq.e.comm.managers.GDTADManager;

/**
 * author: yh
 * date: 2020-02-11 10:34
 * description: 初始化辅助类
 */
public class QcAdManager {

    public static void initSdk (Application application, String appid, String secret) {
        String    decrypt   = AESUtil.decrypt(secret, appid);
        AdSdkBean adSdkBean = GsonUtil.GsonToBean(decrypt.trim(), AdSdkBean.class);
        if (adSdkBean != null) {
            AdSdkBean.OceanengineBean oceanengine = adSdkBean.getOceanengine();
            if (oceanengine != null) {
                String csjAppid = oceanengine.getAppid();
                if (!TextUtils.isEmpty(csjAppid)) {
                    TTAdManagerHolder.init(application,
                            Constants.IS_INIT_SIMPLE_DATA ?
                                    Constants.CSJSimpleData.APP_ID : csjAppid);//穿山甲注册
                }
            }

            AdSdkBean.BaiduBean baidu = adSdkBean.getBaidu();
            if (baidu != null) {
                String bqtAppid = baidu.getAppid();
                if (!TextUtils.isEmpty(bqtAppid)) {
                    BDAdConfig bdAdConfig = new BDAdConfig.Builder()
                            // 2、应用在mssp平台申请到的appsid，和包名一一对应，此处设置等同于在AndroidManifest.xml里面设置
                            .setAppsid(Constants.IS_INIT_SIMPLE_DATA ?
                                    Constants.BaiduSimpleData.APP_ID : bqtAppid)
                            .build(application);
                    bdAdConfig.init();
                }
            }

            AdSdkBean.QqBean qq = adSdkBean.getQq();
            if (qq != null) {
                String qqAppid = qq.getAppid();
                if (!TextUtils.isEmpty(qqAppid)) {
                    GDTADManager.getInstance().initWith(
                            application,
                            Constants.IS_INIT_SIMPLE_DATA ? Constants.GDTSimpleData.APP_ID : qqAppid);
                }
            }

            AdSdkBean.KSBean ks = adSdkBean.getKs();
            if (ks != null) {
                String ksAppId = ks.getAppid();
                if (!TextUtils.isEmpty(ksAppId)) {
                    initKsSdk(
                            application,
                            Constants.IS_INIT_SIMPLE_DATA ? Constants.KSSimpleData.KS_APP_ID : ksAppId);
                }
            }
        }
    }

    /**
     * 初始化快手sdk
     *
     * @param appContext
     */
    private static void initKsSdk (
            Application appContext,
            String appId) {
        KsAdSDK.init(appContext, new SdkConfig.Builder()
                .appId(appId) // 测试appId，请联系快手平台申请正式AppId，必填
                .appName("聚合sdk") // 测试appName，请填写您应用的名称，非必填
                // Feed和入口组件，夜间模式样式配置，如果不配置 默认是"ks_adsdk_night_styles.xml"
                .nightThemeStyleAssetsFileName("ks_adsdk_night_styles.xml")
                .showNotification(true) // 是否展示下载通知栏
                .debug(true)
                .build());
    }
}
