package com.corpize.sdk.mobads.admanager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;

import com.baidu.mobads.sdk.api.RequestParameters;
import com.baidu.mobads.sdk.api.SplashAd;
import com.baidu.mobads.sdk.api.SplashInteractionListener;
import com.bumptech.glide.Glide;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTSplashAd;
import com.corpize.sdk.mobads.TTAdManagerHolder;
import com.corpize.sdk.mobads.bean.AdResponseBean;
import com.corpize.sdk.mobads.bean.AdidBean;
import com.corpize.sdk.mobads.bean.AssetsLinkEventtrackersBean;
import com.corpize.sdk.mobads.bean.ExtBean;
import com.corpize.sdk.mobads.common.CommonUtils;
import com.corpize.sdk.mobads.common.Constants;
import com.corpize.sdk.mobads.common.ErrorUtil;
import com.corpize.sdk.mobads.listener.SplashQcAdListener;
import com.corpize.sdk.mobads.utils.DeviceUtil;
import com.corpize.sdk.mobads.utils.LogUtils;
import com.corpize.sdk.mobads.utils.QcHttpUtil;
import com.corpize.sdk.mobads.utils.ToastUtils;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadInstaller;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadProgressCallBack;
import com.corpize.sdk.mobads.video.CustomCountDownTimer;
import com.corpize.sdk.mobads.video.ThirdAppUtils;
import com.corpize.sdk.mobads.view.QcAdDetialActivity;
import com.corpize.sdk.mobads.view.QcSplashAdView;
import com.corpize.sdk.mobads.view.WebViewUtils;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.KsSplashScreenAd;
import com.qq.e.ads.cfg.MultiProcessFlag;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;

import java.util.List;

/**
 * author: yh
 * date: 2020-02-12 04:67
 * description: ????????????
 */
public class SplashManager {

    private static SplashManager        sQcSpalshAd;
    private        CountDownTimer       mPostDownTime;      //???????????????????????????????????????
    private        ViewGroup            mSplashContainer;  //splash??????
    private        TextView             mSkipView;         //?????????????????????,???????????????
    private        SplashQcAdListener   mListener;//??????
    private        CustomCountDownTimer mCountDownTime;

    //??????
    private String         mQcAdm;
    private AdResponseBean mAdBack;

    //?????????
    private long     fetchSplashADTime = 0;
    private SplashAD splashAD;

    //?????????
    private TTAdNative mTTAdNative;
    private TTSplashAd mCsjSplashAd;

    //?????????
    private SplashAd mSplashAd;

    private boolean mHaveClick        = false;//??????????????????????????????
    private boolean mHaveExposure     = false;//??????????????????????????????
    private boolean mHaveDeepExposure = false;//????????????deeplink????????????
    private boolean mHaveDownStart    = false;//????????????????????????????????????
    private boolean mHaveDownComplete = false;//????????????????????????????????????
    private boolean mHaveDownInstall  = false;//????????????????????????????????????
    private boolean isClickAd         = false; //????????????????????????
    private int     mWidth;
    private int     mHeight;

    /**
     * ????????????
     */
    public static SplashManager get () {
        if (sQcSpalshAd == null) {
            sQcSpalshAd = new SplashManager();
        }
        return sQcSpalshAd;
    }

    /**
     * ???????????????
     */
    private void initData () {
        mHaveClick = false;
        mHaveExposure = false;
        mHaveDeepExposure = false;
        mHaveDownStart = false;
        mHaveDownComplete = false;
        mHaveDownInstall = false;
    }

    /**
     * ???????????????
     */
    public void destroyAd () {
        //qc
        if (mCountDownTime != null) {
            mCountDownTime.cancel();
            mCountDownTime = null;
        }

        //gdt
        splashAD = null;

        //csj
        mTTAdNative = null;
        mCsjSplashAd = null;

        sQcSpalshAd = null;
    }

