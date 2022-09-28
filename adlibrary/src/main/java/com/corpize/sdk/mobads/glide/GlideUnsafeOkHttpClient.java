package com.corpize.sdk.mobads.glide;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class GlideUnsafeOkHttpClient {

    private static final int CONNECT_TIME_OUT = 60;
    private static final int READ_TIME_OUT    = 60;
    private static final int WRITE_TIME_OUT   = 60;

    //自定义工具类修改OkHttpClient证书和超时时间
    public static OkHttpClient getUnsafeOkHttpClient () {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.connectTimeout(CONNECT_TIME_OUT, TimeUnit.SECONDS);
        builder.readTimeout(READ_TIME_OUT, TimeUnit.SECONDS);
        builder.writeTimeout(WRITE_TIME_OUT, TimeUnit.SECONDS);

        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                return builder.build();
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
            builder.sslSocketFactory(getSSLSocketFactory(), trustManager);
            builder.hostnameVerifier(getHostnameVerifier());
            return builder.build();
        } catch (Exception e) {
            return builder.build();
        }
    }

    /**
     * getSSLSocketFactory、getTrustManagers、getHostnameVerifier
     * 使OkHttpClient支持自签名证书，避免Glide加载不了Https图片
     */
    private static SSLSocketFactory getSSLSocketFactory () {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, getTrustManagers(), new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static TrustManager[] getTrustManagers () {
        return new TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted (X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted (X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers () {
                return new X509Certificate[]{};
            }
        }};
    }

    private static HostnameVerifier getHostnameVerifier () {
        return new HostnameVerifier() {
            @Override
            public boolean verify (String hostname, SSLSession session) {
                return true; // 直接返回true，默认verify通过
            }
        };
    }
}
