package com.corpize.sdk.mobads.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.corpize.sdk.R;
import com.corpize.sdk.mobads.bean.AdResponseBean;
import com.corpize.sdk.mobads.bean.AssetsLinkEventtrackersBean;
import com.corpize.sdk.mobads.common.CommonUtils;
import com.corpize.sdk.mobads.common.ErrorUtil;
import com.corpize.sdk.mobads.listener.SplashQcAdListener;
import com.corpize.sdk.mobads.utils.DeviceUtil;
import com.corpize.sdk.mobads.utils.LogUtils;
import com.corpize.sdk.mobads.utils.QcHttpUtil;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadInstaller;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadProgressCallBack;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadVideo;
import com.corpize.sdk.mobads.utils.downloadinstaller.FileUtils;
import com.corpize.sdk.mobads.video.CustomCountDownTimer;
import com.corpize.sdk.mobads.video.MyTextureView;
import com.corpize.sdk.mobads.video.ThirdAppUtils;

import java.util.List;

/**
 * author ：yh
 * date : 2020-05-27 21:14
 * description : 企创开屏广告,含有视频
 */
public class QcSplashAdView extends FrameLayout {

    private Context            mContext;
    private Activity           mActivity;
    private AdResponseBean     mResponse;
    private SplashQcAdListener mListener;
    private TextView           mSkipView;//倒计时的显示按钮

    private View                 mView;
    private MyTextureView        mTextureView;
    private ImageView            mIvPreview;
    private ProgressBar          mLoading;
    private TextView             mTvRefresh;
    private RelativeLayout       mAdRlPaly;
    private CustomCountDownTimer mCountDownTime;

    private float   mClickX;
    private float   mClickY;
    private boolean mHaveSendShow     = false;//是否发送展示曝光请求
    private boolean mHaveSendClick    = false;//是否发送点击曝光请求
    private boolean mHaveSendDeep     = false;//是否发送deeplink曝光请求
    private boolean mHaveDownStart    = false;//是否发送开始下载曝光请求
    private boolean mHaveDownComplete = false;//是否发送完成下载曝光请求
    private boolean mHaveDownInstall  = false;//是否发送开始安装曝光请求
    private boolean isClickAd         = false; //企创广告是否点击
    private int     mWidth;
    private int     mHeight;
    private boolean mIsWebview;

    private boolean mIsHavePlayComplate   = false;  //视频倒计时 结束了, 视频播放结束了
    private boolean mIsHaveTextureDestroy = false;  //视频控件是否销毁(自然销毁和切换后台的销毁)
    private int     mVideoAllTime         = 5000;  //当前视频的总时间,毫秒
    private boolean isFirstPlayVoide      = true;   //是否是第一次播放视频
    private String  mPlayUrl;


    public QcSplashAdView (@NonNull Context context) {
        this(context, null);
    }

