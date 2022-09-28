package com.corpize.sdk.mobads.bean;

/**
 * @author TangHongChang
 * @version 1.0
 * @date 创建时间： 2018/4/26 上午10:55
 * @Description 原生广告物料对象
 * @fileName AssetsBean.java
 */
public class AssetsBean {

    private int             id;     //不重复的素材 id  一般是数组中该元素的计数
    private AssetsTitleBean title;  //标题文本内容要求
    private AssetsImgBean   img;    //图片内容要求
    private AssetsDataBean  data;   //数据内容要求
    private AssetsAudioBean audio;  //音频内容要求

    public int getId () {
        return id;
    }

    public void setId (int id) {
        this.id = id;
    }

    public AssetsTitleBean getTitle () {
        return title;
    }

    public void setTitle (AssetsTitleBean title) {
        this.title = title;
    }

    public AssetsImgBean getImg () {
        return img;
    }

    public void setImg (AssetsImgBean img) {
        this.img = img;
    }

    public AssetsDataBean getData () {
        return data;
    }

    public void setData (AssetsDataBean data) {
        this.data = data;
    }

    public AssetsAudioBean getAudio () {
        return audio;
    }

    public void setAudio (AssetsAudioBean audio) {
        this.audio = audio;
    }
}
