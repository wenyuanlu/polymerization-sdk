package com.chanling.haohai;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.chanling.haohai.utils.Constants;
import com.corpize.sdk.mobads.QcAd;
import com.corpize.sdk.mobads.admanager.SplashManager;
import com.corpize.sdk.mobads.listener.SplashQcAdListener;

/**
 * 开屏广告位展示
 */
public class SplashAdActivity extends AppCompatActivity {

    private ImageView     mSplashHolder;      //默认背景图
    private FrameLayout   mSplashContainer;   //广告容器
    private TextView      mSkipView;          //跳过按钮
    private SplashManager mManager;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_ad);

        mSplashContainer = (FrameLayout) findViewById(R.id.splash_container);//广告显示控件
        mSplashHolder = (ImageView) findViewById(R.id.splash_holder);
        mSkipView = (TextView) findViewById(R.id.skip_view);//自定义按钮控件

        String adId = "";
        if (Constants.IS_AD_TEST) {
            adId = "9716893D4E59C34734D1B5F9ADFE699C";
        } else {
            adId = "2B691B6673B0EB381F988634CB89924A";
        }
        QcAd.get().splashAds(this, mSplashContainer, mSkipView, adId, mListener);

    }

    /**
     * 开屏回调
     */
    private SplashQcAdListener mListener = new SplashQcAdListener() {

        @Override
        public void onADManager (SplashManager manager) {
            Log.e("SplashAdActivity", "onADManager");
            mManager = manager;
        }


        @Override
        public void onADAdd (String tag) {

        }

        @Override
        public void onADExposure (String tag) {
            Log.e("SplashAdActivity", "onADExposure");
            //广告开始展示,建议这时候隐藏背景图
            mSplashHolder.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onADDismissed () {
            //广告消失,点击跳过的消失或者倒计时结束的消失
            Log.e("SplashAdActivity", "onADDismissed");
            goToNextActivity();
        }

        @Override
        public void onAdClicked (String tag) {
            Log.e("SplashAdActivity", "onAdClicked");
            //广告点击了
        }

        @Override
        public void onAdError (String tag, String fail) {
            Log.e("SplashAdActivity", "onAdError=" + fail);
            //出错
            goToNextActivity();
        }
    };

    /**
     * 正常逻辑跳转到主界面,demo关闭当前界面
     */
    private void goToNextActivity () {
        finish();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        //在合适的地方调用
        if (mManager != null) {
            mManager.destroyAd();
        }
    }

}
