package com.corpize.sdk.mobads.admanager;

import android.app.Activity;

import com.corpize.sdk.mobads.bean.AdResponseBean;
import com.corpize.sdk.mobads.bean.AdidBean;
import com.corpize.sdk.mobads.bean.AssetsAudioBean;
import com.corpize.sdk.mobads.bean.AssetsBean;
import com.corpize.sdk.mobads.bean.AssetsLinkBean;
import com.corpize.sdk.mobads.bean.NativeBean;
import com.corpize.sdk.mobads.common.ErrorUtil;
import com.corpize.sdk.mobads.listener.AudioQcAdListener;
import com.corpize.sdk.mobads.utils.MediaPlayerUtil;
import com.corpize.sdk.mobads.utils.QcHttpUtil;

import java.util.List;

/**
 * author: yh
 * date: 2020-02-11 23:35
 * description: 音频广告
 */
public class AudioAdManager {

    private static AudioAdManager    sQcAudioAd;
    private        AudioQcAdListener mListener;
    private        boolean           mHasPlayAd = false;
    private        int               mWidth;
    private        int               mHeight;
    private        String            mUrl;
    private        Activity          mActivity;
    private        List<String>      mImptrackers;

    /**
     * 单例模式
     */
    public static AudioAdManager get () {
        if (sQcAudioAd == null) {
            sQcAudioAd = new AudioAdManager();
        }
        return sQcAudioAd;
    }

    /**
     * 清除广告
     */
    public void destroy () {
        mListener = null;
        mActivity = null;
        mImptrackers = null;
        sQcAudioAd = null;
        /*if (mHasPlayAd) {//不能中断播放音频广告
            //已经播放了,这时候释放
            MediaPlayerUtil.get().stopAndRelease();
        }*/
    }

    /**
     * 展示企创banner的广告
     */
    public void showQcAd (final Activity activity, AdidBean adsSdk, String adId, AudioQcAdListener listener) {
        mListener = listener;
        mActivity = activity;
        mHasPlayAd = false;
        mWidth = adsSdk.getWidth();
        mHeight = adsSdk.getHeight();
        //发送请求获取广告信息
        QcHttpUtil.getAudioAd(activity, adsSdk, adId, new QcHttpUtil.QcHttpOnListener<AdResponseBean>() {

            @Override
            public void OnQcCompletionListener (AdResponseBean response) {
                if (response != null && response.getNative1() != null) {
                    NativeBean native1 = response.getNative1();
                    if (native1 != null) {
                        qcShowAudioAd(activity, native1);
                    }
                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run () {
                            if (mListener != null) {
                                mListener.onADManager(sQcAudioAd);
                                mListener.onAdError(ErrorUtil.NOAD);
                            }
                        }
                    });
                }
            }

            @Override
            public void OnQcErrorListener (final String erro, final int code) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run () {
                        if (mListener != null) {
                            mListener.onADManager(sQcAudioAd);
                            mListener.onAdError(erro + code);
                        }
                    }
                });

            }
        });

    }

    /**
     * 企创 音频广告的展示
     */
    private void qcShowAudioAd (Activity activity, final NativeBean nativeBean) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {

                final AssetsLinkBean link   = nativeBean.getLink();
                List<AssetsBean>     assets = nativeBean.getAssets();
                mImptrackers = nativeBean.getImptrackers();
                if (assets != null && assets.size() > 0) {
                    for (AssetsBean asset : assets) {
                        AssetsAudioBean audio = asset.getAudio();
                        if (audio != null) {
                            mHasPlayAd = true;
                            mUrl = audio.getUrl();
                        }
                    }

                    //遍历结束有数据的时候
                    if (mHasPlayAd){
                        if (mListener != null) {
                            mListener.onADReceive(sQcAudioAd);
                        }
                    }

                } else {
                    if (mListener != null) {
                        mListener.onADManager(sQcAudioAd);
                        mListener.onAdError(ErrorUtil.NOAD);
                    }

                }
            }
        });

    }

    /**
     * 加载广告
     */
    public void addAd () {
        MediaPlayerUtil.getInstance().playVoice(mActivity, mUrl, new MediaPlayerUtil.MediaOnListener() {
            @Override
            public void OnPlayStartListener () {
                if (mListener != null) {
                    mListener.onADExposure();
                    mListener.onADManager(sQcAudioAd);
                }
                //曝光监听
                sendShowExposure(mImptrackers);
            }

            @Override
            public void OnPlayCompletionListener () {
                if (mListener != null) {
                    mListener.onAdCompletion();
                }
            }

            @Override
            public void OnPlayErrorListener (int code, String msg) {
                if (mListener != null) {
                    mListener.onADManager(sQcAudioAd);
                    mListener.onAdError("广告获取成功,播放失败," + msg);
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
                /*if (url.contains("__WIDTH__")) {//宽度替换
                    url = url.replace("__WIDTH__", mWidth + "");
                }
                if (url.contains("__HEIGHT__")) {//高度替换
                    url = url.replace("__HEIGHT__", mHeight + "");
                }*/
                if (url.contains("__TIME_STAMP__")) {//时间戳的替换
                    url = url.replace("__TIME_STAMP__", time + "");
                }

                QcHttpUtil.sendAdExposure(url);

            }
        }

    }

}
