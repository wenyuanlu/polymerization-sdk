package com.corpize.sdk.mobads.admanager;

import android.app.Activity;
import android.os.CountDownTimer;
import android.os.Looper;
import android.view.View;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.corpize.sdk.mobads.TTAdManagerHolder;
import com.corpize.sdk.mobads.bean.AdResponseBean;
import com.corpize.sdk.mobads.bean.AdidBean;
import com.corpize.sdk.mobads.bean.NativeBean;
import com.corpize.sdk.mobads.common.CommonUtils;
import com.corpize.sdk.mobads.common.Constants;
import com.corpize.sdk.mobads.common.ErrorUtil;
import com.corpize.sdk.mobads.listener.InfoMoreQcAdListener;
import com.corpize.sdk.mobads.utils.DeviceUtil;
import com.corpize.sdk.mobads.utils.LogUtils;
import com.corpize.sdk.mobads.utils.QcHttpUtil;
import com.corpize.sdk.mobads.utils.ToastUtils;
import com.corpize.sdk.mobads.view.QcThreeInfoAdView;
import com.qq.e.ads.cfg.MultiProcessFlag;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.comm.util.AdError;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author: yh
 * date: 2020-02-11 23:35
 * description: TODO:三图广告
 */
public class InfoThreeManager {

    private static InfoThreeManager     sQcInfoThreeAd;
    private        CountDownTimer       mPostDownTime;  //同时发送的三个请求的倒计时
    private        AdidBean             mAdidBean;
    private        String               mAdId;
    private        int                  mMaxNum;
    private        InfoMoreQcAdListener mListener;      //单图信息流

    //企创
    private AdResponseBean    mAdBack;
    private NativeBean        mNativeBean;
    private QcThreeInfoAdView mThreeFrameLayout;

    //广点通
    private NativeExpressAD           mADManager;
    private List<NativeExpressADView> mGdtOneAdViewList;

    //穿山甲
    private TTAdNative              mTTAdNative;
    private List<TTNativeExpressAd> mCSJOneAdsList;
    private List<View>              mCSJAdsList;
    private int                     mRenderNumber = 0;//穿山甲渲染的view的数量
    private Map<View, Integer>      mCSJAdViewMap = new HashMap<>();//穿山甲返回的view的保存记录

    private boolean mHaveQC;
    private boolean mHaveCSJ;
    private boolean mHaveTX;
    private boolean mHaveBQT;
    private int     mQCState      = 0;//0初始,1获取到广告,-1未获取到广告
    private int     mCSJState     = 0;//0初始,1获取到广告,-1未获取到广告
    private int     mTXState      = 0;//0初始,1获取到广告,-1未获取到广告
    private boolean mHaveDownOver = false;//倒计时是否已经加载过

    /**
     * 单例模式
     */
    public static InfoThreeManager get () {
        sQcInfoThreeAd = new InfoThreeManager();
        return sQcInfoThreeAd;
    }

    /**
     * 初始化数据
     */
    private void initData () {
        mHaveQC = false;
        mHaveCSJ = false;
        mHaveTX = false;
        mHaveBQT = false;
        mQCState = 0;
        mCSJState = 0;
        mTXState = 0;
        mHaveDownOver = false;
    }

    /**
     * 清除广告
     * 在合适的时机，释放广告的资源
     */
    public void destroyAd () {
        //gdt
        if (mGdtOneAdViewList != null && mGdtOneAdViewList.size() > 0) {
            for (NativeExpressADView nativeExpressADView : mGdtOneAdViewList) {
                nativeExpressADView.destroy();
            }
            mGdtOneAdViewList.clear();
            mCSJOneAdsList = null;
        }
        mADManager = null;

        //csj
        if (mCSJOneAdsList != null) {
            mCSJOneAdsList.clear();
            mCSJOneAdsList = null;
        }

        if (mCSJAdViewMap != null) {
            mCSJAdViewMap.clear();
            mCSJAdViewMap = null;
        }
        mTTAdNative = null;

        //qc
        if (mPostDownTime != null) {
            mPostDownTime.cancel();
            mPostDownTime = null;
        }
        mThreeFrameLayout = null;
        sQcInfoThreeAd = null;
    }

