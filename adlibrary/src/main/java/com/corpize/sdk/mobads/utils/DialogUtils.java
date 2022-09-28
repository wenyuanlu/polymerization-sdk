package com.corpize.sdk.mobads.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.corpize.sdk.mobads.QcAd;
import com.corpize.sdk.R;
import com.corpize.sdk.mobads.bean.AdResponseBean;
import com.corpize.sdk.mobads.bean.AssetsLinkEventtrackersBean;
import com.corpize.sdk.mobads.bean.ExtBean;
import com.corpize.sdk.mobads.common.ErrorUtil;
import com.corpize.sdk.mobads.listener.InsertQcAdListener;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadInstaller;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadProgressCallBack;
import com.corpize.sdk.mobads.video.ThirdAppUtils;
import com.corpize.sdk.mobads.view.QcAdDetialActivity;
import com.corpize.sdk.mobads.view.WebViewUtils;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * author ：yh
 * date : 2020-02-12 04:06
 * description : 插屏弹窗
 */
public class DialogUtils {

    private static Dialog  mInsertDialog;
    private static boolean mHaveClick        = false;       //是否点击过
    private static boolean mHaveExposure     = false;       //是否曝光
    private static boolean mHaveDeepExposure = false;       //是否deep曝光
    private static boolean mHaveDownStart    = false;//是否发送开始下载曝光请求
    private static boolean mHaveDownComplete = false;//是否发送完成下载曝光请求
    private static boolean mHaveDownInstall  = false;//是否发送开始安装曝光请求
    private static float   mClickX;                         //企创 点击位置X
    private static float   mClickY;                         //企创 点击位置Y


    //展示首页的title的底部弹窗
    public static void showInsertDialog (final Activity activity, final AdResponseBean responseBean, String webViewAdm,
                                         final int width, final int height, final InsertQcAdListener listener) {
        if (mInsertDialog != null && mInsertDialog.isShowing()) {
            mInsertDialog.dismiss();
        }
        mHaveClick = false;
        mHaveExposure = false;
        mHaveDeepExposure = false;

        mInsertDialog = new Dialog(activity, R.style.qcDialog);
        mInsertDialog.setContentView(R.layout.qc_insert_ad_layout);

        //获取控件
        FrameLayout express = mInsertDialog.findViewById(R.id.qc_insert_express);
        ImageView   ad      = mInsertDialog.findViewById(R.id.qc_insert_ad);
        ImageView   close1  = mInsertDialog.findViewById(R.id.qc_insert_close1);
        TextView    tvAd1   = mInsertDialog.findViewById(R.id.qc_insert_ad_text1);
        ImageView   close2  = mInsertDialog.findViewById(R.id.qc_insert_close2);
        TextView    tvAd2   = mInsertDialog.findViewById(R.id.qc_insert_ad_text2);

        //设置控件的大小
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) express.getLayoutParams();
        params.width = width;
        params.height = height;

        express.setLayoutParams(params);
        ad.setLayoutParams(params);

