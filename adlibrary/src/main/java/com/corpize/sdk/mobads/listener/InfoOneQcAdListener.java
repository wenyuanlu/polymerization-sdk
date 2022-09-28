package com.corpize.sdk.mobads.listener;

import android.view.View;

import com.corpize.sdk.mobads.admanager.InfoOneManager;

import java.util.List;

/**
 * author: yh
 * date: 2020-02-21 21:21
 * description: Banner广告的回调
 */
public interface InfoOneQcAdListener extends QCADListener {
    void onADManager (InfoOneManager manager);//返回管理类

    void onAdViewSuccess (List<View> adViews, float w, float h, String tag);

    void onAdExposure (String tag);

    void onAdClicked (View view, String tag);

    void onAdClose (View view, String tag);

}
