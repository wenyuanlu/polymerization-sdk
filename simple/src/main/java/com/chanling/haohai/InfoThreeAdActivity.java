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
import com.corpize.sdk.mobads.admanager.InfoThreeManager;
import com.corpize.sdk.mobads.listener.InfoMoreQcAdListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多图信息流广告位展示
 */
public class InfoThreeAdActivity extends AppCompatActivity implements View.OnClickListener {

    private Button              mBtInfoAdd;
    private RecyclerView        mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private InfoOneAdapter      mAdapter;
    private List<NormalItem>    mDataList          = new ArrayList<NormalItem>();
    private int                 mFirstAdPosition   = 0;      // 第一个条目的位置
    public  int                 mOtherPosition     = 5;      // 每间隔5个条目插入一条广告
    private Map<View, Integer>  mAdViewPositionMap = new HashMap<>();//每一个条目的位置的记录
    private int                 mMaxNum            = 3;      //获取广告的最大数量,值1~3(可获取1~maxNum条,最多获取maxNum条)
    private InfoThreeManager    mManager;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_three_ad);

        mBtInfoAdd = (Button) findViewById(R.id.bt_info_three);
        mBtInfoAdd.setOnClickListener(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_one);
        mRecyclerView.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(this);
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
            case R.id.bt_info_three://获取广告
                //清理之前的广告数据
                mAdViewPositionMap = new HashMap<>();
                mDataList.clear();
                for (int i = 0; i < 50; ++i) {
                    mDataList.add(new NormalItem("No." + i + " Normal Data"));
                }

                //开始获取广告
                String adId = "";
                if (Constants.IS_AD_TEST) {
                    adId = "A363898A34EC9DA658071B610EBFBAD3";
                } else {
                    adId = "CF7E9FB74280BF419C44710DA1CE1543";
                }
                QcAd.get().infoThreeAds(InfoThreeAdActivity.this, adId, mMaxNum, mListener);
                break;

            default:
                break;

        }
    }

    /**
     * 多图信息流的回调
     */
    private InfoMoreQcAdListener mListener = new InfoMoreQcAdListener() {

        @Override
        public void onADManager (InfoThreeManager manager) {
            mManager = manager;
        }

        @Override
        public void onAdViewSuccess (List<View> adViews, String tag) {
            Log.e("InfoThreeAdActivity", "返回数据onAdViewSuccess");
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
            Log.e("InfoThreeAdActivity", "返回了=onAdExposure");
        }

        @Override
        public void onAdClicked (View view, String tag) {
            Log.e("InfoThreeAdActivity", "返回了=onAdClicked || view");
        }

        @Override
        public void onAdClose (View view, String tag) {
            int removedPosition = mAdViewPositionMap.get(view);
            Log.e("InfoThreeAdActivity", "返回了=onAdClose=" + removedPosition);
            mDataList.remove(removedPosition);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onAdClicked (String tag) {
            Log.e("InfoThreeAdActivity", "返回了=onAdClicked");
        }

        @Override
        public void onAdError (String tag, String fail) {
            Log.e("InfoThreeAdActivity", "返回了=onAdError:" + fail);
        }

    };

    @Override
    public void onDestroy () {
        super.onDestroy();
        if (mManager != null) {
            mManager.destroyAd();
        }
    }


}
