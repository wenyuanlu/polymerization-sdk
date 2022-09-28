package com.corpize.sdk.mobads.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.corpize.sdk.mobads.common.ErrorUtil;
import com.corpize.sdk.mobads.listener.QCADListener;
import com.corpize.sdk.mobads.utils.LogUtils;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadInstaller;
import com.corpize.sdk.mobads.utils.downloadinstaller.DownloadProgressCallBack;


/**
 * author: yh
 * date: 2020-02-09 22:58
 * description: TODO:
 */
public class WebViewUtils {

    private static WebViewUtils instance;
    private static WebView      mWebView;
    private static Activity     mActivity;

    /**
     * 单利模式
     */
    public static WebViewUtils get () {
        /*if (instance == null) {
            synchronized (WebViewUtils.class) {
                if (instance == null) {
                    instance = new WebViewUtils();
                }
            }
        }*/
        instance = new WebViewUtils();
        return instance;
    }

    private WebViewUtils () {
    }

    /**
     * 初始化webview
     */
    public static WebView initWebview (final Activity activity, final QCADListener listener) {
        mActivity = activity;
        //创建Webview
        try {
            mWebView = new WebView(activity);
        } catch (Exception e) {
            e.printStackTrace();
            //创建失败,只在听说很好玩中发现一例
            return null;
        }

        //mWebView.setScrollBarStyle();

        //WebView属性设置！！！
        WebSettings settings = mWebView.getSettings();
        settings.setAllowFileAccess(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDomStorageEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//适应屏幕，内容将自动缩放
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setDatabaseEnabled(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setJavaScriptEnabled(true);


        // webview在安卓5.0之前默认允许其加载混合网络协议内容
        // 在安卓5.0之后，默认不允许加载http与https混合内容，需要设置webview允许其加载混合网络协议内容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        mWebView.setVerticalScrollBarEnabled(false);//设置不显示滚动条
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

        // =======================Web的请求回调设置=======================
        //JS回调方法,需要自定方法的数据
        //mWebView.addJavascriptInterface(new JavaMethod(), "nativeMethod");
        //处理JavaScript的对话框、网站图片、网站title、加载进度等
        mWebView.setWebChromeClient(new MyWebChromeClient());

        //处理文件下载new MyWebViewDownLoadListener()
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart (String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                LogUtils.e("下载url=" + url);

                new DownloadInstaller(activity, url, new DownloadProgressCallBack() {
                    @Override
                    public void downloadProgress (int progress) {
                        //下载一半的时候数据曝光
                        if (progress >= 50) {
                        }
                    }

                    @Override
                    public void downloadException (Exception e) {
                        LogUtils.e("下载错误=" + e.getMessage());
                    }

                    @Override
                    public void onInstallStart () {
                        LogUtils.d("开始安装=");
                    }
                }).start();
            }
        });

        //处理各种通知、请求时间的 new MyWebViewClient()
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
                //handler.cancel(); // Android默认的处理方式
                handler.proceed();  // 接受所有网站的证书  解决https拦截问题
            }

