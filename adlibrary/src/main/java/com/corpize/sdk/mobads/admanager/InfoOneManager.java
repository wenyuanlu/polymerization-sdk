package com.corpize.sdk.mobads.admanager;

import android.app.Activity;
import android.os.CountDownTimer;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.baidu.mobads.sdk.api.ArticleInfo;
import com.baidu.mobads.sdk.api.BaiduNativeManager;
import com.baidu.mobads.sdk.api.FeedNativeView;
import com.baidu.mobads.sdk.api.NativeResponse;
import com.baidu.mobads.sdk.api.RequestParameters;
import com.baidu.mobads.sdk.api.XAdNativeResponse;
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
import com.corpize.sdk.mobads.listener.InfoOneQcAdListener;
import com.corpize.sdk.mobads.utils.DeviceUtil;
import com.corpize.sdk.mobads.utils.LogUtils;
import com.corpize.sdk.mobads.utils.QcHttpUtil;
import com.corpize.sdk.mobads.utils.ToastUtils;
import com.corpize.sdk.mobads.view.QcOneInfoAdView;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsFeedAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;
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
 * description: TODO:插屏广告
 */
public class InfoOneManager {

    private static InfoOneManager      sQcInfoOneAd;
    private        CountDownTimer      mPostDownTime;       //同时发送的三个请求的倒计时
    private        AdidBean            mAdidBean;
    private        String              mAdId;
    private        int                 mMaxNum;
    private        float               mAdWigth = 0;        //请求的广告的宽度,单位dp,主要给穿山甲使用
    private        InfoOneQcAdListener mListener;           //单图信息流

    //企创
    private AdResponseBean  mAdBack;
    private NativeBean      mNativeBean;
    private QcOneInfoAdView mOneFrameLayout;

    //广点通
    private NativeExpressAD           mADManager;
    private List<NativeExpressADView> mGdtOneAdViewList;

    //穿山甲
    private TTAdNative              mTTAdNative;
    private List<TTNativeExpressAd> mCSJOneAdsList;
    private List<View>              mCSJAdsList;
    private int                     mRenderNumber = 0;//穿山甲渲染的view的数量
    private Map<View, Integer>      mCSJAdViewMap = new HashMap<>();//穿山甲返回的view的保存记录

    //百青藤
    private BaiduNativeManager baiduNativeManager;

    private boolean              mHaveQC;
    private boolean              mHaveCSJ;
    private boolean              mHaveTX;
    private boolean              mHaveBQT;
    private boolean              mHaveKS;
    private int                  mQCState      = 0;//0初始,1获取到广告,-1未获取到广告
    private int                  mCSJState     = 0;//0初始,1获取到广告,-1未获取到广告
    private int                  mTXState      = 0;//0初始,1获取到广告,-1未获取到广告
    private int                  mBQTState     = 0;//0初始,1获取到广告,-1未获取到广告
    private int                  mKSState      = 0;//0初始,1获取到广告,-1未获取到广告
    private boolean              mHaveDownOver = false;//倒计时是否已经加载过
    private KsFeedAd             ksFeedAd;
    private List<View>           ksAdViews;
    private List<NativeResponse> baiduNatives;


    /**
     * 单例模式
     */
    public static InfoOneManager get () {
        if (sQcInfoOneAd == null) {
            sQcInfoOneAd = new InfoOneManager();//不用单例,数据可能有多个地方的返回
        }
        return sQcInfoOneAd;
    }

    /**
     * 初始化数据
     */
    private void initData () {
        mHaveQC = false;
        mHaveCSJ = false;
        mHaveTX = false;
        mHaveBQT = false;
        mHaveKS = false;
        mQCState = 0;
        mCSJState = 0;
        mTXState = 0;
        mBQTState = 0;
        mKSState = 0;
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
        mOneFrameLayout = null;
        mAdidBean = null;
        mAdId = null;
        mListener = null;
        mAdBack = null;
        mNativeBean = null;
        sQcInfoOneAd = null;
    }

