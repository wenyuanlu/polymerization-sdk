package com.chanling.haohai;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.corpize.sdk.mobads.QcAd;
import com.corpize.sdk.mobads.admanager.AudioAdManager;
import com.corpize.sdk.mobads.admanager.NativeVideoManager;
import com.corpize.sdk.mobads.listener.NativeVideoQcAdListener;

/**
 * 激励视频广告位展示
 */
public class VideoNativeAdActivity extends AppCompatActivity implements View.OnClickListener {

    private Button          mBtVideoAdd;
    private FrameLayout     mAdContainer;
    private VideoView       mVideoView;
    private TextView        mTextCountDown;
    private Button          mButtonDetail;
    private TextView        mTvVideoTitle;
    private TextView        mTvVideoContent;
    private MediaController mMediaController;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tie_ad);

        mAdContainer = (FrameLayout) findViewById(R.id.ad_container);
        mVideoView = (VideoView) findViewById(R.id.video_view);
        mTextCountDown = (TextView) findViewById(R.id.text_count_down);
        mButtonDetail = (Button) findViewById(R.id.button_download);
        mTvVideoTitle = (TextView) findViewById(R.id.video_title);
        mTvVideoContent = (TextView) findViewById(R.id.video_content);

        mBtVideoAdd = (Button) findViewById(R.id.bt_tie_add);
        mBtVideoAdd.setOnClickListener(this);

        mMediaController = new MediaController(this);
        mVideoView.setMediaController(mMediaController);

    }

    @Override
    public void onClick (View v) {
        switch (v.getId()) {
            case R.id.bt_tie_add://获取广告
                String adId = "4E017C7CEC921A15604BDC40DC2E9A12";
                QcAd.get().nativeVideoAd(VideoNativeAdActivity.this, adId, mMediaController, mVideoView, mAdContainer,
                        mButtonDetail, mTextCountDown, mTvVideoTitle, mTvVideoContent, mListener);
                break;

            default:
                break;

        }
    }

    private NativeVideoManager      mManager;
    /**
     * 回调
     */
    private NativeVideoQcAdListener mListener = new NativeVideoQcAdListener() {
        @Override
        public void onADManager (NativeVideoManager manager) {
            mManager = manager;
        }

        @Override
        public void onADReceive (NativeVideoManager manager) {
            Log.e("VideoNativeAdActivity", "onADReceive");
            manager.show(VideoNativeAdActivity.this);//展示广告
        }

        @Override
        public void onADExposure () {
            Log.e("VideoNativeAdActivity", "onADExposure");
        }

        @Override
        public void onAdCompletion () {
            Log.e("VideoNativeAdActivity", "onAdCompletion");
        }

        @Override
        public void onAdClicked () {
            Log.e("VideoNativeAdActivity", "onAdClicked");
        }

        @Override
        public void onAdError (String fail) {
            Log.e("VideoNativeAdActivity", "onAdError");
        }
    };

    @Override
    protected void onDestroy () {
        super.onDestroy();
        if (mManager != null) {
            mManager.destroy();
        }
    }
}
