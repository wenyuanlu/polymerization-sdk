package com.corpize.sdk.mobads.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.corpize.sdk.mobads.bean.AdResponseBean;
import com.corpize.sdk.mobads.bean.AssetsLinkEventtrackersBean;
import com.corpize.sdk.mobads.common.ErrorUtil;
import com.corpize.sdk.mobads.listener.BannerQcAdListener;
import com.corpize.sdk.mobads.utils.DeviceUtil;
import com.corpize.sdk.mobads.utils.LogUtils;
import com.corpize.sdk.mobads.utils.QcHttpUtil;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadInstaller;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadProgressCallBack;
import com.corpize.sdk.mobads.video.ThirdAppUtils;
import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.List;

/**
 * author ：yh
 * date : 2020-02-21 21:14
 * description : 企创Banner广告
 */
public class QcBannerAdView extends FrameLayout {

    private Context        mContext;
    private Activity       mActivity;
    private AdResponseBean mResponse;
    private float          mRealyWidth;

    private float              mClickX;
    private float              mClickY;
    private boolean            mHaveSendShow     = false;//是否发送展示曝光请求
    private boolean            mHaveSendClick    = false;//是否发送点击曝光请求
    private boolean            mHaveSendDeep     = false;//是否发送deeplink曝光请求
    private boolean            mHaveDownStart    = false;//是否发送开始下载曝光请求
    private boolean            mHaveDownComplete = false;//是否发送完成下载曝光请求
    private boolean            mHaveDownInstall  = false;//是否发送开始安装曝光请求
    private BannerQcAdListener mListener;
    private int                mWidth;
    private int                mHeight;
    private int                mCurrentWidth;
    private int                mCurrentHeight;
    private boolean            mIsWebview;

    public QcBannerAdView (@NonNull Context context) {
        this(context, null);
    }

