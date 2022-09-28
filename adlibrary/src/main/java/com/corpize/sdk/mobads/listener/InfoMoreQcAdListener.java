package com.corpize.sdk.mobads.listener;

import android.view.View;

import com.corpize.sdk.mobads.admanager.InfoThreeManager;

import java.util.List;

/**
 * author: yh
 * date: 2020-02-21 21:21
 * description: 多图信息流广告的回调
 */
public interface InfoMoreQcAdListener extends QCADListener {
    void onADManager (InfoThreeManager manager);//返回管理类

    void onAdViewSuccess (List<View> adViews, String tag);

    void onAdExposure (String tag);

    void onAdClicked (View view, String tag);

    void onAdClose (View view, String tag);

}
