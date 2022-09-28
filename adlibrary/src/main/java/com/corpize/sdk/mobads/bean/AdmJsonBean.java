package com.corpize.sdk.mobads.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author TangHongChang
 * @version 1.0
 * @date 创建时间： 2018/5/10 下午5:00
 * @Description 广告类型为205的json实体类
 * @fileName AdmJsonBean.java
 */

public class AdmJsonBean implements Serializable {

    /**
     * format : 2
     * duration : 15
     * width : 640
     * icon : {"resource":"http://img5.imgtn.bdimg.com/it/u=728139892,3758641547&fm=27&gp=0.jpg","imps":["http://imp1.icon.com","http://imp2.icon.com"],"w":50,"url":"http://icon.ldp.com","h":50,"clks":["http://click1.icon.com","http://click2.icon.com"]}
     * clks : ["http://clicktracking2.video.com","http://clicktracking1.video.com"]
     * event : {"midpoint":["http://midpoint1.tracking.com","http://midpoint2.tracking.com"],"acceptInvitation":["http://acceptInvitation.tracking1.com","http://acceptInvitation.tracking2.com"],"fullscreen":["http://fullscreen.tracking1.com","http://fullscreen.tracking2.com"],"mute":["http://mute.tracking1.com","http://mute.tracking2.com"],"pause":["http://pause.tracking1.com","http://pause.tracking2.com"],"unmute":["http://unmute.tracking1.com","http://unmute.tracking2.com"],"close":["http://close.tracking1.com","http://close.tracking2.com"],"complete":["http://complete1.tracking.com","http://complete2.tracking.com"],"thirdQuartile":["http://thirdQuartile.tracking1.com","http://thirdQuartile.tracking2.com"],"start":["http://start.tracking1.com","http://start.tracking2.com"],"firstQuartile":["http://firstQuartile.tracking1.com","http://firstQuartile.tracking2.com"]}
     * desc : This is description of the AD
     * ldp : http://www.corpize.com/
     * title : this is ad title
     * skip : 3
     * deeplink :
     * companion : {"resource":"http://test2014.adview.cn:8088/agent/image/yyfl1aa3_vastexpandwith.png","creativeview":["http://compandAd.imp1.com","http://compandAd.imp2.com"],"w":640,"url":"http://compands.ldp.com","h":100}
     * height : 480
     * videourl : http://ubmcvideo.baidustatic.com/media/v1/0f000ntDpAfJrWWXmTK7A0.mp4
     */

    private int           format;
    private double        duration;
    private int           width;
    private int           height;
    private int           action;
    private String        ldp;
    private String        videourl;
    private int           skip;
    private List<String>  imps;
    private List<String>  clks;
    private EventBean     event;
    private String        title;
    private String        desc;
    private String        deeplink;
    private IconBean      icon;
    private String        firstimg;
    private CompanionBean companion;

    public int getFormat () {
        return format;
    }

    public void setFormat (int format) {
        this.format = format;
    }

    public int getDuration () {
        return getCount(duration);
    }

    public static int getCount (double val) {
        if (val % 1 == 0) {//是整数
            return (int) val;
        } else {//不是整数
            return (int) (val + 1);
        }
    }

    public void setDuration (double duration) {
        this.duration = duration;
    }

    public int getWidth () {
        return width;
    }

    public void setWidth (int width) {
        this.width = width;
    }

    public int getHeight () {
        return height;
    }

    public void setHeight (int height) {
        this.height = height;
    }


    public int getAction () {
        return action;
    }

    public void setAction (int action) {
        this.action = action;
    }

    public String getLdp () {
        return ldp;
    }

    public void setLdp (String ldp) {
        this.ldp = ldp;
    }

    public String getVideourl () {
        return videourl;
    }

    public void setVideourl (String videourl) {
        this.videourl = videourl;
    }

    public int getSkip () {
        return skip;
    }

    public void setSkip (int skip) {
        this.skip = skip;
    }

    public List<String> getImps () {
        return imps;
    }

    public void setImps (List<String> imps) {
        this.imps = imps;
    }

    public List<String> getClks () {
        return clks;
    }

    public void setClks (List<String> clks) {
        this.clks = clks;
    }

    public EventBean getEvent () {
        return event;
    }

    public void setEvent (EventBean event) {
        this.event = event;
    }

    public String getTitle () {
        return title;
    }

    public void setTitle (String title) {
        this.title = title;
    }

    public String getDesc () {
        return desc;
    }

    public void setDesc (String desc) {
        this.desc = desc;
    }

    public String getDeeplink () {
        return deeplink;
    }

    public void setDeeplink (String deeplink) {
        this.deeplink = deeplink;
    }

    public IconBean getIcon () {
        return icon;
    }

    public void setIcon (IconBean icon) {
        this.icon = icon;
    }

    public String getFirstimg () {
        return firstimg;
    }

    public void setFirstimg (String firstimg) {
        this.firstimg = firstimg;
    }

    public CompanionBean getCompanion () {
        return companion;
    }

    public void setCompanion (CompanionBean companion) {
        this.companion = companion;
    }
}
