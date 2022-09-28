package com.corpize.sdk.mobads.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.corpize.sdk.R;
import com.corpize.sdk.mobads.bean.AppUserBean;
import com.corpize.sdk.mobads.utils.LogUtils;
import com.corpize.sdk.mobads.utils.StatusBarUtil;
import com.corpize.sdk.mobads.video.VideoAdPlayDialog;

import java.io.File;
import java.util.ArrayList;

/**
 * author ：yh
 * date : 2019-08-26 16:38
 * description : 缺少下载功能,缺少打开相机功能
 */
public class QcAdDetialActivity extends Activity {

    private String         TAG = "QcAdDetialActivity";
    private TextView       mTvTitle;
    private ImageView      mBack;
    private ImageView      mClose;
    private WebView        mWebView;
    private ProgressBar    mProgressBar;
    private TextView       mTvTagContent;
    private LinearLayout   mDialogView;
    private RelativeLayout mRlToobar;

    private String mTitle;
    private String mUrl;
    private int    mType;

    //图片上传相关
    //现在修改后的上传的参数变量
    public static ValueCallback mFilePathCallback;
    private       boolean       mIsInitiativeCancle;//是否主动点击使dialog消失

    private File              picturefile;//照相文件
    private int               mSelectPosition;
    private ArrayList<String> mStepsList;

