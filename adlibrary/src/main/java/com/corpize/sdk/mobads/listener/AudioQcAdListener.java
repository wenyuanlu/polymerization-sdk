package com.corpize.sdk.mobads.listener;

import com.corpize.sdk.mobads.admanager.AudioAdManager;

/**
 * author: yh
 * date: 2020-02-21 21:21
 * description: Banner广告的回调
 */
public interface AudioQcAdListener {
    void onADManager (AudioAdManager manager);//返回管理类

    void onADReceive (AudioAdManager manager);//返回管理类

    void onADExposure ();

    void onAdCompletion ();

    void onAdError (String fail);//出错,无广告时

}
