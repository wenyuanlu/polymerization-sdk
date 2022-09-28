package com.corpize.sdk.mobads.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * author ：yh
 * date : 2020-02-11 22:18
 * description : 广告请求返回数据
 */
public class AdResponseBean implements Serializable {

    private int        version;
    private int        status;
    private int        supplier;//0时为兜底广告
    private String     adm;
    @SerializedName ("native")
    private NativeBean native1;
    private ExtBean    ext;

    public int getSupplier () {
        return supplier;
    }

    public void setSupplier (int supplier) {
        this.supplier = supplier;
    }

    public int getVersion () {
        return version;
    }

    public void setVersion (int version) {
        this.version = version;
    }

    public int getStatus () {
        return status;
    }

    public void setStatus (int status) {
        this.status = status;
    }

    public String getAdm () {
        return adm;
    }

    public void setAdm (String adm) {
        this.adm = adm;
    }

    public NativeBean getNative1 () {
        return native1;
    }

    public void setNative1 (NativeBean native1) {
        this.native1 = native1;
    }

    public ExtBean getExt () {
        return ext;
    }

    public void setExt (ExtBean ext) {
        this.ext = ext;
    }
}
