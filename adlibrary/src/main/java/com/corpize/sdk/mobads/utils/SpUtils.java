package com.corpize.sdk.mobads.utils;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.corpize.sdk.mobads.common.CommonUtils;


/**
 * author ：yh
 * date : 2019-11-29 14:20
 * description :
 */
public class SpUtils {

    private static final String            SP_NAME  = "qc_config";//文件名
    private static       SharedPreferences mSp;
    private static       Application       mContext = CommonUtils.get();

    /**
     * 保存String的方法
     *
     * @param key
     * @param value
     */
    public static void saveString (String key, String value) {
        if (mSp == null) {
            mSp = mContext.getSharedPreferences(SP_NAME, mContext.MODE_PRIVATE);
        }
        Editor edit = mSp.edit();
        edit.putString(key, value);
        edit.commit();
    }


    /**
     * 取出的String
     *
     * @param key
     * @return
     */
    public static String getString (String key) {
        if (mSp == null) {
            mSp = mContext.getSharedPreferences(SP_NAME, mContext.MODE_PRIVATE);
        }
        String result = mSp.getString(key, null);
        return result;
    }

    /**
     * 存入的boolean
     *
     * @param key
     * @param value
     */
    public static void saveBoolean (String key, boolean value) {
        if (mSp == null) {
            mSp = mContext.getSharedPreferences(SP_NAME, mContext.MODE_PRIVATE);
        }
        Editor edit = mSp.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    /**
     * 取出的boolean
     *
     * @param key
     */
    public static Boolean getBoolean (String key) {
        if (mSp == null) {
            mSp = mContext.getSharedPreferences(SP_NAME, mContext.MODE_PRIVATE);
        }
        boolean result = mSp.getBoolean(key, false);
        return result;
    }

    /**
     * 存入的int
     *
     * @param key
     * @param value
     */
    public static void saveInt (String key, int value) {
        if (mSp == null) {
            mSp = mContext.getSharedPreferences(SP_NAME, mContext.MODE_PRIVATE);
        }
        Editor edit = mSp.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    /**
     * 取出的int
     *
     * @param key
     */
    public static int getInt (String key) {
        if (mSp == null) {
            mSp = mContext.getSharedPreferences(SP_NAME, mContext.MODE_PRIVATE);
        }
        int result = mSp.getInt(key, 0);
        return result;
    }

    /**
     * 存入的long
     *
     * @param key
     * @param value
     */
    public static void saveLong (String key, long value) {
        if (mSp == null) {
            mSp = mContext.getSharedPreferences(SP_NAME, mContext.MODE_PRIVATE);
        }
        Editor edit = mSp.edit();
        edit.putLong(key, value);
        edit.commit();
    }

    /**
     * 取出的long
     *
     * @param key
     */
    public static long getLong (String key) {
        if (mSp == null) {
            mSp = mContext.getSharedPreferences(SP_NAME, mContext.MODE_PRIVATE);
        }
        long result = mSp.getLong(key, 0);
        return result;
    }

}
