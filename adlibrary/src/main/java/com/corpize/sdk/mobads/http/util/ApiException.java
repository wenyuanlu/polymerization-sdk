package com.corpize.sdk.mobads.http.util;

import android.text.TextUtils;

/**
 * 错误码类，不同错误码展示不同的返回
 */
public class ApiException extends RuntimeException {

    public static final int    ERROR_NO_AD         = 204;//无广告
    public static final int    ERROR_MISS_PARA     = 400;//请求缺少必填项，请自行检查
    public static final int    ERROR_NO_MATCH      = 401;//请求中广告位类型与平台申请所填不符
    public static final int    ERROR_NO_ID         = 402;//设备号非法
    public static final int    ERROR_NO_FIND_ADID  = 404;//广告位ID未找到
    public static final int    ERROR_MISS_DEVICEID = 405;//缺少设备ID，例如安卓缺少IMEI或iOS缺少IDFA
    private             String mMessage;
    private             int    mCode               = -1;

    public ApiException (String message) {
        mMessage = message;
    }

    public ApiException (String message, int code) {
        mMessage = message;
        mCode = code;
    }

    @Override
    public String getMessage () {
        return mMessage;
    }

    public int getCode () {
        return mCode;
    }

    public void setCode (int code) {
        mCode = code;
    }

    /**
     * 需要根据错误码对错误信息进行一个转换，在显示给用户
     */
    private String getApiExceptionMessage (int code) {
        if (code == ERROR_NO_AD) {
            mMessage = "无广告";
        } else if (code == ERROR_MISS_PARA) {
            mMessage = "请求缺少必填项，请自行检查";
        } else if (code == ERROR_NO_MATCH) {
            mMessage = "请求中广告位类型与平台申请所填不符";
        } else if (code == ERROR_NO_ID) {
            mMessage = "设备号非法";
        } else if (code == ERROR_NO_FIND_ADID) {
            mMessage = "广告位ID未找到";
        } else if (code == ERROR_MISS_DEVICEID) {
            mMessage = "缺少设备ID，例如安卓缺少IMEI或iOS缺少IDFA";
        } else {
            if (TextUtils.isEmpty(mMessage)) {
                mMessage = "其他错误";
            }
        }
        return mMessage;
    }
}
