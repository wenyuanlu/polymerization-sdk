package com.chanling.haohai;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.chanling.haohai.R;
import com.chanling.haohai.utils.Constants;
import com.corpize.sdk.mobads.QcAd;
import com.corpize.sdk.mobads.admanager.InsertManager;
import com.corpize.sdk.mobads.listener.InsertQcAdListener;

/**
 * 插屏广告位展示
 */
public class InsertAdActivity extends AppCompatActivity implements View.OnClickListener {

    private Button        mBtInsertAdd;
    private Button        mBtInsertDestroy;
    private InsertManager mInsertManager;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_ad);

        mBtInsertAdd = (Button) findViewById(R.id.bt_insert_add);
        mBtInsertDestroy = (Button) findViewById(R.id.bt_insert_destroy);
        mBtInsertAdd.setOnClickListener(this);
        mBtInsertDestroy.setOnClickListener(this);

    }

    @Override
    public void onClick (View v) {
        switch (v.getId()) {
            case R.id.bt_insert_add://加载插屏广告
                String adId = "";
                if (Constants.IS_AD_TEST) {
                    adId = "1716893D4E59C34734D1B5F9ADFE699C";
                } else {
                    adId = "E31A5C07F197778C7E2F0752C7F9BA97";
                }
                QcAd.get().insertAds(InsertAdActivity.this, adId, mListener);
                break;

            case R.id.bt_insert_destroy:
                if (mInsertManager != null) {
                    mInsertManager.destroyAd();
                }
                break;

            default:
                break;

        }

    }

    /**
     * 插屏回调
     */
    private InsertQcAdListener mListener = new InsertQcAdListener() {

        @Override
        public void onADManager (InsertManager manager) {
            Log.e("InsertAdActivity", "onADManager");
            mInsertManager = manager;
        }

        @Override
        public void onADReceive (InsertManager manager, String tag) {
            Log.e("InsertAdActivity", "onADReceive");
            manager.showAd(InsertAdActivity.this);
        }

        @Override
        public void onADExposure (String tag) {
            Log.e("InsertAdActivity", "onADExposure");
        }

        @Override
        public void onAdClicked (String tag) {
            Log.e("InsertAdActivity", "onAdClicked");
        }

        @Override
        public void onAdClose () {
            Log.e("InsertAdActivity", "onAdClose");
            //当前页只展示一次插屏广告的时候,建议清除广告
            if (mInsertManager != null) {
                mInsertManager.destroyAd();
            }
        }

        @Override
        public void onAdError (String tag, String fail) {
            Log.e("InsertAdActivity", "onAdError=" + fail);
        }
    };

    @Override
    protected void onDestroy () {
        super.onDestroy();
        //在合适的时候调用清除方法
        if (mInsertManager != null) {
            mInsertManager.destroyAd();
        }
    }


}
