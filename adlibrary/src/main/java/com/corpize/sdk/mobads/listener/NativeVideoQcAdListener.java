package com.corpize.sdk.mobads.listener;

import com.corpize.sdk.mobads.admanager.NativeVideoManager;

/**
 * author: yh
 * date: 2020-02-21 21:21
 * description: Banner广告的回调
 */
public interface NativeVideoQcAdListener {
    void onADManager (NativeVideoManager manager);//返回管理类

    void onADReceive (NativeVideoManager manager);//返回管理类

    void onADExposure ();

    void onAdCompletion ();

    void onAdClicked ();

    void onAdError (String fail);//出错,无广告时

}