    /**
     * 插屏广告权重计算
     */
    public void initWeigth (final Activity mActivity, final AdidBean adsSdk,
            final String adId, final int maxNum, InfoOneQcAdListener oneInfoQCADListener) {
        initData();//初始化数据
        mAdidBean = adsSdk;
        mAdId = adId;
        mMaxNum = maxNum;//请求的数据
        mAdWigth = 0;
        mListener = oneInfoQCADListener;

        final AdidBean.SdkBean sdk = adsSdk.getSdk();

        if (sdk != null) {//banner广告,同时发送三个请求
            //获取企创的ad
            getQcAd(mActivity, adsSdk, adId, false);

            //获取穿山甲ad
            AdidBean.SdkBean.ChuangshanjiaBean chuangshanjia = sdk.getChuangshanjia();
            String                             csjAdid       = "";
            if (chuangshanjia != null) {
                csjAdid = Constants.IS_INIT_SIMPLE_DATA ? Constants.CSJSimpleData.SINGLE_INFO_AD_ID : chuangshanjia.getAdid();
                //获取穿山甲ad
                getCjsAd(mActivity, csjAdid, false);
            } else {
                mCSJState = -1;//未下发,则置为未获取到
            }

            //获取广点通ad
            AdidBean.SdkBean.TengxunBean tengxun = sdk.getTengxun();
            String                       gdtAppid;
            String                       gdtPosId;
            if (tengxun != null) {
                gdtAppid = Constants.IS_INIT_SIMPLE_DATA ? Constants.GDTSimpleData.APP_ID : tengxun.getAppid();
                gdtPosId = Constants.IS_INIT_SIMPLE_DATA ? Constants.GDTSimpleData.SINGLE_INFO_AD_ID : tengxun.getAdid();
                //获取广点通ad
                getGdtAd(mActivity, gdtAppid, gdtPosId, false);
            } else {
                mTXState = -1;//未下发,则置为未获取到
            }

            //百青藤
            AdidBean.SdkBean.BaiQingTeng baiQingTeng = sdk.getBai();
            String                       baiduPosId;
            if (null != baiQingTeng) {
                baiduPosId = Constants.IS_INIT_SIMPLE_DATA ? Constants.BaiduSimpleData.SINGLE_INFO_AD_ID : baiQingTeng.getAppid();
                getBQTAd(mActivity, baiduPosId);
            } else {
                mBQTState = -1;
            }

            //快手
            AdidBean.SdkBean.KuaiShouBean kuaiShouBean = sdk.getKuaishou();
            Long                          ksPosId;
            if (null != kuaiShouBean && null != kuaiShouBean.getAdid()) {
                ksPosId = Constants.IS_INIT_SIMPLE_DATA ? Constants.KSSimpleData.KS_INFO_SINGLE_POSID : Long.valueOf(kuaiShouBean.getAdid());
                getKsAd(mActivity, ksPosId, 1);
            } else {
                mKSState = -1;
            }

            //倒计时确定权重
            countDown(mActivity, sdk);
        } else {
            //sdk未返回数据,则直接展示企创的广告
            getQcAd(mActivity, adsSdk, adId, true);
        }
    }