    /**
     * splash??????????????????
     */
    public void initWeight (final Activity mActivity, ViewGroup splashContainer, TextView skipView,
            final AdidBean adsSdk, final String adId, SplashQcAdListener splashQCADListener) {
        initData();//???????????????
        mSplashContainer = splashContainer;
        mSkipView = skipView;
        mListener = splashQCADListener;

        final AdidBean.SdkBean sdk = adsSdk.getSdk();

        int chuangshanjiaWeight = 0;    //???????????????
        int tengxunWeight       = 0;    //???????????????
        int baiWeight           = 0;    //???????????????
        int qcWeight            = adsSdk.getWeight();    //????????????
        int ksWeight            = 0;

        if (sdk != null) {  //????????????
            AdidBean.SdkBean.TengxunBean tengxun = sdk.getTengxun();
            if (tengxun != null) {
                tengxunWeight = tengxun.getWeight();
            }
            AdidBean.SdkBean.ChuangshanjiaBean chuangshanjia = sdk.getChuangshanjia();
            if (chuangshanjia != null) {
                chuangshanjiaWeight = chuangshanjia.getWeight();
            }
            AdidBean.SdkBean.BaiQingTeng bai = sdk.getBai();
            if (bai != null) {
                baiWeight = bai.getWeight();
            }
            AdidBean.SdkBean.KuaiShouBean ks = sdk.getKuaishou();
            if (ks != null) {
                ksWeight = ks.getWeight();
            }
        }

        int allWeigth = chuangshanjiaWeight + tengxunWeight + qcWeight + baiWeight + ksWeight;
        int weigth    = CommonUtils.getCompareRandow(allWeigth);// 1~allWeigth???????????????
        LogUtils.i("csj=" + chuangshanjiaWeight + "gdt=" + tengxunWeight +
                "qc=" + qcWeight + "bqt=" + baiWeight + "current=" + weigth);

        if (allWeigth == 0) {//??????
            ToastUtils.show(mActivity, "qc");
            getQcAd(mActivity, adsSdk, adId, true);
        } else if (weigth <= chuangshanjiaWeight) {//?????????
            AdidBean.SdkBean.ChuangshanjiaBean chuangshanjia = sdk.getChuangshanjia();
            String                             csjAdid       = "";
            if (chuangshanjia != null) {
                csjAdid = Constants.IS_INIT_SIMPLE_DATA ? Constants.CSJSimpleData.SPLASH_AD_ID : chuangshanjia.getAdid();
            }

            ToastUtils.show(mActivity, "csj");
            getCjsAd(mActivity, csjAdid);
        } else if (weigth <= chuangshanjiaWeight + tengxunWeight) {//?????????
            AdidBean.SdkBean.TengxunBean tengxun  = sdk.getTengxun();
            String                       gdtPosId = "";
            if (tengxun != null) {
                gdtPosId = Constants.IS_INIT_SIMPLE_DATA ? Constants.GDTSimpleData.SPLASH_AD_ID : tengxun.getAdid();
            }

            ToastUtils.show(mActivity, "gdt");
            getGdtAd(mActivity, gdtPosId);
        } else if (weigth <= chuangshanjiaWeight + tengxunWeight + baiWeight) {//?????????
            ToastUtils.show(mActivity, "bqt");
            AdidBean.SdkBean.BaiQingTeng bai = sdk.getBai();
            if (bai != null) {
                String baiduAdId = Constants.IS_INIT_SIMPLE_DATA ? Constants.BaiduSimpleData.SPLASH_AD_ID : bai.getAdid();
                getBqtAd(mActivity, baiduAdId);
            }
        } else if (weigth <= chuangshanjiaWeight + tengxunWeight + baiWeight + ksWeight) {
            ToastUtils.show(mActivity, "ks");
            AdidBean.SdkBean.KuaiShouBean ksBean = sdk.getKuaishou();
            if (null != ksBean) {
                Long ksAdId = Constants.IS_INIT_SIMPLE_DATA ? Constants.KSSimpleData.KS_SPLASH_POSID : Long.valueOf(ksBean.getAdid());
                getKsAd(mActivity, splashContainer, ksAdId);
            }
        } else {//??????
            ToastUtils.show(mActivity, "qc");
            getQcAd(mActivity, adsSdk, adId, true);
        }
    }

    /**
     * ???????????????ad
     */
    private void getQcAd (final Activity activity, AdidBean adsSdk, String adId, final boolean isShow) {
        QcHttpUtil.getAd(activity, adsSdk, adId, new QcHttpUtil.QcHttpOnListener<AdResponseBean>() {
            @Override
            public void OnQcCompletionListener (AdResponseBean response) {
                if (response != null && response.getStatus() == 200) {
                    final String adm = response.getAdm();
                    ExtBean      ext = response.getExt();
                    if (!TextUtils.isEmpty(adm)) {
                        //mHaveQC = true;
                        mAdBack = response;
                    }

                    if (ext != null && ext.getIurl() != null) {
                        //mHaveQC = true;
                        mAdBack = response;
                    }

                    if (isShow) {
                        showQcAd(activity);
                    }
                } else {
                    if (isShow) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run () {
                                if (mListener != null) {
                                    mListener.onAdError(ErrorUtil.GDT, ErrorUtil.NOAD);
                                }
                            }
                        });

                    }
                }
            }

