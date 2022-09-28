package com.corpize.sdk.mobads.bean;

import java.util.List;

/**
 * author ：yh
 * date : 2020-02-16 03:21
 * description : 原生广告物料
 */
public class NativeBean {

    private List<AssetsBean> assets;        //原生广告物料对象
    private AssetsLinkBean   link;          //落地页链接监测对象
    private List<String>     imptrackers;   //广告曝光监测 URL

    public List<AssetsBean> getAssets () {
        return assets;
    }

    public void setAssets (List<AssetsBean> assets) {
        this.assets = assets;
    }

    public AssetsLinkBean getLink () {
        return link;
    }

    public void setLink (AssetsLinkBean link) {
        this.link = link;
    }

    public List<String> getImptrackers () {
        return imptrackers;
    }

    public void setImptrackers (List<String> imptrackers) {
        this.imptrackers = imptrackers;
    }
}