            @Override
            public void onPageStarted (WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished (final WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @RequiresApi (api = Build.VERSION_CODES.LOLLIPOP)
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest (WebView view, WebResourceRequest request) {
                String url = request.getUrl().getPath();
                //LogUtils.e("url1=" + url);
                return super.shouldInterceptRequest(view, request);
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest (WebView view, String url) {
                //LogUtils.e("url2=" + url);
                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading (WebView view, String url) {
                if (!TextUtils.isEmpty(url) && !url.startsWith("http")) {//deeplink类
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        activity.startActivity(intent);
                        Log.e("跳转deepLink=", url);
                    } catch (Exception e) {
                        //e.printStackTrace();
                        Log.e("打开deepLink失败:", url);
                    }

                } else if (!TextUtils.isEmpty(url) && url.contains(".apk")) {//下载类
                    view.loadUrl(url);//调用webview本身的loadUrl方法
                } else {
                    Log.e("跳转url=", url);
                    //TODO:
                    Intent intent = new Intent(activity, QcAdDetialActivity.class);
                    intent.putExtra("url", url);
                    activity.startActivity(intent);
                }

                if (listener != null) {
                    listener.onAdClicked(ErrorUtil.QC);
                }

                return true;

            }

            @RequiresApi (api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading (WebView view, WebResourceRequest request) {
                String url = request.getUrl().getPath();
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        return mWebView;

    }

    /**
     * 加载js代码,显示webview
     */
    public static void addData (WebView webView, String js, int type, int width, int height) {
        if (type == 201) {//banner广告
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) webView.getLayoutParams();
            params.width = width;
            params.height = height;
            webView.setLayoutParams(params);

        } else if (type == 202) {//开屏广告
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) webView.getLayoutParams();
            params.addRule(RelativeLayout.CENTER_VERTICAL);//横向居中，是
            params.width = width;
            params.height = height;
            webView.setLayoutParams(params);

        } else if (type == 203) {//插屏广告
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) webView.getLayoutParams();
            params.width = width;
            params.height = height;
            webView.setLayoutParams(params);
        }

        long time = System.currentTimeMillis();
        if (js.contains("__WIDTH__")) {
            js = js.replace("__WIDTH__", width + "");
        }
        if (js.contains("__HEIGHT__")) {
            js = js.replace("__HEIGHT__", height + "");
        }
        if (js.contains("__TIME_STAMP__")) {//时间戳的替换
            js = js.replace("__TIME_STAMP__", time + "");
        }

        String finalJs = getHtmlData(js);
        if (type == 201) {//banner
            //finalJs = getHtmlDataBanner(js);
            finalJs = getHtmlDataSplash(js);
        } else if (type == 202) {//开屏广告
            finalJs = getHtmlDataSplash(js);
        } else if (type == 203) {//插屏广告
            finalJs = getHtmlDataInstert(js);
        }
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.loadDataWithBaseURL(null, finalJs, "text/html", "utf-8", null);
    }

    /**
     * 加载js代码,显示webview
     */
    public void addData (String js, int type, int width, int height) {
        if (type == 201) {//banner广告
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mWebView.getLayoutParams();
            //params.addRule(RelativeLayout.CENTER_IN_PARENT);//横向居中，是
            params.width = width;
            params.height = height;
            mWebView.setLayoutParams(params);

        } else if (type == 202) {//开屏广告
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mWebView.getLayoutParams();
            params.addRule(RelativeLayout.CENTER_VERTICAL);//横向居中，是
            params.width = width;
            params.height = height;
            mWebView.setLayoutParams(params);

        } else if (type == 203) {//插屏广告
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mWebView.getLayoutParams();
            params.width = width;
            params.height = height;
            mWebView.setLayoutParams(params);
        }

        //跳转类
        //String mycontent = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\"><script src=\"http://ssp.corpize.com/js/app.js\"></script><style>html,body{width:100%; height:100%}*{margin:0;padding:0;border:0}</style></head><body><a href=\"http://www.yindudigital.cn/jsyd/20200106/?WT.mc_id=m1KSeHlUvfVvkyHdq7uC&UX.aucm=slUku9NymqHGYvwSUsms\" onclick=\"clicktrackers(['http://adx-test.corpize.com/test_clks?dx=__DOWN_X__&dy=__DOWN_Y__&ux=__UP_X__&uy=__UP_Y__','http://adx-test.corpize.com/test_clks?w=__WIDTH__&h=__HEIGHT__t=__TIME_STAMP__','http://adx-test.corpize.com/tj?bid=1580806761.182617913&id=0&ua=dcaf2ad9a1edf04c1e8a9b899163a749&ip=123.57.18.109&adid=9716893D4E59C34734D1B5F9ADFE699C&m=rtb&de=1&clk=e9ae8c44237554bdf990f6e972114b0e6664556ee2535e4ff5891ef46b04a3b68e491a3c8ce2626f66e5ea6b37abb695362d470341686e09b908719537f6fc4aabdf293f0ec48cf530e4ce2825868f0bf86b3ad50c50eb9ccb515171e5c69bc14e786057220cc03f7753333c7c8128223f809e664ac0c6433749afa81f96e986&dx=__DOWN_X__&dy=__DOWN_Y__&ux=__UP_X__&uy=__UP_Y__'])\" target=\"_blank\"><img src=\"http://fms.ipinyou.com/5/08/08/5F/F001461U7Lqc003Xpg6M.jpg\" width=\"100%\" height=\"100%\" /></a><img src=\"http://adx-test.corpize.com/tracker\" style=\"display:none\"><img src=\"http://adx-test.corpize.com/test_imps?t=__TIME_STAMP__\" style=\"display:none\"><img src=\"http://adx-test.corpize.com/test_imps?w=__WIDTH__&h=__HEIGHT__\" style=\"display:none\"><img src=\"http://adx-test.corpize.com/tj?bid=1580806761.182608890&id=0&ua=dcaf2ad9a1edf04c1e8a9b899163a749&ip=123.57.18.109&adid=9716893D4E59C34734D1B5F9ADFE699C&m=rtb&de=1&imp=e9ae8c44237554bdf990f6e972114b0e6664556ee2535e4ff5891ef46b04a3b68e491a3c8ce2626f66e5ea6b37abb695362d470341686e09b908719537f6fc4aabdf293f0ec48cf530e4ce2825868f0bf86b3ad50c50eb9ccb515171e5c69bc14e786057220cc03f7753333c7c8128223f809e664ac0c6433749afa81f96e986\" style=\"display:none\"></body>";
        //下载类
        //String mycontent = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\"><script src=\"http://ssp.corpize.com/js/app.js\"></script><style>html,body{width:100%; height:100%}*{margin:0;padding:0;border:0}</style></head><body><a href=\"http://dl.haohaiyoo.cn/app/haohaiyoo.apk?w=__WIDTH__&h=__HEIGHT__\" onclick=\"clicktrackers(['http://adx-test.corpize.com/test_clks?dx=__DOWN_X__&dy=__DOWN_Y__&ux=__UP_X__&uy=__UP_Y__','http://adx-test.corpize.com/test_clks?w=__WIDTH__&h=__HEIGHT__t=__TIME_STAMP__','http://adx-test.corpize.com/tj?bid=1580872422.611626747&id=0&ua=dcaf2ad9a1edf04c1e8a9b899163a749&ip=123.57.18.109&adid=9716893D4E59C34734D1B5F9ADFE699C&m=rtb&de=1&clk=e9ae8c44237554bdf990f6e972114b0e6664556ee2535e4ff5891ef46b04a3b68e491a3c8ce2626f66e5ea6b37abb6950ae803dc599f598e4d8b4cfef56091e38f3c126d35cb31677f345f6b5dbc837d835509fdaaceb8eb27886fafc47938249a0f517fa9cfd81ef65f8168e938753f48210e73fe55842d903b6713fd0cf2eb&dx=__DOWN_X__&dy=__DOWN_Y__&ux=__UP_X__&uy=__UP_Y__'])\" target=\"_blank\"><img src=\"http://fms.ipinyou.com/5/08/08/5F/F001461U7Lqc003Xpg6M.jpg\" width=\"100%\" height=\"100%\" /></a><img src=\"http://adx-test.corpize.com/tracker\" style=\"display:none\"><img src=\"http://adx-test.corpize.com/test_imps?t=__TIME_STAMP__\" style=\"display:none\"><img src=\"http://adx-test.corpize.com/test_imps?w=__WIDTH__&h=__HEIGHT__\" style=\"display:none\"><img src=\"http://adx-test.corpize.com/tj?bid=1580872422.611617886&id=0&ua=dcaf2ad9a1edf04c1e8a9b899163a749&ip=123.57.18.109&adid=9716893D4E59C34734D1B5F9ADFE699C&m=rtb&de=1&imp=e9ae8c44237554bdf990f6e972114b0e6664556ee2535e4ff5891ef46b04a3b68e491a3c8ce2626f66e5ea6b37abb6950ae803dc599f598e4d8b4cfef56091e38f3c126d35cb31677f345f6b5dbc837d835509fdaaceb8eb27886fafc47938249a0f517fa9cfd81ef65f8168e938753f48210e73fe55842d903b6713fd0cf2eb\" style=\"display:none\"></body>";
        //deeplink类
        //String mycontent = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\"><script src=\"http://ssp.corpize.com/js/app.js\"></script><style>html,body{width:100%; height:100%}*{margin:0;padding:0;border:0}</style></head><body><a href=\"tbopen://m.taobao.com/tbopen/index.html?action=ali.open.nav&module=h5&source=alimama&visa=5d429034ad046701&appkey=24537135&bc_fl_src=tanx_df_722544388&backURL=mobilesafe%3A%2F%2Fcom.qihoo360.mobilesafe.ui.index.AppEnterActivity&packageName=com.qihoo360.mobilesafe&h5Url=https%3A%2F%2Fclick.tanx.com%2Ftfn%3Fe%3DsywYXcDBwq9KWSnCql1fefgwwujSplgKvsEL8EExfPn5KFczXEitwu8De7jA9V6K%252frPkVnwMgkxjH96vha4sFgFqzTAfPg2X%252b87GGOKmzufYPMI0kO1IaWSEk1UvLgCd%26u%3Dhttps%253a%252f%252f618.tmall.com%252f%253fwh_weex%253dtrue%2526wh_biz%253dtm%2526bba%253dtrue%2526resource_id%253d15526%26k%3D128%26ext%3Da%253d_IMEI%2526b%253dIDFA%2526c%253dMAC_\" onclick=\"clicktrackers(['http://adx-test.corpize.com/test_clks?dx=__DOWN_X__&dy=__DOWN_Y__&ux=__UP_X__&uy=__UP_Y__','http://adx-test.corpize.com/test_clks?w=__WIDTH__&h=__HEIGHT__t=__TIME_STAMP__','http://adx-test.corpize.com/tj?bid=1580872835.612987366&id=0&ua=dcaf2ad9a1edf04c1e8a9b899163a749&ip=123.57.18.109&adid=9716893D4E59C34734D1B5F9ADFE699C&m=rtb&de=1&clk=e9ae8c44237554bdf990f6e972114b0e6664556ee2535e4ff5891ef46b04a3b68e491a3c8ce2626f66e5ea6b37abb6956d6fccd9883008b6e83e1fcf04322e732791761aff0f26ac079330ffb6cc6c9cafc46006ecd1e09611302c11c33066fd6cce66492cf66b36fa52a94e5d5f08cac773523c724531264cbe84248ea70f6f&dx=__DOWN_X__&dy=__DOWN_Y__&ux=__UP_X__&uy=__UP_Y__'])\" target=\"_blank\"><img src=\"http://fms.ipinyou.com/5/08/08/5F/F001461U7Lqc003Xpg6M.jpg\" width=\"100%\" height=\"100%\" /></a><img src=\"http://adx-test.corpize.com/tracker\" style=\"display:none\"><img src=\"http://adx-test.corpize.com/test_imps?t=__TIME_STAMP__\" style=\"display:none\"><img src=\"http://adx-test.corpize.com/test_imps?w=__WIDTH__&h=__HEIGHT__\" style=\"display:none\"><img src=\"http://adx-test.corpize.com/tj?bid=1580872835.612962008&id=0&ua=dcaf2ad9a1edf04c1e8a9b899163a749&ip=123.57.18.109&adid=9716893D4E59C34734D1B5F9ADFE699C&m=rtb&de=1&imp=e9ae8c44237554bdf990f6e972114b0e6664556ee2535e4ff5891ef46b04a3b68e491a3c8ce2626f66e5ea6b37abb6956d6fccd9883008b6e83e1fcf04322e732791761aff0f26ac079330ffb6cc6c9cafc46006ecd1e09611302c11c33066fd6cce66492cf66b36fa52a94e5d5f08cac773523c724531264cbe84248ea70f6f\" style=\"display:none\"></body>";

        long time = System.currentTimeMillis();
        if (js.contains("__WIDTH__")) {
            js = js.replace("__WIDTH__", width + "");
        }
        if (js.contains("__HEIGHT__")) {
            js = js.replace("__HEIGHT__", height + "");
        }
        if (js.contains("__TIME_STAMP__")) {//时间戳的替换
            js = js.replace("__TIME_STAMP__", time + "");
        }

        String finalJs = getHtmlData(js);
        if (type == 201) {//banner
            finalJs = getHtmlDataSplash(js);
        } else if (type == 202) {//开屏广告
            finalJs = getHtmlDataSplash(js);
        } else if (type == 203) {//插屏广告
            finalJs = getHtmlDataInstert(js);
        }
        mWebView.getSettings().setDefaultTextEncodingName("utf-8");
        mWebView.loadDataWithBaseURL(null, finalJs, "text/html", "utf-8", null);
    }

    /**
     * 加载html标签,插屏填满的展示
     */
    private static String getHtmlDataInstert (String bodyHTML) {
        String head = "<head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width:100%; width:100%; height:100%!important;}</style>" +
                "</head>";
        return "<html>" + head + "<body>" + bodyHTML + "</body></html>";
    }

    /**
     * TODO:加载html标签,Banner.听说app的适配
     */
    private static String getHtmlDataBanner (String bodyHTML) {
        String head = "<head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width:90%; width:90%; height:90%!important;}</style>" +
                "</head>";
        return "<html>" + head + "<body>" + bodyHTML + "</body></html>";
    }

    /**
     * 加载html标签,开屏
     */
    private static String getHtmlDataSplash (String bodyHTML) {
        String head = "<head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width:100%; width:100%; height:100%!important;}</style>" +
                "</head>";
        return "<html>" + head + "<body>" + bodyHTML + "</body></html>";
    }

    /**
     * 加载html标签,自适应展示
     */
    private static String getHtmlData (String bodyHTML) {
        String head = "<head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width:100%; width:auto; height:auto!important;}</style>" +
                "</head>";
        return "<html>" + head + "<body>" + bodyHTML + "</body></html>";
    }

    /**
     * 加载html标签 设置固定的宽高
     */
    private String getHtmlData (String bodyHTML, int width, int height) {
        String head = "<head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> " +
                "<style>img{max-width:100%; width:" + width +
                "; height:" + height +
                "!important;}</style>" +
                "</head>";
        return "<html>" + head + "<body>" + bodyHTML + "</body></html>";
    }


    /**
     * 处理文件下载
     */
    private static class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart (String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            LogUtils.e("下载url=" + url);

            new DownloadInstaller(mActivity, url, new DownloadProgressCallBack() {
                @Override
                public void downloadProgress (int progress) {
                    //TODO:下载一半的时候数据曝光
                    if (progress >= 50) {
                    }
                }

                @Override
                public void downloadException (Exception e) {
                    LogUtils.e("下载错误=" + e.getMessage());
                }

                @Override
                public void onInstallStart () {
                    LogUtils.d("开始安装=");
                }
            }).start();
        }
    }