    private static final int REQUEST_CODE_TAKE_PICETURE = 11;//拍照
    private static final int REQUEST_CODE_PICK_PHOTO    = 12;//相册


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qcad_detial);
        //设置界面
        String titleColor      = AppUserBean.getInstance().getTitleColor();
        String backgroundColor = AppUserBean.getInstance().getBackgroundColor();

        //1.使状态栏透明并使contentView填充到状态栏 2.预留出状态栏的位置，防止界面上的控件离顶部靠的太近。
        if (!TextUtils.isEmpty(backgroundColor)) {
            StatusBarUtil.setColor(this, backgroundColor);
        } else {
            StatusBarUtil.setTransparent(this);
        }

        //获取控件
        mTvTitle = (TextView) findViewById(R.id.tv_qcad_title);
        mBack = (ImageView) findViewById(R.id.iv_qcad_back);
        mClose = (ImageView) findViewById(R.id.iv_qcad_close);
        mWebView = (WebView) findViewById(R.id.web_qcad_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_qcad);
        mRlToobar = (RelativeLayout) findViewById(R.id.rl_qcad_detail_title);

        if (!TextUtils.isEmpty(titleColor)) {
            mTvTitle.setTextColor(Color.parseColor(titleColor));
        }
        if (!TextUtils.isEmpty(backgroundColor)) {
            mRlToobar.setBackgroundColor(Color.parseColor(backgroundColor));
            if (backgroundColor.equals("#FFFFFF") || backgroundColor.equals("#ffffff")) {
                mBack.setImageResource(R.drawable.qcad_left_arrow_black);
            }
        }

        //初始化数据
        initWebView();  //webview初始化
        initData();     //加载数据
        initListener(); //点击回调

    }

    //webview初始化
    public void initWebView () {
        //WebView属性设置！！！
        WebSettings settings = mWebView.getSettings();
        settings.setAllowFileAccess(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDomStorageEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setDatabaseEnabled(true);
        settings.setJavaScriptEnabled(true);

        // webview在安卓5.0之前默认允许其加载混合网络协议内容
        // 在安卓5.0之后，默认不允许加载http与https混合内容，需要设置webview允许其加载混合网络协议内容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

        // =======================Web的请求回调设置=======================
        //JS回调方法,需要自定方法的数据
        //mWebView.addJavascriptInterface(new JavaMethod(), "nativeMethod");
        //处理文件下载
        mWebView.setDownloadListener(new MyWebViewDownLoadListener());
        //处理各种通知、请求时间的
        mWebView.setWebViewClient(new MyWebViewClient());
        //处理JavaScript的对话框、网站图片、网站title、加载进度等
        mWebView.setWebChromeClient(new MyWebChromeClient());
    }

    //加载数据
    private void initData () {
        mUrl = getIntent().getStringExtra("url");
        mTitle = getIntent().getStringExtra("title");
        mType = getIntent().getIntExtra("type", 0);

        if (!TextUtils.isEmpty(mTitle)) {
            mTvTitle.setText(mTitle);
        }
        if (!TextUtils.isEmpty(mUrl)) {
            mWebView.loadUrl(mUrl);
        }
    }

    //点击回调
    private void initListener () {
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                if (mWebView.canGoBack()) {
                    mWebView.goBack();
                } else {
                    finish();
                }
            }
        });

        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View view) {
                finish();
            }
        });
    }

    // *************** START  三方方法回调 *************** /
    public class JavaMethod {

        /**
         * 跳转到app的方法
         */
        @JavascriptInterface
        public void returnNativeMethod (String type) {
            Log.e("TAG", "type:" + type);
            if ("0".equals(type)) {
                finish();
            } else if ("1".equals(type)) {
            } else if ("2".equals(type)) {
            } else if ("3".equals(type)) {
            } else if ("4".equals(type)) {
            } else if ("5".equals(type)) {
            }
        }

        /**
         * 调用改方法去发送短信
         *
         * @param phoneNumber 手机号码
         * @param message     短信内容
         **/
        @JavascriptInterface
        public void sendMessage (String phoneNumber, String message) {
            // 注册广播 发送消息
            //发送短信并且到发送短信页面
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
            intent.putExtra("sms_body", message);
            startActivity(intent);
        }

        /**
         * 协议下载(留存)
         */
        @JavascriptInterface
        public void download (final String link) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            intent.addCategory("android.intent.category.DEFAULT");
            startActivity(intent);
        }

        /**
         * 处理支付宝支付完成后的退出操作
         */
        @JavascriptInterface
        public void repdone () {
            finish();
        }

    }

    /**
     * 处理文件下载
     */
    private class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart (String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            Uri    uri    = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }

    /**
     * 处理各种通知、请求时间的
     */
    class MyWebViewClient extends WebViewClient {

        @Override
        public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
            //handler.cancel(); // Android默认的处理方式
            handler.proceed();  // 接受所有网站的证书  解决https拦截问题
        }

        @Override
        public void onPageStarted (WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished (final WebView view, String url) {
            super.onPageFinished(view, url);
            mUrl = url;
            LogUtils.e(mUrl);
            mProgressBar.setVisibility(View.GONE);

        }

        @Override
        public boolean shouldOverrideUrlLoading (WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);

        }
    }

    /**
     * 处理JavaScript的对话框、网站图片、网站title、加载进度等
     */
    class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged (WebView view, int newProgress) {
            mProgressBar.setProgress(newProgress);

            if (mWebView.canGoBack()) {
                mClose.setVisibility(View.VISIBLE);
            } else {
                mClose.setVisibility(View.GONE);
            }
        }

        @Override
        public void onReceivedTitle (WebView view, String title) {
            super.onReceivedTitle(view, title);

            // 正常情况下，如果没有传入title，我们则使用h5的title。
            // 当是点推送消息进来的时候,getIntent().getStringExtra("title")会拿到推送传入的乱码般的title字符串。
            if (TextUtils.isEmpty(getIntent().getStringExtra("title"))) {
                mTitle = title;
                mTvTitle.setText(mTitle);
            }

        }

        //android 系统版本 < 3.0，8(Android 2.2) <= API <= 10(Android 2.3)回调此方法
        public void openFileChooser (ValueCallback<Uri> valueCallback) {
            Log.d(TAG, "--------调用openFileChooser<3.0");
            mFilePathCallback = valueCallback;

            takePhotoPicture();
        }

        //android 系统版本 3.0+，11(Android 3.0) <= API <= 15(Android 4.0.3)回调此方法
        public void openFileChooser (ValueCallback valueCallback, String acceptType) {
            Log.d(TAG, "--------调用openFileChooser3.0+");
            mFilePathCallback = valueCallback;

            takePhotoPicture();
        }

        // android 系统版本 > 4.1(4.4.1和4.4.2无此方法)，16(Android 4.1.2) <= API <= 20(Android 4.4W.2)回调此方法
        public void openFileChooser (ValueCallback<Uri> valueCallback, String acceptType, String capture) {
            Log.d(TAG, "--------调用openFileChooser>4.1.1");
            mFilePathCallback = valueCallback;

            takePhotoPicture();
        }

        // android 系统版本 > 5.0，API >= 21(Android 5.0.1)回调此方法
        @Override
        public boolean onShowFileChooser (WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {//5.0+
            Log.d(TAG, "--------调用openFileChooser>5.0");
            mFilePathCallback = filePathCallback;

            takePhotoPicture();
            return true;
        }
    }

    //TODO:拍照和相册选项
    private void takePhotoPicture () {
    }

    @Override
    public void onBackPressed () {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            finish();
        }

    }

    @Override
    protected void onResume () {
        super.onResume();
    }

    @Override
    protected void onStop () {
        super.onStop();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();

        if (mWebView != null) {
            mWebView.clearHistory();
            mWebView.clearCache(true);
            mWebView.destroy();
        }

        if (mType == 1) {
            VideoAdPlayDialog.getInstance().activityLifecycle();
        }

    }

    @Override
    public void finish () {
        super.finish();
        if (mType == 1) {
            VideoAdPlayDialog.getInstance().restartVieo();
        }
    }
}
