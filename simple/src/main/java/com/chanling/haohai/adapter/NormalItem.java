package com.chanling.haohai.adapter;

import android.view.View;

public class NormalItem {
    private int    type;//0是原来条目,1是广告
    private String title;
    private View   adView;

    public NormalItem (String title) {
        this.type = 0;
        this.title = title;
    }

    public NormalItem (View view) {
        this.type = 1;
        this.adView = view;
    }

    public String getTitle () {
        return title;
    }

    public void setTitle (String title) {
        this.title = title;
    }

    public int getType () {
        return type;
    }

    public void setType (int type) {
        this.type = type;
    }

    public View getAdView () {
        return adView;
    }

    public void setAdView (View adView) {
        this.adView = adView;
    }
}
