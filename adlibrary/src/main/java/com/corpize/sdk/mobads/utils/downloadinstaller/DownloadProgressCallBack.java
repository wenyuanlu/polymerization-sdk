package com.corpize.sdk.mobads.utils.downloadinstaller;

/**
 * author ：yh
 * date : 2019-12-23 10:47
 * description :下载进度回调
 */
public interface DownloadProgressCallBack {
     void downloadProgress (int progress);
     void downloadException (Exception e);
     void onInstallStart ();
}
