package com.corpize.sdk.mobads.bean;

import java.io.Serializable;
import java.util.List;

/**
 * author ：yh
 * date : 2020-02-15 22:16
 * description :
 */
public class CompanionBean implements Serializable {

    /**
     * resource : http://test2014.adview.cn:8088/agent/image/yyfl1aa3_vastexpandwith.png
     * creativeview : ["http://compandAd.imp1.com","http://compandAd.imp2.com"]
     * w : 640
     * url : http://compands.ldp.com
     * h : 100
     */

    private String       resource;
    private int          w;
    private String       url;
    private int          h;
    private List<String> creativeview;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public List<String> getCreativeview() {
        return creativeview;
    }

    public void setCreativeview(List<String> creativeview) {
        this.creativeview = creativeview;
    }
}
