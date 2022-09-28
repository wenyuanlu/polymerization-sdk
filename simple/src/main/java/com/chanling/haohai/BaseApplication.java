package com.chanling.haohai;

import android.app.Application;

import com.corpize.sdk.mobads.QcAd;
import com.corpize.sdk.mobads.common.Constants;

/**
 * author: yh
 * date: 2019-08-08 13:43
 * description: TODO:
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate () {
        super.onCreate();
        //企创聚合sdk初始化
        String appid  = "CC79C9F5067C39165466819CCAAEB103";
        String secret;
        if (Constants.IS_DEBUG) {
            secret = "KGFJqJdjD6vZGFZB1FDay+M29NDpQ/wcFqv1viOqlcpjBLta1ZkSKZPeQAmdgsZVaFdFWjc7GgKU3x8PUF6sV5WNZk/DhFXpTiyvQUz2JdpDo1MKlV5PM434BD9OodGg0qOdkKLny0ro7NuzkUCBI2PvGv2T5acj79nyQUCt7DRiDLIVZpkqC/TfgHcEmeJGRZxXcStGjgWYpZ0Jpdt3JA==";
        }else {
            secret = "lfAnMZ4Ayw00h3fevH/3aMflfH3rKvSu7bQqIYlM6a9mNAxYOMmnlLlTfvfaOLJlLoJFD41dnsg/KPKQWV+MF0HtUAZUxzHw10My7snWfj00MI2EznCllH4kTAApkSxcwuFbdMwXeHrQAPi/1PoRK8ezn4mpefMHVU3GpI7FFg3C72eGVkvW2gveOoi9vvRjASUEZCQAxyjjGdi/f7R+gzRp8SInavr4YGjeV1lxDdM=";
        }
        QcAd.get().showLog(true,true,true);
        QcAd.get().init(this, appid, secret);
    }
}