    public QcBannerAdView (@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public QcBannerAdView (@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    public QcBannerAdView (@NonNull Activity activity, @Nullable AttributeSet attrs,
                           AdResponseBean bean, int width, int height, float realyWidth, BannerQcAdListener listener) {
        super(activity, attrs);
        this.mActivity = activity;
        this.mResponse = bean;
        this.mRealyWidth = realyWidth;
        this.mListener = listener;
        intView(width, height);//初始化页面
    }

    @Override
    protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //LogUtils.e("进入banner的onLayout");
        mWidth = getWidth();
        mHeight = getHeight();

        //加载的时候 再曝光(本地数据的曝光)
        if (!mHaveSendShow && mResponse != null && mResponse.getExt() != null
                && mResponse.getExt().getImptrackers() != null) {
            mHaveSendShow = true;
            sendShowExposure(mResponse.getExt().getImptrackers());// Native 广告位曝光
            if (mListener != null) {
                mListener.onADExposure(ErrorUtil.QC);
            }
        }

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
    private void intView (int width, int height) {
        if (mResponse == null && mListener != null) {
            mListener.onAdError(ErrorUtil.QC, "加载错误");
            return;
        }
        mHaveSendShow = false;
        final String webViewAdm = mResponse.getAdm();
        //设置默认宽高
        int sw = DeviceUtil.getDeviceWidth();
        //TODO:根据听说的实际情况,进行的宽度的配置
        mCurrentWidth = DeviceUtil.dip2px(mActivity, mRealyWidth - 1);
        mCurrentHeight = sw * 5 / 32;
        LogUtils.e("返回的总宽度=" + sw + "|实际宽度=" + mCurrentWidth);
        //根据下发数据计算真正宽高
        if (width > 0 && height > 0) {
            mCurrentWidth = DeviceUtil.dip2px(mActivity, mRealyWidth - 1);
            mCurrentHeight = mCurrentWidth * height / width;
        }

        //创建内部的广告控件
        if (!TextUtils.isEmpty(webViewAdm)) {//webview的形式加载
            mIsWebview = true;
            WebView webView = WebViewUtils.initWebview(mActivity, mListener);
            if (webView != null) {
                addView(webView);//当前页面加载webview
                WebViewUtils.addData(webView, webViewAdm, 201, mCurrentWidth, mCurrentHeight);//加载数据
            }

        } else {
            mIsWebview = true;
            ImageView imageView = new ImageView(mActivity);
            addView(imageView);//当前页面加载ImageView
            //设置图片的大小
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
            params.width = mCurrentWidth;
            params.height = mCurrentHeight;
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            Glide.with(mActivity).load(mResponse.getExt().getIurl()).into(imageView);

            //获取点击的坐标
            getClickXYPosition(imageView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick (View v) {
                    if (mListener != null) {
                        mListener.onAdClicked(ErrorUtil.QC);
                    }
                    //点击监听
                    sendClickExposure(mResponse.getExt().getClicktrackers());
                    setClickListener(mActivity, mResponse);


                }
            });
        }

        //TODO:设置广告的标识
        TextView tvAd = new TextView(mActivity);
        tvAd.setText("广告");
        tvAd.setTextColor(Color.parseColor("#ECECEC"));
        tvAd.setTextSize(8);
        tvAd.setPadding(10, 4, 10, 4);
        addView(tvAd);//当前页面加载ImageView
        tvAd.setBackgroundColor(Color.parseColor("#3E222222"));
        FrameLayout.LayoutParams adParams = (FrameLayout.LayoutParams) tvAd.getLayoutParams();
        adParams.width = FrameLayout.LayoutParams.WRAP_CONTENT;
        adParams.height = FrameLayout.LayoutParams.WRAP_CONTENT;
        adParams.gravity = Gravity.BOTTOM | Gravity.END;
        adParams.bottomMargin = 0;
        adParams.rightMargin = 60;
        tvAd.setLayoutParams(adParams);

        //渲染填充
        //render ();
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
                        if (!mHaveDownStart&& eventtrackers != null) {
                            mHaveDownStart = true;
                            sendShowExposure(eventtrackers.getStartdownload());
                        }
                        if (progress == 100) {
                            if (!mHaveDownComplete&& eventtrackers != null) {
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
                        if (!mHaveDownInstall&& eventtrackers != null) {
                            mHaveDownInstall = true;
                            sendShowExposure(eventtrackers.getStartinstall());
                        }

                        if (!mHaveDownStart&& eventtrackers != null) {
                            mHaveDownStart = true;
                            sendShowExposure(eventtrackers.getStartdownload());
                        }

                        if (!mHaveDownComplete&& eventtrackers != null) {
                            mHaveDownComplete = true;
                            sendShowExposure(eventtrackers.getCompletedownload());
                        }
                    }
                }).start();

            }

        } else if (7 == responseBean.getExt().getAction()) {   // 7 - deeplink 链接
            String deeplink = responseBean.getExt().getFallback();
            if (null != deeplink && ThirdAppUtils.openLinkApp(activity, deeplink)) {
                //成功打开,发送deepLink的曝光
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

    /**
     * 点击事件
     */
    /*public void setClickListener (Activity activity, NativeBean bean) {
        AssetsLinkBean link = bean.getLink();
        if (link != null) {
            if (mListener != null) {
                mListener.onAdClicked();
            }
            int action = link.getAction();

            if (0 == action) {//链接未确认...

            } else if (1 == action) {         // 1 - App webview 打开链接
                if (!TextUtils.isEmpty(link.getUrl())) {
                    Intent intent = new Intent(activity, QcAdDetialActivity.class);
                    intent.putExtra("url", link.getUrl());
                    activity.startActivity(intent);
                }

            } else if (2 == action) {         // 2 - 系统浏览器打开链接
                if (!TextUtils.isEmpty(link.getUrl())) {
                    Uri    uri      = Uri.parse(link.getUrl());
                    Intent intent11 = new Intent(Intent.ACTION_VIEW, uri);
                    activity.startActivity(intent11);
                }

            } else if (3 == action) {         // 3 - 打开地图
            } else if (4 == action) {         // 4 - 拨打电话
            } else if (5 == action) {         // 5 - 播放视频
            } else if (6 == action) {         // 6- 下载APP
                String dfn = link.getDfn();
                if (!TextUtils.isEmpty(dfn)) {
                    final AssetsLinkEventtrackersBean eventtrackers = link.getEventtrackers();
                    new DownloadInstaller(activity, dfn, new DownloadProgressCallBack() {
                        @Override
                        public void downloadProgress (int progress) {
                            //TODO:下载一半的时候数据曝光
                            if (!mHaveDownStart) {
                                mHaveDownStart = true;
                                sendShowExposure(eventtrackers.getStartdownload());
                            }
                        }

                        @Override
                        public void downloadException (Exception e) {
                        }

                        @Override
                        public void onInstallStart () {
                            LogUtils.d("开始安装=");
                            if (!mHaveDownInstall) {
                                mHaveDownInstall = true;
                                sendShowExposure(eventtrackers.getStartinstall());
                            }
                        }
                    }).start();

                }

            } else if (7 == action) {   // 7 - deeplink 链接
                //Ext 广告位deeplink点击
                if (!mHaveSendDeep) {
                    mHaveSendDeep = true;
                    sendShowExposure(link.getFallbacktrackers());
                }

                String deeplink = link.getFallback();
                if (null != deeplink && ThirdAppUtils.openLinkApp(activity, deeplink)) {
                } else {
                    if (null != link.getUrl()) {
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

    }*/


}
