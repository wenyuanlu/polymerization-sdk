package com.corpize.sdk.mobads.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
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

import com.corpize.sdk.mobads.QcAd;
import com.corpize.sdk.R;
import com.corpize.sdk.mobads.admanager.RewardVideoManager;
import com.corpize.sdk.mobads.bean.AdmJsonBean;
import com.corpize.sdk.mobads.bean.IconBean;
import com.corpize.sdk.mobads.common.ErrorUtil;
import com.corpize.sdk.mobads.listener.RewardVideoQcAdListener;
import com.corpize.sdk.mobads.utils.DeviceUtil;
import com.corpize.sdk.mobads.utils.LogUtils;
import com.corpize.sdk.mobads.utils.QcHttpUtil;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadInstaller;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadProgressCallBack;
import com.corpize.sdk.mobads.video.CustomCountDownTimer;
import com.corpize.sdk.mobads.video.MyTextureView;
import com.corpize.sdk.mobads.video.ThirdAppUtils;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * author ：yh
 * date : 2020-02-15 22:11
 * description : 激励视频的播放页面
 */
public class QcAdVideoActivity extends Activity implements View.OnClickListener {

    private AdmJsonBean             mAdmBean;
    private RewardVideoQcAdListener mListener;
    private String                  mUrl;

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

    private ImageView mIvIcon;
    private TextView  mTvTitle;
    private TextView  mTvContent;
    private ImageView mIvIconLast;
    private TextView  mTvTitleLast;
    private TextView  mTvContentLast;

    private CountDownTimer       mCountDownPreview;
    private CustomCountDownTimer mCountDownTime;
    private boolean              mIsHavePlayComplate   = false;  //视频倒计时 结束了, 视频播放结束了
    private boolean              mIsHaveTextureDestroy = false;  //视频控件是否销毁(自然销毁和切换后台的销毁)
    private int                  mVideoAllTime         = 30000;  //当前视频的总时间,毫秒
    private boolean              mHaveSendStart        = false;  //是否发送开始的监听
    private boolean              mHaveSendCenter       = false;  //是否发送中间的监听
    private boolean              mHaveSendEnd          = false;  //是否发送最后的监听
    private boolean              mHaveClicked          = false;  //是否点击的监听
    private boolean              isFirstInActivity     = true;   //是否是第一次进入activity
    private boolean              isFirstPlayVoide      = true;   //是否是第一次播放视频
    private boolean              isFront;                        //是否处于前台
    private int                  mScreenWidth;
    private int                  mScreenHeight;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置全屏展示
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //加载界面
        setContentView(R.layout.qcad_dialog_video_paly);

