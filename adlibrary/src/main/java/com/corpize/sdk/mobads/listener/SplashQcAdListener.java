package com.corpize.sdk.mobads.listener;

import com.corpize.sdk.mobads.admanager.SplashManager;

/**
 * author: yh
 * date: 2020-02-21 21:21
 * description: Banner广告的回调
 */
public interface SplashQcAdListener extends QCADListener {
    void onADManager (SplashManager manager);//返回管理类

    //void onADReceive (InsertManager manager, String tag);//返回的管理类

//    void onAdClose ();//关闭按钮 关闭

    void onADAdd (String tag);//页面加载时

    void onADExposure (String tag);

    void onADDismissed ();
    
}
