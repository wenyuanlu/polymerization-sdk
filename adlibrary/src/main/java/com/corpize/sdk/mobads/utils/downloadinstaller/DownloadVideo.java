package com.corpize.sdk.mobads.utils.downloadinstaller;

import android.app.Activity;
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.corpize.sdk.mobads.utils.LogUtils;
import com.corpize.sdk.mobads.utils.SpUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * App 下载升级管理器. 单线程稳定，多线程下载异常多！！！  30M 以内还是没有问题
 * 新的进程处理？app 杀了也没有关系
 * 主要下载视频文件
 */
public class DownloadVideo {
    private Activity mContext;
    private int      progress;
    private int      oldProgress;

    //新包的下载地址
    private String downloadApkUrl;
    private String downloadApkUrlMd5;
    private String storageApkPath;

    //事件监听器
    private DownloadProgressCallBack downloadProgressCallBack;

    //保存下载状态信息，临时过度的方案。
    public static ArrayMap<String, Integer> downLoadStatusMap = new ArrayMap<>();

    private String storagePrefix;

    private boolean isShowNotification;

    /**
     * 不需要下载进度回调的 同时不需要通知的
     *
     * @param context        上下文
     * @param downloadApkUrl apk 下载地址
     */
    public DownloadVideo (Activity context, String downloadApkUrl) {
        this(context, downloadApkUrl, false);
    }

    /**
     * 不需要下载进度回调的
     *
     * @param context            上下文
     * @param isShowNotification 是否展示通知
     * @param downloadApkUrl     apk 下载地址
     */
    public DownloadVideo (Activity context, String downloadApkUrl, boolean isShowNotification) {
        this(context, downloadApkUrl, isShowNotification, null);

    }

    /**
     * 需要下载进度回调的
     *
     * @param context            上下文
     * @param downloadApkUrl     apk下载地址
     * @param isShowNotification 是否展示通知
     * @param callBack           进度状态回调
     */
    public DownloadVideo (Activity context, String downloadApkUrl, boolean isShowNotification, DownloadProgressCallBack callBack) {
        this.mContext = context;
        this.downloadApkUrl = downloadApkUrl;
        this.downloadProgressCallBack = callBack;
        this.isShowNotification = isShowNotification;
    }

    /**
     * 获取16位的MD5 值，大写
     *
     * @param str
     * @return
     */
    private String getUpperMD5Str16 (String str) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            LogUtils.d("NoSuchAlgorithmException caught!");
            System.exit(-1);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[]       byteArray  = messageDigest.digest();
        StringBuffer md5StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            } else {
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
            }
        }
        return md5StrBuff.toString().toUpperCase().substring(8, 24);
    }

    /**
     * app下载升级管理
     */
    public void start () {

        String[] split = downloadApkUrl.split("/");
        downloadApkUrlMd5 = split[split.length - 1];

        //前缀要统一 一下 + AppUtils.getAppName(mContext)+"/Download/"
        storagePrefix = mContext.getExternalFilesDir("qc_ad_download").getAbsolutePath() + "/";
        //storagePrefix = Environment.getExternalStorageDirectory().getPath() + "/Download/" + AppUtils.getPackageName(mContext) + "/";
        storageApkPath = storagePrefix /*+ AppUtils.getAppName(mContext)*/ + downloadApkUrlMd5;

        //获取当前的app是否下载
        Integer downloadStatus = downLoadStatusMap.get(downloadApkUrlMd5);

        if (downloadStatus == null || downloadStatus == UpdateStatus.UN_DOWNLOAD
                || downloadStatus == UpdateStatus.DOWNLOAD_ERROR || downloadStatus == UpdateStatus.UNINSTALL) {
            //如果没有正在下载&&没有下载好了还没有安装
            new Thread(mDownApkRunnable).start();
        } else if (downloadStatus == UpdateStatus.DOWNLOADING) {
            LogUtils.d("下载中,请稍后");
        }
    }

    /**
     * 下载线程,使用最原始的HttpURLConnection，减少依赖
     * 大的APK下载还是比较慢的，后面改为多线程下载
     */
    private Runnable mDownApkRunnable = new Runnable() {
        @Override
        public void run () {
            downLoadStatusMap.put(downloadApkUrlMd5, UpdateStatus.DOWNLOADING);
            try {
                URL               url  = new URL(downloadApkUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int length = conn.getContentLength();

                File file = new File(storagePrefix);
                if (!file.exists()) {
                    file.mkdir();
                }

                File apkFile = new File(storageApkPath);

                //如果已经下载过了,无需再次下载
                if (apkFile.exists() && apkFile.length() == length) {
                    //已经下载过了，直接的progress ==100,然后去安装
                    progress = 100;
                    if (downloadProgressCallBack != null) {
                        downloadProgressCallBack.downloadProgress(progress);
                    }
                    downLoadStatusMap.put(downloadApkUrlMd5, UpdateStatus.UNINSTALL);
                    return;
                }

                LogUtils.d("开始下载广告");

                FileOutputStream fos   = new FileOutputStream(apkFile);
                int              count = 0;
                byte[]           buf   = new byte[2048];
                int              byteCount;

                InputStream is = conn.getInputStream();

                while ((byteCount = is.read(buf)) > 0) {
                    count += byteCount;
                    progress = (int) (((float) count / length) * 100);
                    if (progress > oldProgress) {
                        if (downloadProgressCallBack != null) {
                            downloadProgressCallBack.downloadProgress(progress);
                        }
                        oldProgress = progress;
                    }
                    fos.write(buf, 0, byteCount);
                }

                downLoadStatusMap.put(downloadApkUrlMd5, UpdateStatus.UNINSTALL);
                LogUtils.d("下载广告完成");
                SpUtils.saveInt(downloadApkUrlMd5, length);

                fos.flush();
                fos.close();
                is.close();

            } catch (Exception e) {
                e.printStackTrace();

                downLoadStatusMap.put(downloadApkUrlMd5, UpdateStatus.DOWNLOAD_ERROR);

                if (downloadProgressCallBack != null) {
                    downloadProgressCallBack.downloadException(e);
                }

                //后面有时间再完善异常的处理
                if (e instanceof FileNotFoundException) {
                    toastError("下载失败，不存在");
                } else if (e instanceof ConnectException) {
                    toastError("下载失败，无法访问网络");
                } else if (e instanceof UnknownHostException) {
                    toastError("下载失败，无法访问网络");
                } else if (e instanceof UnknownServiceException) {
                    toastError("下载失败，无法访问网络");
                } else if (e.toString().contains("Permission denied")) {
                    toastError("下载失败，无储存权限");
                } else {
                    toastError("下载失败，请稍后再重试");
                }

            } finally {
                //finally do something
            }
        }
    };


    /**
     * get String from id
     */
    @NonNull
    public String getStringFrom (@StringRes int id) {
        return mContext.getResources().getString(id);
    }

    /**
     * Toast error message
     */
    private void toastError (@StringRes int id) {
        LogUtils.e(getStringFrom(id));
    }

    /**
     * Toast error message
     */
    private void toastError (String text) {
        LogUtils.e(text);
    }

}
