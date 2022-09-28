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
 * description: 开屏广告
 */
public class SplashManager {

    private static SplashManager        sQcSpalshAd;
    private        CountDownTimer       mPostDownTime;      //同时发送的三个请求的倒计时
    private        ViewGroup            mSplashContainer;  //splash控件
    private        TextView             mSkipView;         //自定义按钮控件,广点通专用
    private        SplashQcAdListener   mListener;//回调
    private        CustomCountDownTimer mCountDownTime;

    //企创
    private String         mQcAdm;
    private AdResponseBean mAdBack;

    //广点通
    private long     fetchSplashADTime = 0;
    private SplashAD splashAD;

    //穿山甲
    private TTAdNative mTTAdNative;
    private TTSplashAd mCsjSplashAd;

    //百青藤
    private SplashAd mSplashAd;

    private boolean mHaveClick        = false;//是否发送点击曝光请求
    private boolean mHaveExposure     = false;//是否发送展示曝光请求
    private boolean mHaveDeepExposure = false;//是否发送deeplink曝光请求
    private boolean mHaveDownStart    = false;//是否发送开始下载曝光请求
    private boolean mHaveDownComplete = false;//是否发送完成下载曝光请求
    private boolean mHaveDownInstall  = false;//是否发送开始安装曝光请求
    private boolean isClickAd         = false; //企创广告是否点击
    private int     mWidth;
    private int     mHeight;

    /**
     * 单例模式
     */
    public static SplashManager get () {
        if (sQcSpalshAd == null) {
            sQcSpalshAd = new SplashManager();
        }
        return sQcSpalshAd;
    }

