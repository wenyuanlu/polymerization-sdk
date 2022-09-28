package com.corpize.sdk.mobads.bean;

import java.util.List;

/**
 * @author TangHongChang
 * @version 1.0
 * @date 创建时间： 2018/4/26 上午11:00
 * @Description 落地页链接监测对象
 * @fileName AssetsLinkBean.java
 */
public class AssetsLinkBean {

    private String url;    //广告落地页 URL

    //三方点击监测链接 URL
    //必须在广告物料点击同时触发，禁止缓存或延 迟上报
    //点击坐标宏:[CLICK_X], [CLICK_Y] 媒体需对每条三方监测链接中可能存在的点 击坐标宏进行实时替换，若不支持获取点击坐 标需在企创平台媒体广告位设置中选择不支 持点击坐标项
    private List<String> clicktrackers;

    //替代落地页地址，如果设备调用失 败则使用 url 地址
    //使用本地址 (作为 deepLink 的 URL)。如果 deeplink 存在则优先调用，不存在或调用失败 则使用浏览器或 webview 打开 url 字段。
    private String fallback;

    //deeplink 调用成功上报
    //当 fallback 字段的 deeplink 链接在用户端调 用成功时由客户端发起上报
    private List<String> fallbacktrackers;


    //广告点击触发的行为 0 - 未确认 1 - App webview 打开链接 2 - 系统浏览器打开链接
    // 3 - 打开地图 4 - 拨打电话 5 - 播放视频 6- 下载APP 7 - deeplink 链接
    private int                         action;
    private AssetsLinkEventtrackersBean eventtrackers;  //用户端事件上报对象  转化上报
    private String                      dfn;  //APP 应用下载链接

    public String getUrl () {
        return url;
    }

    public void setUrl (String url) {
        this.url = url;
    }

    public List<String> getClicktrackers () {
        return clicktrackers;
    }

    public void setClicktrackers (List<String> clicktrackers) {
        this.clicktrackers = clicktrackers;
    }

    public String getFallback () {
        return fallback;
    }

    public void setFallback (String fallback) {
        this.fallback = fallback;
    }

    public List<String> getFallbacktrackers () {
        return fallbacktrackers;
    }

    public void setFallbacktrackers (List<String> fallbacktrackers) {
        this.fallbacktrackers = fallbacktrackers;
    }

    public int getAction () {
        return action;
    }

    public void setAction (int action) {
        this.action = action;
    }

    public AssetsLinkEventtrackersBean getEventtrackers () {
        return eventtrackers;
    }

    public void setEventtrackers (AssetsLinkEventtrackersBean eventtrackers) {
        this.eventtrackers = eventtrackers;
    }

    public String getDfn () {
        return dfn;
    }

    public void setDfn (String dfn) {
        this.dfn = dfn;
    }
}
