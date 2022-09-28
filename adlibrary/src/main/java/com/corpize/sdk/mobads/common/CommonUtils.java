package com.corpize.sdk.mobads.common;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import com.corpize.sdk.mobads.utils.LogUtils;

import java.util.Calendar;
import java.util.Random;

/**
 * author ：yh
 * date : 2019-08-05 09:55
 * description : 公用类
 */
public class CommonUtils {

    private static final Application INSTANCE;

    //获取系统音量(媒体音乐)
    public static int getSystemAudioVolume (Context context) {
        AudioManager audioManager  = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int          currentVolume = 0;
        if (audioManager != null) {
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        } else {
            currentVolume = -1;
        }
        return currentVolume;
    }

    //获取全局的application
    public static Application get () {
        return INSTANCE;
    }

    static {
        Application app = null;
        try {
            app = (Application) Class.forName("android.app.AppGlobals").getMethod("getInitialApplication").invoke(null);
            if (app == null)
                //throw new IllegalStateException("Static initialization of Applications must be on main thread.");
                Log.e("QCAD","Static initialization of Applications must be on main thread.");
        } catch (final Exception e) {
            e.printStackTrace();
            try {
                app = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null);
            } catch (final Exception ex) {
                e.printStackTrace();
            }
        } finally {
            INSTANCE = app;
        }
    }

    /**
     * 根据两个秒数 获取两个时间差
     */
    public static long getDateDistance (long lastDate, long nowDate) {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        long ns = 1000;
        // 获得两个时间的秒时间差异
        long diff = nowDate - lastDate;
        // 计算差多少秒
        long sec = diff / ns;
        //输出结果
        return sec;
        /*// 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        long   sec = diff % nd % nh % nm / ns;
        String res = "";
        if (day != 0) {
            res += day + "天";
        }
        if (hour != 0) {
            res += "  " + hour + ":" + min + ":" + sec;
        }
        return res;*/
    }

    /**
     * 返回1到100的随机数
     */
    public static int getCompareRandow () {
        int    maxInt = 100;
        Random random = new Random();
        int    target = random.nextInt(maxInt) + 1;    //1到100的任意整数
        return target;

    }

    /**
     * 返回1到weight的随机数
     */
    public static int getCompareRandow (int weight) {
        if (weight != 0) {
            Calendar calendar = Calendar.getInstance();
            Random random = new Random(calendar.getTime().getTime());
            int    target = random.nextInt(weight) + 1;    //1到weigth的任意整数
            return target;
        } else {
            return 1;
        }

    }

}
