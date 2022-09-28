package com.corpize.sdk.mobads.listener;

import com.corpize.sdk.mobads.admanager.InsertManager;

/**
 * author: yh
 * date: 2020-02-21 21:21
 * description: Banner广告的回调
 */
public interface InsertQcAdListener extends QCADListener {
    void onADManager (InsertManager manager);//返回管理类

    void onADReceive (InsertManager manager, String tag);//返回的管理类

    void onADExposure (String tag);//曝光

    void onAdClose ();//关闭按钮 关闭
}
