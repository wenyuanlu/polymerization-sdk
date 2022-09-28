package com.corpize.sdk.mobads.admanager;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.baidu.mobads.sdk.api.RewardVideoAd;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.corpize.sdk.mobads.TTAdManagerHolder;
import com.corpize.sdk.mobads.bean.AdResponseBean;
import com.corpize.sdk.mobads.bean.AdidBean;
import com.corpize.sdk.mobads.bean.AdmJsonBean;
import com.corpize.sdk.mobads.common.CommonUtils;
import com.corpize.sdk.mobads.common.Constants;
import com.corpize.sdk.mobads.common.ErrorUtil;
import com.corpize.sdk.mobads.listener.RewardVideoQcAdListener;
import com.corpize.sdk.mobads.utils.LogUtils;
import com.corpize.sdk.mobads.utils.QcHttpUtil;
import com.corpize.sdk.mobads.utils.StringToJsonUtils;
import com.corpize.sdk.mobads.utils.ToastUtils;
import com.corpize.sdk.mobads.view.QcAdVideoActivity;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsRewardVideoAd;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.SdkConfig;
import com.qq.e.ads.cfg.MultiProcessFlag;
import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.comm.util.AdError;

import java.util.List;
import java.util.Map;

/**
 * author: yh
 * date: 2020-02-11 23:35
 * description: 激励视频广告
 */
public class RewardVideoManager {

    private static RewardVideoManager      sQcRewardAd;
    private        CountDownTimer          mPostDownTime;      //同时发送的三个请求的倒计时
    private        AdidBean                mAdidBean;
    private        String                  mAdId;
    private        RewardVideoQcAdListener mListener;

    //企创
    public  AdmJsonBean    admJsonBean;       //企创 视频类广告返回数据 205
    private AdResponseBean mAdBack;

    //广点通
    private RewardVideoAD mTxRewardVideoAD; //广点通 激励视频

    //穿山甲
    private TTAdNative      mTTAdNative;
    private TTRewardVideoAd mCSJRewardVideoAd;
    private boolean         mHasShowDownloadActive = false;

    //百青藤(激励视频普遍要加载4次左右才能有数据)
    private RewardVideoAd mRewardVideoAd;

    private boolean mHaveQC;
    private boolean mHaveCSJ;
    private boolean mHaveTX;
    private boolean mHaveBQT;
    private boolean mHaveKS;
    private int     mShowType = 0;//要展示的当前的广告渠道,1=企创 2=穿山甲 3=广点通 4=baidu 5快手

    //快手
    private KsRewardVideoAd ksRewardVideoAd;

    /**
     * 单例模式
     */
    public static RewardVideoManager get () {
        if (sQcRewardAd == null) {
            sQcRewardAd = new RewardVideoManager();
        }
        return sQcRewardAd;
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
        mShowType = 0;
    }

    /**
     * 清理
     */
    public void destroy () {
        //gdt
        mTxRewardVideoAD = null;

        //csj
        mCSJRewardVideoAd = null;
        mTTAdNative = null;

        //bqt
        mRewardVideoAd = null;

        mListener = null;
        sQcRewardAd = null;
    }

    /**
     * 获取控件回调
     */
    public RewardVideoQcAdListener getListener () {
        return mListener;
    }

