package com.chanling.haohai;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.chanling.haohai.utils.Constants;
import com.corpize.sdk.mobads.QcAd;
import com.corpize.sdk.mobads.admanager.RewardVideoManager;
import com.corpize.sdk.mobads.listener.RewardVideoQcAdListener;

/**
 * 激励视频广告位展示
 */
public class VideoRewardAdActivity extends AppCompatActivity implements View.OnClickListener {

    private Button             mBtAudioAdd;
    private RewardVideoManager mManager;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_ad);

        mBtAudioAdd = (Button) findViewById(R.id.bt_reward_add);
        mBtAudioAdd.setOnClickListener(this);

    }

    @Override
    public void onClick (View v) {
        switch (v.getId()) {
            case R.id.bt_reward_add:////获取广告
                String adId = "";
                if (Constants.IS_AD_TEST) {
                    adId = "971AB6FDFC33EA132FE6BAC5A73C6FC6";
                } else {
                    adId = "CC79C9F5067C39165466819CCAAEB103";
                }
                QcAd.get().rewardVideoAds(this, adId, mListener);
                break;

            default:
                break;
        }
    }

    /**
     * 广告回调
     */
    private RewardVideoQcAdListener mListener = new RewardVideoQcAdListener() {
        @Override
        public void onADManager (RewardVideoManager manager) {
            mManager = manager;
        }

        @Override
        public void onADReceive (RewardVideoManager manager, String tag) {
            Log.e("VideoRewardAdActivity", "onADReceive" + tag);
            manager.showAd(VideoRewardAdActivity.this);
        }

        @Override
        public void onADExposure (String tag) {
            Log.e("VideoRewardAdActivity", "onADExposure" + tag);
        }

        @Override
        public void onAdClicked (String tag) {
            Log.e("VideoRewardAdActivity", "onAdClicked");
        }

        @Override
        public void onAdClose () {
            Log.e("VideoRewardAdActivity", "onAdClose");
        }

        @Override
        public void onAdCompletion () {
            Log.e("VideoRewardAdActivity", "onAdCompletion");
        }

        @Override
        public void onAdError (String tag, String fail) {
            Log.e("VideoRewardAdActivity", "onAdError=" + fail);
        }
    };

    @Override
    protected void onDestroy () {
        super.onDestroy();
        //合适的位置销毁广告
        if (mManager != null) {
            mManager.destroy();
        }
    }


}
