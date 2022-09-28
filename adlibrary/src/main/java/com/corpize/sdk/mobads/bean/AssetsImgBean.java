package com.corpize.sdk.mobads.bean;

import java.util.List;

/**
 * @author TangHongChang
 * @version 1.0
 * @date 创建时间： 2018/4/26 上午11:00
 * @Description 图片内容要求
 * @fileName AssetsImgBean.java
 */
public class AssetsImgBean {

    private int          type;    //image 元素的类型  1:icon 图片 2:logo 图片 3:广告大图 4:多图(2 或 3 张)
    private List<String> url;   //image 元素的 URL 地址
    private int          w;     //图片宽度
    private int          h;  //图片高度

    public int getType () {
        return type;
    }

    public void setType (int type) {
        this.type = type;
    }

    public List<String> getUrl () {
        return url;
    }

    public void setUrl (List<String> url) {
        this.url = url;
    }

    public int getW () {
        return w;
    }

    public void setW (int w) {
        this.w = w;
    }

    public int getH () {
        return h;
    }

    public void setH (int h) {
        this.h = h;
    }
}
