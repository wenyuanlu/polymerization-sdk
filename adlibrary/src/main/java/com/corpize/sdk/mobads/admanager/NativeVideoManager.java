package com.corpize.sdk.mobads.admanager;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.corpize.sdk.mobads.QcAd;
import com.corpize.sdk.mobads.bean.AdResponseBean;
import com.corpize.sdk.mobads.bean.AdidBean;
import com.corpize.sdk.mobads.bean.AdmJsonBean;
import com.corpize.sdk.mobads.common.ErrorUtil;
import com.corpize.sdk.mobads.listener.NativeVideoQcAdListener;
import com.corpize.sdk.mobads.utils.LogUtils;
import com.corpize.sdk.mobads.utils.QcHttpUtil;
import com.corpize.sdk.mobads.utils.StringToJsonUtils;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadInstaller;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadProgressCallBack;
import com.corpize.sdk.mobads.video.ThirdAppUtils;
import com.corpize.sdk.mobads.view.QcAdDetialActivity;

import java.util.List;

/**
 * author: yh
 * date: 2020-02-11 23:35
 * description: TODO:贴片广告
 */
public class NativeVideoManager {

    private static NativeVideoManager sQcVideoAd;

    private NativeVideoQcAdListener mListener;
    private CountDownTimer          mVideoDowntime;              //企创 视频右上角的倒计时
    private boolean                 mVideoCompletion = false;    //企创  视频是否完整的播放完成
    private boolean                 mHaveClick       = false;    //已经点击过了
    private boolean                 mHaveShow        = false;    //已经展示过了
    private boolean                 mHaveStartUp     = false;    //开始曝光上传过了
    private boolean                 mHaveMidpointUp  = false;    //中间曝光上传过了
    private boolean                 mHaveComplatetUp = false;    //播完曝光上传过了

    private float           mClickX;                     //企创 点击位置X
    private float           mClickY;                     //企创 点击位置Y
    private int             mWidth;
    private int             mHeight;
    private AdResponseBean  mResponse;
    private MediaController mMediaController;
    private VideoView       mVideoView;
    private ViewGroup       mAdContainer;
    private Button          mButtonDetail;
    private TextView        mTextCountDown;
    private TextView        mTvVideoTitle;
    private TextView        mTvVideoContent;
    private boolean         mHavePlay = false;//防止从其他页面切换回来继续播放问题


    /**
     * 单例模式
     */
    public static NativeVideoManager get () {
        if (sQcVideoAd == null) {
            sQcVideoAd = new NativeVideoManager();
        }
        return sQcVideoAd;
    }

    /**
     * 初始化数据
     */
    private void initData () {
        mHaveClick = false;
        mHaveStartUp = false;
        mHaveMidpointUp = false;
        mHaveComplatetUp = false;
        mHaveShow = false;
        mHavePlay = false;
    }

    /**
     * 清理
     */
    public void destroy () {
        mVideoDowntime = null;
        sQcVideoAd = null;
    }