    /**
     * 插屏广告权重计算
     */
    public void initWeigth (final Activity activity, final AdidBean adsSdk, final String adId, int maxNum, InfoMoreQcAdListener listener) {
        initData();//初始化数据
        mAdidBean = adsSdk;
        mAdId = adId;
        mMaxNum = maxNum;//请求的数据
        mListener = listener;

        final AdidBean.SdkBean sdk = adsSdk.getSdk();

        if (sdk != null) {//banner广告,同时发送三个请求
            //获取企创的ad
            getQcAd(activity, adsSdk, adId, false);

            //获取广点通ad
            AdidBean.SdkBean.TengxunBean tengxun  = sdk.getTengxun();
            String                       gdtAppid;
            String                       gdtPosId;
            if (tengxun != null) {
                gdtAppid = Constants.IS_INIT_SIMPLE_DATA ? Constants.GDTSimpleData.APP_ID : tengxun.getAppid();
                gdtPosId = Constants.IS_INIT_SIMPLE_DATA ? Constants.GDTSimpleData.SINGLE_INFO_AD_ID : tengxun.getAdid();
                //获取广点通ad
                getGdtAd(activity, gdtAppid, gdtPosId, false);
            } else {
                mTXState = -1;//未下发,则置为未获取到
            }

            //获取穿山甲ad
            AdidBean.SdkBean.ChuangshanjiaBean chuangshanjia = sdk.getChuangshanjia();
            String                             csjAdid       = "";
            if (chuangshanjia != null) {
                csjAdid = Constants.IS_INIT_SIMPLE_DATA ? Constants.CSJSimpleData.SINGLE_INFO_AD_ID : chuangshanjia.getAdid();
                //获取穿山甲ad
                getCjsAd(activity, csjAdid, false);
            } else {
                mCSJState = -1;//未下发,则置为未获取到
            }

            //倒计时确定权重
            countDown(activity, sdk);

        } else {
            //sdk未返回数据,则直接展示企创的广告
            getQcAd(activity, adsSdk, adId, true);
        }
    }

    /**
     * 倒计时确定权重
     */
    private void countDown (final Activity activity, final AdidBean.SdkBean sdk) {
        Looper.prepare();//增加部分
        if (mPostDownTime != null) {
            mPostDownTime.cancel();
        }
        mPostDownTime = new CountDownTimer(910, 1000) {
            @Override
            public void onTick (long millisUntilFinished) {
                LogUtils.e("--------倒计时的时间=" + millisUntilFinished);
            }

            @Override
            public void onFinish () {
                if (mHaveDownOver) {
                    return;
                }
                mHaveDownOver = true;
                //获取对应的权重
                int chuangshanjiaWeight = 0;    //穿山甲权重
                int tengxunWeight       = 0;    //广点通权重
                //int baiWeight           = 0;    //百青藤权重
                int qcWeight  = 0;    //企创权重
                int allWeight = 0;    //所有权重

                if (mHaveCSJ) {
                    AdidBean.SdkBean.ChuangshanjiaBean chuangshanjia = sdk.getChuangshanjia();
                    if (chuangshanjia != null) {
                        chuangshanjiaWeight = chuangshanjia.getWeight();
                    }
                }

                if (mHaveTX) {
                    AdidBean.SdkBean.TengxunBean tengxun = sdk.getTengxun();
                    if (tengxun != null) {
                        tengxunWeight = tengxun.getWeight();
                    }
                }

                if (mHaveQC) {
                    int supplier = mAdBack.getSupplier();
                    if (supplier == 0) {//0时是兜底,有其他sdk就不用企创的
                        if (!mHaveCSJ && !mHaveTX/* && !mHaveBQT*/) {
                            //其他SDK都没有
                            qcWeight = mAdidBean.getWeight();
                        }
                    } else {
                        qcWeight = mAdidBean.getWeight();
                    }

                }

                allWeight = chuangshanjiaWeight + tengxunWeight + qcWeight;

                int currentWeight = CommonUtils.getCompareRandow(allWeight);// 1 ~ allWeight的任意整数
                LogUtils.i("csj=" + chuangshanjiaWeight + "gdt=" + tengxunWeight +
                        "qc=" + qcWeight + "current当前权重值=" + currentWeight);

                if (allWeight == 0) {//企创
                    ToastUtils.show(activity, "qc");
                    if (mHaveQC) {
                        showQcAd(activity);
                    } else {
                        getQcAd(activity, mAdidBean, mAdId, true);
                    }

                } else if (currentWeight <= chuangshanjiaWeight) {                          // 穿山甲渠道
                    //calculateClickNum();//统计点击次数
                    ToastUtils.show(activity, "csj");
                    showCsjAd(activity);

                } else if (currentWeight <= chuangshanjiaWeight + tengxunWeight) {          // 广点通渠道
                    //calculateClickNum();//统计点击次数
                    ToastUtils.show(activity, "gdt");
                    showGdtAd(activity);

                } else {// 企创渠道
                    //calculateClickNum();//统计点击次数
                    ToastUtils.show(activity, "qc");
                    if (mHaveQC) {
                        showQcAd(activity);
                    } else {
                        getQcAd(activity, mAdidBean, mAdId, true);
                    }
                }
            }
        };

        mPostDownTime.start();

        Looper.loop();//增加部分

    }

