package com.corpize.sdk.mobads.http.callback;

/**
 * author ：yh
 * date : 2019-11-28 13:32
 * description : 自定义的返回处理的基类
 */
public abstract class BaseCallback<T> {

    public abstract T parseNetworkResponse (String response) throws Exception;

    public abstract void onResponse (T response);

    public abstract void onError (int code, Exception e);

}