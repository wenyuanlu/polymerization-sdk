package com.corpize.sdk.mobads.bean;

import java.util.List;

/**
 * @author TangHongChang
 * @version 1.0
 * @date 创建时间： 2018/4/26 上午11:13
 * @Description 用户端事件上报对象  转化上报
 * @fileName AssetsLinkEventtrackersBean.java
 */

public class AssetsLinkEventtrackersBean {

    private String       iconurl;  //APP 的 icon 图标 只针对下载类广告
    private List<String> startdownload;    //当广告 APP 开始下载时上报 只针对下载类广告
    private List<String> completedownload;   //当广告 APP 下载完成时上报  只针对下载类广告
    private List<String> startinstall;    //当广告 APP 开始安装时上报 只针对下载类广告
    private List<String> completeinstall;  //当广告 APP 安装完成时上报 只针对下载类广告
    private List<String> startvideo; //当视频开始播放时上报 只针对视频类型广告
    private List<String> completevideo;   //当视频播放完成时上报 只针对视频类型广告

    public String getIconurl () {
        return iconurl;
    }

    public void setIconurl (String iconurl) {
        this.iconurl = iconurl;
    }

    public List<String> getStartdownload () {
        return startdownload;
    }

    public void setStartdownload (List<String> startdownload) {
        this.startdownload = startdownload;
    }

    public List<String> getCompletedownload () {
        return completedownload;
    }

    public void setCompletedownload (List<String> completedownload) {
        this.completedownload = completedownload;
    }

    public List<String> getStartinstall () {
        return startinstall;
    }

    public void setStartinstall (List<String> startinstall) {
        this.startinstall = startinstall;
    }

    public List<String> getCompleteinstall () {
        return completeinstall;
    }

    public void setCompleteinstall (List<String> completeinstall) {
        this.completeinstall = completeinstall;
    }

    public List<String> getStartvideo () {
        return startvideo;
    }

    public void setStartvideo (List<String> startvideo) {
        this.startvideo = startvideo;
    }

    public List<String> getCompletevideo () {
        return completevideo;
    }

    public void setCompletevideo (List<String> completevideo) {
        this.completevideo = completevideo;
    }
}
