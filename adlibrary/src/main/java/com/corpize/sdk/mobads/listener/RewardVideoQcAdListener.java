package com.corpize.sdk.mobads.listener;

import com.corpize.sdk.mobads.admanager.RewardVideoManager;

/**
 * author: yh
 * date: 2020-02-21 21:21
 * description: 激励视频广告的回调
 */
public interface RewardVideoQcAdListener {
    void onADManager (RewardVideoManager manager);//返回管理类

    void onADReceive (RewardVideoManager manager, String tag);//返回管理类

    void onADExposure (String tag);

    void onAdClose ();

    void onAdCompletion ();

    void onAdClicked (String tag);

    void onAdError (String tag, String fail);//出错,无广告时

}