    public QcSplashAdView (@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public QcSplashAdView (@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    public QcSplashAdView (@NonNull Activity activity, @Nullable AttributeSet attrs,
                           AdResponseBean bean, TextView skipView, SplashQcAdListener listener) {
        super(activity, attrs);
        this.mActivity = activity;
        this.mResponse = bean;
        this.mSkipView = skipView;
        this.mListener = listener;
        intView(activity);//初始化页面
    }

    @Override
    protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //LogUtils.e("进入banner的onLayout");
        mWidth = getWidth();
        mHeight = getHeight();

        /*//加载的时候 再曝光(本地数据的曝光)
        if (!mHaveSendShow && mResponse != null && mResponse.getExt() != null
                && mResponse.getExt().getImptrackers() != null) {
            mHaveSendShow = true;
            sendShowExposure(mResponse.getExt().getImptrackers());// Native 广告位曝光
            if (mListener != null) {
                mListener.onADExposure();
            }
        }*/

        //Webview的曝光
        if (!mHaveSendShow && mIsWebview) {
            mHaveSendShow = true;
            if (mListener != null) {
                mListener.onADExposure(ErrorUtil.QC);
            }
        }

    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //LogUtils.e("进入banner的onMeasure");
    }

    /**
     * 初始化数据
     */
    private void intView (Activity activity) {
        mSkipView.setVisibility(View.GONE);
        if (mResponse == null) {
            if (mListener != null) {
                mListener.onAdError(ErrorUtil.QC, "加载错误");
            }
            return;
        }
        mHaveSendShow = false;
        final String webViewAdm = mResponse.getAdm();
        //设置默认宽高
        mWidth = DeviceUtil.getScreenWidth(mActivity);
        mHeight = DeviceUtil.getScreenHeight(mActivity);


        //创建内部的广告控件
        if (!TextUtils.isEmpty(webViewAdm)) {//webview的形式加载
            mIsWebview = true;
            WebView webView = WebViewUtils.initWebview(mActivity, mListener);
            if (webView != null) {
                addView(webView);//当前页面加载webview
                WebViewUtils.addData(webView, webViewAdm, 202, mWidth, mHeight);//加载数据
            }

        } else {
            mIsWebview = false;
            mView = LayoutInflater.from(activity).inflate(R.layout.qcad_splash_video_paly, null);//左图右文
            mAdRlPaly = mView.findViewById(R.id.splash_ad_rl_paly);
            mTextureView = mView.findViewById(R.id.splash_ad_textureview);
            mIvPreview = mView.findViewById(R.id.splash_ad_iv_preview);
            mLoading = mView.findViewById(R.id.splash_ad_loading);
            mTvRefresh = mView.findViewById(R.id.splash_ad_refresh);
            addView(mView);//当前页面加载ImageView

            play(mResponse.getExt().getIurl(), mResponse.getExt().getDuration());
            //play("http://ubmcvideo.baidustatic.com/media/v1/0f000ntDpAfJrWWXmTK7A0.mp4");

            //获取点击的坐标
            getClickXYPosition(mAdRlPaly);
            mAdRlPaly.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick (View v) {
                    //isClickAd = true;
                    if (mListener != null) {
                        mListener.onAdClicked(ErrorUtil.QC);
                    }
                    //点击监听
                    sendClickExposure(mResponse.getExt().getClicktrackers());
                    setClickListener(mActivity, mResponse);

                }
            });
        }

        /*//TODO:设置广告的标识
        TextView tvAd = new TextView(mActivity);
        tvAd.setText("广告");
        tvAd.setTextColor(Color.parseColor("#7EF0F0F0"));
        tvAd.setTextSize(10);
        tvAd.setPadding(15, 1, 15, 2);
        addView(tvAd);//当前页面加载tvAd
        tvAd.setBackgroundColor(Color.parseColor("#4A666666"));
        LayoutParams adParams = (LayoutParams) tvAd.getLayoutParams();
        adParams.width = LayoutParams.WRAP_CONTENT;
        adParams.height = LayoutParams.WRAP_CONTENT;
        adParams.gravity = Gravity.BOTTOM | Gravity.END;
        //adParams.bottomMargin = 0;
        //adParams.rightMargin = 60;
        tvAd.setLayoutParams(adParams);*/

