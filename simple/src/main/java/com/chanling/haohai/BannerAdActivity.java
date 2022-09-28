package com.chanling.haohai;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.chanling.haohai.utils.Constants;
import com.corpize.sdk.mobads.QcAd;
import com.corpize.sdk.mobads.admanager.BannerManager;
import com.corpize.sdk.mobads.listener.BannerQcAdListener;
import com.corpize.sdk.mobads.utils.DeviceUtil;

/**
 * banner广告位展示
 */
public class BannerAdActivity extends AppCompatActivity implements View.OnClickListener {

    private Button        mBtBannerAdd;
    private Button        mBtBannerDestroy;
    private FrameLayout   mBannerContainer;
    private BannerManager mBannerManager;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner_ad);

        mBannerContainer = (FrameLayout) findViewById(R.id.banner_container);
        mBtBannerAdd = (Button) findViewById(R.id.bt_banner_add);
        mBtBannerDestroy = (Button) findViewById(R.id.bt_banner_destroy);
        mBtBannerAdd.setOnClickListener(this);
        mBtBannerDestroy.setOnClickListener(this);
    }

    @Override
    public void onClick (View v) {
        switch (v.getId()) {
            case R.id.bt_banner_add:        //展示广告
                String adId = "";
                if (Constants.IS_AD_TEST) {
                    adId = "C27F3148ADF7720DFB543D4FCF28A285";
                } else {
                    adId = "1A66026122A2F1358526EDC1CA9E659F";
                }
                int screenWidth = DeviceUtil.getScreenWidth(this);
                int adwigth = DeviceUtil.px2dip(this, screenWidth);
                QcAd.get().bannerAds(this, mBannerContainer, adId, adwigth, mListener);

                break;

            case R.id.bt_banner_destroy:    //清除广告
                //在合适的时机，释放广告的资源
                if (mBannerManager != null) {
                    mBannerManager.destroyAd();
                }
                mBannerContainer.removeAllViews();
                break;

            default:
                break;

        }
    }

    /**
     * Banner广告监听
     */
    private BannerQcAdListener mListener = new BannerQcAdListener() {
        @Override
        public void onADManager (BannerManager manager) {
            //返回当前BannerManager
            mBannerManager = manager;
        }

        @Override
        public void onADExposure (String tag) {
            Log.e("BannerAdActivity", "onADExposure");
        }

        @Override
        public void onAdClose () {
            Log.e("BannerAdActivity", "onAdClose");
        }

        @Override
        public void onAdClicked (String tag) {
            Log.e("BannerAdActivity", "onAdClicked");
        }

        @Override
        public void onAdError (String tag, String fail) {
            Log.e("BannerAdActivity", "onAdError" + fail);
        }
    };

    @Override
    protected void onDestroy () {
        super.onDestroy();
        //在合适的时机，释放广告的资源
        if (mBannerManager != null) {
            mBannerManager.destroyAd();
        }

    }
}
