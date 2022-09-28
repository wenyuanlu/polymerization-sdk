package com.corpize.sdk.mobads.http.callback;

/**
 * author ：yh
 * date : 2019-11-28 16:27
 * description :返回String类型的数据
 */
public abstract class StringCallback extends BaseCallback<String> {
    @Override
    public String parseNetworkResponse (String response) {
        return response;
    }

}
