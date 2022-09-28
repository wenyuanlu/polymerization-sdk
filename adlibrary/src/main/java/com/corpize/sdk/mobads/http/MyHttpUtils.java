package com.corpize.sdk.mobads.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import com.corpize.sdk.mobads.common.CommonUtils;
import com.corpize.sdk.mobads.common.Constants;
import com.corpize.sdk.mobads.http.callback.BaseCallback;
import com.corpize.sdk.mobads.http.util.ApiException;
import com.corpize.sdk.mobads.http.util.ErrorCustom;
import com.corpize.sdk.mobads.http.util.HttpLogUtil;
import com.corpize.sdk.mobads.utils.AESUtil;
import com.corpize.sdk.mobads.utils.DeviceUtil;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * author: yh
 * date: 2019-11-28 11:05
 * description: TODO:原生网络请求封装,减少网络请求的大小,返回数据处于子线程
 */
public class MyHttpUtils {

    private static final int                 TIMEOUT_CONNECT_MILLIONS = 2000;//连接超时时间
    private static final int                 TIMEOUT_READ_MILLIONS    = 3000;//上传超时时间
    private static final int                 TIMEOUT_WRITE_MILLIONS   = 3000;//返回超时时间
    private              int                 mRequestType             = 1;  //1是get,2是post
    private              String              mUrl                     = ""; //请求的url
    private static       MyHttpUtils         sMyHttpUtils;
    private static       Map<String, Object> mParmMap                 = new HashMap<>();
    private              String              mJsonParam               = "";//传递的Json数据
    private              boolean             mIsEncrypt               = false;//是否开启请求加密
    private              String              mToken                   = "";//动态加密的token
    private              List<String>        mResponseLogList         = new ArrayList<>();
    private              List<String>        mRequestLogList          = new ArrayList<>();

    private MyHttpUtils () {
        mParmMap = new HashMap<>();
        mResponseLogList = new ArrayList<>();
        mRequestLogList = new ArrayList<>();
    }

    /**
     * 异步的Get请求
     */
    public static MyHttpUtils getAsyn (String url) {
        sMyHttpUtils = new MyHttpUtils();
        sMyHttpUtils.setRequestType(1);
        sMyHttpUtils.setUrl(url);
        return sMyHttpUtils;
    }

    /**
     * 异步的post请求
     */
    public static MyHttpUtils postAsyn (String url) {
        sMyHttpUtils = new MyHttpUtils();
        sMyHttpUtils.setRequestType(2);
        sMyHttpUtils.setUrl(url);
        return sMyHttpUtils;
    }

    /**
     * 异步的post请求
     */
    public static MyHttpUtils postAsynFile (String url) {
        sMyHttpUtils = new MyHttpUtils();
        sMyHttpUtils.setRequestType(3);
        sMyHttpUtils.setUrl(url);
        return sMyHttpUtils;
    }

    /**
     * 添加请求的参数(get请求和post上传非json参数请求)
     */
    public MyHttpUtils addParams (String key, Object value) {
        mParmMap.put(key, value);
        return sMyHttpUtils;
    }

    /**
     * 添加请求的参数,(post上传json参数请求)
     */
    public MyHttpUtils content (String jsonParam) {
        mJsonParam = jsonParam;
        return sMyHttpUtils;
    }

    /**
     * 进行加密,固定Token加密
     */
    public MyHttpUtils encrypt () {
        mIsEncrypt = true;
        return sMyHttpUtils;
    }

    /**
     * 进行加密,动态加密
     */
    public MyHttpUtils encrypt (String token) {
        mIsEncrypt = true;
        mToken = token;
        return sMyHttpUtils;
    }

    /**
     * 开始请求及返回数据的回调
     */
    public void execute (final BaseCallback callback) {
        new Thread() {
            public void run () {
                if (mRequestType == 1) {//get请求
                    doGet(mUrl, callback);
                } else if (mRequestType == 2) {//post请求
                    doPostJson(mUrl, callback);
                } else if (mRequestType == 3) {
                    //postFile();
                }
            }
        }.start();
    }

    /**
     * 设置请求的类型,1是get,2是post
     */
    private void setRequestType (int requestType) {
        mRequestType = requestType;
    }

    /**
     * 设置请求的url
     */
    private void setUrl (String url) {
        mUrl = url;
    }