            @Override
            public void OnQcErrorListener (final String erro, final int code) {
                if (isShow) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run () {
                            if (mListener != null) {
                                mListener.onAdError(ErrorUtil.GDT, ErrorUtil.NOAD + code + erro);
                            }
                        }
                    });

                }
            }
        });
    }

    /**
     * ??????????????????ad
     */
    private void getGdtAd (final Activity mActivity, String adid) {
        /*???????????? DownloadService ???????????????????????? Activity*/
        MultiProcessFlag.setMultiProcess(true);
        fetchSplashAD(mActivity, mSplashContainer, mSkipView, adid, new SplashADListener() {
            @Override
            public void onADDismissed () {
                LogUtils.i("onADDismissed");
                mSkipView.setVisibility(View.VISIBLE);
                if (mListener != null) {
                    mListener.onADDismissed();
                }
            }

            @Override
            public void onNoAD (AdError adError) {
                LogUtils.i("onNoAD");
                mSkipView.setVisibility(View.GONE);
                if (mListener != null) {
                    mListener.onADManager(sQcSpalshAd);
                    mListener.onAdError(ErrorUtil.GDT, adError.getErrorMsg() + "..." + adError.getErrorCode());
                }
            }

            @Override
            public void onADPresent () {
                LogUtils.i("onADPresent");
                mSkipView.setVisibility(View.VISIBLE);
                if (mListener != null) {
                    mListener.onADAdd(ErrorUtil.GDT);
                }
            }

            @Override
            public void onADClicked () {
                LogUtils.i("onADClicked");
                if (mListener != null) {
                    mListener.onAdClicked(ErrorUtil.GDT);
                }
            }

            @Override
            public void onADTick (long l) {
                LogUtils.i("onADTick:" + l);
                mSkipView.setVisibility(View.VISIBLE);

                String SKIP_TEXT = "??????: %d";
                mSkipView.setText(String.format(SKIP_TEXT, Math.round(l / 1000f)));
            }

            @Override
            public void onADExposure () {
                LogUtils.i("onADExposure");
                if (mListener != null) {
                    mListener.onADExposure(ErrorUtil.GDT);
                }
            }

            @Override
            public void onADLoaded (long l) {
                //???fetchAdOnly????????????,???????????????????????????????????????

            }
        }, 0);


    }

    /**
     * ???????????????????????????????????????????????????3?????????????????????????????????????????????
     *
     * @param activity      ???????????????activity
     * @param adContainer   ????????????????????????
     * @param skipContainer ????????????????????????????????????view???SDK??????SDK??????????????????????????????????????????SkipView??????????????????????????????????????????????????????????????????activity_splash.xml?????????????????????????????????
     * @param posId         ?????????ID
     * @param adListener    ?????????????????????
     * @param fetchDelay    ??????????????????????????????????????????[3000, 5000]?????????0?????????????????????SDK????????????????????????
     */
    private void fetchSplashAD (Activity activity, ViewGroup adContainer, View skipContainer,
            String posId, SplashADListener adListener, int fetchDelay) {
        fetchSplashADTime = System.currentTimeMillis();
        splashAD = new SplashAD(activity, skipContainer, posId, adListener, fetchDelay);
        splashAD.fetchAndShowIn(adContainer);

    }

    /**
     * ????????????
     */
    public void getKsAd (final Activity activity, final ViewGroup splashContainer, final long posId) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                KsScene scene = new KsScene.Builder(posId).build();
                if (KsAdSDK.getLoadManager() != null) {
                    KsAdSDK.getLoadManager().loadSplashScreenAd(scene, new KsLoadManager.SplashScreenAdListener() {
                        @Override
                        public void onError (int i, String s) {
                            if (null != mListener) {
                                mListener.onADManager(sQcSpalshAd);
                                mListener.onAdError(ErrorUtil.KS, s);
                            }
                        }

                        @Override
                        public void onRequestResult (int i) {
                            //????????????????????????????????????
                        }

                        @Override
                        public void onSplashScreenAdLoad (@Nullable KsSplashScreenAd ksSplashScreenAd) {
                            View adView = ksSplashScreenAd.getView(activity, new KsSplashScreenAd.SplashScreenAdInteractionListener() {
                                @Override
                                public void onAdClicked () {
                                    if (mListener != null) {
                                        mListener.onAdClicked(ErrorUtil.KS);
                                    }
                                }

                                @Override
                                public void onAdShowError (int i, String s) {
                                    if (mListener != null) {
                                        mListener.onAdError(ErrorUtil.KS, s);
                                    }
                                }

                                @Override
                                public void onAdShowEnd () {
                                    if (mListener != null) {
                                        mListener.onADDismissed();
                                    }
                                }

                                @Override
                                public void onAdShowStart () {
                                    if (mListener != null) {
                                        mListener.onADAdd(ErrorUtil.KS);
                                    }
                                }

                                @Override
                                public void onSkippedAd () {
                                    if (mListener != null) {
                                        mListener.onADDismissed();
                                    }
                                }
                            });

                            if (splashContainer != null) {
                                splashContainer.addView(adView);
                            }

                            if (mListener != null) {
                                mListener.onADExposure(ErrorUtil.KS);
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * ???????????????
     */
    public void getBqtAd (final Activity activity, final String adid) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                SplashInteractionListener splashInteractionListener = new SplashInteractionListener() {
                    @Override
                    public void onADLoaded () {
                        LogUtils.d("onADLoaded");
                        mSplashAd.show(mSplashContainer);
                    }

                    @Override
                    public void onAdFailed (String s) {
                        LogUtils.d("onAdFailed" + s);
                        if (mListener != null) {
                            mListener.onADManager(sQcSpalshAd);
                            mListener.onAdError(ErrorUtil.BQT, s);
                        }
                    }

                    @Override
                    public void onLpClosed () {
                        //?????????????????????
                        LogUtils.d("onLpClosed");
                        if (mListener != null) {
                            mListener.onADDismissed();
                        }
                    }

                    @Override
                    public void onAdPresent () {
                        LogUtils.d("onAdPresent");
                        if (mListener != null) {
                            mListener.onADExposure(ErrorUtil.BQT);
                        }
                    }

                    @Override
                    public void onAdDismissed () {
                        //?????????????????????,???????????????
                        LogUtils.d("onAdDismissed");
                        if (mListener != null) {
                            mListener.onADDismissed();
                        }
                    }

                    @Override
                    public void onAdClick () {
                        LogUtils.d("onAdClick");
                        if (mListener != null) {
                            mListener.onAdClicked(ErrorUtil.BQT);
                        }
                    }
                };

                // ??????????????????load?????????show???????????????????????????RSplashManagerActivity?????????
                // ????????????????????????????????????????????????????????????????????????????????????????????????;
                final RequestParameters.Builder parameters = new RequestParameters.Builder();
                // sdk???????????????????????????4200??????????????????
                parameters.addExtra(SplashAd.KEY_TIMEOUT, "4200");
                // sdk??????????????????true
                parameters.addExtra(SplashAd.KEY_DISPLAY_DOWNLOADINFO, "true");
                // ??????????????????????????????????????????
                parameters.addExtra(SplashAd.KEY_LIMIT_REGION_CLICK, "false");
                // ????????????????????????????????????????????????????????????????????????????????????????????????????????????
                parameters.addExtra(SplashAd.KEY_DISPLAY_CLICK_REGION, "true");
                // ???????????????????????????????????????????????????Dialog
                // ??????????????????true??????????????????????????? {SplashAd.KEY_DISPLAY_DOWNLOADINFO} ?????????
                parameters.addExtra(SplashAd.KEY_POPDIALOG_DOWNLOAD, "true");
                mSplashAd = new SplashAd(activity, adid, parameters.build(), splashInteractionListener);

                mSplashAd.setDownloadDialogListener(new SplashAd.SplashAdDownloadDialogListener() {
                    @Override
                    public void adDownloadWindowShow () {
                        LogUtils.d("adDownloadWindowShow");
                    }

                    @Override
                    public void adDownloadWindowClose () {
                        LogUtils.d("adDownloadWindowClose");
                    }

                    @Override
                    public void onADPrivacyLpShow () {
                        LogUtils.d("onADPrivacyLpShow");
                    }

                    @Override
                    public void onADPrivacyLpClose () {
                        LogUtils.d("onADPrivacyLpClose");
                    }

                    @Override
                    public void onADPermissionShow () {
                        LogUtils.d("onADPermissionShow");
                    }

                    @Override
                    public void onADPermissionClose () {
                        LogUtils.d("onADPermissionClose");
                    }
                });
                mSplashAd.load();
            }
        });
    }

    /**
     * ???????????????ad
     */
    private void getCjsAd (final Activity mActivity, final String codeId) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                mTTAdNative = TTAdManagerHolder.get().createAdNative(mActivity);
                //step3:??????????????????????????????AdSlot,??????????????????????????????
                AdSlot adSlot = new AdSlot.Builder()
                        .setCodeId(codeId)
                        .setSupportDeepLink(true)
                        .setImageAcceptedSize(1080, 1920)
                        .build();

                //step4:?????????????????????????????????????????????????????????????????????????????????????????????
                mTTAdNative.loadSplashAd(adSlot, new TTAdNative.SplashAdListener() {
                    @Override
                    @MainThread
                    public void onError (int i, String message) {
                        LogUtils.i("onError:" + message);
                        if (mListener != null) {
                            mListener.onADManager(sQcSpalshAd);
                            mListener.onAdError(ErrorUtil.CSJ, message);
                        }
                    }

                    @Override
                    @MainThread
                    public void onTimeout () {
                        LogUtils.i("onTimeout");
                        if (mListener != null) {
                            mListener.onAdError(ErrorUtil.CSJ, "OnTimeout");
                        }

                    }

                    @Override
                    @MainThread
                    public void onSplashAdLoad (TTSplashAd ad) {
                        mCsjSplashAd = ad;
                        showCsjAd(mActivity);
                    }
                }, 2000);
            }
        });
    }

    /**
     * ???????????????ad
     */
    private void showCsjAd (Activity activity) {
        if (mCsjSplashAd == null) {
            return;
        }

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                View splashView = mCsjSplashAd.getSplashView();
                mSplashContainer.removeAllViews();
                mSplashContainer.addView(splashView);
                if (mListener != null) {
                    mListener.onADAdd(ErrorUtil.CSJ);
                }
                //?????????????????????????????????????????????????????????????????????,??????????????????????????????????????????????????????
                //ad.setNotAllowSdkCountdown();
            }
        });

        //??????SplashView??????????????????
        mCsjSplashAd.setSplashInteractionListener(new TTSplashAd.AdInteractionListener() {
            @Override
            public void onAdClicked (View view, int type) {
                LogUtils.i("onAdClicked:??????????????????");
                if (mListener != null) {
                    mListener.onAdClicked(ErrorUtil.CSJ);
                }
            }

            @Override
            public void onAdShow (View view, int type) {
                LogUtils.i("onADReceive:??????????????????");
                if (mListener != null) {
                    mListener.onADExposure("CSJ");
                }
            }

            @Override
            public void onAdSkip () {
                LogUtils.i("onAdSkip:??????????????????");
                if (mListener != null) {
                    mListener.onADDismissed();
                }
            }

            @Override
            public void onAdTimeOver () {
                LogUtils.i("onAdTimeOver:???????????????????????????");
                if (mListener != null) {
                    mListener.onADDismissed();
                }
            }
        });

        //????????????
        if (mCsjSplashAd.getInteractionType() == TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            mCsjSplashAd.setDownloadListener(new TTAppDownloadListener() {
                boolean hasShow = false;

                @Override
                public void onIdle () {
                }

                @Override
                public void onDownloadActive (long totalBytes, long currBytes, String fileName, String appName) {
                    if (!hasShow) {
                        LogUtils.d("?????????...");
                        hasShow = true;
                    }
                }

                @Override
                public void onDownloadPaused (long totalBytes, long currBytes, String fileName, String appName) {
                    LogUtils.d("????????????...");

                }

                @Override
                public void onDownloadFailed (long totalBytes, long currBytes, String fileName, String appName) {
                    LogUtils.d("????????????...");

                }

                @Override
                public void onDownloadFinished (long totalBytes, String fileName, String appName) {
                    LogUtils.d("????????????...");

                }

                @Override
                public void onInstalled (String fileName, String appName) {
                    LogUtils.d("????????????...");
                }
            });
        }
    }

    /**
     * ????????????banner?????????(webview????????????????????????)
     */
    private void showQcAd (final Activity activity) {
        final String webViewAdm = mAdBack.getAdm();
        if (!TextUtils.isEmpty(webViewAdm)) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    mSplashContainer.setVisibility(View.VISIBLE);
                    mSkipView.setVisibility(View.VISIBLE);
                    int width  = mSplashContainer.getWidth();
                    int height = mSplashContainer.getHeight();

                    RelativeLayout relativeLayout = new RelativeLayout(activity);
                    relativeLayout.setBackgroundColor(Color.WHITE);
                    //??????????????????
                    WebView webView = WebViewUtils.initWebview(activity, mListener);
                    if (webView == null) {
                        //???????????????
                        starSkipViewTimeDown();
                        return;
                    }
                    relativeLayout.addView(webView);
                    WebViewUtils.addData(webView, webViewAdm, 202, width, height);
                    //?????????????????????
                    TextView tvAd = new TextView(activity);
                    tvAd.setText("??????");
                    tvAd.setTextColor(Color.parseColor("#7EF0F0F0"));
                    tvAd.setTextSize(10);
                    tvAd.setPadding(15, 1, 15, 2);
                    relativeLayout.addView(tvAd);//??????????????????ImageView

                    tvAd.setBackgroundColor(Color.parseColor("#4A666666"));
                    RelativeLayout.LayoutParams adParams = (RelativeLayout.LayoutParams) tvAd.getLayoutParams();
                    adParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
                    adParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
                    adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    adParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    //adParams.setMargins(0,0,0,DeviceUtil.dip2px(activity, 10));
                    tvAd.setLayoutParams(adParams);


                    //?????????app??????????????????
                    mSplashContainer.removeAllViews();
                    mSplashContainer.addView(relativeLayout);

                    //????????????????????????
                    //mSkipView.setBackground(ThemeUtils.shapeDrawable(rootActivaty, 45, ThemeUtils.MAIN_COLOR));

                    //???????????????
                    starSkipViewTimeDown();
                }
            });
            return;
        }

        //????????????
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                mSplashContainer.setVisibility(View.VISIBLE);
                mSkipView.setVisibility(View.VISIBLE);

                //???????????????
                RelativeLayout relativeLayout = new RelativeLayout(activity);

                //1??????????????????,2??????????????????,?????????????????????
                int       materialtype = mAdBack.getExt().getMaterialtype();
                ImageView imageView    = null;

                if (materialtype == 2) {
                    //????????????
                    QcSplashAdView qcSplashAdView = new QcSplashAdView(activity, null, mAdBack, mSkipView, mListener);
                    relativeLayout.addView(qcSplashAdView);//??????????????????ImageView

                } else {
                    //????????????
                    imageView = new ImageView(activity);
                    relativeLayout.addView(imageView);
                }

                //?????????????????????
                TextView tvAd = new TextView(activity);
                tvAd.setText("??????");
                tvAd.setTextColor(Color.parseColor("#7EF0F0F0"));
                tvAd.setTextSize(10);
                tvAd.setPadding(15, 1, 15, 2);
                relativeLayout.addView(tvAd);//??????????????????ImageView

                tvAd.setBackgroundColor(Color.parseColor("#4A666666"));
                RelativeLayout.LayoutParams adParams = (RelativeLayout.LayoutParams) tvAd.getLayoutParams();
                adParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                adParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                adParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                //adParams.setMargins(0,0,0,DeviceUtil.dip2px(activity, 10));
                tvAd.setLayoutParams(adParams);

                //??????????????????
                mSplashContainer.removeAllViews();
                mSplashContainer.addView(relativeLayout);

                mWidth = DeviceUtil.getScreenWidth(activity);
                mHeight = DeviceUtil.getScreenHeight(activity);

                if (materialtype != 2 && imageView != null) {
                    //?????????????????????
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                    params.width = FrameLayout.LayoutParams.MATCH_PARENT;
                    params.height = FrameLayout.LayoutParams.MATCH_PARENT;
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                    Glide.with(activity).load(mAdBack.getExt().getIurl()).into(imageView);
                    //?????????????????????
                    getClickXYPosition(imageView);

                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick (View v) {
                            //isClickAd = true;
                            //????????????
                            sendClickExposure(mAdBack.getExt().getClicktrackers());
                            if (mListener != null) {
                                mListener.onAdClicked(ErrorUtil.QC);
                            }
                            splashClickListener(activity, mAdBack.getExt());

                        }
                    });
                    //???????????????
                    starSkipViewTimeDown();

                    //????????????
                    if (!mHaveExposure) {
                        mHaveExposure = true;
                        sendShowExposure(mAdBack.getExt().getImptrackers());
                    }
                }
            }
        });
    }

    /**
     * ???????????????????????????
     */
    private void starSkipViewTimeDown () {
        if (mListener != null) {
            mListener.onADExposure("QC");
        }

        if (mCountDownTime != null) {
            mCountDownTime.cancel();
            mCountDownTime = null;
        }

        //LogUtils.d("?????????????????????=" + currentPosition + "???????????????=" + distanceTime);
        mCountDownTime = new CustomCountDownTimer(4000, 1000) {
            @Override
            public void onTick (long millisUntilFinished) {
                LogUtils.d("???????????????=" + millisUntilFinished);

                long time = 0;
                if (millisUntilFinished > 0) {
                    time = millisUntilFinished / 1000;
                    if (millisUntilFinished % 1000 > 0) {
                        time = time + 1;
                    }
                    mSkipView.setText("??????:" + (time + 1));
                } else {
                    mSkipView.setText("??????:" + (time + 1));
                }
            }

            @Override
            public void onFinish () {
                mSkipView.setText("??????:0");
                if (!isClickAd) {
                    if (mListener != null) {
                        mListener.onADDismissed();
                    }
                }

                mCountDownTime = null;
            }
        };

        mCountDownTime.start();

        mSkipView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if (mListener != null) {
                    mListener.onADDismissed();
                }

                if (mCountDownTime != null) {
                    mCountDownTime.cancel();
                    mCountDownTime = null;
                }
            }
        });
    }

    /**
     * ????????????,???????????????????????????
     */
    private void sendShowExposure (List<String> imgList) {
        long time = System.currentTimeMillis();

        if (imgList != null && imgList.size() > 0) {
            for (int i = 0; i < imgList.size(); i++) {
                String urlOld = imgList.get(i);
                String url    = urlOld;
                if (url.contains("__WIDTH__")) {//????????????
                    url = url.replace("__WIDTH__", mWidth + "");
                }
                if (url.contains("__HEIGHT__")) {//????????????
                    url = url.replace("__HEIGHT__", mHeight + "");
                }
                if (url.contains("__TIME_STAMP__")) {//??????????????????
                    url = url.replace("__TIME_STAMP__", time + "");
                }

                QcHttpUtil.sendAdExposure(url);
            }
        }
    }


    private float mClickX;                     //?????? ????????????X
    private float mClickY;                     //?????? ????????????Y

    /**
     * onTouch()??????(????????????)
     * ???????????????
     * true??? view????????????Touch?????????
     * false???view????????????Touch????????????????????????false?????????????????????????????????????????????????????????????????????
     */
    public void getClickXYPosition (View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View view, MotionEvent event) {
                switch (event.getAction()) {
                    //?????????????????????
                    case MotionEvent.ACTION_DOWN:
                        //tvTouchShowStart.setText("???????????????(" + event.getX() + "," + event.getY());
                        mClickX = event.getX();
                        mClickY = event.getY();
                        break;
                    //??????????????????
                    case MotionEvent.ACTION_MOVE:
                        //tvTouchShow.setText("???????????????(" + event.getX() + "," + event.getY());
                        break;
                    //?????????????????????
                    case MotionEvent.ACTION_UP:
                        //tvTouchShow.setText("???????????????(" + event.getX() + "," + event.getY());
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    /**
     * ?????????????????????(????????????)
     */
    public void sendClickExposure (final List<String> list) {
        if (!mHaveClick) {
            mHaveClick = true;

            if (list != null && list.size() > 0) {
                long time = System.currentTimeMillis();

                for (int i = 0; i < list.size(); i++) {
                    String urlOld = list.get(i);
                    String url    = urlOld;
                    if (url.contains("__DOWN_X__")) {//??????X????????????
                        url = url.replace("__DOWN_X__", mClickX + "");
                    }
                    if (url.contains("__DOWN_Y__")) {//??????Y????????????
                        url = url.replace("__DOWN_Y__", mClickY + "");
                    }
                    if (url.contains("__UP_X__")) {//??????X????????????
                        url = url.replace("__UP_X__", mClickX + "");
                    }
                    if (url.contains("__UP_Y__")) {//??????Y????????????
                        url = url.replace("__UP_Y__", mClickY + "");
                    }
                    if (url.contains("__WIDTH__")) {//????????????
                        url = url.replace("__WIDTH__", mWidth + "");
                    }
                    if (url.contains("__HEIGHT__")) {//????????????
                        url = url.replace("__HEIGHT__", mHeight + "");
                    }
                    if (url.contains("__TIME_STAMP__")) {//??????????????????
                        url = url.replace("__TIME_STAMP__", time + "");
                    }

                    QcHttpUtil.sendAdExposure(url);
                }
            }
        }
    }

    /**
     * ?????? adtype ???201???202???203???Action Listener
     */
    private void splashClickListener (Activity rootActivaty, ExtBean extBean) {
        if (extBean == null) {
            return;
        }
        int action = extBean.getAction();
        if (0 == action) {   //0 - ?????????

        } else if (1 == action) {    // 1 - App webview ????????????
            if (null != extBean.getClickurl()) {
                Intent intent = new Intent(rootActivaty, QcAdDetialActivity.class);
                intent.putExtra("url", extBean.getClickurl());
                rootActivaty.startActivity(intent);
            }

        } else if (2 == action) {     // 2 - ???????????????????????????
            if (null != extBean.getClickurl()) {
                Uri    uri      = Uri.parse(extBean.getClickurl());
                Intent intent11 = new Intent(Intent.ACTION_VIEW, uri);
                rootActivaty.startActivity(intent11);
            }

        } else if (3 == action) {    // 3 - ????????????
        } else if (4 == action) {    // 4 - ????????????
        } else if (5 == action) {    // 5 - ????????????
        } else if (6 == action) {    // 6 - ??????APP
            if (null != extBean.getDfn()) {
                final AssetsLinkEventtrackersBean eventtrackers = extBean.getEventtrackers();
                new DownloadInstaller(rootActivaty, extBean.getDfn(), new DownloadProgressCallBack() {
                    @Override
                    public void downloadProgress (int progress) {
                        if (!mHaveDownStart && eventtrackers != null) {
                            mHaveDownStart = true;
                            sendShowExposure(eventtrackers.getStartdownload());
                        }
                        if (progress == 100) {
                            if (!mHaveDownComplete && eventtrackers != null) {
                                mHaveDownComplete = true;
                                sendShowExposure(eventtrackers.getCompletedownload());
                            }
                        }
                    }

                    @Override
                    public void downloadException (Exception e) {
                    }

                    @Override
                    public void onInstallStart () {
                        LogUtils.d("????????????=");
                        if (!mHaveDownInstall && eventtrackers != null) {
                            mHaveDownInstall = true;
                            sendShowExposure(eventtrackers.getStartinstall());
                        }

                        if (!mHaveDownStart && eventtrackers != null) {
                            mHaveDownStart = true;
                            sendShowExposure(eventtrackers.getStartdownload());
                        }

                        if (!mHaveDownComplete && eventtrackers != null) {
                            mHaveDownComplete = true;
                            sendShowExposure(eventtrackers.getCompletedownload());
                        }
                    }
                }).start();

            }

        } else if (7 == action) {   // 7 - deeplink ??????
            String deeplink = extBean.getFallback();
            if (null != deeplink && ThirdAppUtils.openLinkApp(rootActivaty, deeplink)) {
                //??????deeplink?????????
                if (!mHaveDeepExposure) {
                    mHaveDeepExposure = true;
                    sendShowExposure(extBean.getFallbacktrackers());
                }

            } else {
                if (null != extBean.getClickurl()) {
                    Uri    uri      = Uri.parse(extBean.getClickurl());
                    Intent intent11 = new Intent(Intent.ACTION_VIEW, uri);
                    rootActivaty.startActivity(intent11);
                }
            }

        } else {
            if (null != extBean.getClickurl()) {
                Uri    uri      = Uri.parse(extBean.getClickurl());
                Intent intent11 = new Intent(Intent.ACTION_VIEW, uri);
                rootActivaty.startActivity(intent11);
            }
        }
    }
}
