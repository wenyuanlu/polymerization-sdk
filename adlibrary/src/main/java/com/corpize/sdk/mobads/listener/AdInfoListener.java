package com.corpize.sdk.mobads.listener;

import com.corpize.sdk.mobads.bean.AdidBean;

/**
 * author : xpSun
 * date : 6/22/21
 * description :
 */
public interface AdInfoListener {

    void onSuccess(AdidBean adidBean);

    void onAdError (String tag, String fail);//出错,无广告时

}