    /**
     * 插屏广告权重计算
     */
    public void initWeight (final Activity mActivity, final AdidBean adsSdk, final String adId, RewardVideoQcAdListener listener) {
        initData();//初始化数据
        mAdidBean = adsSdk;
        mAdId = adId;
        mListener = listener;

        final AdidBean.SdkBean sdk = adsSdk.getSdk();

        if (sdk != null) {//banner广告,同时发送三个个请求
            //获取穿山甲ad
            AdidBean.SdkBean.ChuangshanjiaBean chuangshanjia = sdk.getChuangshanjia();
            String                             csjAdid       = "";
            if (chuangshanjia != null) {
                csjAdid = Constants.IS_INIT_SIMPLE_DATA ? Constants.CSJSimpleData.REWARD_VIDEO_AD_ID : chuangshanjia.getAdid();
                //获取穿山甲ad
                getCjsAd(mActivity, csjAdid, false);
            }

            //获取广点通ad
            AdidBean.SdkBean.TengxunBean tengxun = sdk.getTengxun();
            String                       gdtPosId;
            if (tengxun != null) {
                gdtPosId = Constants.IS_INIT_SIMPLE_DATA ? Constants.GDTSimpleData.REWARD_VIDEO_AD_ID : tengxun.getAdid();
                //获取广点通ad
                getGdtAd(mActivity, gdtPosId, false);
            }

            //获取百青藤
            AdidBean.SdkBean.BaiQingTeng bai  = sdk.getBai();
            String                       adid = "";
            if (bai != null) {
                adid = Constants.IS_INIT_SIMPLE_DATA ? Constants.BaiduSimpleData.REWARD_VIDEO_AD_ID : bai.getAdid();
                //获取百青藤
                getBqtAd(mActivity, adid, false);
            }

            //快手
            AdidBean.SdkBean.KuaiShouBean ks = sdk.getKuaishou();
            Long                          ksAdid;
            if (ks != null) {
                ksAdid = Constants.IS_INIT_SIMPLE_DATA ? Constants.KSSimpleData.KS_REWARD_VIDEO : Long.valueOf(ks.getAdid());
                //获取快手
                getKs(mActivity, ksAdid,false);
            }
            //获取企创的ad
            getQcAd(mActivity, adsSdk, adId, false);

            //倒计时确定权重
            countDown(mActivity, sdk);
        } else {
            //sdk未返回数据,则直接展示企创的广告
            getQcAd(mActivity, adsSdk, adId, true);
        }
    }

