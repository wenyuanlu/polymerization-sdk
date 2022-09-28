package com.corpize.sdk.mobads.http.callback;


import java.io.IOException;
import java.lang.reflect.ParameterizedType;


/**
 * author ：yh
 * date : 2019-11-28 17:36
 * description : 返回数据转换成Json
 */
public abstract class JsonCallback<T> extends BaseCallback<T> {
    IJsonSerializator mJsonSerializator;

    public JsonCallback (IJsonSerializator serializator) {
        mJsonSerializator = serializator;
    }

    @Override
    public T parseNetworkResponse (String response) throws IOException {
        Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        if (entityClass == String.class) {
            return (T) response;
        }
        T bean = mJsonSerializator.transform(response, entityClass);
        return bean;
    }

}