        if (!TextUtils.isEmpty(webViewAdm)) {
            ad.setVisibility(View.GONE);
            close2.setVisibility(View.GONE);
            tvAd2.setVisibility(View.GONE);

            WebView webView = WebViewUtils.initWebview(activity, listener);
            if (webView != null) {
                express.addView(webView);//当前页面加载webview
                WebViewUtils.addData(webView, webViewAdm, 203, width, height);//加载数据
            }

        } else {
            close1.setVisibility(View.GONE);
            tvAd1.setVisibility(View.GONE);
            if (responseBean.getExt() != null && responseBean.getExt().getIurl() != null) {
                Glide.with(activity).load(responseBean.getExt().getIurl()).into(ad);
            }

            //点击事件位置监听
            getClickPosition(ad);

            //广告曝光
            if (!mHaveExposure) {
                mHaveExposure = true;
                sendShowExposure(responseBean.getExt().getImptrackers(), width, height);
            }

            ad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick (View v) {
                    if (listener != null) {
                        listener.onAdClicked(ErrorUtil.QC);
                    }
                    //Ext 广告位点击
                    sendClickExposure(responseBean.getExt().getClicktrackers(), width, height);
                    setClickListener(activity, responseBean.getExt(), width, height);
                }
            });

        }

        close1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                mInsertDialog.dismiss();
                if (listener != null) {
                    listener.onAdClose();
                }
            }
        });

        close2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                mInsertDialog.dismiss();
                if (listener != null) {
                    listener.onAdClose();
                }
            }
        });

        mInsertDialog.setCancelable(false);
        mInsertDialog.show();

    }

    /**
     * 发送曝光,计算了宽高及时间戳
     */
    private static void sendShowExposure (List<String> imgList, int width, int height) {
        long time = System.currentTimeMillis();

        if (imgList != null && imgList.size() > 0) {
            for (int i = 0; i < imgList.size(); i++) {
                String urlOld = imgList.get(i);
                String url    = urlOld;
                if (url.contains("__WIDTH__")) {//宽度替换
                    url = url.replace("__WIDTH__", width + "");
                }
                if (url.contains("__HEIGHT__")) {//高度替换
                    url = url.replace("__HEIGHT__", height + "");
                }
                if (url.contains("__TIME_STAMP__")) {//时间戳的替换
                    url = url.replace("__TIME_STAMP__", time + "");
                }

                QcHttpUtil.sendAdExposure(url);
            }
        }

    }

    /**
     * 获取点击时的x,y轴坐标 onTouch()事件
     * 注意返回值
     * true： view继续响应Touch操作；
     * false：view不再响应Touch操作，故此处若为false，只能显示起始位置，不能显示实时位置和结束位置
     */
    private static void getClickPosition (View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch (View view, MotionEvent event) {
                switch (event.getAction()) {
                    //点击的开始位置
                    case MotionEvent.ACTION_DOWN:
                        //LogUtils.e("起始位置：(" + event.getX() + "," + event.getY());
                        mClickX = event.getX();
                        mClickY = event.getY();
                        break;

                    //触屏实时位置
                    case MotionEvent.ACTION_MOVE:
                        //LogUtils.e("实时位置：(" + event.getX() + "," + event.getY());
                        break;

                    //离开屏幕的位置
                    case MotionEvent.ACTION_UP:
                        //LogUtils.e("结束位置：(" + event.getX() + "," + event.getY());
                        break;

                    default:
                        break;
                }

                return false;
            }
        });

    }

    /**
     * 广告位点击请求(企创广告)
     */
    public static void sendClickExposure (final List<String> list, int width, int height) {
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
                        url = url.replace("__WIDTH__", width + "");
                    }
                    if (url.contains("__HEIGHT__")) {//高度替换
                        url = url.replace("__HEIGHT__", height + "");
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
     * 企创 adtype 为201、202、203 的 Action Listener
     */
    private static void setClickListener (Activity rootActivaty, ExtBean ext, final int width, final int height) {
        if (ext == null) {
            return;
        }
        int action = ext.getAction();
        if (0 == action) {          // 0 - 未确认
        } else if (1 == action) {   // 1 - App webview 打开链接
            if (null != ext.getClickurl()) {
                Intent intent = new Intent(rootActivaty, QcAdDetialActivity.class);
                intent.putExtra("url", ext.getClickurl());
                rootActivaty.startActivity(intent);
            }

        } else if (2 == action) {   // 2 - 系统浏览器打开链接
            if (null != ext.getClickurl()) {
                Uri    uri      = Uri.parse(ext.getClickurl());
                Intent intent11 = new Intent(Intent.ACTION_VIEW, uri);
                rootActivaty.startActivity(intent11);
            }

        } else if (3 == action) {   // 3 - 打开地图
        } else if (4 == action) {   // 4 - 拨打电话
        } else if (5 == action) {   // 5 - 播放视频
        } else if (6 == action) {   // 6 - 下载APP
            if (null != ext.getDfn()) {
                final AssetsLinkEventtrackersBean eventtrackers = ext.getEventtrackers();
                new DownloadInstaller(rootActivaty, ext.getDfn(), new DownloadProgressCallBack() {
                    @Override
                    public void downloadProgress (int progress) {
                        //TODO:下载一半的时候数据曝光
                        if (!mHaveDownStart && eventtrackers != null) {
                            mHaveDownStart = true;
                            sendShowExposure(eventtrackers.getStartdownload(), width, height);
                        }

                        if (progress == 100) {
                            if (!mHaveDownComplete&& eventtrackers != null) {
                                mHaveDownComplete = true;
                                sendShowExposure(eventtrackers.getCompletedownload(), width, height);
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
                            sendShowExposure(eventtrackers.getStartinstall(), width, height);
                        }
                        if (!mHaveDownStart&& eventtrackers != null) {
                            mHaveDownStart = true;
                            sendShowExposure(eventtrackers.getStartdownload(), width, height);
                        }
                        if (!mHaveDownComplete&& eventtrackers != null) {
                            mHaveDownComplete = true;
                            sendShowExposure(eventtrackers.getCompletedownload(), width, height);
                        }
                    }
                }).start();

            }

        } else if (7 == action) {   // 7 - deeplink 链接
            String deeplink = ext.getFallback();
            if (null != deeplink && ThirdAppUtils.openLinkApp(rootActivaty, deeplink)) {
                if (!mHaveDeepExposure) {
                    mHaveDeepExposure = true;
                    sendShowExposure(ext.getFallbacktrackers(), width, height);
                }
            } else {
                if (null != ext.getClickurl()) {
                    Uri    uri      = Uri.parse(ext.getClickurl());
                    Intent intent11 = new Intent(Intent.ACTION_VIEW, uri);
                    rootActivaty.startActivity(intent11);
                }
            }

        } else {
            if (null != ext.getClickurl()) {
                Uri    uri      = Uri.parse(ext.getClickurl());
                Intent intent11 = new Intent(Intent.ACTION_VIEW, uri);
                rootActivaty.startActivity(intent11);
            }

        }

    }


}