    /**
     * 倒计时确定权重
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
                //获取对应的权重
                int chuangshanjiaWeight = 0;    //穿山甲权重
                int tengxunWeight       = 0;    //广点通权重
                int baiWeigth           = 0;    //百青藤权重
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
                        baiWeigth = bai.getWeight();
                    }
                }

                if (mHaveKS) {
                    AdidBean.SdkBean.KuaiShouBean ks = sdk.getKuaishou();
                    if (ks != null) {
                        ksWeight = ks.getWeight();
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

                allWeight = chuangshanjiaWeight + tengxunWeight + baiWeigth + ksWeight + qcWeight;

                int currentWeight = CommonUtils.getCompareRandow(allWeight);// 1 ~ allWeight的任意整数
                LogUtils.d("csj=" + chuangshanjiaWeight + "gdt=" + tengxunWeight +
                        "bqt=" + baiWeigth + "ks" + ksWeight +
                        "qc=" + qcWeight + "current=" + currentWeight);

                if (allWeight == 0) {//企创
                    ToastUtils.show(mActivity, "qc");
                    if (mHaveQC) {
                        showQcAd(mActivity, false);
                    } else {
                        getQcAd(mActivity, mAdidBean, mAdId, true);
                    }
                } else if (currentWeight <= chuangshanjiaWeight) {              // 穿山甲渠道
                    ToastUtils.show(mActivity, "csj");
                    showCsjAd(mActivity, false);
                } else if (currentWeight <= chuangshanjiaWeight + tengxunWeight) {          // 广点通渠道
                    ToastUtils.show(mActivity, "gdt");
                    showGdtAd(mActivity, false);
                } else if (currentWeight <= chuangshanjiaWeight + tengxunWeight + baiWeigth) {  //百青藤
                    ToastUtils.show(mActivity, "bqt");
                    showBqtAd(mActivity, false);
                } else if (currentWeight <= chuangshanjiaWeight + tengxunWeight + baiWeigth + ksWeight) {//快手
                    ToastUtils.show(mActivity, "ks");
                    showKsAd(mActivity, false);
                } else {// 企创渠道
                    //calculateClickNum();//统计点击次数
                    ToastUtils.show(mActivity, "qc");
                    if (mHaveQC) {
                        showQcAd(mActivity, false);
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
     * 快手
     */
    private void getKs (final Activity activity, final long adid, final boolean isShow) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                KsScene.Builder builder = new KsScene.Builder(adid)
                        .screenOrientation(SdkConfig.SCREEN_ORIENTATION_PORTRAIT);
                KsScene scene = builder.build(); // 此为测试posId，请联系快手平台申请正式posId
                // 请求的期望屏幕方向传递为1，表示期望为竖屏
                KsAdSDK.getLoadManager().loadRewardVideoAd(scene, new KsLoadManager.RewardVideoAdListener() {
                    @Override
                    public void onError (int code, String msg) {
                        if (mListener != null) {
                            mListener.onAdError(ErrorUtil.KS, msg);
                        }
                    }

                    @Override
                    public void onRequestResult (int adNumber) {
                        if(0 != adNumber){
                            mHaveKS = true;
                        }

                        if (mListener != null) {
                            mListener.onADExposure(ErrorUtil.KS);
                        }
                    }

                    @Override
                    public void onRewardVideoAdLoad (@Nullable List<KsRewardVideoAd> adList) {
                        if (null != adList && !adList.isEmpty()) {
                            mHaveKS = true;
                            ksRewardVideoAd = adList.get(0);

                            if(isShow){
                                ksRewardVideoAd.setRewardAdInteractionListener(
                                        new KsRewardVideoAd.RewardAdInteractionListener() {
                                            @Override
                                            public void onAdClicked () {
                                                if (mListener != null) {
                                                    mListener.onAdClicked(ErrorUtil.KS);
                                                }
                                            }

                                            @Override
                                            public void onPageDismiss () {
                                                if (mListener != null) {
                                                    mListener.onAdClose();
                                                }
                                            }

                                            @Override
                                            public void onVideoPlayError (int i, int i1) {
                                                if (mListener != null) {
                                                    mListener.onAdError(ErrorUtil.KS, "激励视频播放失败");
                                                }
                                            }

                                            @Override
                                            public void onVideoPlayEnd () {
                                                if (mListener != null) {
                                                    mListener.onAdCompletion();
                                                }
                                            }

                                            @Override
                                            public void onVideoPlayStart () {
                                                if (mListener != null) {
                                                    mListener.onADManager(sQcRewardAd);
                                                    mListener.onADReceive(sQcRewardAd, ErrorUtil.QC);
                                                }
                                            }

                                            @Override
                                            public void onRewardVerify () {
                                                //激励视频触发激励（观看视频大于一定时长或者视频播放完毕）
                                                LogUtils.i("onReward：");
                                            }
                                        });
                                if (ksRewardVideoAd != null) {
                                    ksRewardVideoAd.showRewardVideoAd(activity, null);
                                }
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
                    if (!TextUtils.isEmpty(adm)) {
                        mHaveQC = true;
                        mAdBack = response;
                    }

                    if (isShow) {
                        showQcAd(activity, false);
                    }
                } else {
                    if (isShow) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run () {
                                if (mListener != null) {
                                    mListener.onAdError(ErrorUtil.QC, ErrorUtil.NOAD);
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
                                mListener.onAdError(ErrorUtil.QC, erro + code);
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
    private void getGdtAd (final Activity mActivity, String adid, final boolean isShow) {
        /*仅限于和 DownloadService 的不在同一进程的 Activity*/
        MultiProcessFlag.setMultiProcess(true);
        // 1. 初始化激励视频广告
        mTxRewardVideoAD = new RewardVideoAD(mActivity, adid, new RewardVideoADListener() {
            @Override
            public void onADLoad () {
                //广告加载成功，可在此回调后进行广告展示
                mHaveTX = true;
                LogUtils.d("获取到视频");
                if (isShow) {
                    showGdtAd(mActivity, false);
                }
            }

            @Override
            public void onVideoCached () {
                //视频素材缓存成功，可在此回调后进行广告展示
                LogUtils.i("onVideoCached：");
            }

            @Override
            public void onADShow () {
                //激励视频广告页面展示
                LogUtils.i("onADShow：");
                if (mListener != null) {
                    mListener.onADExposure(ErrorUtil.GDT);
                }
            }

            @Override
            public void onADExpose () {
                //激励视频广告曝光
                LogUtils.i("onADExpose：");
            }

            @Override
            public void onReward (Map<String, Object> map) {
                //激励视频触发激励（观看视频大于一定时长或者视频播放完毕）
                LogUtils.i("onReward：");
            }

            @Override
            public void onADClick () {
                //激励视频广告被点击
                LogUtils.i("onADClick：");
                if (mListener != null) {
                    mListener.onAdClicked(ErrorUtil.GDT);
                }
            }

            @Override
            public void onVideoComplete () {
                //激励视频播放完毕
                LogUtils.i("onVideoComplete：");
                if (mListener != null) {
                    mListener.onAdCompletion();
                }
            }

            @Override
            public void onADClose () {
                //激励视频广告被关闭
                LogUtils.i("onADClose：");
                if (mListener != null) {
                    mListener.onAdClose();
                }
            }

            @Override
            public void onError (AdError adError) {
                //广告流程出错
                LogUtils.i("onError：" + adError.getErrorMsg() + adError.getErrorCode());
                if (mListener != null) {
                    //mListener.onAdError(ErrorUtil.GDT, adError.getErrorMsg() + adError.getErrorCode());
                }
            }
        });

        // 2. 加载激励视频广告
        mTxRewardVideoAD.loadAD();

    }

    /**
     * 获取百青藤
     */
    private void getBqtAd (final Activity activity, String adid, final boolean isShow) {
        RewardVideoAd.RewardVideoAdListener rewardVideoAdListener = new RewardVideoAd.RewardVideoAdListener() {
            @Override
            public void onAdShow () {
                LogUtils.d("onAdShow");
                if (mListener != null) {
                    mListener.onADExposure(ErrorUtil.BQT);
                }
            }

            @Override
            public void onAdClick () {
                LogUtils.d("onAdClick");
                if (mListener != null) {
                    mListener.onAdClicked(ErrorUtil.BQT);
                }
            }

            @Override
            public void onAdClose (float v) {
                LogUtils.d("onAdClose");
                if (mListener != null) {
                    mListener.onAdClose();
                }
            }

            @Override
            public void onAdFailed (String s) {
                LogUtils.d("onAdFailed=" + s);
                if (mListener != null) {
                    mListener.onAdError(ErrorUtil.BQT, s);
                }
            }

            @Override
            public void onVideoDownloadSuccess () {
                LogUtils.d("onVideoDownloadSuccess");
                mHaveBQT = true;
                if (isShow) {
                    showBqtAd(activity, false);
                }
            }

            @Override
            public void onVideoDownloadFailed () {
                LogUtils.d("onVideoDownloadFailed");
            }

            @Override
            public void playCompletion () {
                LogUtils.d("playCompletion");
                if (mListener != null) {
                    mListener.onAdCompletion();
                }
            }

            @Override
            public void onAdSkip (float v) {
                LogUtils.d("onAdSkip");
                if (mListener != null) {
                    mListener.onAdClose();
                }
            }

            @Override
            public void onAdLoaded () {
                mHaveBQT = true;
                LogUtils.d("onAdLoaded");
            }
        };
        mRewardVideoAd = new RewardVideoAd(activity, adid, rewardVideoAdListener, false);
        mRewardVideoAd.load();
    }

    /**
     * 获取穿山甲ad
     */
    private void getCjsAd (final Activity mActivity, String codeId, final boolean isShow) {
        mTTAdNative = TTAdManagerHolder.get().createAdNative(mActivity);
        TTAdManagerHolder.get().requestPermissionIfNecessary(mActivity);
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(codeId)
                .setSupportDeepLink(true)
                .setImageAcceptedSize(1080, 1920)
                .setUserID("1234")//用户id,必传参数
                .setOrientation(TTAdConstant.VERTICAL) //必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                .build();

        mTTAdNative.loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError (int code, String message) {
                LogUtils.d("onError：" + message + code);
            }

            @Override
            public void onRewardVideoCached () {
                //视频广告加载后，视频资源缓存到本地的回调，在此回调后，播放本地视频，流畅不阻塞。
                LogUtils.d("onRewardVideoCached");
            }

            @Override
            public void onRewardVideoAdLoad (final TTRewardVideoAd ad) {
                //视频广告的素材加载完毕，比如视频url等，在此回调后，可以播放在线视频，网络不好可能出现加载缓冲，影响体验。
                LogUtils.d("onRewardVideoAdLoad");
                mCSJRewardVideoAd = ad;
                mHaveCSJ = true;
                if (isShow) {
                    showCsjAd(mActivity, false);
                }
            }
        });
    }

    /**
     * 展示广点通ad
     */
    private void showGdtAd (final Activity activity, boolean isRealyShow) {
        if (!isRealyShow) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    mShowType = 3;
                    if (mListener != null) {
                        mListener.onADManager(sQcRewardAd);
                        mListener.onADReceive(sQcRewardAd, ErrorUtil.QC);
                    }
                }
            });
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                //广告展示检查1：广告成功加载，此处也可以使用videoCached来实现视频预加载完成后再展示激励视频广告的逻辑
                if (!mTxRewardVideoAD.hasShown()) {//广告展示检查2：当前广告数据还没有展示过
                    long delta = 1000;//建议给广告过期时间加个buffer，单位ms，这里demo采用1000ms的buffer
                    //广告展示检查3：展示广告前判断广告数据未过期
                    if (SystemClock.elapsedRealtime() < (mTxRewardVideoAD.getExpireTimestamp() - delta)) {
                        mTxRewardVideoAD.showAD();
                    } else {
                        if (mListener != null) {
                            mListener.onAdError(ErrorUtil.GDT, "激励视频广告已过期，请再次请求广告后进行广告展示！");
                        }
                    }
                } else {
                    if (mListener != null) {
                        mListener.onAdError(ErrorUtil.GDT, "此条广告已经展示过，请再次请求广告后进行广告展示！");
                    }
                }
            }
        });

    }

    /**
     * 展示穿山甲ad
     */
    private void showCsjAd (final Activity activity, boolean isRealyShow) {
        if (!isRealyShow) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    mShowType = 2;
                    if (mListener != null) {
                        mListener.onADManager(sQcRewardAd);
                        mListener.onADReceive(sQcRewardAd, ErrorUtil.CSJ);
                    }
                }
            });
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                mCSJRewardVideoAd.showRewardVideoAd(activity);
                mCSJRewardVideoAd.setRewardAdInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {

                    @Override
                    public void onAdShow () {
                        //视频广告展示回调
                        LogUtils.i("onADReceive");
                        if (mListener != null) {
                            mListener.onADExposure(ErrorUtil.CSJ);
                        }
                    }

                    @Override
                    public void onAdVideoBarClick () {
                        //广告的下载bar点击回调
                        LogUtils.i("onAdVideoBarClick");
                        if (mListener != null) {
                            mListener.onAdClicked(ErrorUtil.CSJ);
                        }
                    }

                    @Override
                    public void onAdClose () {
                        //视频广告关闭回调
                        LogUtils.i("onAdClose");
                        if (mListener != null) {
                            mListener.onAdClose();
                        }
                    }

                    //视频播放完成回调
                    @Override
                    public void onVideoComplete () {
                        //视频广告播放完毕回调
                        LogUtils.i("视频播放完成回调:onVideoComplete");
                        if (mListener != null) {
                            mListener.onAdCompletion();
                        }
                    }

                    @Override
                    public void onVideoError () {
                        LogUtils.i("onVideoError");
                        if (mListener != null) {
                            mListener.onAdError(ErrorUtil.CSJ, "onVideoError");
                        }
                    }

                    //视频播放完成后，奖励验证回调，rewardVerify：是否有效，rewardAmount：奖励梳理，rewardName：奖励名称
                    @Override
                    public void onRewardVerify (boolean b, int i, String s, int i1, String s1) {
                        LogUtils.i("onRewardVerify");

                        if (mListener != null) {
                            mListener.onAdCompletion();
                        }
                    }

                    @Override
                    public void onSkippedVideo () {
                        //跳过视频
                        LogUtils.i("onSkippedVideo");

                        if (mListener != null) {
                            mListener.onAdClose();
                        }
                    }
                });

                mCSJRewardVideoAd.setDownloadListener(new TTAppDownloadListener() {
                    @Override
                    public void onIdle () {
                        mHasShowDownloadActive = false;
                    }

                    @Override
                    public void onDownloadActive (long totalBytes, long currBytes, String fileName, String appName) {
                        if (!mHasShowDownloadActive) {
                            mHasShowDownloadActive = true;
                            LogUtils.i("下载中，点击下载区域暂停:onDownloadActive");
                            // TToast.show(RewardVideoActivity.this, "下载中，点击下载区域暂停", Toast.LENGTH_LONG);
                        }
                    }

                    @Override
                    public void onDownloadPaused (long totalBytes, long currBytes, String fileName, String appName) {
                        LogUtils.i("下载暂停，点击下载区域继续:onDownloadActive");
                        // TToast.show(RewardVideoActivity.this, "下载暂停，点击下载区域继续", Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onDownloadFailed (long totalBytes, long currBytes, String fileName, String appName) {
                        LogUtils.i("下载失败，点击下载区域重新下载:onDownloadActive");
                        // TToast.show(RewardVideoActivity.this, "下载失败，点击下载区域重新下载", Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onDownloadFinished (long totalBytes, String fileName, String appName) {
                        LogUtils.i("下载完成，点击下载区域重新下载:onDownloadActive");
                        // TToast.show(RewardVideoActivity.this, "下载完成，点击下载区域重新下载", Toast.LENGTH_LONG);
                    }

                    @Override
                    public void onInstalled (String fileName, String appName) {
                        LogUtils.i("安装完成，点击下载区域打开:onDownloadActive");
                        // TToast.show(RewardVideoActivity.this, "安装完成，点击下载区域打开", Toast.LENGTH_LONG);
                    }
                });
            }
        });
    }

    /**
     * 展示快手
     */
    private void showKsAd (final Activity mActivity, boolean isRealyShow) {
        if (!isRealyShow) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    mShowType = 5;
                    if (mListener != null) {
                        mListener.onADManager(sQcRewardAd);
                        mListener.onADReceive(sQcRewardAd, ErrorUtil.KS);
                    }
                }
            });
            return;
        }

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                if (ksRewardVideoAd != null) {
                    ksRewardVideoAd.showRewardVideoAd(mActivity, null);
                }
            }
        });
    }

    /**
     * 展示百青藤
     */
    private void showBqtAd (final Activity mActivity, boolean isRealyShow) {
        if (!isRealyShow) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    mShowType = 4;
                    if (mListener != null) {
                        mListener.onADManager(sQcRewardAd);
                        mListener.onADReceive(sQcRewardAd, ErrorUtil.BQT);
                    }
                }
            });
            return;
        }

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                if (mRewardVideoAd != null) {
                    mRewardVideoAd.show();
                }
            }
        });
    }

    /**
     * 展示企创的广告
     */
    private void showQcAd (final Activity activity, boolean isRealyShow) {
        if (!isRealyShow) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    mShowType = 1;
                    if (mListener != null) {
                        mListener.onADManager(sQcRewardAd);
                        mListener.onADReceive(sQcRewardAd, ErrorUtil.QC);
                    }
                }
            });
            return;
        }

        final String adm = mAdBack.getAdm().trim();
        admJsonBean = StringToJsonUtils.decodeFromJson(adm);
        if (admJsonBean != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run () {
                    if (admJsonBean != null) {
                        Intent intent = new Intent(activity, QcAdVideoActivity.class);
                        intent.putExtra("dataBean", admJsonBean);
                        activity.startActivity(intent);
                    } else {
                        if (mListener != null) {
                            mListener.onAdError(ErrorUtil.QC, ErrorUtil.NOAD);
                        }
                    }
                }
            });

        } else {
            if (mListener != null) {
                mListener.onAdError(ErrorUtil.QC, ErrorUtil.NOAD);
            }
        }
    }

    /**
     * 展示插屏的广告
     */
    public void showAd (final Activity activity) {
        if (mShowType == 1) {//企创
            showQcAd(activity, true);
        } else if (mShowType == 2) {//穿山甲
            showCsjAd(activity, true);
        } else if (mShowType == 3) {//广点通
            showGdtAd(activity, true);
        } else if (mShowType == 4) {//百青藤
            showBqtAd(activity, true);
        } else if (mShowType == 5) {//快手
            showKsAd(activity, true);
        }
    }
}