    /**
     * Get请求，获得返回数据
     */
    private void doGet (String urlStr, final BaseCallback callback) {
        String                urlFinal    = urlStr + getParams();//get请求的参数拼接
        HttpURLConnection     conn        = null;
        InputStream           inputStream = null;
        ByteArrayOutputStream baos        = null;
        String                result      = "";
        showRequestLog(urlFinal, "GET", null);//请求日志显示

        try {
            // String encodURL = URLEncoder.encode(urlStr, "UTF-8");
            String encodURL = Uri.encode(urlFinal);
            encodURL = encodURL.replaceAll("%3A", ":");
            encodURL = encodURL.replaceAll("%2F", "/");
            encodURL = encodURL.replaceAll("%3F", "?");
            encodURL = encodURL.replaceAll("%26", "&");
            encodURL = encodURL.replaceAll("%3D", "=");
            URL url = new URL(encodURL);

            //开始请求
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIMEOUT_READ_MILLIONS);
            conn.setConnectTimeout(TIMEOUT_CONNECT_MILLIONS);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            //TODO:
            String ua = DeviceUtil.getUserAgent(CommonUtils.get());
            if(!TextUtils.isEmpty(ua)){
                conn.setRequestProperty("User-Agent", ua);
            }


            //返回数据
            int    responseCode = conn.getResponseCode();
            String contentType  = conn.getContentType();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                int    len = -1;
                byte[] buf = new byte[128];
                while ((len = inputStream.read(buf)) != -1) {
                    baos.write(buf, 0, len);
                }
                baos.flush();

                result = baos.toString();
                //返回日志的显示
                showResponseLog(urlFinal, responseCode, result, contentType);
                if (callback != null) {
                    if (mIsEncrypt) {//如果是加密,需要解密
                        if (TextUtils.isEmpty(mToken)) {
                            result = AESUtil.decrypt(result);
                        } else {
                            result = AESUtil.decrypt(result, mToken);
                        }
                    }
                    Object response = callback.parseNetworkResponse(result);
                    callback.onResponse(response);
                }

            } else {
                result = conn.getResponseMessage();
                //错误返回处理
                sendFailResultCallback(urlFinal, new ApiException(result, responseCode), responseCode, contentType, callback);
            }


        } catch (Exception e) {
            //错误返回处理
            sendFailResultCallback(urlFinal, e, QcErrorCode.NET_ERROR_NO_CONNECT.errorCode, null, callback);
            if (!Constants.IS_SHOW_LOG) {
                e.printStackTrace();
            }

        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (conn != null) {
                conn.disconnect();
            }
        }

    }

    /**
     * 向指定 URL 发送POST方法的请求,传递Json数据
     */
    private void doPostJson (String url, final BaseCallback callback) {
        BufferedReader    reader = null;
        String            result = "";
        HttpURLConnection conn   = null;
        showRequestLog(url, "POST", mJsonParam);//请求日志显示
        if (mIsEncrypt) {//数据加密
            if (TextUtils.isEmpty(mToken)) {
                mJsonParam = AESUtil.encrypt(mJsonParam);
            } else {
                mJsonParam = AESUtil.encrypt(mJsonParam, mToken);
            }
            HttpLogUtil.d("发送的加密数据=" + mJsonParam);

        }

        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            conn = (HttpURLConnection) realUrl.openConnection();
            // 设置通用的请求属性
            conn.setDoOutput(true); // 发送POST请求必须设置
            conn.setDoInput(true);  // 发送POST请求必须设置
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setInstanceFollowRedirects(true);
            conn.setReadTimeout(TIMEOUT_CONNECT_MILLIONS);
            conn.setConnectTimeout(TIMEOUT_CONNECT_MILLIONS);
            conn.setRequestProperty("accept", "*/*");//接收所有类型
            //conn.setRequestProperty("accept", "application/json");//接收json类型
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            //conn.setRequestProperty("Content-Length", String.valueOf(mJsonParam.length()));//设置发送数据长度（用于发送大量数据使用）
            conn.setRequestProperty("connection", "Keep-Alive");//长连接
            //TODO:
            String ua = DeviceUtil.getUserAgent(CommonUtils.get());
            if(!TextUtils.isEmpty(ua)){
                conn.setRequestProperty("User-Agent", ua);
            }

            //一定要用BufferedReader 来接收响应， 使用字节来接收响应的方法是接收不到内容的
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8"); // utf-8编码
            out.append(mJsonParam);
            out.flush();
            out.close();

            //返回数据
            int    responseCode = conn.getResponseCode();
            String contentType  = conn.getContentType();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    result += line;
                }

                //如果是加密,需要解密
                if (mIsEncrypt) {
                    if (TextUtils.isEmpty(mToken)) {
                        result = AESUtil.decrypt(result);
                    } else {
                        result = AESUtil.decrypt(result, mToken);
                    }
                }

                //返回日志的显示
                showResponseLog(url, responseCode, result, contentType);

                //回调返回
                if (callback != null) {
                    Object response = callback.parseNetworkResponse(result);
                    callback.onResponse(response);
                }

            } else {
                result = conn.getResponseMessage();
                //错误返回处理
                sendFailResultCallback(url, new ApiException(result, responseCode), responseCode, contentType, callback);
            }


        } catch (Exception e) {
            //错误返回处理
            sendFailResultCallback(url, e, QcErrorCode.NET_ERROR_NO_CONNECT.errorCode, null, callback);
            if (!Constants.IS_SHOW_LOG) {
                e.printStackTrace();
            }

        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }

    }

    /**
     * 向指定 URL 发送POST方法的请求
     */
    private void doPost (String url, final BaseCallback callback) {
        String         param  = getParams();//Post请求参数拼接 ( name1=value1&name2=value2 )
        PrintWriter    out    = null;
        BufferedReader in     = null;
        String         result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setUseCaches(false);
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setReadTimeout(TIMEOUT_CONNECT_MILLIONS);
            conn.setConnectTimeout(TIMEOUT_CONNECT_MILLIONS);

            if (param != null && !param.trim().equals("")) {
                // 获取URLConnection对象对应的输出流
                out = new PrintWriter(conn.getOutputStream());
                // 发送请求参数
                out.print(param);
                // flush输出流的缓冲
                out.flush();
            }

            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }

            if (result.equals("{}")) {
                result = conn.getResponseCode() + "";
            }

            if (callback != null) {
                Object response = callback.parseNetworkResponse(result);
                callback.onResponse(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            HttpLogUtil.e(e.toString());

        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        if (result == null && result.equals("")) {
            HttpLogUtil.e("result为空");
        } else {
            HttpLogUtil.e(result);
        }

    }

    /**
     * 使用Post上传文件 (单文件 多文件 可用)
     */
    public static void postFile (String url, Map<String, String> params, Map<String, File> files) {

        String            BOUNDARY  = java.util.UUID.randomUUID().toString(); //定义数据分隔线
        String            PREFIX    = "--";      //边界前缀
        String            LINEND    = "\r\n";    // 换行符
        DataOutputStream  outStream = null;
        HttpURLConnection conn      = null;
        try {
            URL uri = new URL(url);
            conn = (HttpURLConnection) uri.openConnection();
            conn.setReadTimeout(TIMEOUT_CONNECT_MILLIONS);
            conn.setConnectTimeout(TIMEOUT_CONNECT_MILLIONS);
            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setUseCaches(false);
            conn.setRequestMethod("POST"); // Post方式
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + BOUNDARY);
            //TODO:
            String ua = DeviceUtil.getUserAgent(CommonUtils.get());
            if(!TextUtils.isEmpty(ua)){
                conn.setRequestProperty("User-Agent", ua);
            }

            // 首先组拼文本类型的参数
            StringBuilder sbText = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sbText.append(PREFIX);
                sbText.append(BOUNDARY);
                sbText.append(LINEND);
                sbText.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
                sbText.append("Content-Type: text/plain; charset=UTF-8" + LINEND);
                sbText.append("Content-Transfer-Encoding: 8bit" + LINEND);
                sbText.append(LINEND);
                sbText.append(entry.getValue());
                sbText.append(LINEND);
            }

            // 上传文本参数数据
            outStream = new DataOutputStream(conn.getOutputStream());
            outStream.write(sbText.toString().getBytes());

            // 上传文件数据
            if (files != null)
                for (Map.Entry<String, File> file : files.entrySet()) {
                    StringBuilder sbFile = new StringBuilder();
                    sbFile.append(PREFIX);
                    sbFile.append(BOUNDARY);
                    sbFile.append(LINEND);
                    sbFile.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getKey() + "\"" + LINEND);
                    sbFile.append("Content-Type: application/octet-stream; charset=UTF-8" + LINEND);
                    sbFile.append(LINEND);
                    outStream.write(sbFile.toString().getBytes());
                    InputStream is     = new FileInputStream(file.getValue());
                    byte[]      buffer = new byte[1024];
                    int         len    = 0;
                    while ((len = is.read(buffer)) != -1) {
                        outStream.write(buffer, 0, len);
                    }

                    is.close();
                    outStream.write(LINEND.getBytes());
                }

            // 请求结束标志
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
            outStream.write(end_data);// 写上结尾标识
            outStream.flush();

            // 得到响应码
            int               code      = conn.getResponseCode();
            InputStream       in        = conn.getInputStream();
            InputStreamReader isReader  = new InputStreamReader(in);
            BufferedReader    bufReader = new BufferedReader(isReader);
            String            line      = null;
            String            data      = "OK";
            while ((line = bufReader.readLine()) == null) {
                data += line;
            }
            HttpLogUtil.d("返回的响应数据=" + data);

            if (code == 200) {
                int           ch;
                StringBuilder sb2 = new StringBuilder();
                while ((ch = in.read()) != -1) {
                    sb2.append((char) ch);
                }

            } else {
                //result = conn.getResponseMessage();
                //错误返回处理
                // sendFailResultCallback(url, new ApiException(result, responseCode), responseCode, contentType, callback);
            }

            //return in.toString();

        } catch (Exception e) {
            //错误返回处理
            //sendFailResultCallback(url, e, QcErrorCode.NET_ERROR_NO_CONNECT.errorCode, null, callback);
            if (!Constants.IS_SHOW_LOG) {
                e.printStackTrace();
            }
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
                if (conn != null) {
                    conn.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    /**
     * 获取网络图片资源
     */
    public Bitmap getHttpBitmap (String url) {
        URL    myFileURL;
        Bitmap bitmap = null;
        try {
            myFileURL = new URL(url);
            // 获得连接
            HttpURLConnection conn = (HttpURLConnection) myFileURL.openConnection();
            // 设置超时时间为6000毫秒，conn.setConnectionTiem(0);表示没有时间限制
            conn.setConnectTimeout(6000);
            // 连接设置获得数据流
            conn.setDoInput(true);
            // 不使用缓存
            conn.setUseCaches(false);
            // 这句可有可无，没有影响
            // conn.connect();
            // 得到数据流
            InputStream is = conn.getInputStream();
            // 解析得到图片
            bitmap = BitmapFactory.decodeStream(is);
            // 关闭数据流
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return bitmap;

    }

    /**
     * 设置请求的参数 ( 区分 get 和 post )
     */
    private String getParams () {
        String paramsText = "";
        int    count      = 1;
        // 获取所有键值对对象的集合
        Set<Map.Entry<String, Object>> set = mParmMap.entrySet();
        // 遍历键值对对象的集合，得到每一个键值对对象
        for (Map.Entry<String, Object> me : set) {
            // 根据键值对对象获取键和值
            String key   = me.getKey();
            Object value = me.getValue();

            if (count == 1) {
                if (mRequestType == 2) {    //post请求
                    paramsText = key + "=" + value;
                } else {                    //get请求
                    paramsText = "?" + key + "=" + value;
                }
            } else {
                paramsText = paramsText + "&" + key + "=" + value;
            }

            count++;
        }

        return paramsText;
    }

    /**
     * 错误处理的方法
     */
    public void sendFailResultCallback (String url, final Exception e, int code, String type, BaseCallback callback) {
        //自定义的返回错误
        Exception exception = ErrorCustom.checkError(e);
        //返回日志的显示
        if (code != QcErrorCode.NET_ERROR_NO_CONNECT.errorCode) {//不是连接失败
            showResponseLog(url, code, e.getMessage(), type);
        }
        //回调
        if (callback != null) {
            callback.onError(code, exception);
        }
    }

    /**
     * 展示请求的log日志
     */
    private void showRequestLog (String url, String type, String content) {
        if (mRequestLogList == null) {
            mRequestLogList = new ArrayList<>();
        }
        mRequestLogList.clear();
        mRequestLogList.add("======== Request Start =======");
        mRequestLogList.add(" method : " + type);
        mRequestLogList.add(" url : " + url);
        if (!TextUtils.isEmpty(content)) {
            mRequestLogList.add(" content : " + content);
        }
        mRequestLogList.add(" ======= Request End =========");
        mRequestLogList.add("============================================================");

        for (String log : mRequestLogList) {
            HttpLogUtil.e(log);
        }
    }

    /**
     * 展示返回的log日志
     */
    private void showResponseLog (String url, int code, String content, String contentType) {
        if (mResponseLogList == null) {
            mResponseLogList = new ArrayList<>();
        }
        mResponseLogList.clear();
        mResponseLogList.add("======== Response Start =======");
        mResponseLogList.add(" url : " + url);
        mResponseLogList.add(" code : " + code);
        mResponseLogList.add(" contentType : " + contentType);
        mResponseLogList.add(" content     : " + content);
        mResponseLogList.add(" ======= Response End =========");
        mResponseLogList.add("============================================================");

        for (String log : mResponseLogList) {
            HttpLogUtil.e(log);
        }
    }


}
