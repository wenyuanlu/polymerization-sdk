package com.corpize.sdk.mobads.bean;

/**
 * author: yh
 * date: 2019-08-20 16:35
 * description: 音频信息
 */
public class VoiceBean {

    //private String  format = "mp3";     //广告位支持的音频格式数组 例如：mp3
    //private boolean mike   = true;      //媒体调用麦克风的权限

    private String  format;     //广告位支持的音频格式数组 例如：mp3
    private boolean mike;      //媒体调用麦克风的权限

    public String getFormat () {
        return format;
    }

    public void setFormat (String format) {
        this.format = format;
    }

    public boolean isMike () {
        return mike;
    }

    public void setMike (boolean mike) {
        this.mike = mike;
    }
}