    /**
     * 处理各种通知、请求时间的
     */
    static class MyWebViewClient extends WebViewClient {

        @Override
        public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
            //handler.cancel(); // Android默认的处理方式
            handler.proceed();  // 接受所有网站的证书  解决https拦截问题
        }

        @Override
        public void onPageStarted (WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished (final WebView view, String url) {
            super.onPageFinished(view, url);
            //重置webview中img标签的图片大小
            //imgReset();
        }

        @RequiresApi (api = Build.VERSION_CODES.LOLLIPOP)
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest (WebView view, WebResourceRequest request) {
            String url = request.getUrl().getPath();
            //LogUtils.e("url1=" + url);
            return super.shouldInterceptRequest(view, request);

        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest (WebView view, String url) {
            //LogUtils.e("url2=" + url);
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading (WebView view, String url) {
            if (!TextUtils.isEmpty(url) && !url.startsWith("http")) {//deeplink类
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mActivity.startActivity(intent);
                Log.e("跳转deepLink=", url);
            } else if (!TextUtils.isEmpty(url) && url.contains(".apk")) {//下载类
                view.loadUrl(url);//调用webview本身的loadUrl方法
            } else {
                Log.e("跳转url=", url);
                //TODO:
                Intent intent = new Intent(mActivity, QcAdDetialActivity.class);
                intent.putExtra("url", url);
                mActivity.startActivity(intent);
            }

            return true;

        }

        @RequiresApi (api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading (WebView view, WebResourceRequest request) {
            String url = request.getUrl().getPath();
            //LogUtils.e("url3=" + url);
            return super.shouldOverrideUrlLoading(view, request);
        }
    }

    /**
     * 对图片进行重置大小，宽度就是手机屏幕宽度，高度根据宽度比便自动缩放
     * 图片进行自适应(如果js没有自适应的话)
     */
    private void imgReset () {
        mWebView.loadUrl("javascript:(function(){" +
                "var objs = document.getElementsByTagName('img'); " +
                "for(var i=0;i<objs.length;i++)  " +
                "{"
                + "var img = objs[i];   " +
                "    img.style.maxWidth = '100%'; img.style.height = 'auto';  " +
                "}" +
                "})()");
    }

    /**
     * 处理JavaScript的对话框、网站图片、网站title、加载进度等
     */
    static class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged (WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle (WebView view, String title) {
            super.onReceivedTitle(view, title);
        }
    }


}
