package com.corpize.sdk.mobads.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.corpize.sdk.mobads.QcAd;
import com.corpize.sdk.R;
import com.corpize.sdk.mobads.admanager.InfoOneManager;
import com.corpize.sdk.mobads.bean.AssetsBean;
import com.corpize.sdk.mobads.bean.AssetsLinkBean;
import com.corpize.sdk.mobads.bean.AssetsLinkEventtrackersBean;
import com.corpize.sdk.mobads.bean.NativeBean;
import com.corpize.sdk.mobads.common.ErrorUtil;
import com.corpize.sdk.mobads.listener.InfoOneQcAdListener;
import com.corpize.sdk.mobads.utils.DeviceUtil;
import com.corpize.sdk.mobads.utils.LogUtils;
import com.corpize.sdk.mobads.utils.QcHttpUtil;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadInstaller;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadProgressCallBack;
import com.corpize.sdk.mobads.video.ThirdAppUtils;
import com.bumptech.glide.Glide;


import java.util.List;

/**
 * author ：yh
 * date : 2020-02-16 02:52
 * description :企创单图信息流
 */
public class QcOneInfoAdView extends FrameLayout {

    private View         mView;
    private TextView     mTvTitle;
    private TextView     mTvContent;
    private LinearLayout mAdInfoContainer;
    private ImageView    mImgLogo;
    private ImageView    mIvClose;
    private ImageView    mImgPoster;
    private ImageView    mImgPoster1;
    private ImageView    mImgPoster2;
    private ImageView    mImgPoster3;
    private Button       mBtnDownload;
    private Context      mContext;
    private NativeBean   mResponse;
    private String       mLayout = "0";
    private float        mRealyWidth;

    private String mTitle   = "";
    private String mContent = "";
    private String mImgUrl  = "";
    private String mLogoUrl = "";

    private float               mClickX;
    private float               mClickY;
    private boolean             mHaveSendShow     = false;//是否发送展示曝光请求
    private boolean             mHaveSendClick    = false;//是否发送点击曝光请求
    private boolean             mHaveSendDeep     = false;//是否发送deeplink曝光请求
    private boolean             mHaveDownStart    = false;//是否发送开始下载曝光请求
    private boolean             mHaveDownComplete = false;//是否发送完成下载曝光请求
    private boolean             mHaveDownInstall  = false;//是否发送开始安装曝光请求
    private InfoOneQcAdListener mListener;
    private int                 mWidth;
    private int                 mHeight;


    public QcOneInfoAdView (@NonNull Context context) {
        this(context, null);
    }

    public QcOneInfoAdView (@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        intView(context);
    }

