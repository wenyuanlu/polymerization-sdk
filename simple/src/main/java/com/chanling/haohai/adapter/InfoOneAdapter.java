package com.chanling.haohai.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import com.chanling.haohai.R;

import java.util.List;
import java.util.Map;

/**
 * 单图信息流对应的adapter
 */
public class InfoOneAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private              Context            context;
    private              List<NormalItem>   mData;
    private              int                dataSize;
    private static final int                TYPE_AD = 1;
    private              Map<View, Integer> mAdViewPositionMap;
    private              MapChangeListener  mMapChangeListener;

    public InfoOneAdapter (Context context, List data) {
        this.context = context;
        this.mData = data;
        if (mData != null) {
            this.dataSize = mData.size();
        }
    }

    public void setData (List<NormalItem> dataList) {
        this.mData = dataList;
        if (mData != null) {
            this.dataSize = mData.size();
        }
    }

    public void setOnMapChangeListener (Map<View, Integer> adViewPositionMap, MapChangeListener mapChangeListener) {
        mAdViewPositionMap = adViewPositionMap;
        mMapChangeListener = mapChangeListener;
    }

    @Override
    public int getItemViewType (int position) {
        if (mData.get(position).getType() == 1) {
            return TYPE_AD;
        } else {
            return 0;
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder (ViewGroup viewGroup, int viewType) {
        View view;
        if (viewType == TYPE_AD) {//广告布局
            view = LayoutInflater.from(context).inflate(R.layout.item_express_ad, null);
        } else {//原来数据的布局
            view = LayoutInflater.from(context).inflate(R.layout.item_data, null);
        }

        CustomViewHolder customViewHolder = new CustomViewHolder(view);
        return customViewHolder;
    }

    @Override
    public void onBindViewHolder (RecyclerView.ViewHolder viewHolder, final int position) {
        CustomViewHolder customViewHolder = (CustomViewHolder) viewHolder;

        int type = getItemViewType(position);
        if (TYPE_AD == type) {//加载广告
            View qcAdView = mData.get(position).getAdView();
            if (mAdViewPositionMap != null) {
                mAdViewPositionMap.put(qcAdView, position); // 广告在列表中的位置是可以被更新的
                if (mMapChangeListener != null) {
                    mMapChangeListener.onChange(mAdViewPositionMap);
                }
            }

            if (customViewHolder.container.getChildCount() > 0) {
                customViewHolder.container.removeAllViews();
            }
            if (qcAdView.getParent() != null) {
                ((ViewGroup) qcAdView.getParent()).removeView(qcAdView);
            }

            customViewHolder.container.addView(qcAdView);

        } else {//加载原来的数据
            customViewHolder.title.setText(((NormalItem) mData.get(position)).getTitle());
        }

    }

    @Override
    public int getItemCount () {
        if (mData != null) {
            return mData.size();
        } else {
            return 0;
        }
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        public TextView  title;
        public ViewGroup container;      //这个是加载广告的容器

        public CustomViewHolder (View view) {
            super(view);
            title = (TextView) view.findViewWithTag("title");
            container = (ViewGroup) view.findViewWithTag("express_ad_container");
        }
    }

    public interface MapChangeListener {
        void onChange (Map<View, Integer> map);
    }


}
