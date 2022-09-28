package com.corpize.sdk.mobads.bean;

/**
 * @author TangHongChang
 * @version 1.0
 * @date 创建时间： 2018/4/26 上午11:01
 * @Description 音频内容要求
 */
public class AssetsAudioBean {

    private String type;
    private String url;
    private String duration;
    private String bitrate;

    public String getType () {
        return type;
    }

    public void setType (String type) {
        this.type = type;
    }

    public String getUrl () {
        return url;
    }

    public void setUrl (String url) {
        this.url = url;
    }

    public String getDuration () {
        return duration;
    }

    public void setDuration (String duration) {
        this.duration = duration;
    }

    public String getBitrate () {
        return bitrate;
    }

    public void setBitrate (String bitrate) {
        this.bitrate = bitrate;
    }
}
