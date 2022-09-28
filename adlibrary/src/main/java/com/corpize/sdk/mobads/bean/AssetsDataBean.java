package com.corpize.sdk.mobads.bean;

/**
 * @author TangHongChang
 * @version 1.0
 * @date 创建时间： 2018/4/26 上午11:01
 * @Description 数据内容要求
 * @fileName AssetsDataBean.java
 */
public class AssetsDataBean {

    private String label;  //数据元素名称
    private String value;    //格式化文本内容，可以包括修饰字 符，比如“$10“

    public String getLabel () {
        return label;
    }

    public void setLabel (String label) {
        this.label = label;
    }

    public String getValue () {
        return value;
    }

    public void setValue (String value) {
        this.value = value;
    }
}