    /**
     * 展示企创贴片视频的广告
     */
    public void showQcAd (final Activity activity, AdidBean adsSdk, String adId, final MediaController mediaController,
                          final VideoView videoView, final ViewGroup adContainer, final Button buttonDetail, final TextView textCountDown,
                          final TextView tvVideoTitle, final TextView tvVideoContent, NativeVideoQcAdListener listener) {
        initData();
        mListener = listener;
        mWidth = adContainer.getWidth();
        mHeight = adContainer.getHeight();

        QcHttpUtil.getAudioAd(activity, adsSdk, adId, new QcHttpUtil.QcHttpOnListener<AdResponseBean>() {

            @Override
            public void OnQcCompletionListener (final AdResponseBean response) {
                if (response != null) {
                    mResponse = response;
                    mMediaController = mediaController;
                    mVideoView = videoView;
                    mAdContainer = adContainer;
                    mButtonDetail = buttonDetail;
                    mTextCountDown = textCountDown;
                    mTextCountDown = textCountDown;
                    mTvVideoTitle = tvVideoTitle;
                    mTvVideoContent = tvVideoContent;

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run () {
                            if (mListener != null) {
                                mListener.onADReceive(sQcVideoAd);
                            }
                        }
                    });

                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run () {
                            if (mListener != null) {
                                mListener.onADManager(sQcVideoAd);
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
                            mListener.onADManager(sQcVideoAd);
                            mListener.onAdError(erro + code);
                        }
                    }
                });

            }
        });

    }

    /**
     * 展示广告
     */
    public void show (final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run () {
                qcShowVideo(activity, mResponse, mMediaController, mVideoView, mAdContainer,
                        mButtonDetail, mTextCountDown, mTvVideoTitle, mTvVideoContent);
            }
        });

    }

    /**
     * 企创 移动贴片视频 205
     */
    private void qcShowVideo (final Activity activity, AdResponseBean response, MediaController mController, final VideoView mVideoView,
                              ViewGroup mAdContainer, final Button mButtonDetail, final TextView mTextCountDown, TextView videoTitle, TextView videoContent) {

        String            value       = response.getAdm().trim();
        final AdmJsonBean admJsonBean = StringToJsonUtils.decodeFromJson(value);
        if (admJsonBean.getDuration() != 0) {
            mVideoDowntime = new CountDownTimer(admJsonBean.getDuration() * 1000, 1000) {
                @Override
                public void onTick (long millisUntilFinished) {
                    mTextCountDown.setEnabled(false);
                    mTextCountDown.setText(millisUntilFinished / 1000 + "");

                    int millisUntilFinished1 = (int) (millisUntilFinished / 1000);

                    try {
                        if (admJsonBean.getDuration() != 0 && millisUntilFinished1 != 0) {
                            int half = admJsonBean.getDuration() / 2;
                            if (millisUntilFinished1 == half) {
                                //视屏播放一半时监测地址
                                if (admJsonBean.getEvent() != null && admJsonBean.getEvent().getMidpoint() != null) {
                                    if (!mHaveMidpointUp) {
                                        mHaveMidpointUp = true;
                                        sendShowExposure(admJsonBean.getEvent().getMidpoint());
                                    }
                                }
                            }
                        }

                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }

                @Override
                public void onFinish () {
                    mTextCountDown.setEnabled(true);
                    mTextCountDown.setText(String.valueOf(admJsonBean.getDuration()));
                }
            };

        }

        if (TextUtils.isEmpty(admJsonBean.getTitle())) {
            videoTitle.setVisibility(View.GONE);
        } else {
            videoTitle.setVisibility(View.VISIBLE);
            videoTitle.setText(admJsonBean.getTitle());
        }

        if (TextUtils.isEmpty(admJsonBean.getDesc())) {
            videoContent.setVisibility(View.GONE);
        } else {
            videoContent.setVisibility(View.VISIBLE);
            videoContent.setText(admJsonBean.getDesc());
        }

        mAdContainer.setVisibility(View.VISIBLE);
        mVideoView.setVisibility(View.VISIBLE);

        try {
            mController.setMediaPlayer(mVideoView);
            mVideoView.setVideoURI(Uri.parse(admJsonBean.getVideourl()));
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared (MediaPlayer mp) {
                    if (mHavePlay) {
                        return;
                    }
                    mHavePlay = true;

                    LogUtils.d("开始播放");

                    //开始播放视频
                    mVideoView.start();

                    //外部开始回调
                    if (mListener != null && !mHaveShow) {
                        mHaveShow = true;
                        mListener.onADExposure();
                    }

                    //倒计时开始
                    if (mVideoDowntime != null) {
                        mVideoDowntime.start();
                    }

                    //视频开始播放监测地址
                    if (admJsonBean.getEvent() != null && admJsonBean.getEvent().getStart() != null) {
                        if (!mHaveStartUp) {
                            mHaveStartUp = true;
                            sendShowExposure(admJsonBean.getEvent().getStart());
                        }

                    }

                }
            });

            mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion (MediaPlayer mp) {
                    mVideoCompletion = true;

                    if (mListener != null) {
                        mListener.onAdCompletion();
                    }

                    //播放完成监测地址
                    if (admJsonBean.getEvent() != null && admJsonBean.getEvent().getComplete() != null) {
                        if (!mHaveComplatetUp) {
                            mHaveComplatetUp = true;
                            sendShowExposure(admJsonBean.getEvent().getComplete());
                        }

                    }

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        //获取点击的XY值
        getClickPosition(mButtonDetail);

        //点击时事件
        mButtonDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {

                if (mListener != null) {
                    mListener.onAdClicked();
                }

                //按钮的点击事件
                setClickListener(activity, admJsonBean);

                //按钮的点击监测
                sendClickExposure(admJsonBean.getClks());
                //Icon点击监测
                //getClickPosition(admJsonBean.getIcon().getClks());

                //广告控件激活监测地址
                if (admJsonBean.getEvent() != null && admJsonBean.getEvent().getAcceptInvitation() != null) {
                    sendShowExposure(admJsonBean.getEvent().getAcceptInvitation());
                }

            }
        });

        QcHttpUtil.sendAdExposure(admJsonBean.getImps());

        // Video Icon 曝光
        //QcHttpUtil.sendAdExposure(admJsonBean.getIcon().getImps());

        if (null != admJsonBean.getCompanion()) {
            if (null != admJsonBean.getCompanion().getResource()) {
                sendShowExposure(admJsonBean.getCompanion().getCreativeview());
            }
        }
    }

    /**
     * onTouch()事件(企创广告)
     * 注意返回值
     * true： view继续响应Touch操作；
     * false：view不再响应Touch操作，故此处若为false，只能显示起始位置，不能显示实时位置和结束位置
     */
    public void getClickPosition (View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View view, MotionEvent event) {
                switch (event.getAction()) {
                    //点击的开始位置
                    case MotionEvent.ACTION_DOWN:
                        //LogUtils.d("起始位置：(" + event.getX() + "," + event.getY());
                        mClickX = event.getX();
                        mClickY = event.getY();
                        break;

                    //触屏实时位置
                    case MotionEvent.ACTION_MOVE:
                        //tLogUtils.d("实时位置：(" + event.getX() + "," + event.getY());
                        break;

                    //离开屏幕的位置
                    case MotionEvent.ACTION_UP:
                        //tLogUtils.d("结束位置：(" + event.getX() + "," + event.getY());
                        break;

                    default:
                        break;
                }

                return false;
            }
        });

    }

    /**
     * 广告位点击曝光
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

    /**
     * 企创 adtype 为201、202、203的Action Listener
     */
    private void setClickListener (Activity activity, AdmJsonBean mAdmBean) {

        //根据不同的返回打开不同的页面
        String deeplink = mAdmBean.getDeeplink();
        if (!TextUtils.isEmpty(deeplink)
                && ThirdAppUtils.openLinkApp(activity, deeplink)) {
            //Action=7的时候,一般都是 deep link

        } else {
            /**
             * 1 - App Webview 打开链接
             * 2 - 系统浏览器 打开链接
             * 3 - 打开地图 无
             * 4 - 拨打电话 无
             * 5 - 播放视频 都播放
             * 6 - 下载APP 下载
             * 7 - deeplink 链接
             */
            int    action = mAdmBean.getAction();
            String ldp    = mAdmBean.getLdp();
            if (!TextUtils.isEmpty(ldp)) {
                if (action == 6) {//下载apk
                    //String downUrl = "http://dl.haohaiyoo.cn/app/haohaiyoo.apk";
                    LogUtils.d("下载地址=" + ldp);
                    new DownloadInstaller(activity, ldp, new DownloadProgressCallBack() {
                        @Override
                        public void downloadProgress (int progress) {
                            //TODO:下载一半的时候数据曝光
                            if (progress >= 50) {
                            }
                        }

                        @Override
                        public void downloadException (Exception e) {
                        }

                        @Override
                        public void onInstallStart () {
                            LogUtils.d("开始安装=");
                        }
                    }).start();

                } else if (action == 1) {
                    Intent intent = new Intent(activity, QcAdDetialActivity.class);
                    intent.putExtra("url", ldp);
                    activity.startActivity(intent);
                    LogUtils.d("跳转的地址1=" + ldp);

                } else if (action == 2) {
                    Uri    uri      = Uri.parse(ldp);
                    Intent intent11 = new Intent(Intent.ACTION_VIEW, uri);
                    activity.startActivity(intent11);
                    LogUtils.d("跳转的地址2=" + ldp);
                }
            }
        }

    }

    /**
     * 判断权限(所有sdk)
     */
    public void getRequestPermissions (Activity activity) {
        String         packageName = activity.getPackageName();
        PackageManager pm          = activity.getPackageManager();
        boolean        permission  = (PackageManager.PERMISSION_GRANTED == pm.checkPermission("android.permission.READ_PHONE_STATE", packageName));
        if (!permission) {
            //mVideoPasterListener.videoPasterQCADFail("请开通相关权限，否则无法正常使用本应用！");
        }

    }

}
