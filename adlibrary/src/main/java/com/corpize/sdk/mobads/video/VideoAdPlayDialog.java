package com.corpize.sdk.mobads.video;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.corpize.sdk.R;
import com.corpize.sdk.mobads.bean.AdmJsonBean;
import com.corpize.sdk.mobads.bean.IconBean;
import com.corpize.sdk.mobads.common.CommonUtils;
import com.corpize.sdk.mobads.utils.DeviceUtil;
import com.corpize.sdk.mobads.utils.LogUtils;
import com.corpize.sdk.mobads.utils.QcHttpUtil;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadInstaller;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadProgressCallBack;
import com.corpize.sdk.mobads.view.QcAdDetialActivity;
import com.bumptech.glide.Glide;

/**
 * author: yh
 * date: 2019-12-19 13:30
 * description: 激励视频的广告弹窗
 */
public class VideoAdPlayDialog implements View.OnClickListener {

    private static volatile VideoAdPlayDialog singleton = null;

    private Activity       mContext;
    private String         mUrl;
    private Dialog         mVideoAdDialog;
    private RelativeLayout mRlPlay;
    private MyTextureView  mTextureView;
    private ImageView      mIvPreview;
    private ImageView      mIvClose;
    private ProgressBar    mLoading;
    private TextView       mTvTimeDown;
    private TextView       mTvRefresh;

    private LinearLayout mBottonLl;
    private TextView     mBtDownApk;

    private LinearLayout mLastLl;
    private TextView     mLastBtDownApk;

    private CountDownTimer       mCountDownPreview;
    private CustomCountDownTimer mCountDownTime;
    private boolean              mIsHavePlayComplate   = false;  //视频倒计时 结束了, 视频播放结束了
    private boolean              mIsHaveTextureDestroy = false;  //视频控件是否销毁(自然销毁和切换后台的销毁)
    private int                  mVideoAllTime         = 30000;  //当前视频的总时间,毫秒
    private boolean     mHaveSendStart        = false;  //是否发送开始的监听
    private boolean     mHaveSendCenter       = false;  //是否发送中间的监听
    private boolean     mHaveSendEnd          = false;  //是否发送最后的监听
    private boolean     mHaveClicked          = false;  //是否点击的监听
    private int         mActivityCount        = 0;      //前台应用的数量
    private Window      mWindow;
    private int         mScreenWidth;
    private int         mScreenHeight;
    private AdmJsonBean mAdmBean;
    private ImageView   mIvIcon;
    private TextView    mTvTitle;
    private TextView    mTvContent;
    private ImageView   mIvIconLast;
    private TextView    mTvTitleLast;
    private TextView    mTvContentLast;

    private VideoAdPlayDialog () {
    }

    public static VideoAdPlayDialog getInstance () {
        if (singleton == null) {
            synchronized (VideoAdPlayDialog.class) {
                if (singleton == null) {
                    singleton = new VideoAdPlayDialog();
                }
            }
        }
        return singleton;
    }

    /**
     * 弹出视频的弹框
     */
    public void showVideoAdDialog (Activity context, AdmJsonBean bean, String url, int wigth, int higth) {
        mContext = context;
        mAdmBean = bean;
        mUrl = url;
        if (mVideoAdDialog != null && mVideoAdDialog.isShowing()) {
            mVideoAdDialog.dismiss();
        }

        //设置dialog
        mVideoAdDialog = new Dialog(context, R.style.QcDialog_Adver);
        mVideoAdDialog.setContentView(R.layout.qcad_dialog_video_paly);
        mWindow = mVideoAdDialog.getWindow();
        //设置去除dialog中的系统状态栏
        mWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mWindow.setGravity(Gravity.RIGHT);
        WindowManager.LayoutParams params = mWindow.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        mWindow.setAttributes(params);
        mWindow.setWindowAnimations(R.style.QcDialog_Right_Animation);

        //内部的视频的播放
        showVideoAd(context, wigth, higth);
        //开启前后台监听
        CommonUtils.get().registerActivityLifecycleCallbacks(mActivityLifecycleCallback);

        //显示dialog
        mVideoAdDialog.setCancelable(false);
        mVideoAdDialog.setCanceledOnTouchOutside(false);
        mVideoAdDialog.show();
    }

