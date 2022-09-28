package com.corpize.sdk.mobads.bean;

import java.io.Serializable;
import java.util.List;

/**
 * author ：yh
 * date : 2020-02-15 22:17
 * description : 广告类型为205的json的Icon实体类
 */
public class IconBean implements Serializable {

    /**
     * resource : http://img5.imgtn.bdimg.com/it/u=728139892,3758641547&fm=27&gp=0.jpg
     * imps : ["http://imp1.icon.com","http://imp2.icon.com"]
     * w : 50
     * url : http://icon.ldp.com
     * h : 50
     * clks : ["http://click1.icon.com","http://click2.icon.com"]
     */

    private String       resource;
    private int          w;
    private String       url;
    private int          h;
    private List<String> imps;
    private List<String> clks;

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

    public List<String> getImps() {
        return imps;
    }

    public void setImps(List<String> imps) {
        this.imps = imps;
    }

    public List<String> getClks() {
        return clks;
    }

    public void setClks(List<String> clks) {
        this.clks = clks;
    }

}
