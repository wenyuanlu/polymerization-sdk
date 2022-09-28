package com.corpize.sdk.mobads.admanager;

import android.app.Activity;
import android.graphics.Point;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.baidu.mobads.sdk.api.AdView;
import com.baidu.mobads.sdk.api.AdViewListener;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.corpize.sdk.mobads.TTAdManagerHolder;
import com.corpize.sdk.mobads.bean.AdResponseBean;
import com.corpize.sdk.mobads.bean.AdidBean;
import com.corpize.sdk.mobads.bean.ExtBean;
import com.corpize.sdk.mobads.common.CommonUtils;
import com.corpize.sdk.mobads.common.Constants;
import com.corpize.sdk.mobads.common.ErrorUtil;
import com.corpize.sdk.mobads.listener.BannerQcAdListener;
import com.corpize.sdk.mobads.utils.DeviceUtil;
import com.corpize.sdk.mobads.utils.LogUtils;
import com.corpize.sdk.mobads.utils.QcHttpUtil;
import com.corpize.sdk.mobads.utils.ToastUtils;
import com.corpize.sdk.mobads.view.QcBannerAdView;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsFeedAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;
import com.qq.e.ads.banner2.UnifiedBannerADListener;
import com.qq.e.ads.banner2.UnifiedBannerView;
import com.qq.e.ads.cfg.MultiProcessFlag;
import com.qq.e.comm.util.AdError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * author: yh
 * date: 2020-02-11 23:35
 * description: Banner广告的管理
 */
public class BannerManager {

    private static BannerManager      sQcBannerAd;
    private        BannerQcAdListener mListener;
    private        ViewGroup          mContainer;
    private        CountDownTimer     mPostDownTime;      //同时发送的三个请求的倒计时
    private        float              mAdWigth = 0;       //请求的广告的宽度,单位dp,
    private        AdidBean           mAdidBean;
    private        String             mAdId;

    //企创
    private AdResponseBean mAdBack;
    private int            mWidth;
    private int            mHeight;

    //广点通
    private UnifiedBannerView mGdtBannerAd;

    //穿山甲
    private TTAdNative        mTTAdNative;
    private TTNativeExpressAd mCAJNativeExpressAd;
    private View              mCSJBannerView;
    private boolean           mHasShowDownloadActive = false;

    //百青藤
    private AdView mBqtAdView;

    private boolean mHaveQC;
    private boolean mHaveCSJ;
    private boolean mHaveTX;
    private boolean mHaveBQT;


    /**
     * 单例模式
     */
    public static BannerManager get () {
        //不写成单例模式,防止多个页面都需要加载bnanner
        sQcBannerAd = new BannerManager();
        return sQcBannerAd;
    }

    /**
     * 初始化数据
     */
    private void initData () {
    }

    /**
     * 清除广告
     * 在合适的时机，释放广告的资源
     */
    public void destroyAd () {
        //穿山甲释放
        if (mCAJNativeExpressAd != null) {
            mCAJNativeExpressAd.destroy();
            mCAJNativeExpressAd = null;
            mTTAdNative = null;
        }

        //广点通释放
        if (mGdtBannerAd != null) {
            mGdtBannerAd.destroy();
            mGdtBannerAd = null;
        }

        //bqt释放
        if (mBqtAdView != null) {
            mBqtAdView.destroy();
            mBqtAdView = null;
        }

        //qc释放
        if (mPostDownTime != null) {
            mPostDownTime.cancel();
            mPostDownTime = null;
        }

        mListener = null;
        sQcBannerAd = null;
    }

    /**
     * banner广告权重计算
     */
    public BannerManager initWeight (Activity activity, ViewGroup container, AdidBean adsSdk,
            String adId, BannerQcAdListener bannerQCADListener) {
        sQcBannerAd = initWeight(activity, container, adsSdk, adId, 0, bannerQCADListener);
        return sQcBannerAd;
    }


