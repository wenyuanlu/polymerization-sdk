package com.corpize.sdk.mobads.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.corpize.sdk.mobads.bean.AdmJsonBean;

import java.lang.reflect.Field;
import java.util.List;

/**
 *
 *  @date 创建时间： 2018/5/10 下午4:49
 *  @author TangHongChang
 *  @Description String转Json的工具类
 *  @fileName StringToJsonUtils.java
 *  @version  1.0
 *
 */

public class StringToJsonUtils {

    /**
     * 从JsonStr中解析BUserBase
     * @param jsonStr
     * @return
     */
    public static AdmJsonBean decodeFromJson(String jsonStr) {

        AdmJsonBean baseInfo;
        if(TextUtils.isEmpty(jsonStr)){
            baseInfo = new AdmJsonBean();
        }
        Gson gson = new Gson();
        baseInfo = gson.fromJson(jsonStr, AdmJsonBean.class);
        return baseInfo;
    }

    /**
     * 将BUserBase转换为JsonStr
     * @param userBaseInfo
     * @return
     */
    public static String encodeToJson(String userBaseInfo){
        Gson   gson    = new Gson();
        String jsonStr = gson.toJson(userBaseInfo);
        return jsonStr;
    }

    /**
     * Object To json String
     *
     * @param obj
     *
     * @return json String
     */
    public static String objToJsonString(Object obj) {

        // 初始化返回值
        String json = "str_empty";

        if (obj == null) {
            return json;
        }

        StringBuilder buff   = new StringBuilder();
        Field[]       fields = obj.getClass().getFields();
        try {
            buff.append("[");
            buff.append("{");
            int i = 0;
            for (Field field : fields) {
                if (i != 0) {
                    buff.append(",");
                }
                buff.append(field.getName());
                buff.append(":");
                buff.append("\"");
                buff.append(field.get(obj) == null ? "" : field.get(obj));
                buff.append("\"");
                i++;
            }
            buff.append("}");
            buff.append("]");
            json = buff.toString();
        } catch (Exception e) {
            throw new RuntimeException("cause:" + e.toString());
        }
        return json;
    }

    public static String listToString(List ss) {
        StringBuffer s = new StringBuffer("");
        if (null != ss) {
            String[] str = new String[ss.size()];
            for (int i=0; i<ss.size(); i++){
                str[i] = ss.get(i).toString();
            }
            arrayToString(str);
            s.append(arrayToString(str));
        }
        return s.toString();
    }

    /**
     * 把数组转换成'',格式的字符串输出
     * @param ss
     * @return
     */
    public static String arrayToString(String[] ss){
        StringBuffer s = new StringBuffer("");
        if(null != ss){
            for(int i=0;i<ss.length-1;i++){
                s.append("'")
                        .append(ss[i])
                        .append("'")
                        .append(",");
            }
            if(ss.length>0){
                s.append("'").append(ss[ss.length-1]).append("'");
            }
        }
        return s.toString();
    }
    /**
     *  Convert an array of strings to one string.
     *  Put the 'separator' string between each element.
     * @param a
     * @param separator
     * @return
     */
    public static String arrayToString(String[] a, String separator) {
        StringBuffer result = new StringBuffer();
        if(a==null){
            return "";
        }
        if (a.length > 0) {
            result.append(a[0]);
            for (int i=1; i<a.length; i++) {
                result.append(separator);
                result.append(a[i]);
            }
        }
        return result.toString();
    }

}
