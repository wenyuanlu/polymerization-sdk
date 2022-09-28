package com.corpize.sdk.mobads.listener;

import android.view.View;

import com.corpize.sdk.mobads.admanager.BannerManager;

/**
 * author: yh
 * date: 2020-02-21 21:21
 * description: Banner广告的回调
 */
public interface BannerQcAdListener extends QCADListener {
    void onADManager (BannerManager manager);//返回管理类

    //void onADReceive (View view, String tag);//返回的view

    //void onAdSelect (String reason);//选择不喜欢的回调

    void onADExposure (String tag);//曝光

    void onAdClose ();//关闭按钮 关闭
}