    public QcOneInfoAdView (@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    public QcOneInfoAdView (@NonNull Context context, @Nullable AttributeSet attrs, NativeBean bean,
                            @NonNull String layout, float realyWidth, @NonNull InfoOneQcAdListener listener) {
        super(context, attrs);
        this.mContext = context;
        this.mResponse = bean;
        this.mLayout = layout;
        this.mRealyWidth = realyWidth;
        this.mListener = listener;
        intView(context);
    }

    @Override
    protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //LogUtils.e("进入自定义控件的onLayout");
        mWidth = getWidth();
        mHeight = getHeight();
        //加载的时候再曝光
        if (!TextUtils.isEmpty(mImgUrl)) {
            if (null != mResponse.getImptrackers() && !mHaveSendShow) {
                mHaveSendShow = true;
                sendShowExposure(mResponse.getImptrackers());//Native 广告位曝光
                if (mListener != null) {
                    mListener.onAdExposure(ErrorUtil.QC);
                }
            }
        }
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 初始化数据
     */
    private void intView (final Context context) {
        //LogUtils.e("返会的layout=" + mLayout);
        if (mLayout.equals("1")) {
            mView = LayoutInflater.from(context).inflate(R.layout.item_express_qc_one2, null);//左图右文
        } else if (mLayout.equals("2")) {
            mView = LayoutInflater.from(context).inflate(R.layout.item_express_qc_one1, null);//左文右图
        } else if (mLayout.equals("3")) {
            mView = LayoutInflater.from(context).inflate(R.layout.item_express_qc_one3, null);//上图下文
        } else if (mLayout.equals("4")) {
            mView = LayoutInflater.from(context).inflate(R.layout.item_express_qc_one4, null);//上文下图
        } else if (mLayout.equals("5")) {
            mView = LayoutInflater.from(context).inflate(R.layout.item_express_qc_one5, null);//上文下浮层
        } else {
            mView = LayoutInflater.from(context).inflate(R.layout.item_express_qc_one1, null);//左文右图
        }

        mAdInfoContainer = mView.findViewById(R.id.adInfoContainer);
        mTvTitle = (TextView) mView.findViewById(R.id.text_title);
        mTvContent = (TextView) mView.findViewById(R.id.tv_content);
        mImgLogo = (ImageView) mView.findViewById(R.id.img_logo);
        mIvClose = (ImageView) mView.findViewById(R.id.iv_close);
        mImgPoster = (ImageView) mView.findViewById(R.id.img_poster);
        mBtnDownload = (Button) mView.findViewById(R.id.btn_download);

        //设置全局的宽度
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mAdInfoContainer.getLayoutParams();
        params.width = DeviceUtil.dip2px(context, mRealyWidth);
        mAdInfoContainer.setLayoutParams(params);

        //设置图片的宽高
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mImgPoster.getLayoutParams();
        int                         width        = DeviceUtil.getScreenWidth(context);
        //LogUtils.d("获取的长度为=" + width);
        if (mLayout.equals("1")) {
            layoutParams.height = (width - DeviceUtil.dip2px(context, 40)) * 2 / 9;
            mImgPoster.setLayoutParams(layoutParams);
        } else if (mLayout.equals("2")) {
            layoutParams.height = (width - DeviceUtil.dip2px(context, 40)) * 2 / 9;
            mImgPoster.setLayoutParams(layoutParams);
        } else if (mLayout.equals("3")) {
            layoutParams.height = (width - DeviceUtil.dip2px(context, 30)) * 9 / 16;
            mImgPoster.setLayoutParams(layoutParams);
        } else if (mLayout.equals("4")) {
            layoutParams.height = (width - DeviceUtil.dip2px(context, 30)) * 9 / 16;
            mImgPoster.setLayoutParams(layoutParams);
        } else if (mLayout.equals("5")) {
            layoutParams.height = (width - DeviceUtil.dip2px(context, 30)) * 9 / 16;
            mImgPoster.setLayoutParams(layoutParams);
        } else {
            layoutParams.height = (width - DeviceUtil.dip2px(context, 40)) * 2 / 9;
            mImgPoster.setLayoutParams(layoutParams);
        }

        //渲染填充
        //render ();
    }

    /**
     * 渲染界面
     */
    public void render () {
        initData();
        addView(mView);
    }

    /**
     * 初始化数据
     */
    private void initData () {
        List<AssetsBean> assets = mResponse.getAssets();
        if (assets != null && assets.size() > 0) {
            //遍历获取数据
            for (int i = 0; i < assets.size(); i++) {
                AssetsBean assetsBean = assets.get(i);
                //获取标题
                if (null != assetsBean.getTitle()) {
                    mTitle = assetsBean.getTitle().getText();
                }

                //获取内容
                if (assetsBean.getData() != null) {
                    mContent = assetsBean.getData().getValue();
                }

                //获取图标和图片
                if (null != assetsBean.getImg()) {
                    //获取图标
                    if (1 == assetsBean.getImg().getType()) {
                        mLogoUrl = assetsBean.getImg().getUrl().get(0);
                    }
                    //获取图片
                    if (3 == assetsBean.getImg().getType()) {
                        mImgUrl = assetsBean.getImg().getUrl().get(0);
                        mAdInfoContainer.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        //数据填充进控件
        mTvTitle.setText(mTitle);
        mTvContent.setText(mContent);
        if (!TextUtils.isEmpty(mLogoUrl)) {
            Glide.with(mContext).load(mLogoUrl).into(mImgLogo);
        } else {
            mImgLogo.setVisibility(GONE);
        }

        if (!TextUtils.isEmpty(mImgUrl)) {
            Glide.with(mContext).load(mImgUrl).into(mImgPoster);
        }

        //按钮的显示
        final AssetsLinkBean link = mResponse.getLink();
        if (link == null) {
            return;
        }

        int action = link.getAction();
        switch (action) {
            case 0:  // 0 - 未确认
                mBtnDownload.setText("未确认");
                break;

            case 1:  // 1 - App webview 打开链接
            case 2:  // 2 - 系统浏览器打开链接
            case 3:  // 3 - 打开地图
                mBtnDownload.setText("打开");
                break;
            case 4:  // 4 - 拨打电话
                mBtnDownload.setText("拨打");
                break;
            case 5:  // 5 - 播放视频
                mBtnDownload.setText("播放");
                break;
            case 6:  // 6- 下载APP
                mBtnDownload.setText("下载");
                break;
            case 7:  // 7 - deeplink 链接
                mBtnDownload.setText("打开");
                break;

            default:
                mBtnDownload.setText("浏览");
                break;
        }

        //获取点击时候的坐标值
        getClickXYPosition(mAdInfoContainer);

        //点击Link事件
        mBtnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                if (null != link.getUrl()) {
                    // Native 广告位点击
                    sendClickExposure(link.getClicktrackers());
                    // 设置点击事件监听
                    setClickListener((Activity) mContext, mResponse);
                }
            }
        });

        //整体点击
        mAdInfoContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                if (null != link.getUrl()) {
                    // Native 广告位点击
                    sendClickExposure(link.getClicktrackers());
                    // 设置点击事件
                    setClickListener((Activity) mContext, mResponse);

                }
            }
        });

        mIvClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick (View v) {
                //移除自己
                removeView();
            }
        });

    }

    /**
     * 把自己移除
     */
    private void removeView () {
        if (mListener != null) {
            mListener.onAdClose(this, ErrorUtil.QC);
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
                    case MotionEvent.ACTION_DOWN:   //点击的开始位置
                        //LogUtils.e("起始位置：(" + event.getX() + "," + event.getY());
                        mClickX = event.getX();
                        mClickY = event.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:   //触屏实时位置
                        break;
                    case MotionEvent.ACTION_UP:     //离开屏幕的位置
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
     * 点击事件
     */
    public void setClickListener (Activity activity, NativeBean bean) {
        final AssetsLinkBean link = bean.getLink();
        if (link != null) {
            if (mListener != null) {
                mListener.onAdClicked(this,ErrorUtil.QC);
            }
            int action = link.getAction();

            if (0 == action) {//链接未确认...

            } else if (1 == action) {       // 1 - App webview 打开链接
                if (!TextUtils.isEmpty(link.getUrl())) {
                    Intent intent = new Intent(activity, QcAdDetialActivity.class);
                    intent.putExtra("url", link.getUrl());
                    activity.startActivity(intent);
                }

            } else if (2 == action) {       // 2 - 系统浏览器打开链接
                if (!TextUtils.isEmpty(link.getUrl())) {
                    Uri    uri     = Uri.parse(link.getUrl());
                    Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
                    activity.startActivity(intent1);
                }

            } else if (3 == action) {       // 3 - 打开地图
            } else if (4 == action) {       // 4 - 拨打电话
            } else if (5 == action) {       // 5 - 播放视频
            } else if (6 == action) {       // 6- 下载APP
                String dfn = link.getDfn();
                if (!TextUtils.isEmpty(dfn)) {
                    final AssetsLinkEventtrackersBean eventtrackers = link.getEventtrackers();
                    new DownloadInstaller(activity, dfn, new DownloadProgressCallBack() {
                        @Override
                        public void downloadProgress (int progress) {
                            //TODO:下载一半的时候数据曝光
                            if (!mHaveDownStart && eventtrackers != null) {
                                mHaveDownStart = true;
                                sendShowExposure(eventtrackers.getStartdownload());
                            }
                            if (progress == 100) {
                                if (!mHaveDownComplete && eventtrackers != null) {
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
                            if (!mHaveDownInstall && eventtrackers != null) {
                                mHaveDownInstall = true;
                                sendShowExposure(eventtrackers.getStartinstall());
                            }
                            if (!mHaveDownStart && eventtrackers != null) {
                                mHaveDownStart = true;
                                sendShowExposure(eventtrackers.getStartdownload());
                            }
                            if (!mHaveDownComplete && eventtrackers != null) {
                                mHaveDownComplete = true;
                                sendShowExposure(eventtrackers.getCompletedownload());
                            }
                        }
                    }).start();
                }

            } else if (7 == action) {    // 7 - deeplink 链接
                String deeplink = link.getFallback();
                if (!TextUtils.isEmpty(deeplink) && ThirdAppUtils.openLinkApp(activity, deeplink)) {
                    //Ext 广告位deeplink点击
                    if (!mHaveSendDeep) {
                        mHaveSendDeep = true;
                        sendShowExposure(link.getFallbacktrackers());
                    }
                } else {
                    if (!TextUtils.isEmpty(link.getUrl())) {
                        Uri    uri      = Uri.parse(link.getUrl());
                        Intent intent11 = new Intent(Intent.ACTION_VIEW, uri);
                        activity.startActivity(intent11);
                    }
                }

            } else {

                if (null != link.getUrl()) {
                    Uri    uri      = Uri.parse(link.getUrl());
                    Intent intent11 = new Intent(Intent.ACTION_VIEW, uri);
                    activity.startActivity(intent11);
                }

            }
        }
    }

}
