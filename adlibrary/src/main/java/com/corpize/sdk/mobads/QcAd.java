package com.corpize.sdk.mobads;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;

import com.corpize.sdk.mobads.admanager.AudioAdManager;
import com.corpize.sdk.mobads.admanager.BannerManager;
import com.corpize.sdk.mobads.admanager.InfoOneManager;
import com.corpize.sdk.mobads.admanager.InfoThreeManager;
import com.corpize.sdk.mobads.admanager.InsertManager;
import com.corpize.sdk.mobads.admanager.NativeVideoManager;
import com.corpize.sdk.mobads.admanager.RewardVideoManager;
import com.corpize.sdk.mobads.admanager.SplashManager;
import com.corpize.sdk.mobads.bean.AdidBean;
import com.corpize.sdk.mobads.bean.UserBean;
import com.corpize.sdk.mobads.common.Constants;
import com.corpize.sdk.mobads.common.ErrorUtil;
import com.corpize.sdk.mobads.listener.AdInfoListener;
import com.corpize.sdk.mobads.listener.AudioQcAdListener;
import com.corpize.sdk.mobads.listener.BannerQcAdListener;
import com.corpize.sdk.mobads.listener.InfoMoreQcAdListener;
import com.corpize.sdk.mobads.listener.InfoOneQcAdListener;
import com.corpize.sdk.mobads.listener.InsertQcAdListener;
import com.corpize.sdk.mobads.listener.NativeVideoQcAdListener;
import com.corpize.sdk.mobads.listener.QCADListener;
import com.corpize.sdk.mobads.listener.RewardVideoQcAdListener;
import com.corpize.sdk.mobads.listener.SplashQcAdListener;
import com.corpize.sdk.mobads.utils.GPSUtils;
import com.corpize.sdk.mobads.utils.LogUtils;
import com.corpize.sdk.mobads.utils.MiitHelper;
import com.corpize.sdk.mobads.utils.QcHttpUtil;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

import static com.bun.miitmdid.content.ContextKeeper.getApplicationContext;
import static com.corpize.sdk.mobads.utils.DeviceUtil.getPackageName;

/**
 * author: yh
 * date: 2020-02-11 10:34
 * description: TODO:???????????????
 */
public class QcAd {

    //private static Application mApplication;
    private static Context  mContext;
    private static Activity mActivity;
    private static QcAd     sQcAd;
    //private        String      AdsTag;                         //???????????????????????????
    //private        Boolean     mHaveInitBqt = false;           //??????????????????????????????
    //private        Boolean     mHaveInitCsj = false;           //??????????????????????????????
    private        UserBean mUserBean;

    //????????????
    public static QcAd get () {
        if (sQcAd == null) {
            sQcAd = new QcAd();
        }
        return sQcAd;
    }

    private QcAd () {
    }

    /**
     * ?????????
     */
    public void init (Application application, String appid, String secret) {
        if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(secret)) {
            Log.e("QcAD", "?????????????????????[????????????]");
            return;
        }

        String currentProcessName = getCurrentProcessName();
        //?????????sdk
        if (currentProcessName.equals(getPackageName())) {
            // ????????????????????????????????????SDK?????????????????????
            QcAdManager.initSdk(application, appid, secret);
        }

        mContext = application.getApplicationContext();

        //?????????GPRS
        GPSUtils.getInstance(mContext).initLngAndLat();

