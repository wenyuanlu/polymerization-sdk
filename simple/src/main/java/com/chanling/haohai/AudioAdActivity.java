package com.chanling.haohai;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.corpize.sdk.mobads.QcAd;
import com.corpize.sdk.mobads.admanager.AudioAdManager;
import com.corpize.sdk.mobads.listener.AudioQcAdListener;

/**
 * 音频广告位展示
 */
public class AudioAdActivity extends AppCompatActivity implements View.OnClickListener {

    private Button         mBtAudioAdd;
    private AudioAdManager mManager;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_ad);

        mBtAudioAdd = (Button) findViewById(R.id.bt_audio_add);
        mBtAudioAdd.setOnClickListener(this);

    }

    @Override
    public void onClick (View v) {
        switch (v.getId()) {
            case R.id.bt_audio_add:
                String adId = "1F00537BFA2F82669A27E2AFE781B0C9";
                QcAd.get().audioAds(AudioAdActivity.this, adId, mListener);
                break;

            default:
                break;
        }
    }

    /**
     * 音频广告回调
     */
    private AudioQcAdListener mListener = new AudioQcAdListener() {
        @Override
        public void onADManager (AudioAdManager manager) {
            //Log.e("AudioAdActivity", "onADManager");
            mManager = manager;//用于清理广告
        }

        @Override
        public void onADReceive (AudioAdManager manager) {
            Log.e("AudioAdActivity", "onADReceive");
            manager.addAd();//加载广告
        }

        @Override
        public void onADExposure () {
            Log.e("AudioAdActivity", "onADExposure");
            //音频广告开始播放
        }

        @Override
        public void onAdCompletion () {
            Log.e("AudioAdActivity", "onAdCompletion");
            //音频播放结束
        }

        @Override
        public void onAdError (String fail) {
            Log.e("AudioAdActivity", "onAdError=" + fail);
            //广告播放错误
        }
    };

    @Override
    protected void onDestroy () {
        super.onDestroy();
        //合适的时候调用清理广告方法
        if (mManager != null) {
            mManager.destroy();
        }
    }
}