    /**
     * 内部播放器的逻辑
     */
    private void showVideoAd (Context context, int wigth, int higth) {
        mRlPlay = mVideoAdDialog.findViewById(R.id.dialog_ad_rl_paly);
        mTextureView = mVideoAdDialog.findViewById(R.id.dialog_ad_textureview);
        mIvPreview = mVideoAdDialog.findViewById(R.id.dialog_ad_iv_preview);
        mLoading = mVideoAdDialog.findViewById(R.id.dialog_ad_loading);
        mTvTimeDown = mVideoAdDialog.findViewById(R.id.dialog_ad_time_down);

        //按钮显示控件
        mIvClose = mVideoAdDialog.findViewById(R.id.dialog_ad_iv_close);
        mTvRefresh = mVideoAdDialog.findViewById(R.id.dialog_ad_refresh);
        mBtDownApk = mVideoAdDialog.findViewById(R.id.bt_ad_download);
        mLastBtDownApk = mVideoAdDialog.findViewById(R.id.bt_ad_download_last);

        //文字图片显示控件
        mIvIcon = mVideoAdDialog.findViewById(R.id.ad_icon);
        mTvTitle = mVideoAdDialog.findViewById(R.id.tv_ad_title);
        mTvContent = mVideoAdDialog.findViewById(R.id.tv_ad_content);
        mIvIconLast = mVideoAdDialog.findViewById(R.id.ad_icon_last);
        mTvTitleLast = mVideoAdDialog.findViewById(R.id.tv_ad_title_last);
        mTvContentLast = mVideoAdDialog.findViewById(R.id.tv_ad_content_last);

        //区域显示控件
        mLastLl = mVideoAdDialog.findViewById(R.id.dialog_ad_ll_last);
        mBottonLl = mVideoAdDialog.findViewById(R.id.dialog_ad_ll_bottom);

        mTvRefresh.setOnClickListener(this);
        mIvClose.setOnClickListener(this);
        mBtDownApk.setOnClickListener(this);
        mLastBtDownApk.setOnClickListener(this);


        // 设置控件大小及图片在控件中的位置

        mScreenWidth = DeviceUtil.getScreenWidth(context);
        mScreenHeight = DeviceUtil.getRealyScreenHeight(context);
        RelativeLayout.LayoutParams mVideoParams = (RelativeLayout.LayoutParams) mRlPlay.getLayoutParams();
        mVideoParams.width = mScreenWidth;
        mVideoParams.height = mScreenWidth * higth / wigth;
        mVideoParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        //mRlPlay.setLayoutParams(mVideoParams);

        //初始化界面内数据
        initData();

        //播放视频
        play(mUrl);

    }

    /**
     * 初始化界面显示
     */
    private void initData () {
        //设置广告信息的展示,广告
        if (mAdmBean != null) {//TODO:deepling (ldp和)
            String title    = mAdmBean.getTitle();
            String desc     = mAdmBean.getDesc();
            String ldp      = mAdmBean.getLdp();
            String firstimg = mAdmBean.getFirstimg();
            int    action   = mAdmBean.getAction();

            if (!TextUtils.isEmpty(title)) {
                mTvTitle.setText(title);
                mTvTitleLast.setText(title);
            } else {
                mTvTitle.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(desc)) {
                mTvContent.setText(desc);
                mTvContentLast.setText(desc);
            } else {
                mTvContent.setVisibility(View.INVISIBLE);
                mTvContentLast.setVisibility(View.INVISIBLE);
            }
            if (action == 6) {//下载
                mBtDownApk.setText("立即下载");
                mLastBtDownApk.setText("立即下载");
            } else if (action == 1 || action == 2) {
                mBtDownApk.setText("点击打开");
                mLastBtDownApk.setText("点击打开");
            } else if (action == 7) {
                mBtDownApk.setText("查看详情");
                mLastBtDownApk.setText("查看详情");
            }
            if (!TextUtils.isEmpty(firstimg)) {
                Glide.with(mContext).load(firstimg).into(mIvPreview);
            }

            IconBean icon = mAdmBean.getIcon();
            if (icon != null && !TextUtils.isEmpty(icon.getResource())) {
                Glide.with(mContext).load(icon.getResource()).into(mIvIcon);
                Glide.with(mContext).load(icon.getResource()).into(mIvIconLast);
            } else {
                mIvIcon.setVisibility(View.GONE);
                mIvIconLast.setVisibility(View.GONE);
            }
        }

    }