        mListener = RewardVideoManager.get().getListener();
        mAdmBean = (AdmJsonBean) getIntent().getSerializableExtra("dataBean");
        if (mAdmBean != null) {
            //初始化界面
            initView();
            //初始化数据
            initData();
            //播放视频
            play(mUrl);
        }

    }

    /**
     * 内部播放器的逻辑
     */
    private void initView () {
        int wigth = mAdmBean.getWidth();
        int higth = mAdmBean.getHeight();
        mUrl = mAdmBean.getVideourl();

        mRlPlay = findViewById(R.id.dialog_ad_rl_paly);
        mTextureView = findViewById(R.id.dialog_ad_textureview);
        mIvPreview = findViewById(R.id.dialog_ad_iv_preview);
        mLoading = findViewById(R.id.dialog_ad_loading);
        mTvTimeDown = findViewById(R.id.dialog_ad_time_down);

        //按钮显示控件
        mIvClose = findViewById(R.id.dialog_ad_iv_close);
        mTvRefresh = findViewById(R.id.dialog_ad_refresh);
        mBtDownApk = findViewById(R.id.bt_ad_download);
        mLastBtDownApk = findViewById(R.id.bt_ad_download_last);

        //文字图片显示控件
        mIvIcon = findViewById(R.id.ad_icon);
        mTvTitle = findViewById(R.id.tv_ad_title);
        mTvContent = findViewById(R.id.tv_ad_content);
        mIvIconLast = findViewById(R.id.ad_icon_last);
        mTvTitleLast = findViewById(R.id.tv_ad_title_last);
        mTvContentLast = findViewById(R.id.tv_ad_content_last);

        //区域显示控件
        mLastLl = findViewById(R.id.dialog_ad_ll_last);
        mBottonLl = findViewById(R.id.dialog_ad_ll_bottom);

        mTvRefresh.setOnClickListener(this);
        mIvClose.setOnClickListener(this);
        mBtDownApk.setOnClickListener(this);
        mLastBtDownApk.setOnClickListener(this);

        getClickXYPosition1(mBtDownApk);
        getClickXYPosition2(mLastBtDownApk);


        // 设置控件大小及图片在控件中的位置
        mScreenWidth = DeviceUtil.getScreenWidth(this);
        mScreenHeight = DeviceUtil.getRealyScreenHeight(this);
        RelativeLayout.LayoutParams mVideoParams = (RelativeLayout.LayoutParams) mRlPlay.getLayoutParams();
        mVideoParams.width = mScreenWidth;
        mVideoParams.height = mScreenWidth * higth / wigth;
        mVideoParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        //mRlPlay.setLayoutParams(mVideoParams);

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
            int    wigth    = mAdmBean.getWidth();
            int    higth    = mAdmBean.getHeight();

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
                Glide.with(this).load(firstimg).into(mIvPreview);
                //首次加载的图片的曝光
                sendShowExposure(mAdmBean.getImps(), mScreenWidth, mScreenWidth * higth / wigth);
            }

            IconBean icon = mAdmBean.getIcon();
            if (icon != null && !TextUtils.isEmpty(icon.getResource())) {
                Glide.with(this).load(icon.getResource()).into(mIvIcon);
                Glide.with(this).load(icon.getResource()).into(mIvIconLast);
                //首次加载的icon的曝光
                sendShowExposure(icon.getImps(), icon.getW(), icon.getH());

            } else {
                mIvIcon.setVisibility(View.GONE);
                mIvIconLast.setVisibility(View.GONE);
            }

            if (mListener != null) {
                mListener.onADExposure(ErrorUtil.QC);
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
                    LogUtils.e("开始计算倒计时");
                    if (!isFirstPlayVoide) {
                        //showPreview(false);//第一次倒计时,在视频缓存结束后
                    }
                }

                //开启播放的曝光监听
                if (!mHaveSendStart) {
                    mHaveSendStart = true;
                    if (mAdmBean != null && mAdmBean.getEvent() != null) {
                        QcHttpUtil.sendAdExposure(mAdmBean.getEvent().getStart());
                    }
                }

            }

            @Override
            public void OnVideoPreparedListener (MediaPlayer mp) {
                if (isFirstPlayVoide) {//TODO:视频缓存好了可以播放的页面
                    //isFirstPlayVoide = false;
                    showPreview(false);//第一次倒计时,在视频缓存结束后
                }
            }

            @Override
            public void onInfoListener (MediaPlayer mp, int what, int extra) {
                if (what == 804 && extra == -1004) {//播放过程中网络中断
                    mTvRefresh.setVisibility(View.VISIBLE);
                    mLoading.setVisibility(View.GONE);
                }
            }

            @Override
            public void OnCompletionListener (MediaPlayer mp) {//视频播放完毕
                LogUtils.d("视频播放完毕");
                mIsHavePlayComplate = true;

                mTextureView.stop();    //视频播放停止

                //界面显示
                mIvPreview.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.GONE);
                mTvTimeDown.setVisibility(View.GONE);
                mIvClose.setVisibility(View.VISIBLE);
                //结束倒计时
                closeCountDownTime();

                //对数据进行处理
                mBottonLl.setVisibility(View.GONE);
                mLastLl.setVisibility(View.VISIBLE);
                //开启控件的弹动动画
                startViewAnimation(mLastBtDownApk);

                //发送视频播放完成的曝光
                if (!mHaveSendEnd) {
                    mHaveSendEnd = true;
                    if (mAdmBean != null && mAdmBean.getEvent() != null) {
                        QcHttpUtil.sendAdExposure(mAdmBean.getEvent().getComplete());
                    }
                }

                if (mListener != null) {
                    mListener.onAdCompletion();
                }
            }

            @Override
            public void OnErrorListener (MediaPlayer mp, int what, int extra) {
                LogUtils.d("播放错误" + what + extra);
                mTvRefresh.setVisibility(View.VISIBLE);
                mLoading.setVisibility(View.GONE);

            }

            @Override
            public void OnTextureDestroyedListener () {
                LogUtils.d("视频控件销毁了");
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
        final Animation shake    = AnimationUtils.loadAnimation(this, R.anim.qcad_shake_ad);//加载动画资源文件
        final Animation shakeEnd = AnimationUtils.loadAnimation(this, R.anim.qcad_shake_ad_end);//加载动画资源文件
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
        LogUtils.d("获取当前的位置=" + currentPosition + "倒计时时间=" + distanceTime);
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
                LogUtils.d("时间=" + time + " 原时间=" + millisUntilFinished);
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
    protected void onResume () {
        super.onResume();
        isFront = true;

        if (!isFirstInActivity) {
            //首次不进入
            LogUtils.i("应用切换到了前台");
            if (!mIsHavePlayComplate) {//视频未播放完成,从后台切换回来
                if (!mIsHaveTextureDestroy) {//视频控件未销毁,从后台切换回来
                    //应用切换后台,视频未停止
                    mTextureView.start();
                    startViewClickTimeDown();
                }
            }
        } else {
            isFirstInActivity = false;
        }

    }

    @Override
    protected void onPause () {
        super.onPause();
        isFront = false;
        LogUtils.i("应用切换到了后台");
        if (!mIsHavePlayComplate) {
            if (!mIsHaveTextureDestroy) {//部分手机应用切换后台之后,控件被小伙
                //应用切换后台,视频未停止,暂停播放
                mTextureView.pause();
                if (mCountDownTime != null) {
                    mCountDownTime.cancel();
                    mCountDownTime = null;
                }
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent (KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            //do something.
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //不执行父类点击事件
            return true;
        }
        return super.onKeyDown(keyCode, event);//继续执行父类其他点击事件
    }

    @Override
    public void onBackPressed () {
        // super.onBackPressed();
    }

    /**
     * 发送曝光,计算了宽高及时间戳
     */
    private void sendShowExposure (List<String> imgList, int wigth, int heigth) {
        long time = System.currentTimeMillis();

        if (imgList != null && imgList.size() > 0) {
            for (int i = 0; i < imgList.size(); i++) {
                String urlOld = imgList.get(i);
                String url    = urlOld;
                if (url.contains("__WIDTH__")) {//宽度替换
                    url = url.replace("__WIDTH__", wigth + "");
                }
                if (url.contains("__HEIGHT__")) {//高度替换
                    url = url.replace("__HEIGHT__", heigth + "");
                }
                if (url.contains("__TIME_STAMP__")) {//时间戳的替换
                    url = url.replace("__TIME_STAMP__", time + "");
                }

                QcHttpUtil.sendAdExposure(url);

            }
        }

    }

    @Override
    public void onClick (View view) {
        int id = view.getId();
        if (id == R.id.dialog_ad_refresh) {         //刷新重新播放视频
            mTextureView.resumeStart();
            mTvRefresh.setVisibility(View.GONE);
            mLoading.setVisibility(View.VISIBLE);

        } else if (id == R.id.dialog_ad_iv_close) { //TODO:关闭dialog的按钮
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

            if (mListener != null) {
                mListener.onAdClose();
            }

            finish();

        } else if (id == R.id.bt_ad_download || id == R.id.bt_ad_download_last) {   //todo:打开链接,下载apk
            if (mAdmBean != null) {
                //点击监听
                if (!mHaveClicked) {
                    mHaveClicked = true;
                    sendClickExposure(mAdmBean.getClks());
                }

                if (mListener != null) {
                    mListener.onAdClicked(ErrorUtil.QC);
                }

                //根据不同的返回打开不同的页面
                String deeplink = mAdmBean.getDeeplink();
                if (!TextUtils.isEmpty(deeplink)
                        && ThirdAppUtils.openLinkApp(this, deeplink)) {
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
                            new DownloadInstaller(this, ldp, new DownloadProgressCallBack() {
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
                            Intent intent = new Intent(this, QcAdDetialActivity.class);
                            intent.putExtra("url", ldp);
                            startActivity(intent);
                            LogUtils.d("跳转的地址1=" + ldp);

                        } else if (action == 2) {
                            Uri    uri      = Uri.parse(ldp);
                            Intent intent11 = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent11);
                            LogUtils.d("跳转的地址2=" + ldp);
                        }
                    }
                }


            }
        }
    }

    /**
     * 广告位点击请求(企创广告)
     */
    public void sendClickExposure (final List<String> list) {

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
                    url = url.replace("__WIDTH__", mScreenWidth + "");
                }
                if (url.contains("__HEIGHT__")) {//高度替换
                    url = url.replace("__HEIGHT__", mScreenHeight + "");
                }
                if (url.contains("__TIME_STAMP__")) {//时间戳的替换
                    url = url.replace("__TIME_STAMP__", time + "");
                }

                QcHttpUtil.sendAdExposure(url);
            }

        }
    }

    private float mClickX;                     //企创 点击位置X
    private float mClickY;                     //企创 点击位置Y

    /**
     * onTouch()事件(企创广告)
     * 注意返回值
     * true： view继续响应Touch操作；
     * false：view不再响应Touch操作，故此处若为false，只能显示起始位置，不能显示实时位置和结束位置
     */
    public void getClickXYPosition1 (View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View view, MotionEvent event) {
                switch (event.getAction()) {
                    //点击的开始位置
                    case MotionEvent.ACTION_DOWN:
                        //tvTouchShowStart.setText("起始位置：(" + event.getX() + "," + event.getY());
                        mClickX = event.getX();
                        mClickY = event.getY();
                        break;

                    //触屏实时位置
                    case MotionEvent.ACTION_MOVE:
                        //tvTouchShow.setText("实时位置：(" + event.getX() + "," + event.getY());
                        break;

                    //离开屏幕的位置
                    case MotionEvent.ACTION_UP:
                        //tvTouchShow.setText("结束位置：(" + event.getX() + "," + event.getY());
                        break;

                    default:
                        break;
                }

                return false;
            }
        });

    }

    public void getClickXYPosition2 (View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View view, MotionEvent event) {
                switch (event.getAction()) {
                    //点击的开始位置
                    case MotionEvent.ACTION_DOWN:
                        //tvTouchShowStart.setText("起始位置：(" + event.getX() + "," + event.getY());
                        mClickX = event.getX();
                        mClickY = event.getY();
                        break;

                    //触屏实时位置
                    case MotionEvent.ACTION_MOVE:
                        //tvTouchShow.setText("实时位置：(" + event.getX() + "," + event.getY());
                        break;

                    //离开屏幕的位置
                    case MotionEvent.ACTION_UP:
                        //tvTouchShow.setText("结束位置：(" + event.getX() + "," + event.getY());
                        break;

                    default:
                        break;
                }

                return false;
            }
        });

    }
}