    /**
     * 获取企创的ad
     */
    private void getQcAd (final Activity activity, AdidBean adsSdk, String adId, final boolean isShow) {
        QcHttpUtil.getAd(activity, adsSdk, adId, new QcHttpUtil.QcHttpOnListener<AdResponseBean>() {

            @Override
            public void OnQcCompletionListener (AdResponseBean response) {
                if (response != null && response.getNative1() != null) {
                    mAdBack = response;
                    mNativeBean = mAdBack.getNative1();
                    mHaveQC = true;
                    mQCState = 1;

                    if (isShow) {
                        showQcAd(activity);
                    } else {
                        downTimeHandFinish();//判断三个请求是否结束,QC
                    }

                } else {
                    mQCState = -1;
                    if (isShow) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run () {
                                if (mListener != null) {
                                    mListener.onAdError(ErrorUtil.GDT, ErrorUtil.NOAD);
                                }
                            }
                        });
                    } else {
                        downTimeHandFinish();//判断三个请求是否结束,QC
                    }
                }
            }

            @Override
            public void OnQcErrorListener (final String erro, final int code) {
                mQCState = -1;
                if (isShow) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run () {
                            if (mListener != null) {
                                mListener.onAdError(ErrorUtil.GDT, ErrorUtil.NOAD + code + erro);
                            }
                        }
                    });

                } else {
                    downTimeHandFinish();//判断三个请求是否结束,QC
                }
            }
        });
    }

    /**
     * 获取广点通的ad
     */
    private void getGdtAd (final Activity activity, String appid, String adid, final boolean isShow) {
        /*仅限于和 DownloadService 的不在同一进程的 Activity*/
        MultiProcessFlag.setMultiProcess(true);
        ADSize adSize = new ADSize(ADSize.FULL_WIDTH, ADSize.AUTO_HEIGHT);
        mADManager = new NativeExpressAD(activity, adSize, adid, new NativeExpressAD.NativeExpressADListener() {
            @Override
            public void onNoAD (AdError adError) {
                LogUtils.d("onNoAD");
                mTXState = -1;
                downTimeHandFinish();//判断三个请求是否结束,GDT
            }

            @Override
            public void onADLoaded (List<NativeExpressADView> adList) {
                LogUtils.d("onADLoaded: " + adList.size());
                if (adList == null || adList.size() == 0) {
                    return;
                }
                mHaveTX = true;
                mTXState = 1;
                mGdtOneAdViewList = adList;

                if (isShow) {
                    showGdtAd(activity);
                } else {
                    downTimeHandFinish();//判断三个请求是否结束,GDT
                }

            }

            @Override
            public void onRenderFail (NativeExpressADView adView) {
                LogUtils.d("onRenderFail: " + adView.toString());
                if (mListener != null) {
                    mListener.onAdError(ErrorUtil.GDT, "onRenderFail: " + adView.toString());
                }
            }

            @Override
            public void onRenderSuccess (NativeExpressADView adView) {
                LogUtils.d("onRenderSuccess: " + adView.toString());
            }

            @Override
            public void onADExposure (NativeExpressADView adView) {
                LogUtils.d("onADExposure: " + adView.toString());
                if (mListener != null) {
                    mListener.onAdExposure(ErrorUtil.GDT);
                }
            }

            @Override
            public void onADClicked (NativeExpressADView nativeExpressADView) {
                LogUtils.d("onADClicked: " + nativeExpressADView.toString());
                if (mListener != null) {
                    mListener.onAdClicked(nativeExpressADView, ErrorUtil.GDT);
                }
            }

            @Override
            public void onADClosed (NativeExpressADView nativeExpressADView) {
                LogUtils.d("onADClosed: " + nativeExpressADView.toString());
                if (mListener != null) {
                    mListener.onAdClose(nativeExpressADView, ErrorUtil.GDT);
                }
            }

            @Override
            public void onADLeftApplication (NativeExpressADView nativeExpressADView) {
                LogUtils.d("onADLeftApplication: " + nativeExpressADView.toString());
            }

            @Override
            public void onADOpenOverlay (NativeExpressADView nativeExpressADView) {
                LogUtils.d("onADOpenOverlay: " + nativeExpressADView.toString());
            }

            @Override
            public void onADCloseOverlay (NativeExpressADView nativeExpressADView) {
                LogUtils.d("onADCloseOverlay");
            }
        });

        //设置广告视频播放策略
        mADManager.setVideoPlayPolicy(VideoOption.VideoPlayPolicy.AUTO); // 本次拉回的视频广告，从用户的角度看是自动播放的
        mADManager.loadAD(mMaxNum);

    }

    /**
     * 获取穿山甲ad
     */
    private void getCjsAd (final Activity activity, String codeId, final boolean isShow) {
        mTTAdNative = TTAdManagerHolder.get().createAdNative(activity);
        TTAdManagerHolder.get().requestPermissionIfNecessary(activity);
        float expressViewWidth  = 640;
        float expressViewHeight = 0;
        int   screenWidth       = DeviceUtil.getScreenWidth(activity);
        expressViewWidth = DeviceUtil.px2dip(activity, screenWidth);
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(640, 320)
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //必填：期望个性化模板广告view的size,单位dp
                .setAdCount(mMaxNum) //请求广告数量为1到3条
                .build();

        //step5:请求广告，调用feed广告异步请求接口，加载到广告后，拿到广告素材自定义渲染
        mTTAdNative.loadNativeExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError (int code, String message) {
                LogUtils.e("onError：" + message + "..." + code);
                mCSJState = -1;
                downTimeHandFinish();//判断三个请求是否结束,CSJ
                /*if (mListener != null) {
                    mListener.onAdError(ErrorUtil.CSJ, message + "..." + code);
                }*/
            }

            @Override
            public void onNativeExpressAdLoad (List<TTNativeExpressAd> ads) {
                LogUtils.e("onNativeExpressAdLoad的数量" + ads.size());
                mCSJOneAdsList = ads;
                if (ads == null || ads.size() == 0) {
                    return;
                }

                mHaveCSJ = true;
                mCSJState = 1;
                if (isShow) {
                    showCsjAd(activity);
                } else {
                    downTimeHandFinish();//判断三个请求是否结束,CSJ
                }
            }
        });

    }

    /**
     * 展示广点通ad
     */
    private void showGdtAd (final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                if (mListener != null) {
                    List<View> list = new ArrayList<>();
                    for (NativeExpressADView nativeExpressADView : mGdtOneAdViewList) {
                        nativeExpressADView.render();
                        list.add(nativeExpressADView);
                    }
                    mListener.onAdViewSuccess(list, ErrorUtil.GDT);
                }
            }
        });
    }

    /**
     * 展示穿山甲ad
     */
    private void showCsjAd (final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                //遍历获取的广告
                mRenderNumber = 0;
                mCSJAdsList = new ArrayList<>();
                mCSJAdViewMap = new HashMap<>();
                final int allSize = mCSJOneAdsList.size();
                for (int i = 0; i < mCSJOneAdsList.size(); i++) {
                    TTNativeExpressAd ad = mCSJOneAdsList.get(i);
                    ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
                        @Override
                        public void onAdClicked (View view, int i) {
                            LogUtils.e("返回了=onAdClicked=" + i);
                            if (mListener != null) {
                                mListener.onAdClicked(view, ErrorUtil.CSJ);
                            }
                        }

                        @Override
                        public void onAdShow (View view, int i) {
                            LogUtils.e("返回了=onAdExposure=" + i);
                            boolean isHave = mCSJAdViewMap.containsKey(view);
                            if (!isHave) {
                                mCSJAdViewMap.put(view, i);
                                if (mListener != null) {
                                    mListener.onAdExposure(ErrorUtil.CSJ);
                                }
                            }
                        }

                        @Override
                        public void onRenderFail (View view, String s, int i) {
                            LogUtils.e("返回了=onRenderFail=" + i);
                            mRenderNumber++;
                            //全部渲染完毕,数据传递出去
                            if (mRenderNumber == allSize) {
                                if (mListener != null) {
                                    mListener.onAdViewSuccess(mCSJAdsList, ErrorUtil.CSJ);
                                }
                            }
                        }

                        @Override
                        public void onRenderSuccess (View view, float v, float v1) {
                            LogUtils.e("返回了=onRenderSuccess");
                            mRenderNumber++;
                            mCSJAdsList.add(view);
                            //全部渲染完毕,数据传递出去
                            if (mRenderNumber == allSize) {
                                if (mListener != null) {
                                    mListener.onAdViewSuccess(mCSJAdsList, ErrorUtil.CSJ);
                                }
                            }

                        }
                    });

                    //不喜欢和下载回调
                    bindAdListener(activity, ad);
                    //渲染
                    ad.render();
                }
            }
        });
    }

    /**
     * 穿山甲回调
     */
    private void bindAdListener (Activity activity, final TTNativeExpressAd ad) {
        //dislike设置 使用默认个性化模板中默认dislike弹出样式
        ad.setDislikeCallback(activity, new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onShow () {

            }

            @Override
            public void onSelected (int i, String s, boolean b) {
                //用户选择不喜欢原因后，移除广告展示
                View expressAdView = ad.getExpressAdView();
                //用户选择不喜欢原因后，移除广告展示
                if (mListener != null) {
                    mListener.onAdClose(expressAdView, ErrorUtil.QC);
                }
            }

            @Override
            public void onCancel () {
                //TToast.show(mContext, "点击取消 ");
            }
        });

        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            return;
        }

        //可选，下载监听设置
        ad.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle () {
                //TToast.show(NativeExpressActivity.this, "点击开始下载", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadActive (long totalBytes, long currBytes, String fileName, String appName) {
                /*if (!mHasShowDownloadActive) {
                    mHasShowDownloadActive = true;
                    TToast.show(NativeExpressActivity.this, "下载中，点击暂停", Toast.LENGTH_LONG);
                }*/
            }

            @Override
            public void onDownloadPaused (long totalBytes, long currBytes, String fileName, String appName) {
                //TToast.show(NativeExpressActivity.this, "下载暂停，点击继续", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFailed (long totalBytes, long currBytes, String fileName, String appName) {
                //TToast.show(NativeExpressActivity.this, "下载失败，点击重新下载", Toast.LENGTH_LONG);
            }

            @Override
            public void onInstalled (String fileName, String appName) {
                //TToast.show(NativeExpressActivity.this, "安装完成，点击图片打开", Toast.LENGTH_LONG);
            }

            @Override
            public void onDownloadFinished (long totalBytes, String fileName, String appName) {
                //TToast.show(NativeExpressActivity.this, "点击安装", Toast.LENGTH_LONG);
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
                if (mListener != null) {
                    mThreeFrameLayout = new QcThreeInfoAdView(activity, null, mListener, mNativeBean);
                    List<View> list = new ArrayList<>();
                    list.add(mThreeFrameLayout);
                    mThreeFrameLayout.render();
                    mListener.onAdViewSuccess(list, ErrorUtil.QC);
                }
            }
        });
    }

    /**
     * 手动结束倒计时
     * 优化倒计时权重,需要在不同sdk请求后,判断其他sdk有没有请求结束,然后手动结束倒计时
     */
    private void downTimeHandFinish () {
        if (mQCState != 0 && mCSJState != 0 && mTXState != 0 && mPostDownTime != null) {
            mPostDownTime.onFinish();
            LogUtils.e("--------手动调用了结束--------");
        }
    }
}