    /**
     * 播放视频
     */
    public void play (String url) {
        mHaveSendStart = false;
        mHaveSendCenter = false;
        mHaveSendEnd = false;
        mHaveClicked = false;
        mIsHavePlayComplate = false;
        mIsHaveTextureDestroy = false;
        mTextureView.setOnVideoListener(new MyTextureView.MyTextureViewOnListener() {
            @Override
            public void OnPreparedListener (MediaPlayer mp, int duration) {
                //视频准备完毕
                if (!mIsHavePlayComplate) {
                    mVideoAllTime = duration;
                    mTextureView.start();
                    showPreview(false);
                }

                //TODO:开启播放的曝光监听
                if (!mHaveSendStart) {
                    mHaveSendStart = true;
                    if (mAdmBean != null && mAdmBean.getEvent() != null) {
                        QcHttpUtil.sendAdExposure(mAdmBean.getEvent().getStart());
                    }
                }

            }

            @Override
            public void OnVideoPreparedListener (MediaPlayer mp) {

            }

            @Override
            public void OnCompletionListener (MediaPlayer mp) {//视频播放完毕
                LogUtils.d("视频播放完毕");
                mIsHavePlayComplate = true;

                mTextureView.stop();    //视频播放停止
                if (mWindow != null) {  //重新设置动画
                    mWindow.setWindowAnimations(R.style.QcDialog_Right_Animation);
                }

                //界面显示
                mIvPreview.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.GONE);
                mTvTimeDown.setVisibility(View.GONE);
                mIvClose.setVisibility(View.VISIBLE);
                //结束倒计时
                closeCountDownTime();
                //清除 前后台监听
                CommonUtils.get().unregisterActivityLifecycleCallbacks(mActivityLifecycleCallback);

                //对数据进行处理
                mBottonLl.setVisibility(View.GONE);
                mLastLl.setVisibility(View.VISIBLE);
                //开启控件的弹动动画
                startViewAnimation(mLastBtDownApk);

                //TODO:发送视频播放完成的曝光
                if (!mHaveSendEnd) {
                    mHaveSendEnd = true;
                    if (mAdmBean != null && mAdmBean.getEvent() != null) {
                        QcHttpUtil.sendAdExposure(mAdmBean.getEvent().getComplete());
                        //QcHttpUtil.sendAdExposure(mAdmBean.getEvent().getComplete());
                    }
                }
            }

            @Override
            public void OnErrorListener (MediaPlayer mp, int what, int extra) {
                LogUtils.d("播放错误" + what + extra);
                mTvRefresh.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.GONE);

            }

            @Override
            public void onInfoListener (MediaPlayer mp, int what, int extra) {
                if (what == 804 && extra == -1004) {//播放过程中网络中断
                    mTvRefresh.setVisibility(View.VISIBLE);
                    mLoading.setVisibility(View.GONE);
                }
            }

            @Override
            public void OnTextureDestroyedListener () {
                if (!mIsHavePlayComplate) {//视频未播放完成
                    showPreview(true);
                    //每次界面切换时,要隐藏结束倒计时
                    if (mCountDownTime != null) {
                        mCountDownTime.cancel();
                    }
                    mIsHaveTextureDestroy = true;
                }
            }

            @Override
            public void OnBitmapListener (Bitmap bitmap) {//返回的第一帧的图片
                if (bitmap != null) {
                    //mIvPreview.setImageBitmap(bitmap);
                }
            }
        });

        mTextureView.setVideoPath(url);

    }

    /**
     * 开启控件的弹动动画
     */
    private void startViewAnimation (final TextView lastBtDownApk) {
        final Animation shake    = AnimationUtils.loadAnimation(mContext, R.anim.qcad_shake_ad);//加载动画资源文件
        final Animation shakeEnd = AnimationUtils.loadAnimation(mContext, R.anim.qcad_shake_ad_end);//加载动画资源文件
        lastBtDownApk.startAnimation(shake); //给组件播放动画效果
        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart (Animation animation) {
            }

            @Override
            public void onAnimationEnd (Animation animation) {
                lastBtDownApk.startAnimation(shakeEnd); //给组件播放动画效果
            }

            @Override
            public void onAnimationRepeat (Animation animation) {
            }
        });

        shakeEnd.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart (Animation animation) {
            }

            @Override
            public void onAnimationEnd (Animation animation) {
                try {
                    Thread.sleep(800);
                    startViewAnimation(lastBtDownApk);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAnimationRepeat (Animation animation) {
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
     * 设置预览图片的显示
     */
    public void showPreview (Boolean isShow) {
        if (isShow) {
            mIvPreview.setVisibility(View.VISIBLE);
            mLoading.setVisibility(View.VISIBLE);
        } else {
            startPreviewTimeDown(400);//毫秒
            //mIvPreview.setVisibility(GONE);
        }
    }

    /**
     * 预览图片消失的的倒计时
     */
    private void startPreviewTimeDown (int time) {
        if (mCountDownPreview != null) {
            mCountDownPreview.cancel();
        }
        mCountDownPreview = new CountDownTimer(time, 800) {
            @Override
            public void onTick (long millisUntilFinished) {
            }

            @Override
            public void onFinish () {
                mIvPreview.setVisibility(View.GONE);
                mLoading.setVisibility(View.GONE);
                if (!mIsHavePlayComplate) {
                    mTvTimeDown.setVisibility(View.VISIBLE);
                    startViewClickTimeDown();
                } else {
                    //防止播放错误时刷新,倒计时又重新开始
                    mIvClose.setVisibility(View.VISIBLE);
                }
            }
        };

        mCountDownPreview.start();
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
        //LogUtils.d("获取当前的位置=" + currentPosition + "倒计时时间=" + distanceTime);
        mCountDownTime = new CustomCountDownTimer(distanceTime, 1000) {
            @Override
            public void onTick (long millisUntilFinished) {
                if (mTextureView != null && mTextureView.getCurrentPosition() * 2 > mVideoAllTime) {
                    //TODO:中间的监听
                    if (!mHaveSendCenter) {
                        LogUtils.d("超过一半=" + mTextureView.getCurrentPosition());
                        mHaveSendCenter = true;
                        if (mAdmBean != null && mAdmBean.getEvent() != null) {
                            //视屏播放一般时监测地址
                            QcHttpUtil.sendAdExposure(mAdmBean.getEvent().getMidpoint());
                            //QcHttpUtil.sendAdExposure(mAdmBean.getEvent().getMidpoint());
                        }
                    }
                }
                long time = 0;
                if (millisUntilFinished > 0) {
                    time = millisUntilFinished / 1000;
                    if (millisUntilFinished % 1000 > 0) {
                        time = time + 1;
                    }
                }
                //LogUtils.d("时间=" + time + " 原时间=" + millisUntilFinished);
                mTvTimeDown.setText(String.valueOf(time));
                mTvTimeDown.setVisibility(View.VISIBLE);

            }

            @Override
            public void onFinish () {
                mTvTimeDown.setVisibility(View.GONE);
                mIvClose.setVisibility(View.VISIBLE);
                mIsHavePlayComplate = true;
            }
        };

        mCountDownTime.start();
    }

    @Override
    public void onClick (View view) {
        int id = view.getId();
        if (id == R.id.dialog_ad_refresh) {         //刷新重新播放视频
            mTextureView.resumeStart();
            mTvRefresh.setVisibility(View.GONE);
            mLoading.setVisibility(View.VISIBLE);

        } else if (id == R.id.dialog_ad_iv_close) { //关闭dialog的按钮
            mTextureView.release();
            if (mCountDownTime != null) {
                mCountDownTime.onFinish();
                mCountDownTime.cancel();
                mCountDownTime = null;
            }
            if (mCountDownPreview != null) {
                mCountDownPreview.cancel();
                mCountDownPreview = null;
            }

            CommonUtils.get().unregisterActivityLifecycleCallbacks(mActivityLifecycleCallback);

            //关闭
            if (mVideoAdDialog != null && mVideoAdDialog.isShowing()) {
                mVideoAdDialog.dismiss();
                mVideoAdDialog = null;
            }

            singleton = null;

        } else if (id == R.id.bt_ad_download || id == R.id.bt_ad_download_last) {   //todo:打开链接,下载apk
            if (mAdmBean != null) {
                //点击监听
                if (!mHaveClicked) {
                    mHaveClicked = true;
                    QcHttpUtil.sendAdExposure(mAdmBean.getClks());
                    //QcHttpUtil.sendAdExposure(mAdmBean.getClks());
                }

                //根据不同的返回打开不同的页面
                String deeplink = mAdmBean.getDeeplink();
                if (!TextUtils.isEmpty(deeplink)
                        && ThirdAppUtils.openLinkApp(mContext, deeplink)) {
                    //Action=7的时候,一般都是 deep link
                    //ThirdAppUtils打开deeplink
                    LogUtils.d("打开的deeplink地址=" + deeplink);

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
                            new DownloadInstaller(mContext, ldp, new DownloadProgressCallBack() {
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
                            Intent intent = new Intent(mContext, QcAdDetialActivity.class);
                            intent.putExtra("url", ldp);
                            //intent.putExtra("title", mAdmBean.getTitle());
                            if (!mIsHavePlayComplate) {//视频未播放完成,这是后点击的是下方的按钮
                                intent.putExtra("type", 1);
                            }
                            mContext.startActivity(intent);
                            LogUtils.d("跳转的地址1=" + ldp);

                            //视频未播放完成,这是后点击的是下方的按钮
                            if (!mIsHavePlayComplate) {
                                //因为跳转页面也会进入是否是前后台的监听,所以暂时清除监听
                                if (!mIsHaveTextureDestroy) {
                                    //应用切换后台,视频未停止,暂停播放
                                    mTextureView.pause();
                                    if (mCountDownTime != null) {
                                        mCountDownTime.cancel();
                                        mCountDownTime = null;
                                    }
                                    //清除 前后台监听
                                    CommonUtils.get().unregisterActivityLifecycleCallbacks(mActivityLifecycleCallback);
                                }
                            }

                        } else if (action == 2) {
                            Uri    uri      = Uri.parse(ldp);
                            Intent intent11 = new Intent(Intent.ACTION_VIEW, uri);
                            mContext.startActivity(intent11);
                            LogUtils.d("跳转的地址2=" + ldp);
                        }
                    }
                }


            }
        }
    }

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
            //首次不进入
            LogUtils.i("应用切换到了前台");
            if (!mIsHaveTextureDestroy) {
                //应用切换后台,视频未停止
                mTextureView.start();
                startViewClickTimeDown();
            }

            //mActivityCount++;
            //LogUtils.d("onActivityStarted=" + mActivityCount);
        }

        @Override
        public void onActivityResumed (Activity activity) {
        }

        @Override
        public void onActivityPaused (Activity activity) {
            //LogUtils.d("onActivityPaused=" + mActivityCount);
        }

        @Override
        public void onActivityStopped (Activity activity) {
            LogUtils.i("应用切换到了后台");
            if (mWindow != null) {
                mWindow.setWindowAnimations(0);
            }
            if (!mIsHaveTextureDestroy) {
                //应用切换后台,视频未停止,暂停播放
                mTextureView.pause();
                if (mCountDownTime != null) {
                    mCountDownTime.cancel();
                    mCountDownTime = null;
                }
            }
            //mActivityCount--;
            //LogUtils.d("onActivityStopped=" + mActivityCount);
        }

        @Override
        public void onActivitySaveInstanceState (Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed (Activity activity) {
            //LogUtils.d("onActivityDestroyed=" + mActivityCount);
        }

    };

    /**
     * 重启应用
     * 给跳转使用
     */
    public void restartVieo () {
        if (!mIsHaveTextureDestroy) {
            //应用切换后台,视频未停止
            mTextureView.start();
            startViewClickTimeDown();
        }
    }

    /**
     * 开启前后台监听
     * 给跳转使用
     */
    public void activityLifecycle () {
        //开启前后台监听
        CommonUtils.get().registerActivityLifecycleCallbacks(mActivityLifecycleCallback);
    }
}