    /**
     * 初始化数据
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
     * 初始化数据
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
     * splash广告权重计算
     */
    public void initWeight (final Activity mActivity, ViewGroup splashContainer, TextView skipView,
            final AdidBean adsSdk, final String adId, SplashQcAdListener splashQCADListener) {
        initData();//初始化数据
        mSplashContainer = splashContainer;
        mSkipView = skipView;
        mListener = splashQCADListener;

        final AdidBean.SdkBean sdk = adsSdk.getSdk();

        int chuangshanjiaWeight = 0;    //穿山甲权重
        int tengxunWeight       = 0;    //广点通权重
        int baiWeight           = 0;    //百青藤权重
        int qcWeight            = adsSdk.getWeight();    //企创权重
        int ksWeight            = 0;

        if (sdk != null) {  //权重分配
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
        int weigth    = CommonUtils.getCompareRandow(allWeigth);// 1~allWeigth的任意整数
        LogUtils.i("csj=" + chuangshanjiaWeight + "gdt=" + tengxunWeight +
                "qc=" + qcWeight + "bqt=" + baiWeight + "current=" + weigth);

        if (allWeigth == 0) {//企创
            ToastUtils.show(mActivity, "qc");
            getQcAd(mActivity, adsSdk, adId, true);
        } else if (weigth <= chuangshanjiaWeight) {//穿山甲
            AdidBean.SdkBean.ChuangshanjiaBean chuangshanjia = sdk.getChuangshanjia();
            String                             csjAdid       = "";
            if (chuangshanjia != null) {
                csjAdid = Constants.IS_INIT_SIMPLE_DATA ? Constants.CSJSimpleData.SPLASH_AD_ID : chuangshanjia.getAdid();
            }

            ToastUtils.show(mActivity, "csj");
            getCjsAd(mActivity, csjAdid);
        } else if (weigth <= chuangshanjiaWeight + tengxunWeight) {//广点通
            AdidBean.SdkBean.TengxunBean tengxun  = sdk.getTengxun();
            String                       gdtPosId = "";
            if (tengxun != null) {
                gdtPosId = Constants.IS_INIT_SIMPLE_DATA ? Constants.GDTSimpleData.SPLASH_AD_ID : tengxun.getAdid();
            }

            ToastUtils.show(mActivity, "gdt");
            getGdtAd(mActivity, gdtPosId);
        } else if (weigth <= chuangshanjiaWeight + tengxunWeight + baiWeight) {//百青藤
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
        } else {//企创
            ToastUtils.show(mActivity, "qc");
            getQcAd(mActivity, adsSdk, adId, true);
        }
    }

    /**
     * 获取企创的ad
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
     * 获取广点通的ad
     */
    private void getGdtAd (final Activity mActivity, String adid) {
        /*仅限于和 DownloadService 的不在同一进程的 Activity*/
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

                String SKIP_TEXT = "跳过: %d";
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
                //在fetchAdOnly的情况下,表示广告拉取成功可以显示了

            }
        }, 0);


    }

    /**
     * 拉取开屏广告，开屏广告的构造方法有3种，详细说明请参考开发者文档。
     *
     * @param activity      展示广告的activity
     * @param adContainer   展示广告的大容器
     * @param skipContainer 自定义的跳过按钮：传入该view给SDK后，SDK会自动给它绑定点击跳过事件。SkipView的样式可以由开发者自由定制，其尺寸限制请参考activity_splash.xml或者接入文档中的说明。
     * @param posId         广告位ID
     * @param adListener    广告状态监听器
     * @param fetchDelay    拉取广告的超时时长：取值范围[3000, 5000]，设为0表示使用广点通SDK默认的超时时长。
     */
    private void fetchSplashAD (Activity activity, ViewGroup adContainer, View skipContainer,
            String posId, SplashADListener adListener, int fetchDelay) {
        fetchSplashADTime = System.currentTimeMillis();
        splashAD = new SplashAD(activity, skipContainer, posId, adListener, fetchDelay);
        splashAD.fetchAndShowIn(adContainer);

    }

    /**
     * 获取快手
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
                            //返回需要填充的广告的数量
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
     * 获取百青藤
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
                        //广告的结束返回
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
                        //跳过按钮的消失,跳过的消失
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

                // 如果开屏需要load广告和show广告分开，请参考类RSplashManagerActivity的写法
                // 如果需要修改开屏超时时间、隐藏工信部下载整改展示，请设置下面代码;
                final RequestParameters.Builder parameters = new RequestParameters.Builder();
                // sdk内部默认超时时间为4200，单位：毫秒
                parameters.addExtra(SplashAd.KEY_TIMEOUT, "4200");
                // sdk内部默认值为true
                parameters.addExtra(SplashAd.KEY_DISPLAY_DOWNLOADINFO, "true");
                // 是否限制点击区域，默认不限制
                parameters.addExtra(SplashAd.KEY_LIMIT_REGION_CLICK, "false");
                // 是否展示点击引导按钮，默认不展示，若设置可限制点击区域，则此选项默认打开
                parameters.addExtra(SplashAd.KEY_DISPLAY_CLICK_REGION, "true");
                // 用户点击开屏下载类广告时，是否弹出Dialog
                // 此选项设置为true的情况下，会覆盖掉 {SplashAd.KEY_DISPLAY_DOWNLOADINFO} 的设置
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
     * 获取穿山甲ad
     */
    private void getCjsAd (final Activity mActivity, final String codeId) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                mTTAdNative = TTAdManagerHolder.get().createAdNative(mActivity);
                //step3:创建开屏广告请求参数AdSlot,具体参数含义参考文档
                AdSlot adSlot = new AdSlot.Builder()
                        .setCodeId(codeId)
                        .setSupportDeepLink(true)
                        .setImageAcceptedSize(1080, 1920)
                        .build();

                //step4:请求广告，调用开屏广告异步请求接口，对请求回调的广告作渲染处理
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
     * 展示穿山甲ad
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
                //设置不开启开屏广告倒计时功能以及不显示跳过按钮,如果这么设置，您需要自定义倒计时逻辑
                //ad.setNotAllowSdkCountdown();
            }
        });

        //设置SplashView的交互监听器
        mCsjSplashAd.setSplashInteractionListener(new TTSplashAd.AdInteractionListener() {
            @Override
            public void onAdClicked (View view, int type) {
                LogUtils.i("onAdClicked:开屏广告点击");
                if (mListener != null) {
                    mListener.onAdClicked(ErrorUtil.CSJ);
                }
            }

            @Override
            public void onAdShow (View view, int type) {
                LogUtils.i("onADReceive:开屏广告展示");
                if (mListener != null) {
                    mListener.onADExposure("CSJ");
                }
            }

            @Override
            public void onAdSkip () {
                LogUtils.i("onAdSkip:开屏广告跳过");
                if (mListener != null) {
                    mListener.onADDismissed();
                }
            }

            @Override
            public void onAdTimeOver () {
                LogUtils.i("onAdTimeOver:开屏广告倒计时结束");
                if (mListener != null) {
                    mListener.onADDismissed();
                }
            }
        });

        //下载监听
        if (mCsjSplashAd.getInteractionType() == TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            mCsjSplashAd.setDownloadListener(new TTAppDownloadListener() {
                boolean hasShow = false;

                @Override
                public void onIdle () {
                }

                @Override
                public void onDownloadActive (long totalBytes, long currBytes, String fileName, String appName) {
                    if (!hasShow) {
                        LogUtils.d("下载中...");
                        hasShow = true;
                    }
                }

                @Override
                public void onDownloadPaused (long totalBytes, long currBytes, String fileName, String appName) {
                    LogUtils.d("下载暂停...");

                }

                @Override
                public void onDownloadFailed (long totalBytes, long currBytes, String fileName, String appName) {
                    LogUtils.d("下载失败...");

                }

                @Override
                public void onDownloadFinished (long totalBytes, String fileName, String appName) {
                    LogUtils.d("下载完成...");

                }

                @Override
                public void onInstalled (String fileName, String appName) {
                    LogUtils.d("安装完成...");
                }
            });
        }
    }

    /**
     * 展示企创banner的广告(webview加载或者原生加载)
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
                    //添加广告图片
                    WebView webView = WebViewUtils.initWebview(activity, mListener);
                    if (webView == null) {
                        //开始倒计时
                        starSkipViewTimeDown();
                        return;
                    }
                    relativeLayout.addView(webView);
                    WebViewUtils.addData(webView, webViewAdm, 202, width, height);
                    //添加广告的标识
                    TextView tvAd = new TextView(activity);
                    tvAd.setText("广告");
                    tvAd.setTextColor(Color.parseColor("#7EF0F0F0"));
                    tvAd.setTextSize(10);
                    tvAd.setPadding(15, 1, 15, 2);
                    relativeLayout.addView(tvAd);//当前页面加载ImageView

                    tvAd.setBackgroundColor(Color.parseColor("#4A666666"));
                    RelativeLayout.LayoutParams adParams = (RelativeLayout.LayoutParams) tvAd.getLayoutParams();
                    adParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
                    adParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
                    adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    adParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    //adParams.setMargins(0,0,0,DeviceUtil.dip2px(activity, 10));
                    tvAd.setLayoutParams(adParams);


                    //加入到app传递的控件中
                    mSplashContainer.removeAllViews();
                    mSplashContainer.addView(relativeLayout);

                    //按钮样式及倒计时
                    //mSkipView.setBackground(ThemeUtils.shapeDrawable(rootActivaty, 45, ThemeUtils.MAIN_COLOR));

                    //开始倒计时
                    starSkipViewTimeDown();
                }
            });
            return;
        }

        //原生广告
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                mSplashContainer.setVisibility(View.VISIBLE);
                mSkipView.setVisibility(View.VISIBLE);

                //创建父控件
                RelativeLayout relativeLayout = new RelativeLayout(activity);

                //1的时候是图片,2的时候是视频,不存在代表图片
                int       materialtype = mAdBack.getExt().getMaterialtype();
                ImageView imageView    = null;

                if (materialtype == 2) {
                    //展示视频
                    QcSplashAdView qcSplashAdView = new QcSplashAdView(activity, null, mAdBack, mSkipView, mListener);
                    relativeLayout.addView(qcSplashAdView);//当前页面加载ImageView

                } else {
                    //展示图片
                    imageView = new ImageView(activity);
                    relativeLayout.addView(imageView);
                }

                //添加广告的标识
                TextView tvAd = new TextView(activity);
                tvAd.setText("广告");
                tvAd.setTextColor(Color.parseColor("#7EF0F0F0"));
                tvAd.setTextSize(10);
                tvAd.setPadding(15, 1, 15, 2);
                relativeLayout.addView(tvAd);//当前页面加载ImageView

                tvAd.setBackgroundColor(Color.parseColor("#4A666666"));
                RelativeLayout.LayoutParams adParams = (RelativeLayout.LayoutParams) tvAd.getLayoutParams();
                adParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                adParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                adParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                //adParams.setMargins(0,0,0,DeviceUtil.dip2px(activity, 10));
                tvAd.setLayoutParams(adParams);

                //广告控件加载
                mSplashContainer.removeAllViews();
                mSplashContainer.addView(relativeLayout);

                mWidth = DeviceUtil.getScreenWidth(activity);
                mHeight = DeviceUtil.getScreenHeight(activity);

                if (materialtype != 2 && imageView != null) {
                    //设置图片的大小
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                    params.width = FrameLayout.LayoutParams.MATCH_PARENT;
                    params.height = FrameLayout.LayoutParams.MATCH_PARENT;
                    imageView.setLayoutParams(params);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);

                    Glide.with(activity).load(mAdBack.getExt().getIurl()).into(imageView);
                    //获取点击的坐标
                    getClickXYPosition(imageView);

                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick (View v) {
                            //isClickAd = true;
                            //点击监听
                            sendClickExposure(mAdBack.getExt().getClicktrackers());
                            if (mListener != null) {
                                mListener.onAdClicked(ErrorUtil.QC);
                            }
                            splashClickListener(activity, mAdBack.getExt());

                        }
                    });
                    //倒计时按钮
                    starSkipViewTimeDown();

                    //广告曝光
                    if (!mHaveExposure) {
                        mHaveExposure = true;
                        sendShowExposure(mAdBack.getExt().getImptrackers());
                    }
                }
            }
        });
    }

    /**
     * 右上方时间的倒计时
     */
    private void starSkipViewTimeDown () {
        if (mListener != null) {
            mListener.onADExposure("QC");
        }

        if (mCountDownTime != null) {
            mCountDownTime.cancel();
            mCountDownTime = null;
        }

        //LogUtils.d("获取当前的位置=" + currentPosition + "倒计时时间=" + distanceTime);
        mCountDownTime = new CustomCountDownTimer(4000, 1000) {
            @Override
            public void onTick (long millisUntilFinished) {
                LogUtils.d("倒计时时间=" + millisUntilFinished);

                long time = 0;
                if (millisUntilFinished > 0) {
                    time = millisUntilFinished / 1000;
                    if (millisUntilFinished % 1000 > 0) {
                        time = time + 1;
                    }
                    mSkipView.setText("跳过:" + (time + 1));
                } else {
                    mSkipView.setText("跳过:" + (time + 1));
                }
            }

            @Override
            public void onFinish () {
                mSkipView.setText("跳过:0");
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
     * 发送曝光,计算了宽高及时间戳
     */
    private void sendShowExposure (List<String> imgList) {
        long time = System.currentTimeMillis();

        if (imgList != null && imgList.size() > 0) {
            for (int i = 0; i < imgList.size(); i++) {
                String urlOld = imgList.get(i);
                String url    = urlOld;
                if (url.contains("__WIDTH__")) {//宽度替换
                    url = url.replace("__WIDTH__", mWidth + "");
                }
                if (url.contains("__HEIGHT__")) {//高度替换
                    url = url.replace("__HEIGHT__", mHeight + "");
                }
                if (url.contains("__TIME_STAMP__")) {//时间戳的替换
                    url = url.replace("__TIME_STAMP__", time + "");
                }

                QcHttpUtil.sendAdExposure(url);
            }
        }
    }


    private float mClickX;                     //企创 点击位置X
    private float mClickY;                     //企创 点击位置Y

    /**
     * onTouch()事件(企创广告)
     * 注意返回值
     * true： view继续响应Touch操作；
     * false：view不再响应Touch操作，故此处若为false，只能显示起始位置，不能显示实时位置和结束位置
     */
    public void getClickXYPosition (View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View view, MotionEvent event) {
                switch (event.getAction()) {
                    //点击的开始位置
                    case MotionEvent.ACTION_DOWN:
                        //tvTouchShowStart.setText("起始位置：(" + event.getX() + "," + event.getY());
                        mClickX = event.getX();
                        mClickY = event.getY();
                        break;
                    //触屏实时位置
                    case MotionEvent.ACTION_MOVE:
                        //tvTouchShow.setText("实时位置：(" + event.getX() + "," + event.getY());
                        break;
                    //离开屏幕的位置
                    case MotionEvent.ACTION_UP:
                        //tvTouchShow.setText("结束位置：(" + event.getX() + "," + event.getY());
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 广告位点击请求(企创广告)
     */
    public void sendClickExposure (final List<String> list) {
        if (!mHaveClick) {
            mHaveClick = true;

            if (list != null && list.size() > 0) {
                long time = System.currentTimeMillis();

                for (int i = 0; i < list.size(); i++) {
                    String urlOld = list.get(i);
                    String url    = urlOld;
                    if (url.contains("__DOWN_X__")) {//点击X轴的替换
                        url = url.replace("__DOWN_X__", mClickX + "");
                    }
                    if (url.contains("__DOWN_Y__")) {//点击Y轴的替换
                        url = url.replace("__DOWN_Y__", mClickY + "");
                    }
                    if (url.contains("__UP_X__")) {//抬起X轴的替换
                        url = url.replace("__UP_X__", mClickX + "");
                    }
                    if (url.contains("__UP_Y__")) {//抬起Y轴的替换
                        url = url.replace("__UP_Y__", mClickY + "");
                    }
                    if (url.contains("__WIDTH__")) {//宽度替换
                        url = url.replace("__WIDTH__", mWidth + "");
                    }
                    if (url.contains("__HEIGHT__")) {//高度替换
                        url = url.replace("__HEIGHT__", mHeight + "");
                    }
                    if (url.contains("__TIME_STAMP__")) {//时间戳的替换
                        url = url.replace("__TIME_STAMP__", time + "");
                    }

                    QcHttpUtil.sendAdExposure(url);
                }
            }
        }
    }

    /**
     * 企创 adtype 为201、202、203的Action Listener
     */
    private void splashClickListener (Activity rootActivaty, ExtBean extBean) {
        if (extBean == null) {
            return;
        }
        int action = extBean.getAction();
        if (0 == action) {   //0 - 未确认

        } else if (1 == action) {    // 1 - App webview 打开链接
            if (null != extBean.getClickurl()) {
                Intent intent = new Intent(rootActivaty, QcAdDetialActivity.class);
                intent.putExtra("url", extBean.getClickurl());
                rootActivaty.startActivity(intent);
            }

        } else if (2 == action) {     // 2 - 系统浏览器打开链接
            if (null != extBean.getClickurl()) {
                Uri    uri      = Uri.parse(extBean.getClickurl());
                Intent intent11 = new Intent(Intent.ACTION_VIEW, uri);
                rootActivaty.startActivity(intent11);
            }

        } else if (3 == action) {    // 3 - 打开地图
        } else if (4 == action) {    // 4 - 拨打电话
        } else if (5 == action) {    // 5 - 播放视频
        } else if (6 == action) {    // 6 - 下载APP
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
                        LogUtils.d("开始安装=");
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

        } else if (7 == action) {   // 7 - deeplink 链接
            String deeplink = extBean.getFallback();
            if (null != deeplink && ThirdAppUtils.openLinkApp(rootActivaty, deeplink)) {
                //发送deeplink的曝光
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
