package com.corpize.sdk.mobads.http.callback;

import com.corpize.sdk.mobads.utils.GsonUtil;

/**
 * author ：yh
 * date : 2019-11-28 19:09
 * description :
 */
public class JsonSerializator implements IJsonSerializator {

    @Override
    public <T> T transform (String bean, Class<T> classOfT) {
        return GsonUtil.GsonToBean(bean, classOfT);
    }
}
