package com.corpize.sdk.mobads.listener;

/**
 * author: yh
 * date: 2020-02-21 21:21
 * description: 回调的基类
 */
public interface QCADListener {
    void onAdClicked (String tag);

    void onAdError (String tag, String fail);//出错,无广告时
}