    /**
     * banner广告权重计算
     */
    public BannerManager initWeight (final Activity activity, final ViewGroup container, final AdidBean adsSdk,
            final String adId, final float adWigth, BannerQcAdListener bannerQCADListener) {
        initData();//初始化数据
        mListener = bannerQCADListener;
        mContainer = container;
        mAdidBean = adsSdk;
        mAdId = adId;
        mAdWigth = adWigth;
        mWidth = adsSdk.getWidth();
        mHeight = adsSdk.getHeight();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                if (mContainer != null) {
                    mContainer.removeAllViews();
                }
            }
        });

        final AdidBean.SdkBean sdk                 = adsSdk.getSdk();
        int                    chuangshanjiaWeight = 0;    //穿山甲权重
        int                    tengxunWeight       = 0;    //广点通权重
        int                    baiWeight           = 0;    //百青藤权重
        int                    ksWeight            = 0;//快手
        int                    qcWeight            = adsSdk.getWeight();    //企创权重

        if (sdk != null) {  //权重分配
            AdidBean.SdkBean.TengxunBean tengxun = sdk.getTengxun();
            if (tengxun != null) {
                tengxunWeight = tengxun.getWeight();//广点通权重
            }

            AdidBean.SdkBean.ChuangshanjiaBean chuangshanjia = sdk.getChuangshanjia();
            if (chuangshanjia != null) {
                chuangshanjiaWeight = chuangshanjia.getWeight();//穿山甲权重
            }

            AdidBean.SdkBean.BaiQingTeng bai = sdk.getBai();
            if (bai != null) {
                baiWeight = bai.getWeight();//百青藤权重
            }

            AdidBean.SdkBean.KuaiShouBean ks = sdk.getKuaishou();
            if (ks != null) {
                ksWeight = ks.getWeight();//快手权重
            }
        }

        int allWeigth = chuangshanjiaWeight + tengxunWeight + baiWeight + qcWeight;
        int weigth    = CommonUtils.getCompareRandow(allWeigth);// 1~allWeigth的任意整数
        LogUtils.i("csj=" + chuangshanjiaWeight + "gdt=" + tengxunWeight +
                "bqt=" + baiWeight + "qc=" + qcWeight + "current=" + weigth);

        if (allWeigth == 0) {//企创
            ToastUtils.show(activity, "qc");
            getQcAd(activity, adsSdk, adId, true);
        } else if (weigth <= chuangshanjiaWeight) {//穿山甲
            AdidBean.SdkBean.ChuangshanjiaBean chuangshanjia = sdk.getChuangshanjia();
            String                             csjAdid = "";
            if (chuangshanjia != null) {
                csjAdid = Constants.IS_INIT_SIMPLE_DATA ? Constants.CSJSimpleData.BANNER_AD_ID : chuangshanjia.getAdid();
            }

            ToastUtils.show(activity, "csj");
            getCjsAd(activity, csjAdid, true);
        } else if (weigth <= chuangshanjiaWeight + tengxunWeight) {//广点通
            AdidBean.SdkBean.TengxunBean tengxun  = sdk.getTengxun();
            String                       gdtAppid = "";
            String                       gdtPosId = "";
            if (tengxun != null) {
                gdtAppid = Constants.IS_INIT_SIMPLE_DATA ? Constants.GDTSimpleData.APP_ID : tengxun.getAppid();
                gdtPosId = Constants.IS_INIT_SIMPLE_DATA ? Constants.GDTSimpleData.BANNER_AD_ID : tengxun.getAdid();
            }

            ToastUtils.show(activity, "gdt");
            getGdtAd(activity, gdtAppid, gdtPosId, true);
        } else if (weigth <= chuangshanjiaWeight + tengxunWeight + baiWeight) {//百青藤
            ToastUtils.show(activity, "bqt");
            AdidBean.SdkBean.BaiQingTeng bai = sdk.getBai();
            if (bai != null) {
                String adid = Constants.IS_INIT_SIMPLE_DATA ? Constants.BaiduSimpleData.BANNER_AD_ID : bai.getAdid();
                getBqtAd(activity, adid, true);
            }
        } else if (weigth <= chuangshanjiaWeight + tengxunWeight + baiWeight + ksWeight) {
            ToastUtils.show(activity, "ks");
            AdidBean.SdkBean.KuaiShouBean ksBean = sdk.getKuaishou();
            if (null != ksBean) {
                Long ksAdId = Constants.IS_INIT_SIMPLE_DATA ? Constants.KSSimpleData.KS_POSID_FEED_TYPE_1 : Long.valueOf(ksBean.getAdid());
                getKsAd(activity,ksAdId, 1);

            }
        } else {//企创
            ToastUtils.show(activity, "qc");
            getQcAd(activity, adsSdk, adId, true);
        }
        return sQcBannerAd;

    }

    /**
     * 获取快手
     */
    private void getKsAd (
            final Activity activity,
            final long posId,
            final int adNumber) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                final KsScene ksScene = new KsScene.Builder(posId)
                        .adNum(adNumber)
                        .build();
                KsAdSDK.getLoadManager()
                        .loadConfigFeedAd(ksScene,
                                new KsLoadManager.FeedAdListener() {
                                    @Override
                                    public void onError (int i, String s) {
                                        if (mListener != null) {
                                            mListener.onAdError(ErrorUtil.KS, s);
                                        }
                                    }

                                    @Override
                                    public void onFeedAdLoad (@Nullable List<KsFeedAd> list) {
                                        if (null != list && !list.isEmpty()) {
                                            List<View> adViews = new ArrayList<>();

                                            for (KsFeedAd ksFeedAd : list) {
                                                adViews.add(ksFeedAd.getFeedView(activity));
                                            }

                                            final KsFeedAd ksFeedAd = list.get(0);

                                            ksFeedAd.setAdInteractionListener(new KsFeedAd.AdInteractionListener() {
                                                @Override
                                                public void onAdClicked () {
                                                    if (mListener != null) {
                                                        mListener.onAdClicked(ErrorUtil.KS);
                                                    }
                                                }

                                                @Override
                                                public void onAdShow () {
                                                    mListener.onADExposure(ErrorUtil.KS);
                                                }

                                                @Override
                                                public void onDislikeClicked () {
                                                    if (mListener != null) {
                                                        mListener.onAdClicked(ErrorUtil.KS);
                                                    }
                                                }
                                            });
                                            final View view = ksFeedAd.getFeedView(activity);
                                            //计算宽高,调用measure方法之后就可以获取宽高
                                            int width  = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                                            int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                                            view.measure(width, height);

                                            //int measuredWidth  = view.getMeasuredWidth();// 获取宽度
                                            //int measuredHeight = view.getMeasuredHeight();// 获取高度

                                            if (mContainer != null) {
                                                mContainer.removeAllViews();
                                                mContainer.addView(view);
                                            }

                                        }
                                    }
                                });
            }
        });
    }

    /**
     * 获取企创的ad
     */
    private void getQcAd (final Activity activity, AdidBean adsSdk, String adId, final boolean isShow) {
        QcHttpUtil.getAd(activity, adsSdk, adId, new QcHttpUtil.QcHttpOnListener<AdResponseBean>() {

            @Override
            public void OnQcCompletionListener (AdResponseBean response) {
                if (response != null) {
                    final String adm = response.getAdm();
                    ExtBean      ext = response.getExt();
                    if (!TextUtils.isEmpty(adm)) {
                        mHaveQC = true;
                        mAdBack = response;
                    }

                    if (ext != null && ext.getIurl() != null) {
                        mHaveQC = true;
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
                                    mListener.onADManager(sQcBannerAd);
                                    mListener.onAdError(ErrorUtil.QC, ErrorUtil.NOAD);
                                }
                            }
                        });
                    }
                }
            }

            @Override
            public void OnQcErrorListener (final String erro, int code) {
                if (isShow) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run () {
                            if (mListener != null) {
                                mListener.onADManager(sQcBannerAd);
                                mListener.onAdError(ErrorUtil.QC, ErrorUtil.NOAD + erro);
                            }
                        }
                    });

                }
            }
        });
    }

    /**
     * banner2.0规定banner宽高比应该为6.4:1 , 开发者可自行设置符合规定宽高比的具体宽度和高度值
     */
    private FrameLayout.LayoutParams getUnifiedBannerLayoutParams (Activity mActivity) {
        Point screenSize = new Point();
        mActivity.getWindowManager().getDefaultDisplay().getSize(screenSize);
        return new FrameLayout.LayoutParams(screenSize.x, Math.round(screenSize.x / 6.4F));
    }

    /**
     * 获取广点通的ad
     */
    private void getGdtAd (final Activity activity, String appid, String adid, final boolean isShow) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                if (mGdtBannerAd != null) {
                    mGdtBannerAd.destroy();
                }
            }
        });
        /*仅限于和 DownloadService 的不在同一进程的 Activity*/
        MultiProcessFlag.setMultiProcess(true);
        mGdtBannerAd = new UnifiedBannerView(activity, adid, new UnifiedBannerADListener() {
            @Override
            public void onNoAD (AdError adError) {
                LogUtils.e("onNoAD" + adError.getErrorMsg());
                //广点通释放
                if (mGdtBannerAd != null) {
                    mGdtBannerAd.destroy();
                    mGdtBannerAd = null;
                }
                if (mListener != null) {
                    mListener.onADManager(sQcBannerAd);
                    mListener.onAdError(ErrorUtil.GDT, adError.getErrorMsg());
                }
            }

            @Override
            public void onADReceive () {
                LogUtils.e("onADReceive");
                mHaveTX = true;
                if (isShow) {
                    showGdtAd(activity);
                }
            }

            @Override
            public void onADExposure () {
                LogUtils.e("onADExposure");
                if (mListener != null) {
                    mListener.onADManager(sQcBannerAd);
                    mListener.onADExposure(ErrorUtil.GDT);
                }
                //展示过一次之后就销毁掉广点通，防止广点通一直调用刷新
                if (mGdtBannerAd == null){
                    return;
                }
                mGdtBannerAd.postDelayed(new Runnable() {
                    @Override
                    public void run () {
                        if (mGdtBannerAd != null) {
                            mGdtBannerAd.destroy();
                        }
                    }
                },5000);
            }

            @Override
            public void onADClosed () {
                LogUtils.e("onADClosed");
                if (mListener != null) {
                    mListener.onAdClose();
                }

                if (mGdtBannerAd != null) {
                    mGdtBannerAd.destroy();
                }
            }

            @Override
            public void onADClicked () {
                LogUtils.e("onADClicked");
                if (mListener != null) {
                    mListener.onAdClicked(ErrorUtil.GDT);
                }
            }

            @Override
            public void onADLeftApplication () {
                LogUtils.e("onADLeftApplication");
            }

            @Override
            public void onADOpenOverlay () {
                LogUtils.e("onADOpenOverlay");
            }

            @Override
            public void onADCloseOverlay () {
                LogUtils.e("onADCloseOverlay");
            }
        });

        mGdtBannerAd.setRefresh(30);
        mGdtBannerAd.loadAD();

    }

    /**
     * 展示广点通ad
     */
    private void showGdtAd (final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                if (mContainer != null) {
                    mContainer.removeAllViews();
                    if (mGdtBannerAd != null) {
                        mContainer.addView(mGdtBannerAd);
                    }
                }
            }
        });

    }

    /**
     * 获取百青藤的广告
     */
    public void getBqtAd (final Activity activity, String adid, final boolean isShow) {
        mBqtAdView = new AdView(activity, adid);
        // 设置监听器
        mBqtAdView.setListener(new AdViewListener() {
            @Override
            public void onAdSwitch () {
                LogUtils.d("onAdSwitch");
            }

            @Override
            public void onAdShow (JSONObject info) {
                // 广告已经渲染出来
                LogUtils.d("onAdShow " + info.toString());
                if (mListener != null) {
                    mListener.onADExposure(ErrorUtil.BQT);
                }
            }

            @Override
            public void onAdReady (AdView adView) {
                // 资源已经缓存完毕，还没有渲染出来
                LogUtils.d("onAdReady ");
            }

            @Override
            public void onAdFailed (String reason) {
                LogUtils.d("onAdFailed " + reason);
                if (mListener != null) {
                    mListener.onADManager(sQcBannerAd);
                    mListener.onAdError(ErrorUtil.BQT, reason);
                }
            }

            @Override
            public void onAdClick (JSONObject info) {
                LogUtils.d("onAdClick " + info.toString());
                if (mListener != null) {
                    mListener.onAdClicked(ErrorUtil.BQT);
                }
            }

            @Override
            public void onAdClose (JSONObject arg0) {
                LogUtils.d("onAdClose");
                if (mListener != null) {
                    mListener.onAdClose();
                }
            }
        });

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                if (isShow) {
                    mContainer.removeAllViews();
                    mContainer.addView(mBqtAdView);
                }
            }
        });
    }

    /**
     * 获取穿山甲ad
     */
    private void getCjsAd (final Activity mActivity, String codeId, final boolean isShow) {
        mTTAdNative = TTAdManagerHolder.get().createAdNative(mActivity);
        TTAdManagerHolder.get().requestPermissionIfNecessary(mActivity);
        float expressViewWidth = 640;
        //适配听说很好玩
        int screenWidth = DeviceUtil.getScreenWidth(mActivity);
        expressViewWidth = DeviceUtil.px2dip(mActivity, screenWidth) - 38;
        float expressViewHeigth = expressViewWidth * 10 / 64;

        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(1) //请求广告数量为1到3条
                .setImageAcceptedSize((int) expressViewHeigth, (int) expressViewHeigth)
                .setExpressViewAcceptedSize(320, 100) //期望模板广告view的size,单位dp
                .build();

        //step5:请求广告，对请求回调的广告作渲染处理
        mTTAdNative.loadBannerExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError (int code, String message) {
                LogUtils.e("csjbanner加载失败" + message);
                if (mListener != null) {
                    mListener.onADManager(sQcBannerAd);
                    mListener.onAdError(ErrorUtil.CSJ, message);
                }
            }

            @Override
            public void onNativeExpressAdLoad (List<TTNativeExpressAd> ads) {
                if (ads == null || ads.size() == 0) {
                    if (mListener != null) {
                        mListener.onADManager(sQcBannerAd);
                        mListener.onAdError(ErrorUtil.CSJ, ErrorUtil.NOAD);
                    }
                    return;
                }
                mCAJNativeExpressAd = ads.get(0);
                bindAdListener(mActivity, mCAJNativeExpressAd, isShow);
                mCAJNativeExpressAd.render();
            }
        });

    }

    /**
     * 穿山甲广告监听
     */
    private void bindAdListener (final Activity activity, TTNativeExpressAd ad, final boolean isShow) {
        ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            @Override
            public void onAdClicked (View view, int type) {
                LogUtils.d("广告被点击");
                if (mListener != null) {
                    mListener.onAdClicked(ErrorUtil.CSJ);
                }
            }

            @Override
            public void onAdShow (View view, int type) {
                LogUtils.d("广告展示");
                if (mListener != null) {
                    mListener.onADManager(sQcBannerAd);
                    mListener.onADExposure(ErrorUtil.CSJ);
                }
            }

            @Override
            public void onRenderFail (View view, String msg, int code) {
                LogUtils.d("onRenderFail渲染失败:" + msg + code);
            }

            @Override
            public void onRenderSuccess (View view, float width, float height) {
                LogUtils.d("onRenderSuccess渲染成功");
                //返回view的宽高 单位 dp
                mCSJBannerView = view;
                mHaveCSJ = true;
                if (isShow) {
                    showCsjAd(activity);
                }

            }
        });

        //Dislike设置
        bindDislike(activity, ad, false);
        //（可选）设置下载类广告的下载监听
        //bindDownloadListener(mCAJNativeExpressAd);
    }

    /**
     * 展示穿山甲ad
     */
    private void showCsjAd (final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                if (mContainer != null) {
                    mContainer.removeAllViews();
                    mContainer.addView(mCSJBannerView);
                }
            }
        });

    }

    /**
     * 设置广告的不喜欢, 注意：强烈建议设置该逻辑，如果不设置dislike处理逻辑，则模板广告中的 dislike区域不响应dislike事件。
     *
     * @param customStyle 是否自定义样式，true:样式自定义
     */
    private void bindDislike (Activity activity, TTNativeExpressAd ad, boolean customStyle) {
        //使用默认模板中默认dislike弹出样式
        mCAJNativeExpressAd.setDislikeCallback(activity, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onShow () {

            }

            @Override
            public void onSelected (int i, String s, boolean b) {
                //用户选择不喜欢原因后，移除广告展示
                if (mContainer != null) {
                    mContainer.removeAllViews();
                }
                if (mListener != null) {
                    mListener.onAdClose();
                }
            }

            @Override
            public void onCancel () {
                LogUtils.d("点击取消");
            }

        });
    }

    /**
     * 穿山甲下载监听
     */
    private void bindDownloadListener (TTNativeExpressAd ad) {
        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            return;
        }
        ad.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle () {
                LogUtils.d("点击图片开始下载");
            }

            @Override
            public void onDownloadActive (long totalBytes, long currBytes, String fileName, String appName) {
                if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
                    LogUtils.d("下载中，点击图片暂停");
                }
            }

            @Override
            public void onDownloadPaused (long totalBytes, long currBytes, String fileName, String appName) {
                LogUtils.d("下载暂停，点击图片继续");
            }

            @Override
            public void onDownloadFailed (long totalBytes, long currBytes, String fileName, String appName) {
                LogUtils.d("下载失败，点击图片重新下载");
            }

            @Override
            public void onInstalled (String fileName, String appName) {
                LogUtils.d("安装完成，点击图片打开");
            }

            @Override
            public void onDownloadFinished (long totalBytes, String fileName, String appName) {
                LogUtils.d("点击图片安装");
            }
        });
    }

    /**
     * 展示企创banner的广告
     */
    private void showQcAd (final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                float realyWidth  = 0;
                int   screenWidth = DeviceUtil.getScreenWidth(activity);
                if (mAdWigth == 0) {//和屏幕一样大
                    realyWidth = DeviceUtil.px2dip(activity, screenWidth);
                } else {//修改
                    realyWidth = mAdWigth;
                }
                final QcBannerAdView qcBannerAdView =
                        new QcBannerAdView(activity, null, mAdBack, mWidth, mHeight, realyWidth, mListener);
                if (mContainer != null) {
                    mContainer.removeAllViews();
                    mContainer.addView(qcBannerAdView);
                }
            }
        });
    }
}
