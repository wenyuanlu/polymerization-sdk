package com.corpize.sdk.mobads.utils.downloadinstaller;

/**
 * author ：yh
 * date : 2019-12-23 10:47
 * description :下载状态   未下载，下载中，下载完成，下载失败，待安装
 * 下载好了，有可能包有问题。所以假如安装失败要处理一下
 */
public interface UpdateStatus {
    public final static int UN_DOWNLOAD    = -1;//未下载
    public final static int DOWNLOADING    = 0; //下载中
    public final static int DOWNLOAD_ERROR = 1; //下载失败
    public final static int UNINSTALL      = 2; //待安装
}