        //渲染填充
        //render ();
    }

    /**
     * 播放视频
     */
    public void play (String url, final int adDuration) {
        if (TextUtils.isEmpty(url)) {
            if (mListener != null) {
                mListener.onAdError(ErrorUtil.QC, ErrorUtil.NOAD_VIDEO);
            }
            return;
        }
        mPlayUrl = url;
        mIsHavePlayComplate = false;
        mIsHaveTextureDestroy = false;
        mTextureView.setOnVideoListener(new MyTextureView.MyTextureViewOnListener() {
            @Override
            public void OnPreparedListener (MediaPlayer mp, int duration) {
                //视频准备完毕
                if (!mIsHavePlayComplate) {
                    if (adDuration > 0) {
                        if (adDuration * 1000 > duration) {
                            mVideoAllTime = duration;
                        } else {
                            mVideoAllTime = adDuration * 1000;
                        }
                    } else {
                        mVideoAllTime = duration;
                    }
                    mTextureView.start();
                    //开启前后台监听
                    CommonUtils.get().registerActivityLifecycleCallbacks(mActivityLifecycleCallback);
                    LogUtils.e("开始计算倒计时");
                }


                /*if (!mHaveSendStart) {
                    mHaveSendStart = true;
                    if (mAdmBean != null && mAdmBean.getEvent() != null) {
                        QcHttpUtil.sendAdExposure(mAdmBean.getEvent().getStart());
                    }
                }*/

            }

            @Override
            public void OnVideoPreparedListener (MediaPlayer mp) {
                if (isFirstPlayVoide) {//TODO:视频缓存好了可以播放的页面
                    //isFirstPlayVoide = false;
                    LogUtils.d("视频缓存好了可以播放");
                    startshowAndCountdown();
                    //showPreview(false);//第一次倒计时,在视频缓存结束后

                    //开启播放的曝光监听
                    if (!mHaveSendShow && mResponse != null && mResponse.getExt() != null
                            && mResponse.getExt().getImptrackers() != null) {
                        mHaveSendShow = true;
                        sendShowExposure(mResponse.getExt().getImptrackers());// Native 广告位曝光
                        if (mListener != null) {
                            mListener.onADExposure(ErrorUtil.QC);
                        }
                    }
                }
            }

            @Override
            public void onInfoListener (MediaPlayer mp, int what, int extra) {
                if (what == 804 && extra == -1004) {//播放过程中网络中断
                    //mTvRefresh.setVisibility(View.VISIBLE);
                    mLoading.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void OnCompletionListener (MediaPlayer mp) {//视频播放完毕
                LogUtils.d("视频播放完毕");
                mIsHavePlayComplate = true;

                mTextureView.stop();    //视频播放停止

                mLoading.setVisibility(View.GONE);
                //结束倒计时
                //mSkipView.setVisibility(View.GONE);
                //closeCountDownTime();

                /*//发送视频播放完成的曝光
                if (!mHaveSendEnd) {
                    mHaveSendEnd = true;
                    if (mAdmBean != null && mAdmBean.getEvent() != null) {
                        QcHttpUtil.sendAdExposure(mAdmBean.getEvent().getComplete());
                    }
                }*/

                /*if (mListener != null) {
                    mListener.onAdCompletion();
                }*/
            }

            @Override
            public void OnErrorListener (MediaPlayer mp, int what, int extra) {
                LogUtils.d("播放错误" + what + extra);
                //mTvRefresh.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.VISIBLE);
                if (mListener != null) {
                    mListener.onAdError(ErrorUtil.GDT, ErrorUtil.ERROR_VIDEO);
                }
            }

            @Override
            public void OnTextureDestroyedListener () {
                LogUtils.d("视频控件销毁了");
                if (!mIsHavePlayComplate) {//视频未播放完成
                    //showPreview(true);
                    //每次界面切换时,要隐藏结束倒计时
                    if (mCountDownTime != null) {
                        mCountDownTime.cancel();
                    }
                    mIsHaveTextureDestroy = true;
                }
            }

            @Override
            public void OnBitmapListener (Bitmap bitmap) {//返回的第一帧的图片
                LogUtils.d("显示图片2");
                if (bitmap != null) {
                    LogUtils.d("显示图片3");
                    mIvPreview.setVisibility(View.VISIBLE);
                    mLoading.setVisibility(View.GONE);
                    mIvPreview.setImageBitmap(bitmap);
                }
            }
        });

        //TODO:
        String changeUrl = FileUtils.urlFileExist(mActivity, url);
        LogUtils.e("最终的url的地址=" + changeUrl);
        mTextureView.setVideoPath(changeUrl);

    }

    private void startshowAndCountdown () {
        mIvPreview.setVisibility(View.GONE);
        mLoading.setVisibility(View.GONE);
        if (!mIsHavePlayComplate) {
            mSkipView.setVisibility(View.VISIBLE);
            startViewClickTimeDown();
        } else {
            //防止播放错误时刷新,倒计时又重新开始
            //mIvClose.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 右上方时间的倒计时
     */
    private void startViewClickTimeDown () {
        if (mCountDownTime != null) {
            mCountDownTime.cancel();
            mCountDownTime = null;
        }
        int currentPosition = mTextureView.getCurrentPosition();
        int distanceTime    = mVideoAllTime - currentPosition;
        LogUtils.d("获取当前的位置=" + currentPosition + "倒计时时间=" + distanceTime);
        mCountDownTime = new CustomCountDownTimer(distanceTime, 1000) {
            @Override
            public void onTick (long millisUntilFinished) {
                /*if (mTextureView != null && mTextureView.getCurrentPosition() * 2 > mVideoAllTime) {
                    //TODO:中间的监听
                    if (!mHaveSendCenter) {
                        LogUtils.d("超过一半=" + mTextureView.getCurrentPosition());
                        mHaveSendCenter = true;
                        if (mAdmBean != null && mAdmBean.getEvent() != null) {
                            //视屏播放一般时监测地址
                            QcHttpUtil.sendAdExposure(mAdmBean.getEvent().getMidpoint());
                        }
                    }
                }*/
                long time = 0;
                if (millisUntilFinished > 0) {
                    time = millisUntilFinished / 1000;
                    if (millisUntilFinished % 1000 > 0) {
                        time = time + 1;
                    }
                }
                LogUtils.d("时间=" + time + " 原时间=" + millisUntilFinished);
                mSkipView.setText("跳过:" + time);
                mSkipView.setVisibility(View.VISIBLE);

            }

            @Override
            public void onFinish () {
                mSkipView.setVisibility(View.GONE);
                mIsHavePlayComplate = true;
                if (!isClickAd) {
                    if (mListener != null) {
                        mListener.onADDismissed();
                        new DownloadVideo(mActivity, mPlayUrl).start();
                        //清除 前后台监听
                        CommonUtils.get().unregisterActivityLifecycleCallbacks(mActivityLifecycleCallback);
                    }
                }
            }
        };

        mCountDownTime.start();

        mSkipView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                if (mListener != null) {
                    mListener.onADDismissed();
                    new DownloadVideo(mActivity, mPlayUrl).start();
                    //清除 前后台监听
                    CommonUtils.get().unregisterActivityLifecycleCallbacks(mActivityLifecycleCallback);
                }

                if (mCountDownTime != null) {
                    mCountDownTime.cancel();
                    mCountDownTime = null;
                }

                if (mTextureView != null) {
                    mTextureView.release();
                }
            }
        });
    }

    /**
     * 结束倒计时
     */
    private void closeCountDownTime () {
        if (mCountDownTime != null) {
            mCountDownTime.cancel();
            mCountDownTime = null;
        }
    }

    /**
     * 获取点击时候的坐标值
     * 注意返回值
     * true：view继续响应Touch操作
     * false：view不再响应Touch操作，故此处若为false，只能显示起始位置，不能显示实时位置和结束位置
     */
    @SuppressLint ("ClickableViewAccessibility")
    public void getClickXYPosition (View view) {
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch (View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN://点击的开始位置
                        //LogUtils.e("起始位置：(" + event.getX() + "," + event.getY());
                        mClickX = event.getX();
                        mClickY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE://触屏实时位置
                        break;
                    case MotionEvent.ACTION_UP://离开屏幕的位置
                        break;
                    default:
                        break;
                }

                return false;
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

    /**
     * 广告位点击请求(企创广告)
     */
    public void sendClickExposure (final List<String> list) {
        if (!mHaveSendClick) {
            mHaveSendClick = true;
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
    private void setClickListener (Activity activity, AdResponseBean responseBean) {

        if (0 == responseBean.getExt().getAction()) {   //0 - 未确认

        } else if (1 == responseBean.getExt().getAction()) {   // 1 - App webview 打开链接
            if (null != responseBean.getExt().getClickurl()) {
                Intent intent = new Intent(activity, QcAdDetialActivity.class);
                intent.putExtra("url", responseBean.getExt().getClickurl());
                activity.startActivity(intent);
            }

        } else if (2 == responseBean.getExt().getAction()) {      //2 - 系统浏览器打开链接
            if (null != responseBean.getExt().getClickurl()) {
                Uri    uri      = Uri.parse(responseBean.getExt().getClickurl());
                Intent intent11 = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(intent11);
            }

        } else if (3 == responseBean.getExt().getAction()) {    // 3 - 打开地图

        } else if (4 == responseBean.getExt().getAction()) {    //4 - 拨打电话

        } else if (5 == responseBean.getExt().getAction()) {    //5 - 播放视频

        } else if (6 == responseBean.getExt().getAction()) {   // 6- 下载APP
            if (null != responseBean.getExt().getDfn()) {
                final AssetsLinkEventtrackersBean eventtrackers = responseBean.getExt().getEventtrackers();
                new DownloadInstaller(activity, responseBean.getExt().getDfn(), new DownloadProgressCallBack() {
                    @Override
                    public void downloadProgress (int progress) {
                        if (!mHaveDownStart&&eventtrackers!=null) {
                            mHaveDownStart = true;
                            sendShowExposure(eventtrackers.getStartdownload());
                        }
                        if (progress == 100) {
                            if (!mHaveDownComplete&&eventtrackers!=null) {
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
                        if (!mHaveDownInstall&&eventtrackers!=null) {
                            mHaveDownInstall = true;
                            sendShowExposure(eventtrackers.getStartinstall());
                        }

                        if (!mHaveDownStart&&eventtrackers!=null) {
                            mHaveDownStart = true;
                            sendShowExposure(eventtrackers.getStartdownload());
                        }

                        if (!mHaveDownComplete&&eventtrackers!=null) {
                            mHaveDownComplete = true;
                            sendShowExposure(eventtrackers.getCompletedownload());
                        }
                    }
                }).start();

            }

        } else if (7 == responseBean.getExt().getAction()) {   // 7 - deeplink 链接
            String deeplink = responseBean.getExt().getFallback();
            if (null != deeplink && ThirdAppUtils.openLinkApp(activity, deeplink)) {
                //发送deepLink的曝光
                if (!mHaveSendDeep) {
                    mHaveSendDeep = true;
                    sendShowExposure(responseBean.getExt().getFallbacktrackers());
                }
            } else {
                if (null != responseBean.getExt().getClickurl()) {
                    Uri    uri      = Uri.parse(responseBean.getExt().getClickurl());
                    Intent intent11 = new Intent(Intent.ACTION_VIEW, uri);
                    activity.startActivity(intent11);
                }
            }

        } else {
            if (null != responseBean.getExt().getClickurl()) {
                Uri    uri      = Uri.parse(responseBean.getExt().getClickurl());
                Intent intent11 = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(intent11);
            }

        }
    }

    private int mActivityCount = 0;

    /**
     * 监听前后台
     * 注意,跳转到落地页后,也会走这个回调监听,所以需要对这一块进行判断
     */
    private Application.ActivityLifecycleCallbacks mActivityLifecycleCallback = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated (Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted (Activity activity) {
            if (activity.getClass() != QcAdDetialActivity.class) {
                //首次不进入
                //mActivityCount++;
                //LogUtils.d("onActivityStarted=" + mActivityCount);
            }

        }

        @Override
        public void onActivityResumed (Activity activity) {
            if (activity.getClass() != QcAdDetialActivity.class) {
                //LogUtils.i("应用切换到了前台");
                if (!mIsHaveTextureDestroy) {
                    //应用切换后台,视频未停止
                    if (mTextureView != null) {
                        mTextureView.start();
                    }
                    startViewClickTimeDown();
                }
                //LogUtils.d("onActivityResumed=" + mActivityCount);
            }

        }

        @Override
        public void onActivityPaused (Activity activity) {
            if (activity.getClass() != QcAdDetialActivity.class) {
                //LogUtils.i("应用切换到了后台");
                if (!mIsHaveTextureDestroy) {
                    //应用切换后台,视频未停止,暂停播放
                    if (mTextureView != null) {
                        mTextureView.pause();
                    }
                    if (mCountDownTime != null) {
                        mCountDownTime.cancel();
                        mCountDownTime = null;
                    }
                }
                //LogUtils.d("onActivityPaused=" + mActivityCount);
            }
        }

        @Override
        public void onActivityStopped (Activity activity) {
            if (activity.getClass() != QcAdDetialActivity.class) {
                //mActivityCount--;
                //LogUtils.d("onActivityStopped=" + mActivityCount);
            }
        }

        @Override
        public void onActivitySaveInstanceState (Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed (Activity activity) {
            //LogUtils.d("onActivityDestroyed=" + mActivityCount);
        }

    };
}
