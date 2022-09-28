package com.corpize.sdk.mobads.admanager;

import android.app.Activity;
import android.os.CountDownTimer;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;

import com.baidu.mobads.sdk.api.InterstitialAd;
import com.baidu.mobads.sdk.api.InterstitialAdListener;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
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
import com.corpize.sdk.mobads.listener.InsertQcAdListener;
import com.corpize.sdk.mobads.utils.DeviceUtil;
import com.corpize.sdk.mobads.utils.DialogUtils;
import com.corpize.sdk.mobads.utils.LogUtils;
import com.corpize.sdk.mobads.utils.QcHttpUtil;
import com.corpize.sdk.mobads.utils.ToastUtils;
import com.qq.e.ads.cfg.MultiProcessFlag;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.comm.util.AdError;

import java.util.List;

/**
 * author: yh
 * date: 2020-02-11 23:35
 * description: 插屏广告
 */
public class InsertManager {

    private static InsertManager      sQcInsertAd;
    private        CountDownTimer     mPostDownTime;      //同时发送的三个请求的倒计时
    private        AdidBean           mAdidBean;
    private        String             mAdId;
    private        int                mWidth;
    private        int                mHeight;
    private        InsertQcAdListener mListener;

    //企创
    private AdResponseBean mAdBack;

    //广点通
    private UnifiedInterstitialAD mGdtInterstitialAD;

    //穿山甲
    private TTAdNative        mTTAdNative;
    private TTNativeExpressAd mTtNativeExpressAd;

    //百青藤
    private InterstitialAd mBqtInterAd;

    private boolean mHaveQC;
    private boolean mHaveCSJ;
    private boolean mHaveTX;
    private boolean mHaveBQT;
    private int     mShowType     = 0;//要展示的当前的广告渠道,1=企创 2=穿山甲 3=广点通,4=百青藤
    private int     mQCState      = 0;//0初始,1获取到广告,-1未获取到广告
    private int     mCSJState     = 0;//0初始,1获取到广告,-1未获取到广告
    private int     mTXState      = 0;//0初始,1获取到广告,-1未获取到广告
    private boolean mHaveDownOver = false;//倒计时是否已经加载过


    /**
     * 单例模式
     */
    public static InsertManager get () {
        //不写成单例模式,防止多个页面都需要加载bnanner
        sQcInsertAd = new InsertManager();
        return sQcInsertAd;
    }

    /**
     * 初始化数据
     */
    private void initData () {
        mHaveQC = false;
        mHaveCSJ = false;
        mHaveTX = false;
        mHaveBQT = false;
        mShowType = 0;
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

        //广点通
        if (mGdtInterstitialAD != null) {
            mGdtInterstitialAD.close();
            mGdtInterstitialAD.destroy();
            mGdtInterstitialAD = null;
        }

        //穿山甲
        mTTAdNative = null;
        mTtNativeExpressAd = null;

        //百青藤
        if (mBqtInterAd != null) {
            mBqtInterAd.destroy();
            mBqtInterAd = null;
        }

        //企创
        if (mPostDownTime != null) {
            mPostDownTime.cancel();
            mPostDownTime = null;
        }

        mListener = null;
        sQcInsertAd = null;
    }