    /**
     * 插屏广告权重计算
     */
    public void initWeigth (final Activity mActivity, final AdidBean adsSdk,
            final String adId, final int maxNum, final float adWigth, InfoOneQcAdListener oneInfoQCADListener) {
        initData();//初始化数据
        mAdidBean = adsSdk;
        mAdId = adId;
        mMaxNum = maxNum;//请求的数据
        mAdWigth = adWigth;
        mListener = oneInfoQCADListener;

        final AdidBean.SdkBean sdk = adsSdk.getSdk();

        if (sdk != null) {//banner广告,同时发送三个请求
            //获取企创的ad
            getQcAd(mActivity, adsSdk, adId, false);

            //获取穿山甲ad
            AdidBean.SdkBean.ChuangshanjiaBean chuangshanjia = sdk.getChuangshanjia();
            String                             csjAdid       = "901121253";
            if (chuangshanjia != null) {
                csjAdid = chuangshanjia.getAdid();
                //获取穿山甲ad
                getCjsAd(mActivity, csjAdid, false);
            } else {
                mCSJState = -1;//未下发,则置为未获取到
            }

            //获取广点通ad
            AdidBean.SdkBean.TengxunBean tengxun = sdk.getTengxun();
            String                       gdtAppid;
            String                       gdtPosId;
            if (tengxun != null) {
                gdtAppid = Constants.IS_INIT_SIMPLE_DATA ? Constants.GDTSimpleData.APP_ID : tengxun.getAppid();
                gdtPosId = Constants.IS_INIT_SIMPLE_DATA ? Constants.GDTSimpleData.SINGLE_INFO_AD_ID : tengxun.getAdid();
                //获取广点通ad
                getGdtAd(mActivity, gdtAppid, gdtPosId, false);
            } else {
                mTXState = -1;//未下发,则置为未获取到
            }

            //百青藤
            AdidBean.SdkBean.BaiQingTeng baiQingTeng = sdk.getBai();
            String                       baiduPosId;
            if (null != baiQingTeng) {
                baiduPosId = Constants.IS_INIT_SIMPLE_DATA ? Constants.BaiduSimpleData.SINGLE_INFO_AD_ID : baiQingTeng.getAppid();
                getBQTAd(mActivity, baiduPosId);
            } else {
                mBQTState = -1;
            }

            //快手
            AdidBean.SdkBean.KuaiShouBean kuaiShouBean = sdk.getKuaishou();
            Long                          ksPosId;
            if (null != kuaiShouBean && null != kuaiShouBean.getAdid()) {
                ksPosId = Constants.IS_INIT_SIMPLE_DATA ? Constants.KSSimpleData.KS_INFO_SINGLE_POSID : Long.valueOf(kuaiShouBean.getAdid());
                getKsAd(mActivity, ksPosId, 1);
            } else {
                mKSState = -1;
            }

            //倒计时确定权重
            countDown(mActivity, sdk);
        } else {
            //sdk未返回数据,则直接展示企创的广告
            getQcAd(mActivity, adsSdk, adId, true);
        }
    }

