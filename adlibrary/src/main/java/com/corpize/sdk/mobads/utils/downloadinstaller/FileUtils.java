package com.corpize.sdk.mobads.utils.downloadinstaller;

import android.app.Application;
import android.content.Context;

import com.corpize.sdk.mobads.common.CommonUtils;
import com.corpize.sdk.mobads.utils.SpUtils;

import java.io.File;

/**
 * author: yh
 * date: 2019-08-19 18:02
 * description: 获取文件相关信息
 */
public class FileUtils {

    private static Application mApplication = CommonUtils.get();

    /**
     * 手机系统版本
     */
    public static Application getApplication () {
        if (mApplication == null) {
            mApplication = CommonUtils.get();
        }
        return mApplication;
    }

    /**
     * 网络文件,本地是否存在
     */
    public static String urlFileExist (Context context, final String downUrl) {
        try {

            //拆分url,拼接文件的位置
            String[]     split          = downUrl.split("/");
            String       downloadUrlMd5 = split[split.length - 1];
            String       storagePrefix  = context.getExternalFilesDir("qc_ad_download").getAbsolutePath() + "/";
            final String storageApkPath = storagePrefix /*+ AppUtils.getAppName(context)*/ + downloadUrlMd5;

            File file = new File(storagePrefix);
            if (!file.exists()) {
                //return false;
                return downUrl;
            }

            final File apkFile = new File(storageApkPath);
            if (!apkFile.exists()) {
                //return false;
                return downUrl;
            } else {
                int length = SpUtils.getInt(downloadUrlMd5);
                if (apkFile.length() == length) {
                    //return true;
                    return storageApkPath;
                } else {
                    //return false;
                    return downUrl;
                }
            }

        } catch (Exception e) {
            //return false;
            return downUrl;
        }

    }


}