    /**
     * 插屏广告权重计算
     */
    public void initWeight (final Activity activity, final AdidBean adsSdk, final String adId, InsertQcAdListener listener) {
        initData();//初始化数据
        mAdidBean = adsSdk;
        mAdId = adId;
        mListener = listener;
        mWidth = adsSdk.getWidth();
        mHeight = adsSdk.getHeight();

        final AdidBean.SdkBean sdk = adsSdk.getSdk();

        if (sdk != null) {//banner广告,同时发送三个请求
            //获取穿山甲ad
            AdidBean.SdkBean.ChuangshanjiaBean chuangshanjia = sdk.getChuangshanjia();
            String                             csjAdid       = "";
            if (chuangshanjia != null) {
                csjAdid = Constants.IS_INIT_SIMPLE_DATA ? Constants.CSJSimpleData.INSERT_AD_ID : chuangshanjia.getAdid();
                //获取穿山甲ad
                getCjsAd(activity, csjAdid, false);
            } else {
                mCSJState = -1;//未下发,则置为未获取到
            }

            //获取广点通ad
            AdidBean.SdkBean.TengxunBean tengxun = sdk.getTengxun();
            String                       gdtPosId;
            if (tengxun != null) {
                gdtPosId = Constants.IS_INIT_SIMPLE_DATA ? Constants.GDTSimpleData.INSERT_AD_ID : tengxun.getAdid();
                //获取广点通ad
                getGdtAd(activity, gdtPosId, false);
            } else {
                mTXState = -1;//未下发,则置为未获取到
            }

            //获取企创的ad
            getQcAd(activity, adsSdk, adId, false);

            //获取百青藤
            AdidBean.SdkBean.BaiQingTeng bai = sdk.getBai();
            if (bai != null) {
                //获取百青藤
                String adid = Constants.IS_INIT_SIMPLE_DATA ? Constants.BaiduSimpleData.INSERT_AD_ID : bai.getAdid();
                getBqtAd(activity, adid, false);
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

                if (mHaveQC) {
                    int supplier = mAdBack.getSupplier();
                    if (supplier == 0) {//0时是兜底,有其他sdk就不用企创的
                        if (!mHaveCSJ && !mHaveTX && !mHaveBQT) {
                            //其他SDK都没有
                            qcWeight = mAdidBean.getWeight();
                        }
                    } else {
                        qcWeight = mAdidBean.getWeight();
                    }

                }


                allWeight = chuangshanjiaWeight + tengxunWeight + qcWeight + baiWeight;

                int currentWeight = CommonUtils.getCompareRandow(allWeight);// 1 ~ allWeight的任意整数
                LogUtils.i("csj=" + chuangshanjiaWeight + "gdt=" + tengxunWeight +
                        "bqt=" + baiWeight + "qc=" + qcWeight + "current=" + currentWeight);

                if (allWeight == 0) {//企创
                    ToastUtils.show(mActivity, "qc");
                    if (mHaveQC) {
                        showQcAd(mActivity);
                    } else {
                        getQcAd(mActivity, mAdidBean, mAdId, true);
                    }

                } else if (currentWeight <= chuangshanjiaWeight) {              // 穿山甲渠道
                    //calculateClickNum();//统计点击次数
                    ToastUtils.show(mActivity, "csj");
                    showCsjAd(mActivity);

                } else if (currentWeight <= chuangshanjiaWeight + tengxunWeight) {          // 广点通渠道
                    //calculateClickNum();//统计点击次数
                    ToastUtils.show(mActivity, "gdt");
                    showGdtAd(mActivity);

                } else if (currentWeight <= chuangshanjiaWeight + tengxunWeight + baiWeight) {  //百青藤
                    ToastUtils.show(mActivity, "bqt");
                    showBqtAd(mActivity);

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
                        mQCState = 1;
                    }

                    if (ext != null && ext.getIurl() != null) {
                        mHaveQC = true;
                        mAdBack = response;
                        mQCState = 1;
                    }

                    if (isShow) {
                        mShowType = 1;
                        if (!TextUtils.isEmpty(adm) || ext != null && ext.getIurl() != null) {
                            showQcAd(activity);
                        } else {
                            mQCState = -1;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run () {
                                    if (mListener != null) {
                                        mListener.onAdError(ErrorUtil.QC, ErrorUtil.NOAD);
                                    }
                                }
                            });
                        }
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
                                    mListener.onAdError(ErrorUtil.QC, ErrorUtil.NOAD);
                                }
                            }
                        });

                    } else {
                        downTimeHandFinish();//判断三个请求是否结束,QC
                    }

                }
            }

            @Override
            public void OnQcErrorListener (final String erro, int code) {
                mQCState = -1;
                if (isShow) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run () {
                            if (mListener != null) {
                                mListener.onAdError(ErrorUtil.QC, ErrorUtil.NOAD + erro);
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
    private void getGdtAd (final Activity activity, String adid, final boolean isShow) {
        /*仅限于和 DownloadService 的不在同一进程的 Activity*/
        MultiProcessFlag.setMultiProcess(true);
        mGdtInterstitialAD = new UnifiedInterstitialAD(activity, adid, new UnifiedInterstitialADListener() {
            @Override
            public void onADReceive () {
                LogUtils.e("onADReceive");
                mHaveTX = true;
                mTXState = 1;
                //获取成功,可以展示
                if (isShow) {
                    showGdtAd(activity);
                } else {
                    downTimeHandFinish();//判断三个请求是否结束,GDT
                }
            }

            @Override
            public void onVideoCached () {
                LogUtils.e("onVideoCached");
            }

            @Override
            public void onNoAD (AdError adError) {
                LogUtils.e("onNoAD" + adError.getErrorMsg());
                mTXState = -1;
                /*if (mListener != null) {
                    mListener.onAdError(ErrorUtil.GDT, adError.getErrorMsg());
                }*/
            }

            @Override
            public void onADOpened () {
                LogUtils.e("onADOpened");
            }

            @Override
            public void onADExposure () {
                LogUtils.e("onADExposure");
                if (mListener != null) {
                    mListener.onADExposure(ErrorUtil.GDT);
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
            public void onADClosed () {
                LogUtils.e("onADClosed");
                if (mListener != null) {
                    mListener.onAdClose();
                }
            }

            @Override
            public void onRenderSuccess () {

            }

            @Override
            public void onRenderFail () {

            }
        });

        mGdtInterstitialAD.loadAD();

    }

    /**
     * 获取百青藤广告
     */
    public void getBqtAd (final Activity activity, String adid, final boolean isShow) {
        mBqtInterAd = new InterstitialAd(activity, adid);
        mBqtInterAd.setListener(new InterstitialAdListener() {
            @Override
            public void onAdReady () {
                LogUtils.d("onAdReady");
                mHaveBQT = true;
                if (isShow) {
                    showBqtAd(activity);
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
            public void onAdClick (InterstitialAd interstitialAd) {
                LogUtils.d("onAdClick");
                if (mListener != null) {
                    mListener.onAdClicked(ErrorUtil.BQT);
                }
            }

            @Override
            public void onAdDismissed () {
                LogUtils.d("onAdDismissed");
                if (mListener != null) {
                    mListener.onAdClose();
                }
            }

            @Override
            public void onAdFailed (String s) {
                LogUtils.d("onAdFailed=" + s);
            }
        });
        mBqtInterAd.loadAd();
    }

    /**
     * 获取穿山甲ad
     */
    private void getCjsAd (final Activity activity, String codeId, final boolean isShow) {
        mTTAdNative = TTAdManagerHolder.get().createAdNative(activity);
        TTAdManagerHolder.get().requestPermissionIfNecessary(activity);
        int expressViewWidth = 300;
        int width            = DeviceUtil.px2dip(activity, DeviceUtil.getScreenWidth(activity));//获取屏幕宽度的dp值
        if (width > 0) {
            //expressViewWidth = width * 3 / 5;
        }
        int expressViewHeight = 0;
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setSupportDeepLink(true)
                .setAdCount(1) //请求广告数量为1到3条
                .setExpressViewAcceptedSize(expressViewWidth, expressViewHeight) //期望模板广告view的size,单位dp
                .setImageAcceptedSize(640, 320) //这个参数设置即可，不影响个性化模板广告的size
                .build();

        mTTAdNative.loadInteractionExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError (int i, String message) {
                LogUtils.e("onError=" + message);
                mCSJState = -1;
            }

            @Override
            public void onNativeExpressAdLoad (List<TTNativeExpressAd> list) {
                if (list == null || list.size() == 0) {
                    return;
                }
                //获取成功
                mTtNativeExpressAd = list.get(0);
                bindAdListener(activity, mTtNativeExpressAd, isShow);
                mTtNativeExpressAd.render();//调用render开始渲染广告

            }
        });

    }

    /**
     * 穿山甲回调
     */
    private void bindAdListener (final Activity activity, TTNativeExpressAd ad, final boolean isShow) {
        ad.setExpressInteractionListener(new TTNativeExpressAd.AdInteractionListener() {

            @Override
            public void onAdDismiss () {
                LogUtils.e("onAdDismiss");
                if (mListener != null) {
                    mListener.onAdClose();
                }
            }

            @Override
            public void onAdClicked (View view, int type) {
                LogUtils.e("onAdClicked");
                if (mListener != null) {
                    mListener.onAdClicked(ErrorUtil.CSJ);
                }
            }

            @Override
            public void onAdShow (View view, int type) {
                LogUtils.e("onADExposure");
                if (mListener != null) {
                    mListener.onADExposure(ErrorUtil.CSJ);
                }
            }

            @Override
            public void onRenderFail (View view, String msg, int code) {
                LogUtils.e("onRenderFail");
            }

            @Override
            public void onRenderSuccess (View view, float width, float height) {
                LogUtils.e("onRenderSuccess");
                //返回view的宽高 单位 dp
                mHaveCSJ = true;
                mCSJState = 1;
                if (isShow) {
                    showCsjAd(activity);
                } else {
                    downTimeHandFinish();//判断三个请求是否结束,CSJ
                }

            }
        });

        //下载监听
        //bindAdDownListener(ad);

    }

    /**
     * 穿山甲下载监听
     */
    private void bindAdDownListener (TTNativeExpressAd ad) {
        if (ad.getInteractionType() != TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
            return;
        }
        //可选，下载监听设置
        ad.setDownloadListener(new TTAppDownloadListener() {
            @Override
            public void onIdle () {
                LogUtils.e("点击开始下载");
            }

            @Override
            public void onDownloadActive (long totalBytes, long currBytes, String fileName, String appName) {
                LogUtils.e("下载中");
            }

            @Override
            public void onDownloadPaused (long totalBytes, long currBytes, String fileName, String appName) {
                LogUtils.e("下载暂停");
            }

            @Override
            public void onDownloadFailed (long totalBytes, long currBytes, String fileName, String appName) {
                LogUtils.e("下载失败," + totalBytes + "|" + currBytes + "|" + fileName + "|" + appName);
            }

            @Override
            public void onInstalled (String fileName, String appName) {
                LogUtils.e("下载完成");
            }

            @Override
            public void onDownloadFinished (long totalBytes, String fileName, String appName) {
                LogUtils.e("安装完成");
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
                mShowType = 3;
                if (mListener != null) {
                    mListener.onADManager(sQcInsertAd);
                    mListener.onADReceive(sQcInsertAd, ErrorUtil.GDT);
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
                mShowType = 2;
                if (mListener != null) {
                    mListener.onADManager(sQcInsertAd);
                    mListener.onADReceive(sQcInsertAd, ErrorUtil.CSJ);
                }
            }
        });
    }

    /**
     * 展示百青藤
     */
    public void showBqtAd (final Activity mActivity) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                mShowType = 4;
                if (mListener != null) {
                    mListener.onADManager(sQcInsertAd);
                    mListener.onADReceive(sQcInsertAd, ErrorUtil.QC);
                }
            }
        });
    }

    /**
     * 展示企创的广告
     */
    private void showQcAd (final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                mShowType = 1;
                if (mListener != null) {
                    mListener.onADManager(sQcInsertAd);
                    mListener.onADReceive(sQcInsertAd, ErrorUtil.QC);
                }
            }
        });

    }

    /**
     * 展示插屏的广告
     */
    public void showAd (final Activity activity) {
        if (mShowType == 1) {//企创
            final String webViewAdm = mAdBack.getAdm();
            //计算插屏的宽高
            int screenWidth = DeviceUtil.getScreenWidth(activity);
            int width       = screenWidth * 3 / 5;//屏幕的五分之三
            int higth       = width * mHeight / mWidth;//高度适配

            if (!TextUtils.isEmpty(webViewAdm)) {
                //Webview加载弹框
                DialogUtils.showInsertDialog(activity, mAdBack, webViewAdm, width, higth, mListener);
                if (mListener != null) {
                    mListener.onADExposure(ErrorUtil.QC);
                }
                return;
            }

            //原生加载弹框,其他的监听都在dialog中
            DialogUtils.showInsertDialog(activity, mAdBack, "", width, higth, mListener);

            if (mListener != null) {
                mListener.onADExposure(ErrorUtil.QC);
            }

        } else if (mShowType == 2) {//穿山甲
            if (mTtNativeExpressAd != null) {
                mTtNativeExpressAd.showInteractionExpressAd(activity);
            }

        } else if (mShowType == 3) {//广点通
            if (mGdtInterstitialAD != null) {
                mGdtInterstitialAD.show();
            }
        } else if (mShowType == 4) {//百青藤
            if (mBqtInterAd != null) {
                mBqtInterAd.showAd(activity);
            }

        }
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