    /**
     * 倒计时确定权重
     *
     * @param mActivity
     * @param sdk
     */
    private void countDown (final Activity mActivity, final AdidBean.SdkBean sdk) {
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
                int baiWeight           = 0;    //百青藤权重
                int ksWeight            = 0;    //快手权重
                int qcWeight            = 0;    //企创权重
                int allWeight           = 0;    //所有权重

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

                if (mHaveBQT) {
                    AdidBean.SdkBean.BaiQingTeng bai = sdk.getBai();
                    if (bai != null) {
                        baiWeight = bai.getWeight();
                    }
                }

                if (mHaveKS) {
                    AdidBean.SdkBean.KuaiShouBean kuaiShouBean = sdk.getKuaishou();
                    if (kuaiShouBean != null) {
                        ksWeight = kuaiShouBean.getWeight();
                    }
                }

                if (mHaveQC) {
                    int supplier = mAdBack.getSupplier();
                    if (supplier == 0) {//0时是兜底,有其他sdk就不用企创的
                        if (!mHaveCSJ && !mHaveTX && !mHaveBQT && !mHaveKS) {
                            //其他SDK都没有
                            qcWeight = mAdidBean.getWeight();
                        }
                    } else {
                        qcWeight = mAdidBean.getWeight();
                    }
                }

                allWeight = chuangshanjiaWeight + qcWeight + tengxunWeight + ksWeight;

                int currentWeight = CommonUtils.getCompareRandow(allWeight);// 1 ~ allWeight的任意整数
                LogUtils.i("csj=" + chuangshanjiaWeight + "gdt=" + tengxunWeight +
                        "qc=" + qcWeight + "ks=" + ksWeight + "current=" + currentWeight);

                if (allWeight == 0) {//企创
                    ToastUtils.show(mActivity, "qc");
                    if (mHaveQC) {
                        showQcAd(mActivity);
                    } else {
                        getQcAd(mActivity, mAdidBean, mAdId, true);
                    }
                } else if (currentWeight <= chuangshanjiaWeight) {                          // 穿山甲渠道
                    //calculateClickNum();//统计点击次数
                    ToastUtils.show(mActivity, "csj");
                    showCsjAd(mActivity);
                } else if (currentWeight <= chuangshanjiaWeight + tengxunWeight) {          // 广点通渠道
                    //calculateClickNum();//统计点击次数
                    ToastUtils.show(mActivity, "gdt");
                    showGdtAd(mActivity);
                } else if (currentWeight <= chuangshanjiaWeight + tengxunWeight + baiWeight) {
                    //calculateClickNum();//统计点击次数
                    ToastUtils.show(mActivity, "bqt");
                    showBQTAd(mActivity);
                } else if (currentWeight <= chuangshanjiaWeight + tengxunWeight + baiWeight + ksWeight) {
                    //calculateClickNum();//统计点击次数
                    ToastUtils.show(mActivity, "ks");
                    showKsAd(mActivity);
                } else {// 企创渠道
                    //calculateClickNum();//统计点击次数
                    ToastUtils.show(mActivity, "qc");
                    if (mHaveQC) {
                        showQcAd(mActivity);
                    } else {
                        getQcAd(mActivity, mAdidBean, mAdId, true);
                    }
                }
            }
        };

        mPostDownTime.start();

        Looper.loop();//增加部分
    }

    private void showBQTAd (final Activity activity) {
        if (null != baiduNatives && !baiduNatives.isEmpty()) {
            List<View> adViews = new ArrayList<>();

            for (NativeResponse nativeResponse : baiduNatives) {
                final FeedNativeView feedNativeView = new FeedNativeView(activity);

                if (nativeResponse instanceof XAdNativeResponse) {
                    XAdNativeResponse xAdNativeResponse = (XAdNativeResponse) nativeResponse;
                    feedNativeView.setAdData(xAdNativeResponse);
                    adViews.add(feedNativeView);

                    xAdNativeResponse.registerViewForInteraction(feedNativeView.getRootView(),
                            new NativeResponse.AdInteractionListener() {
                                @Override
                                public void onAdClick () {
                                    if (mListener != null) {
                                        mListener.onAdClicked(feedNativeView, ErrorUtil.BQT);
                                    }
                                }

                                @Override
                                public void onADExposed () {
                                    if (mListener != null) {
                                        mListener.onAdExposure(ErrorUtil.BQT);
                                    }
                                }

                                @Override
                                public void onADExposureFailed (int i) {
                                    if (mListener != null) {
                                        mListener.onAdError(ErrorUtil.BQT, "BQT");
                                    }
                                }

                                @Override
                                public void onADStatusChanged () {

                                }

                                @Override
                                public void onAdUnionClick () {

                                }
                            });
                }
            }

            if (!adViews.isEmpty()) {
                final View view = adViews.get(0);
                //计算宽高,调用measure方法之后就可以获取宽高
                int width  = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                view.measure(width, height);

                int measuredWidth  = view.getMeasuredWidth();// 获取宽度
                int measuredHeight = view.getMeasuredHeight();// 获取高度

                if (mListener != null) {
                    mListener.onAdViewSuccess(adViews, measuredWidth, measuredHeight, ErrorUtil.BQT);
                }
            }
        }
    }

    private void getBQTAd (final Activity activity, final String adid) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                baiduNativeManager = new BaiduNativeManager(activity, adid);

                // 若与百度进行相关合作，可使用如下接口上报广告的上下文
                RequestParameters requestParameters = new RequestParameters.Builder()
                        .downloadAppConfirmPolicy(RequestParameters.DOWNLOAD_APP_CONFIRM_ONLY_MOBILE)
                        // 用户维度：用户性别，取值：0-unknown，1-male，2-female
                        .addExtra(ArticleInfo.USER_SEX, "1")
                        // 用户维度：收藏的小说ID，最多五个ID，且不同ID用'/分隔'
                        .addExtra(ArticleInfo.FAVORITE_BOOK, "这是小说的名称1/这是小说的名称2/这是小说的名称3")
                        // 内容维度：小说、文章的名称
                        .addExtra(ArticleInfo.PAGE_TITLE, "测试书名")
                        // 内容维度：小说、文章的ID
                        .addExtra(ArticleInfo.PAGE_ID, "10930484090")
                        // 内容维度：小说分类，一级分类和二级分类用'/'分隔
                        .addExtra(ArticleInfo.CONTENT_CATEGORY, "一级分类/二级分类")
                        // 内容维度：小说、文章的标签，最多10个，且不同标签用'/分隔'
                        .addExtra(ArticleInfo.CONTENT_LABEL, "标签1/标签2/标签3")
                        .build();

                baiduNativeManager.loadFeedAd(requestParameters, new BaiduNativeManager.FeedAdListener() {
                    @Override
                    public void onNativeLoad (List<NativeResponse> list) {
                        baiduNatives = list;
                        mHaveBQT = true;
                        mBQTState = 1;
                    }

                    @Override
                    public void onNativeFail (int i, String s) {
                        mBQTState = -1;
                        mHaveBQT = false;
                        if (mListener != null) {
                            mListener.onAdError(ErrorUtil.BQT, s);
                        }
                    }

                    @Override
                    public void onNoAd (int i, String s) {

                    }

                    @Override
                    public void onVideoDownloadSuccess () {

                    }

                    @Override
                    public void onVideoDownloadFailed () {

                    }

                    @Override
                    public void onLpClosed () {

                    }
                });
            }
        });
    }

    private void showKsAd (final Activity activity) {
        ksFeedAd.setAdInteractionListener(new KsFeedAd.AdInteractionListener() {
            @Override
            public void onAdClicked () {
                if (mListener != null) {
                    mListener.onAdClicked(ErrorUtil.KS);
                }
            }

            @Override
            public void onAdShow () {
                mListener.onAdExposure(ErrorUtil.KS);
            }

            @Override
            public void onDislikeClicked () {
                if (mListener != null) {
                    mListener.onAdClose(ksFeedAd.getFeedView(activity), ErrorUtil.KS);
                }
            }
        });

        final View view = ksFeedAd.getFeedView(activity);
        //计算宽高,调用measure方法之后就可以获取宽高
        int width  = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(width, height);

        int measuredWidth  = view.getMeasuredWidth();// 获取宽度
        int measuredHeight = view.getMeasuredHeight();// 获取高度

        if (mListener != null) {
            mListener.onAdViewSuccess(ksAdViews, measuredWidth, measuredHeight, ErrorUtil.KS);
        }
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
                KsLoadManager ksLoadManager = KsAdSDK.getLoadManager();

                if(null == ksLoadManager){
                    return;
                }

                ksLoadManager.loadConfigFeedAd(ksScene,
                                new KsLoadManager.FeedAdListener() {
                                    @Override
                                    public void onError (int i, String s) {
                                        mKSState = -1;
                                        mHaveKS = false;
                                        if (mListener != null) {
                                            mListener.onAdError(ErrorUtil.KS, s);
                                        }
                                    }

                                    @Override
                                    public void onFeedAdLoad (@Nullable List<KsFeedAd> list) {
                                        if (null != list && !list.isEmpty()) {
                                            ksAdViews = new ArrayList<>();

                                            for (KsFeedAd ksFeedAd : list) {
                                                ksAdViews.add(ksFeedAd.getFeedView(activity));
                                            }

                                            ksFeedAd = list.get(0);
                                            mHaveKS = true;
                                            mKSState = 1;
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
                                    mListener.onADManager(sQcInfoOneAd);
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
                                mListener.onADManager(sQcInfoOneAd);
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

        int screenWidth      = DeviceUtil.getScreenWidth(activity);
        int expressViewWidth = 340;
        if (mAdWigth == 0) {//和屏幕一样大
            expressViewWidth = DeviceUtil.px2dip(activity, screenWidth);
        } else {//修改
            expressViewWidth = (int) mAdWigth;
        }
        ADSize adSize = new ADSize(expressViewWidth, ADSize.AUTO_HEIGHT);
        //ADSize adSize = new ADSize(640, ADSize.AUTO_HEIGHT);
        mADManager = new NativeExpressAD(activity, adSize, adid, new NativeExpressAD.NativeExpressADListener() {
            @Override
            public void onNoAD (AdError adError) {
                LogUtils.d("onNoAD");
                mTXState = -1;
                downTimeHandFinish();//判断三个请求是否结束,GDT
                //gdt
                if (mGdtOneAdViewList != null && mGdtOneAdViewList.size() > 0) {
                    for (NativeExpressADView nativeExpressADView : mGdtOneAdViewList) {
                        nativeExpressADView.destroy();
                    }
                    mGdtOneAdViewList.clear();
                    mCSJOneAdsList = null;
                }
                mADManager = null;
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
                    mListener.onADManager(sQcInfoOneAd);
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
        float expressViewWidth  = 320;
        float expressViewHeight = 0;
        int   screenWidth       = DeviceUtil.getScreenWidth(activity);
        if (mAdWigth == 0) {//和屏幕一样大
            expressViewWidth = DeviceUtil.px2dip(activity, screenWidth);
        } else {//修改
            expressViewWidth = mAdWigth;
        }

        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(320, 140)
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
                if (mCSJOneAdsList != null) {
                    mCSJOneAdsList.clear();
                    mCSJOneAdsList = null;
                }

                if (mCSJAdViewMap != null) {
                    mCSJAdViewMap.clear();
                    mCSJAdViewMap = null;
                }
                mTTAdNative = null;
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
    private void showGdtAd (final Activity mActivity) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                if (mListener != null) {
                    List<View> list = new ArrayList<>();
                    for (NativeExpressADView nativeExpressADView : mGdtOneAdViewList) {
                        nativeExpressADView.render();
                        list.add(nativeExpressADView);
                    }
                    final View view = mGdtOneAdViewList.get(0);
                    //计算宽高,调用measure方法之后就可以获取宽高
                    int width  = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    view.measure(width, height);

                    int measuredWidth  = view.getMeasuredWidth();// 获取宽度
                    int measuredHeight = view.getMeasuredHeight();// 获取高度

                    //mGdtOneAdView.render();
                    mListener.onADManager(sQcInfoOneAd);
                    mListener.onAdViewSuccess(list, measuredWidth, measuredHeight, ErrorUtil.GDT);
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
                                    mListener.onADManager(sQcInfoOneAd);
                                    mListener.onAdViewSuccess(mCSJAdsList, 0, 0, ErrorUtil.CSJ);
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
                                    mListener.onADManager(sQcInfoOneAd);
                                    mListener.onAdViewSuccess(mCSJAdsList, v, v1, ErrorUtil.CSJ);
                                }
                            }
                        }
                    });

                    //不喜欢和下载回调
                    bindAdListener(activity, ad, i);
                    //渲染
                    ad.render();
                }
            }
        });
    }

    /**
     * 穿山甲回调
     */
    private void bindAdListener (Activity activity, final TTNativeExpressAd ad, final int i) {
        //dislike设置 使用默认个性化模板中默认dislike弹出样式
        ad.setDislikeCallback(activity, new TTAdDislike.DislikeInteractionCallback() {

            @Override
            public void onShow () {

            }

            @Override
            public void onSelected (int i, String s, boolean b) {
                View expressAdView = ad.getExpressAdView();
                //用户选择不喜欢原因后，移除广告展示
                LogUtils.e("清除的数据的位置=" + i);
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
                    String layout = mAdidBean.getLayout();
                    if (TextUtils.isEmpty(layout)) {
                        layout = "0";
                    }
                    float realyWidth  = 0;
                    int   screenWidth = DeviceUtil.getScreenWidth(activity);
                    if (mAdWigth == 0) {//和屏幕一样大
                        realyWidth = DeviceUtil.px2dip(activity, screenWidth);
                    } else {//修改
                        realyWidth = mAdWigth;
                    }
                    mOneFrameLayout = new QcOneInfoAdView(activity, null, mNativeBean, layout, realyWidth, mListener);
                    List<View> list = new ArrayList<>();
                    list.add(mOneFrameLayout);
                    mOneFrameLayout.render();

                    //计算宽高,调用measure方法之后就可以获取宽高
                    int width  = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                    mOneFrameLayout.measure(width, height);

                    int measuredWidth  = mOneFrameLayout.getMeasuredWidth();// 获取宽度
                    int measuredHeight = mOneFrameLayout.getMeasuredHeight();// 获取高度

                    //view传递出去
                    mListener.onADManager(sQcInfoOneAd);
                    mListener.onAdViewSuccess(list, measuredWidth, measuredHeight, ErrorUtil.QC);
                }
            }
        });


    }

    /**
     * 手动结束倒计时
     * 优化倒计时权重,需要在不同sdk请求后,判断其他sdk有没有请求结束,然后手动结束倒计时
     */
    private void downTimeHandFinish () {
        if (mQCState != 0
                && mCSJState != 0
                && mTXState != 0
                && mBQTState != 0
                && mKSState != 0
                && mPostDownTime != null) {
            mPostDownTime.onFinish();
            LogUtils.e("--------手动调用了结束--------");
        }
    }
}
