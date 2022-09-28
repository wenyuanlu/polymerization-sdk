package com.corpize.sdk.mobads.video;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

/**
 * author: yh
 * date: 2019-09-12 09:33
 * description: 第三方App的管理
 */
public class ThirdAppUtils {

    /**
     * 通过 deep linking 打开第三方apk
     */
    public static boolean openLinkApp (Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;//打不开,直接报错
        }
    }

    /**
     * 启动第三方apk,推荐方法
     * 如果已经启动apk，则直接将apk从后台调到前台运行，如果未启动apk，则重新启动
     */
    public static boolean openApp (Context context, String packageName) {
        Intent intent = getAppOpenIntentByPackageName(context, packageName);
        if (intent != null) {
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    /**
     * 启动第三方apk
     * 直接打开  每次都会启动到启动界面，每次都会干掉之前的，从新启动
     */
    public static boolean openApps (Context context, String packageName) {
        if (PackageUtils.isAppInstalled(context, packageName)) {
            PackageManager packageManager = context.getPackageManager();
            Intent         intent         = packageManager.getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            }
        }
        return false;
    }

    /**
     * 启动第三方apk
     * 内嵌在当前apk内打开，每次启动都是新的apk,你会发现打开了两个apk
     */
    public static void openAppIn (Context context, String packagename) {
        PackageInfo pi = null;
        try {
            pi = context.getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(pi.packageName);
        List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(resolveIntent, 0);
        ResolveInfo       ri   = apps.iterator().next();
        if (ri != null) {
            String packageName = ri.activityInfo.packageName;
            String className   = ri.activityInfo.name;
            Intent intent      = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            context.startActivity(intent);
        }
    }

    /**
     * @param context
     * @param packageName
     * @return
     */
    public static Intent getAppOpenIntentByPackageName (Context context, String packageName) {
        try {
            String         mainAct = null;
            PackageManager pkgMag  = context.getPackageManager();
            Intent         intent  = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

            @SuppressLint ("WrongConstant")
            List<ResolveInfo> list = pkgMag.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
            for (int i = 0; i < list.size(); i++) {
                ResolveInfo info = list.get(i);
                if (info.activityInfo.packageName.equals(packageName)) {
                    mainAct = info.activityInfo.name;
                    break;
                }
            }
            if (TextUtils.isEmpty(mainAct)) {
                return null;
            }
            intent.setComponent(new ComponentName(packageName, mainAct));
            return intent;

        } catch (Exception e) {
            return null;
        }

    }


    //打开支付宝
    public static boolean openAlipayApp (Context context) {
        if (PackageUtils.isAppInstalled(context, PackageUtils.ZHI_FU_BAO)) {
            Intent intent = new Intent();
            intent.setAction("Android.intent.action.VIEW");
            intent.setClassName(context, PackageUtils.ZHI_FU_BAO);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    //打开微信
    public static boolean openWeichatApp (Context context) {
        if (PackageUtils.isAppInstalled(context, PackageUtils.WEI_XIN)) {
            Intent intent = new Intent();
            intent.setAction("Android.intent.action.VIEW");
            intent.setClassName(context, PackageUtils.WEI_XIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    //打开QQ
    public static boolean openQQApp (Context context) {
        if (PackageUtils.isAppInstalled(context, PackageUtils.QQ)) {
            Intent intent = new Intent();
            intent.setAction("Android.intent.action.VIEW");
            intent.setClassName(context, PackageUtils.QQ);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        }
        return false;
    }


}