        initAoid(application);
    }

    private String getCurrentProcessName () {
        int                                         pid                 = Process.myPid();
        String                                      currentProcessName  = "";
        ActivityManager                             activityManager     = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningAppProcesses) {
            if (pid == processInfo.pid) {
                currentProcessName = processInfo.processName;
            }
        }
        return currentProcessName;
    }

    /**
     * ?????????????????????
     */
    public void showLog (boolean showHttp, boolean showLog, boolean showSdk) {
        Constants.setAllLog(showHttp, showLog, showSdk);
    }

    /**
     * ?????????aoid
     */
    private void initAoid (Application application) {
        try {
            //JLibrary.InitEntry(application);
            MiitHelper.getInstanse().getDeviceIds(application, new MiitHelper.AppIdsUpdater() {
                @Override
                public void OnIdsAvalid (@NonNull String ids) {
                    Log.e("eeee", ids);
                    //mTvShowOaid.setText("??????????????????=" + ids);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????????????????
     */
    public void setUserInfo (Map<String, Object> userMap) {
        String name     = (String) userMap.get("name");
        String yob      = (String) userMap.get("yob");
        String gender   = (String) userMap.get("gender");
        String phone    = (String) userMap.get("phone");
        int    marriage = (int) userMap.get("marriage");
        String hobby    = (String) userMap.get("hobby");
        String edu      = (String) userMap.get("edu");
        //????????????
        mUserBean = new UserBean(name, yob, gender, phone, marriage, hobby, edu);
    }

    /**
     * ??????????????????
     */
    public UserBean getUserInfo () {
        return mUserBean;
    }

    /**
     * ????????????
     */
    public void splashAds (final Activity activity, final ViewGroup splashContainer, final TextView skipView,
            final String adId, final SplashQcAdListener listener) {
        mActivity = activity;

        if (TextUtils.isEmpty(adId)) {
            if (listener != null) {
                listener.onAdError(ErrorUtil.QC, ErrorUtil.NOADID);
            }
            return;
        }

        QcHttpUtil.getAdid(adId, new QcHttpUtil.QcHttpOnListener<AdidBean>() {
            @Override
            public void OnQcCompletionListener (AdidBean response) {
                if (response != null) {
                    LogUtils.d("??????????????????=" + new Gson().toJson(response));
                    SplashManager.get().initWeight(activity, splashContainer, skipView, response, adId, listener);

                } else {
                    showErrorListener(activity, ErrorUtil.NOAD_PLACE, listener);
                }
            }

            @Override
            public void OnQcErrorListener (String erro, int code) {
                LogUtils.d("????????????????????????=" + erro);
                showErrorListener(activity, erro + code, listener);
            }
        });
    }

    /**
     * ??????banner??????
     */
    public void bannerAds (final Activity activity, final ViewGroup container, final String adId, final BannerQcAdListener listener) {
        mActivity = activity;

        if (TextUtils.isEmpty(adId)) {
            if (listener != null) {
                listener.onAdError(ErrorUtil.QC, ErrorUtil.NOADID);
            }
            return;
        }

        QcHttpUtil.getAdid(adId, new QcHttpUtil.QcHttpOnListener<AdidBean>() {
            @Override
            public void OnQcCompletionListener (AdidBean response) {
                if (response != null) {
                    LogUtils.d("Banner????????????=" + new Gson().toJson(response));
                    BannerManager.get().initWeight(activity, container, response, adId, listener);

                } else {
                    showErrorListener(activity, ErrorUtil.NOAD_PLACE, listener);
                }
            }

            @Override
            public void OnQcErrorListener (final String erro, final int code) {
                LogUtils.d("Banner??????????????????=" + erro);
                showErrorListener(activity, erro + code, listener);
            }
        });

    }

    /**
     * ??????banner??????(??????????????????????????????)
     */
    public void bannerAds (final Activity activity, final ViewGroup container, final String adId,
            final float adWigth, final BannerQcAdListener listener) {
        mActivity = activity;

        if (TextUtils.isEmpty(adId)) {
            if (listener != null) {
                listener.onAdError(ErrorUtil.QC, ErrorUtil.NOADID);
            }
            return;
        }

        QcHttpUtil.getAdid(adId, new QcHttpUtil.QcHttpOnListener<AdidBean>() {
            @Override
            public void OnQcCompletionListener (AdidBean response) {
                if (response != null) {
                    LogUtils.d("Banner????????????=" + new Gson().toJson(response));
                    BannerManager.get().initWeight(activity, container, response, adId, adWigth, listener);

                } else {
                    showErrorListener(activity, ErrorUtil.NOAD_PLACE, listener);
                }
            }

            @Override
            public void OnQcErrorListener (final String erro, final int code) {
                LogUtils.d("Banner??????????????????=" + erro);
                showErrorListener(activity, erro + code, listener);
            }
        });

    }

    /**
     * ??????????????????
     */
    public void insertAds (final Activity activity, final String adId, final InsertQcAdListener listener) {
        mActivity = activity;

        if (TextUtils.isEmpty(adId)) {
            if (listener != null) {
                listener.onAdError(ErrorUtil.QC, ErrorUtil.NOADID);
            }
            return;
        }

        QcHttpUtil.getAdid(adId, new QcHttpUtil.QcHttpOnListener<AdidBean>() {
            @Override
            public void OnQcCompletionListener (AdidBean response) {
                if (response != null) {
                    LogUtils.d("??????????????????=" + new Gson().toJson(response));
                    InsertManager.get().initWeight(activity, response, adId, listener);

                } else {
                    showErrorListener(activity, ErrorUtil.NOAD_PLACE, listener);
                }
            }

            @Override
            public void OnQcErrorListener (final String erro, final int code) {
                LogUtils.d("????????????????????????=" + erro);
                showErrorListener(activity, erro + code, listener);
            }
        });
    }

    /**
     * ???????????????
     */
    public void infoOneAds (final Activity activity, final String adId, final int maxNum, final InfoOneQcAdListener listener) {
        mActivity = activity;

        if (TextUtils.isEmpty(adId)) {
            if (listener != null) {
                listener.onAdError(ErrorUtil.QC, ErrorUtil.NOADID);
            }
            return;
        }

        QcHttpUtil.getAdid(adId, new QcHttpUtil.QcHttpOnListener<AdidBean>() {
            @Override
            public void OnQcCompletionListener (AdidBean response) {
                if (response != null) {
                    LogUtils.d("??????????????????=" + new Gson().toJson(response));
                    int num = maxNum;
                    if (maxNum > 3) {
                        num = 3;
                    } else if (maxNum < 1) {
                        num = 1;
                    }
                    InfoOneManager.get().initWeigth(mActivity, response, adId, num, listener);

                } else {
                    showErrorListener(activity, ErrorUtil.NOAD_PLACE, listener);
                }
            }

            @Override
            public void OnQcErrorListener (String erro, int code) {
                showErrorListener(activity, erro + code, listener);
            }
        });
    }

    /**
     * ???????????????
     */
    public void infoOneAds (final Activity activity, final String adId, final int maxNum,
            final float adWigth, final InfoOneQcAdListener listener) {
        mActivity = activity;

        if (TextUtils.isEmpty(adId)) {
            if (listener != null) {
                listener.onAdError(ErrorUtil.QC, ErrorUtil.NOADID);
            }
            return;
        }

        QcHttpUtil.getAdid(adId, new QcHttpUtil.QcHttpOnListener<AdidBean>() {
            @Override
            public void OnQcCompletionListener (AdidBean response) {
                if (response != null) {
                    LogUtils.d("??????????????????=" + new Gson().toJson(response));
                    int num = maxNum;
                    if (maxNum > 3) {
                        num = 3;
                    } else if (maxNum < 1) {
                        num = 1;
                    }
                    InfoOneManager.get().initWeigth(mActivity, response, adId, num, adWigth, listener);

                } else {
                    showErrorListener(activity, ErrorUtil.NOAD_PLACE, listener);
                }
            }

            @Override
            public void OnQcErrorListener (String erro, int code) {
                showErrorListener(activity, erro + code, listener);
            }
        });
    }

    /**
     * ???????????????
     */
    public void infoThreeAds (final Activity activity, final String adId, final int maxNum, final InfoMoreQcAdListener listener) {
        mActivity = activity;

        if (TextUtils.isEmpty(adId)) {
            if (listener != null) {
                listener.onAdError(ErrorUtil.QC, ErrorUtil.NOADID);
            }
            return;
        }

        QcHttpUtil.getAdid(adId, new QcHttpUtil.QcHttpOnListener<AdidBean>() {
            @Override
            public void OnQcCompletionListener (AdidBean response) {
                if (response != null) {
                    LogUtils.d("??????????????????=" + new Gson().toJson(response));
                    int num = maxNum;
                    if (maxNum > 3) {
                        num = 3;
                    } else if (maxNum < 1) {
                        num = 1;
                    }
                    InfoThreeManager.get().initWeigth(mActivity, response, adId, num, listener);

                } else {
                    showErrorListener(activity, ErrorUtil.NOAD_PLACE, listener);
                }
            }

            @Override
            public void OnQcErrorListener (String erro, int code) {
                showErrorListener(activity, erro + code, listener);
            }
        });
    }

    /**
     * ??????????????????
     */
    public void audioAds (final Activity activity, final String adId, final AudioQcAdListener listener) {
        mActivity = activity;

        if (TextUtils.isEmpty(adId)) {
            if (listener != null) {
                listener.onAdError(ErrorUtil.NOADID);
            }
            return;
        }

        QcHttpUtil.getAdid(adId, new QcHttpUtil.QcHttpOnListener<AdidBean>() {
            @Override
            public void OnQcCompletionListener (AdidBean response) {
                if (response != null) {
                    LogUtils.d("??????????????????=" + new Gson().toJson(response));
                    AudioAdManager.get().showQcAd(mActivity, response, adId, listener);

                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run () {
                            if (listener != null) {
                                listener.onAdError(ErrorUtil.NOAD_PLACE);
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
                        if (listener != null) {
                            listener.onAdError(erro + code);
                        }
                    }
                });
            }
        });
    }


    /**
     * ????????????
     */
    public void nativeVideoAd (final Activity activity, final String adId, final MediaController mediaController, final VideoView videoView,
            final ViewGroup adContainer, final Button buttonDetail, final TextView textCountDown,
            final TextView tvVideoTitle, final TextView tvVideoContent, final NativeVideoQcAdListener listener) {
        mActivity = activity;

        if (TextUtils.isEmpty(adId)) {
            if (listener != null) {
                listener.onAdError(ErrorUtil.NOADID);
            }
            return;
        }

        QcHttpUtil.getAdid(adId, new QcHttpUtil.QcHttpOnListener<AdidBean>() {
            @Override
            public void OnQcCompletionListener (AdidBean response) {
                if (response != null) {
                    LogUtils.d("????????????????????????=" + new Gson().toJson(response));
                    NativeVideoManager.get().showQcAd(mActivity, response, adId, mediaController, videoView,
                            adContainer, buttonDetail, textCountDown, tvVideoTitle, tvVideoContent, listener);

                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run () {
                            if (listener != null) {
                                listener.onAdError(ErrorUtil.NOAD_PLACE);
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
                        if (listener != null) {
                            listener.onAdError(erro + code);
                        }
                    }
                });
            }
        });
    }

    /**
     * ????????????????????????
     */
    public void nativeVideoDestroy () {
        NativeVideoManager.get().destroy();
    }

    public void inAdIdFetchInfo (final Activity activity, String adId, final AdInfoListener listener) {
        mActivity = activity;

        if (TextUtils.isEmpty(adId)) {
            if (listener != null) {
                listener.onAdError(ErrorUtil.QC, ErrorUtil.NOADID);
            }
            return;
        }

        QcHttpUtil.getAdid(adId, new QcHttpUtil.QcHttpOnListener<AdidBean>() {
            @Override
            public void OnQcCompletionListener (AdidBean response) {
                if (response != null) {
                    LogUtils.d("adid????????????=" + new Gson().toJson(response));
                    listener.onSuccess(response);
                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run () {
                            if (listener != null) {
                                listener.onAdError(ErrorUtil.QC, ErrorUtil.NOAD_PLACE);
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
                        if (listener != null) {
                            listener.onAdError(ErrorUtil.QC, erro + code);
                        }
                    }
                });
            }
        });
    }

    /**
     * ????????????
     */
    public void rewardVideoAds (final Activity activity, final String adId, final RewardVideoQcAdListener listener) {
        mActivity = activity;

        if (TextUtils.isEmpty(adId)) {
            if (listener != null) {
                listener.onAdError(ErrorUtil.QC, ErrorUtil.NOADID);
            }
            return;
        }

        QcHttpUtil.getAdid(adId, new QcHttpUtil.QcHttpOnListener<AdidBean>() {
            @Override
            public void OnQcCompletionListener (AdidBean response) {
                if (response != null) {
                    LogUtils.d("????????????????????????=" + new Gson().toJson(response));
                    RewardVideoManager.get().initWeight(mActivity, response, adId, listener);
                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run () {
                            if (listener != null) {
                                listener.onAdError(ErrorUtil.QC, ErrorUtil.NOAD_PLACE);
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
                        if (listener != null) {
                            listener.onAdError(ErrorUtil.QC, erro + code);
                        }
                    }
                });
            }
        });
    }

    /**
     * ?????????????????????
     *
     * @param activity
     * @param errorMsg
     */
    private void showErrorListener (Activity activity, final String errorMsg, final QCADListener listener) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                if (listener != null) {
                    listener.onAdError(ErrorUtil.QC, errorMsg);
                }
            }
        });
    }

    /**
     * ????????????
     */
    public void clear () {
        mActivity = null;
        mContext = null;
    }

}
