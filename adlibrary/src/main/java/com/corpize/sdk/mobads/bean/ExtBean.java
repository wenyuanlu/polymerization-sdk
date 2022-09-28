package com.corpize.sdk.mobads.bean;

import java.util.List;

/**
 * @author TangHongChang
 * @version 1.0
 * @date 创建时间： 2018/4/25 下午1:39
 * @Description 根据媒体预先设定情况可选择性 的返回参数形式广告物料，物料内 容与 adm 相同
 * @fileName ExtBean.java
 */
public class ExtBean {

    /**
     * clickurl : http://www.corpize.com/
     * imptrackers : ["http://adx-test.corpize.com/tj?bid=3b06591d0b4f292004ed5eed1d65c7b9&id=0&m=rtb&imp=a073789dcc8e23dac36e55c3caabba0b6ace3239a6b3514bcfba2e7ccf2b8f3273adcaaec1b87eb4f247c471899f2432d5eab976c5bfecd61bd6a96e095790351853d66f7e291d8177519e65bc74143bfee8af1ed18a4a48f61c8664e2f33f5ff937761f9cad68b924ffde0d95dedf5d"]
     * clicktrackers : ["http://adx-test.corpize.com/tj?bid=4c3315e08c5d0371032b0d2f30e8adf0&id=0&m=rtb&clk=a073789dcc8e23dac36e55c3caabba0b6ace3239a6b3514bcfba2e7ccf2b8f3273adcaaec1b87eb4f247c471899f2432d5eab976c5bfecd61bd6a96e095790351853d66f7e291d8177519e65bc74143bfee8af1ed18a4a48f61c8664e2f33f5ff937761f9cad68b924ffde0d95dedf5d"]
     * action : 2
     * iurl : http://img.zcool.cn/community/016fba598be2ab00000021297c6652.jpg
     */

    private String                      clickurl;
    private int                         materialtype;//1的时候是图片,2的时候是视频,不存在代表图片
    private int                         action;
    private int                         duration;//视频的时间
    private String                      iurl;
    private String                      fallback;    // deeplink 链接
    private String                      dfn;   //APP 应用下载链接
    private List<String>                imptrackers;
    private List<String>                fallbacktrackers;
    private List<String>                clicktrackers;
    private AssetsLinkEventtrackersBean eventtrackers;  //用户端事件上报对象  转化上报

    public String getClickurl () {
        return clickurl;
    }

    public void setClickurl (String clickurl) {
        this.clickurl = clickurl;
    }

    public int getMaterialtype () {
        return materialtype;
    }

    public void setMaterialtype (int materialtype) {
        this.materialtype = materialtype;
    }

    public int getAction () {
        return action;
    }

    public void setAction (int action) {
        this.action = action;
    }

    public String getIurl () {
        return iurl;
    }

    public void setIurl (String iurl) {
        this.iurl = iurl;
    }

    public int getDuration () {
        return duration;
    }

    public void setDuration (int duration) {
        this.duration = duration;
    }

    public String getFallback () {
        return fallback;
    }

    public void setFallback (String fallback) {
        this.fallback = fallback;
    }

    public List<String> getImptrackers () {
        return imptrackers;
    }

    public void setImptrackers (List<String> imptrackers) {
        this.imptrackers = imptrackers;
    }

    public List<String> getFallbacktrackers () {
        return fallbacktrackers;
    }

    public void setFallbacktrackers (List<String> fallbacktrackers) {
        this.fallbacktrackers = fallbacktrackers;
    }

    public List<String> getClicktrackers () {
        return clicktrackers;
    }

    public void setClicktrackers (List<String> clicktrackers) {
        this.clicktrackers = clicktrackers;
    }

    public String getDfn () {
        return dfn;
    }

    public void setDfn (String dfn) {
        this.dfn = dfn;
    }

    public AssetsLinkEventtrackersBean getEventtrackers () {
        return eventtrackers;
    }

    public void setEventtrackers (AssetsLinkEventtrackersBean eventtrackers) {
        this.eventtrackers = eventtrackers;
    }
}
