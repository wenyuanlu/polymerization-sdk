package com.chanling.haohai;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.chanling.haohai.utils.Constants;
import com.corpize.sdk.mobads.QcAd;
import com.chanling.haohai.adapter.InfoOneAdapter;
import com.chanling.haohai.adapter.NormalItem;
import com.corpize.sdk.mobads.admanager.InfoOneManager;
import com.corpize.sdk.mobads.listener.InfoOneQcAdListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单图信息流广告位展示
 */
public class InfoOneAdActivity extends AppCompatActivity implements View.OnClickListener {

    private Button             mBtInfoAdd;
    private RecyclerView       mRecyclerView;
    private InfoOneAdapter     mAdapter;
    private List<NormalItem>   mDataList          = new ArrayList<>();
    private int                mFirstAdPosition   = 0;      // 第一个条目的位置
    public  int                mOtherPosition     = 5;      // 每间隔5个条目插入一条广告
    private Map<View, Integer> mAdViewPositionMap = new HashMap<>();//每一个条目的位置的记录
    private int                mMaxNum            = 3;      //获取广告的最大数量,值1~3(可获取1~maxNum条,最多获取maxNum条)
    private InfoOneManager     mManager;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_one_ad);

        mBtInfoAdd = (Button) findViewById(R.id.bt_audio_add);
        mBtInfoAdd.setOnClickListener(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_one);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        //设置RecycleView初始化数据
        initData();

    }

    /**
     * 设置RecycleView初始化数据
     */
    private void initData () {
        for (int i = 0; i < 50; ++i) {
            mDataList.add(new NormalItem("No." + i + " Normal Data"));
        }
        mAdapter = new InfoOneAdapter(this, mDataList);
        mRecyclerView.setAdapter(mAdapter);
        //设置回调,每一次关掉广告,刷新list后,广告的位置会变化,所以要重新记录view
        mAdapter.setOnMapChangeListener(mAdViewPositionMap, new InfoOneAdapter.MapChangeListener() {
            @Override
            public void onChange (Map<View, Integer> map) {
                mAdViewPositionMap.clear();
                mAdViewPositionMap.putAll(map);
            }
        });
    }

    @Override
    public void onClick (View v) {
        switch (v.getId()) {
            case R.id.bt_audio_add://获取广告
                //清理之前的广告数据
                mAdViewPositionMap = new HashMap<>();
                mDataList.clear();
                for (int i = 0; i < 50; ++i) {
                    mDataList.add(new NormalItem("No." + i + " Normal Data"));
                }

                //开始获取广告
                String adId = "";
                if (Constants.IS_AD_TEST) {
                    adId = "FE26EC5023AE52F0C0ABCFAD8B9621C8";
                } else {
                    adId = "12FD5625922D4749B27EA228F6C5BE3B";
                }
                QcAd.get().infoOneAds(InfoOneAdActivity.this, adId, mMaxNum, mOnePhotoListener);
                break;
            default:
                break;
        }
    }

    /**
     * 单图信息流的回调
     */
    private InfoOneQcAdListener mOnePhotoListener = new InfoOneQcAdListener() {

        @Override
        public void onADManager (InfoOneManager manager) {
            mManager = manager;
        }

        @Override
        public void onAdViewSuccess (List<View> adViews, float w, float h, String tag) {
            Log.e("InfoOneAdActivity", "返回数据onAdViewSuccess" + "宽=" + w + "高=" + h);
            if (adViews == null || adViews.size() == 0) {
                return;
            }
            //设置view在list中加载的位置
            for (int i = 0; i < adViews.size(); i++) {
                View view     = adViews.get(i);
                int  position = mFirstAdPosition + i * mOtherPosition;
                //添加广告数据
                mDataList.add(position, new NormalItem(view));
                //把每个广告在列表中位置记录下来
                mAdViewPositionMap.put(view, position);
            }

            //展示数据
            mAdapter.setData(mDataList);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onAdExposure (String tag) {
            Log.e("InfoOneAdActivity", "返回了=onAdExposure");
        }

        @Override
        public void onAdClicked (View view,String tag) {
            Log.e("InfoOneAdActivity", "返回了=onAdClicked || view");
        }

        @Override
        public void onAdClicked (String tag) {
            Log.e("InfoOneAdActivity", "返回了=onAdClicked");
        }

        @Override
        public void onAdClose (View view, String tag) {//广告关闭,自动关闭
            int removedPosition = mAdViewPositionMap.get(view);
            Log.e("InfoOneAdActivity", "返回了=onAdClose=" + removedPosition);
            mDataList.remove(removedPosition);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onAdError (String tag, String fail) {
            Log.e("InfoOneAdActivity", "返回了=onAdError" + fail);
        }

    };

    @Override
    protected void onDestroy () {
        super.onDestroy();
        //不加载时,释放
        if (mManager != null) {
            mManager.destroyAd();
        }
        QcAd.get().clear();
    }


}
