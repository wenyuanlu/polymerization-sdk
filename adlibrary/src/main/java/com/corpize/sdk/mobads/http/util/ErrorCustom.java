package com.corpize.sdk.mobads.http.util;


import com.corpize.sdk.mobads.common.CommonUtils;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

/**
 * author ：yh
 * date : 2019-08-07 16:06
 * description : 自定义的 Error实现,需要根据不同服务端的请求返回
 */
public class ErrorCustom {

    public static Exception checkError (Exception error) {
        error.printStackTrace();
        if (!OkNetUtil.isConnected(CommonUtils.get())) {      //检测网络
            return new Exception("网络可不用");

        } else if (error instanceof ApiException) {             //自定义的返回错误
            int apiCode = ((ApiException) error).getCode();
            if (apiCode == ApiException.ERROR_NO_AD) {
                return new Exception("无广告");

            } else if (apiCode == ApiException.ERROR_MISS_PARA) {
                return new Exception("请求缺少必填项，请自行检查");

            } else if (apiCode == ApiException.ERROR_NO_MATCH) {
                return new Exception("请求中广告位类型与平台申请所填不符");

            } else if (apiCode == ApiException.ERROR_NO_ID) {
                return new Exception("设备号非法");

            } else if (apiCode == ApiException.ERROR_NO_FIND_ADID) {
                return new Exception("广告位ID未找到");

            } else if (apiCode == ApiException.ERROR_MISS_DEVICEID) {
                return new Exception("缺少IMEI");

            } else {
                return error;
            }

        } else if (error instanceof SocketTimeoutException) {   // 通信超时错误
            return new Exception("连接超时，请稍后再试");

        } else if (error instanceof ConnectException) {         // 连接服务器失败错误
            return new Exception("网络不稳定，请稍后再试");

        } else {                                                // 其他错误
            return error;
        }

    }
}
